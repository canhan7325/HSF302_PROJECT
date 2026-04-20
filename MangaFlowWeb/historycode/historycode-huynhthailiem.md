# History Code - Huynh Thai Liem

## 2026-04-20 (Ngày 20 tháng 4 năm 2026)

### Tasks Completed:

- **Admin Dashboard (Trang tổng quan)**:
  - Xây dựng trang dashboard với 4 thẻ thống kê: Active Users, Active Manga, Total Views, Active Genres.
  - Tích hợp biểu đồ cột ngang **Most Viewed Manga** (Chart.js) với toggle Top 5 / Top 10 / All.
  - Tích hợp biểu đồ cột dọc **Revenue Over Time** (Chart.js) với toggle Week / Month / Year, fetch dữ liệu từ endpoint `/admin/dashboard/revenue`.

- **Manga Management (Quản lý truyện)**:
  - Xây dựng trang quản lý manga với chế độ xem **Grid** và **List** (lưu trạng thái bằng `localStorage`).
  - Thêm thanh tìm kiếm theo tên truyện.
  - Hiển thị ảnh bìa, trạng thái (badge), lượt xem, số chương, uploader.
  - Các action bằng icon: Edit, Chapters, Delete (soft-delete).
  - Form tạo/sửa manga: title, description, cover image URL, status, genres (checkbox).

- **Chapter Management (Quản lý chương)**:
  - Tích hợp quản lý chương trực tiếp trong trang manga (không tách trang riêng).
  - Danh sách chương sắp xếp giảm dần, thêm chương mới bằng inline form ở đầu bảng.
  - Inline edit tên chương, xóa chương.
  - **Inline Page Editor**: Hiển thị ảnh trang trực tiếp dưới mỗi chương, hỗ trợ:
    - Drag-to-reorder (kéo thả để sắp xếp lại thứ tự trang, tự động lưu).
    - Xóa trang bằng nút X đỏ (hover).
    - Upload ảnh từ máy tính, lưu vào `static/images/pages/`, cập nhật DB.

- **User Management (Quản lý người dùng)**:
  - Danh sách người dùng với phân trang và tìm kiếm theo username/email.
  - Form tạo/sửa user: username, email, role, password (BCrypt).
  - Disable/Restore user (soft-delete bằng `enabled` flag).

- **Genre Management (Quản lý thể loại)**:
  - Danh sách thể loại với tìm kiếm theo tên.
  - Form tạo/sửa genre, tự động sinh slug.
  - Xóa genre (chặn nếu còn comic liên kết).

- **Revenue & View Tracking (Tích hợp upstream)**:
  - Tích hợp trang `/admin/revenue` từ upstream: tổng doanh thu, giao dịch theo gói, danh sách giao dịch gần đây.
  - Tích hợp trang `/admin/view-tracking` từ upstream: bảng truyện với lượt xem, hỗ trợ sort và filter (Most Viewed / Least Viewed).
  - Style lại cả 2 trang theo design system mới (Tailwind + Material Symbols).

- **Reusable Admin Fragments (Fragments dùng chung)**:
  - `admin/fragments/admin-styles.html`: inject Tailwind CDN + config + Material Symbols.
  - `admin/fragments/admin-sidebar.html`: sidebar dùng chung với tham số `activeSection` để highlight menu hiện tại.
  - `admin/fragments/admin-header.html`, `admin-footer.html`: empty fragments (tương thích ngược).
  - `admin/fragments/admin.html`: flash messages, statusBadge, coverImg (SVG placeholder), pagination.

- **Security Fixes (Sửa lỗi bảo mật)**:
  - Sửa `AdminRedirectInterceptor`: bỏ qua `AnonymousAuthenticationToken` để tránh lỗi 500 khi truy cập ảnh tĩnh.
  - Sửa `CustomAccessDeniedHandler` và `AdminRedirectInterceptor` redirect về `/admin/dashboard` thay vì `/admin`.
  - Thêm `GET /admin` redirect về `/admin/dashboard`.

- **Database & Schema Migration**:
  - Đổi Hibernate naming strategy sang `CamelCaseToUnderscoresNamingStrategy` để khớp với schema snake_case mới.
  - Cập nhật tất cả native SQL queries trong `TransactionRepository` và `ReadingHistoryRepository` sang tên cột snake_case.
  - Viết lại `test-data.sql` hoàn chỉnh: tạo bảng an toàn, clean slate, seed data 2024–2026 cho revenue chart.

- **File Upload (Upload ảnh trang)**:
  - Implement `PageServiceImpl.uploadAndAddPage()`: nhận `MultipartFile`, lưu vào `src/main/resources/static/images/pages/` và `target/classes/static/images/pages/` (phục vụ ngay không cần restart).
  - Cập nhật `PageController` nhận `MultipartFile` thay vì string path.
  - Cập nhật JS trong `manga.html` dùng `FormData` để upload file thực sự.
