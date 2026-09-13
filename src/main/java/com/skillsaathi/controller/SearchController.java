package com.skillsaathi.controller;

import com.skillsaathi.dto.common.ApiResponse;
import com.skillsaathi.dto.search.UserSearchResultResponse;
import com.skillsaathi.security.CustomUserDetails;
import com.skillsaathi.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Public endpoint (works logged-out for city/state/category browsing).
     * sort=matchScore requires authentication — if the caller isn't logged in and
     * requests it, SearchService throws a clean 400 telling them to log in.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserSearchResultResponse>>> searchUsers(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String online,
            @RequestParam(required = false, defaultValue = "false") boolean sortByMatch,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        List<UserSearchResultResponse> results = searchService.searchUsers(
                city, state, category, online, sortByMatch, currentUserId, page, size);

        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
