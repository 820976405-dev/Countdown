package com.expiryreminder.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expiryreminder.app.data.Category
import com.expiryreminder.app.ui.theme.AppColors
import com.expiryreminder.app.ui.theme.ExpiryReminderTheme
import com.expiryreminder.app.util.getCategoryIcon
import com.expiryreminder.app.viewmodel.CategoryViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageScreen(
    onNavigateBack: () -> Unit
) {
    val categoryViewModel: CategoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        categoryViewModel.parentCategories.collectLatest { cats ->
            categories = cats
        }
    }

    ExpiryReminderTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "物品分类管理",
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                items(categories) { category ->
                    CategoryManageItem(
                        category = category,
                        onDelete = { categoryViewModel.deleteCategory(category) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, icon ->
                val newCategory = Category(
                    name = name,
                    icon = icon,
                    parentId = null
                )
                categoryViewModel.addCategory(newCategory)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CategoryManageItem(
    category: Category,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.extended.successGreenBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getCategoryIcon(category.icon),
                    contentDescription = null,
                    tint = AppColors.extended.successGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                category.name,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.extended.textPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = AppColors.extended.dangerRed)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AppColors.extended.cardBackground,
            title = { Text("删除分类", fontWeight = FontWeight.Bold, color = AppColors.extended.textPrimary) },
            text = { Text("确定要删除「${category.name}」吗？", color = AppColors.extended.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
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
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("category") }

    val iconOptions = listOf(
        "restaurant" to "食品",
        "local_laundry_service" to "护理",
        "favorite" to "健康",
        "badge" to "证件",
        "account_balance" to "金融",
        "stars" to "会员",
        "devices" to "数码",
        "home" to "家居",
        "pets" to "宠物",
        "flight" to "出行",
        "work" to "工作",
        "category" to "其他"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.extended.cardBackground,
        title = { Text("添加分类", fontWeight = FontWeight.Bold, color = AppColors.extended.textPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称", color = AppColors.extended.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.extended.successGreen,
                        unfocusedBorderColor = AppColors.extended.inputBorder
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("选择图标", fontWeight = FontWeight.Medium, color = AppColors.extended.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(160.dp)
                ) {
                    items(iconOptions.size) { index ->
                        val (icon, label) = iconOptions[index]
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedIcon == icon) AppColors.extended.successGreenBg else AppColors.extended.inputBackground)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { selectedIcon = icon }
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    getCategoryIcon(icon),
                                    contentDescription = null,
                                    tint = if (selectedIcon == icon) AppColors.extended.successGreen else AppColors.extended.textTertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(text = label, fontSize = 10.sp, color = if (selectedIcon == icon) AppColors.extended.successGreen else AppColors.extended.textTertiary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onAdd(name, selectedIcon) },
                enabled = name.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
