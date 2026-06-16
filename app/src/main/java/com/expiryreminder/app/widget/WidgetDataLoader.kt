package com.expiryreminder.app.widget

import android.content.Context
import com.expiryreminder.app.data.AppDatabase
import com.expiryreminder.app.ui.screens.mainCategoriesList
import com.expiryreminder.app.util.formatExpiryDate
import com.expiryreminder.app.util.getDaysUntilExpiry
import java.util.concurrent.TimeUnit

object WidgetDataLoader {

    data class WidgetStats(
        val expiringIn5Days: Int = 0,
        val expiringIn7Days: Int = 0,
        val totalItems: Int = 0,
        val expiringItems: List<ExpiringItem> = emptyList()
    )

    /** 提醒物品数据（2×4小组件使用） */
    data class ReminderItemsData(
        val allItems: List<ReminderItem> = emptyList()
    )

    /** 提醒物品条目 */
    data class ReminderItem(
        val name: String,
        val daysUntilExpiry: Long,
        val emoji: String,
        val expireDateText: String,
        val remindDays: String
    )

    suspend fun loadStats(context: Context, maxItems: Int = 4): WidgetStats {
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
            val expiringItems = items
                .filter { item ->
                    val days = getDaysUntilExpiry(item.expireDate)
                    days >= 0 && days <= 7
                }
                .take(maxItems)
                .map { item ->
                    val days = getDaysUntilExpiry(item.expireDate)
                    val daysText = when {
                        days == 0L -> "今天"
                        days == 1L -> "明天"
                        else -> "${days}天后"
                    }
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

            WidgetStats(
                expiringIn5Days = expiringIn5Days,
                expiringIn7Days = expiringIn7Days,
                totalItems = totalItems,
                expiringItems = expiringItems
            )
        } catch (e: Exception) {
            WidgetStats()
        }
    }

    /** 加载提醒物品数据（2×4小组件使用，只加载设置了提醒的物品） */
    suspend fun loadReminderItems(context: Context): ReminderItemsData {
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

            // 只加载设置了提醒的物品
            val items = itemDao.getAllItemsForWorker().filter { it.remindDays.isNotEmpty() }

            val reminderItems = items
                .mapNotNull { item ->
                    val days = getDaysUntilExpiry(item.expireDate)
                    // 排除已过期的物品
                    if (days < 0) return@mapNotNull null

                    val emoji = categoryEmojiMap[item.categoryId]?.takeIf { it.isNotEmpty() }
                        ?: itemEmojiMap[item.name]?.takeIf { it.isNotEmpty() }
                        ?: (allTemplateItems.find { template ->
                            template.name.contains(item.name) || item.name.contains(template.name)
                        }?.iconEmoji)?.takeIf { it.isNotEmpty() }
                        ?: "📦"

                    ReminderItem(
                        name = item.name,
                        daysUntilExpiry = days,
                        emoji = emoji,
                        expireDateText = formatExpiryDate(item.expireDate),
                        remindDays = item.remindDays
                    )
                }
                .sortedBy { it.daysUntilExpiry }

            ReminderItemsData(allItems = reminderItems)
        } catch (e: Exception) {
            ReminderItemsData()
        }
    }
}
