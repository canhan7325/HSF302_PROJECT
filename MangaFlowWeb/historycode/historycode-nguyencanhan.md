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



