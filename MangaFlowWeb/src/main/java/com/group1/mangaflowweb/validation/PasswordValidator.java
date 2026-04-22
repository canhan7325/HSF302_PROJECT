package com.group1.mangaflowweb.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // Yêu cầu: tối thiểu 6 ký tự, 1 ký tự đặc biệt, 1 in hoa, 1 thường
    private static final String PASSWORD_PATTERN =
        "^(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(?=.{6,}).*$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return pattern.matcher(value).matches();
    }
}

