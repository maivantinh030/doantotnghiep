## Kế hoạch chuẩn hóa SmartCard với backend hiện tại

Tài liệu này mô tả chi tiết những việc cần làm để:
- Đưa app SmartCard cũ (project `SmardCard`) dùng **chung backend hiện tại**.
- Đồng bộ cách gọi API/DTO với **kiểu `ApiClient` + repository** như `appdesktop`.
- Loại bỏ dần logic “tự xử lý business trên thẻ/offline server cũ”.

---

### 1. Mục tiêu tổng quát

- **Mục tiêu 1 – Thống nhất backend**  
  - Tất cả logic: đăng nhập admin, quản lý game, chơi game, log giao dịch, doanh thu, tra cứu thẻ… đều đi qua **backend Ktor hiện tại** (`backend/`).
  - Không còn phụ thuộc “server cũ” (các endpoint như `/games`, `/transactions`, `/rsa/...` riêng lẻ).

- **Mục tiêu 2 – Thống nhất cách gọi API**  
  - SmartCard client dùng chung phong cách với `appdesktop`:
    - 1 chỗ cấu hình base URL.
    - Có nơi lưu token rõ ràng.
    - DTO khớp chính xác JSON từ backend (thay vì Moshi structures cũ).

- **Mục tiêu 3 – Đơn giản hóa SmartCard**  
  - Trên thẻ chỉ giữ **UID + một vài thông tin crypto cần thiết (nếu còn dùng)**.
  - Không còn tự xử lý ví/số dư/ticket trên thẻ; mọi thứ do backend quyết định.

---

### 2. Hiện trạng nhanh (tóm tắt)

- **Backend hiện tại** (Ktor, thư mục `backend/`):
  - Xác thực user/app: `/api/auth/...`
  - Admin: `/api/admin/auth/login`, `/api/admin/dashboard/stats`, `/api/admin/users/...`, `/api/games/...`, `/api/orders/...`, `/api/cards/...`, `/api/wallet/...`, ...
  - Endpoint đặc biệt cho terminal: `POST /api/games/{gameId}/play` (terminal quét cardUid → dùng ticket/vé).

- **SmartCard server cũ** (đã thể hiện qua `SmardCard/*ApiClient.kt`):
  - Auth: `/admin/login`, `/admin/verify-token`
  - Game: `/games`, `/games/{code}`, `POST /games`, `DELETE /games/{code}`
  - Transaction: `POST /transactions/record`, `GET /transactions/history/{customerId}`, `/analytics/revenue/*`
  - RSA: `/rsa/challenge`, `/rsa/verify`

- **SmartCard client (project `SmardCard`)**:
  - Dùng `OkHttp` + `Moshi` (khác với `appdesktop` dùng `Ktor` + `kotlinx.serialization`).
  - Có 3 app:
    - `MainAdmin.kt` – admin dùng thẻ để ghi info, recharge, manage game trên thẻ.
    - `MainUser.kt` – user xem info, mua vé, chơi game trực tiếp với card.
    - `MainGamePlay.kt` – giao diện quẹt thẻ chơi game tại cổng.

---

### 3. Mapping chức năng từ server cũ → backend mới

#### 3.1. Auth admin

- **Cũ**:
  - `POST /admin/login` với `{ "username": "...", "password": "..." }`.
  - `GET /admin/verify-token`.
- **Mới** (đã áp dụng một phần):
  - `POST /api/admin/auth/login` với `{ "phoneNumber": "...", "password": "..." }`.
  - Sau khi login, backend trả về `AdminAuthResponse { success, message, data { token, admin { ... } } }`.
  - **Việc đã làm**:
    - Đã sửa `SmardCard`:
      - `ServerConfig.baseUrl = "http://localhost:8080/api"`.
      - `AdminApi.login()` → gọi `POST /admin/auth/login` (tức `/api/admin/auth/login`), body `{ phoneNumber, password }`, parse đúng `AdminAuthResponse`.

**Cần làm thêm**:
- (Nếu SmartCard còn dùng) thêm API `verify-token` mới trong backend (tùy nhu cầu) hoặc bỏ hẳn, chỉ rely vào JWT expiry.

#### 3.2. Game list / game info / game CRUD

- **Cũ**:
  - `GET /games` → `GamesListResponse { success, data: [GameDto] }`.
  - `GET /games/{gameCode}` → `GameDto`.
  - `POST /games` → thêm game (trả về `ApiResponse` với `data = gameCode`).
  - `DELETE /games/{gameCode}` → xóa game.

