package com.expiryreminder.app.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.expiryreminder.app.data.Category
import com.expiryreminder.app.data.Item
import com.expiryreminder.app.ui.theme.AppColors
import com.expiryreminder.app.ui.theme.ExpiryReminderTheme
import com.expiryreminder.app.util.ImageCompressor
import com.expiryreminder.app.viewmodel.CategoryViewModel
import com.expiryreminder.app.viewmodel.ItemViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    itemViewModel: ItemViewModel,
    categoryViewModel: CategoryViewModel
) {
    var name by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }
    var expireDate by remember { mutableStateOf<Long?>(null) }
    var expireDateText by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf<Long?>(null) }
    var purchaseDateText by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var remindDays by remember { mutableStateOf("3") }
    var imagePath by remember { mutableStateOf<String?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showExpireDatePicker by remember { mutableStateOf(false) }
    var showPurchaseDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val outputDir = File(context.filesDir, "images")
    if (!outputDir.exists()) outputDir.mkdirs()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val tempFile = File(context.cacheDir, "temp_camera_${System.currentTimeMillis()}.jpg")
            try {
                FileOutputStream(tempFile).use { out ->
                    it.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                val tempUri = Uri.fromFile(tempFile)
                scope.launch {
                    val compressedPath = ImageCompressor.compressImage(context, tempUri, outputDir)
                    if (compressedPath != null) {
                        imagePath = compressedPath
                    }
                    tempFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                tempFile.delete()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val compressedPath = ImageCompressor.compressImage(context, it, outputDir)
                if (compressedPath != null) {
                    imagePath = compressedPath
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        }
    }

    ExpiryReminderTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "添加物品",
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
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                ImageUploadSection(
                    imagePath = imagePath,
                    onCameraClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            cameraLauncher.launch(null)
                        }
                    },
                    onGalleryClick = {
                        galleryLauncher.launch("image/*")
                    },
                    onClearImage = { imagePath = null }
                )

                Spacer(modifier = Modifier.height(24.dp))

                FormField(
                    label = "物品名称",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "请输入物品名称"
                )

                Spacer(modifier = Modifier.height(16.dp))

                FormSelectField(
                    label = "分类",
                    value = selectedCategoryName,
                    placeholder = "选择分类",
                    onClick = { showCategoryPicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FormSelectField(
                    label = "到期日期",
                    value = expireDateText,
                    placeholder = "选择日期",
                    onClick = { showExpireDatePicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FormSelectField(
                    label = "购买日期（选填）",
                    value = purchaseDateText,
                    placeholder = "选择日期",
                    onClick = { showPurchaseDatePicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormField(
                            label = "数量",
                            value = quantity,
                            onValueChange = { quantity = it },
                            placeholder = "1"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormField(
                            label = "单位",
                            value = unit,
                            onValueChange = { unit = it },
                            placeholder = "个/瓶/盒"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FormField(
                    label = "存放位置",
                    value = location,
                    onValueChange = { location = it },
                    placeholder = "如：冰箱、厨房、衣柜"
                )

                Spacer(modifier = Modifier.height(16.dp))

                FormField(
                    label = "备注（选填）",
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "请输入备注信息",
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                FormField(
                    label = "提醒天数",
                    value = remindDays,
                    onValueChange = { remindDays = it },
                    placeholder = "提前几天提醒"
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && expireDate != null) {
                            val now = System.currentTimeMillis()
                            val item = Item(
                                name = name,
                                categoryId = selectedCategoryId,
                                imageUri = imagePath,
                                expireDate = expireDate!!,
                                purchaseDate = purchaseDate,
                                quantity = quantity.toIntOrNull() ?: 1,
                                unit = unit.ifBlank { "个" },
                                location = location.ifBlank { "" },
                                note = note.ifBlank { "" },
                                remindDays = remindDays.ifBlank { "3" },
                                createdAt = now,
                                updatedAt = now
                            )
                            itemViewModel.addItem(item)
                            onSaveSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.extended.successGreen
                    ),
                    enabled = name.isNotBlank() && expireDate != null
                ) {
                    Text("保存", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            onDismiss = { showCategoryPicker = false },
            onCategorySelected = { id, catName ->
                selectedCategoryId = id
                selectedCategoryName = catName
                showCategoryPicker = false
            },
            categoryViewModel = categoryViewModel
        )
    }

    if (showExpireDatePicker) {
        DatePickerDialog(
            onDismiss = { showExpireDatePicker = false },
            onDateSelected = { timestamp, text ->
                expireDate = timestamp
                expireDateText = text
                showExpireDatePicker = false
            }
        )
    }

    if (showPurchaseDatePicker) {
        DatePickerDialog(
            onDismiss = { showPurchaseDatePicker = false },
            onDateSelected = { timestamp, text ->
                purchaseDate = timestamp
                purchaseDateText = text
                showPurchaseDatePicker = false
            }
        )
    }
}

@Composable
fun ImageUploadSection(
    imagePath: String?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onClearImage: () -> Unit
) {
    var showImageOptions by remember { mutableStateOf(false) }

    if (imagePath != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "已上传图片",
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onClearImage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
            ) {
                Icon(Icons.Default.Close, contentDescription = "删除", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.extended.inputBackground)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showImageOptions = true }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = AppColors.extended.placeholderText,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "点击上传图片",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.extended.placeholderText
                )
            }
        }
    }

    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            containerColor = AppColors.extended.cardBackground,
            title = { Text("选择图片来源", fontWeight = FontWeight.Bold, color = AppColors.extended.textPrimary) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    showImageOptions = false
                                    onCameraClick()
                                }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AppColors.extended.successGreen)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("拍照", color = AppColors.extended.textPrimary)
                    }
                    HorizontalDivider(color = AppColors.extended.divider)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    showImageOptions = false
                                    onGalleryClick()
                                }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = AppColors.extended.successGreen)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("从相册选择", color = AppColors.extended.textPrimary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageOptions = false }) {
                    Text("取消")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLines: Int = 1
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppColors.extended.textPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = AppColors.extended.placeholderText) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.extended.successGreen,
                unfocusedBorderColor = AppColors.extended.inputBorder,
                focusedContainerColor = AppColors.extended.inputBackground,
                unfocusedContainerColor = AppColors.extended.inputBackground,
                cursorColor = AppColors.extended.successGreen
            )
        )
    }
}

