package com.expiryreminder.app.notification

import android.content.Context
import androidx.work.*
import com.expiryreminder.app.worker.ReminderWorker
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_NAME = "expiry_reminder_work"

    fun scheduleReminder(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
