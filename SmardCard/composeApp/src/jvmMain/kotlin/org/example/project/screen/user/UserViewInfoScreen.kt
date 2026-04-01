package org.example.project.screen.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose. ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles

import org.jetbrains.skia.Image as SkiaImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserViewInfoScreen(
    smartCardManager: SmartCardManager,
    onBack:  () -> Unit
) {
    var customerID by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var customerPhoto by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        isLoading = true
        status = "⏳ Đang đọc thông tin..."

        try {
            val info = smartCardManager. readCustomerInfo()
            customerID = info["customerID"] ?: ""
            name = info["name"] ?: ""
            dateOfBirth = info["dateOfBirth"] ?: ""
            phoneNumber = info["phoneNumber"] ?: ""

            status = "✅ Đã đọc thông tin"

            val photoBytes = smartCardManager.readCustomerImage()
            if (photoBytes != null && photoBytes. isNotEmpty()) {
                try {
                    val skiaImage = SkiaImage. makeFromEncoded(photoBytes)
                    customerPhoto = skiaImage.toComposeImageBitmap()
                    status = "✅ Đã tải đầy đủ thông tin!"
                } catch (e: Exception) {
                    status = "⚠️ Không thể hiển thị ảnh"
                }
            }

        } catch (e:  Exception) {
            status = "❌ Lỗi:  ${e.message}"
        } finally {
            isLoading = false
        }
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
                .verticalScroll(scrollState)  // ✅ THÊM scroll toàn màn
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
                        .padding(20.dp)  // ✅ GIỐNG
                ) {
                    Row(
                        modifier = Modifier. fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                . size(48.dp)  // ✅ GIỐNG
                                .clip(CircleShape)
                                .background(Color.White. copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color. White,
                                modifier = Modifier.size(26.dp)  // ✅ GIỐNG
                            )
                        }

                        Spacer(modifier = Modifier. width(16.dp))

                        Column(modifier = Modifier. weight(1f)) {
                            Text(
                                text = "👤 Thông Tin Của Tôi",
                                fontSize = 22.sp,  // ✅ GIỐNG
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults. cardColors(
                                    containerColor = Color. White. copy(alpha = 0.25f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📋", fontSize = 18.sp)  // ✅ GIỐNG
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Xem thông tin cá nhân",
                                        fontSize = 14.sp,  // ✅ GIỐNG
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)  // ✅ GIỐNG
                                . clip(CircleShape)
                                .background(Color.White. copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📋", fontSize = 32.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(20.dp))  // ✅ GIỐNG

            // ✅ CONTENT CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()  // ✅ GIỐNG
                    .shadow(12.dp, RoundedCornerShape(28.dp)),  // ✅ GIỐNG
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color. White)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = Color(0xFFFF6B9D),
                            strokeWidth = 5.dp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)  // ✅ GIỐNG
                    ) {
                        // ✅ ẢNH
                        Card(
                            modifier = Modifier. fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),  // ✅ GIỐNG
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3E0)  // ✅ GIỐNG
                            ),
                            elevation = CardDefaults.cardElevation(6.dp)  // ✅ GIỐNG
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),  // ✅ GIỐNG
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement. Center
                                ) {
                                    Text("📸", fontSize = 22.sp)  // ✅ GIỐNG
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Ảnh của tôi",
                                        fontSize = 20.sp,  // ✅ GIỐNG
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFF6B00)  // ✅ GIỐNG
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Box(
                                    modifier = Modifier
                                        .size(150.dp)  // ✅ GIỐNG
                                        .shadow(12.dp, CircleShape)  // ✅ GIỐNG
                                        .clip(CircleShape)
                                        . background(
                                            brush = Brush. radialGradient(
                                                colors = listOf(
                                                    Color(0xFFFF6B9D),  // ✅ GIỐNG
                                                    Color(0xFFFFA07A),
                                                    Color(0xFFFFD700)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (customerPhoto != null) {
                                        Image(
                                            bitmap = customerPhoto!!,
                                            contentDescription = "Photo",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),  // ✅ GIỐNG
                                                tint = Color. White
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Chưa có ảnh",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier. height(28.dp))  // ✅ GIỐNG

                        HorizontalDivider(
                            color = Color(0xFFFFAB91),  // ✅ GIỐNG
                            thickness = 2.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        Spacer(modifier = Modifier. height(20.dp))

                        // ✅ THÔNG TIN
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📝", fontSize = 24.sp)  // ✅ GIỐNG
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Thông tin cơ bản",
                                fontSize = 20.sp,  // ✅ GIỐNG
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF6B00)  // ✅ GIỐNG
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        InfoCard(
                            icon = Icons.Default.Badge,
                            label = "Mã khách hàng",
                            value = customerID.ifEmpty { "Chưa có dữ liệu" },
                            iconColor = Color(0xFF4CAF50)
                        )

                        Spacer(modifier = Modifier. height(18.dp))  // ✅ GIỐNG

                        InfoCard(
                            icon = Icons.Default.Person,
                            label = "Họ và tên",
                            value = name.ifEmpty { "Chưa có" },
                            iconColor = Color(0xFFFF6B9D)  // ✅ GIỐNG màu TextField
                        )

                        Spacer(modifier = Modifier. height(18.dp))

                        InfoCard(
                            icon = Icons.Default.CalendarToday,
                            label = "Ngày sinh",
                            value = dateOfBirth.ifEmpty { "Chưa có" },
                            iconColor = Color(0xFFFFA726)  // ✅ GIỐNG màu TextField
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        InfoCard(
                            icon = Icons.Default.Phone,
                            label = "Số điện thoại",
                            value = phoneNumber.ifEmpty { "Chưa có" },
                            iconColor = Color(0xFF66BB6A)  // ✅ GIỐNG màu TextField
                        )
                    }
                }
            }

            // ✅ STATUS
            if (status.isNotEmpty() && ! isLoading) {
                Spacer(modifier = Modifier. height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(20.dp)),  // ✅ GIỐNG
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            status.startsWith("✅") -> Color(0xFFE8F5E9)
                            status.startsWith("⚠️") -> Color(0xFFFFF3E0)
                            status.startsWith("⏳") -> Color(0xFFE3F2FD)
                            else -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),  // ✅ GIỐNG
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                status.startsWith("✅") -> "✅"
                                status.startsWith("⚠️") -> "⚠️"
                                status.startsWith("⏳") -> "⏳"
                                else -> "❌"
                            },
                            fontSize = 28.sp  // ✅ GIỐNG
                        )
                        Spacer(modifier = Modifier. width(14.dp))
                        Text(
                            text = status. substring(2),
                            fontSize = 16.sp,  // ✅ GIỐNG
                            fontWeight = FontWeight.Bold,
                            color = when {
                                status.startsWith("✅") -> Color(0xFF4CAF50)
                                status.startsWith("⚠️") -> Color(0xFFFFA726)
                                status.startsWith("⏳") -> Color(0xFF2196F3)
                                else -> Color(0xFFE53935)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon:  androidx.compose.ui.graphics. vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Card(
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier. size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Bold  // ✅ ĐỔI: Medium→Bold
                )
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = iconColor
                )
            }
        }
    }
}