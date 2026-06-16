package com.expiryreminder.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expiryreminder.app.data.Item
import com.expiryreminder.app.ui.theme.AppColors
import com.expiryreminder.app.viewmodel.ItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderItemsScreen(
    onNavigateBack: () -> Unit,
    onItemClicked: (Long) -> Unit,
    itemViewModel: ItemViewModel = viewModel()
) {
    var allItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("全部") }
    var selectedRemindTime by remember { mutableStateOf<String>("全部") }
    val subToParentMap = remember { mainCategoriesList.flatMap { parent -> parent.subCategories.map { it.id to parent.id } }.toMap() }

    val categoryIconMap = remember { mainCategoriesList.associate { it.id to it.icon } }
    val categoryEmojiMap = remember {
        mainCategoriesList.flatMap { parent -> parent.subCategories.map { sub -> sub.id to (sub.items.firstOrNull()?.iconEmoji ?: "") } }.toMap()
    }
    val itemEmojiMap = remember {
        mainCategoriesList.flatMap { parent -> parent.subCategories.flatMap { sub -> sub.items.map { it.name to it.iconEmoji } } }.toMap()
    }
    val allTemplateItems = remember { mainCategoriesList.flatMap { parent -> parent.subCategories.flatMap { sub -> sub.items } } }

    val resolveItemEmoji = remember(categoryEmojiMap, itemEmojiMap, allTemplateItems) {
        { item: Item ->
            categoryEmojiMap[item.categoryId]?.takeIf { it.isNotEmpty() }
                ?: itemEmojiMap[item.name]?.takeIf { it.isNotEmpty() }
                ?: (allTemplateItems.find { template -> template.name.contains(item.name) || item.name.contains(template.name) }?.iconEmoji)?.takeIf { it.isNotEmpty() }
                ?: ""
        }
    }

    LaunchedEffect(Unit) {
        itemViewModel.allItems.collectLatest { items ->
            allItems = items.filter { it.remindDays.isNotEmpty() }
        }
    }

    val listState = rememberLazyListState()
    var scrollResetKey by remember { mutableIntStateOf(0) }

    // 分类筛选 + 时间筛选
    val filteredItems = remember(allItems, selectedCategory, selectedRemindTime) {
        var result = if (selectedCategory == "全部") allItems
        else {
            val selectedParentId = mainCategoriesList.find { it.name == selectedCategory }?.id
            allItems.filter { item ->
                item.categoryId == selectedParentId || subToParentMap[item.categoryId] == selectedParentId
            }
        }
        // 时间筛选：按 remindDays 字段过滤
        if (selectedRemindTime != "全部") {
            val targetDay = when (selectedRemindTime) {
                "到期前7天" -> "7"
                "到期前1天" -> "1"
                "到期当天" -> "0"
                else -> ""
            }
            result = result.filter { it.remindDays.contains(targetDay) }
        }
        result
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提醒物品", fontWeight = FontWeight.Bold) },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 分类筛选栏 - 与全部物品页面一致
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
                        CategoryFilterChip(
                            name = "全部",
                            icon = Icons.Default.Apps,
                            isSelected = selectedCategory == "全部",
                            onClick = { selectedCategory = "全部" }
                        )
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

            // 时间筛选栏
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val timeOptions = listOf("全部", "到期前7天", "到期前1天", "到期当天")
                    timeOptions.forEach { option ->
                        val isSelected = selectedRemindTime == option
                        Text(
                            option,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else AppColors.extended.textSecondary,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) AppColors.extended.successGreen else AppColors.extended.chipBackground)
                                .clickable { selectedRemindTime = option }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AppColors.extended.textTertiary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("暂无设置提醒的物品", color = AppColors.extended.textSecondary)
                    }
                }
            } else {
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
    }
}
