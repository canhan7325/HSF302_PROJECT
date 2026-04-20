# History Code - Hoang Huy Hoang

## 2026-04-16 (Ngày 16 tháng 4 năm 2026) - Lúc 8:36, updated lúc 8:00 PM (Ngày 20 tháng 4 năm 2026)

### Tasks Completed:
- **Created Enum, Controller, Service, Repository, DTO Package**: Tạo các package cần thiết cho dự án:
    - `enums` - Chứa các enum như ComicEnum, TransactionEnum
    - `controllers` - Chứa các REST controllers để xử lý yêu cầu từ client
    - `services` - Chứa các service để xử lý logic nghiệp vụ
    - `repositories` - Chứa các repository để tương tác với database
    - `dto` - Chứa các Data Transfer Objects để truyền dữ liệu giữa client và

- **Refactored Status Field(String to Enum)**: Thay đổi trường `status` trong entity từ kiểu String sang kiểu Enum để tăng tính an toàn và dễ bảo trì.
    - Thay đổi kiểu dữ liệu của field status trong:
        - `TransactionEnum.java` - Enum định nghĩa các trạng thái của giao dịch (PENDING, COMPLETED, FAILED)
        - `ComicEnum.java` - Enum định nghĩa các trạng thái của truyện (ONGOING, COMPLETED, HIATUS)
- **UI/UX – Chapter Read**:
    - Xây dựng trang đọc truyện (vertical + horizontal).
    - Có breadcrumb, tiêu đề, prev/next, bottom bar, right actions.
    - Thêm scroll-top và click trái/phải để chuyển trang.

- **Reading Mode Logic**:
    - Toggle đọc ngang/dọc + lưu bằng localStorage.
    - Dùng `scroll-snap` cho hiệu ứng lật trang.
    - JS xử lý next/prev page.

- **Page Slider**:
    - Thanh slider custom theo số page.
    - Highlight page hiện tại (active) và đã đọc (visited).
    - Click để nhảy page.

- **Content Lock / Preview**:
    - Giới hạn nội dung theo login & subscription.
    - Blur ảnh + overlay thông báo khi bị khóa.

- **Bookmark**:
    - Lưu truyện qua POST `/bookmarks/create`.
    - Hiển thị theo trạng thái login.

- **Backend & Navigation**:
    - Load pages, prev/next chapter, danh sách chapter.
    - Dùng Thymeleaf fragments để tái sử dụng UI.

- **Optimization**:
    - Scroll mượt, responsive mobile.
    - Tách riêng CSS & JS, dễ maintain.



