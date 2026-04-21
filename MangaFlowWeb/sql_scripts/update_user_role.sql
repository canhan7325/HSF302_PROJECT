-- SQL Query để update user_id 1 với role AUTHOR

-- Cách 1: Update role của user_id 1 thành AUTHOR
UPDATE users
SET role = 'AUTHOR'
WHERE user_id = 4;

-- Cách 2: Nếu muốn insert user mới với role AUTHOR (nếu chưa tồn tại)
-- INSERT INTO users (username, email, password, role, enabled, created_at)
-- VALUES ('author_user', 'author@example.com', '[BCryptHash]', 'AUTHOR', 1, GETDATE());

-- Cách 3: Xem user hiện tại
SELECT user_id, username, email, role, enabled, created_at FROM users WHERE user_id = 1;

-- Cách 4: Xem tất cả user
SELECT user_id, username, email, role, enabled, created_at FROM users;

