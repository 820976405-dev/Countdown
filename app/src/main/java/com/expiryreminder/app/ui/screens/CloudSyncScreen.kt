package com.expiryreminder.app.ui.screens

import android.content.Context
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expiryreminder.app.data.Category
import com.expiryreminder.app.data.Item
import com.expiryreminder.app.ui.theme.AppColors
import com.expiryreminder.app.widget.WidgetUpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

// 坚果云 WebDAV 配置
private const val JIANGUOYUN_WEBDAV_URL = "https://dav.jianguoyun.com/dav/"

data class WebDavConfig(
    val serverUrl: String = JIANGUOYUN_WEBDAV_URL,
    val username: String = "",
    val password: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(
    onNavigateBack: () -> Unit,
    itemViewModel: com.expiryreminder.app.viewmodel.ItemViewModel,
    categoryViewModel: com.expiryreminder.app.viewmodel.CategoryViewModel
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("cloud_sync", Context.MODE_PRIVATE)

    var serverUrl by remember { mutableStateOf(prefs.getString("server_url", JIANGUOYUN_WEBDAV_URL) ?: JIANGUOYUN_WEBDAV_URL) }
    var username by remember { mutableStateOf(prefs.getString("username", "") ?: "") }
    var password by remember { mutableStateOf(prefs.getString("password", "") ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf("") }
    var syncMessageIsError by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var lastSyncTime by remember { mutableStateOf(prefs.getString("last_sync_time", "从未同步") ?: "从未同步") }
    var isVerified by remember { mutableStateOf(prefs.getBoolean("is_verified", false)) }

    val isConnected = isVerified

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据备份与同步", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 坚果云账号绑定卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            tint = AppColors.extended.infoBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("坚果云账号绑定", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.extended.textPrimary)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "使用坚果云 WebDAV 服务同步数据，请先在坚果云官网创建应用密码",
                        fontSize = 12.sp,
                        color = AppColors.extended.textTertiary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 服务器地址
                    Text("服务器地址", fontSize = 13.sp, color = AppColors.extended.textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 账号
                    Text("账号（邮箱/手机号）", fontSize = 13.sp, color = AppColors.extended.textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        placeholder = { Text("请输入坚果云账号", fontSize = 14.sp, color = AppColors.extended.textTertiary) },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 应用密码
                    Text("应用密码", fontSize = 13.sp, color = AppColors.extended.textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = AppColors.extended.textTertiary
                                )
                            }
                        },
                        placeholder = { Text("请输入应用密码", fontSize = 14.sp, color = AppColors.extended.textTertiary) },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "提示：请在坚果云官网 → 安全选项 → 第三方应用管理中创建应用密码",
                        fontSize = 11.sp,
                        color = AppColors.extended.textTertiary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 保存/连接按钮
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                syncMessage = "请输入账号和应用密码"
                                syncMessageIsError = true
                                return@Button
                            }
                            isVerifying = true
                            syncMessage = ""
                            syncMessageIsError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.extended.successGreen),
                        enabled = !isVerifying
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("验证中...", fontSize = 15.sp)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("保存账号", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // 同步状态卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            tint = AppColors.extended.successGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("同步状态", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.extended.textPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("上次同步时间", fontSize = 13.sp, color = AppColors.extended.textSecondary)
                        Text(lastSyncTime, fontSize = 13.sp, color = AppColors.extended.textPrimary)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("账号状态", fontSize = 13.sp, color = AppColors.extended.textSecondary)
                        Text(
                            if (isConnected) "已绑定" else "未绑定",
                            fontSize = 13.sp,
                            color = if (isConnected) AppColors.extended.successGreen else AppColors.extended.dangerRed
                        )
                    }

                    if (syncMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(syncMessage, fontSize = 12.sp, color = if (syncMessageIsError) AppColors.extended.dangerRed else AppColors.extended.successGreen)
                    }
                }
            }

            // 数据备份卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = AppColors.extended.infoBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("数据备份", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.extended.textPrimary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("将本地物品和分类数据上传到坚果云进行备份", fontSize = 13.sp, color = AppColors.extended.textSecondary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!isConnected) {
                                syncMessage = "请先绑定坚果云账号"
                                return@Button
                            }
                            isSyncing = true
                            syncMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.extended.infoBlue),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("备份中...", fontSize = 15.sp)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("立即备份", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // 数据恢复卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = AppColors.extended.warningOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("数据恢复", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.extended.textPrimary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("从坚果云下载备份数据恢复到本地，恢复后本地数据将被替换", fontSize = 13.sp, color = AppColors.extended.textSecondary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!isConnected) {
                                syncMessage = "请先绑定坚果云账号"
                                return@Button
                            }
                            showPasswordDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.extended.warningOrange),
                        enabled = !isRestoring
                    ) {
                        if (isRestoring) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("恢复中...", fontSize = 15.sp)
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("从坚果云恢复", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // 使用说明
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = AppColors.extended.textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("使用说明", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.extended.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val steps = listOf(
                        "1. 登录坚果云官网 (jianguoyun.com)",
                        "2. 进入「安全选项」→「第三方应用管理」",
                        "3. 点击「添加应用密码」，生成应用密码",
                        "4. 将账号和应用密码填入上方表单并保存",
                        "5. 点击「立即备份」上传数据到坚果云",
                        "6. 在新设备上登录同一账号，点击「从坚果云恢复」"
                    )
                    steps.forEach { step ->
                        Text(step, fontSize = 12.sp, color = AppColors.extended.textTertiary, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // 恢复确认对话框
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            containerColor = AppColors.extended.cardBackground,
            title = { Text("确认恢复数据", fontWeight = FontWeight.Bold) },
            text = { Text("恢复数据将替换本地所有物品和分类数据，此操作不可撤销。确定要继续吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        isRestoring = true
                        syncMessage = ""
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.extended.dangerRed)
                ) { Text("确定恢复") }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text("取消") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 验证账号
    LaunchedEffect(isVerifying) {
        if (isVerifying) {
            val config = WebDavConfig(serverUrl, username, password)
            val result = verifyWebDavCredentials(config)
            if (result.success) {
                prefs.edit()
                    .putString("server_url", serverUrl)
                    .putString("username", username)
                    .putString("password", password)
                    .putBoolean("is_verified", true)
                    .apply()
                isVerified = true
                syncMessage = "账号验证成功，已保存"
                syncMessageIsError = false
            } else {
                prefs.edit().putBoolean("is_verified", false).apply()
                isVerified = false
                syncMessage = result.message
                syncMessageIsError = true
            }
            isVerifying = false
        }
    }

    // 执行备份
    LaunchedEffect(isSyncing) {
        if (isSyncing) {
            try {
                val config = WebDavConfig(serverUrl, username, password)
                val items = itemViewModel.allItemsForSync()
                val categories = categoryViewModel.allCategoriesForSync()
                val result = uploadToWebDav(config, items, categories)
                if (result) {
                    val timeStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    prefs.edit().putString("last_sync_time", timeStr).apply()
                    lastSyncTime = timeStr
                    syncMessage = "备份成功！共 ${items.size} 条物品，${categories.size} 个分类"
                    syncMessageIsError = false
                } else {
                    syncMessage = "备份失败，请检查账号和网络"
                    syncMessageIsError = true
                }
            } catch (e: Exception) {
                syncMessage = "备份失败：${e.message}"
                syncMessageIsError = true
            }
            isSyncing = false
        }
    }

    // 执行恢复
    LaunchedEffect(isRestoring) {
        if (isRestoring) {
            try {
                val config = WebDavConfig(serverUrl, username, password)
                val backupData = downloadFromWebDav(config, context)
                if (backupData != null) {
                    restoreFromBackup(backupData, itemViewModel, categoryViewModel)
                    // 恢复完成后强制刷新所有小组件
                    WidgetUpdateHelper.updateAllWidgets(context)
                    syncMessage = "恢复成功！共 ${backupData.items.size} 条物品，${backupData.categories.size} 个分类"
                    syncMessageIsError = false
                } else {
                    syncMessage = "恢复失败，未找到备份数据"
                    syncMessageIsError = true
                }
            } catch (e: Exception) {
                syncMessage = "恢复失败：${e.message}"
                syncMessageIsError = true
            }
            isRestoring = false
        }
    }
}

private data class BackupData(
    val items: List<Item>,
    val categories: List<Category>,
    val timestamp: Long
)

private data class VerifyResult(
    val success: Boolean,
    val message: String
)

private fun buildAuthHeader(username: String, password: String): String {
    return "Basic " + Base64.encodeToString(
        "$username:$password".toByteArray(), Base64.NO_WRAP
    )
}

/** OkHttp 客户端，支持所有 HTTP 方法包括 WebDAV 扩展方法（MKCOL, PROPFIND 等） */
private fun createOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()
}

/** 构建正确的 WebDAV 路径 URL，确保路径间有 / 分隔 */
private fun buildWebDavUrl(baseUrl: String, vararg segments: String): String {
    val base = baseUrl.trimEnd('/')
    return segments.fold(base) { acc, seg -> "$acc/${seg.trimStart('/')}" }
}

private suspend fun verifyWebDavCredentials(config: WebDavConfig): VerifyResult =
    withContext(Dispatchers.IO) {
        try {
            // 使用 PROPFIND 请求 /dav/ 根目录验证（OkHttp 支持非标准 HTTP 方法）
            val testUrl = config.serverUrl.trimEnd('/') + "/"
            val client = createOkHttpClient()

            val propfindBody = """<?xml version="1.0" encoding="utf-8"?><propfind xmlns="DAV:"><prop></prop></propfind>"""
                .toRequestBody("application/xml; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(testUrl)
                .method("PROPFIND", propfindBody)
                .header("Authorization", buildAuthHeader(config.username, config.password))
                .header("User-Agent", "ExpiryReminder/1.0")
                .header("Depth", "0")
                .build()

            val response = client.newCall(request).execute()
            val code = response.code

            response.close()

            when (code) {
                207, in 200..299 -> VerifyResult(true, "验证成功")
                301, 302 -> {
                    // 重定向说明认证已通过
                    VerifyResult(true, "验证成功")
                }
                401 -> VerifyResult(false, "认证失败(401): 账号或应用密码错误，请确认使用的是应用密码(非登录密码)")
                403 -> VerifyResult(false, "访问被拒绝(403): 请确认使用的是应用密码(非登录密码)，且已在坚果云官网→安全选项→第三方应用管理中创建应用密码")
                404 -> VerifyResult(false, "路径不存在(404): 请检查服务器地址是否正确，应为 https://dav.jianguoyun.com/dav/")
                else -> VerifyResult(false, "验证失败($code)")
            }
        } catch (e: java.net.UnknownHostException) {
            VerifyResult(false, "DNS解析失败: 无法连接到服务器，请检查网络")
        } catch (e: java.net.SocketTimeoutException) {
            VerifyResult(false, "连接超时: 请检查网络是否正常")
        } catch (e: javax.net.ssl.SSLHandshakeException) {
            VerifyResult(false, "SSL握手失败: ${e.message}")
        } catch (e: java.net.ConnectException) {
            VerifyResult(false, "连接被拒绝: 服务器地址可能不正确")
        } catch (e: Exception) {
            VerifyResult(false, "连接异常: ${e.javaClass.simpleName} - ${e.message}")
        }
    }

private suspend fun uploadToWebDav(
    config: WebDavConfig,
    items: List<Item>,
    categories: List<Category>
): Boolean =
    withContext(Dispatchers.IO) {
        try {
            val client = createOkHttpClient()
            val auth = buildAuthHeader(config.username, config.password)

            // 先确保目录存在（坚果云不会自动创建目录，直接 PUT 到不存在的目录会返回 403）
            val folderUrl = buildWebDavUrl(config.serverUrl, "ExpiryReminder/")
            val mkcolRequest = Request.Builder()
                .url(folderUrl)
                .method("MKCOL", null)
                .header("Authorization", auth)
                .header("User-Agent", "ExpiryReminder/1.0")
                .build()
            val mkcolResponse = client.newCall(mkcolRequest).execute()
            val mkcolCode = mkcolResponse.code
            mkcolResponse.close()
            // 201 = 创建成功, 405 = 目录已存在
            if (mkcolCode !in 200..299 && mkcolCode != 405) {
                return@withContext false
            }

            // 确保图片目录存在
            val imagesFolderUrl = buildWebDavUrl(config.serverUrl, "ExpiryReminder/images/")
            val imagesMkcolRequest = Request.Builder()
                .url(imagesFolderUrl)
                .method("MKCOL", null)
                .header("Authorization", auth)
                .header("User-Agent", "ExpiryReminder/1.0")
                .build()
            val imagesMkcolResponse = client.newCall(imagesMkcolRequest).execute()
            imagesMkcolResponse.close()

            // 上传物品图片到坚果云，用 itemId 作为文件名确保对应关系
            val imageMap = mutableMapOf<Long, String>() // itemId -> 远程图片路径
            items.forEach { item ->
                if (!item.imageUri.isNullOrBlank()) {
                    val imageFile = java.io.File(item.imageUri)
                    if (imageFile.exists()) {
                        try {
                            val remoteImagePath = "ExpiryReminder/images/${item.id}.webp"
                            val imageUrl = buildWebDavUrl(config.serverUrl, remoteImagePath)
                            val imageBytes = imageFile.readBytes()
                            val imageRequestBody = imageBytes
                                .toRequestBody("image/webp".toMediaType())

                            val imagePutRequest = Request.Builder()
                                .url(imageUrl)
                                .put(imageRequestBody)
                                .header("Authorization", auth)
                                .header("User-Agent", "ExpiryReminder/1.0")
                                .build()

                            val imageResponse = client.newCall(imagePutRequest).execute()
                            imageResponse.close()
                            if (imageResponse.code in 200..299) {
                                imageMap[item.id] = remoteImagePath
                            }
                        } catch (e: Exception) {
                            // 单个图片上传失败不影响整体备份
                        }
                    }
                }
            }

            // 序列化数据（imageUri 保存为远程路径，便于恢复时下载）
            val json = JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
                put("version", 2)
                put("items", JSONArray().apply {
                    items.forEach { item ->
                        put(JSONObject().apply {
                            put("id", item.id)
                            put("name", item.name)
                            put("categoryId", item.categoryId ?: JSONObject.NULL)
                            // 如果图片已上传到坚果云，保存远程路径；否则保存原始本地路径
                            put("imageUri", imageMap[item.id] ?: (item.imageUri ?: JSONObject.NULL))
                            put("hasImage", imageMap.containsKey(item.id))
                            put("expireDate", item.expireDate)
                            put("purchaseDate", item.purchaseDate ?: JSONObject.NULL)
                            put("quantity", item.quantity)
                            put("unit", item.unit)
                            put("location", item.location)
                            put("note", item.note)
                            put("remindDays", item.remindDays)
                            put("createdAt", item.createdAt)
                            put("updatedAt", item.updatedAt)
                        })
                    }
                })
                put("categories", JSONArray().apply {
                    categories.forEach { cat ->
                        put(JSONObject().apply {
                            put("id", cat.id)
                            put("name", cat.name)
                            put("icon", cat.icon)
                            put("parentId", cat.parentId ?: JSONObject.NULL)
                        })
                    }
                })
            }

            // 上传备份数据文件
            val backupUrl = buildWebDavUrl(config.serverUrl, "ExpiryReminder/backup.json")
            val requestBody = json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val putRequest = Request.Builder()
                .url(backupUrl)
                .put(requestBody)
                .header("Authorization", auth)
                .header("User-Agent", "ExpiryReminder/1.0")
                .build()

            val response = client.newCall(putRequest).execute()
            val code = response.code
            response.close()
            code in 200..299
        } catch (e: Exception) {
            false
        }
    }

private suspend fun downloadFromWebDav(config: WebDavConfig, context: Context): BackupData? =
    withContext(Dispatchers.IO) {
        try {
            val client = createOkHttpClient()
            val urlStr = buildWebDavUrl(config.serverUrl, "ExpiryReminder/backup.json")

            val request = Request.Builder()
                .url(urlStr)
                .get()
                .header("Authorization", buildAuthHeader(config.username, config.password))
                .header("User-Agent", "ExpiryReminder/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext null
            }

            val responseBody = response.body?.string() ?: run {
                response.close()
                return@withContext null
            }
            response.close()

            val json = JSONObject(responseBody)
            val items = mutableListOf<Item>()
            val itemsArr = json.getJSONArray("items")
            for (i in 0 until itemsArr.length()) {
                val obj = itemsArr.getJSONObject(i)
                val hasImage = obj.optBoolean("hasImage", false)
                val remoteImageUri = if (obj.isNull("imageUri")) null else obj.optString("imageUri")

                // 如果有图片，从坚果云下载到本地
                var localImageUri: String? = null
                if (hasImage && !remoteImageUri.isNullOrBlank()) {
                    try {
                        val itemId = obj.optLong("id", 0)
                        val remoteImagePath = if (remoteImageUri.startsWith("ExpiryReminder/images/")) {
                            remoteImageUri
                        } else {
                            "ExpiryReminder/images/${itemId}.webp"
                        }
                        val imageUrl = buildWebDavUrl(config.serverUrl, remoteImagePath)

                        val imageRequest = Request.Builder()
                            .url(imageUrl)
                            .get()
                            .header("Authorization", buildAuthHeader(config.username, config.password))
                            .header("User-Agent", "ExpiryReminder/1.0")
                            .build()

                        val imageResponse = client.newCall(imageRequest).execute()
                        if (imageResponse.isSuccessful) {
                            val imageDir = java.io.File(context.filesDir, "images")
                            imageDir.mkdirs()
                            val localImageFile = java.io.File(imageDir, "${itemId}.webp")
                            imageResponse.body?.byteStream()?.use { input ->
                                java.io.FileOutputStream(localImageFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            localImageUri = localImageFile.absolutePath
                        }
                        imageResponse.close()
                    } catch (e: Exception) {
                        // 单个图片下载失败不影响整体恢复
                    }
                }

                items.add(Item(
                    id = obj.optLong("id", 0),
                    name = obj.optString("name", ""),
                    categoryId = if (obj.isNull("categoryId")) null else obj.optLong("categoryId"),
                    imageUri = localImageUri ?: (if (obj.isNull("imageUri")) null else obj.optString("imageUri")),
                    expireDate = obj.optLong("expireDate", 0),
                    purchaseDate = if (obj.isNull("purchaseDate")) null else obj.optLong("purchaseDate"),
                    quantity = obj.optInt("quantity", 1),
                    unit = obj.optString("unit", ""),
                    location = obj.optString("location", ""),
                    note = obj.optString("note", ""),
                    remindDays = obj.optString("remindDays", ""),
                    createdAt = obj.optLong("createdAt", 0),
                    updatedAt = obj.optLong("updatedAt", 0)
                ))
            }

            val categories = mutableListOf<Category>()
            val catsArr = json.getJSONArray("categories")
            for (i in 0 until catsArr.length()) {
                val obj = catsArr.getJSONObject(i)
                categories.add(Category(
                    id = obj.optLong("id", 0),
                    name = obj.optString("name", ""),
                    icon = obj.optString("icon", ""),
                    parentId = if (obj.isNull("parentId")) null else obj.optLong("parentId")
                ))
            }

            BackupData(items = items, categories = categories, timestamp = json.optLong("timestamp", 0))
        } catch (e: Exception) {
            null
        }
    }

private suspend fun restoreFromBackup(
    backupData: BackupData,
    itemViewModel: com.expiryreminder.app.viewmodel.ItemViewModel,
    categoryViewModel: com.expiryreminder.app.viewmodel.CategoryViewModel
) = withContext(Dispatchers.IO) {
    // 清除本地数据
    itemViewModel.deleteAllItems()
    categoryViewModel.deleteAllCategories()

    // 恢复分类（先恢复，因为物品依赖分类ID），保留原始ID确保对应关系
    backupData.categories.forEach { cat ->
        categoryViewModel.insertCategory(cat)
    }

    // 恢复物品，保留原始ID以确保图片文件名与物品ID对应
    // 图片文件以 itemId.webp 命名，如果ID变了图片就对不上了
    backupData.items.forEach { item ->
        itemViewModel.addItem(item)
    }
}
