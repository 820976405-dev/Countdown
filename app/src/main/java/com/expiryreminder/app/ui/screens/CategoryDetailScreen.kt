package com.expiryreminder.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expiryreminder.app.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToTemplates: (String, List<TemplateItem>) -> Unit,
    onNavigateToCustom: () -> Unit,
    onItemSelected: (TemplateItem, String) -> Unit = { _, _ -> }
) {
    val category = mainCategoriesList.find { it.id == categoryId } ?: mainCategoriesList.first()
    
    var selectedSubCategoryIndex by remember { mutableIntStateOf(0) }
    var isUserInteracting by remember { mutableStateOf(false) }
    
    val rightListState = rememberLazyListState()
    val leftListState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        category.name,
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
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(AppColors.extended.sidebarBackground),
                state = leftListState,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(category.subCategories.size) { index ->
                    val subCategory = category.subCategories[index]
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { 
                                    selectedSubCategoryIndex = index
                                    isUserInteracting = true
                                }
                            )
                            .background(Color.Transparent)
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(24.dp)
                                    .background(
                                        if (selectedSubCategoryIndex == index) 
                                            AppColors.extended.successGreen
                                        else 
                                            Color.Transparent
                                    ),
                                contentAlignment = Alignment.CenterStart
                            ) {}
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                subCategory.name,
                                fontSize = 13.sp,
                                fontWeight = if (selectedSubCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSubCategoryIndex == index) AppColors.extended.successGreen else AppColors.extended.textSecondary,
                                maxLines = 2
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { 
                                    val allItems = category.subCategories.flatMap { it.items }
                                    onNavigateToTemplates("全部${category.name}", allItems)
                                }
                            )
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add, 
                                contentDescription = null, 
                                tint = AppColors.extended.successGreen, 
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "自定义${category.name}", 
                                fontSize = 12.sp, 
                                color = AppColors.extended.successGreen, 
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background),
                state = rightListState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                category.subCategories.forEachIndexed { subIndex, subCategory ->
                    item {
                        Text(
                            subCategory.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.extended.textPrimary,
                            modifier = Modifier.padding(bottom = 12.dp, top = if (subIndex > 0) 24.dp else 0.dp)
                        )
                    }
                    
                    items(subCategory.items) { item ->
                        TemplateItemRowNew(
                            item = item,
                            onClick = { 
                                onItemSelected(item, subCategory.name)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(500.dp))
                }
            }
        }
        
        LaunchedEffect(key1 = selectedSubCategoryIndex, key2 = category.id) {
            if (isUserInteracting && selectedSubCategoryIndex >= 0 && selectedSubCategoryIndex < category.subCategories.size) {
                var targetIndex = 0
                for (i in 0 until selectedSubCategoryIndex) {
                    targetIndex += 1 + category.subCategories[i].items.size
                }
                rightListState.scrollToItem(targetIndex)
            }
        }
    }
}

@Composable
fun TemplateItemRowNew(
    item: TemplateItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
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
                if (item.description.isNotEmpty()) {
                    Text(
                        item.description,
                        fontSize = 12.sp,
                        color = AppColors.extended.textTertiary,
                        modifier = Modifier.padding(top = 2.dp)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateSelectionScreen(
    categoryName: String,
    templates: List<TemplateItem>,
    onNavigateBack: () -> Unit,
    onTemplateSelected: (TemplateItem) -> Unit
) {
    val showWarning = categoryName.contains("化妆品") || categoryName.contains("护肤品") || 
                       categoryName.contains("睫毛膏") || categoryName.contains("眼线笔")

    val actualTemplates = remember(categoryName) {
        if (templates.isEmpty()) {
            if (categoryName == "全部分类") {
                mainCategoriesList.flatMap { category ->
                    category.subCategories.flatMap { it.items }
                }
            } else if (categoryName.startsWith("全部")) {
                val mainCategoryName = categoryName.removePrefix("全部")
                mainCategoriesList.find { it.name == mainCategoryName }
                    ?.subCategories?.flatMap { it.items } ?: emptyList()
            } else {
                mainCategoriesList.flatMap { category ->
                    category.subCategories.find { it.name == categoryName }?.items ?: emptyList()
                }
            }
        } else {
            templates
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        categoryName,
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
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
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
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(actualTemplates) { template ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTemplateSelected(template) }
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.extended.iconBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(template.iconEmoji, fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    template.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.extended.textPrimary
                                )
                                if (template.description.isNotEmpty()) {
                                    Text(
                                        template.description,
                                        fontSize = 12.sp,
                                        color = AppColors.extended.textTertiary
                                    )
                                }
                            }

                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = AppColors.extended.chevronTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.extended.chipBackground)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { }
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.extended.successGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("自定义${categoryName.replace("全部", "")}", fontSize = 14.sp, color = AppColors.extended.successGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    if (showWarning) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.extended.warningOrangeBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppColors.extended.warningOrangeBg)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AppColors.extended.warningOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    "提示：开封后的美妆产品建议关注使用期限，避免变质引起皮肤问题。",
                                    fontSize = 13.sp,
                                    color = AppColors.extended.warningOrange,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
