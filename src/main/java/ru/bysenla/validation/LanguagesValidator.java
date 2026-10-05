package ru.bysenla.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.bysenla.validation.annotations.ValidLanguages;

import java.util.Set;

public class LanguagesValidator implements ConstraintValidator<ValidLanguages, String> {
    private static final Set<String> supported = Set.of("ru", "en");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        for (String lang : value.split(",")) {
            if (!supported.contains(lang.trim())) {
                return false;
            }
        }
        return true;
    }
}
