# Mô Tả Chi Tiết Hệ Thống Quản Lý Vé Công Viên Sử Dụng Smart Card

---

## 1. Tổng Quan Hệ Thống

Hệ thống được xây dựng theo mô hình **cashless park** – công viên không dùng tiền mặt. Khách tham quan được cấp một chiếc thẻ Java Card vật lý khi đến cổng. Thẻ này đóng vai trò **định danh** (như chứng minh thư trong công viên), không lưu tiền bên trong. Toàn bộ số dư và giao dịch được quản lý tập trung trên máy chủ (backend). Khi khách muốn chơi một trò chơi, chỉ cần đặt thẻ vào thiết bị đọc tại cổng trò chơi – hệ thống tự động xác thực thẻ, trừ tiền và gửi thông báo về điện thoại của khách trong vài giây.

### 1.1 Triết lý thiết kế

| Nguyên tắc | Giải thích |
|-----------|-----------|
| **Balance on Server** | Số dư không nằm trên thẻ mà trên server. Thẻ bị mất không mất tiền, chỉ cần khóa thẻ và cấp thẻ mới |
| **Thẻ là định danh** | Thẻ chỉ lưu customerID (chuỗi định danh duy nhất) và thông tin cá nhân mã hóa |
| **Thẻ tái sử dụng** | Khi khách trả thẻ về, nhân viên ghi đè thông tin khách mới lên thẻ cũ. Tiết kiệm chi phí thẻ |
| **Chống clone bằng RSA** | Mỗi thẻ có cặp khóa RSA riêng được sinh ngay trên chip thẻ. Private key không bao giờ rời khỏi thẻ, không thể sao chép |
| **App là tùy chọn** | Khách không có smartphone vẫn dùng được. App chỉ cung cấp thêm tiện ích (xem số dư, nhận thông báo, nạp tiền online) |

---

## 2. Các Thành Phần Hệ Thống

```
┌─────────────────────────────────────────────────────────────────┐
│                         Hệ Thống                                │
│                                                                 │
│  [Java Card]  ←APDU→  [SmardCard Terminal]  ←API→  [Backend]  │
│     (thẻ)              (reader Kotlin Desktop)     (Ktor)      │
│                                                        ↕       │
│  [AppCongVien Android]  ←────── Notifications ────────┘       │
│     (app khách hàng)                                           │
│                                                        ↕       │
│  [appdesktop Admin]     ←────── Admin API ─────────────┘      │
│     (quản lý hệ thống)                                         │
└─────────────────────────────────────────────────────────────────┘
```

### 2.1 Java Card (ParkCard Applet) – Thẻ vật lý

**Phần cứng:** Thẻ Java Card tiêu chuẩn ISO 7816, dạng thẻ nhựa có chip tiếp xúc hoặc không tiếp xúc (NFC/contactless).

**Phần mềm:** ParkCard Applet – ứng dụng Java Card viết bằng Java Card API, được nạp lên chip thẻ bằng công cụ GlobalPlatform (gp.jar).

**Dữ liệu lưu trên thẻ:**

| Dữ liệu | Bộ nhớ | Bảo vệ |
|---------|--------|--------|
| customerID | EEPROM, plaintext | Không mã hóa (định danh công khai) |
| Tên khách | EEPROM, AES-128-CBC | Master Key |
| Số điện thoại | EEPROM, AES-128-CBC | Master Key |
| RSA Private Key (1024-bit) | EEPROM, AES-128-CBC (256 bytes) | Master Key, chỉ load vào RAM khi ký |
| Wrapped Master Key (User) | EEPROM, 16 bytes | User PIN → PBKDF2 → PIN-Key |
| Wrapped Master Key (Admin) | EEPROM, 16 bytes | Admin PIN → PBKDF2 → PIN-Key |
| Master Key (đang dùng) | RAM Transient | Tự xóa khi rút thẻ hoặc deselect |
| PIN salt (User) | EEPROM, 8 bytes | Không mã hóa |
| PIN salt (Admin) | EEPROM, 8 bytes | Không mã hóa |
| PIN retry counter | EEPROM | Tự quản lý |

**Cơ chế unlock thẻ:**
```
Nhập PIN → PBKDF2(PIN, salt, 200 vòng lặp) → PIN-Key (16 bytes)
         → AES-128-CBC.Decrypt(wrappedMasterKey, PIN-Key) → Master Key
         → Lưu Master Key vào RAM transient
         → Dùng Master Key để đọc/ghi dữ liệu mã hóa
         → Khi rút thẻ: Master Key tự động xóa khỏi RAM
```

**Bảo vệ brute-force PIN:**
- User PIN: tối đa 3 lần sai liên tiếp → thẻ bị khóa. Cần Admin PIN để mở lại
- Admin PIN: tối đa 5 lần sai liên tiếp → khóa vĩnh viễn, không thể khôi phục

**Danh sách lệnh APDU (INS):**

