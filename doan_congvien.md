# [TÊN TRƯỜNG ĐẠI HỌC]
## [TÊN KHOA]

---

# ĐỒ ÁN TỐT NGHIỆP

## NGHIÊN CỨU PHÁT TRIỂN HỆ THỐNG QUẢN LÝ VÉ TRONG CÔNG VIÊN SỬ DỤNG SMART CARD

**Học phần:** Đồ án tốt nghiệp
**Giảng viên hướng dẫn:** [Tên giảng viên]
**Sinh viên thực hiện:** [Họ tên] – [Mã sinh viên]

**HÀ NỘI – 2025**

---

## MỤC LỤC

- DANH MỤC HÌNH ẢNH
- DANH MỤC BẢNG BIỂU
- DANH MỤC TỪ VIẾT TẮT
- **CHƯƠNG 1. GIỚI THIỆU TỔNG QUAN**
  - 1.1 Khảo sát và phân tích
    - 1.1.1 Thực trạng hiện tại
    - 1.1.2 Tính cấp thiết của vấn đề
    - 1.1.3 Các ưu nhược điểm của giải pháp hiện có
  - 1.2 Bài toán đặt ra
  - 1.3 Đối tượng sử dụng
    - 1.3.1 Khách tham quan
    - 1.3.2 Nhân viên vận hành và quản trị viên
  - 1.4 Các công nghệ sử dụng
    - 1.4.1 Front-End
    - 1.4.2 Back-End
    - 1.4.3 Cơ sở dữ liệu
    - 1.4.4 Công nghệ thẻ thông minh
    - 1.4.5 Các công nghệ khác
  - 1.5 Cài đặt môi trường
    - 1.5.1 Môi trường ứng dụng Android
    - 1.5.2 Môi trường ứng dụng Desktop (SmardCard Terminal & Admin)
    - 1.5.3 Môi trường Back-End
    - 1.5.4 Môi trường Cơ sở dữ liệu
    - 1.5.5 Công cụ hỗ trợ khác
  - 1.6 Các thách thức khi phát triển ứng dụng
  - 1.7 Tổng kết chương
- **CHƯƠNG 2. PHÂN TÍCH THIẾT KẾ HỆ THỐNG**
  - 2.1 Xác định actor
  - 2.2 Biểu đồ Use Case tổng quát
  - 2.3 Các Use Case chi tiết
  - 2.4 Biểu đồ trình tự
  - 2.5 Biểu đồ lớp
  - 2.6 Thiết kế cơ sở dữ liệu
  - 2.7 Tổng kết chương
- **CHƯƠNG 3. THỰC NGHIỆM**
  - 3.1 Mô tả cách cài đặt
  - 3.2 Cách làm việc
- KẾT LUẬN
- TÀI LIỆU THAM KHẢO

---

## DANH MỤC HÌNH ẢNH

- Hình 1.1 Ảnh hàng dài khách xếp hàng chờ mua vé tại cổng công viên
- Hình 1.2 Ảnh tình trạng quá tải tại quầy bán vé giờ cao điểm
- Hình 1.3 Ảnh gian lận vé gây thất thu doanh nghiệp
- Hình 2.1 Kiến trúc tổng thể hệ thống
- Hình 2.2 Biểu đồ Use Case tổng quát
- Hình 2.3 Biểu đồ trình tự đăng nhập
- Hình 2.4 Biểu đồ trình tự tạo thẻ mới tại quầy
- Hình 2.5 Biểu đồ trình tự quẹt thẻ chơi game (RSA Challenge-Response)
- Hình 2.6 Biểu đồ trình tự nạp tiền qua app và tại quầy
- Hình 2.7 Biểu đồ trình tự trả thẻ và tái sử dụng
- Hình 2.8 Biểu đồ trình tự yêu cầu tạo thẻ qua app
- Hình 2.9 Biểu đồ lớp tổng quát
- Hình 2.10 Sơ đồ cơ sở dữ liệu
- Hình 3.1 Màn hình đăng nhập ứng dụng Android
- Hình 3.2 Màn hình trang chủ (số dư, thẻ, thông báo)
- Hình 3.3 Màn hình quản lý thẻ Smart Card
- Hình 3.4 Màn hình yêu cầu tạo thẻ
- Hình 3.5 Màn hình nạp tiền ví điện tử
- Hình 3.6 Màn hình lịch sử giao dịch (lọc theo thẻ)
- Hình 3.7 Thông báo realtime khi quẹt thẻ chơi game
- Hình 3.8 SmardCard Terminal – Màn hình tạo thẻ mới
- Hình 3.9 SmardCard Terminal – Màn hình ghi đè thẻ cũ
- Hình 3.10 SmardCard Terminal – Màn hình chơi game (soát vé)
- Hình 3.11 SmardCard Terminal – Màn hình duyệt yêu cầu thẻ
- Hình 3.12 Ứng dụng Admin Desktop – Dashboard tổng quan
- Hình 3.13 Ứng dụng Admin Desktop – Quản lý người dùng & nạp tiền tại quầy

---

## DANH MỤC BẢNG BIỂU

- Bảng 1.1 So sánh các giải pháp quản lý vé công viên hiện có
- Bảng 1.2 Vai trò các thành phần trong hệ thống
- Bảng 2.1 Danh sách actor và vai trò
- Bảng 2.2 Đặc tả Use Case Đăng nhập
- Bảng 2.3 Đặc tả Use Case Tạo thẻ mới tại quầy
- Bảng 2.4 Đặc tả Use Case Quẹt thẻ chơi game
- Bảng 2.5 Đặc tả Use Case Nạp tiền
- Bảng 2.6 Đặc tả Use Case Trả thẻ và tái sử dụng
- Bảng 2.7 Đặc tả Use Case Yêu cầu tạo thẻ qua app
- Bảng 2.8 Mô tả các lệnh APDU của Java Card Applet (ParkCard)
- Bảng 2.9 Mô tả các bảng cơ sở dữ liệu

---

## DANH MỤC TỪ VIẾT TẮT

| Từ viết tắt | Giải thích |
|-------------|-----------|
| AES | Advanced Encryption Standard – Tiêu chuẩn mã hóa nâng cao |
| APDU | Application Protocol Data Unit – Đơn vị dữ liệu giao thức ứng dụng (ISO 7816) |
| AID | Application Identifier – Mã định danh ứng dụng trên Java Card |
| API | Application Programming Interface – Giao diện lập trình ứng dụng |
| CSDL | Cơ sở dữ liệu |
| EEPROM | Electrically Erasable Programmable Read-Only Memory – Bộ nhớ lưu trữ trên thẻ |
| HCE | Host Card Emulation – Giả lập thẻ NFC trên thiết bị Android |
| IDE | Integrated Development Environment – Môi trường phát triển tích hợp |
| ISO | International Organization for Standardization – Tổ chức tiêu chuẩn hóa quốc tế |
| JWT | JSON Web Token – Chuẩn xác thực dựa trên token |
| NFC | Near Field Communication – Giao tiếp tầm ngắn |
| PBKDF2 | Password-Based Key Derivation Function 2 – Hàm dẫn xuất khóa từ mật khẩu |
| PIN | Personal Identification Number – Mã xác thực cá nhân |
| RSA | Rivest–Shamir–Adleman – Thuật toán mã hóa bất đối xứng |
| REST | Representational State Transfer – Kiểu kiến trúc dịch vụ web |
| SDK | Software Development Kit – Bộ công cụ phát triển phần mềm |
| UID | Unique Identifier – Mã định danh duy nhất |
| UI/UX | User Interface / User Experience – Giao diện / Trải nghiệm người dùng |

---

# CHƯƠNG 1. GIỚI THIỆU TỔNG QUAN

## 1.1 Khảo sát và phân tích

### 1.1.1 Thực trạng hiện tại

Trong những năm gần đây, ngành công nghiệp giải trí và vui chơi tại Việt Nam phát triển mạnh mẽ với sự xuất hiện ngày càng nhiều của các công viên giải trí quy mô lớn. Tuy nhiên, phần lớn các cơ sở này vẫn đang áp dụng hệ thống quản lý vé và thu phí thủ công, dẫn đến nhiều bất cập nghiêm trọng trong vận hành.

**Thực trạng tại các công viên giải trí:**

