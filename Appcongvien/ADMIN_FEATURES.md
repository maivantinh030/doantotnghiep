# 👮 Các Tính Năng Quản Trị (Admin Features) - Park Adventure

Dựa trên các tính năng của ứng dụng người dùng, hệ thống quản trị (Admin/Backend Dashboard) cần có các chức năng sau để vận hành hệ thống:

## 1. 👥 Quản Lý Người Dùng (User Management)
Admin cần kiểm soát danh sách thành viên ra vào công viên.
*   **Danh sách thành viên**: Xem danh sách tất cả người dùng, tìm kiếm theo Tên/SĐT.
*   **Chi tiết hồ sơ**: Xem lịch sử giao dịch, số dư, hạng thành viên của từng người.
*   **Điều chỉnh số dư**: Cấp tiền/điểm thưởng thủ công (để đền bù hoặc thưởng sự kiện).
*   **Khóa/Mở khóa tài khoản**: Chặn người dùng vi phạm quy định.
*   **Cập nhật hạng**: Nâng/Hạ hạng thành viên (Vàng, Bạc...) thủ công nếu cần.

## 2. 🎡 Quản Lý Trò Chơi & Dịch Vụ (Game Management)
Quản lý các điểm vui chơi hiển thị trên app.
*   **CRUD Trò chơi**: Thêm trò chơi mới, Sửa thông tin (ảnh, mô tả, vị trí), Xóa/Ẩn trò chơi.
*   **Thiết lập giá**: Cập nhật giá vé, thiết lập giảm giá theo khung giờ hoặc sự kiện.
*   **Trạng thái bảo trì**: Đánh dấu trò chơi đang bảo trì để người dùng không mua vé được.
*   **Quản lý lượt chơi**: Kiểm soát sức chứa tối đa của trò chơi (nếu cần).

## 3. 🎫 Quản Lý Voucher & Khuyến Mãi (Promotions)
Đây là công cụ chính để Marketing thu hút khách.
*   **Tạo Voucher mới**:
    *   Thiết lập mức giảm (theo % hoặc số tiền cố định).
    *   Thiết lập điều kiện (đơn tối thiểu, áp dụng cho game nào).
    *   Thiết lập số lượng phát hành và thời gian hết hạn.
*   **Quản lý chiến dịch**: Kích hoạt hoặc dừng sớm các đợt khuyến mãi.
*   **Thống kê hiệu quả**: Xem voucher nào được dùng nhiều nhất.

## 4. 💰 Quản Lý Tài Chính & Doanh Thu (Finance)
Theo dõi dòng tiền vào ra.
*   **Dashboard Doanh thu**: Xem báo cáo doanh thu theo Ngày/Tuần/Tháng.
*   **Lịch sử nạp tiền**: Kiểm tra các giao dịch nạp tiền từ MoMo/VNPay/Bank để đối soát.
*   **Xử lý hoàn tiền (Refund)**: Duyệt hoặc từ chối yêu cầu hoàn tiền từ người dùng.
*   **Thống kê nguồn tiền**: Biết được kênh thanh toán nào phổ biến nhất.

## 5. 📢 Hệ Thống Thông Báo & CMS (Communication)
Gửi tin tức đến người dùng.
*   **Gửi thông báo đẩy (Push Notification)**: Gửi tin nhắn hàng loạt ("Chuc mung nam moi", "Khuyen mai soc") tới tất cả hoặc một nhóm người dùng.
*   **Quản lý Banner**: Thay đổi banner quảng cáo ở trang chủ ứng dụng.

## 6. 🎧 Hỗ Trợ Khách Hàng (Support Center)
*   **Live Chat Agent**: Giao diện cho nhân viên CSKH chat trực tiếp với người dùng (như màn hình `SupportChatScreen` của user).
*   **Xử lý vé hỗ trợ**: Theo dõi các vấn đề người dùng báo cáo.

## 7. 📊 Thống Kê & Báo Cáo (Analytics)
*   **Lượng khách**: Thống kê số người đến công viên theo khung giờ.
*   **Trò chơi hot**: Top các trò chơi được mua vé nhiều nhất.
*   **Tỷ lệ quay lại**: Bao nhiêu khách hàng quay lại lần 2.

---
**Tóm lại**: Admin sẽ đóng vai trò là "người điều hành" đằng sau, đảm bảo dữ liệu trên app người dùng luôn chính xác và xử lý các vấn đề phát sinh.
