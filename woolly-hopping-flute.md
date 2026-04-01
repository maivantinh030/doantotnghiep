# Kế Hoạch Tái Thiết Kế Hệ Thống Công Viên (v2)

## Context

Hệ thống hiện tại yêu cầu người dùng **mua vé trước** khi chơi game. Mô hình mới:

- **Bỏ** cơ chế mua game trước + voucher
- **Thẻ Java Card vật lý** = thẻ định danh khách hàng
- **Balance lưu trên Server** (Backend là nguồn truth), SmardCard terminal đồng bộ qua API
- **Tạo thẻ tại quầy**: Admin dùng SmardCard app ghi thông tin lên Java Card + đăng ký trên backend
- **Thông báo realtime**: Mỗi lần quẹt thẻ → push notification đến app của khách
- **Thẻ có thể trả lại & tái sử dụng**: Ghi đè thông tin khách mới lên thẻ cũ

---

## Kiến Trúc Hệ Thống

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

### Vai trò từng thành phần

| Thành phần | Vai trò |
|-----------|---------|
| **ParkCard** (Java Card Applet) | Lưu customerID, tên, SĐT trên thẻ vật lý. Thẻ là **định danh**, không lưu balance |
| **SmardCard** (Terminal Desktop) | Đọc/ghi thẻ qua APDU, gọi Backend API để trừ tiền + log giao dịch |
| **Backend** (Ktor) | Nguồn truth cho balance, giao dịch, thông báo |
| **AppCongVien** (Android) | Hiển thị balance, nhận notification realtime, yêu cầu tạo thẻ, nạp tiền qua app |
| **appdesktop** (Admin) | Quản lý user, xem thống kê, nạp tiền tại quầy cho user |

---

## Nhiều Thẻ Trong Cùng Tài Khoản (Gia Đình)

Schema hiện tại đã hỗ trợ nhiều thẻ per user (cards.userId không unique). Tận dụng điều này:

### Cơ chế: Ví chung, theo dõi theo thẻ

- **1 account** → **nhiều thẻ** (mỗi đứa trẻ 1 thẻ)
- **Balance chia sẻ** từ cùng 1 ví (phụ huynh nạp 1 lần)
- **Mỗi giao dịch ghi lại cardId** → biết đứa nào tiêu

```
Account phụ huynh: 300.000đ
├─ Thẻ A (con 1): Quẹt Tàu lượn → ghi: userId + cardId=A + -20k
├─ Thẻ B (con 2): Quẹt Đu quay  → ghi: userId + cardId=B + -10k
└─ Thẻ C (con 3): Quẹt Xe hơi   → ghi: userId + cardId=C + -15k

Balance chung còn: 255.000đ

Phụ huynh xem lịch sử trong app:
  [Tất cả]  [Thẻ A (Con 1)]  [Thẻ B (Con 2)]  [Thẻ C (Con 3)]
  → Filter theo thẻ → biết từng đứa tiêu bao nhiêu
```

### Thay đổi kỹ thuật hỗ trợ tính năng này

**Backend — BalanceTransaction:**
- Thêm field `cardId` (nullable) vào bảng `balance_transactions`
- Khi play game: ghi `cardId` của thẻ đang dùng vào transaction

**Backend — /play endpoint:**
- Response thêm `cardId`

**App Android — Lịch sử giao dịch:**
- Thêm filter theo thẻ (nếu account có nhiều thẻ)
- Hiển thị tên thẻ (vd: "Thẻ A") bên cạnh mỗi giao dịch

**SmardCard — Tạo thẻ phụ:**
- Nhân viên có thể tạo nhiều thẻ cho cùng 1 tài khoản
- Mỗi thẻ có `cardName` khác nhau (vd: "Thẻ con 1", "Thẻ con 2")

---

## Quy Trình Người Chơi (End-to-End)

### Lần đầu đến công viên — KHÔNG có app

