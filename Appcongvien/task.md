# Huashu Light UI Upgrade Task

## Nguyên tắc bất biến
- Không thay đổi ViewModel, API, repository, business logic.
- Không thay đổi navigation hoặc route.
- Không thay đổi layout structure hoặc component hierarchy của từng màn hình.
- Chỉ tinh chỉnh UI: spacing, color, typography, size, shape, elevation, trạng thái hiển thị và animation entry rất nhẹ.
- Mỗi đợt chỉ làm một batch nhỏ, ưu tiên màn hình đang dùng trong app trước.

## Chuẩn Huashu Light cần bám
- Primary accent: `#FF7A00`
- Background chính: `#F5F5F3`
- Text chính: `#1C1C1E`
- Orange chỉ dùng như accent, khoảng 10-20% tổng giao diện
- Highlight component dùng gradient rất nhẹ, không lạm dụng
- Spacing chuẩn:
  - Micro: `8dp`
  - Small: `12dp`
  - Medium: `16dp`
  - Section: `24dp`
- Typography:
  - Title tăng `+2sp` đến `+4sp`, đậm hơn
  - Giá trị quan trọng lớn hơn và đậm hơn
  - Secondary text giữ alpha khoảng `0.6f` đến `0.8f`
- Card:
  - Radius `16dp` đến `20dp`
  - Elevation nhẹ
  - Padding thoáng hơn
- Button:
  - Height `44dp` đến `52dp`
  - Radius `12dp` đến `16dp`
- Animation:
  - Chỉ fade + translateY nhẹ `10dp` đến `20dp`
  - Không làm animation nặng

## Phạm vi active cần ưu tiên
Theo `NavGraph.kt`, các màn hình đang đi trực tiếp trong app gồm:
- `LoginScreen`
- `RegisterScreen`
- `ForgotPasswordScreen`
- `ChangePasswordScreen`
- `HomeScreen`
- `CardInfoScreen`
- `CardRequestScreen`
- `BalanceScreen`
- `TopUpScreen`
- `GameListScreen`
- `GameDetailScreen`
- `SettingsScreen`
- `ProfileScreen`
- `SupportChatScreen`
- `NotificationsScreen`
- `PaymentHistoryScreen`
- `UsageHistoryScreen`

## Shared components cần đồng bộ trước
Các composable dùng lại nhiều nơi, nên xử lý trước hoặc cùng batch màn hình liên quan:
- `ui/theme/Color.kt`
- `ui/theme/Theme.kt`
- `ui/theme/Type.kt`
- `components/ParkTopAppBar.kt`
- `components/HeaderSection.kt`
- `components/CardSection.kt`
- `components/QuickActions.kt`
- `components/Carousel.kt`
- `components/ServicesSection.kt`

## Kế hoạch theo batch

### Batch 0 - Foundation
Mục tiêu:
- Chuẩn hóa color tokens sang Huashu Light.
- Chuẩn hóa nền sáng, surface, text primary, text secondary, accent orange.
- Chuẩn hóa typography hierarchy cho title, subtitle, meta.
- Chuẩn hóa radius, elevation, button height, spacing constants.
- Rà soát shared component để đồng bộ padding và visual weight mà không đổi hierarchy.

Checklist:
- [x] Audit toàn bộ màu hiện tại trong `ui/theme` và `components`
- [x] Thiết kế bộ token Huashu Light dùng chung
- [x] Chuẩn hóa `ParkTopAppBar`
- [x] Chuẩn hóa `HeaderSection`
- [x] Chuẩn hóa `CardSection`
- [x] Chuẩn hóa `QuickActions`
- [x] Xem lại các component còn lại có đang lệch spacing hay không

Definition of done:
- Không đổi public API của composable
- Không thêm luồng điều hướng mới
- Shared UI nhìn sáng hơn, thoáng hơn, cam chỉ còn là accent

### Batch 1 - Auth Flow
Phạm vi:
- `auth/LoginScreen.kt`
- `auth/RegisterScreen.kt`
- `auth/ForgotPasswordScreen.kt`
- `auth/ChangePasswordScreen.kt`

Mục tiêu:
- Làm sạch rhythm dọc trong form
- Giảm cảm giác gắt của nền cam toàn màn hình
- Làm card/form premium hơn bằng spacing, radius, text hierarchy
- Chuẩn hóa chiều cao button và input

Checklist:
- [x] `LoginScreen` - bắt đầu đầu tiên
- [x] `RegisterScreen`
- [x] `ForgotPasswordScreen`
- [x] `ChangePasswordScreen`

Lưu ý:
- Giữ nguyên số lượng section, thứ tự trường nhập và hierarchy hiện có
- Chỉ chỉnh padding, khoảng cách, cỡ chữ, màu, shape, elevation

### Batch 2 - Home
Phạm vi:
- `HomeScreen.kt`
- Shared home components liên quan

Mục tiêu:
- Giữ nguyên bố cục header, balance card, quick actions, banner, feature section
- Giảm độ nặng của gradient
- Tăng độ rõ của section spacing và card emphasis
- Làm balance card thành điểm nhấn chính nhưng vẫn theo Huashu Light

Checklist:
- [x] `HomeScreen`
- [x] Căn lại spacing giữa `HeaderSection`, `CardSection`, `QuickActions`, `ImageCarousel`, `FeatureSection`
- [x] Rà soát top padding và section separation

