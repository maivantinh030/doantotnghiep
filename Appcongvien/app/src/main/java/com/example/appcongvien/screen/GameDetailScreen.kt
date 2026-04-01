package com.example.appcongvien.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import com.example.appcongvien.components.ParkTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.GameDTO
import com.example.appcongvien.data.model.GameReviewDTO
import com.example.appcongvien.data.model.PaginatedData
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(app.gameRepository)
    )
    val tokenManager = app.tokenManager

    val gameDetailState by viewModel.gameDetailState.collectAsState()
    val reviewsState by viewModel.reviewsState.collectAsState()
    val myReviewState by viewModel.myReviewState.collectAsState()
    val createReviewState by viewModel.createReviewState.collectAsState()
    val updateReviewState by viewModel.updateReviewState.collectAsState()
    var quantity by remember { mutableStateOf(1) }

    val isLoggedIn = tokenManager.hasToken()

    LaunchedEffect(gameId) {
        viewModel.loadGameDetail(gameId)
        viewModel.loadGameReviews(gameId)
        if (isLoggedIn) viewModel.loadMyReview(gameId)
    }

    // Sau khi tạo/sửa review thành công → reload
    LaunchedEffect(createReviewState) {
        if (createReviewState is Resource.Success) {
            viewModel.loadMyReview(gameId)
            viewModel.loadGameReviews(gameId)
            viewModel.loadGameDetail(gameId)
            viewModel.resetCreateReviewState()
        }
    }
    LaunchedEffect(updateReviewState) {
        if (updateReviewState is Resource.Success) {
            viewModel.loadMyReview(gameId)
            viewModel.loadGameReviews(gameId)
            viewModel.loadGameDetail(gameId)
            viewModel.resetUpdateReviewState()
        }
    }

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Chi tiết trò chơi",
                onBackClick = onBackClick,
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
                    isLoggedIn = isLoggedIn,
                    myReviewState = myReviewState,
                    reviewsState = reviewsState,
                    createReviewState = createReviewState,
                    updateReviewState = updateReviewState,
                    onQuantityChange = { quantity = it },
                    onCreateReview = { rating, comment ->
                        viewModel.createReview(gameId, rating, comment)
                    },
                    onUpdateReview = { reviewId, rating, comment ->
                        viewModel.updateReview(reviewId, rating, comment)
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
    isLoggedIn: Boolean,
    myReviewState: Resource<GameReviewDTO?>?,
    reviewsState: Resource<PaginatedData<GameReviewDTO>>?,
    createReviewState: Resource<GameReviewDTO>?,
    updateReviewState: Resource<GameReviewDTO>?,
    onQuantityChange: (Int) -> Unit,
    onCreateReview: (Int, String?) -> Unit,
    onUpdateReview: (String, Int, String?) -> Unit,
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
            GameReviewFormCard(
                isLoggedIn = isLoggedIn,
                myReviewState = myReviewState,
                createReviewState = createReviewState,
                updateReviewState = updateReviewState,
                onCreateReview = onCreateReview,
                onUpdateReview = onUpdateReview
            )
            GameReviewsListCard(reviewsState = reviewsState)
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

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.WarmOrange.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = AppColors.WarmOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Chạm thẻ tại thiết bị",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.WarmOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

// ===== Review Form Card =====

@Composable
private fun GameReviewFormCard(
    isLoggedIn: Boolean,
    myReviewState: Resource<GameReviewDTO?>?,
    createReviewState: Resource<GameReviewDTO>?,
    updateReviewState: Resource<GameReviewDTO>?,
    onCreateReview: (Int, String?) -> Unit,
    onUpdateReview: (String, Int, String?) -> Unit
) {
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
                text = "Đánh giá của bạn",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark
            )

            if (!isLoggedIn) {
                Text(
                    text = "Đăng nhập để gửi đánh giá",
                    fontSize = 14.sp,
                    color = AppColors.PrimaryGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                return@Column
            }

            when (myReviewState) {
                is Resource.Loading, null -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = AppColors.WarmOrange,
                            strokeWidth = 2.dp
                        )
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = "Không thể tải đánh giá",
                        fontSize = 14.sp,
                        color = AppColors.PrimaryGray
                    )
                }
                is Resource.Success -> {
                    val existingReview = myReviewState.data
                    if (existingReview == null) {
                        // Chưa đánh giá — form tạo mới
                        ReviewCreateForm(
                            createReviewState = createReviewState,
                            onSubmit = onCreateReview
                        )
                    } else {
                        // Đã đánh giá — hiển thị + nút sửa
                        ReviewEditSection(
                            review = existingReview,
                            updateReviewState = updateReviewState,
                            onUpdate = onUpdateReview
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCreateForm(
    createReviewState: Resource<GameReviewDTO>?,
    onSubmit: (Int, String?) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val isSubmitting = createReviewState is Resource.Loading

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Chọn số sao:", fontSize = 13.sp, color = AppColors.PrimaryGray)
        StarRatingRow(rating = selectedRating, onRatingChange = { selectedRating = it })

        OutlinedTextField(
            value = comment,
            onValueChange = { if (it.length <= 500) comment = it },
            placeholder = { Text("Nhận xét của bạn (không bắt buộc)", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.WarmOrange,
                unfocusedBorderColor = AppColors.SurfaceLight
            ),
            supportingText = { Text("${comment.length}/500", fontSize = 11.sp, color = AppColors.PrimaryGray) }
        )

        if (createReviewState is Resource.Error) {
            Text(
                text = createReviewState.message,
                fontSize = 13.sp,
                color = Color.Red
            )
        }

        Button(
            onClick = {
                if (selectedRating > 0) onSubmit(selectedRating, comment.ifBlank { null })
            },
            enabled = selectedRating > 0 && !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text("Gửi đánh giá", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReviewEditSection(
    review: GameReviewDTO,
    updateReviewState: Resource<GameReviewDTO>?,
    onUpdate: (String, Int, String?) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editRating by remember { mutableIntStateOf(review.rating) }
    var editComment by remember { mutableStateOf(review.comment ?: "") }
    val isSubmitting = updateReviewState is Resource.Loading

    if (!isEditing) {
        // Hiển thị review hiện tại
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StarDisplayRow(rating = review.rating, size = 20)
                TextButton(onClick = { isEditing = true }) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = AppColors.WarmOrange)
                    Spacer(Modifier.width(4.dp))
                    Text("Chỉnh sửa", color = AppColors.WarmOrange, fontSize = 13.sp)
                }
            }
            if (!review.comment.isNullOrBlank()) {
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    color = AppColors.PrimaryGray,
                    lineHeight = 20.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Text(
                    text = "Đã xác nhận chơi",
                    fontSize = 11.sp,
                    color = Color(0xFF388E3C),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    } else {
        // Form chỉnh sửa
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Chỉnh sửa đánh giá", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.PrimaryDark)
            StarRatingRow(rating = editRating, onRatingChange = { editRating = it })

            OutlinedTextField(
                value = editComment,
                onValueChange = { if (it.length <= 500) editComment = it },
                placeholder = { Text("Nhận xét của bạn", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.WarmOrange,
                    unfocusedBorderColor = AppColors.SurfaceLight
                ),
                supportingText = { Text("${editComment.length}/500", fontSize = 11.sp, color = AppColors.PrimaryGray) }
            )

            if (updateReviewState is Resource.Error) {
                Text(text = updateReviewState.message, fontSize = 13.sp, color = Color.Red)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        editRating = review.rating
                        editComment = review.comment ?: ""
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Hủy", color = AppColors.PrimaryGray)
                }
                Button(
                    onClick = {
                        onUpdate(review.reviewId, editRating, editComment.ifBlank { null })
                        isEditing = false
                    },
                    enabled = editRating > 0 && !isSubmitting,
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Lưu", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StarRatingRow(rating: Int, onRatingChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = "$i sao",
                tint = if (i <= rating) Color(0xFFFFC107) else AppColors.PrimaryGray,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onRatingChange(i) }
            )
        }
    }
}

@Composable
private fun StarDisplayRow(rating: Int, size: Int = 16) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFC107) else AppColors.PrimaryGray,
                modifier = Modifier.size(size.dp)
            )
        }
    }
}

