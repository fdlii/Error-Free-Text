package ru.bysenla.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.bysenla.validation.LanguagesValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LanguagesValidator.class)
public @interface ValidLanguages {
    String message() default "Allowable languages: ru, en.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
