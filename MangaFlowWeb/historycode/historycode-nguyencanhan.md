# History Code - Nguyen Canh An

## 2026-04-15 - Database & Entities
- Tạo script SQL Server: users, subscriptions, comics, chapters, pages, bookmarks, reading_history, genres, genre_comics
- Implement 10 JPA entities (Users, Subscriptions, Transactions, Genres, Comics, Chapters, Pages, Bookmarks, ReadingHistories, GenreComics) với Lombok

## 2026-04-17 - UI Interface  
- Gen giao diện `buysubscriptions.html` (layout mua gói subscription)
- Tạo fragments: `headers.html` (navigation), `footers.html` (info & copyright)
- Styling CSS cho subscription pages

## 2026-04-19 - Payment & Transactions
- Tích hợp ZaloPay (thay VNPay): Config, DTO, Service, HMAC-SHA256 signature
- Tích hợp MoMo payment
- PaymentCallbackController: xử lý callback từ ZaloPay/MoMo
- TransactionService: tạo + complete giao dịch (PENDING → SUCCESS)
- Trang kết quả: `payment-success.html`, `payment-failed.html`
- Fix entities: Users, Transactions (thêm @Column names)

## 2026-04-20 - Reading & Membership
- Merge: Chapter reading logic + Header (chapter-read.html, chapter-read.js, CSS)
- Fix payment logic: PricingController routing, TransactionServiceImpl
- AccessService: kiểm tra subscription hợp lệ, còn hạn
- ComicDetailController: check subscription trước khi hiển thị chapter full

## 2026-04-21 - Fix Compilation Errors
- Giải quyết 106+ compilation errors
- Fixed duplicate `findBySlug()` trong ComicRepository
- Convert 13 Java Records → Lombok Classes (ComicAdminResponse, ChapterAdminResponse, GenreAdminResponse, UserAdminResponse, TransactionAdminResponse, DashboardStatsResponse, SubscriptionAdminResponse, PageAdminResponse, RevenueDataPointResponse, ComicSummaryResponse, TransactionSummaryResponse, MangaViewDataResponse, GenreComicCountResponse)
- Added missing imports: UserResponse, HttpStatus, ResponseStatusException
- Fixed entity references: getComics() → getGenreComics() (GenreServiceImpl, ComicServiceImpl, AdminDashboardServiceImpl)
- Fixed method references: comicCount() → getComicCount()
- Fixed enum type: ComicEnum → TransactionEnum (TransactionAdminResponse)
- Fixed controller calls: record accessor syntax → Lombok getters (ComicAdController, GenreAdController)

## 2026-04-22 - Validation & Subscription Duration
- Implement `duration_days` từ database: Lấy giá trị từ bảng Subscriptions thay vì hardcode
- Cập nhật `completeTransaction()`: Tính endedAt = startedAt + duration_days (bình thường) hoặc now + duration_days + remaining_days (nâng cấp)
- Thêm helper method `getRemainingDaysFromCurrentSubscription()`: Tính số ngày còn lại của gói hiện tại
- Tăng cường `checkSubscription()`: Chặn hạ cấp xuống FREE, chặn hạ cấp giữa các gói khác
- Thêm lớp validation: `paySubscription()` + `freeSubscriptionStart()` kiểm tra trước khi tạo giao dịch
- Cập nhật giao diện: `buysubscriptions.html` - hiển thị dialog lỗi màu sắc (đỏ cho lỗi downgrade, xanh cho thành công)
- Tự động phát hiện URL parameters: `?error=cannot_downgrade`, `?success=free_subscription`

## 2026-04-22 (Lần 2) - Automatic Transaction Expiry & User Downgrade
### Part 1: Scheduler Implementation
- **MangaFlowWebApplication.java**: Thêm `@EnableScheduling` để kích hoạt Spring scheduling
- **TransactionService.java**: Thêm interface method `void cancelExpiredTransactionsAndDowngradeUsers()`
- **TransactionServiceImpl.java**: 
  - Thêm `@Scheduled(cron = "0 * * * * *")` - Chạy mỗi phút để kiểm tra gói hết hạn
  - Implement logic hủy tự động:
    - Lấy tất cả transactions hết hạn: `endedAt < CURRENT_TIMESTAMP AND status != 'CANCELED'`
    - Mark từng transaction là CANCELED
    - Track affected users
    - Check active transactions của mỗi user (status = SUCCESS/UPDATED, endedAt > NOW)
    - Downgrade user to role = "user" nếu không có gói hoạt động

### Part 2: Repository Query Methods
- **TransactionRepository.java**: Thêm 2 query methods:
  - `List<Transactions> getExpiredTransactions()`: Tìm các giao dịch hết hạn chưa bị cancel
  - `List<Transactions> getActiveTransactionsByUserId(Integer userId)`: Kiểm tra các gói hoạt động của user

### Part 3: Header Display Fix
- **TransactionsServiceImpl.java**: Fix `getCurrentMembershipPrice()`:
  - **Trước**: Lấy bất kỳ transaction nào không phải CANCELED (bao gồm PENDING, UPDATED)
  - **Sau**: Chỉ lấy transaction với status = SUCCESS (đã hoàn thành) và chưa hết hạn
  - Tính toán chính xác: `endedAt IS NULL OR endedAt > NOW`
  - Return 0L nếu không có gói hoạt động → Header hiển thị "Người dùng thường"





