package com.qlda.authservice.dto.request;
import java.time.LocalDate;
import java.util.List;

import com.qlda.authservice.validator.Dob;
import com.qlda.authservice.validator.DobValidator;

import lombok.*;
import lombok.experimental.FieldDefaults;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    String firstName;
    String lastName;

    @Dob(min = 13, message = "User must be at least 13 years old")
    LocalDate dob;

    List<String> roles;
}