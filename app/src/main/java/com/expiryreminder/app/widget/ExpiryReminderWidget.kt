package com.expiryreminder.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.expiryreminder.app.data.AppDatabase
import com.expiryreminder.app.ui.screens.mainCategoriesList
import com.expiryreminder.app.util.formatExpiryDate
import com.expiryreminder.app.util.getDaysUntilExpiry

data class ReminderWidgetData(
    val categories: List<ReminderCategory> = emptyList(),
    val allItems: List<ReminderWidgetItem> = emptyList()
)

data class ReminderCategory(
    val name: String,
    val emoji: String,
    val parentId: Long
)

data class ReminderWidgetItem(
    val name: String,
    val daysUntilExpiry: Long,
    val daysText: String,
    val emoji: String,
    val expireDateText: String,
    val categoryName: String
)

object ExpiryReminderWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetData = loadWidgetData(context)

        provideContent {
            ExpiryReminderWidgetContent(widgetData)
        }
    }

    private suspend fun loadWidgetData(context: Context): ReminderWidgetData {
        return try {
            val database = AppDatabase.getDatabase(context)
            val itemDao = database.itemDao()

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

            val subToParentMap = mainCategoriesList.flatMap { parent ->
                parent.subCategories.map { sub -> sub.id to parent.name }
            }.toMap()

            val parentEmojiMap = mainCategoriesList.associate { parent ->
                parent.id to (parent.subCategories.firstOrNull()?.items?.firstOrNull()?.iconEmoji ?: "📦")
            }

            // 只加载设置了提醒的物品，排除已过期
            val items = itemDao.getAllItemsForWorker()
                .filter { it.remindDays.isNotEmpty() }
                .mapNotNull { item ->
                    val days = getDaysUntilExpiry(item.expireDate)
                    if (days < 0) return@mapNotNull null

                    val emoji = categoryEmojiMap[item.categoryId]?.takeIf { it.isNotEmpty() }
                        ?: itemEmojiMap[item.name]?.takeIf { it.isNotEmpty() }
                        ?: (allTemplateItems.find { template ->
                            template.name.contains(item.name) || item.name.contains(template.name)
                        }?.iconEmoji)?.takeIf { it.isNotEmpty() }
                        ?: "📦"

                    val daysText = when {
                        days == 0L -> "今天到期"
                        days == 1L -> "明天到期"
                        else -> "${days}天后到期"
                    }

                    val categoryName = subToParentMap[item.categoryId] ?: "其他"

                    ReminderWidgetItem(
                        name = item.name,
                        daysUntilExpiry = days,
                        daysText = daysText,
                        emoji = emoji,
                        expireDateText = formatExpiryDate(item.expireDate),
                        categoryName = categoryName
                    )
                }
                .sortedBy { it.daysUntilExpiry }

            // 构建分类列表：只包含有物品的分类
            val categoryNames = items.map { it.categoryName }.distinct()
            val categories = categoryNames.mapNotNull { name ->
                val parent = mainCategoriesList.find { it.name == name }
                val emoji = parent?.let { parentEmojiMap[it.id] } ?: "📦"
                val parentId = parent?.id ?: 0L
                ReminderCategory(name = name, emoji = emoji, parentId = parentId)
            }

            ReminderWidgetData(
                categories = categories,
                allItems = items
            )
        } catch (e: Exception) {
            ReminderWidgetData()
        }
    }
}
