package com.expiryreminder.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.expiryreminder.app.data.Item
import com.expiryreminder.app.ui.theme.AppColors
import com.expiryreminder.app.ui.theme.ExpiryReminderTheme
import com.expiryreminder.app.util.*
import com.expiryreminder.app.viewmodel.ItemViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: Long,
    onNavigateBack: () -> Unit,
    onEditClicked: (Long) -> Unit,
    itemViewModel: ItemViewModel
) {
    var item by remember { mutableStateOf<Item?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        if (itemId > 0) {
            item = itemViewModel.getItemById(itemId)
        }
    }

    ExpiryReminderTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (itemId > 0) "物品详情" else "全部物品",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        if (itemId > 0) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "更多")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            if (itemId > 0 && item != null) {
                ItemDetailContent(
                    item = item!!,
                    onEditClicked = { onEditClicked(item!!.id) },
                    onDeleteClicked = { showDeleteDialog = true },
                    modifier = Modifier.padding(padding)
                )
            } else if (itemId == -1L) {
                AllItemsList(
                    itemViewModel = itemViewModel,
                    onItemClicked = { id -> onEditClicked(id) },
                    modifier = Modifier.padding(padding)
                )
            } else if (itemId == -2L) {
                ExpiringItemsList(
                    itemViewModel = itemViewModel,
                    onItemClicked = { id -> onEditClicked(id) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (showDeleteDialog && item != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AppColors.extended.cardBackground,
            title = { Text("删除物品", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除「${item!!.name}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemViewModel.deleteItem(item!!)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppColors.extended.dangerRed
                    )
                ) {
                    Text("确定删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ItemDetailContent(
    item: Item,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val days = getDaysUntilExpiry(item.expireDate)
    val daysText = getDaysText(days)
    val daysColor = Color(android.graphics.Color.parseColor(getDaysTextColor(days)))
    var showImagePreview by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        LazyColumn(Modifier.fillMaxSize()) {
            if (item.imageUri != null) {
                item {
                    AsyncImage(
                        model = File(item.imageUri),
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showImagePreview = true }
                            )
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (item.categoryId != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = AppColors.extended.successGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "分类",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.extended.successGreen,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AppColors.extended.successGreenBg)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(Icons.Default.CalendarToday, "到期日期", formatExpiryDateWithWeekday(item.expireDate))
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = AppColors.extended.divider)
                        DetailRow(Icons.Default.AccessTime, "剩余天数", daysText, valueColor = daysColor)
                        if (item.purchaseDate != null) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = AppColors.extended.divider)
                            DetailRow(Icons.Default.ShoppingCart, "购买日期", formatExpiryDate(item.purchaseDate))
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = AppColors.extended.divider)
                        DetailRow(Icons.Default.Category, "数量", "${item.quantity}${item.unit}")
                        if (item.location.isNotBlank()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = AppColors.extended.divider)
                            DetailRow(Icons.Default.LocationOn, "存放位置", item.location)
                        }
                        if (item.note.isNotBlank()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = AppColors.extended.divider)
                            DetailRow(Icons.Default.Note, "备注", item.note)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditClicked,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.extended.successGreen),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(AppColors.extended.successGreen)
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("编辑")
                    }
                    OutlinedButton(
                        onClick = onDeleteClicked,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.extended.dangerRed),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(AppColors.extended.dangerRed)
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("删除")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text(
                    "提醒设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("到期前提醒", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val remindText = buildString {
                                val d = item.remindDays ?: ""
                                if (d.contains("7")) append("7天前、")
                                if (d.contains("1")) append("1天前、")
                                if (d.contains("0")) append("当天")
                                if (isEmpty()) append("未设置")
                                else if (endsWith("、")) delete(length - 1, length)
                            }
                            Text(remindText, style = MaterialTheme.typography.bodyMedium, color = AppColors.extended.textTertiary)
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = AppColors.extended.textTertiary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // 图片放大预览覆盖层
        if (showImagePreview && item.imageUri != null) {
            var scale by remember { mutableStateOf(1f) }
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }
            val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
                offsetX += panChange.x
                offsetY += panChange.y
            }
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f))
                    .pointerInput(Unit) { detectTapGestures(onTap = { showImagePreview = false }) },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(item.imageUri),
                    contentDescription = "预览图片",
                    modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.8f)
                        .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY)
                        .transformable(state = transformState)
                )
                IconButton(
                    onClick = { showImagePreview = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.extended.textTertiary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AppColors.extended.textTertiary, modifier = Modifier.width(70.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor, modifier = Modifier.weight(1f))
    }
}