| INS | Tên lệnh | CLA | P1 | P2 | Mô tả |
|-----|----------|-----|----|----|-------|
| 0x01 | INS_CREATE_PIN | 0x00 | 0x00 | 0x00 | Khởi tạo PIN user mặc định lần đầu |
| 0x07 | INS_WRITE_INFO | 0x00 | 0x00 | 0x00 | Ghi tên + SĐT (AES-128-CBC, cần Master Key) |
| 0x08 | INS_READ_INFO | 0x00 | 0x00 | 0x00 | Đọc tên + SĐT (giải mã AES) |
| 0x17 | INS_SET_CUSTOMER_ID | 0x00 | 0x00 | 0x00 | Ghi customerID lên thẻ |
| 0x18 | INS_GET_CUSTOMER_ID | 0x00 | 0x00 | 0x00 | Đọc customerID từ thẻ |
| 0x1B | INS_SIGN_CHALLENGE | 0x00 | 0x00 | 0x00 | Ký 16 bytes challenge bằng RSA private key |
| 0x1D | INS_GENERATE_RSA_KEYPAIR | 0x00 | 0x00 | 0x00 | Sinh cặp khóa RSA-1024 mới trên thẻ |
| 0x1E | INS_GET_PUBLIC_KEY | 0x00 | 0x00 | 0x00 | Đọc RSA public key (128 bytes) |
| 0x1F | INS_VERIFY_ADMIN_PIN | 0x00 | 0x00 | 0x00 | Xác minh admin PIN, unlock quyền ghi đè |
| 0x20 | INS_VERIFY_USER_PIN | 0x00 | 0x00 | 0x00 | Xác minh user PIN, unlock Master Key |
| 0x21 | INS_RESET_USER_PIN | 0x00 | 0x00 | 0x00 | Đặt lại PIN user (cần Admin PIN đã verify) |
| 0x22 | INS_CREATE_ADMIN_PIN | 0x00 | 0x00 | 0x00 | Khởi tạo Admin PIN lần đầu |

**Mã trạng thái phản hồi (SW1 SW2):**

| SW | Ý nghĩa |
|----|---------|
| 90 00 | Thành công |
| 69 82 | Chưa xác thực (cần verify PIN trước) |
| 69 83 | Thẻ bị khóa (PIN sai quá số lần cho phép) |
| 6A 80 | Dữ liệu đầu vào không hợp lệ |
| 6F 00 | Lỗi không xác định |

---

### 2.2 SmardCard Terminal – Ứng dụng Desktop Kotlin

**Vai trò:** Ứng dụng Kotlin Desktop (Compose Multiplatform) chạy trên máy tính tại quầy lễ tân và tại các điểm trò chơi. Đây là cầu nối giữa thẻ Java Card và Backend API.

**Kết nối card reader:** Sử dụng `javax.smartcardio` (có sẵn trong JDK) qua chuẩn PC/SC. Tương thích với các đầu đọc thẻ tiêu chuẩn như ACR122U, ACR1252U, Gemalto PC Twin Reader.

**SmartCardManager.kt** – lớp trung tâm xử lý mọi giao tiếp APDU:
```
SmartCardManager
├── connectReader()         → Tìm và kết nối card reader PC/SC
├── detectCard()            → Phát hiện thẻ được cắm vào
├── selectApplet()          → Gửi SELECT AID để chọn ParkCard Applet
├── getCustomerID()         → INS_GET_CUSTOMER_ID
├── writeNewCard(userId, name, phone, customerID)
│   ├── INS_SET_CUSTOMER_ID
│   ├── INS_GENERATE_RSA_KEYPAIR
│   ├── INS_GET_PUBLIC_KEY → lấy publicKey
│   ├── INS_CREATE_ADMIN_PIN
│   ├── INS_CREATE_PIN
│   └── INS_WRITE_INFO
├── overwriteCard(newUserId, name, phone, customerID)
│   ├── INS_VERIFY_ADMIN_PIN
│   ├── INS_RESET_USER_PIN
│   ├── INS_SET_CUSTOMER_ID
│   ├── INS_GENERATE_RSA_KEYPAIR → keypair MỚI
│   ├── INS_GET_PUBLIC_KEY
│   ├── INS_WRITE_INFO
│   └── INS_CREATE_PIN
├── signChallenge(challenge: ByteArray): ByteArray
│   └── INS_SIGN_CHALLENGE → trả về signature 128 bytes
└── getPublicKey(): ByteArray
    └── INS_GET_PUBLIC_KEY → trả về 128 bytes
```

**AID của ParkCard Applet:** `F0 50 41 52 4B 43 41 52 44` (hex của "FOPARKCARD")

**Các màn hình (tab) của SmardCard Terminal:**

```
SmardCard Terminal
├── Tab 1: Tạo Thẻ Mới
│   ├── Form: Tên khách, SĐT
│   ├── Tìm hoặc tạo user trên backend
│   ├── Cắm thẻ → Ghi APDU → Đăng ký backend
│   └── Hiển thị kết quả: "Thẻ đã tạo thành công – CustomerID: C001234"
│
├── Tab 2: Ghi Đè Thẻ Cũ (Card Recycle)
│   ├── Quét thẻ → đọc customerID → gọi API lấy thông tin
│   ├── Hiển thị: "Thẻ cũ của [Tên]. Số dư còn: [X]đ – Hoàn tiền mặt"
│   ├── Form: Thông tin khách mới
│   └── Ghi đè APDU → Đăng ký backend
│
├── Tab 3: Chơi Game (Terminal Mode)
│   ├── Chọn Game đang phụ trách
│   ├── Màn hình chờ: "Chờ khách quẹt thẻ..."
│   ├── Khi có thẻ: Tự động đọc → RSA sign → gọi /play API
│   └── Hiển thị: Tên khách, tiền trừ, số dư còn lại
│
├── Tab 4: Yêu Cầu Thẻ (Card Requests)
│   ├── Danh sách PENDING requests từ app
│   ├── Nút "Duyệt + Tạo Thẻ"
│   └── Nút "Từ Chối"
│
└── Tab 5: Nạp Tiền Tại Quầy
    ├── Tìm user (quét thẻ hoặc nhập SĐT)
    ├── Nhập số tiền mặt
    └── Gọi POST /api/admin/topup
```

---

### 2.3 Backend – Ktor Server

**Công nghệ:** Kotlin + Ktor 2.x + Exposed ORM + MySQL

**Cổng:** 8080 (HTTP trong môi trường nội bộ LAN công viên)

**Xác thực:** JWT với 3 role phân biệt:
- `USER` – khách hàng (AppCongVien Android)
- `TERMINAL` – SmardCard Terminal tại quầy/trò chơi
- `ADMIN` – appdesktop quản trị

**Danh sách API đầy đủ:**

