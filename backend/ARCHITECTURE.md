# 🏗️ Park Adventure - Architecture Guide

## Tổng quan kiến trúc

Dự án sử dụng **Clean Architecture** với các layer rõ ràng:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│         (Routes/Controllers)            │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Business Layer                  │
│         (Services)                      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Data Access Layer               │
│         (Repositories)                  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Database                        │
│         (MySQL via Exposed)             │
└─────────────────────────────────────────┘
```

## 📁 Cấu trúc thư mục

```
src/main/kotlin/
├── entities/                    # Database entities
│   ├── Account.kt              # Entity cho bảng accounts
│   └── User.kt                 # Entity cho bảng users
│
├── dto/                        # Data Transfer Objects
│   ├── AccountDTO.kt           # DTOs cho Account
│   └── UserDTO.kt              # DTOs cho User
│
├── repositories/               # Data access layer
│   ├── AccountRepository.kt    # Interface + Implementation
│   └── UserRepository.kt       # Interface + Implementation
│
├── services/                   # Business logic layer
│   └── AuthService.kt          # Authentication service
│
├── routes/                     # API endpoints
│   ├── AuthRoutes.kt           # Public routes (register, login)
│   └── UserRoutes.kt           # Protected routes (profile)
│
├── models/                     # Request/Response models
│   └── AuthModels.kt           # RegisterRequest, LoginRequest, AuthResponse
│
├── database/                   # Database configuration
│   ├── DatabaseFactory.kt      # Database connection setup
│   └── tables/                 # Exposed table definitions
│       ├── Accounts.kt
│       └── Users.kt
│
└── plugins/                    # Ktor plugins
    ├── Routing.kt              # Route configuration
    ├── Security.kt             # JWT configuration
    ├── Serialization.kt        # JSON serialization
    └── HTTP.kt                 # CORS configuration
```

## 🎯 Các Pattern được sử dụng

### 1. **Repository Pattern**

**Tại sao?**
- Tách biệt business logic khỏi data access logic
- Dễ dàng test (mock repositories)
- Dễ thay đổi database implementation

**Cách hoạt động:**

```kotlin
// Interface - định nghĩa contract
interface IUserRepository {
    fun findById(userId: String): User?
    fun create(dto: CreateUserDTO): User
    // ...
}

// Implementation - chi tiết cụ thể
class UserRepository : IUserRepository {
    override fun findById(userId: String): User? {
        return transaction {
            Users.select { Users.userId eq userId }
                .singleOrNull()
                ?.let { mapRowToUser(it) }
        }
    }
}

// Service sử dụng interface, không phụ thuộc vào implementation
class AuthService(
    private val userRepository: IUserRepository = UserRepository()
) {
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findById(userId) // ✅ Clean
        // ...
    }
}
```

### 2. **DTO Pattern (Data Transfer Object)**

**Tại sao?**
- Tách biệt database entities với API responses
- Kiểm soát dữ liệu được expose ra bên ngoài
- Format dữ liệu phù hợp với client (String instead of BigDecimal, etc.)

**Cách hoạt động:**

```kotlin
// Entity - đại diện cho database record
data class User(
    val userId: String,
    val currentBalance: BigDecimal,  // Database type
    // ...
)

// DTO - dữ liệu trả về cho client
@Serializable
data class UserDTO(
    val userId: String,
    val currentBalance: String,      // String để tránh mất precision
    // ...
) {
    companion object {
        fun fromEntity(user: User, phoneNumber: String, role: String): UserDTO {
            return UserDTO(
                userId = user.userId,
                currentBalance = user.currentBalance.toString(),
                // ...
            )
        }
    }
}
```

### 3. **Dependency Injection**

Repositories được inject vào Services và Routes:

```kotlin
class AuthService(
    private val accountRepository: IAccountRepository = AccountRepository(),
    private val userRepository: IUserRepository = UserRepository()
)

fun Route.userRoutes(
    userRepository: IUserRepository = UserRepository(),
    accountRepository: IAccountRepository = AccountRepository()
)
```

**Lợi ích:**
- Dễ test với mock repositories
- Loose coupling giữa các components
- Dễ thay đổi implementation

## 🔄 Luồng xử lý request

### Ví dụ: User đăng ký

```
1. Client gửi POST /api/auth/register
   ↓
