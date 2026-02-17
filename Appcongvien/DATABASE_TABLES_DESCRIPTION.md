# 📚 Mô Tả Chi Tiết Từng Bảng Database - Park Adventure

Tài liệu này giải thích chi tiết vai trò, mục đích và cách sử dụng của từng bảng trong hệ thống.

---

## 📦 NHÓM 1: IDENTITY (ĐỊNH DANH)

Nhóm này quản lý **danh tính** của tất cả người dùng trong hệ thống.

---

### 1.1. `accounts` - Tài Khoản Đăng Nhập

**Mục đích**: Lưu trữ thông tin **xác thực (Authentication)** - tức là "bạn là ai" khi đăng nhập.

**Tại sao tách riêng?**: Một tài khoản (`account`) có thể là User thường hoặc Admin/Staff. Việc tách riêng giúp quản lý đăng nhập tập trung.

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `account_id` | ID duy nhất của tài khoản | `a1b2c3d4-...` |
| `phone_number` | Số điện thoại (dùng để đăng nhập) | `0901234567` |
| `password_hash` | Mật khẩu đã mã hóa (không lưu plaintext) | `$2b$10$...` |
| `role` | Vai trò của tài khoản | `USER`, `ADMIN`, `STAFF` |
| `status` | Trạng thái hoạt động | `ACTIVE`, `BANNED` |
| `last_login` | Lần đăng nhập cuối | `2024-01-15 10:30:00` |

**Luồng sử dụng**:
```
Người dùng nhập SĐT + Mật khẩu
    → Server tìm trong bảng accounts
    → So sánh password_hash
    → Check status = ACTIVE
    → Cho phép đăng nhập
```

---

### 1.2. `users` - Hồ Sơ Người Dùng (Khách Hàng)

**Mục đích**: Lưu thông tin **cá nhân** và **ví tiền** của khách hàng.

**Quan hệ**: Liên kết 1-1 với `accounts` (khi `role = USER`).

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `user_id` | ID duy nhất của user | `u1b2c3d4-...` |
| `account_id` | Liên kết tới tài khoản đăng nhập | FK → `accounts` |
| `full_name` | Tên hiển thị | `Nguyễn Văn A` |
| `email` | Email liên hệ | `a@gmail.com` |
| `date_of_birth` | Ngày sinh | `1990-05-15` |
| `gender` | Giới tính | `MALE`, `FEMALE`, `OTHER` |
| `membership_level` | Hạng thành viên | `BRONZE`, `SILVER`, `GOLD`, `PLATINUM` |
| `current_balance` | 💰 **Số dư ví hiện tại** | `500000.00` |
| `loyalty_points` | 🎁 Điểm thưởng tích lũy | `1500` |
| `avatar_url` | Link ảnh đại diện | `https://...` |
| `is_card_locked` | Khóa tất cả thẻ (khi bị mất) | `true/false` |
| `referral_code` | Mã giới thiệu riêng | `NGUYEN123` |
| `referred_by` | Người đã giới thiệu | FK → `users` |

**Lưu ý quan trọng**:
- `current_balance` là **nguồn tiền duy nhất** để chi tiêu. Thẻ không giữ tiền riêng.
- `loyalty_points` dùng để đổi quà/giảm giá, không thể rút thành tiền.

---

### 1.3. `admins` - Hồ Sơ Nhân Viên/Quản Trị

**Mục đích**: Lưu thông tin nhân viên.

**Quan hệ**: Liên kết 1-1 với `accounts` (khi `role = ADMIN/STAFF`).

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `admin_id` | ID duy nhất | `ad1b2c3d4-...` |
| `account_id` | Liên kết tới tài khoản | FK → `accounts` |
| `full_name` | Tên nhân viên | `Trần Thị B` |
| `employee_code` | Mã nhân viên | `NV001` |
| `last_action_at` | Thời gian thao tác cuối | Dùng để audit |

**Lưu ý**: Quyền hạn được xác định bởi `accounts.role` (SUPER_ADMIN / ADMIN / STAFF), không cần bảng permissions riêng.

---

### 1.4. `cards` - Thẻ Vật Lý (Smart Card)

**Mục đích**: Quản lý thẻ JavaCard và liên kết với người dùng.