```
1. Khách đến quầy
   └─ Nhân viên mở SmardCard app

2. Nhân viên chọn "Tạo thẻ mới"
   ├─ Nhập tên, SĐT khách
   ├─ Cắm thẻ Java Card trắng (hoặc thẻ đã trả lại) vào reader
   ├─ SmardCard gửi APDU:
   │   a) INS_SET_CUSTOMER_ID → ghi customerID (do backend cấp)
   │   b) INS_CREATE_ADMIN_PIN → khởi tạo admin PIN
   │   c) INS_CREATE_PIN → khởi tạo user PIN mặc định
   │   d) INS_WRITE_INFO → ghi tên + SĐT
   └─ SmardCard gọi POST /api/admin/cards/create → backend tạo record user + card

3. Nạp tiền tại quầy
   ├─ Nhân viên nhập số tiền
   ├─ SmardCard gọi POST /api/wallet/topup (method=CASH)
   └─ Backend cộng balance vào tài khoản

4. Khách nhận thẻ → Ra khu vui chơi

5. Tại máy game — Khách quẹt thẻ Java Card vào SmardCard reader tại máy game
   ├─ SmardCard đọc customerID từ thẻ (INS_GET_CUSTOMER_ID)
   ├─ SmardCard gọi POST /api/games/{gameId}/play với { customerID, terminalId }
   ├─ Backend:
   │   ├─ Tìm user theo customerID
   │   ├─ Kiểm tra balance >= pricePerTurn
   │   ├─ Trừ tiền → ghi BalanceTransaction (type=GAME_PLAY)
   │   ├─ Ghi GamePlayLog
   │   └─ Tạo Notification cho user (nếu có app liên kết)
   └─ SmardCard hiển thị: "Xin chào [Tên]! Trừ 20.000đ. Số dư: 80.000đ"

6. Hết tiền
   └─ Quay lại quầy → Nhân viên nạp thêm (step 3)

7. Trả thẻ khi về
   ├─ Nhân viên quét thẻ → SmardCard đọc customerID → gọi API lấy số dư
   ├─ Nếu còn số dư > 0:
   │   ├─ SmardCard hiển thị: "Số dư còn: 35.000đ — Hoàn tiền mặt cho khách"
   │   └─ Gọi POST /api/admin/cards/return → Backend ghi REFUND transaction (trừ hết balance về 0)
   └─ Thẻ được thu về (status = INACTIVE, sẵn sàng ghi đè)
```

---

### Lần đầu đến — ĐÃ có app, yêu cầu thẻ qua app

```
1. Khách tải AppCongVien → Đăng ký tài khoản

2. Trong app → "Thẻ của tôi" → "Yêu cầu tạo thẻ"
   └─ App gọi POST /api/cards/request { note: "Tôi ở cổng A" }

3. Nhân viên thấy yêu cầu trong SmardCard app (tab Yêu cầu thẻ)
   ├─ Bấm "Duyệt + Tạo thẻ"
   ├─ Cắm thẻ Java Card vào reader
   ├─ SmardCard ghi customerID của user lên thẻ
   └─ Gọi PUT /api/admin/card-requests/{id}/resolve → APPROVED

4. App nhận notification: "Yêu cầu tạo thẻ đã được duyệt. Đến quầy nhận thẻ."

5. Khách đến quầy nhận thẻ → Nạp tiền → Ra chơi

6. Quẹt thẻ tại máy game → Notification đến app ngay lập tức:
   ┌──────────────────────────────────┐
   │ 🎮 Chơi game thành công          │
   │ Tàu lượn siêu tốc                │
   │ Trừ: 20.000đ                     │
   │ Số dư còn lại: 80.000đ           │
   └──────────────────────────────────┘
```

---

### Trả thẻ và tái sử dụng (Card Recycle)

```
Khách trả thẻ tại quầy
  ↓
Nhân viên quét thẻ → SmardCard đọc customerID
  ↓
SmardCard gọi GET /api/admin/cards/by-customer/{customerID}
  ├─ Nếu còn số dư > 0 → Hoàn tiền cho khách (CASH) + gọi deduct API
  └─ Gọi POST /api/admin/cards/{cardId}/return → Backend set card.status = INACTIVE
  ↓
Thẻ được thu về
  ↓
Khi có khách mới cần thẻ:
  Nhân viên chọn "Ghi đè thẻ cũ":
  ├─ Cắm thẻ vào reader
  ├─ SmardCard gửi APDU:
  │   a) INS_VERIFY_ADMIN_PIN → Xác minh admin PIN để có quyền ghi
  │   b) INS_RESET_USER_PIN  → Xóa PIN user cũ
  │   c) INS_SET_CUSTOMER_ID → Ghi customerID mới
  │   d) INS_WRITE_INFO      → Ghi thông tin khách mới
  │   e) INS_CREATE_PIN      → Tạo PIN user mới (mặc định)
  └─ Gọi POST /api/admin/cards/reassign { oldCustomerID, newUserId }
     → Backend: unlink thẻ khỏi user cũ, link sang user mới
```

