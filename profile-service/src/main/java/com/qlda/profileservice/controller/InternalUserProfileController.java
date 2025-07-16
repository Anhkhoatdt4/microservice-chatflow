package com.qlda.profileservice.controller;

import com.qlda.profileservice.dto.ApiResponse;
import com.qlda.profileservice.dto.ProfileRequest;
import com.qlda.profileservice.dto.UserProfileResponse;
import com.qlda.profileservice.service.UserProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalUserProfileController {

    UserProfileService userProfileService;

    @PostMapping("/internal/users/create")
     ApiResponse<UserProfileResponse>createUserProfile(@RequestBody ProfileRequest profileRequest){
        System.out.println("Creating user profile with request: " + profileRequest);
        UserProfileResponse userProfileResponse = userProfileService.createUserProfile(profileRequest);
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileResponse)
                .build();
    }

    @GetMapping("/internal/users/{userId}")
    ApiResponse<UserProfileResponse> getProfileByUserId(@PathVariable String userId){
        System.out.println("Fetching profile for userId: " + userId);
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.getUserProfileByUserId(userId))
                .build();
    }
}
