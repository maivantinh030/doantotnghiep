# 📡 Danh Sách API Endpoints - App Công Viên

Tài liệu này liệt kê tất cả các API endpoints có thể có dựa trên các màn hình trong ứng dụng.

## 🔐 Authentication Endpoints

### 1. Đăng nhập
- **POST** `/api/auth/login`
- **Body**: `{ "phoneNumber": "string", "password": "string" }`
- **Response**: `{ "token": "string", "user": {...}, "refreshToken": "string" }`

### 2. Đăng ký
- **POST** `/api/auth/register`
- **Body**: `{ "phoneNumber": "string", "password": "string", "fullName": "string", "email": "string" }`
- **Response**: `{ "userId": "string", "message": "string" }`

### 3. Quên mật khẩu
- **POST** `/api/auth/forgot-password`
- **Body**: `{ "phoneNumber": "string" }`
- **Response**: `{ "otpSent": true, "message": "string" }`

### 4. Đặt lại mật khẩu
- **POST** `/api/auth/reset-password`
- **Body**: `{ "phoneNumber": "string", "otp": "string", "newPassword": "string" }`
- **Response**: `{ "success": true, "message": "string" }`

### 5. Đổi mật khẩu
- **POST** `/api/auth/change-password`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "currentPassword": "string", "newPassword": "string" }`
- **Response**: `{ "success": true, "message": "string" }`

### 6. Refresh Token
- **POST** `/api/auth/refresh`
- **Body**: `{ "refreshToken": "string" }`
- **Response**: `{ "token": "string", "refreshToken": "string" }`

### 7. Đăng xuất
- **POST** `/api/auth/logout`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "success": true }`

---

## 👤 User Profile Endpoints

### 8. Lấy thông tin người dùng
- **GET** `/api/users/profile`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "id": "string", "fullName": "string", "phoneNumber": "string", "email": "string", "dateOfBirth": "string", "membershipLevel": "string", "joinDate": "string", "totalVisits": number, "favoriteGame": "string" }`

### 9. Cập nhật thông tin người dùng
- **PUT** `/api/users/profile`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "fullName": "string", "email": "string", "dateOfBirth": "string" }`
- **Response**: `{ "success": true, "user": {...} }`

### 10. Upload ảnh đại diện
- **POST** `/api/users/avatar`
- **Headers**: `Authorization: Bearer {token}`, `Content-Type: multipart/form-data`
- **Body**: `FormData { file: File }`
- **Response**: `{ "avatarUrl": "string" }`

---

## 💰 Balance & Wallet Endpoints

### 11. Lấy số dư hiện tại
- **GET** `/api/wallet/balance`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "balance": number, "points": number, "membershipTier": "string" }`

### 12. Lấy lịch sử giao dịch
- **GET** `/api/wallet/transactions`
- **Headers**: `Authorization: Bearer {token}`
- **Query Params**: `?page=number&limit=number&type=string`
- **Response**: `{ "transactions": [...], "total": number, "page": number }`

### 13. Nạp tiền (Tạo yêu cầu)
- **POST** `/api/wallet/top-up`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "amount": number, "paymentMethod": "string" }`
- **Response**: `{ "transactionId": "string", "paymentUrl": "string", "qrCode": "string" }`

### 14. Xác nhận nạp tiền
- **POST** `/api/wallet/top-up/confirm`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "transactionId": "string", "paymentReference": "string" }`
- **Response**: `{ "success": true, "newBalance": number }`

### 15. Lịch sử nạp tiền
- **GET** `/api/wallet/top-up/history`
- **Headers**: `Authorization: Bearer {token}`
- **Query Params**: `?page=number&limit=number`
- **Response**: `{ "topUps": [...], "total": number }`

---

## 🎮 Game Endpoints

### 16. Lấy danh sách game
- **GET** `/api/games`
- **Query Params**: `?page=number&limit=number&search=string&type=string&riskLevel=string`
- **Response**: `{ "games": [...], "total": number, "page": number }`

### 17. Lấy chi tiết game
- **GET** `/api/games/{gameId}`
- **Response**: `{ "id": "string", "name": "string", "description": "string", "shortDescription": "string", "pricePerTurn": number, "discount": number, "ageRange": "string", "heightRequirement": "string", "location": "string", "type": "string", "riskLevel": "string", "rating": number, "totalRatings": number }`

### 18. Tìm kiếm game
- **GET** `/api/games/search`
- **Query Params**: `?q=string&page=number&limit=number`
- **Response**: `{ "games": [...], "total": number }`

### 19. Lấy game phổ biến
- **GET** `/api/games/popular`
- **Query Params**: `?limit=number`
- **Response**: `{ "games": [...] }`

### 20. Lấy game theo danh mục
- **GET** `/api/games/category/{categoryId}`
- **Query Params**: `?page=number&limit=number`
- **Response**: `{ "games": [...], "total": number }`

---

## 🛒 Cart & Checkout Endpoints

### 21. Lấy giỏ hàng
- **GET** `/api/cart`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "items": [...], "subtotal": number, "totalSaved": number, "voucherDiscount": number, "finalTotal": number }`

