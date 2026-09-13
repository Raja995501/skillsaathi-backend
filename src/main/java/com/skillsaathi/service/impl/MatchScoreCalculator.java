package com.skillsaathi.service.impl;

import com.skillsaathi.entity.User;
import com.skillsaathi.entity.UserSkill;
import com.skillsaathi.entity.enums.SkillLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Pure scoring logic for the matching engine, kept separate from MatchServiceImpl
 * (which handles candidate fetching/DB access) so the algorithm itself is easy to
 * unit-test and tune independently of persistence concerns.
 *
 * Score breakdown (100 points total):
 *   - Skill match (both directions combined): 60 pts  -> 30 pts per direction,
 *     scaled by min(matchedSkillCount, 3) / 3 so a handful of overlapping skills
 *     already gets full credit for that direction (adding a 4th+ doesn't inflate further).
 *   - Location: 20 pts        -> same city = 20, same state only = 10, neither = 0
 *   - Online preference: 10 pts -> compatible (equal, or either side is BOTH) = 10, else 0
 *   - Teaching quality: 10 pts -> based on the average proficiency level of the skills
 *     the OTHER user teaches that the current user wants to learn (a higher-level
 *     teacher is more valuable to a learner)
 */
@Component
public class MatchScoreCalculator {

    private static final int MAX_SKILLS_FOR_FULL_CREDIT = 3;

    public record ScoreResult(BigDecimal percentage, String reason) {}

    public ScoreResult score(User currentUser,
                              User otherUser,
                              List<UserSkill> otherTeachesThatCurrentWantsToLearn,
                              List<UserSkill> otherWantsToLearnThatCurrentTeaches) {

        double skillScore = skillMatchScore(otherTeachesThatCurrentWantsToLearn, otherWantsToLearnThatCurrentTeaches);
        double locationScore = locationScore(currentUser, otherUser);
        double onlineScore = onlinePreferenceScore(currentUser, otherUser);
        double levelScore = teachingQualityScore(otherTeachesThatCurrentWantsToLearn);

        double total = skillScore + locationScore + onlineScore + levelScore;
        BigDecimal percentage = BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);

        String reason = buildReason(otherUser, otherTeachesThatCurrentWantsToLearn,
                otherWantsToLearnThatCurrentTeaches, currentUser, otherUser);

        return new ScoreResult(percentage, reason);
    }

    private double skillMatchScore(List<UserSkill> otherTeaches, List<UserSkill> otherLearns) {
        double aToB = 30.0 * Math.min(otherTeaches.size(), MAX_SKILLS_FOR_FULL_CREDIT) / MAX_SKILLS_FOR_FULL_CREDIT;
        double bToA = 30.0 * Math.min(otherLearns.size(), MAX_SKILLS_FOR_FULL_CREDIT) / MAX_SKILLS_FOR_FULL_CREDIT;
        return aToB + bToA;
    }

    private double locationScore(User a, User b) {
        if (a.getCity() != null && a.getCity().equalsIgnoreCase(safe(b.getCity()))) {
            return 20.0;
        }
        if (a.getState() != null && a.getState().equalsIgnoreCase(safe(b.getState()))) {
            return 10.0;
        }
        return 0.0;
    }

    private double onlinePreferenceScore(User a, User b) {
        if (a.getOnlinePreference() == null || b.getOnlinePreference() == null) {
            return 0.0;
        }
        boolean compatible = a.getOnlinePreference() == b.getOnlinePreference()
                || a.getOnlinePreference().name().equals("BOTH")
                || b.getOnlinePreference().name().equals("BOTH");
        return compatible ? 10.0 : 0.0;
    }

    private double teachingQualityScore(List<UserSkill> otherTeaches) {
        if (otherTeaches.isEmpty()) {
            return 0.0;
        }
        double avgLevelWeight = otherTeaches.stream()
                .map(UserSkill::getLevel)
                .filter(Objects::nonNull)
                .mapToInt(this::levelWeight)
                .average()
                .orElse(1); // no level specified -> treat as baseline/beginner-ish

        // levelWeight ranges 1..4 (BEGINNER..EXPERT) -> scale to 0..10
        return (avgLevelWeight / 4.0) * 10.0;
    }

    private int levelWeight(SkillLevel level) {
        return switch (level) {
            case BEGINNER -> 1;
            case INTERMEDIATE -> 2;
            case ADVANCED -> 3;
            case EXPERT -> 4;
        };
    }

    private String buildReason(User other, List<UserSkill> otherTeaches, List<UserSkill> otherLearns,
                                User currentUser, User otherUserForName) {
        StringBuilder sb = new StringBuilder();

        if (!otherLearns.isEmpty()) {
            String skillNames = joinSkillNames(otherLearns);
            sb.append("You teach ").append(skillNames)
              .append(" and ").append(other.getName()).append(" wants to learn that. ");
        }
        if (!otherTeaches.isEmpty()) {
            String skillNames = joinSkillNames(otherTeaches);
            sb.append(other.getName()).append(" teaches ").append(skillNames)
              .append(" which you want to learn.");
        }
        if (sb.isEmpty()) {
            sb.append("No direct skill overlap yet, but you're both in a similar location/category.");
        }
        return sb.toString().trim();
    }

    private String joinSkillNames(List<UserSkill> skills) {
        return skills.stream()
                .map(us -> us.getSkill().getName())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
