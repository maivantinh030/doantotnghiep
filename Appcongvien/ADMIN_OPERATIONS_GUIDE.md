# 👮 Hướng Dẫn Vận Hành Admin - Park Adventure

Tài liệu này mô tả chi tiết **chức năng** và **các bước thao tác** của Admin trong hệ thống.

---

## 📋 3 Vai Trò Admin

| Vai trò | Mô tả | Ai là? |
|---------|-------|--------|
| **SUPER_ADMIN** | Toàn quyền hệ thống | Chủ công viên, Giám đốc |
| **ADMIN** | Quản lý nghiệp vụ | Quản lý ca, Trưởng nhóm |
| **STAFF** | Thao tác cơ bản | Nhân viên quầy, CSKH |

```
SUPER_ADMIN (Toàn quyền)
    │
    ├── ADMIN (Quản lý)
    │      └── Duyệt tiền, Quản lý game, Tạo voucher, Báo cáo
    │
    └── STAFF (Nhân viên)
           └── Tra cứu, Hỗ trợ, Khóa thẻ
```

---

# 🔧 CHỨC NĂNG THEO VAI TRÒ

---

## 👤 STAFF (Nhân Viên)

Nhân viên có thể thực hiện các thao tác **hỗ trợ khách hàng cơ bản**.

### 1. Tra Cứu Khách Hàng

```
Bước 1: Đăng nhập Admin Portal

Bước 2: Vào "Khách Hàng" → "Tra Cứu"

Bước 3: Tìm theo SĐT / Tên / Mã thẻ

Bước 4: Xem thông tin:
   ├── Thông tin cá nhân (tên, SĐT, hạng thành viên)
   ├── Số dư ví hiện tại
   ├── Danh sách thẻ đang liên kết
   ├── Lịch sử giao dịch gần đây
   └── Lịch sử chơi game gần đây
```

### 2. Khóa Thẻ Cho Khách (Báo Mất)

```
Bước 1: Tra cứu khách hàng

Bước 2: Chọn tab "Thẻ" → Chọn thẻ cần khóa

Bước 3: Click "Khóa Thẻ"
   └── Chọn lý do: Khách báo mất / Nghi ngờ gian lận

Bước 4: Xác nhận
   └── Hệ thống tự động:
       ├── Cập nhật trạng thái thẻ = BLOCKED/LOST
       ├── Thẻ bị từ chối tại mọi máy quẹt
       └── Gửi thông báo cho khách
```

### 3. Xử Lý Ticket Hỗ Trợ

```
Bước 1: Vào "Hỗ Trợ" → "Ticket Đang Mở"

Bước 2: Chọn ticket → "Nhận Xử Lý"

Bước 3: Đọc nội dung, trả lời khách
   └── Gửi tin nhắn / Đính kèm file

Bước 4: Khi xong → "Đóng Ticket"
   └── Chọn kết quả + Ghi chú
```

### 4. Xem Giám Sát Lượt Chơi

```
Bước 1: Vào "Vận Hành" → "Giám Sát"

Bước 2: Xem danh sách lượt chơi real-time
   └── Ai chơi, game gì, máy nào, bao nhiêu tiền
```

---

## 👔 ADMIN (Quản Lý)

Admin có tất cả quyền của STAFF, cộng thêm các quyền quản lý.

### 1. Xem Báo Cáo Doanh Thu

```
Bước 1: Vào "Báo Cáo" → "Doanh Thu"

Bước 2: Chọn khoảng thời gian
   └── Hôm nay / 7 ngày / Tháng / Tùy chọn

Bước 3: Xem:
   ├── Tổng tiền nạp
   ├── Tổng chi tiêu
   ├── Doanh thu theo game
   ├── Biểu đồ theo ngày
   └── Xuất Excel/PDF
```

### 2. Duyệt Yêu Cầu Hoàn Tiền

```
Bước 1: Vào "Tài Chính" → "Hoàn Tiền"

Bước 2: Xem danh sách yêu cầu đang chờ

Bước 3: Click vào yêu cầu → Xem chi tiết
   ├── Thông tin user
   ├── Lý do hoàn tiền
   └── Chứng từ đính kèm

Bước 4: Quyết định
   ├── [DUYỆT] → Nhập ghi chú → Xác nhận
   │   └── Tiền tự động cộng vào ví user
   │
   └── [TỪ CHỐI] → Nhập lý do → Xác nhận
       └── Gửi thông báo cho user
```

