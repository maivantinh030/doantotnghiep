package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.data.model.CreateGameRequest
import com.park.data.model.GameDTO
import com.park.data.model.UpdateGameRequest
import com.park.ui.component.*
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography
import com.park.viewmodel.GameManagementViewModel

@Composable
fun GameManagementScreen(viewModel: GameManagementViewModel = viewModel { GameManagementViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceLight)
            .padding(24.dp)
    ) {
        PageHeader(
            title = "Quản lý Trò chơi",
            subtitle = "${uiState.total} trò chơi trong hệ thống"
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.search(it) },
                placeholder = "Tìm kiếm trò chơi...",
                modifier = Modifier.width(240.dp)
            )
            Button(
                onClick = { viewModel.showCreateDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Thêm mới")
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            SnackbarMessage(uiState.successMessage, isError = false, modifier = Modifier.align(Alignment.TopEnd))
            SnackbarMessage(uiState.errorMessage, isError = true, modifier = Modifier.align(Alignment.TopEnd))
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.WarmOrange)
            }
        } else {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(AppColors.SurfaceLight).padding(16.dp, 12.dp)
                    ) {
                        Text("Tên trò chơi", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(2f))
                        Text("Danh mục", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Giá/lượt", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Đánh giá", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Trạng thái", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Thao tác", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                    }
                    Divider(color = AppColors.LightGray)
                    LazyColumn {
                        items(uiState.games) { game ->
                            GameRow(
                                game = game,
                                onEdit = { viewModel.showEditDialog(game) },
                                onDelete = { viewModel.deleteGame(game.gameId) }
                            )
                            Divider(color = AppColors.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    // Create Dialog
    if (uiState.showCreateDialog) {
        GameFormDialog(
            title = "Thêm Trò chơi mới",
            onDismiss = { viewModel.dismissDialogs() },
            onCreateConfirm = { request ->
                viewModel.createGame(request)
            }
        )
    }

    // Edit Dialog
    if (uiState.showEditDialog && uiState.selectedGame != null) {
        val game = uiState.selectedGame!!
        GameFormDialog(
            title = "Cập nhật Trò chơi",
            existingGame = game,
            onDismiss = { viewModel.dismissDialogs() },
            onUpdateConfirm = { request ->
                viewModel.updateGame(game.gameId, request)
            }
        )
    }
}

@Composable
private fun GameRow(
    game: GameDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(game.name, style = AppTypography.bodyMedium, color = AppColors.PrimaryDark, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (game.isFeatured) {
                    Text("★ Nổi bật", color = AppColors.YellowWarning, fontSize = 11.dp.value.sp)
                }
                game.ageRequired?.let {
                    Text("Tuổi: ${it}+", color = AppColors.PrimaryGray, fontSize = 11.dp.value.sp)
                }
                game.heightRequired?.let {
                    Text("Cao: ${it}cm", color = AppColors.PrimaryGray, fontSize = 11.dp.value.sp)
                }
                game.maxCapacity?.let {
                    Text("SL: $it", color = AppColors.PrimaryGray, fontSize = 11.dp.value.sp)
                }
            }
        }
        Text(game.category ?: "-", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
        Text(formatCurrencyFull(game.pricePerTurn.toDoubleOrNull() ?: 0.0), style = AppTypography.bodyMedium, color = AppColors.PrimaryDark, modifier = Modifier.weight(1f))
        Text(
            game.avgRating?.let { if (it > 0) "★ %.1f".format(it) else "-" } ?: "-",
            style = AppTypography.bodyMedium, color = AppColors.YellowWarning, modifier = Modifier.weight(1f)
        )
        Box(modifier = Modifier.weight(1f)) { StatusBadge(game.status) }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = AppColors.BluePrimary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = AppColors.RedError, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun GameFormDialog(
    title: String,
    existingGame: GameDTO? = null,
    onDismiss: () -> Unit,
    onCreateConfirm: ((CreateGameRequest) -> Unit)? = null,
    onUpdateConfirm: ((UpdateGameRequest) -> Unit)? = null
) {
    var name by remember { mutableStateOf(existingGame?.name ?: "") }
    var category by remember { mutableStateOf(existingGame?.category ?: "") }
    var priceText by remember { mutableStateOf(existingGame?.pricePerTurn ?: "") }
    var shortDesc by remember { mutableStateOf(existingGame?.shortDescription ?: "") }
    var desc by remember { mutableStateOf(existingGame?.description ?: "") }
    var location by remember { mutableStateOf(existingGame?.location ?: "") }
    var thumbnailUrl by remember { mutableStateOf(existingGame?.thumbnailUrl ?: "") }
    var durationText by remember { mutableStateOf(existingGame?.durationMinutes?.toString() ?: "") }
    var ageRequiredText by remember { mutableStateOf(existingGame?.ageRequired?.toString() ?: "") }
    var heightRequiredText by remember { mutableStateOf(existingGame?.heightRequired?.toString() ?: "") }
    var maxCapacityText by remember { mutableStateOf(existingGame?.maxCapacity?.toString() ?: "") }
    var riskLevelText by remember { mutableStateOf(existingGame?.riskLevel?.toString() ?: "") }
    var status by remember { mutableStateOf(existingGame?.status ?: "ACTIVE") }
    var isFeatured by remember { mutableStateOf(existingGame?.isFeatured ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.White),
            modifier = Modifier.width(600.dp).heightIn(max = 700.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = AppTypography.headlineLarge)
                Spacer(Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.WarmOrange,
                            focusedLabelColor = AppColors.WarmOrange
                        )
                        
                        // Tên trò chơi *
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("Tên trò chơi *") },
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        // Danh mục *
                        OutlinedTextField(
                            value = category, onValueChange = { category = it },
                            label = { Text("Danh mục *") },
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        // Giá *
                        OutlinedTextField(
                            value = priceText, onValueChange = { priceText = it },
                            label = { Text("Giá/lượt (VND) *") },
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        // Mô tả ngắn
                        OutlinedTextField(
                            value = shortDesc, onValueChange = { shortDesc = it },
                            label = { Text("Mô tả ngắn") },
                            shape = RoundedCornerShape(12.dp), maxLines = 2,
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        // Mô tả đầy đủ
                        OutlinedTextField(
                            value = desc, onValueChange = { desc = it },
                            label = { Text("Mô tả chi tiết") },
                            shape = RoundedCornerShape(12.dp), maxLines = 3,
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        // Vị trí
                        OutlinedTextField(
                            value = location, onValueChange = { location = it },
                            label = { Text("Vị trí") },
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        // Thumbnail URL
                        OutlinedTextField(
                            value = thumbnailUrl, onValueChange = { thumbnailUrl = it },
                            label = { Text("URL ảnh đại diện") },
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Thời lượng
                            OutlinedTextField(
                                value = durationText, onValueChange = { durationText = it },
                                label = { Text("Thời lượng (phút)") },
                                shape = RoundedCornerShape(12.dp), singleLine = true,
                                modifier = Modifier.weight(1f), colors = fieldColors
                            )
                            
                            // Tuổi tối thiểu
                            OutlinedTextField(
                                value = ageRequiredText, onValueChange = { ageRequiredText = it },
                                label = { Text("Tuổi tối thiểu") },
                                shape = RoundedCornerShape(12.dp), singleLine = true,
                                modifier = Modifier.weight(1f), colors = fieldColors
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Chiều cao tối thiểu
                            OutlinedTextField(
                                value = heightRequiredText, onValueChange = { heightRequiredText = it },
                                label = { Text("Chiều cao (cm)") },
                                shape = RoundedCornerShape(12.dp), singleLine = true,
                                modifier = Modifier.weight(1f), colors = fieldColors
                            )
                            
                            // Sức chứa
                            OutlinedTextField(
                                value = maxCapacityText, onValueChange = { maxCapacityText = it },
                                label = { Text("Sức chứa") },
                                shape = RoundedCornerShape(12.dp), singleLine = true,
                                modifier = Modifier.weight(1f), colors = fieldColors
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Mức độ rủi ro
                            OutlinedTextField(
                                value = riskLevelText, onValueChange = { riskLevelText = it },
                                label = { Text("Mức độ rủi ro (1-5)") },
                                shape = RoundedCornerShape(12.dp), singleLine = true,
                                modifier = Modifier.weight(1f), colors = fieldColors
                            )
                            
                            // Trạng thái (chỉ cho edit)
                            if (existingGame != null) {
                                Box(modifier = Modifier.weight(1f)) {
                                    var expanded by remember { mutableStateOf(false) }
                                    OutlinedTextField(
                                        value = when(status) {
                                            "ACTIVE" -> "Hoạt động"
                                            "MAINTENANCE" -> "Bảo trì"
                                            "CLOSED" -> "Đóng cửa"
                                            else -> status
                                        },
                                        onValueChange = {},
                                        label = { Text("Trạng thái") },
                                        readOnly = true,
                                        trailingIcon = {
                                            IconButton(onClick = { expanded = !expanded }) {
                                                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = fieldColors
                                    )
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        DropdownMenuItem(
                                            text = { Text("Hoạt động") },
                                            onClick = { status = "ACTIVE"; expanded = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Bảo trì") },
                                            onClick = { status = "MAINTENANCE"; expanded = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Đóng cửa") },
                                            onClick = { status = "CLOSED"; expanded = false }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        
                        // Nổi bật
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { isFeatured = !isFeatured }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isFeatured,
                                onCheckedChange = { isFeatured = it },
                                colors = CheckboxDefaults.colors(checkedColor = AppColors.WarmOrange)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Hiển thị nổi bật", style = AppTypography.bodyMedium)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val priceValue = priceText.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && category.isNotBlank() && priceValue > 0) {
                                if (existingGame != null) {
                                    // Update
                                    onUpdateConfirm?.invoke(
                                        UpdateGameRequest(
                                            name = name,
                                            category = category,
                                            pricePerTurn = priceText.toDouble(),
                                            shortDescription = shortDesc.takeIf { it.isNotBlank() },
                                            description = desc.takeIf { it.isNotBlank() },
                                            location = location.takeIf { it.isNotBlank() },
                                            thumbnailUrl = thumbnailUrl.takeIf { it.isNotBlank() },
                                            durationMinutes = durationText.toIntOrNull(),
                                            ageRequired = ageRequiredText.toIntOrNull(),
                                            heightRequired = heightRequiredText.toIntOrNull(),
                                            maxCapacity = maxCapacityText.toIntOrNull(),
                                            riskLevel = riskLevelText.toIntOrNull(),
                                            status = status,
                                            isFeatured = isFeatured
                                        )
                                    )
                                } else {
                                    // Create
                                    onCreateConfirm?.invoke(
                                        CreateGameRequest(
                                            name = name,
                                            category = category,
                                            pricePerTurn = priceText.toDouble(),
                                            shortDescription = shortDesc.takeIf { it.isNotBlank() },
                                            description = desc.takeIf { it.isNotBlank() },
                                            location = location.takeIf { it.isNotBlank() },
                                            thumbnailUrl = thumbnailUrl.takeIf { it.isNotBlank() },
                                            durationMinutes = durationText.toIntOrNull(),
                                            ageRequired = ageRequiredText.toIntOrNull(),
                                            heightRequired = heightRequiredText.toIntOrNull(),
                                            maxCapacity = maxCapacityText.toIntOrNull(),
                                            riskLevel = riskLevelText.toIntOrNull(),
                                            isFeatured = isFeatured
                                        )
                                    )
                                }
                            }
                        },
                        enabled = name.isNotBlank() && category.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Lưu") }
                }
            }
        }
    }
}
