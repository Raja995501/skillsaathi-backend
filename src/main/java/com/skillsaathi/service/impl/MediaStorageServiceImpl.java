package com.skillsaathi.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.skillsaathi.exception.BadRequestException;
import com.skillsaathi.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MediaStorageServiceImpl implements MediaStorageService {

    private final Cloudinary cloudinary;

    // Profile picture constraints
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB

    // === ADDED FOR CHAT MEDIA (IMAGES & VIDEOS) ===
    private static final Set<String> ALLOWED_CHAT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif",
            "video/mp4", "video/webm", "video/quicktime"
    );
    private static final long MAX_CHAT_FILE_SIZE_BYTES = 50L * 1024 * 1024; // 50 MB for videos/images
    // =============================================

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file provided");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG or WEBP images are allowed");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BadRequestException("Image must be smaller than 5MB");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "overwrite", true
            ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload image. Please try again.");
        }
    }

    // === ADDED FOR CHAT MEDIA UPLOAD ===
    @Override
    public String uploadChatMedia(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file provided");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CHAT_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid file type. Only images (JPEG, PNG, WEBP, GIF) and videos (MP4, WEBM) are allowed.");
        }
        if (file.getSize() > MAX_CHAT_FILE_SIZE_BYTES) {
            throw new BadRequestException("File size must be smaller than 50MB");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto" // 'auto' allows Cloudinary to automatically handle images and videos
            ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload media. Please try again.");
        }
    }
    // ===================================
}