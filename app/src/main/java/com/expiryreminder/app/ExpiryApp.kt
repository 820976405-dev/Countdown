package com.expiryreminder.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.expiryreminder.app.data.AppDatabase
import com.expiryreminder.app.data.Category
import com.expiryreminder.app.notification.ReminderScheduler
import com.expiryreminder.app.worker.WidgetUpdateWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ExpiryApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        initDefaultCategories()
        ReminderScheduler.scheduleReminder(this)
        scheduleWidgetUpdate()
    }

    private fun scheduleWidgetUpdate() {
        val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "widget_update_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    private fun initDefaultCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            val categoryDao = database.categoryDao()
            val existing = categoryDao.getParentCategories().first()
            if (existing.isEmpty()) {
                val defaultCategories = listOf(
                    Category(name = "食品厨房", icon = "restaurant", parentId = null),
                    Category(name = "个人护理", icon = "local_laundry_service", parentId = null),
                    Category(name = "健康医疗", icon = "favorite", parentId = null),
                    Category(name = "证件文件", icon = "badge", parentId = null),
                    Category(name = "金融保险", icon = "account_balance", parentId = null),
                    Category(name = "会员订阅", icon = "stars", parentId = null),
                    Category(name = "数码设备", icon = "devices", parentId = null),
                    Category(name = "家居维护", icon = "home", parentId = null),
                    Category(name = "宠物用品", icon = "pets", parentId = null),
                    Category(name = "出行旅游", icon = "flight", parentId = null),
                    Category(name = "工作学习", icon = "work", parentId = null),
                    Category(name = "其他", icon = "category", parentId = null)
                )
                defaultCategories.forEach { categoryDao.insertCategory(it) }
            }
        }
    }
}