Hầu hết các khu vui chơi tại Việt Nam vẫn sử dụng hình thức bán vé giấy hoặc token tại quầy. Khách tham quan phải xếp hàng chờ đợi để mua vé, đặc biệt vào các dịp lễ, Tết hay cuối tuần, tình trạng ùn tắc tại các điểm bán vé trở nên rất phổ biến. Mỗi trò chơi bên trong công viên lại có quầy bán vé riêng, khiến khách phải di chuyển và thanh toán nhiều lần bằng tiền mặt.

*[Hình 1.1 Ảnh hàng dài khách xếp hàng chờ mua vé tại cổng công viên]*

Vé giấy và token kim loại/nhựa dễ bị thất lạc, làm giả hoặc tái sử dụng bất hợp pháp, gây thất thu đáng kể cho đơn vị kinh doanh. Một số khu vui chơi đã thử nghiệm thẻ từ (magnetic stripe), nhưng công nghệ này dễ bị hỏng, dễ bị sao chép và không tích hợp được với hệ thống quản lý hiện đại.

*[Hình 1.2 Ảnh tình trạng quá tải tại quầy bán vé giờ cao điểm]*

Về phía ban quản lý, việc theo dõi doanh thu, lượng khách và tình trạng hoạt động từng trò chơi đòi hỏi nhiều nhân lực thống kê thủ công, thiếu minh bạch và dễ xảy ra sai sót. Khi khách hết tiền trong thẻ hay quên không mua thêm vé cho một trò chơi cụ thể, nhân viên phải xử lý thủ công, gây chậm trễ và ảnh hưởng trải nghiệm.

*[Hình 1.3 Ảnh gian lận vé gây thất thu doanh nghiệp]*

**Các vấn đề cụ thể mà khách tham quan gặp phải:**

- Phải xếp hàng mua vé tại nhiều quầy khác nhau cho từng trò chơi
- Không thể nạp tiền từ xa qua điện thoại; phải mang tiền mặt
- Không có cách nào theo dõi số dư hay lịch sử chi tiêu trong ngày
- Khi mất thẻ/vé, không có cơ chế khóa hay bảo vệ số dư còn lại
- Không nhận được thông báo khi bị trừ tiền, khó kiểm soát chi tiêu của trẻ em

**Các vấn đề cụ thể mà ban quản lý công viên gặp phải:**

- Quản lý doanh thu thủ công, thiếu chính xác, dễ thất thu
- Không có dữ liệu thời gian thực về lượng khách và hoạt động từng trò chơi
- Tốn nhiều nhân lực tại các quầy bán vé và kiểm soát vé
- Không có cơ chế chống gian lận hiệu quả (thẻ giả, tái sử dụng vé cũ)
- Khó triển khai chương trình khuyến mãi hay chăm sóc khách hàng thành viên

### 1.1.2 Tính cấp thiết của vấn đề

Trên thế giới, mô hình **"cashless park"** (công viên không dùng tiền mặt) đang trở thành tiêu chuẩn mới tại các quốc gia phát triển như Nhật Bản, Hàn Quốc, Singapore và các công viên lớn như Disney, Universal Studios. Tại đây, khách tham quan chỉ cần một chiếc thẻ thông minh (Smart Card) để thực hiện toàn bộ hoạt động trong ngày: nạp tiền, thanh toán tự động tại từng điểm trò chơi bằng công nghệ NFC, theo dõi số dư qua ứng dụng điện thoại. Điều này mang lại trải nghiệm liền mạch, hiện đại và giúp công viên tối ưu vận hành đáng kể.

Điểm then chốt trong các mô hình tiên tiến này là kiến trúc **"balance on server"**: số dư ví điện tử được lưu trữ và quản lý tập trung trên máy chủ, thẻ vật lý chỉ đóng vai trò định danh khách hàng. Khi khách quẹt thẻ tại thiết bị đọc, hệ thống tra cứu và trừ tiền trực tiếp trên server, đảm bảo tính nhất quán dữ liệu và khả năng khóa thẻ tức thời khi cần.

Tại Việt Nam, công cuộc chuyển đổi số đang diễn ra mạnh mẽ nhưng ngành giải trí công viên vẫn còn rất nhiều dư địa để số hóa. Việc nghiên cứu và phát triển một hệ thống quản lý vé dựa trên **Java Card** (thẻ thông minh tiêu chuẩn công nghiệp) tích hợp với ứng dụng di động và backend hiện đại là hướng đi hoàn toàn khả thi và cấp thiết.

### 1.1.3 Các ưu nhược điểm của giải pháp hiện có

**Vé giấy / Token vật lý:**

*Ưu điểm:* Triển khai đơn giản, chi phí thấp, không phụ thuộc công nghệ.

*Nhược điểm:* Dễ làm giả, không thể khóa khi mất, không hỗ trợ theo dõi từ xa, tốn nhiều nhân lực vận hành.

**Thẻ từ (Magnetic Stripe):**

*Ưu điểm:* Tái sử dụng được, quen thuộc với người dùng.

*Nhược điểm:* Công nghệ lỗi thời, dễ hỏng từ tính, cực kỳ dễ bị sao chép (clone), không tích hợp với smartphone.

**QR Code / Mã vạch điện tử:**

*Ưu điểm:* Mua vé online được, không cần thẻ vật lý.

*Nhược điểm:* Chỉ giải quyết cổng vào, không quản lý được từng trò chơi bên trong; dễ chụp màn hình chia sẻ lậu; không hoạt động khi điện thoại hết pin.

**So sánh tổng thể:**

| Tiêu chí | Vé giấy / Token | Thẻ từ | QR Code | **Hệ thống đề xuất (Java Card)** |
|----------|----------------|--------|---------|----------------------------------|
| Nạp tiền online qua app | Không | Không | Không | **Có** |
| Balance quản lý tập trung | Không | Không | Không | **Có (trên server)** |
| Soát vé NFC tại trò chơi | Không | Không | Không | **Có** |
| Thông báo realtime khi trừ tiền | Không | Không | Không | **Có** |
| Chống thẻ giả/clone | Kém | Kém | Trung bình | **Tốt (RSA Challenge-Response)** |
| Khóa thẻ từ xa khi mất | Không | Không | Không | **Có** |
| Tái sử dụng thẻ | Không | Hạn chế | Không | **Có (ghi đè thông tin)** |
| Quản lý admin real-time | Không | Hạn chế | Hạn chế | **Có** |

---

## 1.2 Bài toán đặt ra

Từ những phân tích trên, đề tài đặt ra mục tiêu xây dựng một **hệ thống quản lý vé toàn diện cho công viên giải trí sử dụng Java Card và công nghệ NFC**, với kiến trúc gồm bốn thành phần phối hợp với nhau:

```
┌─────────────────────────────────────────────────────────────────┐
│                         Hệ Thống                                │
│                                                                 │
│  [Java Card]  ←APDU→  [SmardCard Terminal]  ←API→  [Backend]  │
│     (thẻ)              (reader Kotlin Desktop)     (Ktor)      │
│                                                        ↕       │
│  [AppCongVien Android]  ←────── Notifications ────────┘       │
│     (app khách hàng)                                           │
│                                                        ↕       │
│  [appdesktop Admin]     ←────── Admin API ─────────────┘      │
│     (quản lý hệ thống)                                         │
└─────────────────────────────────────────────────────────────────┘
```

**Bảng vai trò các thành phần:**

| Thành phần | Vai trò |
|-----------|---------|
| **ParkCard** (Java Card Applet) | Lưu customerID, tên, SĐT trên thẻ vật lý. Thẻ là **định danh**, không lưu balance |
| **SmardCard** (Terminal Desktop) | Đọc/ghi thẻ qua APDU, gọi Backend API để trừ tiền và ghi log giao dịch |
| **Backend** (Ktor) | Nguồn truth cho balance, giao dịch, thông báo |
| **AppCongVien** (Android) | Hiển thị balance, nhận notification realtime, yêu cầu tạo thẻ, nạp tiền qua app |
| **appdesktop** (Admin) | Quản lý user, xem thống kê, nạp tiền tại quầy cho user |

**Các yêu cầu chức năng chính:**

