# 🎡 Park Adventure - Giải Thích Chi Tiết Hệ Thống

Tài liệu này giải thích một cách chi tiết và dễ hiểu nhất về dự án **Park Adventure** - Hệ thống quản lý công viên giải trí hiện đại.

---

## 🎯 Tổng Quan: Park Adventure Là Gì?

**Park Adventure** là một **hệ thống quản lý công viên giải trí hiện đại**, với mục tiêu:
- **Số hóa toàn bộ trải nghiệm** vui chơi tại công viên
- **Loại bỏ hoàn toàn vé giấy và tiền mặt** → thay thế bằng **Thẻ thông minh (Smart Card)** và **Ứng dụng di động**

### 💡 Triết lý cốt lõi: "Unified Balance" (Tài chính hợp nhất)

| Khái niệm | Giải thích |
|-----------|------------|
| **"Tiền ở đâu cũng là một"** | Số dư trong Thẻ và trên App là **một tài khoản duy nhất**, không tách biệt |
| **Real-time Banking** | Mọi giao dịch được xử lý **tức thời trên Server trung tâm**. Thẻ **không lưu tiền** mà chỉ là **chìa khóa xác thực (Token)** |
| **Lợi ích** | Mất thẻ ≠ Mất tiền (chỉ cần khóa trên App là an toàn!) |

---

## 🏗️ Kiến Trúc Hệ Thống (4 Thành Phần Chính)

```
┌──────────────────────────────────────────────────────────────────────┐
│                        BACKEND SERVER                                │
│                      (Core Banking - Bộ não)                         │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │
│   │   Database  │  │  Business   │  │     API     │                 │
│   │  (Lưu trữ)  │  │   Logic     │  │  Endpoints  │                 │
│   └─────────────┘  └─────────────┘  └─────────────┘                 │
└───────────────────────────┬──────────────────────────────────────────┘
                            │ API Requests
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  MOBILE APP  │    │   TERMINAL   │    │  SMART CARD  │
│  (Android)   │◄───│   (Máy POS)  │◄───│  (JavaCard)  │
│              │    │              │    │              │
│ • Ví điện tử │    │ • Đầu đọc    │    │ • Chìa khóa  │
│ • Nạp tiền   │    │ • Mở cổng    │    │ • User ID    │
│ • Quản lý thẻ│    │ • Check vé   │    │ • NFC        │
└──────────────┘    └──────────────┘    └──────────────┘
```

### 1️⃣ Mobile Application (Android App)

**Vai trò**: Ví điện tử + Cổng thông tin của người dùng

**Chức năng chính**:
- Đăng ký/Đăng nhập tài khoản
- Nạp tiền vào ví (qua MoMo, Banking)
- Kích hoạt thẻ mới, Khóa thẻ mất
- Mua vé/Combo vé trước
- Xem lịch sử giao dịch

**Công nghệ sử dụng**: Kotlin + Jetpack Compose (Android Native)

---

### 2️⃣ Smart Card (JavaCard)

**Vai trò**: "Chìa khóa vạn năng" để định danh người chơi tại các máy game

**Đặc điểm kỹ thuật**:
- **Lưu trữ an toàn**: User ID + Secret Keys (khóa bảo mật)
- **Giao tiếp NFC**: "Tap & Play" (Chạm và chơi)
- **Low Cost**: Thẻ trắng có thể mua sẵn và tự kích hoạt qua App (Self-Service)

**Lưu ý quan trọng**: 
> ⚠️ Thẻ KHÔNG lưu tiền trực tiếp! Thẻ chỉ là "chìa khóa" để Server nhận diện bạn là ai. Nếu mất thẻ, chỉ cần khóa trên App là tiền vẫn an toàn.

---

### 3️⃣ Terminal (Thiết bị đầu cuối/Máy POS)

**Vai trò**: Cổng kiểm soát đặt tại từng trò chơi

**Quy trình hoạt động**:
1. Nhận tín hiệu từ thẻ (hoặc QR Code) khi khách chạm thẻ
2. Đọc User ID từ thẻ → Gửi request lên Server
3. Nhận kết quả từ Server (Đủ tiền/Không đủ)
4. Thực thi lệnh: Mở cổng / Kích hoạt máy game

---

### 4️⃣ Backend Server (Core Banking)

**Vai trò**: Bộ não trung tâm - xử lý mọi logic nghiệp vụ

