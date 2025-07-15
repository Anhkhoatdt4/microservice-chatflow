package com.qlda.profileservice.mapper;

import com.qlda.profileservice.dto.ProfileRequest;
import com.qlda.profileservice.dto.UpdateProfileRequest;
import com.qlda.profileservice.dto.UserProfileResponse;
import com.qlda.profileservice.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfile toUserProfile(ProfileRequest profileRequest);
    UserProfileResponse toUserProfileResponse(UserProfile userProfile);

    void updateUserProfile(@MappingTarget UserProfile userProfile, UpdateProfileRequest updateProfileRequest);
}
