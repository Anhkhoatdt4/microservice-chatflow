package com.qlda.profileservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProfileRequest {
    String email;
    String firstName;
    String lastName;
    String avatarUrl;
    LocalDate dob;
    String city;
}
