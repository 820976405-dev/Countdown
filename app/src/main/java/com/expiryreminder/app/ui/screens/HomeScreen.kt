package com.expiryreminder.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.expiryreminder.app.data.Item
import com.expiryreminder.app.ui.theme.AppColors
import com.expiryreminder.app.ui.theme.ExpiryReminderTheme
import com.expiryreminder.app.util.*
import com.expiryreminder.app.viewmodel.CategoryViewModel
import com.expiryreminder.app.viewmodel.ItemViewModel
import kotlinx.coroutines.flow.collectLatest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onItemClicked: (Long) -> Unit,
    onAllItemsClicked: () -> Unit,
    onExpiringItemsClicked: () -> Unit = {},
    onNavigateToReminderItems: () -> Unit = {},
    itemViewModel: ItemViewModel,
    categoryViewModel: CategoryViewModel
) {
    var expiringCount by remember { mutableStateOf(0) }
    var expiringItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var allItems by remember { mutableStateOf<List<Item>>(emptyList()) }

    // 列表滚动状态，用于滑动卡片重置
    val listState = rememberLazyListState()
    var scrollResetKey by remember { mutableIntStateOf(0) }

    // 监听列表滚动，更新重置计数
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { scrollResetKey++ }
    }

    // 从 CategoriesData.mainCategoriesList 同步构建图标映射，无需等待数据库
    // 父分类 → ImageVector，子分类 → emoji
    val categoryIconMap = remember {
        mainCategoriesList.associate { it.id to it.icon }
    }
    val categoryEmojiMap = remember {
        mainCategoriesList.flatMap { parent ->
            parent.subCategories.map { sub ->
                sub.id to (sub.items.firstOrNull()?.iconEmoji ?: "")
            }
        }.toMap()
    }
    // 物品名 → emoji 兜底映射（兼容旧数据：categoryId 不准确时通过名称匹配）
    val itemEmojiMap = remember {
        mainCategoriesList.flatMap { parent ->
            parent.subCategories.flatMap { sub ->
                sub.items.map { it.name to it.iconEmoji }
            }
        }.toMap()
    }
    // 所有模板物品列表（用于模糊匹配）
    val allTemplateItems = remember {
        mainCategoriesList.flatMap { parent ->
            parent.subCategories.flatMap { sub -> sub.items }
        }
    }

    /** 三级图标解析：子分类ID → 物品名精确 → 模糊匹配 */
    val resolveItemEmoji = remember(categoryEmojiMap, itemEmojiMap, allTemplateItems) {
        { item: Item ->
            categoryEmojiMap[item.categoryId]?.takeIf { it.isNotEmpty() }
                ?: itemEmojiMap[item.name]?.takeIf { it.isNotEmpty() }
                ?: (allTemplateItems.find { template -> template.name.contains(item.name) || item.name.contains(template.name) }?.iconEmoji)?.takeIf { it.isNotEmpty() }
                ?: ""
        }
    }

    LaunchedEffect(Unit) {
        itemViewModel.expiringCount.collectLatest { count ->
            expiringCount = count
        }
    }

    LaunchedEffect(Unit) {
        itemViewModel.expiringItems.collectLatest { items ->
            expiringItems = items
        }
    }

    LaunchedEffect(Unit) {
        itemViewModel.allItems.collectLatest { items ->
            allItems = items
        }
    }

    ExpiryReminderTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "到期提醒",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = onNavigateToReminderItems) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "通知",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                item {
                    ExpiringSummaryCard(count = expiringCount)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "即将到期",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onExpiringItemsClicked,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = AppColors.extended.textTertiary
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(expiringItems.filter { val d = getDaysUntilExpiry(it.expireDate); d >= 0 && d <= 7 }.take(3), key = { "expiring_${it.id}" }) { item ->
                    ExpiringItemCard(
                        item = item,
                        onClick = { onItemClicked(item.id) },
                        onDelete = { itemViewModel.deleteItem(item) },
                        categoryIcon = categoryIconMap[item.categoryId] ?: Icons.Default.Category,
                        categoryEmoji = resolveItemEmoji(item),
                        scrollResetKey = scrollResetKey
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "全部物品",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onAllItemsClicked,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "查看全部",
                                tint = AppColors.extended.textTertiary
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(allItems.take(5), key = { "all_${it.id}" }) { item ->
                    AllItemCard(
                        item = item,
                        onClick = { onItemClicked(item.id) },
                        onDelete = { itemViewModel.deleteItem(item) },
                        categoryIcon = categoryIconMap[item.categoryId] ?: Icons.Default.Category,
                        categoryEmoji = resolveItemEmoji(item),
                        scrollResetKey = scrollResetKey
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun ExpiringSummaryCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.extended.dangerRedBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "即将到期",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.extended.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "$count",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.extended.dangerRed,
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "项",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.extended.textSecondary,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "未来 7 天内到期",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.extended.textTertiary
                )
            }
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Alarm,
                    contentDescription = null,
                    tint = Color(0xFFFF8A80),
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

@Composable
fun ExpiringItemCard(
    item: Item,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    categoryIcon: ImageVector = Icons.Default.Category,
    categoryEmoji: String = "",
    scrollResetKey: Int = 0
) {
    val days = getDaysUntilExpiry(item.expireDate)
    val daysText = getDaysText(days)
    val daysColor = Color(0xFFFF4D4F)

    val deleteWidth = 80.dp
    val deleteWidthPx = with(LocalDensity.current) { deleteWidth.toPx() }
    val threshold = deleteWidthPx * 0.3f
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isHorizontalDrag by remember { mutableStateOf(false) }

    // 滚动时自动收起已展开的卡片
    LaunchedEffect(scrollResetKey) {
        if (offsetX.value != 0f) {
            offsetX.animateTo(0f, tween(200))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // 删除按钮背景（底层，铺满整个区域）
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFFF4D4F), RoundedCornerShape(12.dp))
        )

        // 删除按钮可点击区域（右侧固定宽度）
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(deleteWidth)
                .fillMaxHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDelete
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("删除", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        // 卡片内容（上层，可滑动）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = offsetX.value }
                .pointerInput(deleteWidthPx, item.id) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var directionDecided = false
                        var isHorizontal = false
                        var lastX = down.position.x
                        var moved = false

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break

                            val totalDx = change.position.x - down.position.x
                            val totalDy = change.position.y - down.position.y

                            if (!directionDecided) {
                                val touchSlop = viewConfiguration.touchSlop
                                if (kotlin.math.abs(totalDx) > touchSlop || kotlin.math.abs(totalDy) > touchSlop) {
                                    directionDecided = true
                                    isHorizontal = kotlin.math.abs(totalDx) > kotlin.math.abs(totalDy)
                                    moved = true
                                    if (isHorizontal) {
                                        isHorizontalDrag = true
                                        lastX = change.position.x
                                    }
                                }
                            }

                            if (directionDecided && isHorizontal) {
                                change.consume()
                                val dx = change.position.x - lastX
                                val newOffset = (offsetX.value + dx).coerceIn(-deleteWidthPx, 0f)
                                scope.launch { offsetX.snapTo(newOffset) }
                                lastX = change.position.x
                            }
                        } while (change.pressed)

                        if (!moved) {
                            if (offsetX.value == 0f) onClick()
                            else scope.launch { offsetX.animateTo(0f, tween(250)) }
                        } else if (isHorizontalDrag) {
                            if (offsetX.value < -threshold) {
                                scope.launch { offsetX.animateTo(-deleteWidthPx, tween(250)) }
                            } else {
                                scope.launch { offsetX.animateTo(0f, tween(250)) }
                            }
                        }
                        isHorizontalDrag = false
                    }
                }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.extended.cardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.extended.cardBackground)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val uri = item.imageUri
            val hasImage = !uri.isNullOrBlank() && File(uri).exists()
            if (hasImage) {
                AsyncImage(
                    model = File(uri),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.extended.iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    if (categoryEmoji.isNotEmpty()) {
                        Text(categoryEmoji, fontSize = 28.sp)
                    } else {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = AppColors.extended.textTertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.extended.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${formatExpiryDate(item.expireDate)} 到期",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.extended.textTertiary
                )
            }

            Text(
                daysText,
                style = MaterialTheme.typography.labelLarge,
                color = daysColor,
                fontWeight = FontWeight.Medium
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
}

@Composable
fun AllItemCard(
    item: Item,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    categoryIcon: ImageVector = Icons.Default.Category,
    categoryEmoji: String = "",
    scrollResetKey: Int = 0
) {
    val days = getDaysUntilExpiry(item.expireDate)
    val daysText = getDaysText(days)
    val daysColor = Color(android.graphics.Color.parseColor(getDaysTextColor(days)))

    val deleteWidth = 80.dp
    val deleteWidthPx = with(LocalDensity.current) { deleteWidth.toPx() }
    val threshold = deleteWidthPx * 0.3f
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isHorizontalDrag by remember { mutableStateOf(false) }

    // 滚动时自动收起已展开的卡片
    LaunchedEffect(scrollResetKey) {
        if (offsetX.value != 0f) {
            offsetX.animateTo(0f, tween(200))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // 删除按钮背景（底层，铺满整个区域）
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFFF4D4F), RoundedCornerShape(12.dp))
        )

        // 删除按钮可点击区域（右侧固定宽度）
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(deleteWidth)
                .fillMaxHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDelete
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("删除", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        // 卡片内容（上层，可滑动）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = offsetX.value }
                .pointerInput(deleteWidthPx, item.id) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var directionDecided = false
                        var isHorizontal = false
                        var lastX = down.position.x
                        var moved = false

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break

                            val totalDx = change.position.x - down.position.x
                            val totalDy = change.position.y - down.position.y

                            if (!directionDecided) {
                                val touchSlop = viewConfiguration.touchSlop
                                if (kotlin.math.abs(totalDx) > touchSlop || kotlin.math.abs(totalDy) > touchSlop) {
                                    directionDecided = true
                                    isHorizontal = kotlin.math.abs(totalDx) > kotlin.math.abs(totalDy)
                                    moved = true
                                    if (isHorizontal) {
                                        isHorizontalDrag = true
                                        lastX = change.position.x
                                    }
                                }
                            }

                            if (directionDecided && isHorizontal) {
                                change.consume()
                                val dx = change.position.x - lastX
                                val newOffset = (offsetX.value + dx).coerceIn(-deleteWidthPx, 0f)
                                scope.launch { offsetX.snapTo(newOffset) }
                                lastX = change.position.x
                            }
                        } while (change.pressed)

                        if (!moved) {
                            if (offsetX.value == 0f) onClick()
                            else scope.launch { offsetX.animateTo(0f, tween(250)) }
                        } else if (isHorizontalDrag) {
                            if (offsetX.value < -threshold) {
                                scope.launch { offsetX.animateTo(-deleteWidthPx, tween(250)) }
                            } else {
                                scope.launch { offsetX.animateTo(0f, tween(250)) }
                            }
                        }
                        isHorizontalDrag = false
                    }
                }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.extended.cardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.extended.cardBackground)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val uri = item.imageUri
            val hasImage = !uri.isNullOrBlank() && File(uri).exists()
            if (hasImage) {
                AsyncImage(
                    model = File(uri),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.extended.iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    if (categoryEmoji.isNotEmpty()) {
                        Text(categoryEmoji, fontSize = 28.sp)
                    } else {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = AppColors.extended.textTertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.extended.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${formatExpiryDate(item.expireDate)} 到期",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.extended.textTertiary
                )
            }

            Text(
                daysText,
                style = MaterialTheme.typography.labelLarge,
                color = daysColor,
                fontWeight = FontWeight.Medium
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
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onNavigateToHome,
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "首页",
                    tint = if (currentRoute == "home") Color(0xFF5FCF80) else Color(0xFF86909C)
                )
            },
            label = {
                Text(
                    "首页",
                    color = if (currentRoute == "home") Color(0xFF5FCF80) else Color(0xFF86909C),
                    fontSize = 11.sp
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == "add",
            onClick = onNavigateToAdd,
            icon = {
                Icon(
                    Icons.Default.AddCircleOutline,
                    contentDescription = "添加",
                    tint = if (currentRoute == "add") Color(0xFF5FCF80) else Color(0xFF86909C)
                )
            },
            label = {
                Text(
                    "添加",
                    color = if (currentRoute == "add") Color(0xFF5FCF80) else Color(0xFF86909C),
                    fontSize = 11.sp
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = onNavigateToProfile,
            icon = {
                Icon(
                    Icons.Default.PersonOutline,
                    contentDescription = "我的",
                    tint = if (currentRoute == "profile") Color(0xFF5FCF80) else Color(0xFF86909C)
                )
            },
            label = {
                Text(
                    "我的",
                    color = if (currentRoute == "profile") Color(0xFF5FCF80) else Color(0xFF86909C),
                    fontSize = 11.sp
                )
            }
        )
    }
}
