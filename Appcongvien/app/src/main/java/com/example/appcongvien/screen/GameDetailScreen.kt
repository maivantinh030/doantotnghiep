package com.example.appcongvien.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.data.model.GameDTO
import com.example.appcongvien.data.model.GameReviewDTO
import com.example.appcongvien.data.model.PaginatedData
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.network.RetrofitClient
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.GameViewModel
import coil.compose.AsyncImage

private data class DetailTagAppearance(
    val label: String,
    val containerColor: Color,
    val contentColor: Color
)

private fun resolveGameImageUrl(url: String?): String? {
    val value = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val normalizedPath = if (value.startsWith("/")) value else "/$value"
    return RetrofitClient.BASE_URL.trimEnd('/') + normalizedPath
}

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
    val isLoggedIn = tokenManager.hasToken()

    LaunchedEffect(gameId) {
        viewModel.loadGameDetail(gameId)
        viewModel.loadGameReviews(gameId)
        if (isLoggedIn) viewModel.loadMyReview(gameId)
    }

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
                onBackClick = onBackClick
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
                    isLoggedIn = isLoggedIn,
                    myReviewState = myReviewState,
                    reviewsState = reviewsState,
                    createReviewState = createReviewState,
                    updateReviewState = updateReviewState,
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
                        .padding(paddingValues)
                        .background(AppColors.SurfaceLight)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StateMessageCard(
                        title = "Không thể tải chi tiết trò chơi",
                        message = (gameDetailState as Resource.Error).message,
                        actionLabel = "Thử lại",
                        onAction = { viewModel.loadGameDetail(gameId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameDetailContent(
    game: GameDTO,
    isLoggedIn: Boolean,
    myReviewState: Resource<GameReviewDTO?>?,
    reviewsState: Resource<PaginatedData<GameReviewDTO>>?,
    createReviewState: Resource<GameReviewDTO>?,
    updateReviewState: Resource<GameReviewDTO>?,
    onCreateReview: (Int, String?) -> Unit,
    onUpdateReview: (String, Int, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGalleryImage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
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
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            GameHeaderCard(game)
            GameGalleryCard(
                game = game,
                onImageClick = { imageUrl -> selectedGalleryImage = imageUrl }
            )
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
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // SAU - thay bằng đoạn này:
    selectedGalleryImage?.let { imageUrl ->
        Dialog(
            onDismissRequest = { selectedGalleryImage = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false  // Quan trọng - tắt width mặc định của Dialog
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Tap nền đen để đóng
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { selectedGalleryImage = null }
                )

                // Ảnh full width, cao tự động theo tỉ lệ
                AsyncImage(
                    model = imageUrl,
                    contentDescription = game.name,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                )

                // Nút đóng góc trên phải
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(38.dp)
                        .clickable { selectedGalleryImage = null }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✕",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameReviewFormCard(
    isLoggedIn: Boolean,
    myReviewState: Resource<GameReviewDTO?>?,
    createReviewState: Resource<GameReviewDTO>?,
    updateReviewState: Resource<GameReviewDTO>?,
    onCreateReview: (Int, String?) -> Unit,
    onUpdateReview: (String, Int, String?) -> Unit
) {
    GameSectionCard(emphasized = true) {
        SectionHeading(
            title = "Đánh giá của bạn",
            subtitle = if (isLoggedIn) {
                "Chia sẻ trải nghiệm để người chơi khác dễ chọn hơn."
            } else {
                "Đăng nhập để gửi đánh giá sau khi trải nghiệm."
            }
        )

        if (!isLoggedIn) {
            InfoNotice("Đăng nhập để gửi đánh giá cho trò chơi này.")
            return@GameSectionCard
        }

        when (myReviewState) {
            is Resource.Loading, null -> LoadingNotice()
            is Resource.Error -> InfoNotice("Không thể tải đánh giá hiện tại.")
            is Resource.Success -> {
                val existingReview = myReviewState.data
                if (existingReview == null) {
                    ReviewCreateForm(
                        createReviewState = createReviewState,
                        onSubmit = onCreateReview
                    )
                } else {
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

@Composable
private fun ReviewCreateForm(
    createReviewState: Resource<GameReviewDTO>?,
    onSubmit: (Int, String?) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val isSubmitting = createReviewState is Resource.Loading

    LaunchedEffect(createReviewState) {
        if (createReviewState is Resource.Success) {
            selectedRating = 0
            comment = ""
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Chọn số sao",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.PrimaryDark
        )
        StarRatingRow(
            rating = selectedRating,
            onRatingChange = { selectedRating = it }
        )

        OutlinedTextField(
            value = comment,
            onValueChange = { if (it.length <= 500) comment = it },
            placeholder = { Text("Nhận xét của bạn (không bắt buộc)", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 6,
            shape = RoundedCornerShape(16.dp),
            colors = detailFieldColors(),
            supportingText = {
                Text(
                    text = "${comment.length}/500",
                    fontSize = 11.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.78f)
                )
            }
        )

        if (createReviewState is Resource.Error) {
            Text(
                text = createReviewState.message,
                fontSize = 13.sp,
                color = Color(0xFFC43D2F)
            )
        }

        Button(
            onClick = {
                if (selectedRating > 0) {
                    onSubmit(selectedRating, comment.ifBlank { null })
                }
            },
            enabled = selectedRating > 0 && !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = "Gửi đánh giá",
                fontWeight = FontWeight.Bold
            )
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

    LaunchedEffect(updateReviewState) {
        if (updateReviewState is Resource.Success) {
            isEditing = false
        }
    }

    if (!isEditing) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StarDisplayRow(rating = review.rating, size = 18)
                    Text(
                        text = formatReviewDate(review.createdAt),
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.78f)
                    )
                }

                TextButton(onClick = { isEditing = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = AppColors.WarmOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Chỉnh sửa",
                        fontSize = 13.sp,
                        color = AppColors.WarmOrange
                    )
                }
            }

            if (!review.comment.isNullOrBlank()) {
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.9f)
                )
            } else {
                Text(
                    text = "Bạn chưa để lại nhận xét chi tiết.",
                    fontSize = 13.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.75f)
                )
            }

            DetailTagChip(
                text = "Đã xác nhận chơi",
                containerColor = Color(0xFFE6F4EA),
                contentColor = Color(0xFF2F7D32)
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Cập nhật đánh giá",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryDark
            )
            StarRatingRow(
                rating = editRating,
                onRatingChange = { editRating = it }
            )

            OutlinedTextField(
                value = editComment,
                onValueChange = { if (it.length <= 500) editComment = it },
                placeholder = { Text("Nhận xét của bạn", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 6,
                shape = RoundedCornerShape(16.dp),
                colors = detailFieldColors(),
                supportingText = {
                    Text(
                        text = "${editComment.length}/500",
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.78f)
                    )
                }
            )

            if (updateReviewState is Resource.Error) {
                Text(
                    text = updateReviewState.message,
                    fontSize = 13.sp,
                    color = Color(0xFFC43D2F)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(
                    onClick = {
                        editRating = review.rating
                        editComment = review.comment ?: ""
                        isEditing = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Hủy",
                        color = AppColors.PrimaryGray
                    )
                }

                Button(
                    onClick = {
                        onUpdate(review.reviewId, editRating, editComment.ifBlank { null })
                    },
                    enabled = editRating > 0 && !isSubmitting,
                    modifier = Modifier
                        .weight(1.6f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Lưu thay đổi",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StarRatingRow(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = "$i sao",
                tint = if (i <= rating) Color(0xFFFFB21E) else AppColors.PrimaryGray.copy(alpha = 0.55f),
                modifier = Modifier
                    .size(34.dp)
                    .clickable { onRatingChange(i) }
            )
        }
    }
}

@Composable
private fun StarDisplayRow(
    rating: Int,
    size: Int = 16
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFB21E) else AppColors.PrimaryGray.copy(alpha = 0.45f),
                modifier = Modifier.size(size.dp)
            )
        }
    }
}

@Composable
private fun GameReviewsListCard(
    reviewsState: Resource<PaginatedData<GameReviewDTO>>?
) {
    val subtitle = if (reviewsState is Resource.Success) {
        "${reviewsState.data.total} đánh giá gần đây"
    } else {
        "Nhận xét từ người chơi đã trải nghiệm"
    }

    GameSectionCard {
        SectionHeading(
            title = "Đánh giá cộng đồng",
            subtitle = subtitle
        )

        when (reviewsState) {
            is Resource.Loading, null -> LoadingNotice()
            is Resource.Error -> InfoNotice("Không thể tải danh sách đánh giá.")
            is Resource.Success -> {
                val reviews = reviewsState.data.items
                if (reviews.isEmpty()) {
                    InfoNotice("Chưa có đánh giá nào cho trò chơi này.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        reviews.forEachIndexed { index, review ->
                            ReviewItem(review = review)
                            if (index != reviews.lastIndex) {
                                HorizontalDivider(
                                    color = AppColors.BorderSubtle.copy(alpha = 0.72f),
                                    thickness = 1.dp
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AppColors.WarmOrangeSoft.copy(alpha = 0.78f),
            modifier = Modifier.size(44.dp)
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

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = review.userName ?: "Ẩn danh",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.PrimaryDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    StarDisplayRow(rating = review.rating, size = 14)
                }
                Text(
                    text = formatReviewDate(review.createdAt),
                    fontSize = 11.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.78f)
                )
            }

            Text(
                text = review.comment ?: "Không có nhận xét chi tiết.",
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.9f)
            )
        }
    }
}

private fun formatReviewDate(dateStr: String): String {
    return try {
        val date = java.time.Instant.parse(dateStr)
        val formatter = java.time.format.DateTimeFormatter
            .ofPattern("dd/MM/yyyy")
            .withZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
        formatter.format(date)
    } catch (e: Exception) {
        dateStr.take(10)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameHeaderCard(game: GameDTO) {
    val pricePerTurn = game.pricePerTurn.toDoubleOrNull()?.toInt() ?: 0
    val rating = game.averageRating?.toDoubleOrNull()?.toFloat() ?: 0f
    val riskAppearance = game.riskLevel?.let(::detailRiskAppearance)
    val statusAppearance = detailStatusAppearance(game.status)
    val imageModel = remember(game.thumbnailUrl) { resolveGameImageUrl(game.thumbnailUrl) }

    GameSectionCard(emphasized = true) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.WarmOrangeSoft.copy(alpha = 0.76f),
                modifier = Modifier.size(76.dp)
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = game.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Attractions,
                        contentDescription = null,
                        tint = AppColors.WarmOrange,
                        modifier = Modifier
                            .padding(18.dp)
                            .size(40.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = game.name,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )

                game.shortDescription?.let { summary ->
                    Text(
                        text = summary,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.88f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    game.category?.let {
                        DetailTagChip(
                            text = it,
                            containerColor = AppColors.SurfaceLight,
                            contentColor = AppColors.PrimaryDark
                        )
                    }
                    riskAppearance?.let {
                        DetailTagChip(
                            text = it.label,
                            containerColor = it.containerColor,
                            contentColor = it.contentColor
                        )
                    }
                    if (game.isFeatured) {
                        DetailTagChip(
                            text = "Nổi bật",
                            containerColor = AppColors.WarmOrangeSoft.copy(alpha = 0.72f),
                            contentColor = AppColors.WarmOrange,
                            leadingIcon = Icons.Default.Star
                        )
                    }
                    statusAppearance?.let {
                        DetailTagChip(
                            text = it.label,
                            containerColor = it.containerColor,
                            contentColor = it.contentColor
                        )
                    }
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailMetric(
                label = "Giá / lượt",
                value = formatCurrency(pricePerTurn),
                highlight = true
            )
            if (rating > 0f) {
                DetailMetric(
                    label = "Điểm đánh giá",
                    value = String.format("%.1f / 5", rating)
                )
            }
            if (game.totalReviews > 0) {
                DetailMetric(
                    label = "Lượt đánh giá",
                    value = game.totalReviews.toString()
                )
            }
            if (game.durationMinutes != null) {
                DetailMetric(
                    label = "Thời lượng",
                    value = "${game.durationMinutes} phút"
                )
            }
            if (game.maxCapacity != null) {
                DetailMetric(
                    label = "Sức chứa",
                    value = "${game.maxCapacity} người"
                )
            }
        }
    }
}

@Composable
private fun GameDescriptionCard(game: GameDTO) {
    val summary = game.shortDescription?.trim().orEmpty()
    val fullDescription = game.description?.trim().orEmpty()
    val displayDescription = when {
        fullDescription.isNotBlank() -> fullDescription
        summary.isNotBlank() -> summary
        else -> "Chưa có mô tả cho trò chơi này."
    }

    GameSectionCard {
        SectionHeading(
            title = "Mô tả",
            subtitle = "Thông tin trải nghiệm và cảm giác chung của trò chơi"
        )

        if (summary.isNotBlank() && summary != displayDescription) {
            Text(
                text = summary,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryDark
            )
        }

        Text(
            text = displayDescription,
            fontSize = 14.sp,
            lineHeight = 23.sp,
            color = AppColors.PrimaryGray.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun GameGalleryCard(
    game: GameDTO,
    onImageClick: (String) -> Unit
) {
    val items = remember(game.galleryUrls) {
        game.galleryUrls
            ?.mapNotNull { resolveGameImageUrl(it) }
            ?.filter { it.isNotBlank() }
            .orEmpty()
    }
    if (items.isEmpty()) return

    GameSectionCard {
        SectionHeading(
            title = "Gallery",
            subtitle = "Hinh anh thuc te cua tro choi"
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items) { imageUrl ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.SurfaceLight,
                    modifier = Modifier.size(150.dp, 110.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = game.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onImageClick(imageUrl) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameRequirementsCard(game: GameDTO) {
    GameSectionCard {
        SectionHeading(
            title = "Thông tin & điều kiện",
            subtitle = "Rà nhanh vị trí và yêu cầu tham gia trước khi chơi"
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

        game.maxCapacity?.let { capacity ->
            InfoRow(
                icon = Icons.Default.Person,
                label = "Sức chứa",
                value = "Tối đa $capacity người mỗi lượt"
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AppColors.WarmOrangeSoft.copy(alpha = 0.72f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.WarmOrange,
                modifier = Modifier
                    .padding(10.dp)
                    .size(20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.PrimaryGray.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryDark
            )
        }
    }
}

@Composable
private fun GameSectionCard(
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    if (emphasized) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark
        )
        subtitle?.let {
            Text(
                text = it,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun DetailMetric(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    if (highlight) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.WarmOrangeSoft.copy(alpha = 0.68f),
            border = BorderStroke(1.dp, AppColors.WarmOrangeSoft.copy(alpha = 0.92f)),
            modifier = Modifier.widthIn(min = 120.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.76f)
                )
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.WarmOrange
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.widthIn(min = 88.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.74f)
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryDark
            )
        }
    }
}

@Composable
private fun DetailTagChip(
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
private fun InfoNotice(message: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.SurfaceLight.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.55f))
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = AppColors.PrimaryGray.copy(alpha = 0.88f),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun LoadingNotice() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(30.dp),
            color = AppColors.WarmOrange,
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun StateMessageCard(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
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
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = actionLabel,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun detailFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppColors.WarmOrange,
    unfocusedBorderColor = AppColors.BorderSubtle,
    disabledBorderColor = AppColors.BorderSubtle.copy(alpha = 0.7f),
    focusedContainerColor = AppColors.SurfaceWhite,
    unfocusedContainerColor = AppColors.SurfaceWhite,
    disabledContainerColor = AppColors.SurfaceWhite,
    cursorColor = AppColors.WarmOrange,
    focusedPlaceholderColor = AppColors.PrimaryGray.copy(alpha = 0.72f),
    unfocusedPlaceholderColor = AppColors.PrimaryGray.copy(alpha = 0.72f)
)

private fun detailRiskAppearance(level: Int): DetailTagAppearance = when {
    level <= 2 -> DetailTagAppearance(
        label = "An toàn",
        containerColor = Color(0xFFE6F4EA),
        contentColor = Color(0xFF2F7D32)
    )

    level <= 3 -> DetailTagAppearance(
        label = "Vừa phải",
        containerColor = Color(0xFFFFF3D6),
        contentColor = Color(0xFFA66A00)
    )

    else -> DetailTagAppearance(
        label = "Mạo hiểm",
        containerColor = Color(0xFFFDE5E3),
        contentColor = Color(0xFFC43D2F)
    )
}

private fun detailStatusAppearance(status: String): DetailTagAppearance? = when (status) {
    "ACTIVE" -> null
    "INACTIVE" -> DetailTagAppearance(
        label = "Tạm nghỉ",
        containerColor = Color(0xFFFDE5E3),
        contentColor = Color(0xFFC43D2F)
    )

    "MAINTENANCE" -> DetailTagAppearance(
        label = "Bảo trì",
        containerColor = Color(0xFFFFF3D6),
        contentColor = Color(0xFFA66A00)
    )

    else -> DetailTagAppearance(
        label = status,
        containerColor = AppColors.SurfaceLight,
        contentColor = AppColors.PrimaryGray
    )
}

private fun formatCurrency(value: Int): String = String.format("%,d đ", value)
