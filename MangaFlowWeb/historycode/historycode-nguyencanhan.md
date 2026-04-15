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


