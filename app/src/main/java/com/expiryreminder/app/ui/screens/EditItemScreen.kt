package com.expiryreminder.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.expiryreminder.app.data.Item
import com.expiryreminder.app.ui.theme.AppColors
import com.expiryreminder.app.util.formatExpiryDate
import com.expiryreminder.app.viewmodel.CategoryViewModel
import com.expiryreminder.app.viewmodel.ItemViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    itemId: Long,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    itemViewModel: ItemViewModel,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current

    var item by remember { mutableStateOf<Item?>(null) }
    var name by remember { mutableStateOf("") }
    var expireDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var imagePath by remember { mutableStateOf<String?>(null) }

    var remindBefore7Days by remember { mutableStateOf(false) }
    var remindBefore1Day by remember { mutableStateOf(false) }
    var remindOnDay by remember { mutableStateOf(false) }

    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var note by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        val existingItem = itemViewModel.getItemById(itemId)
        item = existingItem
        existingItem?.let {
            name = it.name
            expireDate = it.expireDate
            imagePath = it.imageUri
            note = it.note ?: ""
            // 解析 remindDays 设置提醒选项（独立解码每个选项）
            val daysStr = it.remindDays ?: ""
            remindBefore7Days = daysStr.contains("7")
            remindBefore1Day = daysStr.contains("1")
            remindOnDay = daysStr.contains("0")
            // 查找分类名作为标签
            val categoryName = mainCategoriesList.find { c -> c.id == it.categoryId }?.name
                ?: mainCategoriesList.flatMap { p -> p.subCategories }.find { s -> s.id == it.categoryId }?.name
            if (categoryName != null) {
                selectedTags = setOf(categoryName)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "编辑物品",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && item != null) {
                            val categoryForLookup = selectedTags.firstOrNull()
                            val categoryId = categoryForLookup?.let { tagName ->
                                mainCategoriesList.find { it.name == tagName }?.id
                                    ?: mainCategoriesList.flatMap { p -> p.subCategories }.find { it.name == tagName }?.id
                                    ?: item!!.categoryId
                            } ?: item!!.categoryId

                            val updatedItem = item!!.copy(
                                name = name,
                                categoryId = categoryId,
                                imageUri = imagePath,
                                expireDate = expireDate,
                                note = note.ifBlank { "" },
                                remindDays = buildString {
                                    if (remindBefore7Days) append("7")
                                    if (remindBefore1Day) append("1")
                                    if (remindOnDay) append("0")
                                },
                                updatedAt = System.currentTimeMillis()
                            )
                            itemViewModel.updateItem(updatedItem)
                            onSaveSuccess()
                        } else {
                            Toast.makeText(context, "请输入物品名称", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.extended.successGreen)
                ) {
                    Text("保存", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                ImageUploadSectionNew(imagePath = imagePath, onImageSelected = { imagePath = it }, onPreviewClick = { showImagePreview = true })

                Spacer(modifier = Modifier.height(20.dp))

                FormSection(title = "物品名称") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("点击编辑", color = AppColors.extended.placeholderText, fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.extended.successGreen,
                            unfocusedBorderColor = AppColors.extended.inputBorder,
                            cursorColor = AppColors.extended.successGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "开启后到期提醒：3个月",
                        fontSize = 12.sp,
                        color = AppColors.extended.textTertiary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FormSection(title = "到期日期") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, AppColors.extended.inputBorder, RoundedCornerShape(10.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showDatePicker = true }
                            )
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                formatExpiryDate(expireDate),
                                fontSize = 16.sp,
                                color = AppColors.extended.textPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = AppColors.extended.textTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = expireDate
                    )

                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    expireDate = millis
                                    showDatePicker = false
                                }
                            }) {
                                Text("确定", color = AppColors.extended.successGreen, fontWeight = FontWeight.Medium)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("取消", color = AppColors.extended.textSecondary)
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "提醒时间",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.extended.textTertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                ReminderOptionRow(
                    icon = Icons.Default.Notifications,
                    title = "到期前7天",
                    dateText = formatExpiryDate(expireDate - 7L * 24 * 60 * 60 * 1000),
                    isSelected = remindBefore7Days,
                    onClick = { remindBefore7Days = !remindBefore7Days }
                )

                ReminderOptionRow(
                    icon = Icons.Default.Notifications,
                    title = "到期前1天",
                    dateText = formatExpiryDate(expireDate - 24 * 60 * 60 * 1000),
                    isSelected = remindBefore1Day,
                    onClick = { remindBefore1Day = !remindBefore1Day }
                )

                ReminderOptionRow(
                    icon = Icons.Default.Event,
                    title = "到期当天",
                    dateText = formatExpiryDate(expireDate),
                    isSelected = remindOnDay,
                    onClick = { remindOnDay = !remindOnDay }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FormSection(title = "标签") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.extended.inputBackground)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showTagDialog = true }
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Tag, contentDescription = null, tint = AppColors.extended.textTertiary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (selectedTags.isEmpty()) "选择标签" else selectedTags.joinToString(", "),
                                fontSize = 14.sp,
                                color = if (selectedTags.isEmpty()) AppColors.extended.placeholderText else AppColors.extended.textPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.extended.chevronTint, modifier = Modifier.size(20.dp))
                        }
                    }

                    if (showTagDialog) {
                        TagSelectionDialog(
                            selectedTags = selectedTags,
                            onTagsSelected = {
                                selectedTags = it
                                showTagDialog = false
                            },
                            onDismiss = { showTagDialog = false }
                        )
                    }

                    Text(
                        "常用: 护肤、证件、食品等",
                        fontSize = 12.sp,
                        color = AppColors.extended.placeholderText,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FormSection(title = "备注") {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("可记录购买渠道、价格、批号等信息") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.extended.successGreen,
                            unfocusedBorderColor = AppColors.extended.inputBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "${note.length}/200",
                        fontSize = 11.sp,
                        color = AppColors.extended.placeholderText,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

            // 图片放大预览覆盖层（支持双指缩放）- 放在最外层确保全屏覆盖
            val previewPath = imagePath
            if (showImagePreview && previewPath != null) {
                var scale by remember { mutableStateOf(1f) }
                var offsetX by remember { mutableStateOf(0f) }
                var offsetY by remember { mutableStateOf(0f) }

                val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                    scale = (scale * zoomChange).coerceIn(1f, 5f)
                    offsetX += panChange.x
                    offsetY += panChange.y
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { showImagePreview = false })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = if (previewPath.startsWith("/")) File(previewPath) else previewPath,
                        contentDescription = "预览图片",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(0.8f)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                            .transformable(state = transformState)
                    )
                    IconButton(
                        onClick = { showImagePreview = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
    }
}
