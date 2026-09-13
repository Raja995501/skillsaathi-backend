package com.skillsaathi.service;

import com.skillsaathi.dto.match.MatchResponse;

import java.util.List;

public interface MatchService {
    /** All viable matches for the current user, sorted by match percentage descending. */
    List<MatchResponse> getMatches(Long userId);

    /** Match detail against one specific user (used by "View 96% Match" style screens). */
    MatchResponse getMatchDetail(Long userId, Long otherUserId);
}