**Triết lý**: Thẻ là **"chìa khóa"** để định danh, **KHÔNG chứa tiền**.

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `card_id` | ID trong database | `c1b2c3d4-...` |
| `physical_card_uid` | **UID của chip thẻ** (7-byte hex) | `04A1B2C3D4E5F6` |
| `user_id` | Chủ sở hữu (NULL = thẻ trắng) | FK → `users` |
| `card_name` | Tên gợi nhớ | `Thẻ chính`, `Thẻ của con` |
| `status` | Trạng thái | `ACTIVE`, `BLOCKED`, `LOST`, `INACTIVE` |
| `pin_hash` | Mã PIN đã hash (nếu có) | `$2b$10$...` |
| `issued_at` | Thời gian kích hoạt | `2024-01-10` |
| `blocked_at` | Thời gian bị khóa | `2024-01-20` |
| `blocked_reason` | Lý do khóa | `Người dùng báo mất` |
| `last_used_at` | Lần quẹt cuối | `2024-01-25 15:30:00` |

**Vòng đời thẻ**:
```
INACTIVE (Thẻ trắng, chưa ai sở hữu)
    ↓ [User kích hoạt qua App]
ACTIVE (Đang hoạt động)
    ↓ [User báo mất / Admin khóa]
BLOCKED hoặc LOST
    ↓ [User mở khóa / Cấp thẻ mới]
ACTIVE (hoặc cấp thẻ mới)
```

---

## 📦 NHÓM 2: COMMERCIAL (THƯƠNG MẠI)

Nhóm này quản lý **sản phẩm** và **dịch vụ** của công viên.

---

### 2.1. `games` - Danh Mục Trò Chơi

**Mục đích**: Lưu thông tin tất cả các trò chơi trong công viên.

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `game_id` | ID trò chơi | `g1b2c3d4-...` |
| `name` | Tên trò chơi | `Tàu lượn siêu tốc` |
| `description` | Mô tả chi tiết | `Trải nghiệm tốc độ...` |
| `short_description` | Mô tả ngắn (cho list) | `Cảm giác mạnh 5 sao` |
| `category` | Loại trò chơi | `Adventure`, `Family`, `Kids`, `Water` |
| `price_per_turn` | **Giá vé gốc** 1 lượt | `50000.00` |
| `duration_minutes` | Thời lượng 1 lượt | `5` (phút) |
| `location` | Vị trí | `Khu A`, `Indoor Zone` |
| `thumbnail_url` | Ảnh đại diện | `https://...` |
| `gallery_urls` | Mảng ảnh gallery (JSON) | `["url1", "url2"]` |
| `age_required` | Tuổi tối thiểu | `12` (tuổi) |
| `height_required` | Chiều cao tối thiểu | `140` (cm) |
| `max_capacity` | Số người tối đa/lượt | `24` |
| `status` | Trạng thái | `ACTIVE`, `MAINTENANCE`, `CLOSED` |
| `risk_level` | Mức độ mạo hiểm | `1-5` |
| `is_featured` | Hiển thị nổi bật | `true/false` |
| `average_rating` | Điểm trung bình | `4.5` |
| `total_reviews` | Tổng đánh giá | `1200` |
| `total_plays` | Tổng lượt chơi | `50000` |

---

### 2.2. `tickets` - Vé Đã Mua

**Mục đích**: Quản lý các **vé/lượt chơi đã mua trước** (Pre-paid).

**Khi nào dùng?**: Khi user mua Combo hoặc vé lẻ thay vì trừ tiền trực tiếp mỗi lượt.

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `ticket_id` | ID vé | `t1b2c3d4-...` |
| `user_id` | Người sở hữu | FK → `users` |
| `game_id` | Vé cho game nào (NULL = all games) | FK → `games` |
| `purchase_order_id` | Mua từ đơn nào | FK → `booking_orders` |
| `ticket_type` | Loại vé | `SINGLE`, `COMBO`, `UNLIMITED` |
| `remaining_turns` | Số lượt còn lại | `3` |
| `original_turns` | Số lượt ban đầu | `5` |
| `status` | Trạng thái | `VALID`, `USED`, `EXPIRED`, `CANCELLED` |
| `expiry_date` | Hạn sử dụng | `2024-02-28` |

