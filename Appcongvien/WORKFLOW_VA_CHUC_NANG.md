# 📱 Ứng Dụng Park Adventure - Luồng Hoạt Động & Chức Năng

## 🎯 Tổng Quan Ứng Dụng

**Park Adventure** là một ứng dụng quản lý thẻ thành viên công viên giải trí cho phép người dùng mua vé, nạp tiền, quản lý voucher, chơi game, và theo dõi lịch sử giao dịch.

---

## 🔄 LUỒNG HOẠT ĐỘNG CHÍNH

### 1️⃣ LUỒNG ĐĂNG NHẬP & XÁC THỰC

#### **Màn Hình Đăng Nhập (LoginScreen)**
```
Người dùng → Nhập số điện thoại → Nhập mật khẩu → Xác minh
                                                    ↓
                                    ✅ Đăng nhập thành công → HomeScreen
                                    ❌ Thất bại → Hiển thị lỗi
```

**Chức năng:**
- Nhập số điện thoại (Phone)
- Nhập mật khẩu với toggle hiện/ẩn
- Nút "Quên mật khẩu" → ForgotPasswordScreen
- Nút "Đăng ký" → RegisterScreen
- Hiển thị Loading khi đang xác thực
- Điều khoản sử dụng & Chính sách bảo mật

---

#### **Màn Hình Đăng Ký (RegisterScreen)**
```
Người dùng → Nhập thông tin (Tên, SĐT, Mật khẩu) → Xác minh
                                                    ↓
                                    ✅ Đăng ký thành công → HomeScreen
                                    ❌ Thất bại → Hiển thị lỗi
```

**Chức năng:**
- Nhập Tên đầy đủ
- Nhập Số điện thoại
- Nhập Mật khẩu
- Xác nhận mật khẩu
- Chấp nhận điều khoản dịch vụ

---

#### **Màn Hình Quên Mật Khẩu (ForgotPasswordScreen)**
```
Người dùng → Nhập SĐT → Nhập OTP → Đặt mật khẩu mới
                                        ↓
                                    ✅ Thành công → LoginScreen
```

**Chức năng:**
- Nhập số điện thoại
- Gửi OTP
- Xác minh OTP
- Thiết lập mật khẩu mới
- Quay lại đăng nhập

---

### 2️⃣ LUỒNG TRANG CHỦ (HOME FLOW)

```
HomeScreen (Trang Chủ)
    ├─ Header Section (Chào mừng + Thông báo)
    ├─ Card Section (Thẻ thành viên hiển thị)
    ├─ Quick Actions (3 nút nhanh)
    │   ├─ Nạp tiền
    │   ├─ Mua game
    │   └─ Voucher
    └─ Feature Section (6 dịch vụ chính)
        ├─ Thông tin thẻ
        ├─ Danh sách game
        ├─ Lịch sử
        ├─ Hồ sơ
        ├─ Khóa thẻ
        └─ Bản đồ công viên
```

**Chức năng chính:**
- Hiển thị tên người dùng trong header
- Badge thông báo (3 thông báo chưa đọc)
- Hiển thị số dư & điểm thưởng (có toggle ẩn/hiện)
- Nút quick action để truy cập nhanh các tính năng chính
- Menu feature 6 dịch vụ phổ biến

---

### 3️⃣ LUỒNG QUẢN LÝ THẺ

#### **Xem Thông Tin Thẻ (CardInfoScreen)**
```
CardInfoScreen
    ├─ Hiển thị thẻ thành viên (card visual)
    │   ├─ Số thẻ (có toggle ẩn/hiện)
    │   ├─ Chủ thẻ
    │   └─ Ngày hết hạn
    ├─ Quick Actions
    │   ├─ Quét thẻ (QR Code)
    │   └─ Sao chép số thẻ
    ├─ Trạng thái thẻ (Đang hoạt động/Hết hạn/Tạm khóa)
    └─ Nút Khóa/Mở khóa thẻ
```

**Chức năng:**
- Xem đầy đủ thông tin thẻ
- Toggle ẩn/hiện số thẻ
- Quét QR code từ thẻ
- Sao chép số thẻ vào clipboard
- Khóa thẻ để bảo mật
- Xem tư cách thành viên (Đồng, Bạc, Vàng, Bạch Kim)

---

