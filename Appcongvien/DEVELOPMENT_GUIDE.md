# Park Adventure - Android Development Guide

> Tài liệu này dành cho AI (hoặc developer) tiếp tục phát triển dự án.
> Đọc kỹ trước khi bắt đầu làm thêm tính năng để đảm bảo đúng cấu trúc.

---

## 1. Tech Stack

| Thành phần       | Thư viện / Công nghệ                            |
|------------------|-------------------------------------------------|
| UI               | Jetpack Compose + Material3                     |
| Navigation       | Navigation Compose 2.8.4                        |
| Architecture     | MVVM (ViewModel + StateFlow + Repository)       |
| Network          | Retrofit 2.9.0 + OkHttp 4.12.0                 |
| JSON             | Gson 2.10.1                                     |
| Coroutines       | kotlinx-coroutines-android 1.7.3               |
| State            | StateFlow (không dùng LiveData)                 |
| Auth Storage     | SharedPreferences (TokenManager)                |
| DI               | Manual / Service Locator qua `App.kt`           |
| Backend          | Ktor (Kotlin), REST API trên localhost:8080     |

---

## 2. Cấu trúc thư mục đầy đủ

```
Appcongvien/app/src/main/java/com/example/appcongvien/
│
├── App.kt                          ← Application class, khởi tạo toàn bộ dependencies
├── MainActivity.kt                 ← Entry point, xác định startDestination
│
├── data/
│   ├── local/
│   │   └── TokenManager.kt         ← Lưu/đọc JWT token & user info (SharedPrefs)
│   │
│   ├── model/                      ← Data classes: request body & response DTO
│   │   ├── ApiResponse.kt          ← ApiResponse<T>, PaginatedData<T>, Resource<T>
│   │   ├── AuthModels.kt           ← RegisterRequest, LoginRequest, UserDTO, AuthData
│   │   ├── GameModels.kt           ← GameDTO, GameReviewDTO, CreateReviewRequest
│   │   ├── CardModels.kt           ← CardDTO, LinkCardRequest, BlockCardRequest
│   │   ├── WalletModels.kt         ← WalletBalanceDTO, TransactionDTO, TopUpRequest
│   │   ├── VoucherModels.kt        ← VoucherDTO, UserVoucherDTO
│   │   ├── OrderModels.kt          ← OrderDTO, TicketDTO, CreateOrderRequest
│   │   ├── NotificationModels.kt   ← NotificationDTO, UnreadCountDTO
│   │   └── SupportModels.kt        ← SupportMessageDTO, SendMessageRequest
│   │
│   ├── network/
│   │   ├── ApiService.kt           ← Retrofit interface: tất cả ~55 API endpoints
│   │   ├── RetrofitClient.kt       ← Singleton Retrofit + OkHttp builder
│   │   └── AuthInterceptor.kt      ← Tự động gắn "Authorization: Bearer <token>"
│   │
│   └── repository/
│       ├── AuthRepository.kt       ← login, register, getUserProfile, logout
│       ├── GameRepository.kt       ← getGames, getFeatured, getDetail, reviews
│       ├── CardRepository.kt       ← getMyCards, linkCard, blockCard, unblockCard
│       ├── WalletRepository.kt     ← getBalance, topUp, getTransactions
│       ├── VoucherRepository.kt    ← getVouchers, claimVoucher, getMyVouchers
│       ├── OrderRepository.kt      ← createOrder, getOrders, cancelOrder, tickets
│       ├── NotificationRepository.kt ← getNotifications, markAsRead
│       └── SupportRepository.kt   ← getMessages, sendMessage
│
├── viewmodel/
│   ├── AuthViewModel.kt            ← loginState, registerState, profileState
│   ├── GameViewModel.kt            ← gamesState, featuredGamesState, gameDetailState
│   ├── CardViewModel.kt            ← cardsState, linkCardState, blockCardState
│   ├── WalletViewModel.kt          ← balanceState, topUpState, transactionsState
│   ├── VoucherViewModel.kt         ← vouchersState, myVouchersState, claimState
│   ├── OrderViewModel.kt           ← createOrderState, ordersState, ticketsState
│   ├── NotificationViewModel.kt    ← notificationsState, unreadCount
│   └── SupportViewModel.kt         ← messagesState, sendState
│
├── navigation/
│   └── NavGraph.kt                 ← AppNavGraph composable, Screen sealed class
│
├── components/                     ← Reusable UI components
│   ├── BottomBar.kt
│   ├── CardSection.kt
│   ├── Carousel.kt
│   ├── HeaderSection.kt
│   ├── QuickActions.kt
│   └── ServicesSection.kt
│
├── screen/
│   ├── auth/
│   │   ├── LoginScreen.kt          ← DONE: kết nối AuthViewModel
│   │   ├── RegisterScreen.kt       ← UI sẵn, CHƯA kết nối ViewModel
│   │   ├── ForgotPasswordScreen.kt ← UI sẵn, CHƯA kết nối ViewModel
│   │   └── ChangePasswordScreen.kt ← UI sẵn, CHƯA kết nối ViewModel
│   │
│   ├── HomeScreen.kt               ← UI sẵn, CHƯA kết nối (dữ liệu tĩnh)
│   ├── GameListScreen.kt           ← UI sẵn, CHƯA kết nối GameViewModel
│   ├── GameDetailScreen.kt         ← UI sẵn, CHƯA kết nối GameViewModel
│   ├── VouchersScreen.kt           ← UI sẵn, CHƯA kết nối VoucherViewModel
│   ├── VoucherWalletScreen.kt      ← UI sẵn, CHƯA kết nối VoucherViewModel
│   ├── CardInfoScreen.kt           ← UI sẵn, CHƯA kết nối CardViewModel
│   ├── LockCardScreen.kt           ← UI sẵn, CHƯA kết nối CardViewModel
│   ├── BalanceScreen.kt            ← UI sẵn, CHƯA kết nối WalletViewModel
│   ├── TopUpScreen.kt              ← UI sẵn, CHƯA kết nối WalletViewModel
│   ├── PaymentScreen.kt            ← UI sẵn, CHƯA kết nối OrderViewModel
│   ├── NotificationsScreen.kt      ← UI sẵn, CHƯA kết nối NotificationViewModel
│   ├── SupportChatScreen.kt        ← UI sẵn, CHƯA kết nối SupportViewModel
│   ├── ProfileScreen.kt            ← UI sẵn, CHƯA kết nối AuthViewModel
│   ├── SettingsScreen.kt           ← UI sẵn, CHƯA có logic logout
│   ├── PaymentHistoryScreen.kt     ← UI sẵn, CHƯA kết nối WalletViewModel
│   ├── UsageHistoryScreen.kt       ← UI sẵn, CHƯA kết nối OrderViewModel
│   ├── MemberCardScreen.kt         ← UI sẵn, CHƯA kết nối
│   └── ReferralCodeScreen.kt       ← UI sẵn, CHƯA kết nối
│
├── data/
│   └── Models.kt                   ← Data class cũ (Voucher, DiscountType) - có thể bỏ
│
└── ui/theme/
    ├── AppColors.kt                ← Tất cả màu dùng trong app (dùng AppColors.XYZ)
    ├── Theme.kt
    └── Type.kt
```

