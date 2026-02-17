# App UI Guidelines - Park Adventure

File này đóng vai trò là "Bộ não thiết kế" (Design Context) cho toàn bộ dự án. Bất kỳ màn hình mới nào được tạo ra cũng phải tuân thủ nghiêm ngặt các quy tắc dưới đây để đảm bảo tính nhất quán.

## 1. Global Constants (Hằng số chung)

### Color Palette
Hệ thống màu sử dụng tông cam ấm (Warm Orange) làm chủ đạo, kết hợp với màu trung tính tối cho văn bản và trắng/xám nhẹ cho nền.

| Role | Color Name | Hex Code | Usage |
| :--- | :--- | :--- | :--- |
| **Primary** | `OrangePrimary` / `WarmOrange` | `#FF6B35` / `#E55722` | Buttons, Active States, Brand Elements |
| **Secondary** | `BluePrimary` | `#2196F3` | Info badges, Secondary actions |
| **Background** | `SurfaceLight` | `#F8F9FA` | Main screen background |
| **Surface** | `White` | `#FFFFFF` | Cards, Dialogs, BottomSheet |
| **Text Primary** | `PrimaryDark` | `#1A1A1A` | Headings, Main content |
| **Text Secondary** | `PrimaryGray` | `#6B7280` | Subtitles, Captions, Placeholders |
| **Error** | `RedError` | `#F44336` | Error messages, High Risk badges |
| **Success** | `GreenSuccess` | `#4CAF50` | Success states, Low Risk badges |
| **Warning** | `YellowWarning` | `#FFC107` | Warning states, Medium Risk badges |

*Note: Sử dụng `AppColors` object trong `Theme.kt` để truy cập các màu semantic (ví dụ: `AppColors.WarmOrange`).*

### Typography
Sử dụng **Roboto** (Default System Font) với các style được định nghĩa trong `Type.kt`.

| Style | Size | Weight | Line Height | Usage |
| :--- | :--- | :--- | :--- | :--- |
| **Display Large** | 32sp | Bold | 40sp | Large Greetings, Hero Text |
| **Headline Large** | 22sp | Bold | 28sp | Screen Titles (TopBar) |
| **Title Large** | 18sp | SemiBold | 24sp | Section Headers, Card Titles |
| **Body Large** | 16sp | Normal | 24sp | Main Body Text |
| **Body Medium** | 14sp | Normal | 20sp | Secondary Text, Descriptions |
| **Label Small** | 11sp | Medium | 16sp | Badges, Captions, Labels |

### Spacing & Layout
Sử dụng các giá trị chẵn chia hết cho 4dp.

*   **Standard Spacing / Padding**:
    *   `4dp`: Small spacing (e.g., inside chips/badges).
    *   `8dp`: Medium spacing (e.g., between icon and text).
    *   `12dp`: Standard component spacing (e.g., vertical list spacing).
    *   `16dp`: Standard screen padding (horizontal/vertical).
    *   `24dp`: Section spacing or large screen padding.
*   **Safe Area**: Đảm bảo nội dung không bị che khuất bởi tai thỏ hoặc navigation bar bằng cách sử dụng `Scaffold` paddingValues hoặc `WindowInsets`.

### Shapes & Radius
Thống nhất độ bo góc cho cảm giác hiện đại, mềm mại.

*   **Buttons**: `RoundedCornerShape(12.dp)`
*   **Cards**: `RoundedCornerShape(16.dp)` (List Items) hoặc `RoundedCornerShape(20.dp)` (Large Cards like Login).
*   **Input Fields**: `RoundedCornerShape(16.dp)` (Search bars, Text Fields).
*   **Bottom Sheet**: `RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)`.
*   **Badges/Tags**: `RoundedCornerShape(12.dp)` hoặc `CircleShape`.

## 2. Core Components (Thành phần cốt lõi)

### Buttons
*   **Primary Button**:
    *   Height: `48.dp` (Standard), `36.dp` (Small/Compact).
    *   Background: `AppColors.WarmOrange`.
    *   Content Color: `Color.White`.
    *   Shape: `RoundedCornerShape(12.dp)`.
    *   Text: Bold, Uppercase (optional) or Title Case.
    *   *State*: Disabled -> Alpha 0.5f or Gray background.

### Input Fields
*   **Style**: `OutlinedTextField`.
*   **Shape**: `RoundedCornerShape(16.dp)`.
*   **Colors**:
    *   Unfocused Border: Default Gray.
    *   Focused Border: `AppColors.WarmOrange`.
    *   Cursor/Label Active: `AppColors.WarmOrange`.
    *   Container: Transparent or `Color.White`.
*   **Icons**: Leading Icon for context (Phone, Lock, Search), Trailing Icon for actions (Visibility, Clear).

### Cards & Lists
*   **Card Style**:
    *   Container: `Color.White`.
    *   Elevation: `CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)`.
    *   Shape: `RoundedCornerShape(16.dp)`.
    *   Padding (Content): `16.dp`.
*   **List Layout**:
    *   `LazyColumn` / `LazyRow`.
    *   `verticalArrangement = Arrangement.spacedBy(12.dp)` hoặc `16.dp`.
    *   `contentPadding = PaddingValues(16.dp)`.

### Navigation
*   **TopAppBar**:
    *   Background: `AppColors.WarmOrange` (Solid) hoặc Gradient (nếu có context).
    *   Title Color: `Color.White`.
    *   Navigation Icon: `Color.White` (ArrowBack).
    *   Actions: Notifications, Search (White tint).
*   **BottomNavigationBar** (Adaptive):
    *   Implemented via `NavigationSuiteScaffold`.
    *   Selected Item: Icon Filled, Label Bold, Color defined by system (usually Primary/Secondary variant).
    *   Unselected Item: Icon Outlined, Label Medium.

## 3. Technical Stack Rules (Quy tắc Code)

### Libraries & Tools
*   **UI Toolkit**: Jetpack Compose (Material3 Design).
*   **Navigation**: `androidx.navigation.compose`.
*   **Icons**: `androidx.compose.material.icons` (Extended set).
*   **Async Image**: Coil (nếu cần load ảnh mạng, hiện tại dùng Vector/PainterResource).

### Naming Conventions
*   **Composables**: `PascalCase` (Noun or NounPhrase). Ví dụ: `GameCard`, `LoginScreen`.
*   **State Variables**: `camelCase`. Ví dụ: `isLoading`, `userName`.
*   **Event Handlers**: `on[Event]`. Ví dụ: `onClick`, `onLoginSuccess`, `onValueChange`.
*   **Files**: Tên file trùng với tên Composable chính trong file đó.

---
*Document generated by AI Assistant on 2026-02-09*
