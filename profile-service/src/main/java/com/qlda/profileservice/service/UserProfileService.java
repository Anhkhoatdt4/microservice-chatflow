package com.qlda.profileservice.service;

import com.qlda.profileservice.dto.ProfileRequest;
import com.qlda.profileservice.dto.UpdateProfileRequest;
import com.qlda.profileservice.dto.UserProfileResponse;
import com.qlda.profileservice.entity.UserProfile;
import com.qlda.profileservice.exception.AppException;
import com.qlda.profileservice.exception.ErrorCode;
import com.qlda.profileservice.mapper.UserProfileMapper;
import com.qlda.profileservice.repository.UserProfileRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileService {
    UserProfileMapper userProfileMapper;
    UserProfileRepository userProfileRepository;

    public UserProfileResponse createUserProfile(ProfileRequest profileRequest){
        UserProfile userProfile = userProfileMapper.toUserProfile(profileRequest);
        userProfileRepository.save(userProfile);
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public UserProfileResponse getUserProfileByUserId(String userId){
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found for userId: " + userId));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public UserProfileResponse getProfile(String id) {
        UserProfile userProfile =
                userProfileRepository.findById(id).orElseThrow(
                        () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userProfileMapper.toUserProfileResponse(userProfile);
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserProfileResponse> getAllProfiles() {
        var profiles = userProfileRepository.findAll();

        return profiles.stream().map(userProfileMapper::toUserProfileResponse).toList();
    }

    public UserProfileResponse getMyProfile(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }
    public UserProfileResponse updateProfile(UpdateProfileRequest updateProfileRequest) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        var profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        profile.setAvatarUrl(updateProfileRequest.getAvatarUrl());
        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(profile));
    }
    public List<UserProfileResponse> searchProfiles(String username) {
        var profiles = userProfileRepository.findAllByUsernameLike("%" + username + "%");
        return profiles.stream().map(userProfileMapper::toUserProfileResponse).toList();
    }
}
