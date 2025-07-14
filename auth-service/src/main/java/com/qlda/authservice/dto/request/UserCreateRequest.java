package com.qlda.authservice.dto.request;

import com.qlda.authservice.validator.Dob;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    @Size(min = 4 , message = "USERNAME must be at least 4 characters long")
    String username;

    @Size(min = 6 , message = "PASSWORD must be at least 6 characters long")
    String password;

    @Email(message = "EMAIL must be a valid email address")
    @NotBlank(message = "EMAIL_IS_REQUIRED")
    String email;
    String firstName;
    String lastName;

    @Dob(min = 13 , message = "User must be at least 13 years old")
    LocalDate dob;

    String city;
}