**Trách nhiệm**:
- Quản lý User, Tài khoản, Ví tiền
- Xử lý logic nghiệp vụ (trừ tiền, cộng điểm, check vé)
- Lưu trữ tập trung (Database)
- Cung cấp API cho App và Terminal

---

## 🔄 Luồng Nghiệp Vụ Thực Tế

### Quy Trình 1: Onboarding (Người dùng mới tham gia)

Thay vì xếp hàng mua vé, người dùng có thể **tự phục vụ**:

```
┌─────────────────────────────────────────────────────────────────┐
│ Bước 1: Mua thẻ trắng                                           │
│         (tại máy bán tự động hoặc căng tin)                     │
│         → Thẻ chưa có dữ liệu                                   │
└─────────────────────────────┬───────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ Bước 2: Mở App → Chọn "Thêm thẻ"                                │
└─────────────────────────────┬───────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ Bước 3: Chạm thẻ vào lưng điện thoại (NFC)                      │
└─────────────────────────────┬───────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ Bước 4: App ghi danh tính User vào thẻ                          │
│         → Thẻ chính thức thuộc về User ✅                       │
└─────────────────────────────────────────────────────────────────┘
```

---

### Quy Trình 2: Play (Vui chơi tại công viên)

**Ví dụ cụ thể**: User nạp 500K và chơi trò "Tàu lượn siêu tốc" (giá 50K)

```
┌─────────────────────────────────────────────────────────────────┐
│ Bước 1: USER NẠP TIỀN                                           │
│                                                                 │
│   [User] ──nạp 500K qua MoMo──► [Server]                        │
│                                                                 │
│   Server ghi nhận: Balance của User = 500,000đ                  │
└─────────────────────────────┬───────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ Bước 2: CHẠM THẺ VÀO MÁY GAME                                   │
│                                                                 │
│   [User] ──chạm thẻ──► [Terminal "Tàu lượn"]                    │
│                                                                 │
│   Terminal đọc được: User ID = "U12345"                         │
└─────────────────────────────┬───────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ Bước 3: TERMINAL GỌI LÊN SERVER                                 │
│                                                                 │
│   [Terminal] ──gửi request──► [Server]                          │
│                                                                 │
│   Request: "User U12345 muốn chơi Game G001 (Tàu lượn)"         │
└─────────────────────────────┬───────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ Bước 4: SERVER XỬ LÝ                                            │
│                                                                 │
│   Server check:                                                 │
│   ├─ Balance của U12345 = 500,000đ                              │
│   ├─ Giá vé Tàu lượn = 50,000đ                                  │
│   └─ 500,000 > 50,000 → ĐỦ TIỀN! ✅                             │
│                                                                 │
│   Server thực hiện:                                             │
│   ├─ Trừ 50,000đ → Balance mới = 450,000đ                       │
│   ├─ Ghi log giao dịch                                          │
│   └─ Gửi lệnh "ALLOW" về Terminal                               │
└─────────────────────────────┬───────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ Bước 5: KẾT QUẢ                                                 │
│                                                                 │
│   [Terminal] nhận lệnh "ALLOW" → MỞ CỔNG 🎢                     │
│                                                                 │
│   [App] hiện thông báo:                                         │
│   "Đã thanh toán 50,000đ cho Tàu lượn siêu tốc"                 │
│   "Số dư hiện tại: 450,000đ"                                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Cấu Trúc Database (Các Bảng Chính)

Hệ thống cơ sở dữ liệu được thiết kế chuẩn hóa để quản lý chặt chẽ tiền tệ và hoạt động:

### Nhóm Identity (Định danh)

| Bảng | Mô tả | Trường quan trọng |
|------|-------|-------------------|
| `Accounts` | Tài khoản đăng nhập | phone, password_hash, status |
| `Users` | Thông tin hồ sơ người dùng | name, current_balance, membership_tier |
| `Cards` | Quản lý thẻ vật lý | card_uid, status, user_id |

### Nhóm Commercial (Thương mại)

| Bảng | Mô tả | Trường quan trọng |
|------|-------|-------------------|
| `Games` | Danh mục trò chơi | name, price_per_play, location |
| `Bookings/Orders` | Đơn hàng mua vé/combo | user_id, total_amount, status |
| `Tickets` | Vé điện tử | game_id, is_used, expiry_date |
| `Vouchers` | Mã giảm giá | code, discount_percent, max_uses |

### Nhóm Financial (Tài chính - Quan trọng)

| Bảng | Mô tả | Trường quan trọng |
|------|-------|-------------------|
| `BalanceTransactions` | Lịch sử biến động số dư | type (CREDIT/DEBIT), amount, balance_after |
| `PaymentRecords` | Lịch sử nạp tiền từ cổng thanh toán | gateway, transaction_id, status |

### Nhóm Ops (Vận hành)

| Bảng | Mô tả | Trường quan trọng |
|------|-------|-------------------|
| `Terminals` | Quản lý thiết bị phần cứng | terminal_code, game_id, status |
| `GamePlayLogs` | Log chi tiết lượt chơi | user_id, game_id, terminal_id, timestamp |

---

## 🛠️ Tính Năng Quản Trị (Admin Panel)

Hệ thống dành cho ban quản lý giúp vận hành trơn tru:

### 1. User Management
- Tra cứu thông tin khách hàng
- Xử lý khiếu nại
- Khóa thẻ mất cho khách

### 2. Game Management
- Bật/Tắt chế độ bảo trì máy game
- Điều chỉnh giá vé linh hoạt theo thời gian:
  - Giờ vàng (giảm giá)
  - Ngày lễ (tăng giá)
  - Ngày thường

### 3. Finance Dashboard
- Báo cáo doanh thu thời gian thực
- Đối soát dòng tiền nạp từ các cổng thanh toán
- Thống kê giao dịch theo ngày/tuần/tháng

### 4. Promotion
- Tạo và quản lý mã Voucher
- Chạy chiến dịch khuyến mãi để kích cầu
- Theo dõi hiệu quả khuyến mãi

---

## 🚀 Điểm Mạnh Công Nghệ

| Điểm mạnh | Giải thích chi tiết |
|-----------|---------------------|
| **NFC Connectivity** | Tận dụng NFC trên điện thoại để biến Smartphone thành "trạm cài đặt thẻ cá nhân". Người dùng tự kích hoạt thẻ mà không cần nhân viên hỗ trợ. |
| **Security (Bảo mật)** | Thẻ JavaCard không lưu tiền trực tiếp → Mất thẻ không mất tiền. Chỉ cần mở App, khóa thẻ cũ và kích hoạt thẻ mới. |
| **Server-side Authentication** | Mọi giao dịch đều được xác thực và xử lý trên Server. Terminal không có quyền tự quyết định mở cổng nếu không có lệnh từ Server. |
| **Scalability (Mở rộng)** | Dễ dàng thêm trò chơi mới, thêm kios bán hàng mà không cần thay đổi kiến trúc thẻ hay App. Chỉ cần thêm dữ liệu trên Database và đặt thêm Terminal. |

---

## 📊 Sơ đồ Tổng Quan Hoạt Động

```
                                    ┌────────────────┐
                                    │   ADMIN WEB    │
                                    │   Dashboard    │
                                    └───────┬────────┘
                                            │
                                            ▼
