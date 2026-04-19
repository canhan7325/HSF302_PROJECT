# History Code - Hoang Huy Hoang

## 2026-04-16 (Ngày 16 tháng 4 năm 2026) - Lúc 8:36

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




