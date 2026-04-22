package com.group1.mangaflowweb.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Mật khẩu phải tối thiểu 6 ký tự, chứa 1 ký tự đặc biệt, 1 ký tự in hoa và 1 ký tự thường";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

