package com.expiryreminder.app.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expiryreminder.app.ui.theme.AppColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ==================== 个人信息主页 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditUsername: () -> Unit = {},
    onNavigateToEditAvatar: () -> Unit = {},
    onNavigateToEditSignature: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

    var username by remember { mutableStateOf(prefs.getString("username", "用户名") ?: "用户名") }
    var signature by remember { mutableStateOf(prefs.getString("signature", "让生活更有条理") ?: "让生活更有条理") }
    var avatarUri by remember { mutableStateOf(prefs.getString("avatar_uri", null)) }

    // 重新读取（从子页面返回后刷新）
    LaunchedEffect(Unit) {
        username = prefs.getString("username", "用户名") ?: "用户名"
        signature = prefs.getString("signature", "让生活更有条理") ?: "让生活更有条理"
        avatarUri = prefs.getString("avatar_uri", null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人信息", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // 顶部绿色卡片
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF5FCF80), Color(0xFF43B86A))
                        )
                    )
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // 头像
                    val avatarBitmap = remember(avatarUri) {
                        if (avatarUri != null) {
                            runCatching { BitmapFactory.decodeFile(avatarUri) }.getOrNull()
                        } else null
                    }
                    if (avatarBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = avatarBitmap.asImageBitmap(),
                            contentDescription = "头像",
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                        )
                    } else {
                        DefaultAvatarIcon()
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(username, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(signature, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 设置项列表
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    SettingRow(Icons.Default.Person, "用户名", username, onClick = onNavigateToEditUsername)
                    HorizontalDivider(color = AppColors.extended.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
                    SettingRow(Icons.Default.FaceRetouchingNatural, "头像", "点击更换", onClick = onNavigateToEditAvatar)
                    HorizontalDivider(color = AppColors.extended.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
                    SettingRow(Icons.Default.Edit, "个性签名", signature, onClick = onNavigateToEditSignature)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5FCF80))
            ) {
                Text("保存", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DefaultAvatarIcon() {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.extended.textPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 14.sp, color = AppColors.extended.textPrimary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, color = AppColors.extended.textTertiary)
        Spacer(modifier = Modifier.width(6.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.extended.textTertiary, modifier = Modifier.size(18.dp))
    }
}

// ==================== 修改用户名页面 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUsernameScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
    var username by remember { mutableStateOf(prefs.getString("username", "") ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("修改用户名", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 用户名标签
            Text("用户名", fontSize = 14.sp, color = AppColors.extended.textSecondary)

            Spacer(modifier = Modifier.height(10.dp))

            // 输入框
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = username,
                        onValueChange = { if (it.length <= 16) username = it },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = AppColors.extended.textPrimary),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.padding(vertical = 10.dp)) {
                                if (username.isEmpty()) {
                                    Text("请输入用户名", fontSize = 15.sp, color = AppColors.extended.textTertiary)
                                }
                                innerTextField()
                            }
                        }
                    )
                    if (username.isNotEmpty()) {
                        IconButton(
                            onClick = { username = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "清除", tint = AppColors.extended.textTertiary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("4~16个字符，支持中文、英文、数字和下划线", fontSize = 11.sp, color = AppColors.extended.textTertiary)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    prefs.edit().putString("username", username.ifBlank { "用户名" }).apply()
                    onNavigateBack()
                },
                enabled = username.isNotBlank() && username.length >= 2 && username.length <= 16,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5FCF80),
                    disabledContainerColor = Color(0xFF5FCF80).copy(alpha = 0.4f)
                )
            ) {
                Text("保存", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==================== 个性签名页面 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSignatureScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
    var signature by remember { mutableStateOf(prefs.getString("signature", "") ?: "") }

    val suggestions = listOf(
        "生活因简单而美好",
        "记录生活，发现美好",
        "保持热爱，奔赴山海",
        "所见皆美好"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个性签名", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 输入框区域
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    BasicTextField(
                        value = signature,
                        onValueChange = { if (it.length <= 30) signature = it },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = AppColors.extended.textPrimary),
                        minLines = 3,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (signature.isEmpty()) {
                                    Text("输入你的座右铭或语录", fontSize = 15.sp, color = AppColors.extended.textTertiary)
                                }
                                innerTextField()
                            }
                        }
                    )
                    // 字数统计
                    Text(
                        "${signature.length}/30",
                        fontSize = 11.sp,
                        color = AppColors.extended.textTertiary,
                        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 推荐签名
            Text("选择推荐签名", fontSize = 13.sp, color = AppColors.extended.textSecondary, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(suggestions) { item ->
                    val isSelected = signature == item
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { signature = item },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF5FCF80).copy(alpha = 0.08f) else AppColors.extended.cardBackground
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5FCF80)) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            item,
                            fontSize = 14.sp,
                            color = if (isSelected) Color(0xFF43B86A) else AppColors.extended.textPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    prefs.edit().putString("signature", signature.ifBlank { "让生活更有条理" }).apply()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5FCF80))
            ) {
                Text("保存", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==================== 修改头像页面 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAvatarScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
    var avatarUri by remember { mutableStateOf(prefs.getString("avatar_uri", null)) }

    // 相机权限请求
    var hasCameraPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    // 拍照：使用 TakePicture + FileProvider（比 TakePicturePreview 更稳定）
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingPhotoFile?.let { photoFile ->
                try {
                    // 复制到头像目录
                    val avatarDir = File(context.filesDir, "avatar")
                    if (!avatarDir.exists()) avatarDir.mkdirs()
                    val avatarFile = File(avatarDir, "user_avatar.jpg")
                    photoFile.copyTo(avatarFile, overwrite = true)

                    // 使用文件绝对路径而非 Uri 字符串，确保 BitmapFactory 能正确解码
                    val savedPath = avatarFile.absolutePath
                    prefs.edit().putString("avatar_uri", savedPath).apply()
                    avatarUri = savedPath
                } catch (_: Exception) {}
                // 清理临时文件
                photoFile.delete()
            }
        } else {
            pendingPhotoFile?.delete()
        }
        pendingPhotoFile = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            // 权限获取后启动拍照
            val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            val photoUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            pendingPhotoFile = photoFile
            cameraLauncher.launch(photoUri)
        }
    }

    // 相册选择器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val avatarDir = File(context.filesDir, "avatar")
                if (!avatarDir.exists()) avatarDir.mkdirs()
                val avatarFile = File(avatarDir, "user_avatar.jpg")
                inputStream?.copyTo(avatarFile.outputStream())
                inputStream?.close()

                val savedPath = avatarFile.absolutePath
                prefs.edit().putString("avatar_uri", savedPath).apply()
                avatarUri = savedPath
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("修改头像", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 大头像预览
            Box(contentAlignment = Alignment.BottomEnd) {
                val avatarBitmap = remember(avatarUri) {
                    if (avatarUri != null) {
                        runCatching { BitmapFactory.decodeFile(avatarUri) }.getOrNull()
                    } else null
                }
                if (avatarBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = avatarBitmap.asImageBitmap(),
                        contentDescription = "头像",
                        modifier = Modifier.size(120.dp).clip(CircleShape)
                    )
                } else {
                    LargeDefaultAvatar()
                }
                // 相机图标
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.7f))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("点击更换头像", fontSize = 13.sp, color = AppColors.extended.textTertiary)

            Spacer(modifier = Modifier.height(36.dp))

            // 选择方式卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    AvatarOptionRow(Icons.Default.PhotoLibrary, "从相册选择", onClick = { galleryLauncher.launch("image/*") })
                    HorizontalDivider(color = AppColors.extended.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    AvatarOptionRow(Icons.Default.CameraAlt, "拍照", onClick = {
                        if (hasCameraPermission) {
                            val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                            val photoUri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            pendingPhotoFile = photoFile
                            cameraLauncher.launch(photoUri)
                        } else {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("支持 JPG、PNG 格式，大小不超过 5MB", fontSize = 11.sp, color = AppColors.extended.textTertiary)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5FCF80))
            ) {
                Text("保存", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun LargeDefaultAvatar() {
    Box(
        modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFF5FCF80).copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = "", tint = Color(0xFF5FCF80), modifier = Modifier.size(60.dp))
    }
}

@Composable
private fun AvatarOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.extended.infoBlue, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(title, fontSize = 14.sp, color = AppColors.extended.textPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.extended.textTertiary, modifier = Modifier.size(18.dp))
    }
}
