package com.skillsaathi.service.impl;

import com.skillsaathi.dto.match.MatchResponse;
import com.skillsaathi.entity.User;
import com.skillsaathi.entity.UserSkill;
import com.skillsaathi.entity.enums.SkillType;
import com.skillsaathi.exception.ResourceNotFoundException;
import com.skillsaathi.repository.UserRepository;
import com.skillsaathi.repository.UserSkillRepository;
import com.skillsaathi.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Candidate generation strategy:
 *   1. Fetch what the current user teaches and wants to learn (small lists, indexed lookups).
 *   2. Two indexed queries find everyone who (a) teaches something the current user wants to
 *      learn, and (b) wants to learn something the current user teaches.
 *   3. Union of those user IDs = candidate pool. Anyone with ZERO overlap in either direction
 *      never becomes a candidate at all, so we never score the entire user base — only people
 *      with at least one real skill connection point.
 *
 * This keeps the expensive part (candidate discovery) as two indexed DB queries rather than an
 * O(all users) scan; only the (usually much smaller) candidate set gets full scoring in Java.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchServiceImpl implements MatchService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final MatchScoreCalculator scoreCalculator;

    @Override
    public List<MatchResponse> getMatches(Long userId) {
        User currentUser = getUserOrThrow(userId);

        List<UserSkill> myTeach = userSkillRepository.findByUserIdAndType(userId, SkillType.TEACH);
        List<UserSkill> myLearn = userSkillRepository.findByUserIdAndType(userId, SkillType.LEARN);

        List<Long> myTeachSkillIds = myTeach.stream().map(us -> us.getSkill().getId()).distinct().toList();
        List<Long> myLearnSkillIds = myLearn.stream().map(us -> us.getSkill().getId()).distinct().toList();

        // Direction A: others who TEACH something I want to LEARN
        List<UserSkill> othersTeachingWhatIWantToLearn = myLearnSkillIds.isEmpty()
                ? List.of()
                : userSkillRepository.findBySkillIdInAndType(myLearnSkillIds, SkillType.TEACH);

        // Direction B: others who want to LEARN something I TEACH
        List<UserSkill> othersLearningWhatITeach = myTeachSkillIds.isEmpty()
                ? List.of()
                : userSkillRepository.findBySkillIdInAndType(myTeachSkillIds, SkillType.LEARN);

        Map<Long, List<UserSkill>> teachByCandidate = othersTeachingWhatIWantToLearn.stream()
                .filter(us -> !us.getUser().getId().equals(userId))
                .collect(Collectors.groupingBy(us -> us.getUser().getId()));

        Map<Long, List<UserSkill>> learnByCandidate = othersLearningWhatITeach.stream()
                .filter(us -> !us.getUser().getId().equals(userId))
                .collect(Collectors.groupingBy(us -> us.getUser().getId()));

        Set<Long> candidateIds = new HashSet<>();
        candidateIds.addAll(teachByCandidate.keySet());
        candidateIds.addAll(learnByCandidate.keySet());

        List<MatchResponse> results = new ArrayList<>();
        for (Long candidateId : candidateIds) {
            User candidate = userRepository.findById(candidateId).orElse(null);
            if (candidate == null || candidate.isBlocked()) continue;

            List<UserSkill> otherTeaches = teachByCandidate.getOrDefault(candidateId, List.of());
            List<UserSkill> otherLearns = learnByCandidate.getOrDefault(candidateId, List.of());

            MatchScoreCalculator.ScoreResult result = scoreCalculator.score(
                    currentUser, candidate, otherTeaches, otherLearns);

            results.add(toResponse(candidate, result, otherTeaches, otherLearns));
        }

        results.sort(Comparator.comparing(MatchResponse::getMatchPercentage).reversed());
        return results;
    }

    @Override
    public MatchResponse getMatchDetail(Long userId, Long otherUserId) {
        User currentUser = getUserOrThrow(userId);
        User otherUser = getUserOrThrow(otherUserId);

        List<UserSkill> myLearn = userSkillRepository.findByUserIdAndType(userId, SkillType.LEARN);
        List<UserSkill> myTeach = userSkillRepository.findByUserIdAndType(userId, SkillType.TEACH);

        Set<Long> myLearnSkillIds = myLearn.stream().map(us -> us.getSkill().getId()).collect(Collectors.toSet());
        Set<Long> myTeachSkillIds = myTeach.stream().map(us -> us.getSkill().getId()).collect(Collectors.toSet());

        List<UserSkill> otherTeaches = userSkillRepository.findByUserIdAndType(otherUserId, SkillType.TEACH)
                .stream().filter(us -> myLearnSkillIds.contains(us.getSkill().getId())).toList();

        List<UserSkill> otherLearns = userSkillRepository.findByUserIdAndType(otherUserId, SkillType.LEARN)
                .stream().filter(us -> myTeachSkillIds.contains(us.getSkill().getId())).toList();

        MatchScoreCalculator.ScoreResult result = scoreCalculator.score(
                currentUser, otherUser, otherTeaches, otherLearns);

        return toResponse(otherUser, result, otherTeaches, otherLearns);
    }

    private MatchResponse toResponse(User candidate, MatchScoreCalculator.ScoreResult result,
                                      List<UserSkill> otherTeaches, List<UserSkill> otherLearns) {
        return MatchResponse.builder()
                .userId(candidate.getId())
                .name(candidate.getName())
                .profilePictureUrl(candidate.getProfilePictureUrl())
                .city(candidate.getCity())
                .state(candidate.getState())
                .averageRating(candidate.getAverageRating())
                .matchPercentage(result.percentage())
                .matchReason(result.reason())
                .theyTeachThatYouWantToLearn(otherTeaches.stream().map(us -> us.getSkill().getName()).distinct().toList())
                .theyWantToLearnThatYouTeach(otherLearns.stream().map(us -> us.getSkill().getName()).distinct().toList())
                .build();
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
