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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.GameDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.CartViewModel
import com.example.appcongvien.viewmodel.GameViewModel

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
    val context = LocalContext.current
    val app = context.applicationContext as App
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(app.gameRepository)
    )

    val gamesState by viewModel.gamesState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Load games from API on first launch
    LaunchedEffect(Unit) {
        viewModel.loadGames(page = 1, size = 50)
    }

    // Reload when search query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2 || searchQuery.isEmpty()) {
            viewModel.loadGames(page = 1, size = 50, search = searchQuery.ifBlank { null })
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
            when (gamesState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.WarmOrange)
                    }
                }
                is Resource.Success -> {
                    val games = (gamesState as Resource.Success).data.items
                    if (games.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không tìm thấy game nào",
                                color = AppColors.PrimaryGray,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(games) { game ->
                                GameCardFromDTO(
                                    game = game,
                                    onClick = { onGameClick(game.gameId) }
                                )
                            }

                            // Extra space for bottom navigation
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Lỗi: ${(gamesState as Resource.Error).message}",
                                color = Color.Red,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Vui lòng thử lại sau",
                                color = AppColors.PrimaryGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.WarmOrange)
                    }
                }
            }
        }
    }
}

@Composable
fun GameCardFromDTO(
    game: GameDTO,
    onClick: () -> Unit
) {
    val pricePerTurn = game.pricePerTurn.toDoubleOrNull()?.toInt() ?: 0
    val rating = game.avgRating?.toFloat() ?: 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
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
                            color = AppColors.PrimaryDark,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Rating and age
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (rating > 0) {
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
                                        text = String.format("%.1f", rating),
                                        fontSize = 12.sp,
                                        color = AppColors.PrimaryGray
                                    )
                                }
                            }

                            game.ageRequired?.let { age ->
                                Text(
                                    text = "• $age+",
                                    fontSize = 12.sp,
                                    color = AppColors.PrimaryGray
                                )
                            }
                        }
                    }
                }

                // Price column
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "%,d đ".format(pricePerTurn),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.WarmOrange
                    )
                    game.status?.let { status ->
                        if (status != "ACTIVE") {
                            Text(
                                text = when(status) {
                                    "INACTIVE" -> "Tạm nghỉ"
                                    "MAINTENANCE" -> "Bảo trì"
                                    else -> status
                                },
                                fontSize = 10.sp,
                                color = Color.Red
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            game.shortDescription?.let { desc ->
                Text(
                    text = desc,
                    fontSize = 14.sp,
                    color = AppColors.PrimaryGray,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Info badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Location
                game.location?.let { location ->
                    InfoBadge(
                        icon = Icons.Default.LocationOn,
                        text = location
                    )
                }

                // Category
                game.category?.let { category ->
                    InfoBadge(
                        icon = null,
                        text = category
                    )
                }

                // Risk level
                game.riskLevel?.let { risk ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            risk <= 2 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            risk <= 3 -> Color(0xFFFFC107).copy(alpha = 0.2f)
                            else -> Color(0xFFF44336).copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = when {
                                risk <= 2 -> "An toàn"
                                risk <= 3 -> "Vừa phải"
                                else -> "Mạo hiểm"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                risk <= 2 -> Color(0xFF4CAF50)
                                risk <= 3 -> Color(0xFFFFC107)
                                else -> Color(0xFFF44336)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Featured badge
                if (game.isFeatured) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.WarmOrange.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = AppColors.WarmOrange,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "Nổi bật",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.WarmOrange
                            )
                        }
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