@Composable
fun FormSelectField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppColors.extended.textPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(12.dp),
            color = AppColors.extended.inputBackground,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (value.isBlank()) placeholder else value,
                    color = if (value.isBlank()) AppColors.extended.placeholderText else AppColors.extended.textPrimary
                )
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppColors.extended.chevronTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryPickerDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (Long, String) -> Unit,
    categoryViewModel: CategoryViewModel
) {
    var parentCategories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var childCategories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedParentId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        categoryViewModel.parentCategories.collect { categories ->
            parentCategories = categories
        }
    }

    LaunchedEffect(selectedParentId) {
        if (selectedParentId != null) {
            categoryViewModel.getChildCategories(selectedParentId!!).collect { categories ->
                childCategories = categories
            }
        } else {
            childCategories = emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.extended.cardBackground,
        title = { Text("选择分类", fontWeight = FontWeight.Bold, color = AppColors.extended.textPrimary) },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                if (selectedParentId == null) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(parentCategories) { category ->
                            CategoryIconItem(
                                category = category,
                                onClick = { selectedParentId = category.id }
                            )
                        }
                    }
                } else {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { selectedParentId = null }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回", modifier = Modifier.size(20.dp), tint = AppColors.extended.textSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("返回上级", color = AppColors.extended.successGreen)
                        }
                        HorizontalDivider(color = AppColors.extended.divider)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(childCategories) { category ->
                                ChildCategoryItem(
                                    category = category,
                                    onClick = { onCategorySelected(category.id, category.name) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CategoryIconItem(
    category: Category,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.extended.successGreenBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                getCategoryIcon(category.icon),
                contentDescription = null,
                tint = AppColors.extended.successGreen,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            category.name,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.extended.textPrimary
        )
    }
}

@Composable
fun ChildCategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        color = AppColors.extended.inputBackground,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                getCategoryIcon(category.icon),
                contentDescription = null,
                tint = AppColors.extended.successGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                category.name,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.extended.textPrimary
            )
        }
    }
}

@Composable
fun getCategoryIcon(iconName: String) = when (iconName) {
    "restaurant" -> Icons.Default.Restaurant
    "local_laundry_service" -> Icons.Default.LocalLaundryService
    "favorite" -> Icons.Default.Favorite
    "badge" -> Icons.Default.Badge
    "account_balance" -> Icons.Default.AccountBalance
    "stars" -> Icons.Default.Stars
    "devices" -> Icons.Default.Devices
    "home" -> Icons.Default.Home
    "pets" -> Icons.Default.Pets
    "flight" -> Icons.Default.Flight
    "work" -> Icons.Default.Work
    else -> Icons.Default.Category
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Long, String) -> Unit
) {
    val state = rememberDatePickerState()
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    val text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                    onDateSelected(millis, text)
                }
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        androidx.compose.material3.DatePicker(state = state)
    }
}