```
=== AUTH ===
POST   /api/auth/register                    → Đăng ký tài khoản
POST   /api/auth/login                       → Đăng nhập, nhận JWT
GET    /api/auth/health                      → Kiểm tra server

=== USER ===
GET    /api/user/profile                     → Lấy thông tin cá nhân
PUT    /api/user/profile                     → Cập nhật thông tin
POST   /api/user/change-password             → Đổi mật khẩu

=== CARD (User) ===
GET    /api/cards                            → Danh sách thẻ của user
POST   /api/cards/request                    → Yêu cầu tạo thẻ qua app
GET    /api/cards/request/status             → Trạng thái yêu cầu hiện tại
POST   /api/cards/{cardId}/block             → Khóa thẻ
POST   /api/cards/{cardId}/unblock           → Mở khóa thẻ

=== GAME ===
GET    /api/games                            → Danh sách trò chơi (phân trang)
GET    /api/games/featured                   → Trò chơi nổi bật
GET    /api/games/categories                 → Danh sách thể loại
GET    /api/games/{gameId}                   → Chi tiết trò chơi
POST   /api/games/{gameId}/play              → Quẹt thẻ chơi game (TERMINAL only)
  Body: { customerID, terminalId, challenge, signature }
  Response: { success, gameName, amountCharged, remainingBalance, userName }

=== WALLET ===
GET    /api/wallet/balance                   → Số dư hiện tại
POST   /api/wallet/topup                     → Nạp tiền online
GET    /api/wallet/transactions              → Lịch sử giao dịch (filter theo cardId)
GET    /api/wallet/payments                  → Lịch sử thanh toán

=== GAME REVIEWS ===
POST   /api/reviews                          → Viết đánh giá
GET    /api/games/{gameId}/reviews           → Xem đánh giá của trò chơi
GET    /api/reviews/my-review                → Đánh giá của mình
PUT    /api/reviews/{reviewId}               → Sửa đánh giá
DELETE /api/reviews/{reviewId}              → Xóa đánh giá

=== NOTIFICATIONS ===
GET    /api/notifications                    → Danh sách thông báo
GET    /api/notifications/unread-count       → Số thông báo chưa đọc
POST   /api/notifications/{id}/read          → Đánh dấu đã đọc
POST   /api/notifications/read-all           → Đọc tất cả

=== SUPPORT ===
GET    /api/support/messages                 → Lịch sử chat
POST   /api/support/messages                 → Gửi tin nhắn
WS     /api/support/chat                     → WebSocket realtime chat

=== ANNOUNCEMENTS ===
GET    /api/announcements                    → Carousel banners

=== ADMIN – DASHBOARD ===
GET    /api/admin/dashboard/stats            → Thống kê tổng quan
GET    /api/admin/dashboard/revenue          → Biểu đồ doanh thu

=== ADMIN – USER MANAGEMENT ===
GET    /api/admin/users                      → Danh sách users
POST   /api/admin/topup                      → Nạp tiền tại quầy (CASH)
POST   /api/admin/users/{id}/adjust-balance  → Điều chỉnh số dư
POST   /api/admin/users/{id}/update-membership → Nâng hạng

=== ADMIN – CARD MANAGEMENT ===
POST   /api/admin/cards/create               → Tạo record thẻ mới
  Body: { userId, customerID, cardName, rsaPublicKey }
POST   /api/admin/cards/return               → Thu thẻ về (status=INACTIVE, hoàn tiền)
  Body: { cardId }
POST   /api/admin/cards/reassign             → Ghi đè thẻ cũ cho khách mới
  Body: { cardId, newUserId, newCustomerID, newRsaPublicKey }
GET    /api/admin/cards                      → Danh sách thẻ (filter)
GET    /api/admin/cards/by-customer/{id}     → Tra cứu theo customerID

=== ADMIN – CARD REQUESTS ===
GET    /api/admin/card-requests              → Danh sách yêu cầu PENDING
PUT    /api/admin/card-requests/{id}/resolve → Duyệt/Từ chối yêu cầu

=== ADMIN – CONTENT ===
POST   /api/admin/games                      → Tạo trò chơi
PUT    /api/admin/games/{id}                 → Sửa trò chơi
POST   /api/admin/announcements              → Tạo banner
POST   /api/admin/notifications/broadcast    → Gửi thông báo hàng loạt

=== UPLOAD ===
POST   /api/upload                           → Upload hình ảnh
```

**Luồng xử lý play game (quan trọng nhất):**
```kotlin
// POST /api/games/{gameId}/play
fun playGame(gameId, customerID, terminalId, challenge, signature):

1.  Tìm card theo customerID → lấy cardId, userId, rsaPublicKey, status
2.  if (card.status == BLOCKED) → return 403 "Thẻ đã bị khóa"
3.  RSA.verify(data=challenge, signature=signature, publicKey=rsaPublicKey)
    → if (!valid) → return 401 "Xác thực thẻ thất bại (thẻ giả)" + log cảnh báo
4.  Lấy game.pricePerTurn
5.  Lấy user.currentBalance
6.  if (balance < pricePerTurn) → return 400 "Số dư không đủ: còn [X]đ, cần [Y]đ"
7.  BEGIN TRANSACTION
    a. UPDATE users SET currentBalance = currentBalance - pricePerTurn WHERE userId
    b. INSERT balance_transactions (userId, cardId, type=GAME_PLAY, amount=-pricePerTurn,
                                    gameId, terminalId, description)
    c. INSERT game_play_logs (userId, gameId, terminalId, cardId, playedAt)
    d. INSERT notifications (userId, type=GAME_PLAY, title="Chơi game thành công",
                             message="[gameName] – Trừ [price]đ. Còn [balance]đ")
    COMMIT
8.  return { success:true, gameName, amountCharged, remainingBalance, userName }
```

---

### 2.4 AppCongVien – Ứng dụng Android

**Công nghệ:** Kotlin + Jetpack Compose + Retrofit + Kotlin Coroutines

