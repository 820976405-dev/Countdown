package com.expiryreminder.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expiryreminder.app.data.Category
import com.expiryreminder.app.data.Item
import com.expiryreminder.app.ui.theme.AppColors
import com.expiryreminder.app.ui.theme.ExpiryReminderTheme
import com.expiryreminder.app.util.*
import com.expiryreminder.app.viewmodel.CategoryViewModel
import com.expiryreminder.app.viewmodel.ItemViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    itemViewModel: ItemViewModel,
    categoryViewModel: CategoryViewModel
) {
    var totalItems by remember { mutableStateOf(0) }
    var expiringItems by remember { mutableStateOf(0) }
    var expiredItems by remember { mutableStateOf(0) }
    var allItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }

    LaunchedEffect(Unit) {
        itemViewModel.itemCount.collectLatest { count ->
            totalItems = count
        }
    }

    LaunchedEffect(Unit) {
        val sevenDaysFromNow = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)
        itemViewModel.expiringCount.collectLatest { count ->
            expiringItems = count
        }
    }

    LaunchedEffect(Unit) {
        itemViewModel.expiredCount.collectLatest { count ->
            expiredItems = count
        }
    }

    LaunchedEffect(Unit) {
        itemViewModel.allItems.collectLatest { items ->
            allItems = items
        }
    }

    LaunchedEffect(Unit) {
        categoryViewModel.parentCategories.collectLatest { cats ->
            categories = cats
        }
    }

    val categoryStats = remember(allItems, categories) {
        val subToParentMap = mainCategoriesList.flatMap { parent ->
            parent.subCategories.map { it.id to parent.id }
        }.toMap()
        categories.map { category ->
            val count = allItems.count { item ->
                item.categoryId == category.id || subToParentMap[item.categoryId] == category.id
            }
            CategoryStat(category, count)
        }.filter { it.count > 0 }
            .sortedByDescending { it.count }
    }

    ExpiryReminderTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "数据统计",
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
                item {
                    StatsOverviewCard(
                        totalItems = totalItems,
                        expiringItems = expiringItems,
                        expiredItems = expiredItems
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    CategoryPieChart(
                        categoryStats = categoryStats,
                        totalItems = totalItems
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text(
                        "分类占比",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.extended.textPrimary
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(categoryStats) { stat ->
                    CategoryStatItem(
                        stat = stat,
                        totalItems = totalItems
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text(
                        "最近到期",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.extended.textPrimary
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val expiringSoon = allItems
                    .filter { it.expireDate >= System.currentTimeMillis() }
                    .sortedBy { it.expireDate }
                    .take(5)

                items(expiringSoon) { item ->
                    ExpiringStatItem(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

data class CategoryStat(
    val category: Category,
    val count: Int
)

@Composable
fun StatsOverviewCard(
    totalItems: Int,
    expiringItems: Int,
    expiredItems: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatOverviewItem(
                count = totalItems,
                label = "总物品",
                countColor = AppColors.extended.successGreen
            )
            VerticalDivider(
                modifier = Modifier
                    .height(48.dp)
                    .width(1.dp),
                color = AppColors.extended.divider
            )
            StatOverviewItem(
                count = expiringItems,
                label = "临期",
                countColor = AppColors.extended.warningOrange
            )
            VerticalDivider(
                modifier = Modifier
                    .height(48.dp)
                    .width(1.dp),
                color = AppColors.extended.divider
            )
            StatOverviewItem(
                count = expiredItems,
                label = "已过期",
                countColor = AppColors.extended.dangerRed
            )
        }
    }
}

@Composable
fun StatOverviewItem(
    count: Int,
    label: String,
    countColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "$count",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = countColor,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.extended.textTertiary
        )
    }
}

@Composable
fun CategoryPieChart(
    categoryStats: List<CategoryStat>,
    totalItems: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "分类分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.extended.textPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (totalItems > 0 && categoryStats.isNotEmpty()) {
                val colors = listOf(
                    AppColors.extended.successGreen,
                    AppColors.extended.warningOrange,
                    AppColors.extended.dangerRed,
                    AppColors.extended.infoBlue,
                    Color(0xFFBA68C8),
                    Color(0xFFFFD54F),
                    Color(0xFF4DB6AC),
                    Color(0xFFF06292),
                    Color(0xFF7986CB),
                    Color(0xFFA1887F),
                    Color(0xFF90A4AE),
                    Color(0xFF8D6E63)
                )
                
                Canvas(
                    modifier = Modifier.size(160.dp)
                ) {
                    val total = categoryStats.sumOf { it.count }.toFloat()
                    var startAngle = -90f
                    
                    categoryStats.forEachIndexed { index, stat ->
                        val sweepAngle = (stat.count / total) * 360f
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true
                        )
                        startAngle += sweepAngle
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    categoryStats.take(3).forEachIndexed { index, stat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(colors[index % colors.size], RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                stat.category.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.extended.textTertiary
                            )
                        }
                    }
                }
            } else {
                Text(
                    "暂无数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.extended.textTertiary
                )
            }
        }
    }
}

@Composable
fun CategoryStatItem(
    stat: CategoryStat,
    totalItems: Int
) {
    val percentage = if (totalItems > 0) (stat.count.toFloat() / totalItems * 100) else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.extended.successGreenBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getCategoryIcon(stat.category.icon),
                    contentDescription = null,
                    tint = AppColors.extended.successGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stat.category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.extended.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { percentage / 100 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AppColors.extended.successGreen,
                    trackColor = AppColors.extended.progressBackground
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${stat.count}个",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.extended.textPrimary
                )
                Text(
                    String.format("%.1f%%", percentage),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.extended.textTertiary
                )
            }
        }
    }
}

@Composable
fun ExpiringStatItem(item: Item) {
    val days = getDaysUntilExpiry(item.expireDate)
    val daysText = getDaysText(days)
    val daysColor = Color(android.graphics.Color.parseColor(getDaysTextColor(days)))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.extended.dangerRedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Alarm,
                    contentDescription = null,
                    tint = AppColors.extended.dangerRed,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.extended.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    formatExpiryDate(item.expireDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.extended.textTertiary
                )
            }
            
            Text(
                daysText,
                style = MaterialTheme.typography.labelMedium,
                color = daysColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
