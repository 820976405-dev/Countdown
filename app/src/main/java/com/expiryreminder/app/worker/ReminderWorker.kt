package com.expiryreminder.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expiryreminder.app.MainActivity
import com.expiryreminder.app.R
import com.expiryreminder.app.data.AppDatabase
import com.expiryreminder.app.util.getDaysUntilExpiry
import com.expiryreminder.app.widget.WidgetUpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val items = database.itemDao().getAllItemsForWorker()
            
            val now = System.currentTimeMillis()
            val expiringItems = items.filter { item ->
                val days = getDaysUntilExpiry(item.expireDate)
                days >= 0 && days <= 7
            }

            if (expiringItems.isNotEmpty()) {
                createNotificationChannel()
                showNotification(expiringItems)
            }

            WidgetUpdateHelper.updateAllWidgets(applicationContext)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "expiry_reminder",
                "到期提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "物品到期提醒通知"
            }
            
            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(items: List<com.expiryreminder.app.data.Item>) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val inboxStyle = NotificationCompat.InboxStyle()
        items.take(5).forEach { item ->
            val days = getDaysUntilExpiry(item.expireDate)
            val daysText = when {
                days == 0L -> "今天到期"
                days == 1L -> "明天到期"
                else -> "${days}天后到期"
            }
            inboxStyle.addLine("${item.name} - $daysText")
        }
        if (items.size > 5) {
            inboxStyle.addLine("还有 ${items.size - 5} 个物品...")
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "expiry_reminder")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("到期提醒")
            .setContentText("有 ${items.size} 个物品即将到期")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(inboxStyle)

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }
}
