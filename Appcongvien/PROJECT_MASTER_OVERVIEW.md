# 🎡 Park Adventure - Tổng Quan Dự Án (Project Master Overview)

Tài liệu này cung cấp cái nhìn toàn diện và chi tiết nhất về dự án **Park Adventure** - Hệ thống quản lý và vận hành công viên giải trí hiện đại áp dụng công nghệ "Non-cash" (Không tiền mặt) và "One-Touch" (Một chạm).

---

## 1. 🌟 Giới Thiệu Chung (Introduction)

**Park Adventure** là một hệ sinh thái phần mềm kết hợp phần cứng nhằm số hóa toàn bộ trải nghiệm vui chơi tại công viên. Mục tiêu cốt lõi là loại bỏ vé giấy, tiền mặt, thay thế bằng **Thẻ thông minh (Smart Card)** và **Ứng dụng di động (Mobile App)**.

### Triết lý thiết kế: "Unified Balance" (Tài chính hợp nhất)
*   **"Tiền ở đâu cũng là một"**: Số dư trong Thẻ và trên App là một.
*   **Real-time Banking**: Mọi giao dịch được xử lý tức thời trên Server trung tâm (Core Banking). Thẻ đóng vai trò là chìa khóa xác thực (Token), không lưu tiền trực tiếp (để tránh rủi ro mất thẻ mất tiền và đồng bộ dữ liệu).

---

## 2. 🏗️ Kiến Trúc Hệ Thống (System Architecture)

Hệ thống hoạt động dựa trên mô hình **Online First**, bao gồm 4 thành phần chính tương tác chặt chẽ:

### 2.1. Mobile Application (Android App)
*   **Vai trò**: Là ví điện tử và cổng thông tin của người dùng.
*   **Chức năng chính**:
    *   Đăng ký/Đăng nhập.
    *   Nạp tiền vào tài khoản (MoMo, Banking).
    *   Quản lý thẻ (Kích hoạt thẻ mới, Khóa thẻ mất).
    *   Mua vé/Combo vé trước.
    *   Xem lịch sử giao dịch.
*   **Công nghệ**: Android Native (Kotlin), Jetpack Compose (UI).

### 2.2. Smart Card (JavaCard)
*   **Vai trò**: "Chìa khóa vạn năng" để định danh người chơi tại các máy game.
*   **Đặc điểm**:
    *   Lưu trữ an toàn: User ID và Khóa bảo mật (Secret Keys).
    *   Giao tiếp NFC: "Tap & Play" (Chạm và chơi).
    *   **Low Cost**: Thẻ trắng có thể mua và tự kích hoạt (Self-Service).

### 2.3. Terminals (Thiết bị đầu cuối/Máy POS)
*   **Vai trò**: Cổng kiểm soát tại từng trò chơi.
*   **Hoạt động**:
    1.  Nhận tín hiệu từ thẻ (hoặc QR Code).
    2.  Gửi yêu cầu lên Server kiểm tra số dư/vé.
    3.  Nhận lệnh từ Server -> Mở cổng/Kích hoạt máy game.

### 2.4. Backend Server (Core Banking)
*   **Vai trò**: Bộ não trung tâm.
*   **Chức năng**:
    *   Quản lý User, Tài khoản, Ví tiền.
    *   Xử lý logic nghiệp vụ (Trừ tiền, cộng điểm, check vé).
    *   Lưu trữ tập trung (Database).
    *   Cung cấp API cho App và Terminals.

---

## 3. 🔄 Luồng Nghiệp Vụ Chính (Key User Flows)

### 3.1. Quy trình "Onboarding" (Tham gia)
Thay vì xếp hàng mua vé, người dùng có thể tự phục vụ:
1.  **Mua thẻ trắng**: Tại máy bán tự động hoặc căng tin (Thẻ chưa có dữ liệu).
2.  **Liên kết thẻ (Link Card)**:
    *   Mở App -> Chọn "Thêm thẻ".
    *   Chạm thẻ vào lưng điện thoại (NFC).
    *   App ghi danh tính User vào thẻ -> Thẻ chính thức thuộc về User.

