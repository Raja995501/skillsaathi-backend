package com.skillsaathi.service.impl;

import com.skillsaathi.dto.skill.UserSkillResponse;
import com.skillsaathi.dto.user.PublicProfileResponse;
import com.skillsaathi.dto.user.UpdateProfileRequest;
import com.skillsaathi.dto.user.UserProfileResponse;
import com.skillsaathi.entity.User;
import com.skillsaathi.entity.UserSkill;
import com.skillsaathi.entity.enums.*;
import com.skillsaathi.exception.BadRequestException;
import com.skillsaathi.exception.ResourceNotFoundException;
import com.skillsaathi.repository.UserRepository;
import com.skillsaathi.repository.UserSkillRepository;
import com.skillsaathi.service.MediaStorageService;
import com.skillsaathi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final MediaStorageService mediaStorageService;

    private static final String PROFILE_PICTURE_FOLDER = "skillsaathi/profile-pictures";

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = getUserOrThrow(userId);
        List<UserSkill> skills = userSkillRepository.findByUserId(userId);

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profilePictureUrl(user.getProfilePictureUrl())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .dateOfBirth(user.getDateOfBirth())
                .city(user.getCity())
                .state(user.getState())
                .bio(user.getBio())
                .languages(user.getLanguages())
                .availability(user.getAvailability() != null ? user.getAvailability().name() : null)
                .onlinePreference(user.getOnlinePreference() != null ? user.getOnlinePreference().name() : null)
                .emailVerified(user.isEmailVerified())
                .averageRating(user.getAverageRating())
                .skillsToTeach(mapSkills(skills, SkillType.TEACH))
                .skillsToLearn(mapSkills(skills, SkillType.LEARN))
                .build();
    }

    @Override
    public UserProfileResponse updateMyProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserOrThrow(userId);

        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getState() != null) user.setState(request.getState());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getLanguages() != null) user.setLanguages(request.getLanguages());

        if (request.getGender() != null) {
            user.setGender(parseEnum(Gender.class, request.getGender(), "gender"));
        }
        if (request.getAvailability() != null) {
            user.setAvailability(parseEnum(AvailabilityType.class, request.getAvailability(), "availability"));
        }
        if (request.getOnlinePreference() != null) {
            user.setOnlinePreference(parseEnum(OnlinePreference.class, request.getOnlinePreference(), "onlinePreference"));
        }

        userRepository.save(user);
        return getMyProfile(userId);
    }

    @Override
    public String updateProfilePicture(Long userId, MultipartFile file) {
        User user = getUserOrThrow(userId);
        String url = mediaStorageService.uploadImage(file, PROFILE_PICTURE_FOLDER);
        user.setProfilePictureUrl(url);
        userRepository.save(user);
        return url;
    }

    @Override
    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(Long userId) {
        User user = getUserOrThrow(userId);
        List<UserSkill> skills = userSkillRepository.findByUserId(userId);

        return PublicProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .city(user.getCity())
                .state(user.getState())
                .bio(user.getBio())
                .languages(user.getLanguages())
                .availability(user.getAvailability() != null ? user.getAvailability().name() : null)
                .onlinePreference(user.getOnlinePreference() != null ? user.getOnlinePreference().name() : null)
                .averageRating(user.getAverageRating())
                .skillsToTeach(mapSkills(skills, SkillType.TEACH))
                .skillsToLearn(mapSkills(skills, SkillType.LEARN))
                .build();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String fieldName) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid value for " + fieldName + ": " + value);
        }
    }
}