---

## 3. Backend API

- **Base URL (Emulator):** `http://10.0.2.2:8080/`
- **Base URL (Thiết bị thật):** `http://<IP_LAN_máy>:8080/`
- **Authentication:** `Authorization: Bearer <JWT_TOKEN>`
- **File cấu hình:** `data/network/RetrofitClient.kt` → dòng `BASE_URL`

### Nhóm API endpoint chính:
```
/api/auth/register   POST  - Đăng ký
/api/auth/login      POST  - Đăng nhập
/api/user/profile    GET   - Thông tin user (cần token)
/api/games           GET   - Danh sách game
/api/games/{id}      GET   - Chi tiết game
/api/cards           GET   - Danh sách thẻ
/api/wallet/balance  GET   - Số dư ví
/api/wallet/topup    POST  - Nạp tiền
/api/vouchers        GET   - Danh sách voucher
/api/orders          POST  - Tạo đơn hàng
/api/notifications   GET   - Thông báo
/api/support/messages GET/POST - Chat hỗ trợ
```

---

## 4. Patterns cốt lõi

### 4.1. Resource sealed class (xử lý trạng thái API)

Mọi kết quả từ Repository đều trả về `Resource<T>`:

```kotlin
// data/model/ApiResponse.kt
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int = -1) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
```

**Trong ViewModel:**
```kotlin
private val _someState = MutableStateFlow<Resource<MyDTO>?>(null)
val someState: StateFlow<Resource<MyDTO>?> = _someState

fun loadData() {
    viewModelScope.launch {
        _someState.value = Resource.Loading
        _someState.value = repository.getData()
    }
}
```

