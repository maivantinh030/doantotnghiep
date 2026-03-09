package org.example.project.screen

import androidx.compose.foundation. background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose. foundation.shape.RoundedCornerShape
import androidx.compose. material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose. ui.draw.shadow
import androidx.compose. ui.graphics.Brush
import androidx.compose.ui.graphics. Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose. ui.unit.sp

enum class UserRole {
    USER,
    ADMIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    onRoleSelected: (UserRole) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFFFF0F5),
                        Color(0xFFE0F7FA)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF5C6BC0)),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color. White.copy(alpha = 0.3f),
                                        Color. Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎭", fontSize = 64.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Chọn Vai Trò",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Bạn là khách hàng hay quản lý? ",
                        fontSize = 16.sp,
                        color = Color.White. copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // User Card
            RoleCard(
                title = "Khách Hàng",
                emoji = "👤",
                description = "Xem thông tin, số dư và chơi game",
                gradientColors = listOf(Color(0xFF64B5F6), Color(0xFF90CAF9)),
                onClick = { onRoleSelected(UserRole.USER) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Admin Card
            RoleCard(
                title = "Quản Lý",
                emoji = "👨‍💼",
                description = "Quản lý thông tin, nạp tiền và vé chơi",
                gradientColors = listOf(Color(0xFFFF7043), Color(0xFFFF8A65)),
                onClick = { onRoleSelected(UserRole. ADMIN) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Back button
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier. fillMaxWidth(0.5f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF5C6BC0)
                )
            ) {
                Text(
                    text = "🔙 Quay lại",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleCard(
    title: String,
    emoji: String,
    description:  String,
    gradientColors:  List<Color>,
    onClick:  () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color. White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.linearGradient(colors = gradientColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 48.sp)
                }

                Spacer(modifier = Modifier. width(24.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        lineHeight = 18.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color. Transparent,
                                gradientColors. first().copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            )
        }
    }
}