---

### Nạp tiền

```
Qua app:
  AppCongVien → "Nạp tiền" → Chọn MOMO/BANKING → Xác nhận
  → Gọi POST /api/wallet/topup
  → Balance tăng → App hiển thị số dư mới

Tại quầy:
  SmardCard app → Tìm user (theo SĐT hoặc quét thẻ)
  → Nhập số tiền CASH
  → Gọi POST /api/admin/topup { userId, amount, method: CASH }
  → Balance tăng → Notification đến app (nếu có): "Nạp tiền 100.000đ thành công"
```

---

## Bảo Mật Hệ Thống

### A. Bảo mật trên Java Card (đã có sẵn trong ParkCard)

| Dữ liệu | Lưu trữ | Bảo vệ |
|--------|--------|--------|
| Tên, Ngày sinh, SĐT | EEPROM - AES-128-CBC | Master Key |
| Số dư (balance) | EEPROM - AES-128-CBC | Master Key |
| Ảnh khách | EEPROM - AES-128-ECB | Master Key |
| RSA Private Key | EEPROM - AES-128-CBC (256B) | Master Key |
| Master Key (plaintext) | RAM Transient | Tự xóa khi deselect thẻ |
| Wrapped Master Key | EEPROM 16B | User PIN → PBKDF2 → PIN-Key |
| Wrapped Admin Key | EEPROM 16B | Admin PIN → PBKDF2 → PIN-Key |
| customerID | EEPROM plaintext | Không mã hóa (định danh công khai) |

**Luồng xác thực khi unlock thẻ:**
```
Nhập PIN → PBKDF2(PIN, salt, 200 iter) → PIN-Key
         → AES-Decrypt(wrappedMasterKey, PIN-Key) → Master Key (RAM)
         → Dùng Master Key để đọc/ghi dữ liệu mã hóa
         → Master Key xóa khỏi RAM khi rút thẻ
```

**Bảo vệ PIN:**
- User PIN: tối đa 3 lần sai → block (cần Admin PIN để reset)
- Admin PIN: tối đa 5 lần sai → block vĩnh viễn (không recover được)
- PIN truyền từ terminal xuống card: mã hóa bằng Session Key (AES-ECB)

**RSA Private Key bảo vệ hai lớp:**
- Lưu encrypted trong EEPROM (256 bytes, AES-CBC với Master Key)
- Chỉ load vào RAM khi cần ký → xóa ngay sau khi dùng

---

### B. Bảo mật khi quẹt thẻ — RSA Challenge-Response (mới)

Ngăn chặn **thẻ giả/clone** vì RSA private key không thể đọc ra ngoài thẻ:

```
Terminal                    Java Card                   Backend
    |── random challenge ──→|                              |
    |                       |── sign(challenge, privKey) ──|
    |←── signature (128B) ──|                              |
    |── POST /play { customerID, challenge, signature } ──→|
    |                                              verify(sig, pubKey)
    |                                              → trừ tiền nếu hợp lệ
```

**Backend lưu:** `cards.rsaPublicKey` (TEXT) — được upload khi tạo thẻ lần đầu

---

### C. Bảo mật API & Network

- **Terminal token**: SmardCard app đăng nhập với role TERMINAL, nhận JWT riêng
- **HTTPS**: Toàn bộ giao tiếp giữa SmardCard ↔ Backend
- **Rate limiting**: Backend giới hạn số request `/play` per terminal per phút
- **Audit log**: Mọi giao dịch ghi: cardId, terminalId, timestamp, gameId, IP

---

### D. Bảo mật khi mất/trộm thẻ