@Composable
fun AllItemsList(
    itemViewModel: ItemViewModel,
    onItemClicked: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var allItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<String>("全部") }

    val categoryIconMap = remember { mainCategoriesList.associate { it.id to it.icon } }
    val categoryEmojiMap = remember {
        mainCategoriesList.flatMap { parent -> parent.subCategories.map { sub -> sub.id to (sub.items.firstOrNull()?.iconEmoji ?: "") } }.toMap()
    }
    val itemEmojiMap = remember {
        mainCategoriesList.flatMap { parent -> parent.subCategories.flatMap { sub -> sub.items.map { it.name to it.iconEmoji } } }.toMap()
    }
    val allTemplateItems = remember { mainCategoriesList.flatMap { parent -> parent.subCategories.flatMap { sub -> sub.items } } }
    val subToParentMap = remember { mainCategoriesList.flatMap { parent -> parent.subCategories.map { it.id to parent.id } }.toMap() }

    val resolveItemEmoji = remember(categoryEmojiMap, itemEmojiMap, allTemplateItems) {
        { item: Item ->
            categoryEmojiMap[item.categoryId]?.takeIf { it.isNotEmpty() }
                ?: itemEmojiMap[item.name]?.takeIf { it.isNotEmpty() }
                ?: (allTemplateItems.find { template -> template.name.contains(item.name) || item.name.contains(template.name) }?.iconEmoji)?.takeIf { it.isNotEmpty() }
                ?: ""
        }
    }

    LaunchedEffect(Unit) { itemViewModel.allItems.collectLatest { items -> allItems = items } }

    val listState = rememberLazyListState()
    var scrollResetKey by remember { mutableIntStateOf(0) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { scrollResetKey++ }
    }

    // 分类筛选
    val filteredItems = remember(allItems, selectedCategory) {
        if (selectedCategory == "全部") allItems
        else {
            val selectedParentId = mainCategoriesList.find { it.name == selectedCategory }?.id
            allItems.filter { item ->
                item.categoryId == selectedParentId || subToParentMap[item.categoryId] == selectedParentId
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 分类筛选栏 - 带图标的横向滚动标签
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // "全部" 选项
                    CategoryFilterChip(
                        name = "全部",
                        icon = Icons.Default.Apps,
                        isSelected = selectedCategory == "全部",
                        onClick = { selectedCategory = "全部" }
                    )
                    // 各分类选项
                    mainCategoriesList.forEach { category ->
                        CategoryFilterChip(
                            name = category.name,
                            icon = category.icon,
                            iconColor = category.iconColor,
                            bgColor = category.bgColor,
                            isSelected = selectedCategory == category.name,
                            onClick = { selectedCategory = category.name }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                DetailItemCard(
                    item = item,
                    onClick = { onItemClicked(item.id) },
                    onDelete = { itemViewModel.deleteItem(item) },
                    categoryIcon = categoryIconMap[item.categoryId] ?: Icons.Default.Category,
                    categoryEmoji = resolveItemEmoji(item),
                    scrollResetKey = scrollResetKey
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun CategoryFilterChip(
    name: String,
    icon: ImageVector,
    iconColor: Color = AppColors.extended.successGreen,
    bgColor: Color = AppColors.extended.successGreenBg,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .then(if (isSelected) Modifier.border(2.dp, iconColor, RoundedCornerShape(12.dp)) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            name,
            fontSize = 12.sp,
            color = if (isSelected) iconColor else AppColors.extended.textSecondary,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
fun ExpiringItemsList(
    itemViewModel: ItemViewModel,
    onItemClicked: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expiringItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<String>("全部") }

    val categoryIconMap = remember { mainCategoriesList.associate { it.id to it.icon } }
    val categoryEmojiMap = remember {
        mainCategoriesList.flatMap { parent -> parent.subCategories.map { sub -> sub.id to (sub.items.firstOrNull()?.iconEmoji ?: "") } }.toMap()
    }
    val itemEmojiMap = remember {
        mainCategoriesList.flatMap { parent -> parent.subCategories.flatMap { sub -> sub.items.map { it.name to it.iconEmoji } } }.toMap()
    }
    val allTemplateItems = remember { mainCategoriesList.flatMap { parent -> parent.subCategories.flatMap { sub -> sub.items } } }
    val subToParentMap = remember { mainCategoriesList.flatMap { parent -> parent.subCategories.map { it.id to parent.id } }.toMap() }

    val resolveItemEmoji = remember(categoryEmojiMap, itemEmojiMap, allTemplateItems) {
        { item: Item ->
            categoryEmojiMap[item.categoryId]?.takeIf { it.isNotEmpty() }
                ?: itemEmojiMap[item.name]?.takeIf { it.isNotEmpty() }
                ?: (allTemplateItems.find { template -> template.name.contains(item.name) || item.name.contains(template.name) }?.iconEmoji)?.takeIf { it.isNotEmpty() }
                ?: ""
        }
    }

    LaunchedEffect(Unit) { itemViewModel.expiringItems.collectLatest { items -> expiringItems = items.filter { getDaysUntilExpiry(it.expireDate) >= 0 } } }

    val listState = rememberLazyListState()
    var scrollResetKey by remember { mutableIntStateOf(0) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { scrollResetKey++ }
    }

    // 分类筛选
    val filteredItems = remember(expiringItems, selectedCategory) {
        if (selectedCategory == "全部") expiringItems
        else {
            val selectedParentId = mainCategoriesList.find { it.name == selectedCategory }?.id
            expiringItems.filter { item ->
                item.categoryId == selectedParentId || subToParentMap[item.categoryId] == selectedParentId
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 分类筛选栏 - 带图标的横向滚动标签
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // "全部" 选项
                    CategoryFilterChip(
                        name = "全部",
                        icon = Icons.Default.Apps,
                        isSelected = selectedCategory == "全部",
                        onClick = { selectedCategory = "全部" }
                    )
                    // 各分类选项
                    mainCategoriesList.forEach { category ->
                        CategoryFilterChip(
                            name = category.name,
                            icon = category.icon,
                            iconColor = category.iconColor,
                            bgColor = category.bgColor,
                            isSelected = selectedCategory == category.name,
                            onClick = { selectedCategory = category.name }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                DetailItemCard(
                    item = item,
                    onClick = { onItemClicked(item.id) },
                    onDelete = { itemViewModel.deleteItem(item) },
                    categoryIcon = categoryIconMap[item.categoryId] ?: Icons.Default.Category,
                    categoryEmoji = resolveItemEmoji(item),
                    scrollResetKey = scrollResetKey
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun DetailItemCard(
    item: Item,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    categoryIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Category,
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

    LaunchedEffect(scrollResetKey) {
        if (offsetX.value != 0f) {
            offsetX.animateTo(0f, tween(200))
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
    ) {
        // 删除按钮背景
        Box(
            modifier = Modifier.matchParentSize().background(AppColors.extended.dangerRed, RoundedCornerShape(12.dp))
        )
        Box(
            modifier = Modifier.align(Alignment.CenterEnd).width(deleteWidth).fillMaxHeight()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("删除", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        // 卡片内容
        Box(
            modifier = Modifier.fillMaxWidth()
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
                colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
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
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.extended.progressBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            if (categoryEmoji.isNotEmpty()) {
                                Text(categoryEmoji, fontSize = 28.sp)
                            } else {
                                Icon(imageVector = categoryIcon, contentDescription = null, tint = AppColors.extended.textTertiary, modifier = Modifier.size(28.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${formatExpiryDate(item.expireDate)} 到期", style = MaterialTheme.typography.bodySmall, color = AppColors.extended.textTertiary)
                    }

                    Text(daysText, style = MaterialTheme.typography.labelLarge, color = daysColor, fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.extended.textTertiary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