- **Mới**:
  - Xem `backend/src/main/kotlin/routes/GameRoutes.kt`:
    - `GET /api/games?page=&size=&category=&search=` trả về `ApiResponse<PaginatedData<GameDTO>>`.
    - `GET /api/games/{gameId}` trả về `ApiResponse<GameDTO>`.
    - `POST /api/games` / `PUT /api/games/{gameId}` / `DELETE /api/games/{gameId}` – CRUD game cho admin.

**Cần làm**:
- Thiết kế DTO Moshi SmartCard **match với `GameDTO` trong backend**:
  - Hoặc: bỏ bớt SmartCard `GameDto` cũ, map `GameDTO` → model nội bộ.
- Sửa `GameApiClient`:
  - `getAllGames()` → gọi `GET /games` của backend với query phù hợp, parse `ApiResponse<PaginatedData<GameDTO>>` thay vì `GamesListResponse`.
  - `getGame(gameCode)` → sử dụng `gameId` backend, hoặc thêm một endpoint backend cho “find by code” nếu thật sự cần.
  - `addGame()` / `deleteGame()` → đổi sang `POST /api/games`, `DELETE /api/games/{gameId}` theo backend mới.

#### 3.3. Transaction & doanh thu

- **Cũ (SmartCard server)**:
  - `POST /transactions/record` với `CreateTransactionRequest` (customerId, type, amount, tickets, gameCode,...).
  - `GET /transactions/history/{customerId}` → `TransactionsResponse`.
  - `GET /analytics/revenue/day|month|game` → doanh thu ngày/tháng/theo game.

- **Mới (backend)**:
  - Lịch sử đơn hàng, giao dịch:
    - `GET /api/admin/orders`, `GET /api/admin/transactions` (cho admin).
    - `GET /api/wallet/transactions`, `GET /api/wallet/payments` (cho user).
  - Doanh thu:
    - `GET /api/admin/revenue/chart?period=daily|weekly|monthly`.

**Cần làm**:
- Quyết định:
  - SmartCard admin **cần** xem doanh thu mức nào?
    - Nếu chỉ cần **tổng quan**: dùng luôn `GET /api/admin/revenue/chart`.
    - Nếu cần chi tiết: thêm (hoặc dùng sẵn) `/api/admin/transactions`, `/api/admin/orders`.
- Sửa `TransactionApiClient`:
  - Bỏ gọi `/transactions/*`, `/analytics/*` cũ.
  - Tạo client mới đơn giản:
    - `getRevenue(period)` → `GET /admin/revenue/chart?period=...`.
    - `getTransactions(page,size)` → `GET /admin/transactions?page=&size=`.
  - Sửa UI `AdminRevenueScreen` map sang format backend mới (đã có DTO `RevenueChartResponse`, `AdminTransactionDTO`).

#### 3.4. RSA challenge–response

- **Cũ**:
  - `GET /rsa/challenge` → `ChallengeResponse { challenge, expiresAt }`.
  - `POST /rsa/verify` → verify chữ ký RSA theo `RSAVerifyRequest`.
- **Mới**:
  - Backend hiện tại **chưa có** route `/api/rsa/...`.

**Cần làm (nếu vẫn muốn giữ RSA)**:
- Thiết kế & implement trong backend:
  - `GET /api/rsa/challenge` → giống `ChallengeResponse`.
  - `POST /api/rsa/verify` → verify chữ ký từ thẻ, trả về `RSAVerifyResponse`.
- Giữ nguyên `RSAApiClient` nhưng chỉnh path thành `/rsa/*` dưới `/api`:
  - `getChallenge()` → `GET /rsa/challenge`.
  - `verifySignature()` → `POST /rsa/verify`.

Hoặc: nếu đã có backend JWT + bảo mật khác, có thể **loại bỏ hoàn toàn phần RSA challenge** trong giai đoạn đầu để đơn giản hóa.

---

### 4. Việc cần làm theo từng bước

#### Bước 1 – Khóa cấu hình chung SmartCard theo backend mới

- [x] Sửa `ServerConfig.baseUrl` thành `http://localhost:8080/api`.
- [x] Sửa `AdminApi.login` để gọi `POST /api/admin/auth/login` với `{ phoneNumber, password }` và parse `AdminAuthResponse`.
- [ ] (Optional) Thêm API verify token mới hoặc bỏ logic verify token ở SmartCard, rely vào JWT + backend trả 401.

#### Bước 2 – Đồng bộ Game API SmartCard với `GameRoutes`

- [ ] Tạo (hoặc reuse) DTO ở SmartCard tương thích với backend `GameDTO` (hoặc thêm mapper).
- [ ] Sửa `GameApiClient.getAllGames()`:
  - Gọi `GET /games?page=&size=` (backend) thay vì `/games` cũ.
  - Parse `ApiResponse<PaginatedData<GameDTO>>` → `List<GameDto>` cho UI SmartCard.