**Màn hình và chức năng:**

```
AppCongVien
├── Đăng nhập / Đăng ký / Quên mật khẩu
│
├── HomeScreen
│   ├── Hiển thị số dư hiện tại (lấy từ /api/wallet/balance)
│   ├── Danh sách thẻ đang liên kết (tên thẻ, trạng thái)
│   ├── Carousel banner thông báo/khuyến mãi
│   └── Quick actions: [Nạp tiền] [Thẻ của tôi] [Lịch sử]
│
├── CardInfoScreen
│   ├── Danh sách thẻ liên kết với tài khoản
│   ├── Trạng thái từng thẻ (ACTIVE / BLOCKED / INACTIVE)
│   ├── Nút "Khóa thẻ" / "Mở khóa thẻ"
│   └── Nút "Yêu cầu tạo thẻ" (khi chưa có thẻ nào)
│
├── CardRequestScreen (Yêu cầu tạo thẻ)
│   ├── Form: Ghi chú vị trí (VD: "Tôi đang ở cổng A")
│   ├── Gọi POST /api/cards/request
│   └── Hiển thị trạng thái: PENDING / APPROVED / REJECTED
│
├── BalanceScreen
│   ├── Số dư hiện tại, điểm thưởng, hạng thành viên
│   └── Nút "Nạp tiền"
│
├── TopUpScreen
│   ├── Chọn số tiền (50k, 100k, 200k, 500k, tùy chọn)
│   ├── Chọn phương thức (MoMo, VNPay, Chuyển khoản)
│   └── Gọi POST /api/wallet/topup
│
├── PaymentHistoryScreen (Lịch sử giao dịch)
│   ├── Danh sách giao dịch (TOP_UP, GAME_PLAY, REFUND)
│   ├── Filter theo loại giao dịch
│   ├── Filter theo thẻ (nếu có nhiều thẻ)
│   │   ├── [Tất cả]
│   │   ├── [Thẻ A – Tên]
│   │   └── [Thẻ B – Tên]
│   └── Mỗi GAME_PLAY hiển thị: tên trò chơi, tiền trừ, số dư sau
│
├── GameListScreen
│   ├── Danh sách trò chơi phân trang
│   ├── Filter theo thể loại
│   └── Tìm kiếm theo tên
│
├── GameDetailScreen
│   ├── Thông tin chi tiết (giá, yêu cầu tuổi/chiều cao, vị trí)
│   ├── Đánh giá trung bình và danh sách reviews
│   └── Nút "Viết đánh giá"
│
├── NotificationsScreen
│   ├── GAME_PLAY: 🎮 icon, hiển thị tên game + số tiền trừ + số dư
│   ├── TOP_UP: 💰 icon, hiển thị số tiền nạp + số dư mới
│   ├── CARD_APPROVED: 🎴 icon, "Thẻ đã duyệt, đến quầy nhận"
│   └── Badge đếm số chưa đọc trên HomeScreen
│
├── SupportChatScreen
│   ├── Giao tiếp realtime qua WebSocket
│   └── Lịch sử tin nhắn
│
└── ProfileScreen
    ├── Thông tin cá nhân
    ├── Điểm thưởng và hạng thành viên
    └── Đổi mật khẩu / Đăng xuất
```

**Kiến trúc Android (MVVM + Repository):**
```
UI (Composable Screen)
    ↕ observe StateFlow
ViewModel (state management, coroutines)
    ↕ call suspend functions
Repository (business logic)
    ↕ call API
ApiService (Retrofit interface)
    ↕ HTTP
Backend
```

---

### 2.5 appdesktop Admin – Ứng dụng Quản Trị Desktop

**Công nghệ:** Kotlin + Compose Multiplatform (JVM)

**Màn hình:**

```
appdesktop Admin
├── LoginScreen (ADMIN credentials)
│
├── DashboardScreen
│   ├── Cards thống kê: Tổng users, Thẻ đang hoạt động, Doanh thu hôm nay, Số lượt chơi
│   ├── Biểu đồ doanh thu (chọn: ngày/tuần/tháng/năm)
│   └── Bảng giao dịch gần nhất
│
├── UserManagementScreen
│   ├── Danh sách users (phân trang, tìm kiếm)
│   ├── Chi tiết user: số dư, hạng, danh sách thẻ
│   ├── Nút "Nạp tiền": dialog nhập số tiền CASH → POST /api/admin/topup
│   ├── Nút "Điều chỉnh số dư": tăng/giảm tùy ý (có lý do) → POST /api/admin/users/{id}/adjust-balance
│   └── Nút "Nâng hạng thành viên"
│
├── GameManagementScreen
│   ├── Thêm/sửa/xóa trò chơi
│   └── Bật/tắt trạng thái (ACTIVE/MAINTENANCE/CLOSED)
│
├── AnnouncementScreen
│   ├── Quản lý banner carousel
│   └── Thêm banner (title, imageUrl, link)
│
├── FinanceScreen
│   ├── Báo cáo doanh thu chi tiết
│   ├── Lọc theo ngày/tuần/tháng/năm
│   └── Phân tích theo trò chơi (trò nào kiếm nhiều nhất)
│
├── NotificationScreen
│   └── Gửi thông báo đến tất cả / từng user
│
└── SupportScreen
    ├── Danh sách ticket hỗ trợ
    └── Trả lời trực tiếp qua chat
```

---

## 3. Cơ Sở Dữ Liệu Chi Tiết

### 3.1 Sơ đồ quan hệ

```
accounts ──────────────── users
    (accountId=userId)       │
                             │ 1:N
                          cards ──────── balance_transactions
                          (cardId)           (cardId nullable FK)
                             │
                             │ 1:N
                         card_requests
                         (per user)

games ────────── game_play_logs ──────── users
(gameId)            (gameId FK)           (userId FK)
    │               (cardId FK)
    │
    └── game_reviews (userId FK)
    └── terminals

users ──── notifications
      ──── support_messages
      ──── payment_records
      ──── announcements (không liên kết user)
```

