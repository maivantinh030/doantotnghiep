# Park Adventure - Hướng Dẫn Kiến Trúc & Pattern

## 1. Cấu Trúc Tổng Thể

```
testdoan/
├── appdesktop/          # Kotlin Compose Desktop (admin app)
├── backend/             # Ktor REST API server
├── Appcongvien/         # Mobile app (user side)
├── ParkCard/            # Smart card module
└── SmardCard/           # Smart card utilities
```

---

## 2. Cấu Trúc Desktop App (`appdesktop`)

```
com.park/
├── main.kt                          # Entry point (1280x800 Window)
├── App.kt                           # Root composable + navigation
├── data/
│   ├── model/Models.kt             # Data classes (DTOs) - @Serializable
│   ├── network/
│   │   ├── ApiClient.kt            # Ktor HTTP client singleton
│   │   └── SupportWebSocketClient.kt
│   └── repository/
│       ├── AuthRepository.kt
│       ├── UserManagementRepository.kt
│       ├── GameRepository.kt
│       └── ...                     # Mỗi feature có 1 repository
├── navigation/
│   └── AdminScreen.kt              # Enum: LOGIN, DASHBOARD, USERS, GAMES...
├── ui/
│   ├── screen/                     # 1 file per screen
│   ├── component/                  # Reusable components
│   └── theme/Theme.kt              # Colors, Typography
└── viewmodel/                      # 1 ViewModel per screen
```

---

## 3. API Client Pattern

**File:** `data/network/ApiClient.kt`

```kotlin
object ApiClient {
    private const val BASE_URL = "http://192.168.0.101:8080"
    private var token: String? = null

    val http = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
        defaultRequest {
            url(BASE_URL)
            token?.let { header("Authorization", "Bearer $it") }
            contentType(ContentType.Application.Json)
        }
    }

    fun setToken(newToken: String) { token = newToken }
    fun clearToken() { token = null }
}
```

**Lưu ý khi thêm feature mới:**
- Giữ `BASE_URL` trong `ApiClient`, không hardcode ở repository
- `setToken()` được gọi ngay sau khi login thành công

---

## 4. Response Wrapper

**File:** `data/model/Models.kt`

```kotlin
// Mọi API response đều wrap trong ApiResponse<T>
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

// Dữ liệu phân trang
@Serializable
data class PaginatedData<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val size: Int = 10,
    val totalPages: Int = 1
)
```

---

## 5. Repository Pattern

Mỗi feature có 1 repository. Tất cả đều follow cùng 1 template:

```kotlin
class XxxRepository {

    // Lấy danh sách (có phân trang)
    suspend fun getItems(page: Int = 1, size: Int = 20): Result<PaginatedData<XxxDTO>> {
        return try {
            val response = ApiClient.http.get("/api/admin/xxx") {
                parameter("page", page)
                parameter("size", size)
            }
            val body = response.body<ApiResponse<PaginatedData<XxxDTO>>>()
            if (body.success && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Tạo mới
    suspend fun createItem(request: CreateXxxRequest): Result<XxxDTO> {
        return try {
            val response = ApiClient.http.post("/api/admin/xxx") {
                setBody(request)
            }
            val body = response.body<ApiResponse<XxxDTO>>()
            if (body.success && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cập nhật
    suspend fun updateItem(id: Int, request: UpdateXxxRequest): Result<XxxDTO> {
        return try {
            val response = ApiClient.http.put("/api/admin/xxx/$id") {
                setBody(request)
            }
            val body = response.body<ApiResponse<XxxDTO>>()
            if (body.success && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xóa
    suspend fun deleteItem(id: Int): Result<Boolean> {
        return try {
            val response = ApiClient.http.delete("/api/admin/xxx/$id")
            val body = response.body<ApiResponse<Unit>>()
            if (body.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(body.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Quy tắc:**
- Luôn dùng `Result<T>` làm return type (không throw exception ra ngoài)
- Wrap trong `try/catch` để bắt network errors
- Check `body.success` trước khi dùng `body.data`

---

## 6. UI State Pattern

```kotlin
// Định nghĩa state (immutable data class)
data class XxxUiState(
    val isLoading: Boolean = false,
    val items: List<XxxDTO> = emptyList(),
    val totalItems: Int = 0,
    val currentPage: Int = 1,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Dialog states
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val selectedItem: XxxDTO? = null
)
```

---

## 7. ViewModel Pattern

```kotlin
class XxxViewModel : ViewModel() {
    private val repository = XxxRepository()