- [ ] Sửa `getGame()` để dùng `gameId` backend:
  - Hoặc: thêm trường `legacyCode` trong backend nếu muốn giữ `gameCode` cũ.
- [ ] Sửa `addGame()`/`deleteGame()` tương thích `POST /api/games`, `DELETE /api/games/{gameId}`.
- [ ] Cập nhật UI (màn AdminGameManagementScreen) nếu có thay đổi field (vd `pricePerTurn` từ string → double).

#### Bước 3 – Sử dụng endpoint doanh thu/transaction mới

- [ ] Phân tích `AdminRevenueScreen` và các màn hình liên quan đến `TransactionApiClient`.
- [ ] Mapping:
  - `revenueByDay()/Month()` → `GET /api/admin/revenue/chart?period=daily|monthly`.
  - `revenueByGame()` → hoặc bỏ, hoặc thêm endpoint backend nếu còn cần.
- [ ] Viết `AdminRevenueApi` / sửa `TransactionApiClient` để gọi các endpoint `/api/admin/...` hiện có.
- [ ] Chỉnh lại UI biểu đồ để đọc từ `RevenueChartResponse` backend.

#### Bước 4 – Tách/migrate luồng chơi game (GamePlay)

Hiện tại `MainGamePlay.kt` + `SmartCardManager`:
- Đang **trừ lượt trực tiếp trên thẻ**, check tickets offline.

**Mục tiêu mới**:
- Khi quẹt thẻ:
  1. Reader đọc **UID**.
  2. Gọi backend:
     - `POST /api/games/{gameId}/play` với `{ "cardUid": "...", "terminalId": "..." }`.
  3. Backend:
     - Xác định user từ `cards.physical_card_uid`.
     - Check vé/ticket từ DB (`tickets` + `game_play_logs`).
     - Quyết định: cho chơi / từ chối.

**Việc cần làm**:
- [ ] Thiết kế rõ DTO request/response cho `POST /api/games/{gameId}/play` (backend đã có khung `UseGameRequest`).
- [ ] Trong SmartCard:
  - Thêm 1 client (`TerminalGameApiClient` chẳng hạn) gọi thẳng backend `POST /games/{gameId}/play` thay vì dùng `SmartCardManager.decreaseGameTickets`.
  - `SmartCardManager` vẫn chỉ lo **giao tiếp với reader + card để đọc UID** (RSA nếu cần).
- [ ] Cập nhật `GamePlayScreen`:
  - Thay đoạn “tìm game trên thẻ + trừ tickets trên thẻ” bằng:
    - Đọc UID → gọi API backend → hiển thị kết quả.

#### Bước 5 – Quyết định giữ hay bỏ RSA flow

- Nếu cần bảo mật cao:
  - [ ] Implement `/api/rsa/challenge` và `/api/rsa/verify` trong backend.
  - [ ] Giữ `RSAApiClient` và chỉ sửa base path → `/rsa/...` dưới `/api`.
- Nếu thấy đủ với HTTPS + JWT:
  - [ ] Bỏ toàn bộ phần RSA challenge ở SmartCard (giảm độ phức tạp đáng kể).

---

### 5. Ưu tiên triển khai (thứ tự đề xuất)

1. **Auth admin + base URL** (đã làm xong phần lớn).
2. **Luồng quẹt thẻ chơi game**:
   - Đưa `MainGamePlay` dùng endpoint `POST /api/games/{gameId}/play` với cardUid.
   - Đây là giá trị thực tiễn lớn nhất (kết nối thực tế giữa thẻ ↔ backend).
3. **Game list & CRUD** – để Admin SmartCard thấy giống danh mục game trên app/web.
4. **Doanh thu & transaction** – dùng `/api/admin/revenue/chart` + `/api/admin/transactions`.
5. **RSA** – nếu cần để tăng bảo mật, triển khai cuối cùng.

---

### 6. Ghi chú triển khai

- Mỗi khi chỉnh SmartCard để gọi backend mới:
  - Ưu tiên **tạo/mở rộng endpoint** trong backend trước, test bằng `API_TESTING.http`.
  - Sau đó mới cập nhật `*ApiClient` + DTO bên SmartCard để khớp JSON.
- Đối với các DTO trùng ý nghĩa giữa 2 bên (vd `GameDTO`, `TransactionDTO`):
  - Nên giữ tên/trường giống backend để giảm công mapping.
- Nên log rõ ràng ở cả backend và SmartCard khi thử nghiệm luồng mới (đặc biệt là luồng quẹt thẻ và chơi game).

