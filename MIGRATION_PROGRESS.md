# MIGRATION PROGRESS — Chuyển sang bài toán Quản lý Vé Công Viên Smart Card

> File này theo dõi tiến độ chuyển đổi toàn hệ thống sang bài toán mới.
> Cập nhật mỗi khi hoàn thành một phần để session sau có thể tiếp tục từ đó.

---

## Tổng quan bài toán mới

**Thay đổi cốt lõi so với hệ thống cũ:**
- Thẻ Smart Card chỉ là **định danh vật lý** (card_id + xác thực RSA)
- **Balance lưu trên server** (`users.current_balance`), không lưu trên thẻ
- **Cơ chế tiền cọc**: thu khi nhận thẻ, hoàn khi trả thẻ (mất thẻ = mất cọc)
- **Khóa thẻ mất**: server-side only (`cards.status = BLOCKED`)
- **Trả thẻ nhưng giữ tài khoản**: card unlink khỏi user, account giữ lại
- **Không có pre-booking, tickets, vouchers** — chơi = quẹt thẻ = trừ tiền

---

## TRẠNG THÁI TỔNG QUAN

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| JavaCard (ParkCard) | ✅ XONG | Xem chi tiết bên dưới |
| Database Schema (MySQL) | ✅ XONG | `database_new.sql` đã tạo |
| Backend (Kotlin/Ktor) | ✅ XONG | Tất cả services/routes đã refactor |
| Desktop App (appdesktop) | ✅ XONG | Card lifecycle UI + bỏ Voucher/Orders |
| Mobile App (Appcongvien) | ⏳ CHƯA LÀM | Cần bỏ booking/voucher, thêm card request |

---

## ✅ 1. JavaCard — HOÀN THÀNH

**Files đã sửa:**

| File | Thay đổi |
|---|---|
| `ParkCard/src/ParkCard/CardModel.java` | Xóa balance/photo/DOB/gameList; giữ name+phone; thêm `clearCardData()` |
| `ParkCard/src/ParkCard/CryptoManager.java` | Xóa `encryptPhotoECB()` + `decryptPhotoECB()` |
| `ParkCard/src/ParkCard/CustomerCardApplet.java` | Xóa ~20 APDU commands; thêm `INS_CLEAR_CARD_DATA (0x30)` |
| `ParkCard/src/ParkCard/PinManager.java` | Cập nhật lời gọi `ensureMasterKeyWithAdmin()` |
| ~~`ParkCard/src/ParkCard/GameManager.java`~~ | **Đã xóa** |

**APDU commands còn lại trên thẻ:**

| Mã | Lệnh | Mô tả |
|---|---|---|
| 0x07 | WRITE_INFO | Ghi name (64B) + phone (16B), cần admin auth |
| 0x0B | READ_INFO | Đọc customerID + name + phone, cần admin auth |
| 0x10 | GET_CRYPTO_INFO | Lấy IV + Salt |
| 0x17 | SET_CUSTOMER_ID | Ghi customer ID |
| 0x18 | GET_CUSTOMER_ID | Đọc customer ID (không cần PIN) |
| 0x1B | SIGN_CHALLENGE | Ký challenge RSA, cần master key sẵn sàng |
| 0x1C | GET_RSA_STATUS | Kiểm tra RSA keypair đã generate chưa |
| 0x1D | GENERATE_RSA_KEYPAIR | Generate RSA-1024, cần admin auth |
| 0x1E | GET_PUBLIC_KEY | Lấy public key (modulus/exponent) |
| 0x1F | VERIFY_ADMIN_PIN | Verify admin PIN plaintext |
| 0x20 | CREATE_ADMIN_PIN | Tạo admin PIN (chỉ 1 lần) |
| 0x22 | GET_ADMIN_PIN_TRIES | Lấy số lần thử còn lại |
| 0x23 | SET_SESSION_KEY | Set session key cho encrypted PIN |
| 0x24 | VERIFY_ADMIN_PIN_ENCRYPTED | Verify admin PIN đã mã hóa bằng session key |
| 0x26 | GET_SESSION_KEY_STATUS | Kiểm tra session key đã set chưa |
| **0x30** | **CLEAR_CARD_DATA** | **Mới: xóa customerID+name+phone, dùng khi trả thẻ** |

---

## ✅ 2. Database Schema — HOÀN THÀNH

**Files:**
- `Appcongvien/database_new.sql` — SQL hoàn chỉnh cho MySQL
- `Appcongvien/DATABASE.md` — Mô tả schema mới

**Các bảng trong schema mới:**

