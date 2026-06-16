package com.expiryreminder.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expiryreminder.app.ui.theme.AppColors

data class QuickAddItem(
    val name: String,
    val emoji: String,
    val bgColor: Color,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemEntryScreen(
    categories: List<Any> = emptyList(),
    onNavigateBack: () -> Unit,
    onNavigateToCategory: (Long) -> Unit,
    onNavigateToCustom: () -> Unit,
    onQuickAdd: (String, String) -> Unit,
    onNavigateToMore: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    
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
                        Icon(Icons.Default.Close, contentDescription = "关闭")
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
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { newValue: String -> searchText = newValue },
                    placeholder = { 
                        Text(
                            "搜索物品或分类，如：牛奶、身份证、车险...", 
                            color = AppColors.extended.placeholderText, 
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = null, 
                            tint = AppColors.extended.textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.extended.successGreen,
                        unfocusedBorderColor = AppColors.extended.inputBorder,
                        cursorColor = AppColors.extended.successGreen,
                        unfocusedContainerColor = AppColors.extended.inputBackground,
                        focusedContainerColor = AppColors.extended.cardBackground
                    ),
                    singleLine = true,
                    textStyle = TextStyle(
                        textAlign = TextAlign.Start,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = AppColors.extended.textPrimary
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            val searchResults = if (searchText.isNotBlank()) {
                val searchTextLower = searchText.lowercase()
                mainCategoriesList.flatMap { category ->
                    category.subCategories.flatMap { subCategory ->
                        subCategory.items.filter { 
                            it.name.lowercase().indexOf(searchTextLower as String) >= 0 ||
                            subCategory.name.lowercase().indexOf(searchTextLower as String) >= 0 ||
                            category.name.lowercase().indexOf(searchTextLower as String) >= 0
                        }.map { Triple(it, subCategory.name, category.name) }
                    }
                }
            } else {
                emptyList()
            }

            if (searchResults.isNotEmpty()) {
                item {
                    Text(
                        "搜索结果",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.extended.textPrimary
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                items(searchResults.size) { index ->
                    val (item, subCategoryName, categoryName) = searchResults[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onQuickAdd(item.name, subCategoryName) }
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.extended.cardBackground)
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.extended.iconBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.iconEmoji, fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.extended.textPrimary
                                )
                                Text(
                                    "$categoryName > $subCategoryName",
                                    fontSize = 12.sp,
                                    color = AppColors.extended.textTertiary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                if (item.description.isNotEmpty()) {
                                    Text(
                                        item.description,
                                        fontSize = 12.sp,
                                        color = AppColors.extended.placeholderText,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }
                            }

                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = AppColors.extended.chevronTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                if (searchText.isNotBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = AppColors.extended.placeholderText
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "未找到相关物品",
                                    fontSize = 14.sp,
                                    color = AppColors.extended.placeholderText
                                )
                                Text(
                                    "尝试使用其他关键词",
                                    fontSize = 12.sp,
                                    color = AppColors.extended.textTertiary
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            "常用快捷添加",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.extended.textPrimary
                        )
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                QuickAddItem("牛奶", "🥛", Color(0xFF4ECDC4), "生鲜食品"),
                                QuickAddItem("药品", "💊", Color(0xFFFF6B6B), "药品类"),
                                QuickAddItem("身份证", "🆔", Color(0xFF95E1D3), "个人证件"),
                                QuickAddItem("车险", "🚗", Color(0xFF45B7D1), "保险类"),
                                QuickAddItem("会员", "🎫", Color(0xFFFFA726), "娱乐会员")
                            ).forEach { item ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { 
                                                if (item.name == "更多") {
                                                    onNavigateToMore()
                                                } else {
                                                    onQuickAdd(item.name, item.category)
                                                }
                                            }
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(item.bgColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.emoji, fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        item.name,
                                        fontSize = 13.sp,
                                        color = AppColors.extended.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                QuickAddItem("护肤品", "🧴", Color(0xFFE056FD), "护肤品"),
                                QuickAddItem("牙刷", "🪥", Color(0xFF26C6DA), "口腔护理"),
                                QuickAddItem("灭火器", "🧯", Color(0xFFEF5350), "安全用品"),
                                QuickAddItem("会员", "🎫", Color(0xFFAB47BC), "娱乐会员"),
                                QuickAddItem("更多", "⋯", Color(0xFF95A5A6), "")
                            ).forEach { item ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { 
                                                if (item.name == "更多") {
                                                    onNavigateToMore()
                                                } else {
                                                    onQuickAdd(item.name, item.category)
                                                }
                                            }
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(item.bgColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.emoji, fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        item.name,
                                        fontSize = 13.sp,
                                        color = AppColors.extended.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(28.dp)) }

                    item {
                        Text(
                            "选择分类",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.extended.textPrimary
                        )
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    items(mainCategoriesList.chunked(2).size) { rowIndex ->
                        val rowCategories = mainCategoriesList.chunked(2)[rowIndex]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowCategories.forEach { category ->
                                CategoryGridItemNew(
                                    category = category,
                                    onClick = { onNavigateToCategory(category.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(2 - rowCategories.count()) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onNavigateToCustom() }
                                ),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.extended.chipBackground),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = AppColors.extended.successGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    "自定义物品/事项",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.extended.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = AppColors.extended.chevronTint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryGridItemNew(
    category: MainCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = category.bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                category.icon,
                contentDescription = null,
                tint = category.iconColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.extended.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = category.description,
                    fontSize = 11.sp,
                    color = AppColors.extended.textTertiary,
                    maxLines = 1
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.extended.chevronTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
