package com.group1.mangaflowweb.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDTO {
    private Integer userId;

    @NotBlank(message = "Username không được để trống")
    @Size(min = 6, message = "Username phải tối thiểu 6 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email phải đúng định dạng")
    @Size(min = 6, message = "Email phải tối thiểu 6 ký tự")
    private String email;

    @NotBlank(message = "Vai trò không được để trống")
    private String role;

    private String password;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
