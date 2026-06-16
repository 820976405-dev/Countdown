package com.expiryreminder.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expiryreminder.app.ui.theme.AppColors
import com.expiryreminder.app.viewmodel.ItemViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToCategoryManage: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToCloudSync: () -> Unit = {},
    onNavigateToUserProfile: () -> Unit = {},
    onClearData: () -> Unit,
    onClearAllStorage: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAdd: () -> Unit
) {
    var totalItems by remember { mutableStateOf(0) }
    var expiringItems by remember { mutableStateOf(0) }
    var expiredItems by remember { mutableStateOf(0) }
    var notExpiredItems by remember { mutableStateOf(0) }
    var safeItems by remember { mutableStateOf(0) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val itemViewModel: ItemViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = LocalContext.current

    // 读取上次备份成功的时间
    val savedSyncTime = remember {
        context.getSharedPreferences("cloud_sync", Context.MODE_PRIVATE)
            .getString("last_sync_time", null)
    }

    LaunchedEffect(Unit) { itemViewModel.itemCount.collectLatest { count -> totalItems = count } }
    LaunchedEffect(Unit) { itemViewModel.expiringCount.collectLatest { count -> expiringItems = count } }
    LaunchedEffect(Unit) { itemViewModel.expiredCount.collectLatest { count -> expiredItems = count } }
    LaunchedEffect(Unit) { itemViewModel.notExpiredCount.collectLatest { count -> notExpiredItems = count } }
    LaunchedEffect(Unit) { itemViewModel.safeItemCount.collectLatest { count -> safeItems = count } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item { UserProfileCard(onClick = onNavigateToUserProfile) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            // 仪表盘 + 统计 合并在一个Card中
            item {
                GaugeAndStatsCard(
                    expiringCount = expiringItems,
                    expiredCount = expiredItems,
                    notExpiredCount = notExpiredItems,
                    totalCount = totalItems
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                ItemStatusDetailCard(
                    expiringCount = expiringItems,
                    expiredCount = expiredItems,
                    notExpiredCount = notExpiredItems,
                    totalCount = totalItems
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                ProfileMenuCard(
                    onBackupClick = onNavigateToCloudSync,
                    onDeleteExpiredClick = { showClearDialog = true },
                    savedSyncTime = savedSyncTime
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = AppColors.extended.cardBackground,
            title = { Text("删除过期物品", fontWeight = FontWeight.Bold) },
            text = { Text("确定要一键删除所有已过期的物品吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = { onClearData(); showClearDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.extended.dangerRed)
                ) { Text("确定删除") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("取消") } },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            containerColor = AppColors.extended.cardBackground,
            title = { Text("一键删除本地存储", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除所有本地存储数据吗？包括所有物品、分类和提醒记录，此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = { onClearAllStorage(); showClearAllDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.extended.dangerRed)
                ) { Text("确定删除") }
            },
            dismissButton = { TextButton(onClick = { showClearAllDialog = false }) { Text("取消") } },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/** 用户卡片 */
@Composable
private fun UserProfileCard(onClick: () -> Unit) {
    val context = LocalContext.current
    val username = remember {
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
            .getString("username", "用户名") ?: "用户名"
    }
    val signature = remember {
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
            .getString("signature", "让生活更有条理") ?: "让生活更有条理"
    }
    val avatarUri = remember {
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
            .getString("avatar_uri", null)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(Color(0xFF5FCF80), Color(0xFF43B86A))
                )
            )
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
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

/** 仪表盘 + 三列统计 合并在一个Card中 */
@Composable
private fun GaugeAndStatsCard(
    expiringCount: Int,
    expiredCount: Int,
    notExpiredCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 16.dp)
        ) {
            // 半圆仪表盘 - 进度 = 临期物品 / 总物品
            val totalItemsForGauge = totalCount.coerceAtLeast(1)
            val progress = (expiringCount.toFloat() / totalItemsForGauge).coerceIn(0f, 1f)
            val targetSweep = 180f * progress
            val animatedSweep by animateFloatAsState(
                targetValue = targetSweep,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
                label = "gauge"
            )

            Box(
                modifier = Modifier.size(width = 200.dp, height = 120.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                val progressBgColor = AppColors.extended.progressBackground
                val warningColor = AppColors.extended.warningOrange
                val cardBgColor = AppColors.extended.cardBackground

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12f
                    val centerX = size.width / 2f
                    val centerY = size.height - 2f
                    val radius = (size.width / 2f) - strokeWidth / 2f - 4f

                    // 背景弧
                    drawArc(
                        color = progressBgColor,
                        startAngle = 180f, sweepAngle = 180f, useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(centerX - radius, centerY - radius),
                        size = Size(radius * 2, radius * 2)
                    )

                    // 进度弧
                    if (animatedSweep > 0f) {
                        drawArc(
                            color = warningColor,
                            startAngle = 180f, sweepAngle = animatedSweep.coerceIn(0f, 180f), useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = Offset(centerX - radius, centerY - radius),
                            size = Size(radius * 2, radius * 2)
                        )

                        // 末端指示器（沿弧线轨迹运动）
                        val endAngleRad = Math.toRadians((180 + animatedSweep).toDouble())
                        val dotX = centerX + (radius * kotlin.math.cos(endAngleRad)).toFloat()
                        val dotY = centerY + (radius * kotlin.math.sin(endAngleRad)).toFloat()
                        drawCircle(color = cardBgColor, radius = strokeWidth / 2f + 3f, center = Offset(dotX, dotY))
                        drawCircle(color = warningColor, radius = strokeWidth / 2f + 0.5f, center = Offset(dotX, dotY))
                    }
                }

                // 数字和文字叠在弧内部
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "$expiringCount",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.extended.warningOrange
                    )
                    Text(
                        text = "临期物品",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.extended.textTertiary
                    )
                }
            }

            Text(
                text = "即将过期，请及时处理",
                fontSize = 13.sp,
                color = AppColors.extended.textTertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 分隔线
            HorizontalDivider(color = AppColors.extended.divider, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(14.dp))

            // 三列统计
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMiniCard(label = "过期物品", count = expiredCount, bgColor = AppColors.extended.dangerRedBg, textColor = AppColors.extended.dangerRed)
                StatMiniCard(label = "未过期物品", count = notExpiredCount, bgColor = AppColors.extended.successGreenBg, textColor = AppColors.extended.successGreen)
                StatMiniCard(label = "总物品", count = totalCount, bgColor = AppColors.extended.infoBlueBg, textColor = AppColors.extended.infoBlue)
            }
        }
    }
}

@Composable
private fun RowScope.StatMiniCard(label: String, count: Int, bgColor: Color, textColor: Color) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
        ) {
            Text(text = "$count", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = textColor, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = label, fontSize = 11.sp, color = AppColors.extended.textTertiary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** 物品状态详情 */
@Composable
private fun ItemStatusDetailCard(
    expiringCount: Int,
    expiredCount: Int,
    notExpiredCount: Int,
    totalCount: Int
) {
    val total = totalCount.coerceAtLeast(1)
    val expiringPct = expiringCount.toFloat() / total * 100f
    val expiredPct = expiredCount.toFloat() / total * 100f
    val notExpiredPct = notExpiredCount.toFloat() / total * 100f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text("物品状态详情", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.extended.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            StatusDetailRow(Icons.Default.Schedule, AppColors.extended.warningOrange, "临期物品", expiringPct, {})
            StatusDetailRow(Icons.Default.Warning, AppColors.extended.dangerRed, "过期物品", expiredPct, {})
            StatusDetailRow(Icons.Default.CheckCircle, AppColors.extended.successGreen, "未过期物品", notExpiredPct, {})
        }
    }
}

@Composable
private fun StatusDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    percent: Float,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 圆形图标
        Box(
            modifier = Modifier.size(30.dp).clip(CircleShape).background(iconTint.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))

        // 标签文字
        Text(label, fontSize = 13.sp, color = AppColors.extended.textSecondary, modifier = Modifier.width(70.dp))

        // 进度条
        Box(
            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(AppColors.extended.progressBackground)
        ) {
            if (percent > 0) {
                Box(
                    modifier = Modifier.fillMaxHeight().fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(3.dp)).background(iconTint)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 百分比数字
        Text(
            text = String.format(Locale.getDefault(), "%.1f%%", percent),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.extended.textSecondary,
            modifier = Modifier.width(56.dp).wrapContentWidth(),
            textAlign = TextAlign.End,
            maxLines = 1
        )

        // 右箭头
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.extended.textTertiary, modifier = Modifier.size(18.dp))
    }
}

/** 菜单项 */
@Composable
private fun ProfileMenuCard(onBackupClick: () -> Unit, onDeleteExpiredClick: () -> Unit, savedSyncTime: String?) {
    val syncTimeText = if (savedSyncTime != null) {
        try {
            // 存储格式: "yyyy-MM-dd HH:mm"
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = fmt.parse(savedSyncTime)
            if (date != null) {
                val now = Calendar.getInstance()
                val syncCal = Calendar.getInstance().apply { time = date }
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                if (syncCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    syncCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                    "今天 $timeStr"
                } else {
                    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(date)
                }
            } else {
                "从未同步"
            }
        } catch (_: Exception) {
            "从未同步"
        }
    } else {
        "从未同步"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MenuItemRow(Icons.Default.CloudUpload, "数据备份与同步", "上次同步：$syncTimeText", AppColors.extended.infoBlueBg, AppColors.extended.infoBlue, onClick = onBackupClick)
            HorizontalDivider(color = AppColors.extended.divider, thickness = 1.dp)
            MenuItemRow(Icons.Default.DeleteSweep, "一键清除数据", "", AppColors.extended.dangerRedBg, AppColors.extended.dangerRed, titleColor = AppColors.extended.dangerRed, onClick = onDeleteExpiredClick)
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconBgColor: Color,
    iconTint: Color,
    titleColor: Color = AppColors.extended.textPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = titleColor, fontWeight = if (titleColor != AppColors.extended.textPrimary) FontWeight.Medium else FontWeight.Normal)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 11.sp, color = AppColors.extended.textTertiary)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.extended.textTertiary, modifier = Modifier.size(20.dp))
    }
}