### 3.2 Định nghĩa bảng chi tiết

**Bảng `accounts`**
```sql
accounts
├── account_id      VARCHAR(36) PK (UUID)
├── phone_number    VARCHAR(15) UNIQUE NOT NULL
├── password_hash   VARCHAR(255) NOT NULL        -- BCrypt
├── role            VARCHAR(20) NOT NULL         -- USER | TERMINAL | ADMIN
└── created_at      TIMESTAMP DEFAULT NOW()
```

**Bảng `users`**
```sql
users
├── user_id          VARCHAR(36) PK = account_id
├── full_name        VARCHAR(100) NOT NULL
├── phone_number     VARCHAR(15) NOT NULL
├── email            VARCHAR(100) NULL
├── date_of_birth    DATE NULL
├── gender           VARCHAR(10) NULL
├── avatar_url       VARCHAR(500) NULL
├── current_balance  DECIMAL(15,0) DEFAULT 0     -- Tính bằng VNĐ (đồng)
├── loyalty_points   INT DEFAULT 0
├── membership_level VARCHAR(20) DEFAULT 'STANDARD'  -- STANDARD|SILVER|GOLD|PLATINUM
├── referral_code    VARCHAR(20) UNIQUE
├── status           VARCHAR(20) DEFAULT 'ACTIVE'    -- ACTIVE | SUSPENDED
└── created_at       TIMESTAMP DEFAULT NOW()
```

**Bảng `cards`**
```sql
cards
├── card_id          VARCHAR(36) PK (UUID)
├── user_id          VARCHAR(36) NULL FK → users   -- NULL khi thẻ chưa gắn user
├── customer_id      VARCHAR(50) UNIQUE NOT NULL    -- Định danh ghi trên thẻ vật lý
├── card_name        VARCHAR(100) NULL               -- "Thẻ con 1", "Thẻ con 2"
├── rsa_public_key   TEXT NULL                       -- Base64 RSA-1024 public key
├── status           VARCHAR(20) DEFAULT 'ACTIVE'   -- ACTIVE | BLOCKED | INACTIVE
├── issued_at        TIMESTAMP DEFAULT NOW()
├── blocked_at       TIMESTAMP NULL
├── blocked_reason   VARCHAR(500) NULL
└── last_used_at     TIMESTAMP NULL
```

**Bảng `balance_transactions`**
```sql
balance_transactions
├── transaction_id   VARCHAR(36) PK (UUID)
├── user_id          VARCHAR(36) NOT NULL FK → users
├── card_id          VARCHAR(36) NULL FK → cards    -- Thẻ nào thực hiện (GAME_PLAY)
├── type             VARCHAR(30) NOT NULL
│                    -- TOP_UP | GAME_PLAY | REFUND | ADJUSTMENT | CASH_TOPUP
├── amount           DECIMAL(15,0) NOT NULL          -- Dương: vào, Âm: ra
├── description      VARCHAR(500) NOT NULL
├── game_id          INT NULL FK → games             -- Chỉ có khi GAME_PLAY
├── terminal_id      VARCHAR(36) NULL FK → terminals
└── created_at       TIMESTAMP DEFAULT NOW()
```

**Bảng `card_requests`**
```sql
card_requests
├── request_id       VARCHAR(36) PK (UUID)
├── user_id          VARCHAR(36) NOT NULL FK → users
├── status           VARCHAR(20) DEFAULT 'PENDING'  -- PENDING | APPROVED | REJECTED
├── note             TEXT NULL                        -- Ghi chú từ user (vị trí, lý do)
├── resolved_by      VARCHAR(36) NULL FK → admins   -- Admin duyệt
├── resolved_at      TIMESTAMP NULL
└── created_at       TIMESTAMP DEFAULT NOW()
```

**Bảng `game_play_logs`**
```sql
game_play_logs
├── log_id           VARCHAR(36) PK (UUID)
├── user_id          VARCHAR(36) NOT NULL FK → users
├── game_id          INT NOT NULL FK → games
├── card_id          VARCHAR(36) NOT NULL FK → cards
├── terminal_id      VARCHAR(36) NULL FK → terminals
├── amount_charged   DECIMAL(15,0) NOT NULL
└── played_at        TIMESTAMP DEFAULT NOW()
```

**Bảng `games`**
```sql
games
├── game_id          INT PK AUTO_INCREMENT
├── name             VARCHAR(200) NOT NULL
├── description      TEXT NULL
├── category         VARCHAR(100) NOT NULL
├── price_per_turn   DECIMAL(15,0) NOT NULL
├── duration_minutes INT NULL
├── location         VARCHAR(200) NULL               -- "Khu A, cổng số 3"
├── thumbnail_url    VARCHAR(500) NULL
├── age_required     INT NULL                         -- Tuổi tối thiểu
├── height_required  INT NULL                         -- Chiều cao tối thiểu (cm)
├── max_capacity     INT DEFAULT 1
├── risk_level       VARCHAR(20) DEFAULT 'LOW'        -- LOW | MEDIUM | HIGH
├── is_featured      BOOLEAN DEFAULT FALSE
├── status           VARCHAR(20) DEFAULT 'ACTIVE'    -- ACTIVE | MAINTENANCE | CLOSED
├── avg_rating       DECIMAL(3,1) DEFAULT 0.0
└── total_reviews    INT DEFAULT 0
```

**Bảng `terminals`**
```sql
terminals
├── terminal_id      VARCHAR(36) PK
├── account_id       VARCHAR(36) FK → accounts       -- Tài khoản TERMINAL
├── name             VARCHAR(100) NOT NULL            -- "Terminal Tàu lượn #1"
├── location         VARCHAR(200) NULL
├── current_game_id  INT NULL FK → games              -- Game đang phụ trách
├── status           VARCHAR(20) DEFAULT 'ACTIVE'
└── last_seen_at     TIMESTAMP NULL
```