```
Khách báo mất thẻ
  → Admin bấm "Khóa thẻ" trên appdesktop hoặc khách khóa qua app
  → Backend: card.status = BLOCKED
  → Mọi request /play với cardId này bị reject
  → Thẻ vật lý dù có RSA signature cũng không dùng được
```

---

## Thay Đổi Chi Tiết

### 1. Backend

#### 1.1 Bỏ / Disable
- `routes/VoucherRoutes.kt` → disable (comment out trong Routing.kt)
- `routes/OrderRoutes.kt` → disable
- Bảng `tickets`, `booking_orders`, `booking_order_details`, `vouchers`, `user_vouchers` → giữ nguyên DB nhưng không dùng nữa

#### 1.2 Sửa endpoint play game
**File:** `backend/src/main/kotlin/routes/GameReviewRoutes.kt`

```
POST /api/games/{gameId}/play
Body: { "customerID": "...", "terminalId": "..." }

Flow mới:
  1. Tìm card theo customerID → lấy userId
  2. Kiểm tra card.status == ACTIVE
  3. Lấy game.pricePerTurn
  4. Kiểm tra user.currentBalance >= pricePerTurn
     - Nếu không đủ: return error "Số dư không đủ" (terminal hiển thị)
  5. Trừ tiền: user.currentBalance -= pricePerTurn
  6. Ghi BalanceTransaction (type=GAME_PLAY)
  7. Ghi GamePlayLog (userId, gameId, terminalId, cardId)
  8. Tạo Notification: "Bạn vừa chơi {gameName}, trừ {price}đ. Còn {balance}đ"
  9. Return: { success, gameName, amountCharged, remainingBalance, userName }
```

#### 1.3 Thêm API Admin cho Card Management
**File:** `backend/src/main/kotlin/routes/AdminRoutes.kt`

```
POST /api/admin/cards/create
Body: { "userId": "...", "customerID": "...", "cardName": "..." }
→ Tạo card record với status=ACTIVE

POST /api/admin/cards/return
Body: { "cardId": "..." }
→ Set card.status = INACTIVE, unlink userId (hoặc keep for history)

POST /api/admin/cards/reassign
Body: { "cardId": "...", "newUserId": "..." }
→ Link card sang user mới, status = ACTIVE

POST /api/admin/topup
Body: { "userId": "...", "amount": "...", "method": "CASH" }
→ Nạp tiền cho user (ADMIN only)

GET /api/admin/cards
→ Danh sách tất cả thẻ (filter: status, userId)

GET /api/admin/cards/by-customer/{customerID}
→ Tra cứu thẻ + user theo customerID từ thẻ Java Card
```

#### 1.4 Thêm API yêu cầu tạo thẻ (từ user)
**File:** `backend/src/main/kotlin/routes/CardRoutes.kt`

```
POST /api/cards/request
Body: { "note": "..." }

GET /api/cards/request/status
→ Trạng thái yêu cầu hiện tại của user

GET /api/admin/card-requests
→ Admin xem danh sách yêu cầu PENDING

PUT /api/admin/card-requests/{id}/resolve
Body: { "action": "APPROVED" | "REJECTED", "note": "..." }
```

**Bảng mới:** `card_requests`
```
card_requests
├── requestId (varchar 36) PK
├── userId (varchar 36) FK
├── status (varchar 20): PENDING | APPROVED | REJECTED
├── note (text) nullable
├── resolvedBy (varchar 36) nullable
├── resolvedAt (timestamp) nullable
└── createdAt (timestamp)
```

#### 1.5 Bảo mật RSA Challenge-Response

**Khi tạo thẻ (setup):**
```
SmardCard gọi APDU:
  a) INS_GENERATE_RSA_KEYPAIR (0x1D) → Card sinh RSA-1024 keypair
  b) INS_GET_PUBLIC_KEY (0x1E) → Đọc public key (128 bytes)
  c) SmardCard gửi POST /api/admin/cards/create + publicKey
  d) Backend lưu publicKey vào bảng cards (field mới: rsa_public_key TEXT)
```