#### **Khóa Thẻ (LockCardScreen)**
```
LockCardScreen
    ├─ Hiển thị trạng thái thẻ (Đã khóa / Hoạt động)
    ├─ Icon & thông báo tương ứng
    └─ Nút Mở khóa / Khóa thẻ
```

**Chức năng:**
- Xem trạng thái khóa thẻ
- Toggle khóa/mở khóa thẻ
- Hiển thị loading khi đang xử lý
- Thông báo trạng thái hiện tại

---

### 4️⃣ LUỒNG NẠP TIỀN (TOP-UP FLOW)

```
TopUpScreen
    ├─ Hiển thị thông tin nạp
    │   ├─ Số tiền hiện tại: 250,000 VND
    │   └─ Điểm thưởng: 1,250 pts
    ├─ Nhập số tiền muốn nạp
    ├─ Nút nạp nhanh (50k, 100k, 200k, 500k)
    ├─ Chọn phương thức thanh toán
    │   ├─ MoMo
    │   ├─ VNPay
    │   └─ Ngân hàng
    └─ Nút Nạp tiền (với loading)
```

**Chức năng:**
- Nhập số tiền tùy chỉnh (tối thiểu 10,000 VND)
- Quick amount buttons (50k, 100k, 200k, 500k)
- Chọn phương thức thanh toán
- Hiển thị phương thức được chọn
- Loading indicator khi đang xử lý
- Kiểm tra tối thiểu trước khi thanh toán

---

### 5️⃣ LUỒNG MUA GAME (CHECKOUT FLOW)

```
BuyGameScreen (Giỏ hàng)
    ├─ Danh sách game đã thêm vào giỏ
    │   ├─ Tên game
    │   ├─ Số lượt (với +/- để điều chỉnh)
    │   ├─ Giá gốc & giá khuyến mãi
    │   ├─ Tiết kiệm
    │   └─ Nút xóa
    ├─ Chọn voucher & khuyến mãi
    │   ├─ Danh sách voucher có sẵn
    │   └─ Hiển thị tiết kiệm từ voucher
    ├─ Tính toán giá
    │   ├─ Tạm tính
    │   ├─ Tiết kiệm
    │   ├─ Voucher discount
    │   └─ Tổng cộng
    └─ Nút Thanh toán
```

**Chức năng:**
- Hiển thị giỏ hàng trống nếu không có game
- Điều chỉnh số lượt chơi (+/-)
- Xóa game khỏi giỏ hàng
- Chọn & áp dụng voucher
- Tự động tính toán giá
- Hiển thị tiết kiệm từ voucher & khuyến mãi
- Nút thanh toán

---

#### **Thanh Toán (PaymentScreen)**
```
PaymentScreen
    ├─ Hiển thị tổng số tiền
    ├─ Chọn phương thức thanh toán
    │   ├─ Ví Momo
    │   ├─ VNPay
    │   └─ Thẻ ngân hàng
    └─ Nút Xác nhận thanh toán
```

**Chức năng:**
- Hiển thị tổng giá trị thanh toán
- Chọn 1 trong 3 phương thức
- Xác nhận và thanh toán

---

### 6️⃣ LUỒNG XEM DANH SÁCH GAME (GAME LIST)

```
GameListScreen
    ├─ Search bar (tìm kiếm game)
    ├─ Danh sách game
    │   ├─ Hình ảnh game
    │   ├─ Tên game
    │   ├─ Mô tả ngắn
    │   ├─ Giá per turn
    │   ├─ Khuyến mãi (%)
    │   ├─ Yêu cầu tuổi & chiều cao
    │   ├─ Vị trí
    │   ├─ Mức độ (Thấp/Trung bình/Cao)
    │   ├─ Đánh giá ⭐
    │   └─ Nút "Xem chi tiết"
    └─ Click game → GameDetailScreen
```

**Chức năng:**
- Tìm kiếm game theo tên
- Hiển thị chi tiết từng game
- Xem yêu cầu và vị trí
- Click để xem chi tiết và mua

---

#### **Chi Tiết Game (GameDetailScreen)**
```
GameDetailScreen
    ├─ Hình ảnh game full-width
    ├─ Tên game & đánh giá
    ├─ Mô tả chi tiết
    ├─ Thông tin kỹ thuật
    │   ├─ Tuổi: 8+
    │   ├─ Chiều cao: 1.2m
    │   ├─ Vị trí: Khu A
    │   ├─ Loại: Indoor/Outdoor
    │   └─ Mức độ: Cao
    ├─ Giá cả
    │   ├─ Giá gốc
    │   ├─ Khuyến mãi
    │   └─ Giá cuối cùng
    ├─ Quyền lợi
    │   ├─ ✓ Bảo hiểm tai nạn
    │   ├─ ✓ Hướng dẫn viên
    │   └─ ✓ Ảnh lưu niệm
    └─ Nút "Thêm vào giỏ hàng"
```