**Trong Screen:**
```kotlin
val state by viewModel.someState.collectAsState()

when (val s = state) {
    is Resource.Loading -> CircularProgressIndicator()
    is Resource.Success -> { /* dùng s.data */ }
    is Resource.Error   -> Text(s.message)
    null -> { /* idle, chưa gọi */ }
}
```

### 4.2. Lấy ViewModel trong Composable (không dùng Hilt)

```kotlin
@Composable
fun SomeScreen(...) {
    val app = LocalContext.current.applicationContext as App
    val viewModel: XxxViewModel = viewModel(
        factory = XxxViewModel.Factory(app.xxxRepository)
    )
    ...
}
```

Mỗi ViewModel có inner class `Factory`:
```kotlin
class XxxViewModel(private val repository: XxxRepository) : ViewModel() {
    // ...
    class Factory(private val repository: XxxRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return XxxViewModel(repository) as T
        }
    }
}
```

### 4.3. Sử dụng LaunchedEffect để xử lý side effect

Khi cần navigate hoặc show snackbar sau khi state thay đổi:
```kotlin
LaunchedEffect(state) {
    when (val s = state) {
        is Resource.Success -> {
            viewModel.resetXxxState() // reset về null
            onSuccess()               // navigate hoặc callback
        }
        is Resource.Error -> {
            errorMessage = s.message  // hiển thị lỗi
        }
        else -> {}
    }
}
```

### 4.4. Thêm repository mới vào App

```kotlin
// App.kt - thêm vào cuối onCreate()
lateinit var newRepository: NewRepository
    private set

override fun onCreate() {
    super.onCreate()
    // ... code cũ ...
    newRepository = NewRepository(apiService)
}
```

---

## 5. Quy trình thêm tính năng mới (checklist)

Ví dụ thêm tính năng **"Xem lịch sử thanh toán"**:

### Bước 1: Thêm endpoint vào `ApiService.kt` (nếu chưa có)
```kotlin
@GET("api/wallet/payments")
suspend fun getPayments(
    @Query("page") page: Int = 1,
    @Query("size") size: Int = 10
): Response<ApiResponse<PaginatedData<PaymentRecordDTO>>>
```

### Bước 2: Thêm/kiểm tra model trong `data/model/`
```kotlin
data class PaymentRecordDTO(
    val paymentId: String,
    val amount: String,
    val method: String,
    val status: String,
    val createdAt: String
)
```

### Bước 3: Thêm hàm vào Repository tương ứng
```kotlin
// data/repository/WalletRepository.kt
suspend fun getPayments(page: Int = 1, size: Int = 10): Resource<PaginatedData<PaymentRecordDTO>> {
    return try {
        val response = apiService.getPayments(page, size)
        if (response.isSuccessful && response.body()?.success == true) {
            Resource.Success(response.body()!!.data!!)
        } else {
            Resource.Error(response.body()?.message ?: "Không thể tải lịch sử")
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Lỗi kết nối")
    }
}
```

### Bước 4: Thêm state và hàm vào ViewModel
```kotlin
// viewmodel/WalletViewModel.kt
private val _paymentsState = MutableStateFlow<Resource<PaginatedData<PaymentRecordDTO>>?>(null)
val paymentsState: StateFlow<Resource<PaginatedData<PaymentRecordDTO>>?> = _paymentsState

fun loadPayments(page: Int = 1) {
    viewModelScope.launch {
        _paymentsState.value = Resource.Loading
        _paymentsState.value = walletRepository.getPayments(page)
    }
}
```

### Bước 5: Kết nối trong Screen
```kotlin
// screen/PaymentHistoryScreen.kt
@Composable
fun PaymentHistoryScreen(onBackClick: () -> Unit = {}) {
    val app = LocalContext.current.applicationContext as App
    val viewModel: WalletViewModel = viewModel(
        factory = WalletViewModel.Factory(app.walletRepository)
    )
    val paymentsState by viewModel.paymentsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPayments()
    }

    when (val state = paymentsState) {
        is Resource.Loading -> CircularProgressIndicator()
        is Resource.Success -> { /* hiển thị state.data.items */ }
        is Resource.Error   -> Text(state.message)
        null -> {}
    }
}
```

---

## 6. Design System