// ===== Reviews List Card =====

@Composable
private fun GameReviewsListCard(
    reviewsState: Resource<PaginatedData<GameReviewDTO>>?
) {
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
            val totalText = if (reviewsState is Resource.Success)
                "Đánh giá (${reviewsState.data.total})"
            else "Đánh giá"
            Text(
                text = totalText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark
            )

            when (reviewsState) {
                is Resource.Loading, null -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = AppColors.WarmOrange,
                            strokeWidth = 2.dp
                        )
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = "Không thể tải danh sách đánh giá",
                        fontSize = 14.sp,
                        color = AppColors.PrimaryGray
                    )
                }
                is Resource.Success -> {
                    val reviews = reviewsState.data.items
                    if (reviews.isEmpty()) {
                        Text(
                            text = "Chưa có đánh giá nào",
                            fontSize = 14.sp,
                            color = AppColors.PrimaryGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        reviews.forEach { review ->
                            ReviewItem(review = review)
                            if (review != reviews.last()) {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(AppColors.SurfaceLight)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(review: GameReviewDTO) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar initials
        Surface(
            shape = CircleShape,
            color = AppColors.WarmOrangeSoft,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = (review.userName?.firstOrNull()?.uppercaseChar() ?: '?').toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.WarmOrange
                )
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = review.userName ?: "Ẩn danh",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.PrimaryDark
                )
                Text(
                    text = formatReviewDate(review.createdAt),
                    fontSize = 11.sp,
                    color = AppColors.PrimaryGray
                )
            }
            StarDisplayRow(rating = review.rating, size = 14)
            if (!review.comment.isNullOrBlank()) {
                Text(
                    text = review.comment,
                    fontSize = 13.sp,
                    color = AppColors.PrimaryGray,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

private fun formatReviewDate(dateStr: String): String {
    return try {
        // dateStr có thể là ISO 8601: "2024-01-15T10:30:00Z"
        val date = java.time.Instant.parse(dateStr)
        val formatter = java.time.format.DateTimeFormatter
            .ofPattern("dd/MM/yyyy")
            .withZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
        formatter.format(date)
    } catch (e: Exception) {
        dateStr.take(10)
    }
}

// ===== Existing Cards (unchanged) =====

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

                game.averageRating?.toDoubleOrNull()?.let { rating ->
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
                            if (game.totalReviews > 0) {
                                Text(
                                    text = "(${game.totalReviews} đánh giá)",
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
