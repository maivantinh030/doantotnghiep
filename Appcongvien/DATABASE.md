# DATABASE - PARK ADVENTURE (v2.0)

Schema MySQL cho hệ thống Quản lý Vé Công Viên Smart Card.
File SQL đầy đủ: `Appcongvien/database_new.sql`

## Nguyên tắc thiết kế
- **Balance lưu server** (`users.current_balance`), không lưu trên thẻ
- **Thẻ là định danh vật lý** — thẻ NFC chỉ lưu customer_id + xác thực RSA
- **Khóa thẻ server-side** (`cards.status = BLOCKED`) — không phụ thuộc vào việc ghi lên thẻ vật lý
- **1 tài khoản có thể liên kết nhiều thẻ** (phục vụ gia đình, nhóm)
- **Tài khoản giữ lại khi trả thẻ** — `cards.user_id = NULL` sau khi unlink

---

## 1) Nhóm Identity

### `accounts`
- `account_id` char(36) PK
- `phone_number` varchar(15) unique
- `password_hash` varchar(255)
- `role` enum: USER / STAFF / ADMIN
- `status` enum: ACTIVE / BANNED
- `last_login` datetime nullable
- `created_at`, `updated_at` datetime

### `users`
- `user_id` char(36) PK
- `account_id` char(36) unique FK → accounts
- `full_name` varchar(100) nullable
- `email` varchar(100) nullable
- `date_of_birth` date nullable
- `gender` enum: MALE / FEMALE / OTHER
- `current_balance` decimal(15,2) default 0 — **số dư ví (server-side)**
- `avatar_url` text nullable
- `created_at`, `updated_at` datetime

> Áp dụng cho cả tài khoản app (Case B) lẫn tài khoản tạm tại quầy (Case A)

### `admins`
- `admin_id` char(36) PK
- `account_id` char(36) unique FK → accounts
- `full_name` varchar(100)
- `employee_code` varchar(20) unique
- `role_level` enum: STAFF / ADMIN
- `is_active` bool
- `last_action_at` datetime nullable
- `created_at`, `updated_at` datetime

---

## 2) Nhóm Card / Smart Card

### `cards`
- `card_id` char(36) PK
- `physical_card_uid` varchar(50) unique — UID NFC vật lý
- `card_name` varchar(50) nullable
- `user_id` char(36) nullable FK → users — **NULL = chưa liên kết**
- `status` enum: AVAILABLE / ACTIVE / BLOCKED
- `deposit_amount` decimal(15,2) — số tiền cọc
- `deposit_status` enum: NONE / PAID / REFUNDED / FORFEITED
- `issued_at` datetime nullable
- `blocked_at` datetime nullable
- `blocked_reason` text nullable
- `last_used_at` datetime nullable
- `created_at`, `updated_at` datetime

### `card_requests`
Yêu cầu cấp thẻ từ app trước khi đến quầy (Case B)
- `request_id` char(36) PK
- `user_id` char(36) FK → users
- `status` enum: PENDING / APPROVED / REJECTED / COMPLETED
- `deposit_paid_online` bool — đã thanh toán cọc qua app
- `deposit_amount` decimal(15,2)
- `note` text nullable
- `approved_by` char(36) nullable FK → admins
- `created_at`, `updated_at` datetime

---

## 3) Nhóm Game / Vận hành

### `games`
- `game_id` char(36) PK
- `name` varchar(100)
- `description` text nullable
- `short_description` varchar(255) nullable
- `category` varchar(50) nullable
- `price_per_turn` decimal(10,2)
- `duration_minutes` int nullable
- `location` varchar(100) nullable
- `thumbnail_url` text nullable
- `gallery_urls` text nullable (JSON array)
- `age_required` int nullable
- `height_required` int nullable (cm)
- `max_capacity` int nullable
- `status` enum: ACTIVE / INACTIVE / MAINTENANCE
- `risk_level` int nullable (1=thấp, 2=trung bình, 3=cao)
- `is_featured` bool
- `average_rating` decimal(2,1)
- `total_reviews` int
- `total_plays` int
- `created_at`, `updated_at` datetime