- **Quản lý thẻ Java Card:** Tạo thẻ mới tại quầy (ghi APDU lên thẻ), tái sử dụng thẻ cũ bằng cách ghi đè thông tin khách mới; khách có thể yêu cầu tạo thẻ qua app và nhận thẻ tại quầy
- **Ví điện tử trên server:** Khách nạp tiền qua app (MoMo, ngân hàng) hoặc tại quầy bằng tiền mặt; balance luôn cập nhật tức thì
- **Soát vé tự động:** Khách quẹt thẻ Java Card vào thiết bị đọc tại từng trò chơi; hệ thống trừ tiền tự động, không cần mua vé riêng từng trò
- **Thông báo realtime:** Mỗi lần quẹt thẻ, khách nhận ngay thông báo trên điện thoại: tên trò chơi, số tiền bị trừ, số dư còn lại
- **Bảo mật RSA:** Mỗi thẻ sinh một cặp khóa RSA riêng, chống clone thẻ bằng cơ chế Challenge-Response
- **Hỗ trợ gia đình nhiều thẻ:** Một tài khoản có thể liên kết nhiều thẻ (mỗi thành viên gia đình một thẻ), dùng chung số dư nhưng theo dõi chi tiêu theo từng thẻ
- **Quản lý trực quan cho admin:** Dashboard thống kê doanh thu, quản lý người dùng và thẻ, nạp tiền hộ tại quầy

---

## 1.3 Đối tượng sử dụng

### 1.3.1 Khách tham quan

Đối tượng sử dụng ứng dụng Android **AppCongVien** bao gồm:

- **Khách cá nhân:** Người đến vui chơi tại công viên, muốn trải nghiệm nhanh chóng, không phải xếp hàng mua vé tại từng trò chơi.
- **Gia đình có trẻ em:** Nhóm chiếm tỷ lệ lớn. Phụ huynh nạp tiền một lần vào ví chung, mỗi thành viên có thẻ riêng; phụ huynh theo dõi được đứa nào tiêu bao nhiêu thông qua lịch sử giao dịch theo thẻ.
- **Khách chưa có smartphone:** Vẫn có thể sử dụng hệ thống bình thường thông qua thẻ Java Card vật lý, không bắt buộc phải cài app.

### 1.3.2 Nhân viên vận hành và quản trị viên

- **Nhân viên tại quầy (SmardCard Terminal):** Tạo thẻ mới cho khách, ghi đè thẻ cũ khi khách trả lại, nạp tiền mặt cho khách, duyệt yêu cầu tạo thẻ từ app.
- **Nhân viên vận hành trò chơi (SmardCard Terminal – chế độ Game):** Đặt thiết bị đọc thẻ tại cổng trò chơi. Thiết bị tự động đọc thẻ, gọi API trừ tiền và hiển thị kết quả.
- **Quản trị viên hệ thống (appdesktop):** Xem Dashboard tổng quan, quản lý toàn bộ người dùng và tài khoản, xem báo cáo tài chính, gửi thông báo đến khách hàng.

---

## 1.4 Các công nghệ sử dụng

### 1.4.1 Front-End

**Ứng dụng Android – Kotlin với Jetpack Compose**

Jetpack Compose là framework phát triển giao diện người dùng hiện đại theo phương pháp khai báo (declarative UI), giúp xây dựng giao diện nhanh, linh hoạt và dễ bảo trì. Ứng dụng Android **AppCongVien** phục vụ khách tham quan với các chức năng: xem số dư, nhận thông báo realtime, nạp tiền, yêu cầu tạo thẻ và xem lịch sử giao dịch.

**Ứng dụng Desktop – Kotlin Compose Multiplatform**

Ứng dụng **SmardCard Terminal** (dành cho nhân viên) và **appdesktop** (dành cho admin) được xây dựng bằng Kotlin Compose Multiplatform cho nền tảng JVM/Desktop. Hai ứng dụng Desktop này chia sẻ chung nền tảng công nghệ nhưng phục vụ hai vai trò khác nhau: một cho vận hành tại quầy/trò chơi, một cho quản trị hệ thống.

### 1.4.2 Back-End

**Kotlin với Ktor**

Ktor là framework backend nhẹ, linh hoạt, được phát triển bởi JetBrains, chạy trên JVM. Backend cung cấp REST API phục vụ đồng thời cho app Android, SmardCard Terminal và appdesktop Admin. Ktor hỗ trợ WebSocket cho tính năng thông báo realtime, và tích hợp JWT để phân quyền các loại client (user, terminal, admin).

*Lợi ích của Ktor:*
- Cú pháp đơn giản, dễ mở rộng thêm route và middleware
- Tích hợp sẵn JWT authentication, content negotiation (JSON)
- Hỗ trợ coroutines Kotlin cho xử lý bất đồng bộ hiệu quả

### 1.4.3 Cơ sở dữ liệu

**MySQL với Exposed ORM**

MySQL là hệ quản trị cơ sở dữ liệu quan hệ mã nguồn mở phổ biến, ổn định, phù hợp cho dữ liệu giao dịch tài chính đòi hỏi tính toàn vẹn cao (ACID). Exposed ORM (thư viện Kotlin của JetBrains) cho phép tương tác với MySQL theo kiểu type-safe, giảm nguy cơ lỗi SQL injection.

Backend là **nguồn sự thật duy nhất (single source of truth)** cho toàn bộ số dư và giao dịch của khách hàng. Thẻ Java Card không lưu số dư mà chỉ lưu định danh (customerID) để tra cứu trên server.

### 1.4.4 Công nghệ thẻ thông minh

**Java Card và giao thức APDU (ISO 7816-4)**

Java Card là nền tảng phần mềm tiêu chuẩn công nghiệp cho thẻ thông minh tiếp xúc (contact smart card) và không tiếp xúc (contactless NFC). Ứng dụng trên thẻ (gọi là **Applet**) được phát triển bằng Java Card API và nạp lên thẻ vật lý.

Giao tiếp giữa thiết bị đọc (SmardCard Terminal) và thẻ thực hiện qua giao thức **APDU (Application Protocol Data Unit)** theo chuẩn ISO/IEC 7816-4. Mỗi lệnh APDU gồm header (CLA, INS, P1, P2) và dữ liệu, thẻ phản hồi bằng dữ liệu kèm mã trạng thái (SW1 SW2).

**ParkCard Applet** – Applet tùy chỉnh được triển khai trên thẻ Java Card, bao gồm các chức năng:
- Lưu và đọc customerID (định danh khách hàng)
- Lưu thông tin cá nhân (tên, số điện thoại) được mã hóa AES-128
- Quản lý PIN người dùng và PIN admin (PBKDF2 + AES)
- Sinh cặp khóa RSA-1024 và thực hiện ký số (Challenge-Response) để chống clone thẻ

### 1.4.5 Các công nghệ khác

| Công nghệ | Mục đích |
|-----------|----------|
| **JWT** | Xác thực và phân quyền (User / Terminal / Admin) |
| **BCrypt** | Băm mật khẩu tài khoản người dùng |
| **RSA-1024** | Ký số trên thẻ Java Card, xác thực thẻ thật tại server |
| **AES-128-CBC** | Mã hóa dữ liệu lưu trên EEPROM thẻ |
| **PBKDF2** | Dẫn xuất khóa mã hóa từ PIN người dùng |
| **Retrofit** | HTTP Client trên Android |
| **Ktor HttpClient** | HTTP Client trên Desktop |
| **kotlinx.serialization** | Xử lý JSON |
| **WebSocket** | Thông báo realtime (push notification) |
| **Kotlin Coroutines + Flow** | Xử lý bất đồng bộ, reactive state |

---

## 1.5 Cài đặt môi trường

### 1.5.1 Môi trường ứng dụng Android

**Công cụ:** Android Studio (phiên bản Electric Eel trở lên)

Mô tả: Android Studio là IDE chính thức của Google cho phát triển Android, được tối ưu hóa cho Jetpack Compose. Bao gồm Android SDK, trình giả lập và các công cụ debug/profiling.

*Yêu cầu thiết bị chạy app:*
- Android 5.0 (API 21) trở lên
- Hỗ trợ NFC (để nhận thông báo và tích hợp với thẻ trong tương lai)
- Kết nối internet (LAN hoặc WiFi cùng mạng với server)

