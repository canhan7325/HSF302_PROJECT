# History Code - Nguyen Canh An

## 2026-04-15 (Ngày 15 tháng 4 năm 2026) - Lúc 21:38 (9:38 PM)

### Tasks Completed:
- **Created Database Script**: Tạo script SQL Server hoàn chỉnh cho database `Manga_flow` với các bảng:
  - `subscriptions` - Gói đăng ký (Free, Silver, Gold)
  - `users` - Tài khoản người dùng
  - `transactions` - Giao dịch đăng ký
  - `genres` - Thể loại truyện
  - `comics` - Nội dung truyện chính
  - `genre_comics` - Mối quan hệ nhiều-nhiều giữa genres và comics
  - `chapters` - Các chương của truyện
  - `pages` - Các trang trong chương
  - `bookmarks` - Dấu trang của người dùng
  - `reading_history` - Lịch sử đọc của người dùng

-  **Created Entity Classes (10 entities)**: Implement các JPA entities với annotations:
  - `Users.java` - Entity người dùng
  - `Subscriptions.java` - Entity gói đăng ký
  - `Transactions.java` - Entity giao dịch
  - `Genres.java` - Entity thể loại
  - `Comics.java` - Entity truyện chính
  - `Chapters.java` - Entity chương
  - `Pages.java` - Entity trang
  - `Bookmarks.java` - Entity dấu trang
  - `ReadingHistories.java` - Entity lịch sử đọc
  - `GenreComics.java` - Entity junction

## 2026-04-17 (Ngày 17 tháng 4 năm 2026) - Lúc 8:00 (8:00 AM)

### Tasks Completed:
- **Generated UI Interface**: Dùng AI gen giao diện từ code của Google Stitch sang `buysubscriptions.html`:
  - Tạo layout cho trang mua đăng ký với gọi các subscription packages
  - Styling với CSS tương ứng trong `buysubscriptions.css`

- **Created HTML Fragments**: Tạo các fragments cho layout chung:
  - `headers.html` - Fragment header với navigation
  - `footers.html` - Fragment footer với thông tin liên hệ và copyright

## 2026-04-19 (Ngày 19 tháng 4 năm 2026) - Lúc 17:14 (5:14 PM)

### Tasks Completed:

- **Tích hợp cổng thanh toán ZaloPay** (thay thế VNPay):
  - `ZaloPayConfig.java` - Config class đọc thông số ZaloPay từ `application.properties`
  - `ZaloPayPaymentResponse.java` - DTO cho response từ ZaloPay API
  - `ZaloPayService.java` - Interface service ZaloPay
  - `ZaloPayServiceImpl.java` - Implement gọi API `v2/create` của ZaloPay sandbox, tạo HMAC-SHA256 signature
  - Cập nhật `application.properties` - Thêm config ZaloPay (appId, key1, key2, endpoint, returnUrl)

- **Cập nhật giao diện chọn phương thức thanh toán**:
  - `buysubscriptions.html` - Đổi option VNPay thành ZaloPay, thêm dấu tích ✓ cho ZaloPay
  - `buysubscriptions.css` - Đổi class `.payment-badge.vnpay` thành `.payment-badge.zalopay`
  - Thêm JavaScript xử lý toggle active khi chọn phương thức thanh toán (MoMo / ZaloPay)

- **Cập nhật PricingController**:
  - Thêm `@RequestParam("payment")` để nhận phương thức thanh toán từ form
  - Điều hướng sang ZaloPay hoặc MoMo tùy theo lựa chọn của user
  - Dùng `subscriptionId` trong `orderId` để callback có thể truy vết gói đăng ký

- **Tạo hệ thống lưu lịch sử giao dịch (Transactions)**:
  - `TransactionsDTO.java` - DTO cho transactions (transactionId, price, status, startedAt, endedAt, subscriptionName...)
  - `TransactionsService.java` - Interface với `createTransaction()` và `completeTransaction()`
  - `TransactionsServiceImpl.java` - Implement logic tạo giao dịch PENDING, complete thành SUCCESS với startedAt = now và endedAt = now + durationDays
  - `UsersRepository.java` - Tạo repository cho Users entity, thêm `findByUsername()`

- **Tạo PaymentCallbackController** (xử lý callback từ cổng thanh toán):
  - `GET /payment/zalopay/return` - Xử lý ZaloPay redirect sau thanh toán
  - `GET /payment/momo/return` - Xử lý MoMo redirect sau thanh toán
  - Khi thanh toán thành công → tạo transaction + complete → hiển thị trang success
  - Khi thất bại → hiển thị trang failed

- **Tạo 2 trang kết quả thanh toán**:
  - `payment-success.html` - Trang thanh toán thành công (banner xanh, chi tiết giao dịch, mã GD, ngày bắt đầu/kết thúc)
  - `payment-failed.html` - Trang thanh toán thất bại (banner đỏ, lý do lỗi, gợi ý khắc phục)

- **Fix bugs**:
  - Fix entity `Users.java` - Thêm `@Column(name = "user_id")`, `@Column(name = "created_at")` cho đúng tên cột snake_case trong DB SQL Server
  - Fix entity `Transactions.java` - Thêm `@Column(name = "transaction_id")`, `@Column(name = "started_at")`, `@Column(name = "ended_at")`, `@Column(name = "created_at")`
  - Fix `ZaloPayServiceImpl.java` - Xóa dependency `ObjectMapper` (jackson-databind chưa có trong pom.xml), tạo JSON thủ công