┌──────────────┐    ┌──────────────┐    ┌────────────────────────┐
│   Cổng TT    │───►│              │◄───│      MOBILE APP        │
│  MoMo/Bank   │    │   BACKEND    │    │  (Android + NFC)       │
└──────────────┘    │   SERVER     │    └───────────┬────────────┘
                    │              │                │
                    │  (Database)  │                │ NFC Write
                    │              │                ▼
┌──────────────┐    │              │    ┌────────────────────────┐
│  TERMINAL    │───►│              │    │     SMART CARD         │
│  (Máy POS)   │◄───│              │    │   (JavaCard - Token)   │
└──────┬───────┘    └──────────────┘    └────────────────────────┘
       │                                            │
       │ NFC Read                                   │
       └────────────────────────────────────────────┘
```

---

## 🔐 Bảo Mật: Tại Sao Mất Thẻ Không Mất Tiền?

1. **Thẻ chỉ chứa User ID** - không có thông tin số dư hay PIN
2. **Mọi giao dịch phải qua Server** - Terminal không thể tự trừ tiền
3. **Khóa thẻ từ xa** - Khi mất thẻ, chỉ cần vào App → Khóa thẻ
4. **Thẻ bị khóa = vô hiệu hóa** - Server từ chối mọi request từ thẻ đó

---

*Tài liệu này được tạo để giải thích chi tiết về hệ thống Park Adventure, phục vụ việc chuyển giao và đào tạo.*