*Cài đặt:* Tải Android Studio từ trang chủ developer.android.com, cài Kotlin Plugin và Jetpack Compose Plugin (đã tích hợp sẵn từ phiên bản mới).

### 1.5.2 Môi trường ứng dụng Desktop (SmardCard Terminal & Admin)

**Công cụ:** IntelliJ IDEA (Community hoặc Ultimate Edition)

Mô tả: IntelliJ IDEA là IDE mạnh mẽ của JetBrains, hỗ trợ Kotlin Multiplatform và Compose Desktop.

*Yêu cầu thiết bị SmardCard Terminal:*
- Máy tính Windows/Linux/macOS
- Có cổng USB hoặc PC/SC card reader để kết nối thiết bị đọc thẻ Java Card (ví dụ: ACR122U, ACR1252U)
- Driver PC/SC được cài đặt đúng cho card reader

*Thư viện Java Card Reader:*
- Sử dụng **javax.smartcardio** (có sẵn trong JDK) để giao tiếp với card reader qua PC/SC
- Kết nối card reader → gửi/nhận APDU đến Java Card

### 1.5.3 Môi trường Back-End

**Công cụ:** IntelliJ IDEA

*Yêu cầu:* JDK 17 trở lên, Gradle build tool.

*Cấu hình:* Tạo file `application.conf` với thông tin:
```
database.url = "jdbc:mysql://localhost:3306/park_db"
database.user = "root"
database.password = "your_password"
jwt.secret = "your_jwt_secret_key"
server.port = 8080
```

*Kiểm tra:* Sau khi khởi động, gọi `GET /api/auth/health` → trả về `{ "status": "ok" }`.

### 1.5.4 Môi trường Cơ sở dữ liệu

**Công cụ:** MySQL 8.x + MySQL Workbench

MySQL Workbench là công cụ GUI quản lý CSDL trực quan, hỗ trợ thiết kế sơ đồ ERD, chạy query và theo dõi hiệu suất. Sau khi cài MySQL, tạo database `park_db` và chạy migration script để tạo toàn bộ bảng.

### 1.5.5 Công cụ hỗ trợ khác

- **Postman:** Kiểm thử API backend, đặc biệt hữu ích khi test luồng play game và admin card management
- **Git:** Quản lý phiên bản mã nguồn
- **GlobalPlatform Pro (gp.jar):** Công cụ dòng lệnh để nạp ParkCard Applet lên thẻ Java Card trong môi trường phát triển
- **Card Reader (PC/SC):** Thiết bị vật lý đọc thẻ Java Card, kết nối USB với máy tính chạy SmardCard Terminal

---

## 1.6 Các thách thức khi phát triển ứng dụng

### 1.6.1 Lập trình Java Card và giao thức APDU

Đây là thách thức kỹ thuật lớn nhất. Java Card có môi trường runtime rất hạn chế (bộ nhớ RAM tính bằng bytes, EEPROM vài KB). Lập trình Applet trên Java Card đòi hỏi hiểu biết sâu về bộ nhớ transient/persistent, giao thức APDU ở mức byte và các ràng buộc đặc biệt của Java Card API (không có garbage collection, kiểu dữ liệu hạn chế).

### 1.6.2 Bảo mật đa lớp

Hệ thống cần đảm bảo bảo mật đồng thời ở nhiều tầng: mã hóa AES-128 dữ liệu lưu trên thẻ, bảo vệ PIN bằng PBKDF2, xác thực thẻ thật bằng RSA Challenge-Response, và bảo vệ API bằng JWT. Điều phối các cơ chế bảo mật này hoạt động đúng và nhất quán là thách thức không nhỏ.

### 1.6.3 Tính nhất quán dữ liệu giao dịch

Khi nhiều thiết bị Terminal cùng lúc gọi API trừ tiền cho cùng một tài khoản (ví dụ gia đình có nhiều thẻ), cần đảm bảo không xảy ra race condition làm sai lệch số dư. Backend phải sử dụng database transaction và khóa đúng cách.

### 1.6.4 Độ trễ trong môi trường thực tế

Tại cổng soát vé, thời gian từ lúc khách quẹt thẻ đến lúc nhận phản hồi phải đủ nhanh (dưới 2 giây) để không gây cảm giác chậm trễ. Cần tối ưu cả thời gian đọc APDU lẫn thời gian gọi API backend.

### 1.6.5 Tái sử dụng thẻ an toàn

Khi thẻ được trả lại và ghi đè cho khách mới, cần đảm bảo toàn bộ dữ liệu cũ bị xóa sạch (PIN cũ, thông tin cũ, keypair RSA cũ) và khóa RSA mới được sinh và đăng ký lên server trước khi thẻ hoạt động trở lại. Quy trình này phải được thực hiện nguyên tử (không thể bị gián đoạn giữa chừng).

### 1.6.6 Trải nghiệm người dùng đa dạng

Hệ thống phục vụ cả những khách không có smartphone (chỉ dùng thẻ vật lý) lẫn những người dùng app thành thạo. Cần đảm bảo cả hai luồng đều hoạt động trơn tru mà không buộc khách phải có app.

---

## 1.7 Tổng kết chương

Chương này đã phân tích thực trạng quản lý vé tại các công viên giải trí Việt Nam, chỉ ra những bất cập của các giải pháp truyền thống (vé giấy, thẻ từ, QR code) và tính cấp thiết của việc áp dụng công nghệ Java Card tích hợp backend hiện đại. Bài toán được xác định rõ ràng với bốn thành phần: Java Card vật lý làm định danh, SmardCard Terminal đọc/ghi thẻ tại quầy và trò chơi, Backend Ktor là nguồn truth cho số dư, và AppCongVien Android nhận thông báo realtime.

Các công nghệ được lựa chọn (Kotlin/Compose, Ktor, MySQL, Java Card/APDU, RSA) cùng môi trường phát triển tương ứng đã được trình bày, tạo nền tảng định hướng cho quá trình phân tích, thiết kế và hiện thực hóa ở các chương tiếp theo.

---

# CHƯƠNG 2. PHÂN TÍCH THIẾT KẾ HỆ THỐNG

## 2.1 Xác định actor

Hệ thống bao gồm các actor sau:

- **Khách (Guest):** Người chưa có tài khoản. Có thể sử dụng thẻ Java Card nhận tại quầy mà không cần app, hoặc đăng ký tài khoản để sử dụng đầy đủ tính năng.

- **Người dùng đã đăng nhập (User):** Khách đã có tài khoản trên AppCongVien. Có thể xem số dư, nhận thông báo realtime, nạp tiền online và yêu cầu tạo thẻ qua app.

- **Nhân viên quầy (SmardCard Terminal – chế độ Quầy):** Tạo thẻ mới, ghi đè thẻ cũ, nạp tiền mặt, duyệt yêu cầu thẻ từ app.

- **Nhân viên vận hành trò chơi (SmardCard Terminal – chế độ Game):** Sử dụng SmardCard Terminal ở chế độ đọc thẻ tự động tại cổng trò chơi.

- **Quản trị viên (Admin – appdesktop):** Quản lý toàn bộ hệ thống: người dùng, thẻ, tài chính, thông báo, thống kê.

- **Hệ thống Backend (Ktor):** Actor hệ thống xử lý logic nghiệp vụ, lưu trữ dữ liệu, gửi thông báo.

## 2.2 Biểu đồ Use Case tổng quát

*[Hình 2.2 Biểu đồ Use Case tổng quát]*

## 2.3 Các Use Case chi tiết

### 2.3.1 Use Case Đăng nhập

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Đăng nhập |
| **Actor** | Người dùng, Quản trị viên, Nhân viên quầy |
| **Mô tả** | Xác thực bằng số điện thoại + mật khẩu, nhận JWT token |
| **Luồng chính** | 1. Nhập SĐT + mật khẩu → 2. Server xác thực BCrypt → 3. Trả về JWT token (kèm role: USER/TERMINAL/ADMIN) → 4. Lưu token, chuyển đến màn hình chính |
| **Luồng thay thế** | Sai thông tin → Thông báo lỗi, không trả token |