### 22. Thêm vào giỏ hàng
- **POST** `/api/cart/items`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "gameId": "string", "quantity": number }`
- **Response**: `{ "success": true, "cart": {...} }`

### 23. Cập nhật số lượng
- **PUT** `/api/cart/items/{itemId}`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "quantity": number }`
- **Response**: `{ "success": true, "cart": {...} }`

### 24. Xóa khỏi giỏ hàng
- **DELETE** `/api/cart/items/{itemId}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "success": true, "cart": {...} }`

### 25. Xóa toàn bộ giỏ hàng
- **DELETE** `/api/cart`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "success": true }`

### 26. Áp dụng voucher vào giỏ hàng
- **POST** `/api/cart/apply-voucher`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "voucherId": "string" }`
- **Response**: `{ "success": true, "discount": number, "cart": {...} }`

### 27. Xóa voucher khỏi giỏ hàng
- **DELETE** `/api/cart/voucher`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "success": true, "cart": {...} }`

### 28. Thanh toán (Checkout)
- **POST** `/api/cart/checkout`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "paymentMethod": "string", "voucherId": "string" }`
- **Response**: `{ "orderId": "string", "success": true, "newBalance": number }`

---

## 💳 Payment Endpoints

### 29. Lấy lịch sử thanh toán
- **GET** `/api/payments/history`
- **Headers**: `Authorization: Bearer {token}`
- **Query Params**: `?page=number&limit=number&status=string`
- **Response**: `{ "payments": [...], "total": number }`

### 30. Lấy chi tiết thanh toán
- **GET** `/api/payments/{paymentId}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "id": "string", "method": "string", "amount": number, "status": "string", "timestamp": "string", "transactionId": "string", "orderDetails": {...} }`

### 31. Kiểm tra trạng thái thanh toán
- **GET** `/api/payments/{paymentId}/status`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "status": "string", "paidAt": "string" }`

### 32. Hoàn tiền
- **POST** `/api/payments/{paymentId}/refund`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "reason": "string" }`
- **Response**: `{ "refundId": "string", "success": true }`

---

## 🎟️ Voucher Endpoints

### 33. Lấy danh sách voucher khả dụng
- **GET** `/api/vouchers`
- **Query Params**: `?category=string&page=number&limit=number`
- **Response**: `{ "vouchers": [...], "total": number }`

### 34. Lấy voucher của người dùng
- **GET** `/api/vouchers/my-vouchers`
- **Headers**: `Authorization: Bearer {token}`
- **Query Params**: `?status=string&page=number&limit=number`
- **Response**: `{ "vouchers": [...], "total": number }`

### 35. Nhận voucher
- **POST** `/api/vouchers/{voucherId}/claim`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "success": true, "voucher": {...} }`

### 36. Lưu voucher
- **POST** `/api/vouchers/{voucherId}/save`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "success": true }`

### 37. Lấy chi tiết voucher
- **GET** `/api/vouchers/{voucherId}`
- **Response**: `{ "id": "string", "title": "string", "description": "string", "discountType": "string", "value": number, "minPurchase": number, "expiryDate": "string", "category": "string" }`

---

## 🎴 Member Card Endpoints

### 38. Lấy thông tin thẻ thành viên
- **GET** `/api/member-card`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "cardNumber": "string", "status": "string", "activatedAt": "string", "points": number, "membershipTier": {...} }`

### 39. Kích hoạt thẻ mới
- **POST** `/api/member-card/activate`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "cardId": "string" }` (NFC card ID)
- **Response**: `{ "success": true, "card": {...} }`

### 40. Khóa thẻ
- **POST** `/api/member-card/lock`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "cardId": "string", "reason": "string" }`
- **Response**: `{ "success": true, "message": "string" }`

### 41. Mở khóa thẻ
- **POST** `/api/member-card/unlock`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "cardId": "string" }`
- **Response**: `{ "success": true }`

