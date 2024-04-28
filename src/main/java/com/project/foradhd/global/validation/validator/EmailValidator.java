package com.project.foradhd.global.validation.validator;

import com.project.foradhd.global.validation.annotation.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Component
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String DEFAULT_EMAIL_REGEX = "^\\S+@\\S+\\.\\S+$";
    private static final List<String> VALID_EMAIL_DOMAIN = List.of("naver.com", "gmail.com", "icloud.com",
            "hotmail.com", "outlook.com", "facebook.com");
    private static final String EMAIL_AT_SYMBOL = "@";
    private static final String EMAIL_DOMAIN_VALIDATION_MESSAGE_CODE = "email.notSupported.domain";

    private final MessageSource validationMessageSource;

    private String emailRegex = DEFAULT_EMAIL_REGEX;

    @Override
    public void initialize(ValidEmail validEmail) {
        if (StringUtils.hasText(validEmail.regexp())) {
            emailRegex = validEmail.regexp();
        }
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(email) ||
                !email.matches(emailRegex)) {
            return false;
        }
        if (!VALID_EMAIL_DOMAIN.contains(parseDomain(email))) {
            String message = validationMessageSource.getMessage(EMAIL_DOMAIN_VALIDATION_MESSAGE_CODE, null, Locale.KOREA);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private String parseDomain(String email) {
        if (!email.matches(DEFAULT_EMAIL_REGEX)) return "";
        return email.split(EMAIL_AT_SYMBOL)[1];
    }
}