### 2.3.2 Use Case Tạo thẻ mới tại quầy

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Tạo thẻ mới tại quầy |
| **Actor** | Nhân viên quầy (SmardCard Terminal) |
| **Mô tả** | Nhân viên nhập thông tin khách, ghi APDU lên thẻ Java Card trắng, đăng ký thẻ trên backend |
| **Tiền điều kiện** | Nhân viên đã đăng nhập, có thẻ Java Card trắng (hoặc thẻ đã trả lại) trong reader |
| **Luồng chính** | 1. Nhân viên nhập tên + SĐT khách → 2. Hệ thống tìm hoặc tạo tài khoản user trên backend → 3. SmardCard gửi APDU: (a) INS_SET_CUSTOMER_ID → ghi customerID, (b) INS_GENERATE_RSA_KEYPAIR → sinh keypair RSA, (c) INS_GET_PUBLIC_KEY → đọc public key, (d) INS_CREATE_ADMIN_PIN → khởi tạo admin PIN, (e) INS_CREATE_PIN → khởi tạo user PIN mặc định, (f) INS_WRITE_INFO → ghi tên + SĐT (mã hóa AES) → 4. SmardCard gọi POST /api/admin/cards/create với customerID + publicKey → 5. Backend lưu bản ghi thẻ, liên kết với userId, lưu publicKey → 6. Nhân viên giao thẻ cho khách |
| **Luồng thay thế** | Card reader lỗi → Thông báo kiểm tra kết nối và thử lại |

### 2.3.3 Use Case Quẹt thẻ chơi game (RSA Challenge-Response)

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Quẹt thẻ chơi game |
| **Actor** | Khách (thẻ Java Card), SmardCard Terminal, Backend |
| **Mô tả** | Khách đặt thẻ vào thiết bị đọc tại cổng trò chơi; hệ thống xác thực thẻ thật bằng RSA, trừ tiền và gửi thông báo |
| **Tiền điều kiện** | Thẻ đang ở trạng thái ACTIVE, tài khoản có đủ số dư |
| **Luồng chính** | 1. Khách chạm/đặt thẻ vào reader → 2. SmardCard đọc customerID (INS_GET_CUSTOMER_ID) → 3. SmardCard tạo challenge ngẫu nhiên 16 byte → 4. SmardCard gửi APDU INS_SIGN_CHALLENGE với challenge → 5. Thẻ ký bằng RSA private key → trả về signature 128 byte → 6. SmardCard gọi POST /api/games/{gameId}/play với { customerID, terminalId, challenge, signature } → 7. Backend: (a) tìm card theo customerID, (b) lấy publicKey, (c) xác thực chữ ký RSA, (d) kiểm tra số dư >= pricePerTurn, (e) trừ tiền, (f) ghi BalanceTransaction (type=GAME_PLAY), (g) ghi GamePlayLog, (h) tạo Notification → 8. Backend trả về { gameName, amountCharged, remainingBalance, userName } → 9. Terminal hiển thị kết quả → 10. App Android khách nhận push notification tức thì |
| **Luồng thay thế A** | Chữ ký RSA sai → Từ chối (thẻ giả/clone), ghi log cảnh báo |
| **Luồng thay thế B** | Số dư không đủ → Terminal hiển thị "Số dư không đủ, vui lòng nạp thêm" |
| **Luồng thay thế C** | Thẻ bị BLOCKED → Terminal từ chối, hiển thị "Thẻ đã bị khóa" |

### 2.3.4 Use Case Nạp tiền

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Nạp tiền |
| **Actor** | Người dùng (qua app), Nhân viên quầy (tiền mặt), Quản trị viên (điều chỉnh) |
| **Luồng chính – Qua app** | 1. User chọn số tiền và phương thức (MoMo/ngân hàng) → 2. Gọi POST /api/wallet/topup → 3. Backend cộng currentBalance, ghi BalanceTransaction (type=TOP_UP), ghi PaymentRecord → 4. App hiển thị số dư mới |
| **Luồng chính – Tại quầy** | 1. Nhân viên tìm user (quét thẻ hoặc nhập SĐT) → 2. Nhập số tiền mặt → 3. Gọi POST /api/admin/topup { userId, amount, method: CASH } → 4. Backend cộng balance → 5. Nếu user có app: gửi Notification "Nạp tiền [X]đ thành công tại quầy" |

### 2.3.5 Use Case Trả thẻ và tái sử dụng

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Trả thẻ và tái sử dụng |
| **Actor** | Nhân viên quầy (SmardCard Terminal) |
| **Mô tả** | Khi khách trả thẻ về, nhân viên hoàn tiền và tái sử dụng thẻ cho khách mới |
| **Luồng chính** | 1. Nhân viên đặt thẻ vào reader → 2. SmardCard đọc customerID → 3. Gọi GET /api/admin/cards/by-customer/{customerID} → lấy số dư còn lại → 4. Nếu còn số dư: hiển thị "Hoàn [X]đ tiền mặt cho khách" → Gọi POST /api/admin/cards/return (trừ hết balance về 0, set status=INACTIVE) → 5. Thu thẻ về → 6. Khi có khách mới: Ghi đè APDU (INS_VERIFY_ADMIN_PIN → INS_RESET_USER_PIN → INS_SET_CUSTOMER_ID → INS_GENERATE_RSA_KEYPAIR → INS_GET_PUBLIC_KEY → INS_WRITE_INFO → INS_CREATE_PIN) → 7. Gọi POST /api/admin/cards/reassign { cardId, newUserId, newPublicKey } |

### 2.3.6 Use Case Yêu cầu tạo thẻ qua app

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Yêu cầu tạo thẻ qua app |
| **Actor** | Người dùng, Nhân viên quầy |
| **Mô tả** | User gửi yêu cầu tạo thẻ qua app, nhân viên duyệt và tạo thẻ, user nhận thông báo đến quầy lấy |
| **Luồng chính** | 1. User mở app → "Thẻ của tôi" → "Yêu cầu tạo thẻ" (kèm ghi chú vị trí) → 2. Gọi POST /api/cards/request → 3. SmardCard Terminal hiển thị yêu cầu trong tab "Yêu cầu thẻ" → 4. Nhân viên bấm "Duyệt + Tạo thẻ" → chạy luồng tạo thẻ mới (APDU) → 5. Gọi PUT /api/admin/card-requests/{id}/resolve { action: APPROVED } → 6. Backend gửi Notification đến user: "Yêu cầu tạo thẻ đã được duyệt, đến quầy nhận thẻ" |

### 2.3.7 Use Case Khóa thẻ khi mất

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Khóa thẻ |
| **Actor** | Người dùng (qua app), Quản trị viên |
| **Luồng chính** | 1. User/Admin chọn thẻ → "Khóa thẻ" → 2. Gọi POST /api/cards/{cardId}/block → 3. Backend set card.status = BLOCKED → 4. Mọi request /play với cardId này bị từ chối tức thì, kể cả khi chữ ký RSA hợp lệ |

---

## 2.4 Biểu đồ trình tự

### 2.4.1 Biểu đồ trình tự Đăng nhập

*[Hình 2.3 Biểu đồ trình tự đăng nhập]*

```
User         AppCongVien         Backend              Database
 |                |                  |                    |
 |--SĐT+mật khẩu-->|                  |                    |
 |                |--POST /auth/login->|                    |
 |                |                  |--SELECT Account---->|
 |                |                  |<--Account record----|
 |                |                  |--BCrypt.verify()    |
 |                |                  |--issueJWT(userId)   |
 |                |<---AuthData{token}|                    |
 |                |--lưu token------->|                    |
 |<--HomeScreen---|                  |                    |
```

### 2.4.2 Biểu đồ trình tự Tạo thẻ mới tại quầy

*[Hình 2.4 Biểu đồ trình tự tạo thẻ mới tại quầy]*

```
Nhân viên   SmardCard Terminal    Java Card          Backend
    |               |                 |                  |
    |--Nhập tên,SĐT>|                 |                  |
    |               |--POST /user/find or create--------->|
    |               |<---{ userId, customerID }-----------|
    |               |                 |                  |
    |               |--APDU: INS_SET_CUSTOMER_ID--------->|
    |               |<---SW: 90 00----|                  |
    |               |--APDU: INS_GENERATE_RSA_KEYPAIR---->|
    |               |<---SW: 90 00----|                  |
    |               |--APDU: INS_GET_PUBLIC_KEY---------->|
    |               |<---publicKey (128B) + 90 00---------|
    |               |--APDU: INS_CREATE_ADMIN_PIN-------->|
    |               |--APDU: INS_CREATE_PIN-------------->|
    |               |--APDU: INS_WRITE_INFO (AES-enc)---->|
    |               |<---SW: 90 00----|                  |
    |               |--POST /admin/cards/create { customerID, publicKey }->|
    |               |                                    |--INSERT Card---->|
    |               |<-----------{ cardId, status:ACTIVE }----------------|
    |<--Thẻ sẵn sàng|                 |                  |
```

