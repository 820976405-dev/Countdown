package com.expiryreminder.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.expiryreminder.app.data.Item
import com.expiryreminder.app.ui.theme.AppColors
import com.expiryreminder.app.util.formatExpiryDate
import com.expiryreminder.app.viewmodel.CategoryViewModel
import com.expiryreminder.app.viewmodel.ItemViewModel
import kotlinx.coroutines.runBlocking
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDetailScreen(
    templateName: String? = null,
    defaultTag: String? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    itemViewModel: ItemViewModel,
    categoryViewModel: CategoryViewModel
) {
    val context = LocalContext.current
    
    var name by remember { mutableStateOf(templateName ?: "") }
    var expireDate by remember { mutableStateOf(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000) }
    var imagePath by remember { mutableStateOf<String?>(null) }
    
    var remindBefore7Days by remember { mutableStateOf(true) }
    var remindBefore1Day by remember { mutableStateOf(true) }
    var remindOnDay by remember { mutableStateOf(false) }
    
    val initialTags = remember(defaultTag) {
        if (defaultTag != null && defaultTag.isNotBlank()) {
            setOf(defaultTag)
        } else {
            emptySet()
        }
    }
    var selectedTags by remember { mutableStateOf(initialTags) }
    var note by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "添加提醒",
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
                    if (name.isNotBlank()) {
                        val categoryForLookup = defaultTag ?: selectedTags.firstOrNull()
                        val categoryId = runBlocking {
                            categoryForLookup?.let { name ->
                                categoryViewModel.getCategoryByName(name)?.id
                                ?: mainCategoriesList.flatMap { parent ->
                                    listOf(parent.name to parent.id) +
                                        parent.subCategories.map { it.name to it.id }
                                }.find { it.first == name }?.second
                            } ?: 0L
                        }
                        val item = Item(
                            id = 0,
                            name = name,
                            categoryId = categoryId,
                            imageUri = imagePath,
                            expireDate = expireDate,
                            purchaseDate = null,
                            quantity = 1,
                            unit = "个",
                            location = "",
                            note = note,
                            remindDays = buildString {
                                if (remindBefore7Days) append("7")
                                if (remindBefore1Day) append("1")
                                if (remindOnDay) append("0")
                            },
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        itemViewModel.addItem(item)
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
                
                androidx.compose.material3.DatePickerDialog(
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
                var showTagDialog by remember { mutableStateOf(false) }
                
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

            Spacer(modifier = Modifier.height(20.dp))

            FormSection(title = "重复") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.extended.inputBackground)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("不重复", fontSize = 14.sp, color = AppColors.extended.textPrimary)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.extended.chevronTint, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

            // 图片放大预览覆盖层
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

@Composable
fun ImageUploadSectionNew(
    imagePath: String?,
    onImageSelected: (String?) -> Unit,
    onPreviewClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val outputDir = File(context.cacheDir, "images")
            if (!outputDir.exists()) outputDir.mkdirs()
            val path = com.expiryreminder.app.util.ImageCompressor.compressImage(context, it, outputDir)
            if (path != null) onImageSelected(path)
        }
    }

    Box {
        if (imagePath != null && imagePath.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.extended.inputBackground),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = if (imagePath.startsWith("/")) File(imagePath) else imagePath,
                    contentDescription = "已上传图片",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onPreviewClick
                        )
                )
                IconButton(
                    onClick = { onImageSelected(null) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "删除", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.extended.inputBackground)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { launcher.launch("image/*") }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = "添加图片",
                        tint = AppColors.extended.placeholderText,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "添加图片（选填）",
                        fontSize = 13.sp,
                        color = AppColors.extended.placeholderText
                    )
                }
            }
        }
    }
}

@Composable
fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.extended.textPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun ReminderOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    dateText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = AppColors.extended.successGreen,
                uncheckedColor = AppColors.extended.checkboxUnchecked
            ),
            modifier = Modifier.size(22.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(icon, contentDescription = null, tint = AppColors.extended.successGreen, modifier = Modifier.size(18.dp))
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            title,
            fontSize = 14.sp,
            color = AppColors.extended.textPrimary,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            dateText,
            fontSize = 13.sp,
            color = AppColors.extended.textTertiary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(Icons.Default.Notifications, contentDescription = null, tint = AppColors.extended.textTertiary, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun TagSelectionDialog(
    selectedTags: Set<String>,
    onTagsSelected: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val categoryList = listOf(
        "食品厨房",
        "个人护理",
        "健康医疗",
        "证件文件",
        "金融保险",
        "会员订阅",
        "数码设备",
        "家居维护",
        "宠物用品",
        "出行旅游",
        "工作学习",
        "其他"
    )
    
    var tempSelectedTags by remember { mutableStateOf(selectedTags) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.extended.cardBackground,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                "选择标签",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.extended.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categoryList) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        tempSelectedTags = if (tempSelectedTags.contains(category)) {
                                            tempSelectedTags - category
                                        } else {
                                            tempSelectedTags + category
                                        }
                                    }
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = tempSelectedTags.contains(category),
                                onCheckedChange = { 
                                    tempSelectedTags = if (it) {
                                        tempSelectedTags + category
                                    } else {
                                        tempSelectedTags - category
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AppColors.extended.successGreen,
                                    uncheckedColor = AppColors.extended.checkboxUnchecked
                                ),
                                modifier = Modifier.size(22.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Text(
                                category,
                                fontSize = 15.sp,
                                color = AppColors.extended.textPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTagsSelected(tempSelectedTags) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.extended.successGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("确定", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.extended.textSecondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("取消", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    )
}
