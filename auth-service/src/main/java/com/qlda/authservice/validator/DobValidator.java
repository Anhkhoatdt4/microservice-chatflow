package com.qlda.authservice.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DobValidator implements ConstraintValidator<Dob, LocalDate> {

    private int min;
    @Override
    public void initialize(Dob constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        if (localDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !localDate.isAfter(today.minusYears(13));
    }
}