### 2.4.3 Biểu đồ trình tự Quẹt thẻ chơi game (RSA Challenge-Response)

*[Hình 2.5 Biểu đồ trình tự quẹt thẻ chơi game]*

```
Khách  JavaCard  SmardCard Terminal         Backend              App Android
  |       |              |                     |                      |
  |chạm-->|              |                     |                      |
  |       |<--INS_GET_CUSTOMER_ID----|         |                      |
  |       |---customerID----------->|         |                      |
  |       |                         |         |                      |
  |       | challenge = random(16B) |         |                      |
  |       |<--INS_SIGN_CHALLENGE----|         |                      |
  |       |   (challenge bytes)     |         |                      |
  |       |---signature(128B)------>|         |                      |
  |       |                         |         |                      |
  |       |      POST /games/{id}/play { customerID, challenge, signature }
  |       |                         |-------->|                      |
  |       |                         |         |--findCard(customerID)|
  |       |                         |         |--RSA.verify(sig,pubK)|
  |       |                         |         |--checkBalance        |
  |       |                         |         |--deductBalance       |
  |       |                         |         |--INSERT Transaction  |
  |       |                         |         |--INSERT GamePlayLog  |
  |       |                         |         |--createNotification  |
  |       |         { gameName, amountCharged, remainingBalance }     |
  |       |                         |<--------|                      |
  |       |   "Xin chào [Tên]!"     |         |--push notification-->|
  |<------|   "Trừ 20.000đ"         |         |                      |
  |       |   "Còn: 80.000đ"        |         |    🎮 Tàu lượn       |
  |       |                         |         |    Trừ: 20.000đ      |
  |       |                         |         |    Còn: 80.000đ      |
```

### 2.4.4 Biểu đồ trình tự Nạp tiền qua app và tại quầy

*[Hình 2.6 Biểu đồ trình tự nạp tiền]*

### 2.4.5 Biểu đồ trình tự Trả thẻ và tái sử dụng

*[Hình 2.7 Biểu đồ trình tự trả thẻ và tái sử dụng]*

### 2.4.6 Biểu đồ trình tự Yêu cầu tạo thẻ qua app

*[Hình 2.8 Biểu đồ trình tự yêu cầu tạo thẻ qua app]*

---

## 2.5 Biểu đồ lớp

### 2.5.1 Biểu đồ lớp tổng quát

*[Hình 2.9 Biểu đồ lớp tổng quát]*

### 2.5.2 Chi tiết các lớp chính

**Lớp Account:**
- `accountId`: String (UUID)
- `phoneNumber`: String (unique)
- `passwordHash`: String (BCrypt)
- `role`: String (USER / TERMINAL / ADMIN)
- `createdAt`: Timestamp

**Lớp User:**
- `userId`: String (UUID)
- `fullName`: String
- `phoneNumber`: String
- `currentBalance`: BigDecimal
- `loyaltyPoints`: Int
- `membershipLevel`: String (STANDARD / SILVER / GOLD / PLATINUM)
- `referralCode`: String
- `status`: String (ACTIVE / SUSPENDED)

**Lớp Card:**
- `cardId`: String (UUID)
- `userId`: String (FK → User, nullable khi thẻ chưa gắn tài khoản)
- `customerID`: String (định danh ghi trên thẻ vật lý)
- `cardName`: String (ví dụ: "Thẻ con 1", "Thẻ con 2")
- `rsaPublicKey`: String (TEXT, upload khi tạo thẻ)
- `status`: String (ACTIVE / BLOCKED / INACTIVE)
- `issuedAt`: Timestamp
- `blockedAt`: Timestamp?
- `blockedReason`: String?

**Lớp Game:**
- `gameId`: Int
- `name`: String
- `category`: String
- `pricePerTurn`: BigDecimal
- `durationMinutes`: Int
- `location`: String
- `ageRequired`: Int?
- `heightRequired`: Int?
- `riskLevel`: String
- `isFeatured`: Boolean
- `status`: String (ACTIVE / MAINTENANCE / CLOSED)

**Lớp BalanceTransaction:**
- `transactionId`: String (UUID)
- `userId`: String (FK → User)
- `cardId`: String? (FK → Card, nullable – ghi lại thẻ nào thực hiện giao dịch)
- `type`: String (TOP_UP / GAME_PLAY / REFUND / ADJUSTMENT / CASH_TOPUP)
- `amount`: BigDecimal
- `description`: String
- `gameId`: Int? (nullable, chỉ có khi type=GAME_PLAY)
- `terminalId`: String? (nullable)
- `createdAt`: Timestamp

**Lớp CardRequest:**
- `requestId`: String (UUID)
- `userId`: String (FK → User)
- `status`: String (PENDING / APPROVED / REJECTED)
- `note`: String?
- `resolvedBy`: String? (adminId)
- `resolvedAt`: Timestamp?
- `createdAt`: Timestamp

**Lớp Notification:**
- `notificationId`: String
- `userId`: String (FK → User)
- `title`: String
- `message`: String
- `type`: String (GAME_PLAY / TOP_UP / CARD_APPROVED / SYSTEM)
- `isRead`: Boolean
- `createdAt`: Timestamp

---

## 2.6 Thiết kế cơ sở dữ liệu

Cơ sở dữ liệu gồm các bảng được tổ chức theo nhóm chức năng:

*[Hình 2.10 Sơ đồ cơ sở dữ liệu]*

**Nhóm quản lý tài khoản:**

| Bảng | Mô tả |
|------|-------|
| `accounts` | Thông tin đăng nhập (SĐT, mật khẩu băm, role) |
| `users` | Hồ sơ khách hàng (tên, số dư, điểm thưởng, hạng) |
| `admins` | Tài khoản nhân viên và quản trị viên |

**Nhóm quản lý thẻ:**

| Bảng | Mô tả |
|------|-------|
| `cards` | Thông tin thẻ Java Card (customerID, rsaPublicKey, status, userId) |
| `card_requests` | Yêu cầu tạo thẻ từ app (userId, status, note) |

**Nhóm trò chơi:**

| Bảng | Mô tả |
|------|-------|
| `games` | Danh mục trò chơi (tên, giá, vị trí, yêu cầu độ tuổi/chiều cao) |
| `game_reviews` | Đánh giá và nhận xét của khách |
| `game_play_logs` | Lịch sử quẹt thẻ tại từng trò chơi |
| `terminals` | Thiết bị terminal NFC tại các điểm trò chơi |

**Nhóm tài chính:**

| Bảng | Mô tả |
|------|-------|
| `balance_transactions` | Mọi biến động số dư (TOP_UP, GAME_PLAY, REFUND, ADJUSTMENT). Có field `cardId` để biết thẻ nào dùng |
| `payment_records` | Lịch sử thanh toán online (MoMo, ngân hàng) |

**Nhóm nội dung và truyền thông:**

| Bảng | Mô tả |
|------|-------|
| `notifications` | Thông báo đẩy đến người dùng (type: GAME_PLAY, TOP_UP, CARD_APPROVED) |
| `announcements` | Bảng tin / carousel quảng cáo trên HomeScreen app |
| `support_messages` | Tin nhắn hỗ trợ trực tuyến |

**Lưu ý thiết kế đặc biệt:**

Trường `cardId` trong bảng `balance_transactions` là điểm quan trọng hỗ trợ tính năng **"gia đình nhiều thẻ"**. Mỗi giao dịch ghi lại thẻ nào đã thực hiện, cho phép lọc lịch sử theo từng thẻ trong ứng dụng Android:

```
Tài khoản phụ huynh: 300.000đ
├─ Thẻ A (con 1): Tàu lượn  → transaction { userId, cardId=A, -20.000đ }
├─ Thẻ B (con 2): Đu quay   → transaction { userId, cardId=B, -10.000đ }
└─ Thẻ C (con 3): Xe hơi    → transaction { userId, cardId=C, -15.000đ }
Số dư chung còn: 255.000đ
```

