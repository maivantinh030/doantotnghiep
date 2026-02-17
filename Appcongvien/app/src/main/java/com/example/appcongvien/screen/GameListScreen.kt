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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors

enum class GameType {
    INDOOR, OUTDOOR
}

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

data class Game(
    val id: String,
    val name: String,
    val shortDescription: String,
    val pricePerTurn: Int,
    val discount: Int = 0,
    val ageRange: String,
    val heightRequirement: String,
    val location: String,
    val type: GameType,
    val riskLevel: RiskLevel,
    val rating: Float = 4.5f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onGameClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val games = remember {
        listOf(
            Game(
                id = "1",
                name = "Đu Quay Khổng Lồ",
                shortDescription = "Trải nghiệm cảm giác mạnh với đu quay cao 50m, ngắm toàn cảnh công viên từ trên cao",
                pricePerTurn = 50000,
                discount = 10,
                ageRange = "8+",
                heightRequirement = "Từ 1.2m",
                location = "Khu vực A",
                type = GameType.OUTDOOR,
                riskLevel = RiskLevel.HIGH,
                rating = 4.8f
            ),
            Game(
                id = "2",
                name = "Tàu Lượn Siêu Tốc",
                shortDescription = "Cảm giác tốc độ với những khúc cua nghẹt thở và vòng lộn 360 độ",
                pricePerTurn = 80000,
                discount = 15,
                ageRange = "12+",
                heightRequirement = "Từ 1.4m",
                location = "Khu vực B",
                type = GameType.OUTDOOR,
                riskLevel = RiskLevel.HIGH,
                rating = 4.9f
            ),
            Game(
                id = "3",
                name = "Vòng Quay May Mắn",
                shortDescription = "Trò chơi thư giãn cho cả gia đình với tầm nhìn đẹp",
                pricePerTurn = 30000,
                discount = 0,
                ageRange = "3+",
                heightRequirement = "Không hạn chế",
                location = "Khu vực C",
                type = GameType.OUTDOOR,
                riskLevel = RiskLevel.LOW,
                rating = 4.3f
            ),
            Game(
                id = "4",
                name = "Nhà Ma Bí Ẩn",
                shortDescription = "Khám phá những bí mật kinh hoàng trong ngôi nhà ma ám",
                pricePerTurn = 60000,
                discount = 20,
                ageRange = "10+",
                heightRequirement = "Không hạn chế",
                location = "Khu vực D",
                type = GameType.INDOOR,
                riskLevel = RiskLevel.MEDIUM,
                rating = 4.6f
            ),
            Game(
                id = "5",
                name = "Thuyền Đạp Hồ Sen",
                shortDescription = "Thư giãn với thuyền đạp trên hồ sen xanh mát",
                pricePerTurn = 40000,
                discount = 5,
                ageRange = "5+",
                heightRequirement = "Có người lớn kèm",
                location = "Hồ Sen",
                type = GameType.OUTDOOR,
                riskLevel = RiskLevel.LOW,
                rating = 4.4f
            )
        )
    }

    val filteredGames = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            games
        } else {
            games.filter { game ->
                game.name.contains(searchQuery, ignoreCase = true) ||
                        game.shortDescription.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Danh Sách Game",
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
        ) {

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Tìm kiếm game...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AppColors.WarmOrange
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.WarmOrange,
                    focusedLabelColor = AppColors.WarmOrange,
                    cursorColor = AppColors.WarmOrange
                )
            )

            // Games list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredGames.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không tìm thấy game nào",
                                color = AppColors.PrimaryGray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    items(filteredGames) { game ->
                        GameCard(
                            game = game,
                            onClick = { onGameClick(game.id) }
                        )
                    }

                    // Extra space for bottom navigation
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    game: Game,
    onClick: () -> Unit
) {
    val discountedPrice = if (game.discount > 0) {
        game.pricePerTurn * (100 - game.discount) / 100
    } else {
        game.pricePerTurn
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                // Game icon and basic info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppColors.WarmOrangeSoft,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Attractions,
                            contentDescription = null,
                            tint = AppColors.WarmOrange,
                            modifier = Modifier
                                .padding(14.dp)
                                .size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = game.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark
                        )

                        // Rating and age
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = game.rating.toString(),
                                    fontSize = 12.sp,
                                    color = AppColors.PrimaryGray
                                )
                            }

                            Text(
                                text = "• ${game.ageRange}",
                                fontSize = 12.sp,
                                color = AppColors.PrimaryGray
                            )
                        }
                    }
                }

                // Price column
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (game.discount > 0) {
                        Text(
                            text = "${game.pricePerTurn} đ",
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "-${game.discount}%",
                                fontSize = 10.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${discountedPrice} đ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.WarmOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = game.shortDescription,
                fontSize = 14.sp,
                color = AppColors.PrimaryGray,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Info badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location
                InfoBadge(
                    icon = Icons.Default.LocationOn,
                    text = game.location
                )

                // Type
                InfoBadge(
                    icon = null,
                    text = if (game.type == GameType.INDOOR) "Trong nhà" else "Ngoài trời"
                )

                // Risk level
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (game.riskLevel) {
                        RiskLevel.LOW -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        RiskLevel.MEDIUM -> Color(0xFFFFC107).copy(alpha = 0.2f)
                        RiskLevel.HIGH -> Color(0xFFF44336).copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = when (game.riskLevel) {
                            RiskLevel.LOW -> "An toàn"
                            RiskLevel.MEDIUM -> "Vừa phải"
                            RiskLevel.HIGH -> "Mạo hiểm"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (game.riskLevel) {
                            RiskLevel.LOW -> Color(0xFF4CAF50)
                            RiskLevel.MEDIUM -> Color(0xFFFFC107)
                            RiskLevel.HIGH -> Color(0xFFF44336)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AppColors.SurfaceLight
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    it,
                    contentDescription = null,
                    tint = AppColors.PrimaryGray,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = text,
                fontSize = 10.sp,
                color = AppColors.PrimaryGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameListScreenPreview() {
    GameListScreen()
}