### Batch 3 - Wallet / Payment
Phạm vi:
- `BalanceScreen.kt`
- `TopUpScreen.kt`
- `PaymentHistoryScreen.kt`
- `UsageHistoryScreen.kt`

Mục tiêu:
- Đồng bộ spacing list/card/action
- Tăng hierarchy cho số dư, lịch sử, trạng thái giao dịch
- Làm CTA rõ hơn nhưng không quá chói

Checklist:
- [x] `BalanceScreen`
- [x] `TopUpScreen`
- [x] `PaymentHistoryScreen`
- [x] `UsageHistoryScreen`

### Batch 4 - Card Flow
Phạm vi:
- `CardInfoScreen.kt`
- `CardRequestScreen.kt`

Mục tiêu:
- Giữ nguyên cấu trúc thông tin thẻ và yêu cầu thẻ
- Tăng độ thoáng, đọc nhanh hơn
- Làm card thông tin và trạng thái nổi bật hơn bằng shape, spacing, typography

Checklist:
- [ ] `CardInfoScreen`
- [ ] `CardRequestScreen`

### Batch 5 - Game Flow
Phạm vi:
- `GameListScreen.kt`
- `GameDetailScreen.kt`

Mục tiêu:
- Đồng bộ card game, chip/filter, review blocks
- Tăng phân cấp cho tên game, meta, review, mô tả
- Rà soát spacing trong list và detail sections

Checklist:
- [x] `GameListScreen`
- [x] `GameDetailScreen`

### Batch 6 - Profile / Settings / Support / Notifications
Phạm vi:
- `ProfileScreen.kt`
- `SettingsScreen.kt`
- `SupportChatScreen.kt`
- `NotificationsScreen.kt`

Mục tiêu:
- Làm section rõ và sạch hơn
- Đồng bộ card setting, item row, header block, form profile
- Giữ chat bubble và notification card gọn, dễ quét

Checklist:
- [x] `ProfileScreen`
- [x] `SettingsScreen`
- [x] `SupportChatScreen`
- [x] `NotificationsScreen`

## Màn hình cần xác minh sau
Các file này có trong thư mục `screen` nhưng chưa thấy nằm trong `NavGraph.kt` hiện tại:
- `PaymentScreen.kt`
- `VoucherWalletScreen.kt`
- `VouchersScreen.kt`
- `ReferralCodeScreen.kt`
- `MemberCardScreen.kt`
- `ChartScreen.kt`

Ghi chú:
- `MyGamesScreen.kt` đã có comment cho biết đã bỏ và được thay bằng lịch sử trong `BalanceScreen`
- Các màn hình chưa active chỉ xử lý sau khi xong active flow hoặc khi có xác nhận cần dùng

## Cách làm cho từng batch
1. Đọc code màn hình và component liên quan
2. Chốt các điểm lệch về spacing, color, typography
3. Chỉ sửa UI, không đổi structure
4. Compile sau mỗi batch nếu cần
5. Ghi lại:
   - spacing improvements
   - visual improvements
   - TODO còn lại

## Thứ tự thực hiện ngay bây giờ
- [x] Batch 0.1: audit token hiện tại và shared components
- [x] Batch 0.2: chuẩn hóa theme Huashu Light
- [x] Batch 0.3: chuẩn hóa `ParkTopAppBar`
- [x] Batch 0.4: chuẩn hóa `HeaderSection`
- [x] Batch 0.5: chuẩn hóa `CardSection`
- [x] Batch 0.6: chuẩn hóa `QuickActions`
- [x] Batch 0.7: rà và tinh chỉnh `Carousel` / `ServicesSection`
- [x] Batch 1.1: nâng cấp `LoginScreen`
- [x] Batch 1.2: nâng cấp `RegisterScreen`
- [x] Batch 1.3: nâng cấp `ForgotPasswordScreen`
- [x] Batch 1.4: nâng cấp `ChangePasswordScreen`
- [x] Batch 2.1: nâng cấp `HomeScreen`
- [x] Batch 2.2: cân lại spacing giữa `HeaderSection` / `CardSection` / `QuickActions` / `ImageCarousel` / `FeatureSection`
- [x] Batch 2.3: rà soát top padding và section separation
- [x] Batch 3.1: nâng cấp `BalanceScreen`
- [x] Batch 3.2: nâng cấp `TopUpScreen`
- [x] Batch 3.3: nâng cấp `PaymentHistoryScreen`
- [x] Batch 3.4: nâng cấp `UsageHistoryScreen`
- [x] Batch 5.1: nâng cấp `GameListScreen`
- [x] Batch 5.2: nâng cấp `GameDetailScreen`
- [x] Batch 6.1: nâng cấp `ProfileScreen`
- [x] Batch 6.2: nâng cấp `SettingsScreen`
- [x] Batch 6.3: nâng cấp `SupportChatScreen`
- [x] Batch 6.4: nâng cấp `NotificationsScreen`

## Ghi chú triển khai
- Nếu một shared component đang phục vụ nhiều màn hình, ưu tiên chỉnh token và spacing an toàn trước
- Nếu một màn hình có quá nhiều phần, chia tiếp theo section nhưng vẫn commit theo đúng một màn hình
- Khi có nguy cơ ảnh hưởng layout, ưu tiên dừng ở mức token hoặc style cục bộ
