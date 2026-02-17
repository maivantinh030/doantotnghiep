# Phân tích khả thi: Xây dựng Module giao tiếp USB Token trên Android sử dụng USB Host API

## 1. Kết luận về tính khả thi
**Hoàn toàn khả thi.** Android cung cấp `android.hardware.usb` (USB Host API) cho phép ứng dụng giao tiếp trực tiếp với các thiết bị USB ngoại vi, bao gồm cả USB Token, mà không cần quyền root hay driver kernel đặc biệt, miễn là thiết bị Android hỗ trợ USB OTG (On-The-Go).

Tuy nhiên, "khả thi" không có nghĩa là "dễ dàng". Việc này yêu cầu kiến thức sấu về giao thức USB, CCID (Chip Card Interface Device) và ISO 7816 (Smart Card).

## 2. Những thách thức và khó khăn chi tiết

Dưới đây là các rào cản kỹ thuật chính bạn sẽ gặp phải khi xây dựng module này từ đầu (from scratch) bằng USB Host API:

### 2.1. Thiếu Driver CCID Native
*   **Vấn đề:** Trên Windows/macOS, hệ điều hành đã có sẵn driver CCID để nhận diện USB Token như một Smart Card Reader. Trên Android, **không có sẵn driver này**.
*   **Thách thức:** Bạn phải tự "viết driver" ở tầng ứng dụng (User Space Driver).
    *   Bạn phải sử dụng `UsbDeviceConnection` để gửi các gói tin `Control Transfer` và `Bulk Transfer` tuân theo chuẩn **USB CCID Class Specification**.
    *   Bạn phải tự quản lý các trạng thái của thiết bị (Power On, Power Off, Get Slot Status, XfrBlock...).
*   **Độ khó:** Cao. Sai sót nhỏ trong cấu trúc gói tin USB có thể khiến giao tiếp thất bại.

### 2.2. Giao thức Smart Card (ISO 7816-4)
*   **Vấn đề:** Sau khi thiết lập được giao tiếp USB (tầng Transport), bạn cần nói chuyện với chip bảo mật bên trong bằng ngôn ngữ của nó.
*   **Thách thức:** Bạn phải xây dựng module để đóng gói và giải mã các lệnh **APDU** (Application Protocol Data Unit).
    *   Cần hiểu rõ cấu trúc APDU: `CLA | INS | P1 | P2 | Lc | Data | Le`.
    *   Cần biết các lệnh đặc thù để: Chọn Applet, Verify PIN, Sign Data, Get Certificate.
*   **Độ khó:** Trung bình - Cao. Cần tài liệu kỹ thuật chính xác của loại Token đang dùng.

