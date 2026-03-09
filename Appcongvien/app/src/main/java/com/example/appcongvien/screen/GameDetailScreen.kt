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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.appcongvien.components.ParkTopAppBar
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.GameDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onAddToCart: (String, Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(app.gameRepository)
    )
    val cartViewModel = app.cartViewModel

    val gameDetailState by viewModel.gameDetailState.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    var quantity by remember { mutableStateOf(1) }

    val isInCart = cartItems.any { it.gameId == gameId }
    val cartItemCount = cartItems.sumOf { it.quantity }

    LaunchedEffect(gameId) {
        viewModel.loadGameDetail(gameId)
    }

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Chi tiết trò chơi",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = onCartClick) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Giỏ hàng",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (gameDetailState) {
            is Resource.Loading, null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }
            is Resource.Success -> {
                val game = (gameDetailState as Resource.Success<GameDTO>).data
                GameDetailContent(
                    game = game,
                    quantity = quantity,
                    isInCart = isInCart,
                    onQuantityChange = { quantity = it },
                    onAddToCart = {
                        cartViewModel.addToCart(
                            gameId = game.gameId,
                            gameName = game.name,
                            pricePerTurn = game.pricePerTurn.toDoubleOrNull()?.toInt() ?: 0,
                            discount = 0
                        )
                    },
                    modifier = modifier.padding(paddingValues)
                )
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Lỗi: ${(gameDetailState as Resource.Error).message}",
                            color = Color.Red,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.loadGameDetail(gameId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.WarmOrange
                            )
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameDetailContent(
    game: GameDTO,
    quantity: Int,
    isInCart: Boolean,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pricePerTurn = game.pricePerTurn.toDoubleOrNull() ?: 0.0
    val totalPrice = pricePerTurn * quantity

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.SurfaceLight,
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GameHeaderCard(game)
            GameDescriptionCard(game)
            GameRequirementsCard(game)
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Bottom action bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Quantity selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Số lượng:",
                        fontSize = 14.sp,
                        color = AppColors.PrimaryGray
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                            enabled = quantity > 1
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Giảm",
                                tint = if (quantity > 1) AppColors.WarmOrange else AppColors.PrimaryGray
                            )
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
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        IconButton(
                            onClick = { if (quantity < 10) onQuantityChange(quantity + 1) },
                            enabled = quantity < 10
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Tăng",
                                tint = if (quantity < 10) AppColors.WarmOrange else AppColors.PrimaryGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tổng cộng",
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray
                        )
                        Text(
                            text = "${totalPrice.toInt()} đ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.WarmOrange
                        )
                    }

                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInCart) Color(0xFF4CAF50) else AppColors.WarmOrange
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            if (isInCart) Icons.Default.Check else Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isInCart) "Đã thêm vào giỏ" else "Thêm vào giỏ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameHeaderCard(game: GameDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AppColors.WarmOrangeSoft,
                modifier = Modifier.size(70.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(18.dp)
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
                    color = AppColors.PrimaryDark
                )

                game.avgRating?.let { rating ->
                    if (rating > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = String.format("%.1f", rating),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            game.totalReviews?.let { reviews ->
                                Text(
                                    text ="($reviews đánh giá)",
                                    fontSize = 12.sp,
                                    color = AppColors.PrimaryGray
                                )
                            }
                        }
                    }
                }

                game.category?.let { category ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.WarmOrangeSoft
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            color = AppColors.WarmOrange,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameDescriptionCard(game: GameDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Mô tả",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark
            )

            Text(
                text = game.description ?: game.shortDescription ?: "Không có mô tả",
                fontSize = 14.sp,
                color = AppColors.PrimaryGray,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun GameRequirementsCard(game: GameDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Thông tin",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark
            )

            game.location?.let { location ->
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Vị trí",
                    value = location
                )
            }

            game.ageRequired?.let { age ->
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Độ tuổi",
                    value = "Từ $age tuổi trở lên"
                )
            }

            game.heightRequired?.let { height ->
                InfoRow(
                    icon = Icons.Default.Height,
                    label = "Chiều cao",
                    value = "Tối thiểu ${height}cm"
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
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

