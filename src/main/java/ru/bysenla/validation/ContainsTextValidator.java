package ru.bysenla.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.bysenla.validation.annotations.ContainsText;

public class ContainsTextValidator implements ConstraintValidator<ContainsText, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.codePoints().anyMatch(Character::isLetter)) {
            return true;
        }
        return false;
    }
}