    private val _uiState = MutableStateFlow(XxxUiState())
    val uiState: StateFlow<XxxUiState> = _uiState

    // Load khi khởi tạo
    init {
        loadItems()
    }

    fun loadItems(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getItems(page).fold(
                onSuccess = { data ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        items = data.items,
                        totalItems = data.total,
                        currentPage = page
                    )}
                },
                onFailure = { e ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Lỗi không xác định"
                    )}
                }
            )
        }
    }

    fun createItem(request: CreateXxxRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.createItem(request).fold(
                onSuccess = {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showCreateDialog = false,
                        successMessage = "Tạo thành công"
                    )}
                    loadItems() // Refresh list
                },
                onFailure = { e ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )}
                }
            )
        }
    }

    // Helpers for dialog control
    fun showCreateDialog() = _uiState.update { it.copy(showCreateDialog = true) }
    fun hideCreateDialog() = _uiState.update { it.copy(showCreateDialog = false) }
    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    fun selectItem(item: XxxDTO) = _uiState.update { it.copy(selectedItem = item, showEditDialog = true) }
}
```

---

## 8. Screen (Composable) Pattern

```kotlin
@Composable
fun XxxScreen(
    viewModel: XxxViewModel = viewModel { XxxViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Hiển thị snackbar khi có message
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            // show snackbar
            viewModel.clearMessages()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {

        // Header
        PageHeader(
            title = "Quản lý Xxx",
            subtitle = "Tổng: ${uiState.totalItems}",
            actionButton = { Button(onClick = viewModel::showCreateDialog) { Text("Thêm mới") } }
        )

        // Loading / Content
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Table / List content
            LazyColumn {
                items(uiState.items) { item ->
                    XxxRow(
                        item = item,
                        onEdit = { viewModel.selectItem(item) },
                        onDelete = { viewModel.deleteItem(item.id) }
                    )
                }
            }
        }

        // Error message
        uiState.errorMessage?.let {
            SnackbarMessage(message = it, onDismiss = viewModel::clearMessages)
        }
    }

    // Dialogs
    if (uiState.showCreateDialog) {
        CreateXxxDialog(
            onConfirm = { request -> viewModel.createItem(request) },
            onDismiss = viewModel::hideCreateDialog
        )
    }
}
```

---

## 9. Navigation & App Root

**File:** `navigation/AdminScreen.kt`
```kotlin
enum class AdminScreen {
    LOGIN, DASHBOARD, USERS, GAMES, CARDS, FINANCE, NOTIFICATIONS, SUPPORT, ANNOUNCEMENTS
}
```

**File:** `App.kt`
```kotlin
@Composable
fun App() {
    val authViewModel = viewModel { AuthViewModel() }
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    if (!authState.isLoggedIn) {
        LoginScreen(authViewModel)
    } else {
        var currentScreen by remember { mutableStateOf(AdminScreen.DASHBOARD) }
        Row(Modifier.fillMaxSize()) {
            SideNav(
                currentScreen = currentScreen,
                onNavigate = { currentScreen = it },
                onLogout = { authViewModel.logout() }
            )
            Box(Modifier.fillMaxSize()) {
                when (currentScreen) {
                    AdminScreen.DASHBOARD -> DashboardScreen()
                    AdminScreen.USERS -> UserManagementScreen()
                    AdminScreen.GAMES -> GameManagementScreen()
                    // ... thêm screen mới vào đây
                    else -> {}
                }
            }
        }
    }
}
```

**Khi thêm screen mới:**
1. Thêm entry vào enum `AdminScreen`
2. Thêm `when` branch trong `App.kt`
3. Thêm nav item trong `SideNav.kt`

---

## 10. Backend Pattern (Ktor)

### Route Handler
```kotlin
fun Route.xxxRoutes() {
    val xxxService = XxxService()

    route("/api/admin/xxx") {
        // Public endpoint
        post("/action") {
            val request = call.receive<XxxRequest>()
            val result = xxxService.doAction(request)
            call.respond(if (result.success) HttpStatusCode.OK else HttpStatusCode.BadRequest, result)
        }

        // Protected endpoints
        authenticate("auth-jwt") {
            get {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                val data = xxxService.getItems(page, size)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = data))
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Invalid ID"))
                val item = xxxService.getById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, message = "Not found"))
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = item))
            }

            post {
                val request = call.receive<CreateXxxRequest>()
                val item = xxxService.create(request)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = item))
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()!!
                val request = call.receive<UpdateXxxRequest>()
                val item = xxxService.update(id, request)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = item))
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()!!
                xxxService.delete(id)
                call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Deleted"))
            }
        }
    }
}
```

### Service Layer
```kotlin
class XxxService {
    private val repository = XxxRepository()

