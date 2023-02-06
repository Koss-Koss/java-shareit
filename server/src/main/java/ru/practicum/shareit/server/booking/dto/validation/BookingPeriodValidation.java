package ru.practicum.shareit.server.booking.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Constraint(validatedBy = BookingPeriodValidator.class)
public @interface BookingPeriodValidation {

    String message() default "{BookingPeriodValidation.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
