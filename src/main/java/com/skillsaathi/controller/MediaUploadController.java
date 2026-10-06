package com.skillsaathi.controller;

import com.skillsaathi.dto.common.ApiResponse;
import com.skillsaathi.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaUploadController {

    private final MediaStorageService mediaStorageService;

    /**
     * Upload chat media (image/video).
     * Frontend sends: multipart/form-data with "file" field.
     * Returns: { fileUrl, type }
     */
    @PostMapping("/chat/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadChatMedia(
            @RequestParam("file") MultipartFile file) {

        String url = mediaStorageService.uploadChatMedia(file, "skillsaathi/chat");

        String contentType = file.getContentType();
        String type = (contentType != null && contentType.startsWith("video")) ? "VIDEO" : "IMAGE";

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "fileUrl", url,
                "type", type
        )));
    }
}