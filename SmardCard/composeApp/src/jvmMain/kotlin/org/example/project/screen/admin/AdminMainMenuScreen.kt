package org.example.project.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx. compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics. Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AdminMainMenuScreen(
//    smartCardManager: SmartCardManager,
//    onNavigateWriteInfo: () -> Unit,
//    onNavigateRecharge: () -> Unit,
//    onNavigateGameManagement: () -> Unit,
//    onNavigateViewCustomer: () -> Unit,
//    onNavigateSettings: () -> Unit,
//    onDisconnect: () -> Unit
//) {
//    val menuOptions = remember {
//        listOf(
//            AdminMenuOption(
//                title = "Ghi thông tin",
//                emoji = "✍️",
//                description = "Thêm khách hàng mới",
//                gradientColors = listOf(Color(0xFF81C784), Color(0xFFA5D6A7)),
//                onClick = onNavigateWriteInfo
//            ),
//            AdminMenuOption(
//                title = "Xem khách hàng",
//                emoji = "👁️",
//                description = "Xem thông tin & ảnh",
//                gradientColors = listOf(Color(0xFF64B5F6), Color(0xFF90CAF9)),
//                onClick = onNavigateViewCustomer
//            ),
//            AdminMenuOption(
//                title = "Nạp tiền",
//                emoji = "💰",
//                description = "Nạp tiền vào thẻ",
//                gradientColors = listOf(Color(0xFFFFB74D), Color(0xFFFFCC02)),
//                onClick = onNavigateRecharge
//            ),
//            AdminMenuOption(
//                title = "Quản lý vé game",
//                emoji = "🎮",
//                description = "Thêm/Xóa vé chơi",
//                gradientColors = listOf(Color(0xFFBA68C8), Color(0xFFCE93D8)),
//                onClick = onNavigateGameManagement
//            ),
////            AdminMenuOption(
////                title = "RSA Authentication",
////                emoji = "🔐",
////                description = "Xác thực bằng RSA",
////                gradientColors = listOf(Color(0xFFEF5350), Color(0xFFE57373)),
////                onClick = onNavigateRSAAuth
////            ),
////            AdminMenuOption(
////                title = "Cài đặt",
////                emoji = "⚙️",
////                description = "Cấu hình hệ thống",
////                gradientColors = listOf(Color(0xFF90A4AE), Color(0xFFB0BEC5)),
////                onClick = onNavigateSettings
////            )
//        )
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        Color(0xFFFFF3E0),
//                        Color(0xFFFFF0F5),
//                        Color(0xFFE0F7FA)
//                    )
//                )
//            )
//    ) {
//        FloatingBubbles()
//
//        Column(modifier = Modifier.fillMaxSize()) {
//            // Header
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp),
//                shape = RoundedCornerShape(32.dp),
//                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF7043)),
//                elevation = CardDefaults. cardElevation(16.dp)
//            ) {
//                Column(
//                    modifier = Modifier. padding(28.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(80.dp)
//                            .clip(CircleShape)
//                            .background(
//                                brush = Brush.radialGradient(
//                                    colors = listOf(
//                                        Color. White. copy(alpha = 0.4f),
//                                        Color. White.copy(alpha = 0.2f),
//                                        Color. Transparent
//                                    )
//                                )
//                            ),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("👨‍💼", fontSize = 40.sp)
//                    }
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Text(
//                        text = "Quản Lý Hệ Thống",
//                        fontSize = 28.sp,
//                        fontWeight = FontWeight.ExtraBold,
//                        color = Color. White
//                    )
//
//                    Text(
//                        text = "Bảng điều khiển quản trị viên",
//                        fontSize = 16.sp,
//                        color = Color.White. copy(alpha = 0.9f)
//                    )
//                }
//            }
//
//            // Menu Grid
//            Column(
//                modifier = Modifier
//                    .weight(1f)
//                    . padding(horizontal = 20.dp)
//                    .padding(top = 16.dp),
//                verticalArrangement = Arrangement. spacedBy(16.dp)
//            ) {
//                menuOptions.chunked(2).forEach { rowOptions ->
//                    Row(
//                        modifier = Modifier. fillMaxWidth(),
//                        horizontalArrangement = Arrangement. spacedBy(16.dp)
//                    ) {
//                        rowOptions.forEach { option ->
//                            AdminMenuCard(
//                                option = option,
//                                modifier = Modifier. weight(1f)
//                            )
//                        }
//                        if (rowOptions.size == 1) {
//                            Spacer(modifier = Modifier.weight(1f))
//                        }
//                    }
//                }
//            }
//
//            // Disconnect button
//            OutlinedButton(
//                onClick = onDisconnect,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp),
//                shape = RoundedCornerShape(16.dp),
//                colors = ButtonDefaults.outlinedButtonColors(
//                    contentColor = Color(0xFFE53935)
//                )
//            ) {
//                Text(
//                    text = "🚪 Ngắt kết nối",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AdminMenuCard(
//    option: AdminMenuOption,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        onClick = option.onClick,
//        modifier = modifier. height(160.dp),
//        shape = RoundedCornerShape(20.dp),
//        colors = CardDefaults.cardColors(containerColor = Color. White),
//        elevation = CardDefaults.cardElevation(8.dp)
//    ) {
//        Box(modifier = Modifier.fillMaxSize()) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(20.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(64.dp)
//                        .background(
//                            brush = Brush.radialGradient(colors = option.gradientColors),
//                            shape = CircleShape
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(text = option.emoji, fontSize = 28.sp)
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Text(
//                    text = option.title,
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF333333),
//                    textAlign = TextAlign.Center
//                )
//
//                Spacer(modifier = Modifier. height(4.dp))
//
//                Text(
//                    text = option.description,
//                    fontSize = 12.sp,
//                    color = Color(0xFF666666),
//                    textAlign = TextAlign. Center,
//                    lineHeight = 16.sp,
//                    maxLines = 2
//                )
//            }
//
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        brush = Brush.verticalGradient(
//                            colors = listOf(
//                                Color. Transparent,
//                                option.gradientColors.first().copy(alpha = 0.05f)
//                            )
//                        ),
//                        shape = RoundedCornerShape(20.dp)
//                    )
//            )
//        }
//    }
//}
//
//data class AdminMenuOption(
//    val title: String,
//    val emoji: String,
//    val description:  String,
//    val gradientColors: List<Color>,
//    val onClick: () -> Unit
//)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainMenuScreen(
    smartCardManager:  SmartCardManager,
    onNavigateWriteInfo: () -> Unit,
    onNavigateRecharge: () -> Unit,
    onNavigateGameManagement: () -> Unit,
    onNavigateRevenue: () -> Unit,
    onNavigateViewCustomer: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateResetUserPin: () -> Unit,
    onDisconnect: () -> Unit
) {
    val scrollState = rememberScrollState()

    val menuOptions = remember {
        listOf(
            AdminMenuOption(
                title = "Ghi thông tin",
                emoji = "✍️",
                description = "Thêm khách hàng mới",
                gradientColors = listOf(
                    Color(0xFFFF6B9D),  // ✅ Hồng (giống TextField Họ tên)
                    Color(0xFFFFA07A)   // ✅ Cam nhạt
                ),
                onClick = onNavigateWriteInfo
            ),
            AdminMenuOption(
                title = "Xem khách hàng",
                emoji = "👁️",
                description = "Xem thông tin & ảnh",
                gradientColors = listOf(
                    Color(0xFF4CAF50),  // ✅ Xanh lá (giống button Ghi)
                    Color(0xFF81C784)
                ),
                onClick = onNavigateViewCustomer
            ),
            AdminMenuOption(
                title = "Nạp tiền",
                emoji = "💰",
                description = "Nạp tiền vào thẻ",
                gradientColors = listOf(
                    Color(0xFFFFA726),  // ✅ Cam (giống TextField Ngày sinh)
                    Color(0xFFFFD700)   // ✅ Vàng
                ),
                onClick = onNavigateRecharge
            ),
            AdminMenuOption(
                title = "Quản lý vé game",
                emoji = "🎮",
                description = "Thêm/Xóa vé chơi",
                gradientColors = listOf(
                    Color(0xFFC06FBB),  // ✅ Tím (giống header)
                    Color(0xFFCE93D8)
                ),
                onClick = onNavigateGameManagement
            ),
            AdminMenuOption(
                title = "Doanh thu",
                emoji = "📊",
                description = "Thống kê ngày/tháng/game",
                gradientColors = listOf(
                    Color(0xFF26C6DA),
                    Color(0xFF80DEEA)
                ),
                onClick = onNavigateRevenue
            ),
            AdminMenuOption(
                title = "Reset User PIN",
                emoji = "🔐",
                description = "Đặt lại PIN cho user",
                gradientColors = listOf(
                    Color(0xFF9C27B0),
                    Color(0xFFBA68C8)
                ),
                onClick = onNavigateResetUserPin
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE5EC),  // ✅ GIỐNG
                        Color(0xFFFFF4E6),
                        Color(0xFFE8F5E9)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 80.dp, vertical = 20.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF6B9D),  // ✅ GIỐNG header các màn trước
                                    Color(0xFFC06FBB),
                                    Color(0xFFFEC163)
                                )
                            )
                        )
                        .padding(28.dp)
                ) {
                    Column(
                        modifier = Modifier. fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                . size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color. White.copy(alpha = 0.4f),
                                            Color.White.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👨‍💼", fontSize = 50.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Quản Lý Hệ Thống",
                            fontSize = 32.sp,
                            fontWeight = FontWeight. ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color. White.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier. padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚡", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bảng điều khiển quản trị viên",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color. White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    menuOptions.chunked(2).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            rowOptions.forEach { option ->
                                AdminMenuCard(
                                    option = option,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowOptions. size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onDisconnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935)  // ✅ Đỏ vừa phải
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 16.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement. Center
                ) {
                    Text(
                        text = "Ngắt kết nối",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuCard(
    option: AdminMenuOption,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = option.onClick,
        modifier = modifier. height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults. cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(colors = option.gradientColors)
                            )
                    )
                    Text(text = option.emoji, fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = option.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier. height(6.dp))

                Text(
                    text = option.description,
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign. Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                    maxLines = 2
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color. Transparent,
                                option.gradientColors.first().copy(alpha = 0.08f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }
    }
}

data class AdminMenuOption(
    val title: String,
    val emoji: String,
    val description:  String,
    val gradientColors: List<Color>,
    val onClick: () -> Unit
)