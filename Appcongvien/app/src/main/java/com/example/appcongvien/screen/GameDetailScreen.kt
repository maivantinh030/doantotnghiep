package com.example.appcongvien.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors

data class GameDetail(
    val id: String,
    val name: String,
    val description: String,
    val shortDescription: String,
    val ageRange: String,
    val heightRequirement: String,
    val location: String,
    val type: GameType,
    val riskLevel: RiskLevel,
    val pricePerTurn: Int,
    val discount: Int,
    val rating: Float = 4.5f,
    val totalRatings: Int = 127
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddToCart: (String, Int) -> Unit = { _, _ -> }
) {
    var quantity by remember { mutableStateOf(1) }

    // Mock game data - in real app, fetch by gameId
    val game = remember(gameId) {
        GameDetail(
            id = gameId,
            name = "Đu Quay Khổng Lồ",
            description = "Trải nghiệm cảm giác mạnh với đu quay khổng lồ cao 50m. Từ đỉnh cao, bạn sẽ ngắm nhìn toàn cảnh công viên và thành phố xung quanh. Trò chơi phù hợp cho những người yêu thích cảm giác mạnh và muốn thử thách bản thân. Hệ thống an toàn hiện đại đảm bảo sự an toàn tuyệt đối cho người chơi.",
            shortDescription = "Cảm giác mạnh với đu quay cao 50m",
            ageRange = "8+",
            heightRequirement = "Từ 1.2m",
            location = "Khu vực A - Trung tâm công viên",
            type = GameType.OUTDOOR,
            riskLevel = RiskLevel.HIGH,
            pricePerTurn = 50000,
            discount = 10,
            rating = 4.8f,
            totalRatings = 342
        )
    }

    val discountedPrice = if (game.discount > 0) {
        game.pricePerTurn * (100 - game.discount) / 100
    } else {
        game.pricePerTurn
    }

    val totalPrice = discountedPrice * quantity
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chi Tiết Game",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.WarmOrange
                )
            )
        },
        bottomBar = {
            // Add to Cart Bottom Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Quantity selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Số lượt:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.PrimaryDark
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                enabled = quantity > 1
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (quantity > 1) AppColors.WarmOrange else AppColors.PrimaryGray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Giảm",
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppColors.SurfaceLight,
                                modifier = Modifier.width(60.dp)
                            ) {
                                Text(
                                    text = quantity.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            IconButton(onClick = { quantity++ }) {
                                Surface(
                                    shape = CircleShape,
                                    color = AppColors.WarmOrange,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Tăng",
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Price summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (game.discount > 0) {
                                Text(
                                    text = "Giá gốc: ${game.pricePerTurn * quantity} đ",
                                    fontSize = 12.sp,
                                    color = AppColors.PrimaryGray,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            }
                            Text(
                                text = "Tổng tiền:",
                                fontSize = 14.sp,
                                color = AppColors.PrimaryGray
                            )
                        }

                        Text(
                            text = "${totalPrice} đ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.WarmOrange
                        )
                    }

                    // Add to cart button
                    Button(
                        onClick = { onAddToCart(game.id, quantity) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.WarmOrange,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thêm Vào Giỏ Hàng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColors.SurfaceLight,
                            Color.White
                        )
                    )
                )
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Game header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Title and rating
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = game.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) { index ->
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (index < game.rating.toInt())
                                            Color(0xFFFFC107)
                                        else
                                            AppColors.PrimaryGray.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "${game.rating} (${game.totalRatings} đánh giá)",
                                    fontSize = 14.sp,
                                    color = AppColors.PrimaryGray
                                )
                            }
                        }

                        // Price display
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            if (game.discount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "-${game.discount}%",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${game.pricePerTurn} đ",
                                    fontSize = 14.sp,
                                    color = AppColors.PrimaryGray,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            }
                            Text(
                                text = "${discountedPrice} đ/lượt",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.WarmOrange
                            )
                        }
                    }

                    // Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameTag(
                            text = if (game.type == GameType.INDOOR) "Trong nhà" else "Ngoài trời",
                            backgroundColor = AppColors.WarmOrangeSoft,
                            textColor = AppColors.WarmOrange
                        )

                        GameTag(
                            text = when (game.riskLevel) {
                                RiskLevel.LOW -> "An toàn"
                                RiskLevel.MEDIUM -> "Vừa phải"
                                RiskLevel.HIGH -> "Mạo hiểm"
                            },
                            backgroundColor = when (game.riskLevel) {
                                RiskLevel.LOW -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                RiskLevel.MEDIUM -> Color(0xFFFFC107).copy(alpha = 0.2f)
                                RiskLevel.HIGH -> Color(0xFFF44336).copy(alpha = 0.2f)
                            },
                            textColor = when (game.riskLevel) {
                                RiskLevel.LOW -> Color(0xFF4CAF50)
                                RiskLevel.MEDIUM -> Color(0xFFFFC107)
                                RiskLevel.HIGH -> Color(0xFFF44336)
                            }
                        )
                    }
                }
            }

            // Description card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Mô Tả",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    Text(
                        text = game.description,
                        fontSize = 14.sp,
                        color = AppColors.PrimaryGray,
                        lineHeight = 22.sp
                    )
                }
            }

            // Game info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Thông Tin Game",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )

                    InfoRow(
                        icon = Icons.Default.Person,
                        label = "Độ tuổi phù hợp",
                        value = game.ageRange
                    )
                    InfoRow(
                        icon = Icons.Default.Height,
                        label = "Chiều cao yêu cầu",
                        value = game.heightRequirement
                    )
                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Vị trí",
                        value = game.location
                    )
                }
            }

            // Extra space for bottom bar
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun GameTag(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = AppColors.WarmOrangeSoft,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AppColors.WarmOrange,
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = AppColors.PrimaryGray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryDark
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameDetailScreenPreview() {
    GameDetailScreen(gameId = "1")
}


