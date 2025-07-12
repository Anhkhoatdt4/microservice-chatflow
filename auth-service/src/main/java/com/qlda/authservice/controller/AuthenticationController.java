package com.qlda.authservice.controller;


import com.qlda.authservice.dto.request.AuthenticationRequest;
import com.qlda.authservice.dto.request.LogoutRequest;
import com.qlda.authservice.dto.request.RefreshRequest;
import com.qlda.authservice.dto.response.ApiResponse;
import com.qlda.authservice.dto.response.AuthenticationResponse;
import com.qlda.authservice.dto.response.TokenResponse;
import com.qlda.authservice.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody AuthenticationRequest authenticationRequest){
        TokenResponse tokenResponse = authenticationService.generateTokenAndRefreshToken(authenticationRequest.getUsername());
        TokenResponse authenticationResponse = TokenResponse.builder()
                .token(tokenResponse.getToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiryTime(tokenResponse.getExpiryTime())
                .refreshTokenExpiryTime(tokenResponse.getRefreshTokenExpiryTime())
                .build();
        return ApiResponse.<TokenResponse>builder()
                .message("Login successful")
                .result(authenticationResponse)
                .build();
    }

    @GetMapping("/profile")
    public ApiResponse<String> getUsername(HttpServletRequest request){
        String token = authenticationService.getToken(request);
        String username = authenticationService.getUsernameFromToken(token);
        return ApiResponse.<String>builder()
                .message("Username retrieved successfully")
                .result(username)
                .build();
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest refreshRequest){
        String refreshToken = refreshRequest.getToken();
        if (!authenticationService.validateToken(refreshToken)) {
            return ApiResponse.<AuthenticationResponse>builder()
                    .code(4001)
                    .message("Invalid or expired refresh token")
                    .build();
        }

        String username = authenticationService.getUsernameFromToken(refreshToken);
        if (username == null) {
            return ApiResponse.<AuthenticationResponse>builder()
                    .code(4002)
                    .message("Could not extract username from token")
                    .build();
        }

        TokenResponse tokenResponse = authenticationService.generateTokenAndRefreshToken(refreshRequest.getToken());
            AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                    .token(tokenResponse.getToken())
                    .expiryTime(tokenResponse.getExpiryTime())
                    .build();

            return ApiResponse.<AuthenticationResponse>builder()
                    .message("Token refreshed successfully")
                    .result(authenticationResponse)
                    .build();
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestBody LogoutRequest logoutRequest) {
        if (authenticationService.validateToken(logoutRequest.getToken())){
            authenticationService.logOut(logoutRequest);
            return ApiResponse.<String>builder()
                    .message("Logout successful")
                    .result("You have been logged out successfully.")
                    .build();
        } else {
            return ApiResponse.<String>builder()
                    .code(4000)
                    .message("Invalid token")
                    .result("The provided token is invalid or has expired.")
                    .build();
        }
    }
}
