# 🎡 Tổng Quan Hệ Thống Park Adventure (Mô Hình Ngân Hàng - Unified Balance)

Hệ thống được thiết kế theo **Mô hình Ngân Hàng (Banking Model)**, đảm bảo trải nghiệm thống nhất: **"Tiền ở đâu cũng là một"**. Dù bạn dùng Thẻ hay App, số dư là duy nhất và cập nhật tức thời.

---

## 🏗️ Kiến Trúc Hệ Thống: "Online First" (Ưu Tiên Kết Nối)

Để đạt được sự đồng nhất về tài khoản, hệ thống yêu cầu sự kết nối chặt chẽ hơn so với mô hình thuần offline.

1.  **Server (Core Banking)**: Nơi duy nhất lưu trữ "Số dư thực" (True Balance).
2.  **Mobile App**: Một giao diện để xem và tiêu tiền từ Server.
3.  **Thẻ Thông Minh (JavaCard)**: Đóng vai trò là **Token xác thực** (giống thẻ ATM) + **Ví Dự Phòng** (cho trường hợp mất mạng).
4.  **Terminal (Máy POS)**: Được kết nối Internet (Wifi/Lan) để kiểm tra số dư thực tế từ Server.

---

## 🔁 Cơ Chế "1 Nguồn Tiền" hoạt động như thế nào?

### 1. Nguyên Tắc 100% Online (Real-time Banking)
Toàn bộ hệ thống hoạt động dựa trên kết nối mạng liên tục (như thẻ ATM/Visa Debit).
*   **Thẻ JavaCard**: Chỉ chứa **ID Thành Viên** (và khóa bảo mật). Không chứa tiền.
*   **Server**: Chứa tiền.

### 2. Quy Trình Đơn Giản Hóa
1.  **Nạp tiền**: Khách nạp qua App -> Server +500k.
2.  **Tiêu tiền**:
    *   Khách quẹt thẻ -> Máy game gửi ID lên Server.
    *   Server kiểm tra số dư -> Trừ tiền -> Mở máy.
    *   App báo tin nhắn ngay lập tức.

### 3. Giải Quyết Vấn Đề
Với mô hình này, chúng ta **KHÔNG CẦN LO** về:
*   ❌ Đồng bộ dữ liệu (Sync).
*   ❌ Tiêu quá số dư (Overdraft).
*   ❌ Tiêu 2 lần (Double spending).
*   ❌ Khóa tiền hay Hạn mức offline.

### 4. ⚠️ Rủi Ro Duy Nhất: Mất Mạng = Ngừng Hoạt Động
Trong mô hình này, nếu máy game mất kết nối với Server:
*   Máy sẽ báo lỗi "Kết nối thất bại".
*   Khách **không thể chơi** cho đến khi có mạng lại.
*   *Giải pháp*: Công viên cần đầu tư hạ tầng Wifi/LAN dự phòng (Line Backup 4G) thật tốt.

---

## 🎫 5. Quy Trình Phát Hành & Kích Hoạt Thẻ (Card Onboarding)

Để giảm tải cho quầy vé, hệ thống hỗ trợ quy trình **Tự Kích Hoạt (Self-Service)**.

1.  **Mua Thẻ Trắng (White Card)**:
    *   Khách mua thẻ tại Canteen/Máy bán tự động.
    *   Đây là thẻ có chip JavaCard đã cài sẵn Applet, nhưng **chưa có thông tin người dùng** (Unlinked).
2.  **Liên Kết Thẻ (Link Card via App)**:
    *   Khách mở App -> Chọn "Thêm thẻ mới".
    *   App yêu cầu: "Vui lòng áp thẻ vào mặt sau điện thoại".
    *   **Hành động**: App ghi **User ID + Secret Keys** của người dùng hiện tại vào trong Chip thẻ thông qua NFC.
3.  **Kết Quả**:
    *   Thẻ chuyển từ trạng thái "Vô danh" -> "Thẻ của Nguyễn Văn A".
    *   Khách có thể cầm thẻ đi quẹt ngay lập tức.

-> **Ưu điểm**: Khách không cần xếp hàng khai báo thông tin. Mua thẻ như mua chai nước, về tự kích hoạt.

---

## ✅ Tóm Tắt Trải Nghiệm Người Dùng

*   **Một tài khoản duy nhất**: Không cần quan tâm ví thẻ hay ví app.
*   **Nạp là dùng**: Nạp tiền xong là ra quẹt thẻ được ngay (hoặc quét App được ngay).
*   **Báo cáo tức thì**: Quẹt thẻ xong thấy App báo trừ tiền ting ting (như SMS banking).
*   **Tự hành phục vụ**: Tự mua thẻ, tự kích hoạt, tự khóa thẻ trên App.

Đây chính xác là trải nghiệm của **Ngân hàng số hiện đại**.