**Ví dụ**:
- User mua "Combo 5 lượt Tàu lượn" giá 200K → Tạo ticket với `remaining_turns = 5`
- Mỗi lần chơi → `remaining_turns -= 1`
- Khi `remaining_turns = 0` → `status = USED`

---

### 2.3. `vouchers` - Mã Giảm Giá

**Mục đích**: Định nghĩa các mã giảm giá do hệ thống tạo ra.

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `voucher_id` | ID | `v1b2c3d4-...` |
| `code` | Mã voucher | `SUMMER2024`, `NEWYEAR50` |
| `title` | Tên hiển thị | `Giảm 20% mùa hè` |
| `description` | Mô tả | `Áp dụng cho đơn từ 200K` |
| `discount_type` | Loại giảm | `PERCENT`, `FIXED_AMOUNT` |
| `discount_value` | Giá trị | `20` (20%) hoặc `50000` (50K) |
| `max_discount` | Giảm tối đa | `100000` (cho PERCENT) |
| `min_order_value` | Đơn tối thiểu | `200000` |
| `usage_limit` | Tổng lượt dùng tối đa | `1000` |
| `used_count` | Đã dùng bao nhiêu | `456` |
| `per_user_limit` | Mỗi user dùng tối đa | `1` |
| `applicable_games` | Game được áp dụng (JSON) | `["g1", "g2"]` hoặc `null` (tất cả) |
| `start_date` | Bắt đầu hiệu lực | `2024-06-01` |
| `end_date` | Hết hạn | `2024-08-31` |
| `is_active` | Đang hoạt động | `true/false` |

---

### 2.4. `user_vouchers` - Voucher Của Người Dùng

**Mục đích**: Lưu voucher mà user **đã nhận/lưu** vào tài khoản.

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `id` | ID | `uv1b2c3d4-...` |
| `user_id` | Người sở hữu | FK → `users` |
| `voucher_id` | Voucher nào | FK → `vouchers` |
| `source` | Nguồn nhận | `CLAIMED`, `GIFTED`, `REFERRAL`, `PROMOTION` |
| `is_used` | Đã dùng chưa | `true/false` |
| `used_at` | Dùng lúc nào | `2024-07-15 10:00:00` |
| `used_order_id` | Dùng cho đơn nào | FK → `booking_orders` |

---

## 📦 NHÓM 3: ORDERS (ĐƠN HÀNG)

Nhóm quản lý **đơn hàng mua vé/dịch vụ**.

---

### 3.1. `booking_orders` - Đơn Hàng

**Mục đích**: Lưu thông tin **đơn hàng** khi user mua vé/combo.

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `order_id` | ID đơn hàng | `o1b2c3d4-...` |
| `user_id` | Người mua | FK → `users` |
| `subtotal` | Tổng trước giảm | `300000.00` |
| `discount_amount` | Số tiền giảm | `50000.00` |
| `total_amount` | **Tổng sau giảm** | `250000.00` |
| `voucher_id` | Voucher đã dùng | FK → `vouchers` |
| `payment_method` | Phương thức thanh toán | `BALANCE`, `MOMO`, `VNPAY`, `CASH` |
| `status` | Trạng thái | `PENDING`, `COMPLETED`, `CANCELLED`, `REFUNDED` |
| `note` | Ghi chú | `Mua cho gia đình` |

---

### 3.2. `booking_order_details` - Chi Tiết Đơn Hàng

**Mục đích**: Lưu từng **dòng sản phẩm** trong đơn hàng (1 đơn có thể mua nhiều loại vé).

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `detail_id` | ID | `d1b2c3d4-...` |
| `order_id` | Đơn hàng cha | FK → `booking_orders` |
| `game_id` | Vé trò chơi nào | FK → `games` |
| `quantity` | Số lượng | `2` |
| `unit_price` | Giá tại thời điểm mua | `50000.00` |
| `line_total` | Thành tiền | `100000.00` |

**Ví dụ đơn hàng**:
```
Đơn hàng ORD001:
  - 2 vé Tàu lượn x 50K = 100K
  - 3 vé Đu quay x 30K = 90K
  Subtotal: 190K
  Voucher SUMMER20 (-20%): -38K
  TOTAL: 152K
```

---

## 📦 NHÓM 4: FINANCIAL (TÀI CHÍNH)

Nhóm quản lý **dòng tiền** và **giao dịch**.