2. AuthRoutes nhận request → parse RegisterRequest
   ↓
3. AuthService.register(request)
   ├─→ Validate input
   ├─→ AccountRepository.existsByPhoneNumber() ❌ Đã tồn tại → Return error
   ├─→ UserRepository.existsByEmail()
   ├─→ AccountRepository.create(CreateAccountDTO) → Account entity
   ├─→ UserRepository.create(CreateUserDTO) → User entity
   ├─→ Generate JWT token
   └─→ UserDTO.fromEntity() → Convert entity → DTO
   ↓
4. Return AuthResponse với UserDTO
   ↓
5. Client nhận JSON response
```

## 📊 So sánh: Trước vs Sau

### ❌ Trước (Không có Repository/DTO)

```kotlin
// Service truy cập trực tiếp vào database
fun login(request: LoginRequest): AuthResponse {
    transaction {
        val account = Accounts.select { ... }.singleOrNull()  // ❌ Tight coupling
        val user = Users.select { ... }.singleOrNull()

        // ❌ Return dữ liệu trực tiếp từ database
        return AuthResponse(data = mapOf(
            "balance" to user[Users.currentBalance]  // BigDecimal
        ))
    }
}
```

**Vấn đề:**
- Service phụ thuộc vào database implementation
- Khó test (phải có database thật)
- Không kiểm soát được data format

### ✅ Sau (Với Repository/DTO)

```kotlin
// Service sử dụng repository interface
fun login(request: LoginRequest): AuthResponse {
    val account = accountRepository.findByPhoneNumber(request.phoneNumber)  // ✅ Clean
    val user = userRepository.findByAccountId(account.accountId)

    // ✅ Convert entity → DTO
    val userDTO = UserDTO.fromEntity(user, account.phoneNumber, account.role)

    return AuthResponse(data = AuthData(user = userDTO))
}
```

**Lợi ích:**
- ✅ Dễ test (mock repository)
- ✅ Clean code, dễ đọc
- ✅ Kiểm soát data format

## 🧪 Testing với Repository Pattern

```kotlin
// Mock repository cho unit test
class MockUserRepository : IUserRepository {
    private val users = mutableMapOf<String, User>()

    override fun findById(userId: String): User? = users[userId]
    override fun create(dto: CreateUserDTO): User {
        val user = User(...)
        users[user.userId] = user
        return user
    }
}

// Test service
@Test
fun testLogin() {
    val mockRepo = MockUserRepository()
    val authService = AuthService(userRepository = mockRepo)

    // Test logic mà không cần database thật
    val result = authService.login(...)
    // Assert...
}
```

## 📝 Best Practices

### 1. Repository Interface

✅ **DO:**
```kotlin
interface IUserRepository {
    fun findById(userId: String): User?
    fun create(dto: CreateUserDTO): User
}
```

❌ **DON'T:**
```kotlin
class UserRepository {
    // Không có interface → khó test, khó thay đổi implementation
}
```

### 2. DTO Mapping

✅ **DO:**
```kotlin
@Serializable
data class UserDTO(
    val currentBalance: String  // String để tránh mất precision
) {
    companion object {
        fun fromEntity(user: User): UserDTO { ... }
    }
}
```

❌ **DON'T:**
```kotlin
// Trả về entity trực tiếp
call.respond(user)  // ❌ Expose toàn bộ database fields
```

### 3. Service Layer

✅ **DO:**
```kotlin
class AuthService(
    private val userRepository: IUserRepository
) {
    fun register(request: RegisterRequest): AuthResponse {
        // Business logic
        val user = userRepository.create(...)
        return AuthResponse(...)
    }
}
```

❌ **DON'T:**
```kotlin
class AuthService {
    fun register(request: RegisterRequest): AuthResponse {
        transaction {
            Users.insert { ... }  // ❌ Direct database access
        }
    }
}
```

## 🎓 Kết luận

Architecture hiện tại:

✅ **Separation of Concerns** - Mỗi layer có trách nhiệm riêng
✅ **Testable** - Dễ dàng unit test với mock
✅ **Maintainable** - Dễ thay đổi và mở rộng
✅ **Clean Code** - Code dễ đọc, dễ hiểu
✅ **Scalable** - Dễ scale khi dự án lớn

Đây là foundation tốt để phát triển các tính năng tiếp theo! 🚀
