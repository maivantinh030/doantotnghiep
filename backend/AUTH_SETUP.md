# Park Adventure - Authentication Setup Guide

## 📋 Tổng quan

Hệ thống đăng ký và đăng nhập đã được tích hợp hoàn chỉnh với các tính năng:

- ✅ Đăng ký tài khoản với số điện thoại
- ✅ Đăng nhập với số điện thoại và mật khẩu
- ✅ Mã hóa mật khẩu bằng BCrypt
- ✅ Xác thực JWT (JSON Web Token)
- ✅ Validation dữ liệu đầu vào
- ✅ Hệ thống mã giới thiệu (referral code)

## 🏗️ Cấu trúc code đã tạo

```
src/main/kotlin/
├── models/
│   └── AuthModels.kt              # Data classes cho request/response
├── database/
│   └── tables/
│       ├── Accounts.kt            # Table accounts
│       └── Users.kt               # Table users
├── services/
│   └── AuthService.kt             # Business logic (register, login, JWT)
├── routes/
│   └── AuthRoutes.kt              # API endpoints
└── plugins/
    ├── Security.kt                # JWT configuration (đã cập nhật)
    └── Routing.kt                 # Route registration (đã cập nhật)
```

## 🚀 Cách sử dụng

### 1. Đảm bảo database đã được tạo

Chạy file SQL schema để tạo database và tables:

```sql
-- File: database_schema.sql
CREATE DATABASE IF NOT EXISTS park_adventure;
USE park_adventure;
-- ... (chạy toàn bộ schema)
```

### 2. Cấu hình database connection

Kiểm tra file `src/main/resources/application.conf` hoặc `application.yml`:

```yaml
datasource:
  url: jdbc:mysql://localhost:3306/park_adventure
  user: root
  password: your_password
```

### 3. Chạy ứng dụng

```bash
./gradlew run
```

Server sẽ chạy tại: `http://localhost:8080`

## 📡 API Endpoints

### 1. Health Check

```http
GET /api/auth/health
```

**Response:**
```json
{
  "status": "OK",
  "service": "Park Adventure Auth API",
  "timestamp": 1234567890
}
```

### 2. Đăng ký tài khoản

```http
POST /api/auth/register
Content-Type: application/json

{
  "phoneNumber": "0987654321",
  "password": "password123",
  "fullName": "Nguyễn Văn A",
  "email": "nguyenvana@example.com",
  "dateOfBirth": "1995-05-15",
  "gender": "MALE",
  "referralCode": "ABC12345"  // Optional
}
```

**Response (Success - 201 Created):**
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "userId": "uuid-here",
      "accountId": "uuid-here",
      "phoneNumber": "0987654321",
      "fullName": "Nguyễn Văn A",
      "email": "nguyenvana@example.com",
      "role": "USER",
      "membershipLevel": "BRONZE",
      "currentBalance": "0.00",
      "loyaltyPoints": 0,
      "avatarUrl": null
    }
  }
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "success": false,
  "message": "Số điện thoại đã được sử dụng"
}
```

### 3. Đăng nhập

```http
POST /api/auth/login
Content-Type: application/json

{
  "phoneNumber": "0987654321",
  "password": "password123"
}
```

**Response (Success - 200 OK):**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "userId": "uuid-here",
      "accountId": "uuid-here",
      "phoneNumber": "0987654321",
      "fullName": "Nguyễn Văn A",
      "email": "nguyenvana@example.com",
      "role": "USER",
      "membershipLevel": "BRONZE",
      "currentBalance": "0.00",
      "loyaltyPoints": 0,
      "avatarUrl": null
    }
  }
}
```

**Response (Error - 401 Unauthorized):**
```json
{
  "success": false,
  "message": "Số điện thoại hoặc mật khẩu không đúng"
}
```

## 🔐 Xác thực với JWT

Sau khi đăng nhập/đăng ký thành công, client nhận được JWT token. Sử dụng token này cho các API cần xác thực:

```http
GET /api/protected-endpoint
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Tạo protected route:

```kotlin
route("/api/protected") {
    authenticate("auth-jwt") {
        get("/profile") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
            // ... xử lý
        }
    }
}
```

## ✅ Validation Rules

### Phone Number
- Bắt buộc
- Định dạng: 10 số, bắt đầu bằng 0
- Ví dụ: `0987654321`

### Password
- Bắt buộc
- Tối thiểu 6 ký tự

### Email (Optional)
- Định dạng email hợp lệ
- Ví dụ: `user@example.com`

### Gender (Optional)
- Giá trị: `MALE`, `FEMALE`, `OTHER`

### Date of Birth (Optional)
- Định dạng: `YYYY-MM-DD`
- Ví dụ: `1995-05-15`

## 🧪 Testing với API_TESTING.http

Mở file `API_TESTING.http` trong IntelliJ IDEA hoặc VS Code (với REST Client extension) để test nhanh các API.

## 🔧 Các bước tiếp theo

1. ✅ Tạo API lấy thông tin profile
2. ✅ Tạo API cập nhật profile
3. ✅ Tạo API đổi mật khẩu
4. ✅ Tạo API quên mật khẩu (OTP)
5. ✅ Tạo API refresh token

## 📝 Notes

- JWT token có thời hạn 30 ngày
- Mỗi user sẽ có một mã giới thiệu (referral code) duy nhất
- Password được mã hóa bằng BCrypt
- JWT Secret hiện tại đang hardcode, nên move vào config file trong production
