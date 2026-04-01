package com.park.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.data.model.AnnouncementDTO
import com.park.data.model.CreateAnnouncementRequest
import com.park.data.model.UpdateAnnouncementRequest
import com.park.ui.component.PageHeader
import com.park.ui.component.SnackbarMessage
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography
import com.park.viewmodel.AnnouncementViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.SwingUtilities
import kotlin.collections.find

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementScreen(viewModel: AnnouncementViewModel = viewModel { AnnouncementViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Form state
    var editingId by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var linkType by remember { mutableStateOf("") }
    var linkValue by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    var sortOrder by remember { mutableStateOf("0") }

    // Preview bitmap loaded from selected local file
    var previewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(selectedFile) {
        previewBitmap = selectedFile?.let { file ->
            withContext(Dispatchers.IO) {
                runCatching { SkiaImage.makeFromEncoded(file.readBytes()).toComposeImageBitmap() }.getOrNull()
            }
        }
    }

    // Dropdown state
    var gameDropdownExpanded by remember { mutableStateOf(false) }
    var gameSearch by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf<AnnouncementDTO?>(null) }

    val selectedGameName = remember(linkValue, uiState.games) {
        uiState.games.find { it.gameId == linkValue }?.name ?: linkValue
    }

    val filteredGames = remember(gameSearch, uiState.games) {
        if (gameSearch.isBlank()) uiState.games
        else uiState.games.filter {
            it.name.contains(gameSearch, ignoreCase = true) || it.category.contains(gameSearch, ignoreCase = true)
        }
    }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    fun resetForm() {
        editingId = null
        title = ""
        description = ""
        imageUrl = ""
        selectedFile = null
        linkType = ""
        linkValue = ""
        isActive = true
        sortOrder = "0"
        gameSearch = ""
    }

    fun normalizeImageUrl(url: String): String {
        // Nếu là full URL cũ, chỉ giữ lại phần path /uploads/...
        val uploadsIndex = url.indexOf("/uploads/")
        return if (uploadsIndex > 0) url.substring(uploadsIndex) else url
    }

    fun loadIntoForm(a: AnnouncementDTO) {
        editingId = a.announcementId
        title = a.title
        description = a.description ?: ""
        imageUrl = normalizeImageUrl(a.imageUrl)
        selectedFile = null
        linkType = a.linkType ?: ""
        linkValue = a.linkValue ?: ""
        isActive = a.isActive
        sortOrder = a.sortOrder.toString()
        gameSearch = ""

    }

    fun openFilePicker() {
        scope.launch {
            val file = withContext(Dispatchers.IO) {
                val deferred = CompletableDeferred<File?>()
                SwingUtilities.invokeLater {
                    val dialog = FileDialog(null as Frame?, "Chọn ảnh banner", FileDialog.LOAD)
                    dialog.setFilenameFilter { _, name ->
                        val lower = name.lowercase()
                        lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                        lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp")
                    }
                    dialog.isVisible = true
                    deferred.complete(
                        if (dialog.file != null) File(dialog.directory, dialog.file) else null
                    )
                }
                deferred.await()
            }
            file?.let {
                selectedFile = it
                imageUrl = "" // reset URL cũ, sẽ được set lại sau khi upload
                viewModel.uploadImage(it) { url -> imageUrl = url }
            }
        }
    }

    // Delete dialog
    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa banner \"${item.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAnnouncement(item.announcementId)
                    showDeleteDialog = null
                }) { Text("Xóa", color = AppColors.RedError) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Hủy") }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceLight)
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── Left Panel: Form ────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            PageHeader(
                title = if (editingId == null) "Thêm Banner" else "Sửa Banner",
                subtitle = "Quản lý carousel thông báo trên app"
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.WarmOrange,
                        focusedLabelColor = AppColors.WarmOrange,
                        cursorColor = AppColors.WarmOrange
                    )

                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("Tiêu đề banner *") }, singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(), colors = fieldColors
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Mô tả ngắn (hiển thị trên banner)") }, maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(), colors = fieldColors
                    )
                    Spacer(Modifier.height(10.dp))

                    // ─── Image picker with preview ───────────────────────────
                    Text("Ảnh banner *", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray)
                    Spacer(Modifier.height(6.dp))

                    // Preview box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.SurfaceLight),
                        contentAlignment = Alignment.Center
                    ) {
                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap!!,
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    tint = if (imageUrl.isNotBlank()) AppColors.WarmOrange
                                           else AppColors.PrimaryGray.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (imageUrl.isNotBlank()) "Ảnh hiện tại (từ server)" else "Chưa có ảnh",
                                    fontSize = 12.sp,
                                    color = if (imageUrl.isNotBlank()) AppColors.WarmOrange
                                            else AppColors.PrimaryGray.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    // Picker row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when {
                                uiState.isUploading -> "Đang upload ảnh..."
                                imageUrl.isNotBlank() && selectedFile != null -> "✓ ${selectedFile!!.name}"
                                imageUrl.isNotBlank() -> imageUrl
                                selectedFile != null -> selectedFile!!.name
                                else -> "Chưa chọn ảnh"
                            },
                            modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            color = when {
                                uiState.isUploading -> AppColors.WarmOrange
                                imageUrl.isNotBlank() -> AppColors.GreenSuccess
                                else -> AppColors.PrimaryGray
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (uiState.isUploading) {
                            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = AppColors.WarmOrange,
                                    strokeWidth = 3.dp
                                )
                            }
                        } else {
                            Button(
                                onClick = { openFilePicker() },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Chọn ảnh", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    // Link type selector
                    Text("Điều hướng khi bấm vào", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("" to "Không", "GAME" to "Game", "SCREEN" to "Màn hình").forEach { (type, label) ->
                            FilterChip(
                                selected = linkType == type,
                                onClick = { linkType = type; linkValue = ""; gameSearch = "" },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppColors.WarmOrange,
                                    selectedLabelColor = AppColors.White
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    when (linkType) {
                        "GAME" -> {
                            ExposedDropdownMenuBox(
                                expanded = gameDropdownExpanded,
                                onExpandedChange = { gameDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = if (gameDropdownExpanded) gameSearch else selectedGameName,
                                    onValueChange = { gameSearch = it; gameDropdownExpanded = true },
                                    label = { Text("Chọn trò chơi") },
                                    singleLine = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gameDropdownExpanded) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                                    colors = fieldColors
                                )
                                ExposedDropdownMenu(
                                    expanded = gameDropdownExpanded,
                                    onDismissRequest = { gameDropdownExpanded = false; gameSearch = "" }
                                ) {
                                    if (filteredGames.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Không có trò chơi nào", color = AppColors.PrimaryGray) },
                                            onClick = {}
                                        )
                                    } else {
                                        filteredGames.forEach { game ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(game.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                                        Text(game.category, fontSize = 11.sp, color = AppColors.PrimaryGray)
                                                    }
                                                },
                                                onClick = {
                                                    linkValue = game.gameId
                                                    gameDropdownExpanded = false
                                                    gameSearch = ""
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "SCREEN" -> {
                            OutlinedTextField(
                                value = linkValue, onValueChange = { linkValue = it },
                                label = { Text("Tên màn hình (games, vouchers, balance...)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(), colors = fieldColors
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = sortOrder, onValueChange = { sortOrder = it.filter { c -> c.isDigit() } },
                            label = { Text("Thứ tự hiển thị") }, singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(140.dp), colors = fieldColors
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Đang hiển thị", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray)
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AppColors.WarmOrange,
                                    checkedTrackColor = AppColors.OrangeLight
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (editingId != null) {
                            OutlinedButton(
                                onClick = { resetForm() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("Hủy") }
                        }

                        Button(
                            onClick = {
                                val order = sortOrder.toIntOrNull() ?: 0
                                if (editingId == null) {
                                    viewModel.createAnnouncement(
                                        CreateAnnouncementRequest(
                                            title = title,
                                            description = description.ifBlank { null },
                                            imageUrl = imageUrl,
                                            linkType = linkType.ifBlank { null },
                                            linkValue = linkValue.ifBlank { null },
                                            isActive = isActive,
                                            sortOrder = order
                                        )
                                    )
                                } else {
                                    viewModel.updateAnnouncement(
                                        editingId!!,
                                        UpdateAnnouncementRequest(
                                            title = title,
                                            description = description.ifBlank { null },
                                            imageUrl = imageUrl,
                                            linkType = linkType.ifBlank { null },
                                            linkValue = linkValue.ifBlank { null },
                                            isActive = isActive,
                                            sortOrder = order
                                        )
                                    )
                                }
                                resetForm()
                            },
                            enabled = title.isNotBlank() && imageUrl.isNotBlank() && !uiState.isSaving && !uiState.isUploading,
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(color = AppColors.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    if (editingId == null) Icons.Default.Add else Icons.Default.Edit,
                                    contentDescription = null, modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (editingId == null) "Thêm banner" else "Lưu thay đổi",
                                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    SnackbarMessage(uiState.successMessage, isError = false)
                    SnackbarMessage(uiState.errorMessage, isError = true)
                }
            }
        }

        // ─── Right Panel: Banner list ────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Danh sách Banner (${uiState.announcements.size})",
                style = AppTypography.headlineLarge,
                color = AppColors.PrimaryDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.WarmOrange)
                    }
                } else if (uiState.announcements.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Chưa có banner nào", color = AppColors.PrimaryGray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.announcements) { item ->
                            AnnouncementListItem(
                                item = item,
                                linkLabel = when (item.linkType) {
                                    "GAME" -> uiState.games.find { it.gameId == item.linkValue }?.name ?: item.linkValue
                                    "VOUCHER" -> item.linkValue
                                    else -> item.linkValue
                                },
                                onEdit = { loadIntoForm(item) },
                                onDelete = { showDeleteDialog = item }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementListItem(
    item: AnnouncementDTO,
    linkLabel: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("#${item.sortOrder}", fontSize = 11.sp, color = AppColors.PrimaryGray, fontWeight = FontWeight.Medium)
                    Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.PrimaryDark)
                    Surface(
                        color = if (item.isActive) AppColors.GreenSuccess else AppColors.PrimaryGray,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            if (item.isActive) "Active" else "Ẩn",
                            color = AppColors.White, fontSize = 10.sp, fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    item.linkType?.let { lt ->
                        Surface(color = AppColors.BluePrimary, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                lt, color = AppColors.White, fontSize = 10.sp, fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (!linkLabel.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text("→ $linkLabel", fontSize = 11.sp, color = AppColors.WarmOrange, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (item.description != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(item.description, fontSize = 12.sp, color = AppColors.PrimaryGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = AppColors.WarmOrange)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = AppColors.RedError)
                }
            }
        }
    }
}
