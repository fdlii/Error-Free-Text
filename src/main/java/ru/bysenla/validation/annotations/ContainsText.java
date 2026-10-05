package ru.bysenla.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.bysenla.validation.ContainsTextValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContainsTextValidator.class)
public @interface ContainsText {
    String message() default "Text doesn't contain regular characters.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