---

## 2.7 Bảng lệnh APDU của ParkCard Applet

| INS | Tên lệnh | Mô tả |
|-----|----------|-------|
| 0x01 | INS_CREATE_PIN | Khởi tạo PIN người dùng mặc định |
| 0x07 | INS_WRITE_INFO | Ghi tên + SĐT (mã hóa AES-128-CBC) |
| 0x17 | INS_SET_CUSTOMER_ID | Ghi customerID lên thẻ (plaintext) |
| 0x18 | INS_GET_CUSTOMER_ID | Đọc customerID từ thẻ |
| 0x1B | INS_SIGN_CHALLENGE | Ký challenge bằng RSA private key |
| 0x1D | INS_GENERATE_RSA_KEYPAIR | Sinh cặp khóa RSA-1024 mới trên thẻ |
| 0x1E | INS_GET_PUBLIC_KEY | Đọc RSA public key (128 bytes) |
| 0x1F | INS_VERIFY_ADMIN_PIN | Xác minh admin PIN để có quyền ghi đè |
| 0x21 | INS_RESET_USER_PIN | Xóa PIN cũ (cần admin PIN trước) |

**Quy trình bảo mật PIN trên thẻ:**
```
Nhập PIN → PBKDF2(PIN, salt, 200 vòng) → PIN-Key
          → AES-Decrypt(wrappedMasterKey, PIN-Key) → Master Key (RAM transient)
          → Master Key dùng để đọc/ghi dữ liệu mã hóa trên EEPROM
          → Master Key tự xóa khỏi RAM khi rút thẻ
```

---

## 2.8 Tổng kết chương

Chương 2 đã trình bày chi tiết phân tích và thiết kế hệ thống quản lý vé công viên sử dụng Java Card. Các actor được xác định rõ ràng với vai trò phân biệt giữa khách tham quan, nhân viên quầy, nhân viên vận hành trò chơi và quản trị viên. Bảy Use Case chính được đặc tả đầy đủ, trong đó Use Case "Quẹt thẻ chơi game với RSA Challenge-Response" là thành phần kỹ thuật cốt lõi nhất của hệ thống, đảm bảo thẻ thật không thể bị clone.

Thiết kế cơ sở dữ liệu với trường `cardId` trong bảng `balance_transactions` cho phép theo dõi chi tiêu theo từng thẻ trong cùng một tài khoản, hỗ trợ tính năng hữu ích cho gia đình có nhiều thành viên. Bảng lệnh APDU của ParkCard Applet cung cấp nền tảng kỹ thuật cho toàn bộ giao tiếp giữa terminal và thẻ vật lý.

---

# CHƯƠNG 3. THỰC NGHIỆM

## 3.1 Mô tả cách cài đặt

### 3.1.1 Cài đặt Backend

**Bước 1:** Cài đặt JDK 17+, MySQL 8.x, IntelliJ IDEA.

**Bước 2:** Clone source code, mở thư mục `backend/` bằng IntelliJ.

**Bước 3:** Tạo database và cấu hình trong `application.conf`:
```hocon
database {
    url = "jdbc:mysql://localhost:3306/park_db"
    user = "root"
    password = "your_password"
}
jwt {
    secret = "your_jwt_secret_key_min_32_chars"
    issuer = "park-system"
}
```

**Bước 4:** Chạy `Application.kt` – server khởi động trên cổng 8080.

**Kiểm tra:** `GET http://localhost:8080/api/auth/health` → `{ "status": "ok" }`

**Endpoint play game mới:**
```
POST /api/games/{gameId}/play
Authorization: Bearer {terminalJWT}
Body: {
  "customerID": "C001234",
  "terminalId": "T001",
  "challenge": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6",
  "signature": "3a7f...128bytes...b9c2"
}
Response: {
  "success": true,
  "gameName": "Tàu lượn siêu tốc",
  "amountCharged": 20000,
  "remainingBalance": 80000,
  "userName": "Nguyễn Văn A"
}
```

**Endpoint admin card management:**
```
POST /api/admin/cards/create
POST /api/admin/cards/return
POST /api/admin/cards/reassign
POST /api/admin/topup
GET  /api/admin/cards/by-customer/{customerID}
GET  /api/admin/card-requests
PUT  /api/admin/card-requests/{id}/resolve
```

### 3.1.2 Cài đặt SmardCard Terminal

**Bước 1:** Cài IntelliJ IDEA, JDK 17+.

**Bước 2:** Cắm card reader (ACR122U hoặc tương tự) vào USB. Cài driver PC/SC tương ứng với hệ điều hành.

**Bước 3:** Mở project SmardCard trong IntelliJ, cấu hình địa chỉ backend.

**Bước 4:** Chạy ứng dụng. Đăng nhập bằng tài khoản TERMINAL (JWT role=TERMINAL).

**Kiểm tra kết nối card reader:**
```kotlin
// SmartCardManager.kt
val terminals = TerminalFactory.getDefault().terminals().list()
println("Card readers: ${terminals.map { it.name }}")
// Output: Card readers: [ACS ACR122U PICC Interface 0]
```

**Bước 5:** Cắm thẻ Java Card đã được nạp ParkCard Applet vào reader → SmardCard Terminal tự động nhận diện.

### 3.1.3 Cài đặt AppCongVien (Android)

**Bước 1:** Cài Android Studio, mở project `Appcongvien/`.

**Bước 2:** Cấu hình địa chỉ server trong `RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "http://192.168.1.x:8080/"
```

**Bước 3:** Build và chạy trên thiết bị Android thực (API 21+).

### 3.1.4 Cài đặt appdesktop Admin

**Bước 1:** Mở project `appdesktop/` trong IntelliJ.

**Bước 2:** Cấu hình địa chỉ backend, chạy `main.kt`.

**Bước 3:** Đăng nhập bằng tài khoản ADMIN.

---

## 3.2 Cách làm việc

### 3.2.1 Quy trình khách lần đầu đến – không có app

*[Hình 3.8 SmardCard Terminal – Màn hình tạo thẻ mới]*

**Bước 1 – Đến quầy:**
Khách đến quầy lễ tân, nhân viên mở SmardCard Terminal tab "Tạo thẻ mới".

**Bước 2 – Nhập thông tin và ghi thẻ:**
Nhân viên nhập tên và số điện thoại khách. SmardCard Terminal tự động thực hiện chuỗi lệnh APDU lên thẻ Java Card trắng:
- Tạo customerID duy nhất, ghi lên thẻ
- Sinh cặp khóa RSA-1024 trên thẻ, lấy public key
- Ghi tên + SĐT (mã hóa AES-128)
- Khởi tạo PIN admin và PIN user mặc định
- Đăng ký thẻ lên backend (kèm public key)

**Bước 3 – Nạp tiền:**
Nhân viên nhập số tiền mặt khách đưa, SmardCard gọi API `/api/admin/topup`. Số dư hiển thị ngay trên terminal.

**Bước 4 – Ra khu vui chơi:**
Khách nhận thẻ và tự do chơi. Tại mỗi trò chơi, chỉ cần đặt thẻ vào thiết bị đọc, tiền tự động bị trừ.

**Bước 5 – Tại cổng trò chơi:**

*[Hình 3.10 SmardCard Terminal – Màn hình chơi game (soát vé)]*

SmardCard Terminal chạy ở chế độ Game hiển thị màn hình chờ. Khi khách đặt thẻ:
- Đọc customerID từ thẻ
- Thực hiện RSA Challenge-Response (tự động, dưới 1 giây)
- Gọi API play game
- Terminal hiển thị: **"Xin chào Nguyễn Văn A! Tàu lượn siêu tốc – Trừ 20.000đ – Còn: 80.000đ"**
- Cổng mở / đèn xanh

**Bước 6 – Hết tiền:**
Nếu số dư không đủ, terminal hiển thị: **"Số dư không đủ. Vui lòng nạp thêm tại quầy."**

**Bước 7 – Trả thẻ khi về:**

*[Hình 3.9 SmardCard Terminal – Màn hình ghi đè thẻ cũ]*

