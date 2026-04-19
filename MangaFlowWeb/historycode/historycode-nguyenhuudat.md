# History Code - Nguyen Huu Dat

## 2026-04-19 (Ngày 19 tháng 4 năm 2026)

### Tasks Completed:
- **UI/UX Refactoring**:
  - Thiết kế lại trang **Login** chuyên nghiệp dựa trên bản mẫu: thêm background dot-pattern, tích hợp Bootstrap Icons, và xử lý ẩn/hiện mật khẩu bằng JavaScript.
  - Refactor trang **Register** theo layout mới.
  - Xây dựng hệ thống Layout Fragments cho **Header** và **Footer** dùng chung.

- **Security & Authentication Implementation**:
  - Tích hợp **Spring Security** với phân quyền: `ADMIN` và `READER`.
  - Triển khai cơ chế xác thực dựa trên **JWT (JSON Web Token)** và lưu trữ token trong **HttpSession**.
  - Xây dựng `JwtUtil`, `JwtAuthenticationFilter` và `CustomUserDetailsService` để quản lý bảo mật.
  - Cấu hình `SecurityConfig` cho phép Guest truy cập trang chủ và các tài nguyên tĩnh (`permitAll`).

- **Backend Development**:
  - Tạo `AuthController`: Xử lý logic đăng nhập, tạo JWT, lưu Session và điều hướng theo Role (Admin về Dashboard, Reader về trang chủ).
  - Tạo `RegisterController`: Xử lý đăng ký người dùng mới, tự động mã hóa mật khẩu bằng `BCryptPasswordEncoder` và gán role mặc định.

- **Template Logic (Thymeleaf)**:
  - Cấu hình Header thông minh: Tự động ẩn menu điều hướng khi ở trang Login/Register bằng biến `hideNav`.
  - Hiển thị thông tin người dùng (`username`) và nút Logout trên thanh Header sau khi đăng nhập thành công bằng `thymeleaf-extras-springsecurity6`.