**Chức năng:**
- Xem thông tin chi tiết game
- Xem quyền lợi & bảo hiểm
- Thêm vào giỏ hàng
- Quay lại

---

### 7️⃣ LUỒNG QUẢN LÝ VOUCHER

#### **Danh Sách Voucher Khả Dụng (VouchersScreen)**
```
VouchersScreen
    ├─ Danh sách tất cả voucher
    │   ├─ Badge chiết khấu (20%, 50k)
    │   ├─ Tên voucher
    │   ├─ Mô tả
    │   ├─ Điều kiện áp dụng
    │   ├─ Ngày hết hạn
    │   ├─ Cảnh báo sắp hết hạn ⚠️
    │   └─ Nút "Lưu"
    └─ Click để lưu voucher
```

**Chức năng:**
- Xem danh sách voucher khả dụng
- Hiển thị loại chiết khấu (% hoặc tiền)
- Hiển thị điều kiện áp dụng (đơn từ bao nhiêu)
- Cảnh báo sắp hết hạn
- Lưu voucher

---

#### **Ví Voucher (VoucherWalletScreen)**
```
VoucherWalletScreen
    ├─ Tab "Voucher của tôi"
    ├─ Danh sách voucher đã lưu
    │   ├─ Tên voucher
    │   ├─ Mô tả
    │   ├─ Điều kiện áp dụng
    │   ├─ Ngày hết hạn
    │   ├─ Cảnh báo ⚠️ nếu sắp hết hạn
    │   └─ Nút "Xóa"
    └─ Click để xoá hoặc sử dụng
```

**Chức năng:**
- Xem voucher đã lưu
- Xóa voucher
- Sử dụng khi thanh toán
- Cảnh báo hết hạn

---

### 8️⃣ LUỒNG XEM SỐ DƯ & LỊCH SỬ

#### **Màn Hình Số Dư (BalanceScreen)**
```
BalanceScreen
    ├─ Thẻ hiển thị số dư
    │   ├─ Số dư hiện tại (toggle ẩn/hiện)
    │   └─ Điểm thưởng
    ├─ Thẻ thông tin thành viên
    │   ├─ Tư cách thành viên
    │   ├─ Ngày tham gia
    │   └─ Quyền lợi
    ├─ Quick Actions
    │   ├─ Nạp tiền
    │   ├─ Lịch sử thanh toán
    │   └─ Lịch sử sử dụng
    └─ Danh sách giao dịch gần đây
        ├─ Loại: Nạp tiền/Chơi game/Hoàn tiền/Thưởng
        ├─ Số tiền
        ├─ Mô tả
        └─ Thời gian
```

**Chức năng:**
- Xem số dư hiện tại
- Toggle ẩn/hiện số dư
- Xem điểm thưởng
- Xem lịch sử giao dịch
- Nạp tiền nhanh
- Xem chi tiết thanh toán & sử dụng

---

#### **Lịch Sử Thanh Toán (PaymentHistoryScreen)**
```
PaymentHistoryScreen
    ├─ Danh sách giao dịch nạp tiền
    │   ├─ Phương thức (MoMo/VNPay/Ngân hàng)
    │   ├─ Số tiền
    │   ├─ Trạng thái (✅ Thành công / ⏳ Chờ xử lý / ❌ Thất bại)
    │   ├─ Thời gian
    │   └─ Mã giao dịch
    └─ Xem chi tiết từng giao dịch
```

**Chức năng:**
- Xem lịch sử nạp tiền
- Filter theo trạng thái
- Xem chi tiết giao dịch
- Mã giao dịch để theo dõi

---

#### **Lịch Sử Sử Dụng (UsageHistoryScreen)**
```
UsageHistoryScreen
    ├─ Danh sách game đã chơi
    │   ├─ Tên game
    │   ├─ Số lượt
    │   ├─ Giá gốc
    │   ├─ Giá ưu đãi
    │   ├─ Tiết kiệm
    │   ├─ Voucher sử dụng
    │   └─ Thời gian
    ├─ Chi tiết từng game
    │   ├─ Giá gốc mỗi lượt
    │   ├─ Tổng giá gốc
    │   ├─ Chiết khấu
    │   ├─ Voucher discount
    │   └─ Giá cuối cùng
    └─ Xem chi tiết
```