### 2.3. Sự đa dạng của USB Token (Vendor Specific)
*   **Vấn đề:** Mặc dù đa số tuân theo chuẩn, nhưng mỗi nhà cung cấp (Viettel, VNPT, FPT, HyperPKI...) có thể có những đặc thù riêng.
*   **Thách thức:**
    *   Một số Token yêu cầu chuỗi khởi tạo (Initialization sequence) riêng biệt trước khi nhận lệnh APDU.
    *   Middleware (PKCS#11) thường dùng trên PC là các file DLL đóng gói sẵn logic này. Trên Android, bạn không dùng được DLL đó. Bạn phải tự implement lại logic của Middleware.
    *   Cấu trúc lưu trữ Certificate/Key bên trong Token (File System) có thể khác nhau.

### 2.4. Trải nghiệm người dùng (UX) và Phần cứng
*   **Vấn đề:** Android phone là thiết bị di động, USB Token là thiết bị rời.
*   **Thách thức:**
    *   **Cáp OTG:** Bắt buộc người dùng phải có cáp chuyển đổi OTG (Type-C to USB-A). Điều này gây bất tiện.
    *   **Nguồn điện:** Token lấy nguồn từ điện thoại. Một số Token cũ tiêu thụ dòng cao có thể bị Android ngắt kết nối để tiết kiệm pin hoặc nếu điện áp sụt giảm.
    *   **Permission:** Mỗi khi cắm Token vào, Android sẽ hiện popup hỏi người dùng "Allow app to access USB device?". App phải xử lý việc xin quyền này mượt mà.

### 2.5. Bảo mật (Security)
*   **Vấn đề:** Làm việc với chữ ký số yêu cầu bảo mật cao.
*   **Thách thức:**
    *   **Mã PIN:** App phải xây dựng giao diện nhập PIN an toàn.
    *   **Ký số:** Tuyệt đối **KHÔNG ĐƯỢC** (và thường là không thể) trích xuất Private Key ra khỏi Token. Mọi thao tác ký (Sign) phải gửi hash vào Token và nhận lại chữ ký. Đảm bảo quy trình này đúng chuẩn (PKCS#1, PKCS#7) là rất quan trọng.

### 2.6. Thách thức về "Thiết bị lai" (The Composite Device/Mode Switching Problem)
Đây là một vấn đề cực kỳ đau đầu nhưng ít người để ý đến khi mới bắt đầu.
*   **Hiện tượng:** Khi bạn cắm USB Token vào máy tính mới, thường nó sẽ hiện ra như một ổ đĩa CD-ROM (Mass Storage) chứa bộ cài đặt driver. Sau khi cài driver xong, nó mới "biến hình" thành Smart Card Reader.
*   **Cơ chế:** Thiết bị USB này là **Composite Device** hoặc có tính năng **Mode Switching**. Mặc định ban đầu nó chạy ở chế độ CD-ROM (để user cài driver). Driver trên PC khi chạy sẽ gửi một lệnh đặc biệt (thường là SCSI command) xuống USB để yêu cầu nó ngắt kết nối và kết nối lại ở chế độ CCID Smart Card.
*   **Vấn đề trên Android:**
    *   Khi cắm vào Android, điện thoại sẽ nhận diện nó là... một USB Driver/CD-ROM. Android sẽ Mount nó lên để đọc file.
    *   **App của bạn sẽ KHÔNG tìm thấy giao diện CCID interface** để giao tiếp. Bạn gọi `usbManager.getDeviceList()` chỉ thấy một thiết bị Mass Storage.
*   **Giải pháp (Khó):**
    *   Bạn phải biết được "câu thần chú" (lệnh Mode Switch) của loại Token đó. Mỗi hãng dùng một lệnh khác nhau (ví dụ: `Eject` command của SCSI).
    *   Bạn phải claim interface Mass Storage, gửi lệnh SCSI Eject, chờ thiết bị disconnect và connect lại với VendorID/ProductID mới (lúc này mới là mode CCID).
    *   Công cụ `usb-modeswitch` trên Linux giải quyết việc này. Trên Android, bạn phải tự implement logic này trong code của mình.

## 3. Lộ trình triển khai khuyến nghị

Thay vì viết raw từ USB Host API, bạn nên cân nhắc các bước sau:

1.  **Tìm SDK của hãng:** Liên hệ nhà cung cấp Token xem họ có SDK cho Android không. Đây là cách nhanh nhất.
2.  **Sử dụng thư viện Open Source:**
    *   Tìm các thư viện Java/Kotlin port của **libccid** hoặc **libusb** trên Android.
    *   Tham khảo các dự án như *sc-android* (Smart Card for Android) để tái sử dụng lớp xử lý APDU/CCID.
3.  **Tự xây dựng (Hard mode):**
    *   Dùng `UsbManager` để detect thiết bị (VendorID, ProductID).
    *   Dùng `UsbDeviceConnection` để claim interface.
    *   Implement hàm `transmit()` để gửi nhận APDU qua Bulk endpoints.

## 4. Vấn đề về PKCS#11 trên Android

Bạn có thắc mắc về **PKCS#11** (Public-Key Cryptography Standards #11), tiêu chuẩn "vàng" giao tiếp với Token/SmartCard trên máy tính. Dưới đây là cách nó hoạt động (hoặc không hoạt động) trên Android:

### 4.1. Trên PC (Windows/Linux/macOS)
*   **Cơ chế:** Các ứng dụng không giao tiếp trực tiếp với Token. Thay vào đó, chúng gọi hàm từ một file thư viện động (`.dll` trên Windows, `.so` trên Linux) do nhà sản xuất Token cung cấp.
*   **PKCS#11 API:** Đây là giao diện chuẩn chung. Mọi Token đều cung cấp các hàm giống nhau như `C_Login`, `C_Sign`, `C_GetAttributeValue`...
*   **Lợi ích:** Ứng dụng chỉ cần viết code theo chuẩn PKCS#11 là có thể chạy với nhiều loại Token khác nhau, chỉ cần đổi file driver (.dll).

### 4.2. Trên Android
*   **Thực tế:** Android **KHÔNG** hỗ trợ nạp các file driver PKCS#11 (`.dll`/`.so`) một cách tự nhiên như PC.
    *   **Kiến trúc khác biệt:** Android dùng kiến trúc ARM, còn driver Windows thường là x86/x64. Bạn không thể mang file `.dll` từ máy tính sang chạy trên điện thoại.
    *   **Sandbox:** Ứng dụng Android chạy trong sandbox bị giới hạn quyền truy cập phần cứng và file hệ thống.
*   **Giải pháp thay thế:**
    1.  **Vendor SDK (Phổ biến nhất):** Các hãng như VNPT, Viettel... thường viết lại một thư viện riêng cho Android (file `.jar` hoặc `.aar`). Thư viện này bên trong sẽ tự xử lý giao tiếp USB CCID (như mục 2.1 đã nói) và cung cấp các hàm Java dễ dùng (`signPDF()`, `loginToken()`). Về mặt logic, nó đóng vai trò giống như PKCS#11 driver, nhưng giao diện lập trình là Java, không phải C API của PKCS#11.
    2.  **Porting thư viện C/C++ (Khó):** Nếu bạn có mã nguồn driver (Open Source như OpenSC), bạn có thể compile lại nó ra file `.so` cho Android (dùng NDK). Sau đó dùng JNI để gọi từ Java. Tuy nhiên, việc cấp quyền truy cập USB từ Java xuống tầng C (thông qua file descriptor) rất phức tạp.
    3.  **Tự viết lớp giả lập PKCS#11:** Nếu bạn tự xây dựng module USB Host, bạn nên thiết kế các Class/Interface trong code của mình mô phỏng lại các hàm quan trọng của PKCS#11 (Session management, Login, Sign, Logout) để code business logic dễ dàng hơn.

### Tóm lại
Trên Android, bạn **không thể** copy-paste file driver PKCS#11 từ máy tính sang. Bạn phải dùng SDK riêng của hãng cho Android, hoặc tự viết code để giao tiếp ở mức thấp (CCID/APDU) như đã phân tích bên trên.

## 5. Tóm tắt
Việc này **KHẢ THI** nhưng **KHÓ** và tốn nhiều công sức để ổn định (stable) cho nhiều loại Token khác nhau. Nếu chỉ target 1 loại Token cụ thể, khối lượng việc sẽ giảm đi đáng kể.
