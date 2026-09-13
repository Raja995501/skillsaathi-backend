package com.skillsaathi.controller;

import com.skillsaathi.dto.common.ApiResponse;
import com.skillsaathi.dto.match.MatchResponse;
import com.skillsaathi.service.MatchService;
import com.skillsaathi.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getMatches() {
        List<MatchResponse> matches = matchService.getMatches(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(matches));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<MatchResponse>> getMatchDetail(@PathVariable Long userId) {
        MatchResponse match = matchService.getMatchDetail(SecurityUtils.getCurrentUserId(), userId);
        return ResponseEntity.ok(ApiResponse.success(match));
    }
}