### 3.3 Tính năng gia đình nhiều thẻ

Trường `card_id` trong `balance_transactions` cho phép theo dõi giao dịch theo từng thẻ:

```
SELECT t.*, c.card_name, g.name as game_name
FROM balance_transactions t
LEFT JOIN cards c ON t.card_id = c.card_id
LEFT JOIN games g ON t.game_id = g.game_id
WHERE t.user_id = ?
  AND (? IS NULL OR t.card_id = ?)   -- filter theo thẻ (tùy chọn)
ORDER BY t.created_at DESC
LIMIT 20 OFFSET ?
```

Kết quả trên app Android:
```
Tài khoản phụ huynh (balance: 255.000đ)

[Tất cả] [Thẻ A – Con 1] [Thẻ B – Con 2] [Thẻ C – Con 3]

→ Chọn "Thẻ B – Con 2":
  14:32  🎮 Đu quay          –10.000đ   (Thẻ B)
  13:55  🎮 Xe hơi điện      –15.000đ   (Thẻ B)
  13:10  🎮 Tàu lượn         –20.000đ   (Thẻ B)
```

---

## 4. Bảo Mật Chi Tiết

### 4.1 Mô hình bảo mật tổng thể

```
Lớp 1 – Thẻ vật lý (Java Card):
  - Dữ liệu mã hóa AES-128-CBC trên EEPROM
  - PIN bảo vệ với PBKDF2 + retry limit
  - RSA private key không bao giờ rời khỏi chip thẻ

Lớp 2 – Giao tiếp Terminal ↔ Thẻ (APDU):
  - Các lệnh nhạy cảm cần verify PIN trước
  - PIN truyền từ terminal xuống thẻ được mã hóa bằng Session Key

Lớp 3 – Giao tiếp Terminal ↔ Backend (API):
  - HTTPS (TLS) cho tất cả giao tiếp
  - JWT token riêng cho từng loại client (USER/TERMINAL/ADMIN)
  - RSA Challenge-Response xác thực mỗi giao dịch chơi game

Lớp 4 – Backend:
  - Mật khẩu người dùng: BCrypt (cost factor 12)
  - Database transaction đảm bảo tính nguyên tử giao dịch
  - Rate limiting: max 10 request /play per terminal per phút
  - Audit log đầy đủ: cardId, terminalId, timestamp, gameId, IP
```

### 4.2 RSA Challenge-Response chi tiết

Mục đích: Ngăn kẻ tấn công sao chép (clone) thẻ bằng cách đọc dữ liệu từ thẻ thật.

```
Vì sao clone thẻ không hoạt động:
- Kẻ tấn công có thể đọc được customerID từ thẻ (plaintext)
- Kẻ tấn công KHÔNG thể đọc RSA private key (được mã hóa AES + bảo vệ bởi chip)
- Kẻ tấn công chỉ biết customerID, nhưng không có private key để ký challenge

Luồng xác thực mỗi lần quẹt thẻ:
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│ SmardCard    │         │  Java Card   │         │   Backend    │
│ Terminal     │         │  (ParkCard)  │         │   (Ktor)     │
└──────┬───────┘         └──────┬───────┘         └──────┬───────┘
       │                        │                        │
       │  INS_GET_CUSTOMER_ID   │                        │
       │───────────────────────>│                        │
       │<── customerID ─────────│                        │
       │                        │                        │
       │  challenge = random(16 bytes)                   │
       │                        │                        │
       │  INS_SIGN_CHALLENGE    │                        │
       │  (challenge as Lc)     │                        │
       │───────────────────────>│                        │
       │                        │ load privKey to RAM    │
       │                        │ sig = RSA.sign(chal)   │
       │                        │ clear privKey from RAM │
       │<── signature (128B) ───│                        │
       │                        │                        │
       │  POST /games/{id}/play { customerID, challenge, signature }
       │────────────────────────────────────────────────>│
       │                        │         findCard(customerID)
       │                        │         RSA.verify(challenge, sig, pubKey)
       │                        │         if OK → deduct balance
       │<─────────── { success, amountCharged, balance } │
```

**Tại sao an toàn:**
- `challenge` được tạo mới ngẫu nhiên mỗi lần → Replay attack không thể (signature cũ không dùng lại được)
- RSA private key chỉ tồn tại trong RAM thẻ trong thời gian ký, bị xóa ngay sau đó
- Backend lưu public key khi tạo thẻ → chỉ thẻ gốc mới có thể ký đúng

### 4.3 Tái sử dụng thẻ an toàn

```
Quy trình ghi đè đảm bảo xóa sạch dữ liệu cũ:

Bước 1: INS_VERIFY_ADMIN_PIN → Xác minh nhân viên có quyền ghi đè
Bước 2: INS_RESET_USER_PIN → Xóa PIN user cũ (khách cũ không thể dùng)
Bước 3: INS_SET_CUSTOMER_ID → Ghi customerID mới
Bước 4: INS_GENERATE_RSA_KEYPAIR → Sinh keypair RSA HOÀN TOÀN MỚI trên chip
         (keypair cũ bị ghi đè, public key cũ trên server sẽ không verify được)
Bước 5: INS_GET_PUBLIC_KEY → Đọc public key mới
Bước 6: INS_WRITE_INFO → Ghi thông tin khách mới (mã hóa AES)
Bước 7: INS_CREATE_PIN → Tạo PIN user mới

Backend (POST /api/admin/cards/reassign):
- Unlink thẻ khỏi user cũ
- Link thẻ sang user mới
- Cập nhật rsaPublicKey = publicKey mới
- Set status = ACTIVE

→ Sau bước này: public key của user cũ không còn trên server
  → Kể cả có giữ lại signature từ thẻ cũ cũng không thể giả mạo
```