### 3.2. Quy trình "Play" (Vui chơi)
1.  **Nạp tiền**: User nạp 500k qua App (Ví dụ MoMo).
2.  **Chạm thẻ**: User đến máy "Tàu lượn siêu tốc", chạm thẻ vào đầu đọc.
3.  **Xử lý Server**:
    *   Terminal đọc ID từ thẻ -> Gửi về Server.
    *   Server kiểm tra: "Tk còn 500k, giá vé 50k" -> ĐỦ TIỀN.
    *   Server trừ 50k -> Còn 450k -> Gửi lệnh "MỞ CỔNG".
4.  **Kết thúc**: Cổng mở, App báo ting ting "Đã thanh toán 50k cho Tàu lượn".

---

## 4. 🗄️ Cấu Trúc Dữ Liệu (Database Schema Summary)

Hệ thống cơ sở dữ liệu được thiết kế chuẩn hóa để quản lý chặt chẽ tiền tệ và hoạt động:

*   **Identity (Định danh)**:
    *   `Accounts`: Tài khoản đăng nhập (SĐT, Mật khẩu).
    *   `Users`: Thông tin hồ sơ, Ví tiền (`current_balance`), Hạng thành viên.
    *   `Cards`: Quản lý thẻ vật lý (UID, Status, User sở hữu).
*   **Commercial (Thương mại)**:
    *   `Games`: Danh mục trò chơi (Tên, Giá vé, Vị trí).
    *   `Bookings/Orders`: Đơn hàng mua vé/combo.
    *   `Tickets`: Vé điện tử (dùng cho các gói Combo mua trước).
    *   `Vouchers`: Mã giảm giá.
*   **Financial (Tài chính - Quan trọng)**:
    *   `BalanceTransactions`: Lịch sử biến động số dư (Nạp, Trừ, Hoàn). Đảm bảo tính toàn vẹn dữ liệu kế toán.
    *   `PaymentRecords`: Lịch sử nạp tiền từ cổng thanh toán ngoài.
*   **Ops (Vận hành)**:
    *   `Terminals`: Quản lý thiết bị phần cứng.
    *   `GamePlayLogs`: Log chi tiết lượt chơi (Ai? Chơi gì? Lúc nào? Tại máy nào?).

---

## 5. 🛠️ Tính Năng Quản Trị (Admin Features)

Hệ thống dành cho ban quản lý giúp vận hành trơn tru:
*   **User Management**: Tra cứu khách hàng, xử lý khiếu nại, khóa thẻ mất.
*   **Game Management**: Bật/Tắt bảo trì máy game, điều chỉnh giá vé linh hoạt (Giờ vàng, Ngày lễ).
*   **Finance Dashboard**: Báo cáo doanh thu thời gian thực, đối soát dòng tiền nạp.
*   **Promotion**: Tung ra các mã Voucher, chiến dịch khuyến mãi để kích cầu.

---

## 6. 🚀 Điểm Nhấn Công Nghệ (Technical Highlights)

1.  **NFC Connectivity**: Tận dụng tối đa giao tiếp NFC trên điện thoại để biến Smartphone thành trạm cài đặt thẻ cá nhân.
2.  **Security**:
    *   Thẻ JavaCard không lưu tiền -> Mất thẻ không mất tiền (chỉ cần khóa trên App).
    *   Mọi giao dịch đều được xác thực Server-side.
3.  **Scalability**: Mô hình dễ dàng mở rộng thêm trò chơi, thêm kios bán hàng mà không cần thay đổi kiến trúc thẻ.

---

*Tài liệu này được tổng hợp từ các thiết kế hệ thống hiện tại (Database Schema, System Overview, Admin Features) để phục vụ việc chuyển giao và tìm hiểu dự án.*