### 42. Lấy thông tin hạng thành viên
- **GET** `/api/member-card/tiers`
- **Response**: `{ "tiers": [...], "currentTier": {...}, "progress": {...} }`

### 43. Lấy tiến trình thành viên
- **GET** `/api/member-card/progress`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "currentPoints": number, "currentTier": {...}, "nextTier": {...}, "progressToNext": number }`

---

## 📱 Notification Endpoints

### 44. Lấy danh sách thông báo
- **GET** `/api/notifications`
- **Headers**: `Authorization: Bearer {token}`
- **Query Params**: `?page=number&limit=number&unreadOnly=boolean`
- **Response**: `{ "notifications": [...], "unreadCount": number, "total": number }`

### 45. Đánh dấu đã đọc
- **PUT** `/api/notifications/{notificationId}/read`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "success": true }`

### 46. Đánh dấu tất cả đã đọc
- **PUT** `/api/notifications/read-all`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "success": true, "markedCount": number }`

### 47. Xóa thông báo
- **DELETE** `/api/notifications/{notificationId}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "success": true }`

### 48. Lấy số lượng thông báo chưa đọc
- **GET** `/api/notifications/unread-count`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "count": number }`

---

## 🎁 Referral Endpoints

### 49. Lấy mã giới thiệu
- **GET** `/api/referral/code`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "code": "string", "totalReferrals": number, "totalReward": number }`

### 50. Áp dụng mã giới thiệu
- **POST** `/api/referral/apply`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "referralCode": "string" }`
- **Response**: `{ "success": true, "reward": number }`

### 51. Lấy lịch sử giới thiệu
- **GET** `/api/referral/history`
- **Headers**: `Authorization: Bearer {token}`
- **Query Params**: `?page=number&limit=number`
- **Response**: `{ "referrals": [...], "total": number }`

---

## 💬 Support Chat Endpoints

### 52. Lấy danh sách tin nhắn
- **GET** `/api/support/chat/messages`
- **Headers**: `Authorization: Bearer {token}`
- **Query Params**: `?page=number&limit=number`
- **Response**: `{ "messages": [...], "total": number }`

### 53. Gửi tin nhắn
- **POST** `/api/support/chat/messages`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "content": "string", "attachments": [...] }`
- **Response**: `{ "messageId": "string", "sentAt": "string" }`

### 54. Đánh dấu đã đọc tin nhắn
- **PUT** `/api/support/chat/messages/{messageId}/read`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "success": true }`

### 55. Tạo ticket hỗ trợ
- **POST** `/api/support/tickets`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "subject": "string", "category": "string", "description": "string" }`
- **Response**: `{ "ticketId": "string", "createdAt": "string" }`

---

## 📊 Usage History Endpoints

### 56. Lấy lịch sử sử dụng
- **GET** `/api/usage/history`
- **Headers**: `Authorization: Bearer {token}`
- **Query Params**: `?page=number&limit=number&gameId=string&dateFrom=string&dateTo=string`
- **Response**: `{ "history": [...], "total": number }`

### 57. Lấy thống kê sử dụng
- **GET** `/api/usage/statistics`
- **Headers**: `Authorization: Bearer {token}`
- **Query Params**: `?period=string` (daily/weekly/monthly/yearly)
- **Response**: `{ "totalGames": number, "totalSpent": number, "favoriteGame": {...}, "visits": number }`

---

## 🔒 Card Info Endpoints

### 58. Lấy thông tin thẻ
- **GET** `/api/card/info`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "cardNumber": "string", "cardHolder": "string", "expiryDate": "string", "status": "string", "balance": number }`

### 59. Cập nhật thông tin thẻ
- **PUT** `/api/card/info`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: `{ "cardHolder": "string" }`
- **Response**: `{ "success": true, "card": {...} }`

---

## 📈 Statistics & Analytics Endpoints

### 60. Lấy thống kê tổng quan
- **GET** `/api/statistics/overview`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `{ "totalSpent": number, "totalVisits": number, "favoriteGames": [...], "membershipProgress": {...} }`

---

## 📝 Notes

- Tất cả các endpoint yêu cầu authentication sẽ cần header `Authorization: Bearer {token}`
- Các endpoint có thể có thêm query parameters cho pagination (`page`, `limit`)
- Response format có thể khác nhau tùy theo implementation thực tế
- Một số endpoint có thể cần thêm validation và error handling
- Các endpoint liên quan đến payment có thể tích hợp với các gateway như MoMo, ZaloPay, Banking

---

**Tổng cộng: 60 endpoints**