**Khi quẹt thẻ chơi game (mỗi lần):**
```
1. SmardCard tạo challenge random (16 bytes)
2. SmardCard gửi APDU INS_SIGN_CHALLENGE (0x1B) + challenge
   → Card ký bằng RSA private key (chỉ thẻ thật mới ký được)
   → Trả về signature (128 bytes)
3. SmardCard gửi POST /api/games/{gameId}/play:
   { customerID, terminalId, challenge, signature }
4. Backend:
   a) Tìm card → lấy publicKey
   b) Verify: RSA.verify(challenge, signature, publicKey)
   c) Nếu signature sai → reject (thẻ clone/giả)
   d) Nếu đúng → trừ tiền bình thường
```

**Thay đổi DB:**
- Thêm `rsaPublicKey TEXT nullable` vào bảng `cards`

**File:** `backend/src/main/kotlin/database/tables/Cards.kt`

**File SmardCard:** `SmartCardManager.kt` thêm:
- `generateRSAKeypair()` → INS 0x1D
- `getPublicKey()` → INS 0x1E
- `signChallenge(challenge)` → INS 0x1B

**Bảo vệ khác đi kèm:**
- Terminal có JWT token riêng (không dùng user token)
- HTTPS trên mọi API
- Mọi giao dịch ghi log: cardId, terminalId, timestamp, gameId

#### 1.6 Thêm GAME_PLAY transaction type
**File:** `backend/src/main/kotlin/database/tables/BalanceTransactions.kt`

Thêm `GAME_PLAY` vào type enum (hiện tại: TOPUP | PAYMENT | REFUND | BONUS | ADJUSTMENT)

---

### 2. SmardCard Terminal (Sửa đổi)

**File chính:** `SmardCard/composeApp/src/jvmMain/kotlin/org/example/project/SmartCardManager.kt`

#### 2.1 Màn hình Tạo Thẻ Mới
- Form: Tên, SĐT khách hàng
- Tìm user trong DB hoặc tạo mới
- Viết thẻ qua APDU (INS_SET_CUSTOMER_ID, INS_WRITE_INFO, INS_CREATE_PIN, INS_CREATE_ADMIN_PIN)
- Gọi `POST /api/admin/cards/create`

#### 2.2 Màn hình Ghi Đè Thẻ Cũ
- Quét thẻ → đọc customerID cũ
- Gọi `/api/admin/cards/return` (deactivate thẻ cũ)
- Nhập thông tin khách mới
- Ghi đè bằng APDU:
  a) `INS_VERIFY_ADMIN_PIN` (0x1F)
  b) `INS_RESET_USER_PIN` (0x21)
  c) `INS_SET_CUSTOMER_ID` (0x17)
  d) `INS_WRITE_INFO` (0x07)
  e) `INS_CREATE_PIN` (0x01)
- Gọi `POST /api/admin/cards/reassign`

#### 2.3 Màn hình Terminal Chơi Game
- Quét thẻ → `INS_GET_CUSTOMER_ID` (0x18)
- Gọi `POST /api/games/{gameId}/play` với customerID
- Hiển thị kết quả: Tên khách, tiền trừ, số dư còn lại

#### 2.4 Tab Yêu Cầu Thẻ
- Hiển thị danh sách card requests PENDING từ `GET /api/admin/card-requests`
- Nút "Duyệt + Tạo thẻ" → mở flow Tạo Thẻ Mới với thông tin user từ request
- Nút "Từ chối"

#### 2.5 Nạp Tiền Tại Quầy
- Tìm user (quét thẻ hoặc nhập SĐT)
- Nhập số tiền
- Gọi `POST /api/admin/topup`

---

### 3. App Android (AppCongVien)

#### 3.1 Bỏ
- `screen/BuyGameScreen.kt`, `screen/CheckoutScreen.kt`
- `screen/VouchersScreen.kt`, `screen/VoucherWalletScreen.kt`
- `screen/MyGamesScreen.kt`
- `viewmodel/CartViewModel.kt`, `viewmodel/OrderViewModel.kt`

#### 3.2 Sửa HomeScreen
**File:** `Appcongvien/app/src/main/java/com/example/appcongvien/screen/HomeScreen.kt`

Quick actions mới: **Nạp tiền | Thẻ của tôi | Lịch sử**
(Bỏ: Mua game, Voucher)

