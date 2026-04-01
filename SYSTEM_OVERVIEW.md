# Hệ thống Quản lý Vé Công Viên — Smart Card

---

## 1. Tổng quan

Hệ thống quản lý hoạt động vui chơi tại công viên giải trí thông qua **Smart Card (thẻ NFC vật lý)**, kết hợp với:

- **Ứng dụng mobile** — dành cho người dùng (Android)
- **Ứng dụng desktop** — dành cho nhân viên và quản trị viên
- **Backend API** — Kotlin + Ktor + MySQL
- **JavaCard applet** — chạy trên thẻ NFC vật lý

---

## 2. Kiến trúc dữ liệu cốt lõi

| Nguyên tắc | Mô tả |
|---|---|
| **Balance lưu trên server** | `users.current_balance` — không lưu trên thẻ |
| **Thẻ chỉ là định danh** | Thẻ NFC lưu `customer_id` + RSA keypair để xác thực |
| **1 tài khoản — nhiều thẻ** | Phục vụ gia đình, nhóm người dùng |
| **Tài khoản giữ lại khi trả thẻ** | Thẻ bị unlink, account vẫn tồn tại |

---

## 3. Cơ chế tiền cọc

- **Khi nhận thẻ**: khách đóng tiền cọc
- **Khi trả thẻ**: hoàn tiền cọc + hoàn số dư còn lại trong ví
- **Khi mất thẻ**: không hoàn cọc, hệ thống khóa thẻ (`cards.status = BLOCKED`)
- **Người dùng có app**: có thể chọn thanh toán cọc online hoặc tại quầy khi nhận thẻ

---

## 4. Luồng khách hàng

### Case A — Không dùng app

```
Khách đến quầy
  → Nhân viên tạo tài khoản tạm + phát hành thẻ
  → Khách đóng tiền cọc + nạp tiền tại quầy
  → Sử dụng thẻ chơi trò chơi
  → Trả thẻ → nhận lại cọc + số dư còn lại
```

### Case B — Có dùng app

```
Đăng ký tài khoản trên app
  → Gửi yêu cầu cấp thẻ (có thể trả cọc online)
  → Đến quầy nhận thẻ
  → Nạp tiền qua app hoặc tại quầy
  → Sử dụng thẻ chơi trò chơi
  → Có thể giữ thẻ cho lần sau HOẶC trả thẻ lấy lại tiền
```

---

## 5. Luồng quẹt thẻ tại trò chơi

```
Người dùng quẹt thẻ tại terminal
  → Terminal đọc card_id (physical_card_uid)
  → Terminal gửi request lên server kèm challenge
  → Thẻ ký challenge bằng RSA private key
  → Server xác minh chữ ký RSA (dùng public key đã lưu)
  → Server kiểm tra cards.status != BLOCKED
  → Server kiểm tra users.current_balance >= price_per_turn
      ├── Đủ tiền → cho vào + trừ tiền + ghi game_play_log
      └── Không đủ → từ chối
  → Gửi push notification realtime về app
```

---

## 6. Tính năng ứng dụng mobile

- Xem số dư ví
- Nạp tiền online (qua cổng thanh toán)
- Gửi yêu cầu cấp thẻ
- Nhận thông báo realtime khi quẹt thẻ
- Xem danh sách trò chơi
- Xem lịch sử giao dịch
- Xem lịch sử chơi

---

## 7. Tính năng ứng dụng desktop (nhân viên)

- **Phát hành thẻ mới**: init thẻ (generate RSA, set PIN, ghi customer info)
- **Ghi đè thẻ cũ**: tái sử dụng thẻ đã trả
- **Thu tiền cọc**
- **Nạp tiền cho khách** tại quầy
- **Xử lý trả thẻ**: hoàn cọc + hoàn số dư + hủy liên kết thẻ
- **Khóa thẻ mất**: gọi API block, không cần thẻ vật lý
- **Duyệt yêu cầu cấp thẻ** từ app

---

## 8. Chức năng Admin

- Quản lý người dùng (khóa/mở tài khoản)
- Xem thống kê doanh thu
- Quản lý trò chơi (thêm/sửa/xóa)
- Gửi thông báo hệ thống
- Duyệt yêu cầu cấp thẻ

---

## 9. Cơ chế "Trả thẻ nhưng giữ tài khoản"

```
Khách trả thẻ
  ├── Thẻ: CLEAR_CARD_DATA (APDU 0x30) → xóa customer_id + name + phone
  ├── Server: cards.user_id = NULL, cards.status = AVAILABLE
  ├── Server: hoàn deposit + balance → ghi balance_transactions
  └── Tài khoản: vẫn giữ nguyên trên hệ thống

Lần sau quay lại: nhận thẻ mới mà không cần đăng ký lại
```

---

## 10. Stack công nghệ

| Thành phần | Công nghệ |
|---|---|
| Backend | Kotlin + Ktor + Exposed ORM + MySQL |
| Mobile | Android + Kotlin + Jetpack Compose |
| Desktop | Kotlin Multiplatform + Compose Desktop |
| Smart Card | JavaCard (RSA-1024, AES-128, PBKDF2) |
| Realtime | WebSocket (Ktor) |
| Auth | JWT |

---

## 11. Dữ liệu lưu trên thẻ vật lý

| Dữ liệu | Kích thước | Mã hóa |
|---|---|---|
| Customer ID | 15 bytes | Plain text |
| Tên khách | 64 bytes | AES-128 CBC |
| Số điện thoại | 16 bytes | AES-128 CBC |
| RSA private key | 256 bytes | AES-128 CBC |
| RSA public key | ~131 bytes | Plain text |
| IV + Salt | 32 bytes | Plain text |
| Wrapped master key (admin) | 16 bytes | Plain text |
| Session key | 16 bytes | Transient RAM |

> **Không lưu trên thẻ**: balance, ảnh, lịch sử chơi, game tickets

---

## 12. File tham khảo trong project

| File | Mô tả |
|---|---|
| `MIGRATION_PROGRESS.md` | Tiến độ thực hiện, checklist từng bước |
| `Appcongvien/database_new.sql` | SQL tạo database MySQL |
| `Appcongvien/DATABASE.md` | Mô tả schema chi tiết |
| `ParkCard/src/ParkCard/` | JavaCard source code |
| `backend/src/main/resources/application.yaml` | Config backend (DB, port, JWT) |
