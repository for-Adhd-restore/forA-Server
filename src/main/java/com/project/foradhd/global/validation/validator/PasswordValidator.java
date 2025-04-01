package com.project.foradhd.global.validation.validator;

import com.project.foradhd.domain.user.web.dto.request.PasswordRequest;
import com.project.foradhd.global.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@RequiredArgsConstructor
@Component
public class PasswordValidator implements ConstraintValidator<ValidPassword, PasswordRequest> {

    //숫자, 영문, 특수문자 조합 8자리 이상(공백 문자 포함X)
    private static final String DEFAULT_PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9])(?=\\S+$).{8,}$";
    private static final String PASSWORD_REGEX_VALIDATION_MESSAGE_CODE = "password.invalid.format";
    private static final String PASSWORD_CONFIRM_VALIDATION_MESSAGE_CODE = "password.notMatches.passwordConfirm";
    private final MessageSource validationMessageSource;

    private String passwordRegex = DEFAULT_PASSWORD_REGEX;

    @Override
    public void initialize(ValidPassword validPassword) {
        if (StringUtils.hasText(validPassword.regexp())) {
            passwordRegex = validPassword.regexp();
        }
    }

    @Override
    public boolean isValid(PasswordRequest passwordRequest, ConstraintValidatorContext context) {
        String password = passwordRequest.getPassword();
        String passwordConfirm = passwordRequest.getPasswordConfirm();

        if (!StringUtils.hasText(password) || !StringUtils.hasText(passwordConfirm)) {
            return false;
        }
        if (!password.matches(passwordRegex)) {
            String message = validationMessageSource.getMessage(PASSWORD_REGEX_VALIDATION_MESSAGE_CODE, null, Locale.KOREA);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }
        if (!password.equals(passwordConfirm)) {
            String message = validationMessageSource.getMessage(PASSWORD_CONFIRM_VALIDATION_MESSAGE_CODE, null, Locale.KOREA);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }
        return true;
    }
}
