package com.qlda.authservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TokenResponse {
    String token;
    String refreshToken;
    Date expiryTime;
    Date refreshTokenExpiryTime;
}