| Bảng | Ghi chú |
|---|---|
| `accounts` | Đăng nhập, role: USER/STAFF/ADMIN |
| `users` | Hồ sơ + `current_balance` (số dư ví) |
| `admins` | Nhân viên + quản trị |
| `cards` | Thẻ vật lý NFC; có `deposit_amount`, `deposit_status` |
| `card_requests` | **Mới**: app user yêu cầu cấp thẻ trước khi đến quầy |
| `games` | Danh sách trò chơi |
| `terminals` | Thiết bị đọc thẻ |
| `game_play_logs` | Lịch sử chơi; có `amount_charged` |
| `game_reviews` | Đánh giá trò chơi |
| `balance_transactions` | Lịch sử ví; type gồm cả DEPOSIT_PAID/REFUND/FORFEITED |
| `payment_records` | Thanh toán qua cổng payment |
| `notifications` | Push notification |
| `support_messages` | Chat hỗ trợ |
| `announcements` | Thông báo hệ thống |
| `rsa_public_keys` | Public key của từng thẻ (PK = card_id) |

**Đã xóa so với schema cũ:** `tickets`, `booking_orders`, `booking_order_details`, `vouchers`, `user_vouchers`

---

## ✅ 3. Backend (Kotlin/Ktor) — HOÀN THÀNH

**Stack:** Kotlin + Ktor + Exposed ORM + MySQL

**Việc cần làm:**

### 3.1 Database / Entities
- [ ] Chạy `database_new.sql` để tạo lại schema (rename DB cũ hoặc drop tables)
- [ ] Xóa Exposed table objects: `Tickets`, `BookingOrders`, `BookingOrderDetails`, `Vouchers`, `UserVouchers`
- [ ] Sửa `Users` table object: xóa `isCardLocked`, `membershipLevel`, `loyaltyPoints`, `referralCode`, `referredBy`
- [ ] Sửa `Cards` table object: xóa `virtualCardUid`, `cardType`, `pinHash`; thêm `depositAmount`, `depositStatus`
- [ ] Thêm `CardRequests` table object
- [ ] Sửa `GamePlayLogs`: xóa `ticketId`; thêm `amountCharged`
- [ ] Sửa `RsaPublicKeys`: đổi PK từ `customerId` → `cardId` (FK → cards)
- [ ] Sửa `BalanceTransactions` type enum: thêm `DEPOSIT_PAID`, `DEPOSIT_REFUND`, `DEPOSIT_FORFEITED`

### 3.2 Repositories
- [ ] Xóa: `TicketRepository`, `BookingOrderRepository`, `VoucherRepository`
- [ ] Sửa: `CardRepository` → thêm `deposit_amount/status`, xóa virtual/pin_hash
- [ ] Thêm: `CardRequestRepository`
- [ ] Sửa: `UserRepository` → xóa các field bị bỏ
- [ ] Sửa: `GamePlayLogRepository` → cập nhật fields
- [ ] Sửa: `RsaKeyRepository` → đổi PK sang card_id

### 3.3 Services
- [ ] Xóa: `TicketService`, `BookingService`, `VoucherService`
- [ ] Thêm: `CardLifecycleService` — phát hành thẻ, trả thẻ, khóa thẻ
  - `issueCard(userId, cardUid, depositAmount)` — phát hành, thu cọc
  - `returnCard(cardId)` — trả thẻ, hoàn cọc + balance
  - `blockCard(cardId, reason)` — khóa thẻ mất
  - `linkCard(cardId, userId)` — liên kết thẻ với tài khoản
  - `unlinkCard(cardId)` — tách thẻ khỏi tài khoản
- [ ] Thêm: `CardRequestService` — xử lý yêu cầu cấp thẻ từ app
- [ ] Sửa: `BalanceService` — thêm các loại giao dịch deposit
- [ ] Sửa: `GamePlayService` — bỏ ticket logic, chỉ check balance và trừ tiền

### 3.4 Routes / API
- [ ] Xóa routes: `/tickets`, `/booking`, `/vouchers`
- [ ] Thêm routes:
  - `POST /cards/issue` — nhân viên phát hành thẻ
  - `POST /cards/{id}/return` — nhân viên xử lý trả thẻ
  - `POST /cards/{id}/block` — khóa thẻ
  - `GET /cards/{id}` — lấy thông tin thẻ
  - `POST /card-requests` — app user gửi yêu cầu cấp thẻ
  - `GET /card-requests` — nhân viên xem danh sách yêu cầu
  - `PUT /card-requests/{id}/approve` — nhân viên duyệt yêu cầu