Nhân viên quét thẻ → xem số dư còn lại → hoàn tiền mặt → thu thẻ. Thẻ được ghi đè và tái sử dụng cho khách tiếp theo.

---

### 3.2.2 Quy trình khách đã có app – yêu cầu thẻ từ xa

*[Hình 3.1 Màn hình đăng nhập ứng dụng Android]*

**Bước 1:** Khách tải AppCongVien, đăng ký tài khoản bằng SĐT.

*[Hình 3.2 Màn hình trang chủ (số dư, thẻ, thông báo)]*

**Bước 2:** Vào "Thẻ của tôi" → "Yêu cầu tạo thẻ" → nhập ghi chú (ví dụ: "Tôi đang ở cổng A").

*[Hình 3.4 Màn hình yêu cầu tạo thẻ]*

**Bước 3:** Nhân viên thấy yêu cầu trong SmardCard Terminal tab "Yêu cầu thẻ".

*[Hình 3.11 SmardCard Terminal – Màn hình duyệt yêu cầu thẻ]*

**Bước 4:** Nhân viên bấm "Duyệt + Tạo thẻ" → tự động chạy chuỗi APDU → đăng ký thẻ.

**Bước 5:** App khách nhận thông báo: **"Thẻ đã sẵn sàng. Đến quầy nhận thẻ tại cổng A."**

**Bước 6:** Sau khi nhận thẻ và nạp tiền, khi quẹt thẻ tại bất kỳ trò chơi nào, khách nhận ngay thông báo trên điện thoại:

*[Hình 3.7 Thông báo realtime khi quẹt thẻ chơi game]*

```
┌──────────────────────────────────┐
│ 🎮 Chơi game thành công          │
│ Tàu lượn siêu tốc                │
│ Trừ: 20.000đ                     │
│ Số dư còn lại: 80.000đ           │
└──────────────────────────────────┘
```

---

### 3.2.3 Nạp tiền qua app

*[Hình 3.5 Màn hình nạp tiền ví điện tử]*

Khách chọn số tiền và phương thức thanh toán (MoMo, chuyển khoản ngân hàng). Sau khi hoàn tất thanh toán, số dư cập nhật ngay trong app. Notification xác nhận: **"Nạp tiền 200.000đ thành công"**.

---

### 3.2.4 Lịch sử giao dịch và theo dõi chi tiêu theo thẻ

*[Hình 3.6 Màn hình lịch sử giao dịch (lọc theo thẻ)]*

Màn hình lịch sử giao dịch hiển thị toàn bộ biến động số dư. Đối với tài khoản có nhiều thẻ (gia đình), người dùng có thể lọc theo từng thẻ:

```
[Tất cả]  [Thẻ A – Con 1]  [Thẻ B – Con 2]  [Thẻ C – Con 3]
→ Chọn "Thẻ A – Con 1" → chỉ hiện giao dịch của thẻ đó
→ Phụ huynh biết con 1 tiêu bao nhiêu trong ngày
```

---

### 3.2.5 Quản lý hệ thống qua appdesktop Admin

*[Hình 3.12 Ứng dụng Admin Desktop – Dashboard tổng quan]*

Dashboard hiển thị các chỉ số theo thời gian thực: tổng số người dùng, số thẻ đang hoạt động, doanh thu ngày/tuần/tháng, và biểu đồ doanh thu theo khoảng thời gian tùy chọn.

*[Hình 3.13 Ứng dụng Admin Desktop – Quản lý người dùng & nạp tiền tại quầy]*

Tại màn hình Quản lý người dùng, admin có thể:
- Tra cứu theo tên/SĐT
- Xem số dư và lịch sử giao dịch
- Nạp tiền hộ (CASH) bằng nút "Nạp tiền" → dialog nhập số tiền → gọi `/api/admin/topup`
- Khóa/mở khóa thẻ
- Xem danh sách thẻ đang liên kết với tài khoản

---

# KẾT LUẬN

Đề tài **"Nghiên cứu phát triển hệ thống quản lý vé trong công viên sử dụng Smart Card"** đã đạt được các kết quả sau:

**Về mặt lý thuyết:**
- Nắm vững và áp dụng công nghệ Java Card với giao thức APDU (ISO 7816-4) để phát triển ParkCard Applet chạy trực tiếp trên thẻ thông minh vật lý
- Nghiên cứu và triển khai cơ chế bảo mật đa lớp: mã hóa AES-128 dữ liệu trên thẻ, bảo vệ PIN bằng PBKDF2, và đặc biệt là cơ chế **RSA Challenge-Response** để xác thực thẻ thật chống clone
- Xây dựng kiến trúc hệ thống phân tán với **"balance on server"**: thẻ chỉ là định danh, toàn bộ dữ liệu tài chính quản lý tập trung trên backend, đảm bảo tính nhất quán và khả năng kiểm soát từ xa

**Về mặt thực tiễn:**
- Xây dựng thành công **AppCongVien** (Android) với tính năng nhận thông báo realtime mỗi lần quẹt thẻ, xem số dư, nạp tiền online, yêu cầu tạo thẻ và lịch sử giao dịch lọc theo từng thẻ thành viên
- Xây dựng **SmardCard Terminal** (Desktop) phục vụ nhân viên quầy: tạo thẻ mới, ghi đè thẻ cũ tái sử dụng, duyệt yêu cầu thẻ từ app và vận hành soát vé tự động tại cổng trò chơi
- Xây dựng **appdesktop Admin** với Dashboard thống kê doanh thu, quản lý người dùng và nạp tiền tại quầy
- Xây dựng **Backend Ktor** với đầy đủ API cho cả ba loại client, hỗ trợ hạ tầng thông báo realtime qua WebSocket và xác thực RSA cho từng giao dịch
- Giải quyết bài toán thực tế về tính năng gia đình nhiều thẻ: ví chung, theo dõi chi tiêu theo từng thẻ thành viên

**Hướng phát triển tiếp theo:**
- Tích hợp cổng thanh toán thực tế (MoMo SDK, VNPAY) thay vì giả lập
- Phát triển tính năng thẻ không tiếp xúc (contactless Java Card) thông qua NFC để không cần cắm thẻ vào reader
- Thêm tính năng gợi ý trò chơi và chương trình khách hàng thân thiết dựa trên lịch sử sử dụng
- Triển khai hệ thống lên cloud server để phục vụ nhiều chi nhánh công viên
- Bổ sung cơ chế backup và phục hồi thẻ khi quên PIN

---

# TÀI LIỆU THAM KHẢO

[1] Oracle. *Java Card Technology*. https://www.oracle.com/java/technologies/javacard-sdk.html

[2] ISO/IEC 7816-4:2020. *Identification cards – Integrated circuit cards – Part 4: Organization, security and commands for interchange*. International Organization for Standardization.

[3] GlobalPlatform. *GlobalPlatform Card Specification v2.3*. https://globalplatform.org/specs-library/

[4] JetBrains. *Ktor Documentation*. https://ktor.io/docs/

[5] JetBrains. *Kotlin Exposed ORM*. https://github.com/JetBrains/Exposed

[6] Android Developers. *Jetpack Compose Documentation*. https://developer.android.com/jetpack/compose

[7] Auth0. *JSON Web Tokens Introduction*. https://jwt.io/introduction

[8] NIST. *Recommendation for Password-Based Key Derivation (PBKDF2)*. NIST Special Publication 800-132.

[9] RSA Laboratories. *PKCS #1: RSA Cryptography Standard*. https://www.rfc-editor.org/rfc/rfc8017

[10] MySQL Documentation. *MySQL 8.0 Reference Manual*. https://dev.mysql.com/doc/refman/8.0/en/

[11] Square Inc. *Retrofit: A type-safe HTTP client for Android and Java*. https://square.github.io/retrofit/

[12] JetBrains. *Kotlin Compose Multiplatform*. https://www.jetbrains.com/lp/compose-multiplatform/

[13] Nguyen, T. A., & Le, V. B. (2022). *Ứng dụng công nghệ thẻ thông minh trong hệ thống thanh toán không tiền mặt tại Việt Nam*. Tạp chí Công nghệ Thông tin và Truyền thông.

[14] ACS. *ACR122U NFC Reader/Writer Technical Specifications*. Advanced Card Systems Ltd.
