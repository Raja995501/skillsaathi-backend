package com.skillsaathi.controller;

import com.skillsaathi.dto.common.ApiResponse;
import com.skillsaathi.dto.connection.ConnectionResponse;
import com.skillsaathi.service.ConnectionService;
import com.skillsaathi.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/{userId}/request")
    public ResponseEntity<ApiResponse<ConnectionResponse>> sendRequest(@PathVariable Long userId) {
        ConnectionResponse response = connectionService.sendRequest(SecurityUtils.getCurrentUserId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Request sent", response));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ConnectionResponse>> cancel(@PathVariable Long id) {
        ConnectionResponse response = connectionService.cancelRequest(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Request cancelled", response));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<ConnectionResponse>> accept(@PathVariable Long id) {
        ConnectionResponse response = connectionService.acceptRequest(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Request accepted", response));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ConnectionResponse>> reject(@PathVariable Long id) {
        ConnectionResponse response = connectionService.rejectRequest(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Request rejected", response));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<ApiResponse<ConnectionResponse>> block(@PathVariable Long id) {
        ConnectionResponse response = connectionService.blockConnection(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("User blocked", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConnectionResponse>>> list(
            @RequestParam(required = false) String status) {
        List<ConnectionResponse> response = connectionService.listConnections(SecurityUtils.getCurrentUserId(), status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