    fun getItems(page: Int, size: Int): PaginatedResponse<XxxDTO> {
        val items = repository.findAll(page, size)
        val total = repository.count()
        return PaginatedResponse(
            items = items.map { it.toDTO() },
            total = total,
            page = page,
            size = size,
            totalPages = ceil(total.toDouble() / size).toInt()
        )
    }
    // ...
}
```

### Database Table (Exposed ORM)
```kotlin
object XxxTable : Table("xxx") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}
```

### Đăng ký route trong Routing.kt
```kotlin
fun Application.configureRouting() {
    routing {
        authRoutes()
        userRoutes()
        xxxRoutes()   // Thêm vào đây
    }
}
```

---

## 11. Checklist Thêm Feature Mới

### Backend:
- [ ] Tạo table trong `database/tables/XxxTable.kt`
- [ ] Tạo entity trong `entities/XxxEntity.kt`
- [ ] Tạo DTOs trong `dto/XxxDTO.kt`
- [ ] Tạo repository trong `repositories/XxxRepository.kt`
- [ ] Tạo service trong `services/XxxService.kt`
- [ ] Tạo routes trong `routes/XxxRoutes.kt`
- [ ] Đăng ký route trong `plugins/Routing.kt`
- [ ] Tạo table trong `DatabaseFactory.kt` (`SchemaUtils.create(XxxTable)`)

### Desktop App:
- [ ] Thêm DTO trong `data/model/Models.kt`
- [ ] Tạo repository trong `data/repository/XxxRepository.kt`
- [ ] Định nghĩa `XxxUiState` data class
- [ ] Tạo `XxxViewModel.kt`
- [ ] Tạo `XxxScreen.kt`
- [ ] Thêm entry vào `AdminScreen` enum
- [ ] Thêm `when` branch trong `App.kt`
- [ ] Thêm nav item trong `SideNav.kt`

---

## 12. Thông Tin Kết Nối

| Config | Giá Trị |
|--------|---------|
| Base URL | `http://192.168.0.101:8080` |
| Database | MySQL `park_card_system_v2` |
| DB Port | 3306 |
| JWT Secret | `park-adventure-secret-key-2024` |
| JWT TTL | 30 ngày |
| Server Port | 8080 |

---

## 13. WebSocket (Support Chat)

```kotlin
// Client connects to: ws://192.168.0.101:8080/ws/admin/support
// Token passed as query param: ?token=xxx

class SupportWebSocketClient(private val token: String) {
    private val _newMessage = MutableSharedFlow<SupportMessageDTO>()
    val newMessage: SharedFlow<SupportMessageDTO> = _newMessage

    fun connect(scope: CoroutineScope) {
        scope.launch {
            while (isActive) {
                try {
                    ApiClient.http.webSocket("/ws/admin/support?token=$token") {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val msg = Json.decodeFromString<SupportMessageDTO>(frame.readText())
                                _newMessage.emit(msg)
                            }
                        }
                    }
                } catch (e: Exception) {
                    delay(5000) // Auto-reconnect sau 5s
                }
            }
        }
    }

    suspend fun sendMessage(userId: Int, content: String) {
        // Call REST API to send reply
        ApiClient.http.post("/api/admin/support/reply") {
            setBody(ReplyRequest(userId, content))
        }
    }
}
```

---

## 14. Upload File (Multipart)

```kotlin
// Repository
suspend fun uploadImage(file: ByteArray, fileName: String): Result<String> {
    return try {
        val response = ApiClient.http.post("/api/upload") {
            setBody(MultiPartFormDataContent(formData {
                append("file", file, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
            }))
        }
        val body = response.body<ApiResponse<String>>()
        if (body.success && body.data != null) Result.success(body.data)
        else Result.failure(Exception(body.message))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```
