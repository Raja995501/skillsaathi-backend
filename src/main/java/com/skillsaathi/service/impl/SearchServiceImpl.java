package com.skillsaathi.service.impl;

import com.skillsaathi.dto.match.MatchResponse;
import com.skillsaathi.dto.search.UserSearchResultResponse;
import com.skillsaathi.dto.skill.UserSkillResponse;
import com.skillsaathi.entity.User;
import com.skillsaathi.entity.UserSkill;
import com.skillsaathi.entity.enums.OnlinePreference;
import com.skillsaathi.entity.enums.SkillType;
import com.skillsaathi.exception.BadRequestException;
import com.skillsaathi.repository.UserRepository;
import com.skillsaathi.repository.UserSkillRepository;
import com.skillsaathi.service.MatchService;
import com.skillsaathi.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final MatchService matchService;

    @Override
    public List<UserSearchResultResponse> searchUsers(String city, String state, Long categoryId,
                                                      String onlinePreference, boolean sortByMatch,
                                                      Long currentUserId, int page, int size) {

        if (sortByMatch && currentUserId == null) {
            throw new BadRequestException("You must be logged in to sort by match score");
        }

        OnlinePreference onlinePref = parseOnlinePreference(onlinePreference);
        Long excludeId = currentUserId != null ? currentUserId : -1L;

        List<User> candidates = userRepository.searchUsersAdvanced(city, state, categoryId, onlinePref, excludeId);

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> sortedCandidates;
        Map<Long, MatchResponse> matchByUserId;

        if (sortByMatch) {
            // Build lookup of matchPercentage
            matchByUserId = matchService.getMatches(currentUserId).stream()
                    .collect(Collectors.toMap(MatchResponse::getUserId, m -> m, (existing, replacement) -> existing));

            sortedCandidates = candidates.stream()
                    .sorted(Comparator.comparing(
                            (User u) -> {
                                MatchResponse m = matchByUserId.get(u.getId());
                                return (m != null && m.getMatchPercentage() != null) ? m.getMatchPercentage() : BigDecimal.ZERO;
                            }
                    ).reversed())
                    .toList();
        } else {
            matchByUserId = Collections.emptyMap();
            sortedCandidates = candidates;
        }

        // Apply pagination in-memory as per scaling architecture
        int fromIndex = Math.min(page * size, sortedCandidates.size());
        int toIndex = Math.min(fromIndex + size, sortedCandidates.size());
        List<User> paginatedUsers = sortedCandidates.subList(fromIndex, toIndex);

        if (paginatedUsers.isEmpty()) {
            return Collections.emptyList();
        }

        // Batch fetch skills ONLY for paginated result set to avoid N+1 queries
        List<Long> userIds = paginatedUsers.stream().map(User::getId).toList();
        Map<Long, List<UserSkill>> skillsByUserId = userSkillRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(s -> s.getUser().getId()));

        return paginatedUsers.stream()
                .map(u -> toResponse(u, matchByUserId.get(u.getId()), skillsByUserId.getOrDefault(u.getId(), Collections.emptyList())))
                .toList();
    }

    private UserSearchResultResponse toResponse(User user, MatchResponse matchInfo, List<UserSkill> skills) {
        return UserSearchResultResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .city(user.getCity())
                .state(user.getState())
                .onlinePreference(user.getOnlinePreference() != null ? user.getOnlinePreference().name() : null)
                .averageRating(user.getAverageRating())
                .skillsToTeach(mapSkills(skills, SkillType.TEACH))
                .skillsToLearn(mapSkills(skills, SkillType.LEARN))
                .matchPercentage(matchInfo != null ? matchInfo.getMatchPercentage() : null)
                .build();
    }

    private List<UserSkillResponse> mapSkills(List<UserSkill> skills, SkillType type) {
        return skills.stream()
                .filter(s -> s.getType() == type)
                .map(s -> UserSkillResponse.builder()
                        .id(s.getId())
                        .skillId(s.getSkill().getId())
                        .skillName(s.getSkill().getName())
                        .categoryName(s.getSkill().getCategory().getName())
                        .type(s.getType().name())
                        .level(s.getLevel() != null ? s.getLevel().name() : null)
                        .build())
                .toList();
    }

    private OnlinePreference parseOnlinePreference(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return OnlinePreference.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid onlinePreference value: " + value);
        }
    }
}