**Chức năng:**
- Xem lịch sử chơi game
- Xem giá gốc & giá thanh toán
- Xem voucher đã sử dụng
- Xem tiết kiệm được

---

### 9️⃣ LUỒNG QUẢN LÝ HỒNG SƠ & SETTINGS

#### **Xem Hồ Sơ (ProfileScreen)**
```
ProfileScreen
    ├─ Ảnh đại diện
    ├─ Thông tin cá nhân
    │   ├─ Tên: Mai Văn Tĩnh
    │   ├─ Tư cách: Thành viên Vàng
    │   ├─ Ngày tham gia: 15/01/2024
    │   └─ Điện thoại: 09XXXXXXXXX
    ├─ Thống kê tài khoản
    │   ├─ Tổng lượt chơi
    │   ├─ Tổng chi tiêu
    │   ├─ Tiết kiệm được
    │   └─ Game yêu thích
    └─ Các tùy chọn khác
        ├─ Chỉnh sửa hồ sơ
        ├─ Cài đặt
        └─ Đăng xuất
```

**Chức năng:**
- Xem thông tin cá nhân
- Xem thống kê sử dụng
- Chỉnh sửa hồ sơ
- Truy cập cài đặt
- Đăng xuất

---

#### **Cài Đặt (SettingsScreen)**
```
SettingsScreen
    ├─ Cài đặt tài khoản
    │   ├─ Thay đổi mật khẩu
    │   └─ Xóa tài khoản
    ├─ Cài đặt ứng dụng
    │   ├─ Ngôn ngữ (Tiếng Việt)
    │   ├─ Chế độ tối / Sáng
    │   └─ Thông báo
    ├─ Hỗ trợ & Thông tin
    │   ├─ Liên hệ hỗ trợ
    │   └─ Về ứng dụng
    └─ Thoát
        └─ Nút Đăng xuất
```

**Chức năng:**
- Thay đổi mật khẩu
- Cài đặt ngôn ngữ
- Bật/tắt chế độ tối
- Quản lý thông báo
- Liên hệ hỗ trợ
- Xem thông tin phiên bản
- Đăng xuất

---

#### **Thay Đổi Mật Khẩu (ChangePasswordScreen)**
```
ChangePasswordScreen
    ├─ Nhập mật khẩu cũ
    ├─ Nhập mật khẩu mới
    ├─ Xác nhận mật khẩu mới
    └─ Nút "Cập nhật"
```

**Chức năng:**
- Xác minh mật khẩu cũ
- Nhập mật khẩu mới
- Xác nhận mật khẩu
- Cập nhật thành công

---

### 🔟 LUỒNG MÃ GIỚI THIỆU (REFERRAL FLOW)

```
ReferralCodeScreen
    ├─ Mã giới thiệu của bạn
    │   ├─ Hiển thị mã
    │   └─ Nút Sao chép / Chia sẻ
    ├─ Quyền lợi giới thiệu
    │   ├─ Cho người mời: Voucher 100k
    │   └─ Cho người được mời: 5 lượt chơi miễn phí
    ├─ Thống kê
    │   ├─ Người đã mời: 12
    │   └─ Người đã nạp tiền: 8
    └─ Danh sách người được mời
        ├─ Tên người
        ├─ Trạng thái
        └─ Tổng chi tiêu
```

**Chức năng:**
- Xem mã giới thiệu
- Sao chép mã
- Chia sẻ mã
- Xem quyền lợi
- Xem thống kê giới thiệu
- Xem danh sách người đã mời

---

### 1️⃣1️⃣ LUỒNG THÔNG BÁO

```
NotificationsScreen
    ├─ Danh sách thông báo
    │   ├─ Thông báo khuyến mãi
    │   │   └─ "Voucher 20% vừa được phát hành!"
    │   ├─ Thông báo lịch sử
    │   │   └─ "Giao dịch #12345 thành công"
    │   ├─ Thông báo nạp tiền
    │   │   └─ "Tài khoản vừa được nạp 250k"
    │   ├─ Thông báo quà tặng
    │   │   └─ "Bạn có 5 lượt chơi miễn phí!"
    │   └─ Thông báo nâng cấp
    │       └─ "Bạn đã nâng lên thành viên Vàng"
    └─ Click để xem chi tiết hoặc hành động
```