### 4.4 Khóa thẻ khi mất

```
Khách báo mất thẻ qua app:
POST /api/cards/{cardId}/block
→ Backend: card.status = BLOCKED, ghi blockedAt, blockedReason

Khi thẻ bị BLOCKED:
- Gọi POST /api/games/{gameId}/play → Backend trả 403 ngay lập tức
- Kể cả thẻ thật (RSA signature đúng) cũng bị từ chối
- Số dư vẫn còn nguyên trong tài khoản

Khách đến quầy nhận thẻ mới:
- Nhân viên tạo thẻ mới, gắn vào cùng tài khoản
- Số dư cũ vẫn còn → khách không mất tiền
```

---

## 5. Các Quy Trình Nghiệp Vụ Đầy Đủ

### 5.1 Quy trình khách lần đầu đến – không có app

```
1. Khách đến quầy lễ tân
   └─ Nhân viên mở SmardCard Terminal, Tab "Tạo Thẻ Mới"

2. Nhân viên nhập tên + SĐT khách
   ├─ SmardCard gọi POST /api/user/find-or-create { name, phone }
   └─ Backend trả về { userId, customerID }  (tạo account nếu chưa có)

3. Cắm thẻ Java Card trắng vào reader
   └─ SmardCard tự động thực hiện chuỗi APDU:
      a. SELECT AID (chọn ParkCard Applet)
      b. INS_SET_CUSTOMER_ID (ghi customerID)
      c. INS_GENERATE_RSA_KEYPAIR (sinh keypair RSA)
      d. INS_GET_PUBLIC_KEY (đọc public key)
      e. INS_CREATE_ADMIN_PIN (ghi admin PIN: mặc định bảo mật)
      f. INS_CREATE_PIN (ghi user PIN: mặc định)
      g. INS_WRITE_INFO (ghi tên + SĐT mã hóa AES)

4. SmardCard gọi POST /api/admin/cards/create
   Body: { userId, customerID, cardName, rsaPublicKey }
   → Backend lưu thẻ vào DB, liên kết với userId

5. Nạp tiền
   ├─ Nhân viên nhập số tiền mặt khách đưa
   ├─ SmardCard gọi POST /api/admin/topup { userId, amount, method: CASH }
   └─ Backend cộng balance, ghi BalanceTransaction (type=CASH_TOPUP)

6. Giao thẻ → Khách ra khu vui chơi

7. Tại cổng trò chơi
   ├─ Khách đặt thẻ vào reader → SmardCard Terminal tự động xử lý
   ├─ RSA sign → Gọi /play API → Backend trừ tiền
   └─ Terminal hiển thị: "Xin chào [Tên]! Trừ 20.000đ. Còn: 80.000đ"

8. Hết tiền → Quay lại quầy nạp thêm (bước 5)

9. Trả thẻ khi về
   ├─ Nhân viên quét thẻ → SmardCard đọc customerID
   ├─ Gọi GET /api/admin/cards/by-customer/{customerID}
   │   → Hiển thị: "Số dư còn: 35.000đ – Hoàn tiền mặt cho khách"
   ├─ Gọi POST /api/admin/cards/return
   │   → Backend: ghi REFUND transaction (trừ hết balance về 0), set status=INACTIVE
   └─ Thẻ được thu về, sẵn sàng ghi đè cho khách tiếp theo
```

### 5.2 Quy trình khách đã có app – yêu cầu thẻ từ xa

```
1. Tải AppCongVien → Đăng ký tài khoản bằng SĐT + mật khẩu

2. Trong app → "Thẻ của tôi" → "Yêu cầu tạo thẻ"
   ├─ Nhập ghi chú: "Tôi đang ở cổng phía Bắc"
   └─ Gọi POST /api/cards/request { note }

3. SmardCard Terminal nhận yêu cầu trong Tab "Yêu Cầu Thẻ"
   ├─ Hiển thị: "Nguyễn Văn A (0901234567) – Ở cổng phía Bắc"
   ├─ Nhân viên bấm "Duyệt + Tạo Thẻ"
   ├─ Chạy chuỗi APDU như quy trình 5.1 bước 3
   └─ Gọi PUT /api/admin/card-requests/{id}/resolve { action: APPROVED }

4. Backend tạo notification → AppCongVien nhận push notification:
   "🎴 Yêu cầu tạo thẻ đã được duyệt. Đến quầy nhận thẻ tại cổng phía Bắc."

5. Khách đến quầy nhận thẻ → nạp tiền → ra chơi

6. Mỗi lần quẹt thẻ tại trò chơi, app nhận notification ngay lập tức:
   ┌──────────────────────────────────┐
   │ 🎮 Chơi game thành công          │
   │ Tàu lượn siêu tốc                │
   │ Trừ: 20.000đ                     │
   │ Số dư còn lại: 80.000đ           │
   └──────────────────────────────────┘
```

### 5.3 Quy trình gia đình nhiều thẻ

```
1. Phụ huynh đăng ký tài khoản, nạp tiền 300.000đ

2. Đến quầy, nhân viên tạo 3 thẻ cho 3 thành viên:
   ├─ Thẻ A – "Con 1 (Bé Nam)" → liên kết userId phụ huynh, cardName="Thẻ con 1"
   ├─ Thẻ B – "Con 2 (Bé Lan)" → liên kết userId phụ huynh, cardName="Thẻ con 2"
   └─ Thẻ C – "Con 3 (Bé Hùng)" → liên kết userId phụ huynh, cardName="Thẻ con 3"

3. Cả 3 thẻ dùng chung ví 300.000đ của phụ huynh

4. Trong ngày:
   ├─ Thẻ A quẹt Tàu lượn      → balance_transaction { cardId=A, -20.000đ }
   ├─ Thẻ B quẹt Đu quay       → balance_transaction { cardId=B, -10.000đ }
   ├─ Thẻ C quẹt Xe hơi điện   → balance_transaction { cardId=C, -15.000đ }
   └─ Balance chung còn: 255.000đ

5. Phụ huynh mở app xem lịch sử:
   Tab [Thẻ con 1]: Tàu lượn -20k
   Tab [Thẻ con 2]: Đu quay -10k
   Tab [Thẻ con 3]: Xe hơi -15k
   Tab [Tất cả]: Tổng cộng -45k

6. Nếu một thẻ bị mất: chỉ khóa thẻ đó, 2 thẻ còn lại vẫn dùng bình thường
```