---

### 4.1. `balance_transactions` - Lịch Sử Biến Động Số Dư

**Mục đích**: Ghi lại **TỪNG giao dịch** làm thay đổi số dư ví. Đây là bảng **quan trọng nhất** cho kế toán.

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `transaction_id` | ID giao dịch | `tx1b2c3d4-...` |
| `user_id` | Người thực hiện | FK → `users` |
| `amount` | Số tiền (+/-) | `+500000` hoặc `-50000` |
| `balance_before` | Số dư TRƯỚC | `100000.00` |
| `balance_after` | Số dư SAU | `600000.00` |
| `type` | Loại giao dịch | `TOPUP`, `PAYMENT`, `REFUND`, `BONUS`, `ADJUSTMENT` |
| `reference_type` | Loại tham chiếu | `ORDER`, `PAYMENT`, `GAME_PLAY`, `ADMIN` |
| `reference_id` | ID tham chiếu | `o1b2c3d4-...` (order_id) |
| `description` | Mô tả | `Nạp tiền qua MoMo` |
| `created_by` | Người tạo (nếu ADJUSTMENT) | FK → `admins` |

**Các loại type**:
| Type | Mô tả | Amount |
|------|-------|--------|
| `TOPUP` | Nạp tiền vào ví | `+` |
| `PAYMENT` | Thanh toán (mua vé, chơi game) | `-` |
| `REFUND` | Hoàn tiền | `+` |
| `BONUS` | Thưởng (giới thiệu, promotion) | `+` |
| `ADJUSTMENT` | Admin điều chỉnh | `+/-` |

---

### 4.2. `payment_records` - Lịch Sử Nạp Tiền

**Mục đích**: Ghi lại các giao dịch **nạp tiền từ bên ngoài** (MoMo, VNPay, Bank).

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `payment_id` | ID | `pay1b2c3d4-...` |
| `user_id` | Người nạp | FK → `users` |
| `method` | Phương thức | `MOMO`, `VNPAY`, `BANKING`, `CASH` |
| `amount` | Số tiền nạp | `500000.00` |
| `status` | Trạng thái | `PENDING`, `SUCCESS`, `FAILED` |

**Luồng nạp tiền đơn giản**:
```
1. User chọn nạp 500K qua MoMo
2. Tạo payment_record (status = PENDING)
3. User thanh toán qua MoMo
4. Cập nhật payment_record (status = SUCCESS)
5. Cộng tiền vào users.current_balance
```

---

## 📦 NHÓM 5: OPERATIONS (VẬN HÀNH)

Nhóm quản lý **thiết bị** và **nhật ký hoạt động**.

---

### 5.1. `terminals` - Thiết Bị Đầu Cuối

**Mục đích**: Quản lý các **máy quẹt thẻ/kiosk** đặt tại công viên.

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `terminal_id` | ID | `term1b2c3d4-...` |
| `name` | Tên | `Cổng vào Tàu lượn 1` |
| `game_id` | Gắn với game nào | FK → `games` |
| `terminal_type` | Loại | `ENTRY_GATE`, `EXIT_GATE`, `KIOSK`, `POS` |
| `location` | Vị trí | `Khu A - Lối vào chính` |
| `status` | Trạng thái | `ONLINE`, `OFFLINE`, `MAINTENANCE` |
| `last_heartbeat` | Ping cuối về Server | `2024-01-25 15:30:00` |

---

### 5.2. `game_play_logs` - Nhật Ký Chơi Game

**Mục đích**: Ghi lại **CHI TIẾT từng lượt chơi** - rất quan trọng để đối soát và phân tích.

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `log_id` | ID | `log1b2c3d4-...` |
| `user_id` | Ai chơi | FK → `users` |
| `game_id` | Chơi game gì | FK → `games` |
| `terminal_id` | Tại máy nào | FK → `terminals` |
| `card_id` | Dùng thẻ nào | FK → `cards` |
| `ticket_id` | Dùng vé nào (nếu có) | FK → `tickets` |
| `method` | Phương thức | `CARD_TAP`, `TICKET` |
| `played_at` | Thời gian chơi | `2024-01-25 15:35:00` |