- [ ] Sửa routes: `/games/play` — không cần ticket_id nữa

### 3.5 WebSocket
- [ ] Xem lại WebSocket handler cho realtime notification khi quẹt thẻ

---

## ✅ 4. Desktop App (appdesktop) — HOÀN THÀNH

**Stack:** Kotlin Multiplatform Compose Desktop

**Việc cần làm:**

### 4.1 Xóa màn hình cũ
- [x] Xóa VoucherManagementScreen, VoucherManagementViewModel, VoucherRepository
- [x] Xóa OrderRepository

### 4.2 Thêm / sửa màn hình nhân viên
- [x] **CardManagementScreen** — tab Thẻ AVAILABLE + tab Yêu cầu cấp thẻ
  - Đăng ký thẻ mới (RegisterCardDialog)
  - Phát hành thẻ cho khách (IssueCardDialog) — API POST /api/cards/issue
  - Trả thẻ (ConfirmDialog) — API POST /api/cards/{id}/return
  - Khóa thẻ (BlockCardDialog) — API POST /api/cards/{id}/block
  - Duyệt/từ chối yêu cầu (ReviewRequestDialog) — API POST /api/card-requests/{id}/review
- [x] Navigation: VOUCHERS → CARDS (AdminScreen, App.kt, SideNav)
- [x] DashboardScreen: stats cards cập nhật (activeCards/availableCards/totalTopUpRevenue)
- [x] FinanceScreen: bỏ tab Orders, chỉ còn Giao dịch
- [x] UserManagementScreen: bỏ MembershipBadge, loyaltyPoints

### 4.3 Lưu ý còn lại
- APDU `CLEAR_CARD_DATA` (0x30) chưa được tích hợp vào desktop (desktop app dùng API thuần, không có NFC reader)
- Nếu có NFC reader gắn vào máy tính nhân viên → cần thêm module đọc thẻ riêng

---

## ⏳ 5. Mobile App (Appcongvien - Android) — CHƯA LÀM

**Stack:** Android + Kotlin + Jetpack Compose

**Việc cần làm:**

### 5.1 Xóa màn hình cũ
- [ ] Xóa màn hình Booking / Order
- [ ] Xóa màn hình Vouchers / User Vouchers
- [ ] Xóa màn hình Tickets

### 5.2 Thêm / sửa màn hình
- [ ] **Yêu cầu cấp thẻ**: Form điền thông tin → chọn thanh toán cọc online hoặc tại quầy → gửi request
  - API: `POST /card-requests`
- [ ] **Xem trạng thái thẻ**: Hiển thị thẻ đang liên kết, trạng thái (ACTIVE/BLOCKED)
- [ ] **Lịch sử giao dịch**: Cập nhật hiển thị type mới (DEPOSIT_PAID, DEPOSIT_REFUND, DEPOSIT_FORFEITED)
- [ ] **Thông báo realtime**: Đảm bảo nhận notification khi quẹt thẻ chơi (WebSocket)

---

## Thứ tự ưu tiên tiếp tục

```
1. Backend — cơ sở hạ tầng, cần làm trước
   1a. Migrate DB (chạy database_new.sql)
   1b. Sửa Entities + Repositories
   1c. Sửa/thêm Services (CardLifecycleService quan trọng nhất)
   1d. Sửa/thêm Routes

2. Desktop App — nhân viên cần để thao tác thẻ
   2a. Cập nhật APDU calls
   2b. Màn hình phát hành + trả thẻ + khóa thẻ

3. Mobile App — người dùng, có thể làm sau cùng
   3a. Xóa booking/voucher
   3b. Thêm card request flow
```

---

## Ghi chú kỹ thuật quan trọng

- **RSA challenge-response**: server tạo challenge (32 bytes random), gửi xuống terminal, terminal gửi xuống thẻ ký, thẻ trả signature, terminal gửi lên server verify. Challenge giữ trong memory service, không lưu DB.
- **Balance transaction atomicity**: mỗi lần trừ tiền chơi cần atomic: update `users.current_balance` + insert `balance_transactions` trong 1 DB transaction.
- **Card return flow**: 1) CLEAR_CARD_DATA trên thẻ vật lý; 2) server: hoàn balance → hoàn deposit → unlink card (user_id = NULL) → status = AVAILABLE.
- **Deposit khi mất thẻ**: server set `deposit_status = FORFEITED`, `status = BLOCKED`, ghi `balance_transactions` type `DEPOSIT_FORFEITED`.
- `application.yaml` backend đang dùng DB `park_card_system_v2` — cần update tên DB hoặc drop/recreate.