### Colors (dùng `AppColors.XYZ`)
```kotlin
// Màu chủ đạo:
AppColors.WarmOrange        // #E55722 - màu chính của app
AppColors.WarmOrangeLight   // #FF7A00 - gradient sáng hơn
AppColors.WarmOrangeSoft    // #FFE4D6 - background nhạt

// Text:
AppColors.PrimaryDark       // #1A1A1A - text chính
AppColors.PrimaryGray       // #6B7280 - text phụ

// Backgrounds:
AppColors.SurfaceLight      // #F8F9FA - nền trang
AppColors.SurfaceWhite      // #FFFFFF

// Card/Header gradient:
AppColors.CardGrad1         // #374151
AppColors.CardGrad2         // #4B5563
AppColors.HeaderGrad1/2/3   // Cam từ nhạt sang đậm
```

### OutlinedTextField chuẩn
```kotlin
OutlinedTextField(
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.WarmOrange,
        focusedLabelColor = AppColors.WarmOrange,
        cursorColor = AppColors.WarmOrange,
        errorBorderColor = Color.Red
    )
)
```

### Button chuẩn
```kotlin
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = AppColors.WarmOrange,
        contentColor = Color.White,
        disabledContainerColor = AppColors.WarmOrange.copy(alpha = 0.6f),
        disabledContentColor = Color.White
    ),
    shape = RoundedCornerShape(12.dp)
)
```

### TopAppBar chuẩn
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
TopAppBar(
    title = { Text("Tiêu đề", fontWeight = FontWeight.Bold, color = Color.White) },
    navigationIcon = {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
        }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.WarmOrange)
)
```

---

## 7. Navigation

### Screen routes (sealed class trong NavGraph.kt)
```
login, register, forgot_password, change_password
home, profile, settings
card_info, lock_card
balance, top_up, check_out, payment_history, usage_history
game_list, game_detail/{gameId}, vouchers, voucher_wallet
member_card, referral_code, support_chat, notifications
```

### Navigate forward
```kotlin
navController.navigate(Screen.SomeScreen.route)
```

### Navigate và clear back stack (sau login/register)
```kotlin
navController.navigate(Screen.Home.route) {
    popUpTo(0) { inclusive = true }
}
```

### Navigate với argument
```kotlin
// Khai báo route: "game_detail/{gameId}"
navController.navigate(Screen.GameDetail.createRoute(gameId))
```

### Thêm route mới vào NavGraph.kt
```kotlin
composable(Screen.NewScreen.route) {
    NewScreen(
        onBackClick = { navController.popBackStack() }
    )
}
```

---

## 8. BottomBar và showBottomBar

Các route hiển thị BottomBar (định nghĩa trong `MainActivity.kt`):
```kotlin
val showBottomBar = currentRoute in listOf(
    Screen.Home.route,
    Screen.Vouchers.route, Screen.VoucherWallet.route,
    Screen.Profile.route, Screen.Settings.route, Screen.MemberCard.route,
    Screen.CardInfo.route
)
```
Để thêm màn hình vào BottomBar, thêm route vào list này.

---

## 9. Trạng thái hiện tại của từng màn hình

| Screen                  | UI  | ViewModel kết nối | Ghi chú                            |
|-------------------------|-----|-------------------|------------------------------------|
| LoginScreen             | ✅  | ✅ AuthViewModel   | Hoàn chỉnh                         |
| RegisterScreen          | ✅  | ❌                | Cần kết nối AuthViewModel          |
| ForgotPasswordScreen    | ✅  | ❌                | Chưa có API forgot password        |
| ChangePasswordScreen    | ✅  | ❌                | Cần kết nối AuthViewModel          |
| HomeScreen              | ✅  | ❌                | Cần AuthViewModel (balance, name)  |
| GameListScreen          | ✅  | ❌                | Cần GameViewModel                  |
| GameDetailScreen        | ✅  | ❌                | Cần GameViewModel                  |
| VouchersScreen          | ✅  | ❌                | Cần VoucherViewModel               |
| VoucherWalletScreen     | ✅  | ❌                | Cần VoucherViewModel               |
| CardInfoScreen          | ✅  | ❌                | Cần CardViewModel                  |
| LockCardScreen          | ✅  | ❌                | Cần CardViewModel                  |
| BalanceScreen           | ✅  | ❌                | Cần WalletViewModel                |
| TopUpScreen             | ✅  | ❌                | Cần WalletViewModel                |
| PaymentScreen (Checkout)| ✅  | ❌                | Cần OrderViewModel                 |
| NotificationsScreen     | ✅  | ❌                | Cần NotificationViewModel          |
| SupportChatScreen       | ✅  | ❌                | Cần SupportViewModel               |
| ProfileScreen           | ✅  | ❌                | Cần AuthViewModel                  |
| SettingsScreen          | ✅  | ❌                | Cần AuthViewModel (logout)         |
| PaymentHistoryScreen    | ✅  | ❌                | Cần WalletViewModel                |
| UsageHistoryScreen      | ✅  | ❌                | Cần OrderViewModel                 |
| MemberCardScreen        | ✅  | ❌                | Dữ liệu từ AuthViewModel/profile   |
| ReferralCodeScreen      | ✅  | ❌                | referralCode từ UserDTO            |

---

## 10. TokenManager - các hàm có sẵn

```kotlin
// Lấy instance
val tokenManager = (context.applicationContext as App).tokenManager

