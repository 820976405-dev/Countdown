package com.expiryreminder.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onItemClicked: (Long) -> Unit,
    itemViewModel: ItemViewModel,
    categoryViewModel: CategoryViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

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
    val itemEmojiMap = remember {
        mainCategoriesList.flatMap { parent ->
            parent.subCategories.flatMap { sub ->
                sub.items.map { it.name to it.iconEmoji }
            }
        }.toMap()
    }
    val allTemplateItems = remember {
        mainCategoriesList.flatMap { parent ->
            parent.subCategories.flatMap { sub -> sub.items }
        }
    }

    val resolveItemEmoji = remember(categoryEmojiMap, itemEmojiMap, allTemplateItems) {
        { item: Item ->
            categoryEmojiMap[item.categoryId]?.takeIf { it.isNotEmpty() }
                ?: itemEmojiMap[item.name]?.takeIf { it.isNotEmpty() }
                ?: (allTemplateItems.find { template -> template.name.contains(item.name) || item.name.contains(template.name) }?.iconEmoji)?.takeIf { it.isNotEmpty() }
                ?: ""
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isSearching = true
            itemViewModel.searchItems(searchQuery).collectLatest { items ->
                searchResults = items
                isSearching = false
            }
        } else {
            searchResults = emptyList()
        }
    }

    ExpiryReminderTheme {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
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
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "清除",
                                                tint = AppColors.extended.textTertiary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
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
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.extended.successGreen)
                }
            } else if (searchQuery.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = AppColors.extended.placeholderText,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "输入关键词搜索全部物品",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.extended.textTertiary
                        )
                    }
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = AppColors.extended.placeholderText,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "未找到相关物品",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.extended.textTertiary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    item {
                        Text(
                            "找到 ${searchResults.size} 个结果",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.extended.textPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    items(searchResults) { item ->
                        SearchItemCard(
                            item = item,
                            onClick = { onItemClicked(item.id) },
                            categoryIcon = categoryIconMap[item.categoryId] ?: Icons.Default.Category,
                            categoryEmoji = resolveItemEmoji(item)
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
}

@Composable
fun SearchItemCard(
    item: Item,
    onClick: () -> Unit,
    categoryIcon: ImageVector = Icons.Default.Category,
    categoryEmoji: String = ""
) {
    val days = getDaysUntilExpiry(item.expireDate)
    val daysText = getDaysText(days)
    val daysColor = Color(android.graphics.Color.parseColor(getDaysTextColor(days)))

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
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.extended.iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    if (categoryEmoji.isNotEmpty()) {
                        Text(categoryEmoji, fontSize = 20.sp)
                    } else {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = AppColors.extended.textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
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
                    "${formatExpiryDate(item.expireDate)} 到期",
                    fontSize = 12.sp,
                    color = AppColors.extended.textTertiary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Text(
                daysText,
                style = MaterialTheme.typography.labelLarge,
                color = daysColor,
                fontWeight = FontWeight.Medium
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
