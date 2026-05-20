package com.park.smartcard.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.park.smartcard.SmartCardManager
import com.park.smartcard.screen.FloatingBubbles

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UserMainMenuScreen(
//    smartCardManager: SmartCardManager,
//    onNavigateViewInfo: () -> Unit,
//    onNavigateUpdateInfo: () -> Unit,
//    onNavigateBuyTickets: () -> Unit,
//    onNavigateGames: () -> Unit,
//    onNavigateChangePin: () -> Unit,
//    onDisconnect: () -> Unit
//) {
//    val menuOptions = remember {
//        listOf(
//            UserMenuOption(
//                title = "Thông tin của tôi",
//                emoji = "👤",
//                description = "Xem thông tin cá nhân & ảnh",
//                gradientColors = listOf(Color(0xFF64B5F6), Color(0xFF90CAF9)),
//                onClick = onNavigateViewInfo
//            ),
//            UserMenuOption(
//                title = "Cập nhật thông tin",
//                emoji = "✏️",
//                description = "Chỉnh sửa thông tin cá nhân",
//                gradientColors = listOf(Color(0xFF4FC3F7), Color(0xFF81D4FA)),
//                onClick = onNavigateUpdateInfo
//            ),
//            UserMenuOption(
//                title = "Mua vé game",  // ✅ ĐỔI TÊN
//                emoji = "🎟️",          // ✅ ĐỔI ICON
//                description = "Mua vé chơi trò",  // ✅ ĐỔI MÔ TẢ
//                gradientColors = listOf(Color(0xFF81C784), Color(0xFFA5D6A7)),
//                onClick = onNavigateBuyTickets  // ✅ ĐỔI CALLBACK
//            ),
//            UserMenuOption(
//                title = "Vé của tôi",  // ✅ ĐỔI TÊN
//                emoji = "🎫",          // ✅ ĐỔI ICON
//                description = "Xem vé đã mua",  // ✅ ĐỔI MÔ TẢ
//                gradientColors = listOf(Color(0xFFBA68C8), Color(0xFFCE93D8)),
//                onClick = onNavigateGames
//            ),
//            UserMenuOption(
//                title = "Đổi mã PIN",
//                emoji = "🔐",
//                description = "Thay đổi mã bảo mật",
//                gradientColors = listOf(Color(0xFFFFB74D), Color(0xFFFFCC02)),
//                onClick = onNavigateChangePin
//            )
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
//                colors = CardDefaults.cardColors(containerColor = Color(0xFF64B5F6)),
//                elevation = CardDefaults. cardElevation(16.dp)
//            ) {
//                Column(
//                    modifier = Modifier.padding(28.dp),
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
//                        Text("👋", fontSize = 40.sp)
//                    }
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Text(
//                        text = "Xin chào, Khách hàng! ",
//                        fontSize = 28.sp,
//                        fontWeight = FontWeight.ExtraBold,
//                        color = Color. White
//                    )
//
//                    Text(
//                        text = "Bạn muốn làm gì hôm nay?",
//                        fontSize = 16.sp,
//                        color = Color.White.copy(alpha = 0.9f)
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
//                            UserMenuCard(
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
//fun UserMenuCard(
//    option: UserMenuOption,
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
//data class UserMenuOption(
//    val title: String,
//    val emoji: String,
//    val description:  String,
//    val gradientColors: List<Color>,
//    val onClick: () -> Unit
//)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainMenuScreen(
    smartCardManager: SmartCardManager,
    onNavigateViewInfo: () -> Unit,
    onNavigateUpdateInfo: () -> Unit,
    onNavigateBuyTickets:  () -> Unit,
    onNavigateGames: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateChangePin:  () -> Unit,
    onDisconnect: () -> Unit
) {
    val scrollState = rememberScrollState()  // ✅ THÊM

    val menuOptions = remember {
        listOf(
            UserMenuOption(
                title = "Thông tin của tôi",
                emoji = "👤",
                description = "Xem thông tin cá nhân & ảnh",
                gradientColors = listOf(
                    Color(0xFF4CAF50),  // ✅ Xanh lá (đồng bộ)
                    Color(0xFF81C784)
                ),
                onClick = onNavigateViewInfo
            ),
            UserMenuOption(
                title = "Cập nhật thông tin",
                emoji = "✏️",
                description = "Chỉnh sửa thông tin cá nhân",
                gradientColors = listOf(
                    Color(0xFFFF6B9D),  // ✅ Hồng (đồng bộ)
                    Color(0xFFFFA07A)
                ),
                onClick = onNavigateUpdateInfo
            ),
            UserMenuOption(
                title = "Mua vé game",
                emoji = "🎟️",
                description = "Mua vé chơi trò",
                gradientColors = listOf(
                    Color(0xFFFFA726),  // ✅ Cam (đồng bộ)
                    Color(0xFFFFD700)
                ),
                onClick = onNavigateBuyTickets
            ),
            UserMenuOption(
                title = "Vé của tôi",
                emoji = "🎫",
                description = "Xem vé đã mua",
                gradientColors = listOf(
                    Color(0xFFC06FBB),  // ✅ Tím (đồng bộ)
                    Color(0xFFCE93D8)
                ),
                onClick = onNavigateGames
            ),
            UserMenuOption(
                title = "Lịch sử giao dịch",
                emoji = "🧾",
                description = "Xem mua vé & nạp tiền",
                gradientColors = listOf(
                    Color(0xFF26C6DA),
                    Color(0xFF80DEEA)
                ),
                onClick = onNavigateHistory
            ),
            UserMenuOption(
                title = "Đổi mã PIN",
                emoji = "🔐",
                description = "Thay đổi mã bảo mật",
                gradientColors = listOf(
                    Color(0xFF2196F3),  // ✅ Xanh dương
                    Color(0xFF64B5F6)
                ),
                onClick = onNavigateChangePin
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
                . fillMaxSize()
                .verticalScroll(scrollState)  // ✅ THÊM scroll
                .padding(horizontal = 80.dp, vertical = 20.dp)  // ✅ GIỐNG
        ) {
            // ✅ HEADER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),  // ✅ GIỐNG
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF6B9D),  // ✅ GIỐNG
                                    Color(0xFFC06FBB),
                                    Color(0xFFFEC163)
                                )
                            )
                        )
                        .padding(28.dp)  // ✅ GIỐNG
                ) {
                    Column(
                        modifier = Modifier. fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp) // ✅ GIỐNG
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
                        Text("👋", fontSize = 50.sp)  // ✅ GIỐNG
                    }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Xin chào, Khách hàng! ",
                            fontSize = 32.sp,  // ✅ GIỐNG
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults. cardColors(
                                containerColor = Color.White. copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💫", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bạn muốn làm gì hôm nay? ",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color. White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(28.dp))  // ✅ GIỐNG

            // ✅ MENU GRID
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),  // ✅ GIỐNG
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),  // ✅ GIỐNG
                    verticalArrangement = Arrangement. spacedBy(20.dp)  // ✅ GIỐNG
                ) {
                    menuOptions.chunked(2).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)  // ✅ GIỐNG
                        ) {
                            rowOptions.forEach { option ->
                                UserMenuCard(
                                    option = option,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowOptions.size == 1) {
                                Spacer(modifier = Modifier. weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ✅ DISCONNECT BUTTON
            Button(
                onClick = onDisconnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),  // ✅ GIỐNG
                shape = RoundedCornerShape(18.dp),  // ✅ GIỐNG
                colors = ButtonDefaults. buttonColors(
                    containerColor = Color(0xFFE53935)
                ),
                elevation = ButtonDefaults. buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 16.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
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
fun UserMenuCard(
    option: UserMenuOption,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = option. onClick,
        modifier = modifier. height(180.dp),  // ✅ GIỐNG AdminMenuCard
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),  // ✅ GIỐNG
        elevation = CardDefaults. cardElevation(6.dp)  // ✅ GIỐNG
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    . fillMaxSize()
                    . padding(24.dp),  // ✅ GIỐNG
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp),  // ✅ GIỐNG
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
                    Text(text = option.emoji, fontSize = 40.sp)  // ✅ GIỐNG
                }

                Spacer(modifier = Modifier.height(18.dp))  // ✅ GIỐNG

                Text(
                    text = option.title,
                    fontSize = 18.sp,  // ✅ GIỐNG
                    fontWeight = FontWeight.ExtraBold,  // ✅ GIỐNG
                    color = Color(0xFF333333),
                    textAlign = TextAlign. Center
                )

                Spacer(modifier = Modifier. height(6.dp))  // ✅ GIỐNG

                Text(
                    text = option.description,
                    fontSize = 13.sp,  // ✅ GIỐNG
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,  // ✅ GIỐNG
                    maxLines = 2
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                option.gradientColors.first().copy(alpha = 0.08f)  // ✅ GIỐNG
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }
    }
}

data class UserMenuOption(
    val title: String,
    val emoji: String,
    val description:  String,
    val gradientColors: List<Color>,
    val onClick: () -> Unit
)