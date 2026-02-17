# 📱 Các Tính Năng Dự Án: Ứng Dụng Park Adventure

## 📝 Tổng Quan
**Park Adventure** là một ứng dụng di động toàn diện được thiết kế để quản lý vé thành viên công viên giải trí, hỗ trợ mua vé và nâng cao trải nghiệm của khách tham quan. Nó đóng vai trò như một người bạn đồng hành kỹ thuật số, xử lý mọi thứ từ quyền truy cập vào cổng đến thanh toán và tích điểm thưởng.

---

## 🚀 Các Tính Năng Chính

### 1. Xác Thực & Bảo Mật
*   **Đăng nhập/Đăng ký an toàn**: Hỗ trợ xác thực bằng số điện thoại và mật khẩu.
*   **Bảo mật thẻ**: Người dùng có thể chủ động **Khóa/Mở khóa** thẻ kỹ thuật số của mình để ngăn chặn việc sử dụng trái phép.
*   **Quyền riêng tư**: Chế độ ẩn/hiện cho các dữ liệu nhạy cảm như số dư tài khoản và mã số thẻ.

### 2. Quản Lý Người Dùng & Thành Viên
*   **Thẻ Thành Viên Kỹ Thuật Số**: Hiển thị trực quan thẻ thành viên (Các hạng Đồng, Bạc, Vàng, Bạch Kim).
*   **Truy cập bằng mã QR**: Tích hợp mã QR để quét nhanh tại cổng và sử dụng các dịch vụ.
*   **Quản lý hồ sơ**: Cho phép chỉnh sửa thông tin cá nhân đầy đủ, bao gồm quản lý mật khẩu.
*   **Cài đặt**: Các cài đặt toàn ứng dụng bao gồm Ngôn ngữ (Hỗ trợ tiếng Việt), Chế độ Tối/Sáng, và Tùy chọn thông báo.

### 3. Ví & Tài Chính
*   **Hệ thống Nạp tiền**: Tích hợp nhiều cổng thanh toán (MoMo, VNPay, Ngân hàng) để nạp tiền vào tài khoản.
*   **Theo dõi số dư**: Xem số dư hiện tại và điểm thưởng tích lũy theo thời gian thực.
*   **Lịch sử giao dịch**: Nhật ký chi tiết của tất cả các hoạt động tài chính:
    *   **Lịch sử thanh toán**: Ghi lại các khoản nạp tiền và trạng thái hoàn tiền.
    *   **Lịch sử sử dụng**: Ghi lại các lượt chơi game, chi phí và số tiền tiết kiệm được.

### 4. Cửa Hàng Trò Chơi & Dịch Vụ
*   **Danh mục trò chơi**: Danh sách các trò chơi và điểm tham quan có thể duyệt qua với khả năng tìm kiếm và lọc.
*   **Chi tiết trò chơi**: Thông tin phong phú cho từng địa điểm bao gồm:
    *   Yêu cầu về độ tuổi và chiều cao.
    *   Mức độ mạo hiểm.
    *   Vị trí (Trong nhà/Ngoài trời).
    *   Giá cả và các giảm giá đặc biệt.
*   **Giỏ hàng & Thanh toán**: Thêm vé trò chơi vào giỏ, quản lý số lượng và thanh toán liền mạch bằng ví trong ứng dụng.

### 5. Phần Thưởng & Khuyến Mãi
*   **Hệ thống Voucher**:
    *   **Kho Voucher**: Duyệt các voucher và mã giảm giá có sẵn.
    *   **Ví Voucher**: Quản lý các voucher đã lưu và áp dụng chúng khi thanh toán để tiết kiệm.
*   **Chương trình giới thiệu**: Người dùng có thể chia sẻ mã giới thiệu duy nhất.
    *   **Người giới thiệu**: Nhận voucher hoặc tín dụng.
    *   **Người được mời**: Nhận lượt chơi miễn phí hoặc tiền thưởng cho người mới.

### 6. Giao Tiếp & Hỗ Trợ
*   **Trung tâm thông báo**: Nơi cập nhật các thông tin về khuyến mãi, trạng thái giao dịch và cảnh báo hệ thống.
*   **Chat hỗ trợ trực tuyến**: Kênh trực tiếp để giao tiếp với nhân viên hỗ trợ khách hàng để được giải đáp thắc mắc.

---

## 🛠️ Công Nghệ Sử Dụng

*   **Ngôn ngữ**: Kotlin
*   **UI Framework**: Jetpack Compose (Giao diện hiện đại, khai báo)
*   **Kiến trúc**: MVVM (Model-View-ViewModel)
*   **Điều hướng**: Jetpack Navigation Compose
*   **Quản lý trạng thái**: Kotlin Flows & State
