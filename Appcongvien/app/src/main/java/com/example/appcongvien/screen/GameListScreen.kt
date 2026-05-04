package com.example.appcongvien.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.data.model.GameDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.network.RetrofitClient
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.GameViewModel
import coil.compose.AsyncImage

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

private fun resolveGameImageUrl(url: String?): String? {
    val value = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return if (value.startsWith("http://") || value.startsWith("https://")) {
        value
    } else {
        RetrofitClient.BASE_URL.trimEnd('/') + if (value.startsWith("/")) value else "/$value"
    }
}

private data class TagAppearance(
    val label: String,
    val containerColor: Color,
    val contentColor: Color
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

    LaunchedEffect(Unit) {
        viewModel.loadGames(page = 1, size = 50)
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2 || searchQuery.isEmpty()) {
            viewModel.loadGames(page = 1, size = 50, search = searchQuery.ifBlank { null })
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.WarmOrange,
        unfocusedBorderColor = AppColors.BorderSubtle,
        disabledBorderColor = AppColors.BorderSubtle.copy(alpha = 0.7f),
        focusedPlaceholderColor = AppColors.PrimaryGray.copy(alpha = 0.72f),
        unfocusedPlaceholderColor = AppColors.PrimaryGray.copy(alpha = 0.72f),
        focusedLeadingIconColor = AppColors.WarmOrange,
        unfocusedLeadingIconColor = AppColors.PrimaryGray.copy(alpha = 0.72f),
        cursorColor = AppColors.WarmOrange,
        focusedContainerColor = AppColors.SurfaceWhite,
        unfocusedContainerColor = AppColors.SurfaceWhite,
        disabledContainerColor = AppColors.SurfaceWhite
    )

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Danh sách game",
                onBackClick = onBackClick
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
                            AppColors.HeaderGrad1.copy(alpha = 0.18f),
                            AppColors.SurfaceLight,
                            AppColors.SurfaceWhite
                        )
                    )
                )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                shape = RoundedCornerShape(22.dp),
                color = AppColors.SurfaceWhite.copy(alpha = 0.98f),
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Khám phá trò chơi phù hợp",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    Text(
                        text = "Tìm theo tên game để xem giá, đánh giá và điều kiện tham gia.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.84f)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm kiếm game") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                        colors = fieldColors
                    )
                }
            }

            when (gamesState) {
                is Resource.Loading, null -> {
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            StateMessageCard(
                                title = if (searchQuery.isBlank()) "Chưa có game hiển thị" else "Không tìm thấy game phù hợp",
                                message = if (searchQuery.isBlank()) {
                                    "Danh sách game sẽ xuất hiện ở đây khi dữ liệu sẵn sàng."
                                } else {
                                    "Thử đổi từ khóa ngắn hơn hoặc tìm theo tên khác."
                                }
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(games) { game ->
                                GameCardFromDTO(
                                    game = game,
                                    onClick = { onGameClick(game.gameId) }
                                )
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        StateMessageCard(
                            title = "Không thể tải danh sách game",
                            message = (gamesState as Resource.Error).message
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameCardFromDTO(
    game: GameDTO,
    onClick: () -> Unit
) {
    val pricePerTurn = game.pricePerTurn.toDoubleOrNull()?.toInt() ?: 0
    val rating = game.averageRating?.toDoubleOrNull()?.toFloat() ?: 0f
    val riskAppearance = game.riskLevel?.let(::riskAppearance)
    val statusAppearance = statusAppearance(game.status)
    val imageModel = remember(game.thumbnailUrl) { resolveGameImageUrl(game.thumbnailUrl) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppColors.WarmOrangeSoft.copy(alpha = 0.72f),
                    modifier = Modifier.size(64.dp)
                ) {
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = game.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Attractions,
                            contentDescription = null,
                            tint = AppColors.WarmOrange,
                            modifier = Modifier
                                .padding(16.dp)
                                .size(32.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = game.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (rating > 0f) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB21E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = String.format("%.1f", rating),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.PrimaryDark
                                )
                                Text(
                                    text = "(${game.totalReviews})",
                                    fontSize = 12.sp,
                                    color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                                )
                            }
                        } else {
                            Text(
                                text = "Chưa có đánh giá",
                                fontSize = 12.sp,
                                color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                            )
                        }

                        if (game.totalPlays > 0) {
                            Text(
                                text = "${game.totalPlays} lượt chơi",
                                fontSize = 12.sp,
                                color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                            )
                        }
                    }

                    game.shortDescription?.let { description ->
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = AppColors.PrimaryGray.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statusAppearance?.let {
                        TagChip(
                            text = it.label,
                            containerColor = it.containerColor,
                            contentColor = it.contentColor
                        )
                    }
                    Text(
                        text = "Giá / lượt",
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.75f)
                    )
                    Text(
                        text = formatCurrency(pricePerTurn),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.WarmOrange
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                game.location?.let { location ->
                    InfoBadge(
                        icon = Icons.Default.LocationOn,
                        text = location
                    )
                }
                game.category?.let { category ->
                    InfoBadge(text = category)
                }
                game.ageRequired?.let { age ->
                    InfoBadge(
                        icon = Icons.Default.Person,
                        text = "$age+"
                    )
                }
                game.durationMinutes?.let { duration ->
                    InfoBadge(text = "$duration phút")
                }
                riskAppearance?.let {
                    TagChip(
                        text = it.label,
                        containerColor = it.containerColor,
                        contentColor = it.contentColor
                    )
                }
                if (game.isFeatured) {
                    TagChip(
                        text = "Nổi bật",
                        containerColor = AppColors.WarmOrangeSoft.copy(alpha = 0.74f),
                        contentColor = AppColors.WarmOrange,
                        leadingIcon = Icons.Default.Star
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppColors.WarmOrangeSoft.copy(alpha = 0.72f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Attractions,
                        contentDescription = null,
                        tint = AppColors.WarmOrange,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(32.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = game.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB21E),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = game.rating.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.PrimaryDark
                        )
                        Text(
                            text = game.ageRange,
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                        )
                    }
                    Text(
                        text = game.shortDescription,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (game.discount > 0) {
                        Text(
                            text = formatCurrency(game.pricePerTurn),
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray.copy(alpha = 0.72f)
                        )
                    }
                    Text(
                        text = formatCurrency(discountedPrice),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.WarmOrange
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoBadge(
                    icon = Icons.Default.LocationOn,
                    text = game.location
                )
                InfoBadge(
                    text = if (game.type == GameType.INDOOR) "Trong nhà" else "Ngoài trời"
                )
                when (game.riskLevel) {
                    RiskLevel.LOW -> TagChip(
                        text = "An toàn",
                        containerColor = Color(0xFFE6F4EA),
                        contentColor = Color(0xFF2F7D32)
                    )

                    RiskLevel.MEDIUM -> TagChip(
                        text = "Vừa phải",
                        containerColor = Color(0xFFFFF3D6),
                        contentColor = Color(0xFFA66A00)
                    )

                    RiskLevel.HIGH -> TagChip(
                        text = "Mạo hiểm",
                        containerColor = Color(0xFFFDE5E3),
                        contentColor = Color(0xFFC43D2F)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AppColors.SurfaceLight,
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.55f)),
        modifier = Modifier.widthIn(max = 220.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = AppColors.PrimaryGray.copy(alpha = 0.82f),
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                fontSize = 11.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.88f),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun StateMessageCard(
    title: String,
    message: String
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = AppColors.SurfaceWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppColors.WarmOrangeSoft.copy(alpha = 0.7f),
                modifier = Modifier.size(58.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Attractions,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(30.dp)
                )
            }
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.84f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun riskAppearance(level: Int): TagAppearance = when {
    level <= 2 -> TagAppearance(
        label = "An toàn",
        containerColor = Color(0xFFE6F4EA),
        contentColor = Color(0xFF2F7D32)
    )

    level <= 3 -> TagAppearance(
        label = "Vừa phải",
        containerColor = Color(0xFFFFF3D6),
        contentColor = Color(0xFFA66A00)
    )

    else -> TagAppearance(
        label = "Mạo hiểm",
        containerColor = Color(0xFFFDE5E3),
        contentColor = Color(0xFFC43D2F)
    )
}

private fun statusAppearance(status: String): TagAppearance? = when (status) {
    "ACTIVE" -> null
    "INACTIVE" -> TagAppearance(
        label = "Tạm nghỉ",
        containerColor = Color(0xFFFDE5E3),
        contentColor = Color(0xFFC43D2F)
    )

    "MAINTENANCE" -> TagAppearance(
        label = "Bảo trì",
        containerColor = Color(0xFFFFF3D6),
        contentColor = Color(0xFFA66A00)
    )

    else -> TagAppearance(
        label = status,
        containerColor = AppColors.SurfaceLight,
        contentColor = AppColors.PrimaryGray
    )
}

private fun formatCurrency(value: Int): String = String.format("%,d đ", value)

@Preview(showBackground = true)
@Composable
fun GameListScreenPreview() {
    GameListScreen()
}
