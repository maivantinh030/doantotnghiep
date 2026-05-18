package com.example.appcongvien.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appcongvien.data.model.AnnouncementDTO
import com.example.appcongvien.data.network.RetrofitClient
import com.example.appcongvien.ui.theme.AppColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel(
    announcements: List<AnnouncementDTO>,
    onAnnouncementClick: (AnnouncementDTO) -> Unit = {}
) {
    if (announcements.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = Int.MAX_VALUE / 2, // bắt đầu ở giữa để scroll được cả 2 chiều
        pageCount = { Int.MAX_VALUE }
    )
    val carouselHeight = 140.dp
    val carouselWidth = carouselHeight * (16f / 9f)

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(carouselHeight),
            pageSize = PageSize.Fixed(carouselWidth),
            contentPadding = PaddingValues(horizontal = 50.dp),
            pageSpacing = 30.dp
        ) { page ->
            val item = announcements[page % announcements.size]

            Card(
                modifier = Modifier
                    .height(carouselHeight)
                    .aspectRatio(16f / 9f)
                    .clickable { onAnnouncementClick(item) },
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = if (item.imageUrl.startsWith("http")) {
                            item.imageUrl
                        } else {
                            RetrofitClient.BASE_URL.trimEnd('/') + item.imageUrl
                        },
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (!item.description.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            AppColors.ImageScrim
                                        )
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = item.description,
                                color = AppColors.OnAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(announcements.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(if (pagerState.currentPage % announcements.size == index) 18.dp else 8.dp)
                        .height(8.dp)
                        .background(
                            color = if (pagerState.currentPage % announcements.size == index) {
                                AppColors.WarmOrange
                            } else {
                                AppColors.BorderSubtle
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImageCarouselPreview(){

}