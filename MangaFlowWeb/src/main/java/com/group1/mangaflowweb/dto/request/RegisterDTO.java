package com.group1.mangaflowweb.dto.request;

import com.group1.mangaflowweb.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterDTO {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email phải đúng định dạng (ví dụ: a@gmail.com)")
    private String email;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @ValidPassword(message = "Mật khẩu phải tối thiểu 6 ký tự, chứa 1 ký tự đặc biệt, 1 ký tự in hoa và 1 ký tự thường")
    private String password;
}