**Giới hạn**: ADMIN chỉ duyệt được ≤ 2 triệu. Trên 2 triệu cần SUPER_ADMIN.

### 3. Điều Chỉnh Số Dư Thủ Công

```
Bước 1: Vào "Tài Chính" → "Điều Chỉnh Số Dư"

Bước 2: Tìm user cần điều chỉnh

Bước 3: Nhập:
   ├── Loại: Cộng / Trừ
   ├── Số tiền
   ├── Lý do (bắt buộc, ≥20 ký tự)
   └── File chứng từ (nếu có)

Bước 4: Nhập mật khẩu Admin → Xác nhận
   └── Hệ thống ghi log đầy đủ để audit
```

### 4. Quản Lý Games

```
┌─ Thêm Game Mới ─────────────────────────────────┐
│ Bước 1: "Vận Hành" → "Games" → "Thêm Mới"       │
│ Bước 2: Nhập: Tên, Mô tả, Giá, Vị trí, Ảnh...   │
│ Bước 3: Lưu                                     │
└─────────────────────────────────────────────────┘

┌─ Bật/Tắt Bảo Trì ───────────────────────────────┐
│ Bước 1: Chọn game → "Bảo Trì"                   │
│ Bước 2: Nhập lý do + Thời gian dự kiến          │
│ Bước 3: Xác nhận → Game ngừng cho chơi          │
│                                                 │
│ Khi xong → "Mở Lại" → Game hoạt động bình thường│
└─────────────────────────────────────────────────┘

┌─ Điều Chỉnh Giá ────────────────────────────────┐
│ Bước 1: Chọn game → Tab "Bảng Giá"              │
│ Bước 2: Thêm giá mới:                           │
│    - Tên: "Giờ vàng"                            │
│    - Giá: 40K (giảm từ 50K)                     │
│    - Áp dụng: T2-T6, 14:00-17:00                │
│ Bước 3: Lưu                                     │
└─────────────────────────────────────────────────┘
```

### 5. Quản Lý Thiết Bị (Terminals)

```
Bước 1: Vào "Vận Hành" → "Thiết Bị"

Bước 2: Xem dashboard
   ├── 🟢 Online: 45 máy
   ├── 🔴 Offline: 3 máy
   └── 🟡 Bảo trì: 2 máy

Bước 3: Click thiết bị lỗi → Xem chi tiết
   └── Ping lại / Khởi động lại / Đánh dấu bảo trì
```

### 6. Tạo & Phát Voucher

```
┌─ Tạo Voucher ───────────────────────────────────┐
│ Bước 1: "Marketing" → "Voucher" → "Tạo Mới"     │
│ Bước 2: Nhập:                                   │
│    - Mã: SUMMER2024                             │
│    - Loại: Giảm % hoặc Giảm tiền cố định        │
│    - Giá trị: 20% (max 100K)                    │
│    - Điều kiện: Đơn từ 200K                     │
│    - Số lượng: 1000 lượt                        │
│    - Thời gian: 01/06 - 31/08                   │
│ Bước 3: Lưu                                     │
└─────────────────────────────────────────────────┘

┌─ Phát Voucher ──────────────────────────────────┐
│ Bước 1: Chọn voucher → "Phát"                   │
│ Bước 2: Chọn đối tượng:                         │
│    - Tất cả user                                │
│    - Theo hạng (Gold, Platinum)                 │
│    - Danh sách SĐT cụ thể                       │
│ Bước 3: Xác nhận → Gửi notification cho user    │
└─────────────────────────────────────────────────┘
```

### 7. Gửi Thông Báo Hàng Loạt

```
Bước 1: "Marketing" → "Thông Báo" → "Tạo Mới"

Bước 2: Soạn:
   ├── Tiêu đề: Khuyến mãi cuối tuần!
   ├── Nội dung: Giảm 30% tất cả game...
   └── Link/Action: Mở màn hình khuyến mãi

Bước 3: Chọn đối tượng → Gửi ngay hoặc Hẹn giờ
```

### 8. Mở Khóa Thẻ