tokenManager.getToken()           // JWT token (nullable String)
tokenManager.hasToken()           // Boolean - đã đăng nhập chưa
tokenManager.getUserId()          // String?
tokenManager.getFullName()        // String?
tokenManager.getPhone()           // String?
tokenManager.getBalance()         // String? - số dư hiện tại (cache local)
tokenManager.getRole()            // "USER" | "ADMIN"
tokenManager.updateBalance(str)   // Cập nhật balance cache
tokenManager.clear()              // Xóa toàn bộ (logout)
```

---

## 11. Logout pattern

```kotlin
// Trong SettingsScreen hoặc ProfileScreen:
val app = LocalContext.current.applicationContext as App
val authViewModel: AuthViewModel = viewModel(
    factory = AuthViewModel.Factory(app.authRepository)
)

// Khi user bấm Logout:
authViewModel.logout()
navController.navigate(Screen.Login.route) {
    popUpTo(0) { inclusive = true }
}
```

---

## 12. Xử lý lỗi 401 (token hết hạn)

Hiện tại chưa có auto-redirect khi token hết hạn. Nếu cần thêm:
1. Tạo `UnauthorizedInterceptor` trong `data/network/`
2. Intercept response code 401
3. Gọi `tokenManager.clear()` và broadcast event để navigate về Login

---

## 13. Quy ước đặt tên

| Loại              | Quy ước                         | Ví dụ                          |
|-------------------|---------------------------------|--------------------------------|
| Screen file       | `XxxScreen.kt`                  | `GameDetailScreen.kt`          |
| ViewModel file    | `XxxViewModel.kt`               | `GameViewModel.kt`             |
| Repository file   | `XxxRepository.kt`              | `GameRepository.kt`            |
| Model file        | `XxxModels.kt`                  | `GameModels.kt`                |
| ViewModel state   | `_xxxState` (private MutableSF) | `_gameDetailState`             |
| ViewModel state   | `xxxState` (public SF)          | `gameDetailState`              |
| Reset hàm         | `resetXxxState()`               | `resetCreateReviewState()`     |
| Load hàm          | `loadXxx()` hoặc `getXxx()`     | `loadGames()`, `loadBalance()` |

---

## 14. Import cần thiết thường dùng

```kotlin
// ViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Compose
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

// App & ViewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.Resource
```

---

## 15. Cấu trúc file Screen chuẩn (template)

```kotlin
package com.example.appcongvien.screen

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.viewmodel.XxxViewModel

@Composable
fun XxxScreen(
    onBackClick: () -> Unit = {}
    // thêm các callback navigation cần thiết
) {
    // 1. Lấy ViewModel
    val app = LocalContext.current.applicationContext as App
    val viewModel: XxxViewModel = viewModel(
        factory = XxxViewModel.Factory(app.xxxRepository)
    )

    // 2. Observe state
    val dataState by viewModel.dataState.collectAsState()

    // 3. Load data khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // 4. Xử lý side effects (navigate, show error)
    LaunchedEffect(dataState) {
        when (val state = dataState) {
            is Resource.Success -> { /* xử lý thành công */ }
            is Resource.Error   -> { /* xử lý lỗi */ }
            else -> {}
        }
    }

    // 5. UI
    when (val state = dataState) {
        is Resource.Loading -> { /* show loading */ }
        is Resource.Success -> { /* show data: state.data */ }
        is Resource.Error   -> { /* show error: state.message */ }
        null -> { /* idle state */ }
    }
}
```
