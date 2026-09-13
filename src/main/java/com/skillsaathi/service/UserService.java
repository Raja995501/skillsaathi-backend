package com.skillsaathi.service;

import com.skillsaathi.dto.user.PublicProfileResponse;
import com.skillsaathi.dto.user.UpdateProfileRequest;
import com.skillsaathi.dto.user.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileResponse getMyProfile(Long userId);
    UserProfileResponse updateMyProfile(Long userId, UpdateProfileRequest request);
    String updateProfilePicture(Long userId, MultipartFile file);
    PublicProfileResponse getPublicProfile(Long userId);
}
