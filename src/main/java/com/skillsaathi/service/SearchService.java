package com.skillsaathi.service;

import com.skillsaathi.dto.search.UserSearchResultResponse;

import java.util.List;

public interface SearchService {
    /**
     * @param currentUserId nullable — pass null for unauthenticated/public search.
     *                       Required (non-null) when sortByMatch=true.
     */
    List<UserSearchResultResponse> searchUsers(String city, String state, Long categoryId,
                                               String onlinePreference, boolean sortByMatch,
                                               Long currentUserId, int page, int size);
}