### `terminals`
Thiết bị đọc thẻ tại các trò chơi
- `terminal_id` char(36) PK
- `name` varchar(100)
- `game_id` char(36) nullable FK → games
- `terminal_type` enum: ENTRY_GATE / GAME_READER
- `location` varchar(100) nullable
- `status` enum: ONLINE / OFFLINE
- `ip_address` varchar(45) nullable
- `last_heartbeat` datetime nullable
- `created_at`, `updated_at` datetime

### `game_play_logs`
Lịch sử mỗi lượt chơi (quẹt thẻ → trừ tiền → ghi log)
- `log_id` char(36) PK
- `user_id` char(36) FK → users
- `game_id` char(36) FK → games
- `terminal_id` char(36) nullable FK → terminals
- `card_id` char(36) nullable FK → cards
- `method` enum: CARD / BALANCE
- `amount_charged` decimal(10,2) — số tiền đã trừ
- `played_at` datetime

### `game_reviews`
- `review_id` char(36) PK
- `user_id` char(36) FK → users
- `game_id` char(36) FK → games
- `rating` int (1-5)
- `comment` text nullable
- `is_verified_play` bool
- `is_visible` bool
- `created_at` datetime

---

## 4) Nhóm Wallet / Payment

### `balance_transactions`
Mọi thay đổi số dư đều được ghi vào đây
- `transaction_id` char(36) PK
- `user_id` char(36) FK → users
- `amount` decimal(15,2) — dương = cộng, âm = trừ
- `balance_before` decimal(15,2)
- `balance_after` decimal(15,2)
- `type` enum: TOPUP / PAYMENT / REFUND / DEPOSIT_PAID / DEPOSIT_REFUND / DEPOSIT_FORFEITED / ADJUSTMENT
- `reference_type` varchar(50) nullable
- `reference_id` char(36) nullable
- `description` text nullable
- `created_at` datetime
- `created_by` char(36) nullable — account_id người tạo

### `payment_records`
Thanh toán qua cổng thanh toán (nạp tiền online)
- `payment_id` char(36) PK
- `user_id` char(36) FK → users
- `method` varchar(20) — VNPAY, MOMO, ZALOPAY, CASH...
- `amount` decimal(15,2)
- `status` enum: PENDING / SUCCESS / FAILED
- `external_ref_id` varchar(100) nullable
- `created_at` datetime

---

## 5) Nhóm Notification / Support / Content

### `notifications`
- `notification_id` char(36) PK
- `user_id` char(36) FK → users
- `type` enum: SYSTEM / CARD_SWIPE / TOPUP / REFUND / CARD_BLOCKED / GENERAL
- `title` varchar(200)
- `message` text
- `data` text nullable (JSON)
- `is_read` bool
- `read_at` datetime nullable
- `created_at` datetime

### `support_messages`
- `message_id` char(36) PK
- `user_id` char(36) FK → users
- `sender_id` char(36)
- `sender_type` enum: USER / STAFF / ADMIN
- `content` text
- `is_read` bool
- `created_at` datetime

### `announcements`
- `announcement_id` char(36) PK
- `title` varchar(200)
- `content` text
- `image_url` text nullable
- `priority` enum: LOW / NORMAL / HIGH / URGENT
- `is_active` bool
- `start_date` datetime nullable
- `end_date` datetime nullable
- `created_by` char(36) nullable
- `created_at`, `updated_at` datetime

---

## 6) Nhóm RSA Smart Card

### `rsa_public_keys`
Server dùng để verify chữ ký RSA từ thẻ
- `card_id` char(36) PK FK → cards (1 thẻ = 1 RSA keypair)
- `public_key_pem` text
- `status` enum: ACTIVE / REVOKED
- `created_at`, `updated_at` datetime

> Challenge dùng để xác thực giữ trong memory của service, không lưu DB.

---

## 7) Quan hệ chính

- `accounts` 1-1 `users`
- `accounts` 1-1 `admins`
- `users` 1-n `cards`
- `cards` 1-1 `rsa_public_keys`
- `users` 1-n `card_requests`
- `users` 1-n `balance_transactions`
- `users` 1-n `game_play_logs`
- `cards` 1-n `game_play_logs`
- `games` 1-n `game_play_logs`
- `games` 1-n `terminals`
- `games` 1-n `game_reviews`
