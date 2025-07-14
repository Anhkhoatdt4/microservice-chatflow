package com.qlda.authservice.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DobValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Dob {
    int min();
    String message() default "User must be at least 13 years old";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
