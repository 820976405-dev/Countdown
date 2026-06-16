package com.expiryreminder.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.expiryreminder.app.data.AppDatabase
import com.expiryreminder.app.ui.screens.TemplateItem
import com.expiryreminder.app.ui.screens.mainCategoriesList
import com.expiryreminder.app.util.formatExpiryDate
import com.expiryreminder.app.util.getDaysUntilExpiry
import java.util.concurrent.TimeUnit

data class ExpiringItem(
    val name: String,
    val daysUntilExpiry: Long,
    val daysText: String,
    val emoji: String = "📦",
    val expireDateText: String = ""
)

data class DashboardWidgetData(
    val expiringIn5Days: Int = 0,
    val expiringIn7Days: Int = 0,
    val totalItems: Int = 0,
    val expiringItems: List<ExpiringItem> = emptyList(),
    val expiringItemCount: Int = 0
)

object ExpiryDashboardWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetData = loadDashboardData(context)

        provideContent {
            ExpiryDashboardWidgetContent(widgetData)
        }
    }

    private suspend fun loadDashboardData(context: Context): DashboardWidgetData {
        return try {
            val database = AppDatabase.getDatabase(context)
            val itemDao = database.itemDao()

            val startOfToday = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis

            val fiveDaysEnd = startOfToday + TimeUnit.DAYS.toMillis(6)
            val sevenDaysEnd = startOfToday + TimeUnit.DAYS.toMillis(8)

            val expiringIn5Days = itemDao.getExpiringSoonCountSync(startOfToday, fiveDaysEnd)
            val expiringIn7Days = itemDao.getExpiringSoonCountSync(startOfToday, sevenDaysEnd)
            val totalItems = itemDao.getTotalItemCount()

            // 构建 emoji 映射
            val categoryEmojiMap = mainCategoriesList.flatMap { parent ->
                parent.subCategories.map { sub ->
                    sub.id to (sub.items.firstOrNull()?.iconEmoji ?: "")
                }
            }.toMap()
            val itemEmojiMap = mainCategoriesList.flatMap { parent ->
                parent.subCategories.flatMap { sub ->
                    sub.items.map { it.name to it.iconEmoji }
                }
            }.toMap()
            val allTemplateItems = mainCategoriesList.flatMap { parent ->
                parent.subCategories.flatMap { sub -> sub.items }
            }

            val items = itemDao.getAllItemsForWorker()
            val allExpiring = items.filter { item ->
                val days = getDaysUntilExpiry(item.expireDate)
                days >= 0 && days <= 7
            }
            val expiringItems = allExpiring
                .take(6)
                .map { item ->
                    val days = getDaysUntilExpiry(item.expireDate)
                    val daysText = when {
                        days == 0L -> "今天"
                        days == 1L -> "明天"
                        else -> "${days}天后"
                    }
                    // 解析 emoji：子分类ID → 物品名精确 → 模糊匹配
                    val emoji = categoryEmojiMap[item.categoryId]?.takeIf { it.isNotEmpty() }
                        ?: itemEmojiMap[item.name]?.takeIf { it.isNotEmpty() }
                        ?: (allTemplateItems.find { template ->
                            template.name.contains(item.name) || item.name.contains(template.name)
                        }?.iconEmoji)?.takeIf { it.isNotEmpty() }
                        ?: "📦"

                    ExpiringItem(
                        name = item.name,
                        daysUntilExpiry = days,
                        daysText = daysText,
                        emoji = emoji,
                        expireDateText = formatExpiryDate(item.expireDate)
                    )
                }

            DashboardWidgetData(
                expiringIn5Days = expiringIn5Days,
                expiringIn7Days = expiringIn7Days,
                totalItems = totalItems,
                expiringItems = expiringItems,
                expiringItemCount = allExpiring.size
            )
        } catch (e: Exception) {
            DashboardWidgetData()
        }
    }
}