#### 3.3 Thêm màn hình Yêu cầu tạo thẻ
**File mới:** `screen/CardRequestScreen.kt`
- Form gửi yêu cầu (ghi chú)
- Hiển thị trạng thái: PENDING / APPROVED / REJECTED
- Khi APPROVED: hướng dẫn đến quầy

#### 3.4 Nâng cấp Notifications
**File:** `screen/NotificationsScreen.kt`
- Notification type GAME_PLAY: icon game đặc biệt, hiển thị tiền trừ + số dư
- Notification type TOPUP: icon nạp tiền, hiển thị số tiền + số dư mới
- Badge đếm notification chưa đọc trên HomeScreen

#### 3.5 Cải thiện CardInfoScreen
**File:** `screen/CardInfoScreen.kt`
- Hiển thị số dư hiện tại ngay trên card widget
- Thêm nút "Yêu cầu tạo thẻ" khi chưa có thẻ

---

### 4. appdesktop (Admin)

#### 4.1 Nạp tiền tại quầy trong UserManagementScreen
**File:** `appdesktop/composeApp/src/jvmMain/kotlin/com/park/ui/screen/UserManagementScreen.kt`
- Thêm nút "Nạp tiền" trong chi tiết user
- Dialog: nhập số tiền, chọn CASH → gọi `POST /api/admin/topup`

---

## Các File Cần Thay Đổi

### Backend
| File | Thay đổi |
|------|----------|
| `plugins/Routing.kt` | Disable VoucherRoutes, OrderRoutes; thêm admin card routes |
| `routes/GameReviewRoutes.kt` | Sửa `/play`: dùng customerID, trừ balance thay vì check ticket |
| `routes/AdminRoutes.kt` | Thêm card create/return/reassign/topup endpoints |
| `routes/CardRoutes.kt` | Thêm card request endpoints |
| `services/GameReviewService.kt` | Sửa logic play game |
| `services/AdminService.kt` | Thêm card management + topup methods |
| `services/NotificationService.kt` | Thêm game play + topup notification |
| `database/tables/BalanceTransactions.kt` | Thêm type GAME_PLAY |
| **Mới:** `database/tables/CardRequests.kt` | Bảng card_requests |

### SmardCard Terminal
| File | Thay đổi |
|------|----------|
| `SmartCardManager.kt` | Thêm methods: writeNewCard, overwriteCard, getCustomerID |
| `data/network/ApiClient.kt` | Thêm admin card API calls |
| **Mới:** Màn hình tạo thẻ, ghi đè thẻ, duyệt requests |

### App Android
| File | Thay đổi |
|------|----------|
| `screen/HomeScreen.kt` | Sửa quick actions |
| `screen/CardInfoScreen.kt` | Thêm balance, nút request |
| `screen/NotificationsScreen.kt` | Cải thiện GAME_PLAY notification |
| **Mới:** `screen/CardRequestScreen.kt` | Yêu cầu tạo thẻ |
| **Xóa:** BuyGame, Checkout, Vouchers, MyGames screens | Không cần |
| `data/network/ApiService.kt` | Bỏ order/voucher, thêm card request |

### appdesktop Admin
| File | Thay đổi |
|------|----------|
| `ui/screen/UserManagementScreen.kt` | Thêm nút nạp tiền tại quầy |
| `data/network/ApiClient.kt` | Thêm admin topup API |

---

## Verification

1. **Tạo thẻ mới:** SmardCard → Tạo thẻ → Cắm Java Card → Ghi APDU → Backend tạo record → User có thẻ
2. **Quẹt thẻ chơi:** Java Card quẹt reader → SmardCard đọc customerID → Gọi `/play` API → Tiền trừ → App nhận notification
3. **Trả thẻ và tái sử dụng:** SmardCard return API → Ghi đè APDU → Reassign API → Thẻ sẵn sàng cho khách mới
4. **Yêu cầu thẻ qua app:** App gửi request → SmardCard duyệt + ghi thẻ → App nhận notification
5. **Nạp tiền qua app:** TopUp API → Balance tăng → App hiển thị số dư mới
6. **Nạp tiền tại quầy:** SmardCard/appdesktop → Admin topup API → Balance tăng → App nhận notification