**Chức năng:**
- Hiển thị tất cả thông báo
- Badge số lượng (3)
- Click thông báo để hành động
- Các action: "Dùng ngay", "Xem ngay", "Nạp tiền", "Nhận quà", "Nâng cấp"

---

### 1️⃣2️⃣ LUỒNG HỖ TRỢ (SUPPORT CHAT)

```
SupportChatScreen
    ├─ Header: Hỗ Trợ Khách Hàng
    ├─ Chat messages
    │   ├─ Bot: "Chào bạn! Tôi là [tên agent], rất vui được hỗ trợ bạn"
    │   ├─ Người dùng: "Bạn có thể hỏi về cách nạp tiền"
    │   ├─ Bot: "Bạn có thể nạp tiền qua MoMo, tại quầy..."
    │   └─ ...tiếp tục chat
    ├─ Input field: Nhập tin nhắn
    └─ Send button
```

**Chức năng:**
- Chat với đại lý hỗ trợ
- Xem lịch sử chat
- Gửi tin nhắn
- Nhận hỗ trợ từ agent

---

### 1️⃣3️⃣ LUỒNG THẺ THÀNH VIÊN (MEMBER CARD)

```
MemberCardScreen
    ├─ Hiển thị thẻ thành viên
    │   ├─ Logo/Tên công viên
    │   ├─ Tư cách thành viên
    │   ├─ Số thẻ
    │   ├─ Tên thành viên
    │   └─ Hết hạn
    ├─ Quyền lợi thành viên
    │   ├─ Giảm 10% tất cả game
    │   ├─ Điểm thưởng x1.5
    │   └─ Ưu tiên VIP
    └─ Nút In thẻ
```

**Chức năng:**
- Xem thẻ thành viên
- Xem quyền lợi
- In thẻ

---

## 📊 CÁC THÀNH PHẦN CHÍNH (COMPONENTS)

### **HeaderSection**
- Hiển thị chào mừng
- Tên người dùng
- Nút thông báo với badge

### **CardSection**
- Hiển thị thẻ thành viên
- Toggle ẩn/hiện số dư
- Hiển thị điểm thưởng
- Nút quét thẻ

### **QuickActions**
- 3 nút hành động nhanh
- Nạp tiền / Mua game / Voucher

### **FeatureSection (ServicesSection)**
- 6 dịch vụ chính
- Thông tin thẻ / Game list / Lịch sử / Hồ sơ / Khóa thẻ / Bản đồ

---

## 🗂️ CẤU TRÚC DỮ LIỆU (DATA MODELS)

### **MemberCard**
```kotlin
data class MemberCard(
    val cardNumber: String,
    val holderName: String,
    val expiryDate: String,
    val membershipLevel: MembershipLevel,
    val issueDate: String,
    val status: CardStatus,
    val balance: Int,
    val points: Int,
    val isLocked: Boolean
)

enum class MembershipLevel {
    BRONZE("Đồng"),
    SILVER("Bạc"),
    GOLD("Vàng"),
    PLATINUM("Bạch Kim")
}
```

### **Voucher**
```kotlin
data class Voucher(
    val id: String,
    val title: String,
    val description: String,
    val discountType: DiscountType, // PERCENTAGE hoặc AMOUNT
    val value: Int,
    val minPurchase: Int,
    val expiryDate: String,
    val isExpiringSoon: Boolean
)
```

### **Game**
```kotlin
data class Game(
    val id: String,
    val name: String,
    val pricePerTurn: Int,
    val discount: Int,
    val ageRange: String,
    val heightRequirement: String,
    val location: String,
    val type: GameType,
    val riskLevel: RiskLevel,
    val rating: Float
)
```

### **BalanceTransaction**
```kotlin
data class BalanceTransaction(
    val id: String,
    val type: TransactionType, // TOP_UP, GAME_PLAY, REFUND, BONUS
    val amount: Int,
    val description: String,
    val timestamp: String
)
```

### **PaymentRecord**
```kotlin
data class PaymentRecord(
    val id: String,
    val method: PaymentMethod, // MOMO, ZALOPAY, BANKING, CREDIT_CARD, CASH
    val amount: Int,
    val status: PaymentStatus, // SUCCESS, PENDING, FAILED
    val timestamp: String,
    val transactionId: String
)
```

