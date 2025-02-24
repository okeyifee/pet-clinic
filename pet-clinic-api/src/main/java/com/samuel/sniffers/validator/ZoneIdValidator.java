package com.samuel.sniffers.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.ZoneId;

public class ZoneIdValidator implements ConstraintValidator<ValidZoneId, String> {

    @Override
    public void initialize(ValidZoneId constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value.isEmpty()) {
            return false;
        }

        try {
            // Parse the value into a valid ZoneId
            ZoneId.of(value);
            return true;
        } catch (Exception e) {
            // If parsing fails, it's an invalid ZoneId
            return false;
        }
    }
}

