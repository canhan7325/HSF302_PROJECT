package com.group1.mangaflowweb.dto.user;

import com.group1.mangaflowweb.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer userId;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email phải đúng định dạng (ví dụ: a@gmail.com)")
    private String email;

    @ValidPassword(message = "Mật khẩu phải tối thiểu 6 ký tự, chứa 1 ký tự đặc biệt, 1 ký tự in hoa và 1 ký tự thường")
    private String password;

    private String role;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