---

## 🧭 ĐIỀU HƯỚNG (NAVIGATION STRUCTURE)

```
Screen Routes:
├─ Login
├─ Register
├─ ForgotPassword
├─ ChangePassword
├─ Home (Trang Chủ)
├─ CardInfo (Thông Tin Thẻ)
├─ LockCard (Khóa Thẻ)
├─ Balance (Số Dư)
├─ TopUp (Nạp Tiền)
├─ Checkout (Giỏ Hàng / Mua Game)
├─ GameDetail (Chi Tiết Game)
├─ Vouchers (Danh Sách Voucher)
├─ VoucherWallet (Ví Voucher)
├─ ReferralCode (Mã Giới Thiệu)
├─ MemberCard (Thẻ Thành Viên)
├─ Settings (Cài Đặt)
├─ Profile (Hồ Sơ)
├─ SupportChat (Hỗ Trợ)
├─ Notifications (Thông Báo)
├─ PaymentHistory (Lịch Sử Thanh Toán)
├─ UsageHistory (Lịch Sử Sử Dụng)
└─ GameList (Danh Sách Game)
```

---

## 🎨 THIẾT KẾ & CHỦ ĐỀ MÀU

### **Color Scheme:**
- Primary Orange: `#FF6F00` (Nút, highlight)
- Primary Dark: `#2C2C2C` (Text)
- Primary Gray: `#757575` (Text phụ)
- Warm Orange: `#FF8C00` (Action buttons)
- Surface Light: `#FFF8F0` (Background)
- Success Green: `#4CAF50` (Thành công)
- Error Red: `#F44336` (Lỗi)

### **Gradient:**
- Header Gradient: Kem nhạt → Cam vừa → Cam ấm
- Card Gradient: Than chì → Xám nhạt hơn

---

## 📱 NAVIGATION BAR (3 Tabs Chính)

```
┌─────────────────────────────────────┐
│ 🏠 Trang Chủ  │  🎁 Khuyến Mãi  │ 👤 Hồ Sơ │
└─────────────────────────────────────┘
    HOME              VOUCHERS        PROFILE
```

---

## 🔐 TÍNH NĂNG BẢO MẬT

1. **Đăng nhập / Đăng ký**: Xác thực bằng số điện thoại + mật khẩu
2. **Khóa Thẻ**: Có thể khóa thẻ để tránh sử dụng trái phép
3. **Toggle ẩn/hiện số dư**: Bảo vệ quyền riêng tư
4. **Lịch sử giao dịch**: Theo dõi tất cả hoạt động

---

## ✨ TÍNH NĂNG NỔI BẬT

1. ✅ **Quản lý thẻ thành viên** - Xem, khóa, quét thẻ
2. ✅ **Nạp tiền linh hoạt** - Hỗ trợ MoMo, VNPay, Ngân hàng
3. ✅ **Mua vé game online** - Với chiết khấu & voucher
4. ✅ **Quản lý voucher** - Lưu, sử dụng, theo dõi hạn sử dụng
5. ✅ **Lịch sử chi tiết** - Theo dõi tất cả giao dịch
6. ✅ **Mã giới thiệu** - Nhận thưởng khi giới thiệu bạn
7. ✅ **Chat hỗ trợ** - Liên hệ đại lý 24/7
8. ✅ **Thông báo đẩy** - Cập nhật khuyến mãi & sự kiện
9. ✅ **Quản lý hồ sơ** - Cập nhật thông tin cá nhân
10. ✅ **Tư cách thành viên** - Đồng, Bạc, Vàng, Bạch Kim với quyền lợi khác nhau

---

## 🚀 CÔNG NGHỆ SỬ DỤNG

- **Framework**: Jetpack Compose (Android UI)
- **Navigation**: Navigation Compose
- **Architecture**: MVVM Pattern
- **State Management**: Mutable State / Remember
- **Language**: Kotlin

---

## 📝 GHI CHÚ

- Tất cả đơn vị tiền tệ: VND (Đồng Việt Nam)
- Tất cả thời gian: Định dạng 24h
- Tất cả giao diện: Hỗ trợ Tiếng Việt
- Responsive design: Thích ứng với mọi kích cỡ màn hình

---

**Cập nhật lần cuối**: 21/01/2026
