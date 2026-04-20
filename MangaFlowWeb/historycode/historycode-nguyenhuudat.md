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

## 2026-04-20 (Ngày 20 tháng 4 năm 2026)

### Tasks Completed:
- **Home Page UI + Responsive Update**:
  - Thiết kế lại trang `index.html` theo dạng card truyện, bố cục responsive.
  - Tối ưu hiển thị desktop: **2 hàng, mỗi hàng 6 truyện**.

- **Comic Listing + Pagination**:
  - Hiển thị danh sách truyện động từ database trên trang chủ sau khi đăng nhập.
  - Cấu hình phân trang: tối đa **12 truyện/trang**, chuyển trang bằng `Previous/Next` và số trang.
  - Đồng bộ URL phân trang theo `/index?page=...&size=12`.

- **Local Image Serving**:
  - Cấu hình map resource local từ thư mục `D:/uploads/` thông qua `WebMvcConfig`.
  - Chuẩn hóa hiển thị ảnh cover theo `coverImg` (ví dụ: `comics/one-piece/cover.jpg`) qua đường dẫn `/uploads/...`.

- **Backend & Security Adjustments**:
  - Cập nhật `ComicRepository` sử dụng `JpaRepository` để hỗ trợ truy vấn phân trang.
  - Bổ sung xử lý lấy `comicPage` trong `ComicController` cho trang `/` và `/index`.
  - Sửa lỗi **403 khi bấm trang 2** bằng cách mở quyền truy cập `/index` trong `SecurityConfig`.
  - Mở quyền static cho `/uploads/**` để ảnh local tải đúng.

- **Bug Fixes & Stability**:
  - Sửa lỗi Thymeleaf parse expression ở phần hiển thị chapter trên `index.html`.
  - Khắc phục lỗi startup `EOFException` của Tomcat session persistence bằng cấu hình `server.servlet.session.persistent=false`.

- **Home Page Structure Update (Bootstrap/CSS only)**:
  - Refactor lại `index.html` theo bố cục 2 section: **Truyện Hay** và **Mới Cập Nhật**.
  - Chuẩn hóa giao diện chỉ dùng **Bootstrap + CSS + JavaScript**.
  - Tối ưu layout desktop hiển thị ổn định **6 truyện / hàng** (tối đa 2 hàng cho 1 trang = 12 truyện).

- **Data Display Logic Update**:
  - Hiển thị mục **Truyện Hay** theo danh sách **Top 6 `viewCount` giảm dần**.
  - Hiển thị mục **Mới Cập Nhật** theo thời gian cập nhật mới nhất trước (`updatedAt DESC`).
  - Giữ phân trang riêng cho danh sách mới cập nhật qua tham số `latestPage`.

- **UI Details & Visual Fixes**:
  - Thêm badge **"Nổi bật"** cho từng card trong mục Truyện Hay.
  - Thêm icon **bookmark** ở góc phải phía trên của mỗi card truyện.
  - Tinh chỉnh hiệu ứng bookmark dạng nền mờ/trong suốt, khi hover nền đậm hơn nhẹ.
  - Sửa CSS specificity và cache-busting để tránh lỗi style không nhận do trình duyệt cache.

- **Fragment & Consistency Improvements**:
  - Đồng bộ lại `headers.html` và `footers.html` theo hệ style mới của trang chủ.
  - Bổ sung class CSS `mf-*` để giữ thống nhất toàn bộ giao diện.