```
Bước 1: Tra cứu user → Chọn thẻ đang bị khóa

Bước 2: Click "Mở Khóa"

Bước 3: Xác nhận danh tính khách
   └── OTP qua SĐT hoặc CCCD tại quầy

Bước 4: Nhập ghi chú → Xác nhận
```

---

## 👑 SUPER_ADMIN (Chủ Công Viên)

SUPER_ADMIN có tất cả quyền của ADMIN, cộng thêm:

### 1. Quản Lý Tài Khoản Admin

```
┌─ Tạo Admin Mới ─────────────────────────────────┐
│ Bước 1: "Hệ Thống" → "Quản Lý Admin" → "Thêm"   │
│ Bước 2: Nhập:                                   │
│    - SĐT, Họ tên, Mã nhân viên                  │
│    - Vai trò: ADMIN hoặc STAFF                  │
│ Bước 3: Lưu → Gửi mật khẩu tạm qua SMS          │
└─────────────────────────────────────────────────┘

┌─ Khóa/Xóa Admin ────────────────────────────────┐
│ Bước 1: Chọn admin cần xử lý                    │
│ Bước 2: "Khóa" hoặc "Xóa"                       │
│ Bước 3: Nhập lý do → Xác nhận                   │
└─────────────────────────────────────────────────┘
```

### 2. Cấu Hình Hệ Thống

```
Bước 1: "Hệ Thống" → "Cấu Hình"

Bước 2: Xem/Sửa các config:
   ├── MIN_TOPUP_AMOUNT = 50000 (Nạp tối thiểu)
   ├── MAX_TOPUP_AMOUNT = 10000000 (Nạp tối đa)
   ├── REFERRAL_BONUS = 20000 (Thưởng giới thiệu)
   └── ...

Bước 3: Sửa → Nhập lý do → Lưu
```

### 3. Xem Nhật Ký Kiểm Toán (Audit Log)

```
Bước 1: "Hệ Thống" → "Nhật Ký"

Bước 2: Tìm kiếm:
   ├── Theo Admin: Ai thực hiện
   ├── Theo hành động: Tạo/Sửa/Xóa/Khóa
   ├── Theo đối tượng: User/Game/Voucher
   └── Theo thời gian

Bước 3: Xem chi tiết từng log
   └── Dữ liệu trước/sau khi thay đổi
```

### 4. Duyệt Hoàn Tiền Lớn (> 2 triệu)

```
Bước 1: Vào "Tài Chính" → "Hoàn Tiền" → Lọc "> 2 triệu"

Bước 2: Xem xét kỹ từng yêu cầu

Bước 3: Duyệt hoặc Từ chối
```

---

# 📊 BẢNG TÓM TẮT QUYỀN HẠN

| Chức năng | STAFF | ADMIN | SUPER_ADMIN |
|-----------|:-----:|:-----:|:-----------:|
| **Khách hàng** |
| Tra cứu user | ✅ | ✅ | ✅ |
| Khóa thẻ | ✅ | ✅ | ✅ |
| Mở khóa thẻ | ❌ | ✅ | ✅ |
| Xử lý ticket hỗ trợ | ✅ | ✅ | ✅ |
| **Tài chính** |
| Xem giao dịch | ✅ | ✅ | ✅ |
| Duyệt hoàn tiền ≤ 2 triệu | ❌ | ✅ | ✅ |
| Duyệt hoàn tiền > 2 triệu | ❌ | ❌ | ✅ |
| Điều chỉnh số dư | ❌ | ✅ | ✅ |
| Xem báo cáo doanh thu | ❌ | ✅ | ✅ |
| **Vận hành** |
| Xem giám sát lượt chơi | ✅ | ✅ | ✅ |
| Quản lý games | ❌ | ✅ | ✅ |
| Quản lý thiết bị | ❌ | ✅ | ✅ |
| **Marketing** |
| Tạo/Phát voucher | ❌ | ✅ | ✅ |
| Gửi thông báo hàng loạt | ❌ | ✅ | ✅ |
| **Hệ thống** |
| Xem audit log | ❌ | ❌ | ✅ |
| Sửa config hệ thống | ❌ | ❌ | ✅ |
| Tạo/Xóa Admin | ❌ | ❌ | ✅ |

---

*Tài liệu hướng dẫn vận hành Admin - Park Adventure*
