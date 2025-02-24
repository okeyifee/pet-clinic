package com.samuel.sniffers.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ZoneIdValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidZoneId {
    String message() default "Invalid time zone"; // Custom message
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