**Truy vấn hữu ích**:
```sql
-- Top 10 game được chơi nhiều nhất tháng này
SELECT game_id, COUNT(*) as plays
FROM game_play_logs
WHERE played_at >= '2024-01-01'
GROUP BY game_id
ORDER BY plays DESC
LIMIT 10;

-- Lịch sử chơi của user
SELECT * FROM game_play_logs
WHERE user_id = 'xxx'
ORDER BY played_at DESC;
```

---

## 📦 NHÓM 6: ENGAGEMENT (TƯƠNG TÁC)

Nhóm quản lý **tương tác** giữa user và hệ thống.

---

### 6.1. `game_reviews` - Đánh Giá Trò Chơi

**Mục đích**: Lưu đánh giá/review của user cho các game.

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `review_id` | ID | `r1b2c3d4-...` |
| `user_id` | Người đánh giá | FK → `users` |
| `game_id` | Game được đánh giá | FK → `games` |
| `rating` | Điểm sao (1-5) | `5` |
| `comment` | Nhận xét | `Rất vui, sẽ quay lại!` |
| `is_verified_play` | Đã xác minh đã chơi | `true` (check từ game_play_logs) |
| `is_visible` | Hiển thị | `true` (Admin có thể ẩn) |

**Ràng buộc**: Mỗi user chỉ được đánh giá 1 game 1 lần (unique: user_id + game_id).

---

### 6.2. `notifications` - Thông Báo

**Mục đích**: Gửi thông báo đến user (push notification, in-app).

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `notification_id` | ID | `n1b2c3d4-...` |
| `user_id` | Người nhận | FK → `users` |
| `type` | Loại | `TRANSACTION`, `PROMOTION`, `SYSTEM`, `REMINDER` |
| `title` | Tiêu đề | `Nạp tiền thành công` |
| `message` | Nội dung | `Bạn đã nạp 500,000đ vào ví` |
| `data` | Dữ liệu đính kèm (JSON) | `{"screen": "wallet", "tx_id": "xxx"}` |
| `is_read` | Đã đọc | `true/false` |
| `read_at` | Thời gian đọc | `2024-01-25 16:00:00` |

---

### 6.3. `support_messages` - Tin Nhắn Hỗ Trợ

**Mục đích**: Lưu các tin nhắn hỗ trợ giữa User và Admin (chat đơn giản).

| Trường | Mô tả | Ví dụ |
|--------|-------|-------|
| `message_id` | ID tin nhắn | `m1b2c3d4-...` |
| `user_id` | Cuộc hội thoại của user nào | FK → `users` |
| `sender_id` | Người gửi | FK → `users` hoặc `admins` |
| `sender_type` | Loại người gửi | `USER`, `ADMIN` |
| `content` | Nội dung | `Tôi không nạp được tiền qua MoMo` |
| `is_read` | Đã đọc chưa | `true/false` |

**Cách hoạt động**:
```
User gửi tin nhắn:
  → sender_type = 'USER'
  → sender_id = user_id

Admin trả lời:
  → sender_type = 'ADMIN'
  → sender_id = admin_id
  → user_id = ID của user đang chat
```

---

## 📊 SƠ ĐỒ QUAN HỆ TỔNG QUAN

```
                    ┌─────────────┐
                    │  accounts   │
                    └──────┬──────┘
                           │ 1:1
              ┌────────────┼────────────┐
              ▼                         ▼
        ┌─────────┐               ┌─────────┐
        │  users  │               │ admins  │
        └────┬────┘               └─────────┘
             │
    ┌────────┼────────┐
    │        │        │
    ▼        ▼        ▼
┌───────┐ ┌────────┐ ┌─────────┐
│ cards │ │tickets │ │ orders  │
└───────┘ └────────┘ └────┬────┘
                          │
                          ▼
                    ┌───────────┐
                    │order_details│
                    └─────┬─────┘
                          │
                          ▼
                    ┌─────────┐
                    │  games  │
                    └────┬────┘
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
        ┌─────────┐ ┌──────────┐ ┌─────────┐
        │terminals│ │game_pricing│ │reviews │
        └────┬────┘ └──────────┘ └─────────┘
             │
             ▼
      ┌─────────────┐
      │game_play_logs│
      └─────────────┘
```

---

*Tài liệu này giúp hiểu rõ vai trò và mối quan hệ của từng bảng trong hệ thống Park Adventure.*