---

## 6. Luồng Dữ Liệu Tổng Thể

```
                    ┌───────────────────────────────────────────┐
                    │              Backend (Ktor)               │
                    │                                           │
     Android App ───┤── GET /wallet/balance ──► users.balance   │
                    │                                           │
                    │── POST /wallet/topup ──► balance+=amount  │
                    │                     └──► notification     │
                    │                                           │
 SmardCard ─────────┤── POST /admin/cards/create               │
 Terminal           │   (customerID, rsaPublicKey)              │
                    │                     └──► INSERT card      │
                    │                                           │
                    │── POST /games/{id}/play                   │
                    │   (customerID, challenge, signature)       │
                    │     1. lookup card by customerID          │
                    │     2. RSA.verify(challenge, sig, pubKey) │
                    │     3. balance -= pricePerTurn            │
                    │     4. INSERT balance_transaction         │
                    │     5. INSERT game_play_log               │
                    │     6. INSERT notification                │
                    │           └──► push to Android App ───────┼──► App nhận
                    │                                           │
 Admin Desktop ─────┤── GET /admin/dashboard/stats             │
                    │── POST /admin/topup                       │
                    │── GET /admin/card-requests                │
                    └───────────────────────────────────────────┘
                                      │
                                      │ ORM (Exposed)
                                      ▼
                    ┌───────────────────────────────────────────┐
                    │               MySQL Database              │
                    │  accounts, users, cards, card_requests    │
                    │  games, balance_transactions              │
                    │  game_play_logs, notifications            │
                    │  support_messages, announcements          │
                    └───────────────────────────────────────────┘
```

---

## 7. Ví Dụ Request/Response API Thực Tế

### 7.1 Tạo thẻ mới

```http
POST /api/admin/cards/create
Authorization: Bearer {terminalJWT}
Content-Type: application/json

{
  "userId": "a1b2c3d4-...",
  "customerID": "C001234",
  "cardName": "Thẻ của Nguyễn Văn A",
  "rsaPublicKey": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQ..."
}

Response 201:
{
  "cardId": "f7e8d9c0-...",
  "customerId": "C001234",
  "userId": "a1b2c3d4-...",
  "status": "ACTIVE",
  "issuedAt": "2025-06-15T09:32:00Z"
}
```

### 7.2 Quẹt thẻ chơi game

```http
POST /api/games/3/play
Authorization: Bearer {terminalJWT}
Content-Type: application/json

{
  "customerID": "C001234",
  "terminalId": "T-TAULUAN-01",
  "challenge": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6",
  "signature": "3a7fbc9d1e2f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f..."
}

Response 200:
{
  "success": true,
  "gameName": "Tàu lượn siêu tốc",
  "amountCharged": 20000,
  "remainingBalance": 80000,
  "userName": "Nguyễn Văn A",
  "cardName": "Thẻ của Nguyễn Văn A"
}

Response 400 (không đủ tiền):
{
  "success": false,
  "error": "INSUFFICIENT_BALANCE",
  "message": "Số dư không đủ. Hiện có: 5.000đ, cần: 20.000đ"
}

Response 401 (thẻ giả):
{
  "success": false,
  "error": "INVALID_SIGNATURE",
  "message": "Xác thực thẻ thất bại"
}

Response 403 (thẻ bị khóa):
{
  "success": false,
  "error": "CARD_BLOCKED",
  "message": "Thẻ đã bị khóa. Liên hệ nhân viên để được hỗ trợ."
}
```

### 7.3 Lịch sử giao dịch theo thẻ

```http
GET /api/wallet/transactions?cardId=f7e8d9c0-...&page=0&size=20
Authorization: Bearer {userJWT}

Response 200:
{
  "transactions": [
    {
      "transactionId": "...",
      "type": "GAME_PLAY",
      "amount": -20000,
      "description": "Chơi Tàu lượn siêu tốc",
      "gameName": "Tàu lượn siêu tốc",
      "cardName": "Thẻ con 1",
      "createdAt": "2025-06-15T14:32:00Z"
    },
    {
      "transactionId": "...",
      "type": "CASH_TOPUP",
      "amount": 300000,
      "description": "Nạp tiền mặt tại quầy",
      "cardName": null,
      "createdAt": "2025-06-15T09:35:00Z"
    }
  ],
  "totalElements": 15,
  "currentBalance": 80000
}
```

---

## 8. Thống Kê Kỹ Thuật

| Thành phần | Công nghệ | Số dòng code |
|-----------|-----------|-------------|
| Java Card Applet (ParkCard) | Java Card API | ~800 |
| SmardCard Terminal | Kotlin + Compose Desktop | ~3.000 |
| Backend | Kotlin + Ktor | ~8.000 |
| AppCongVien Android | Kotlin + Jetpack Compose | ~13.000 |
| appdesktop Admin | Kotlin + Compose Desktop | ~4.000 |
| **Tổng cộng** | | **~28.800** |

| Metric | Giá trị |
|--------|---------|
| Bảng CSDL | 17 |
| API Endpoints | 45+ |
| Màn hình Android | 18+ |
| Màn hình Desktop (SmardCard) | 5 tab |
| Màn hình Desktop (Admin) | 8 màn hình |
| Lệnh APDU tùy chỉnh | 12 |
| Services (Backend) | 12 |
