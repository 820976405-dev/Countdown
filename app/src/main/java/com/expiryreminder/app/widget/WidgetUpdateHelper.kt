package com.expiryreminder.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager

object WidgetUpdateHelper {

    private const val TAG = "WidgetUpdateHelper"

    private val widgetReceivers = listOf(
        ExpiryReminderWidgetReceiver::class.java,
        ExpiryDashboardWidgetReceiver::class.java,
        ExpirySmallWidgetReceiver::class.java
    )

    private val widgetObjects = listOf(
        ExpiryReminderWidget,
        ExpiryDashboardWidget,
        ExpirySmallWidget
    )

    /**
     * 通知所有小组件更新数据
     * 在数据变更（增删改、云恢复）后调用
     */
    suspend fun updateAllWidgets(context: Context) {
        // 方式1：通过 GlanceAppWidget.update() 直接更新
        try {
            val manager = GlanceAppWidgetManager(context)
            for (widget in widgetObjects) {
                try {
                    val glanceIds = manager.getGlanceIds(widget.javaClass)
                    for (glanceId in glanceIds) {
                        widget.update(context, glanceId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Glance update failed for ${widget.javaClass.simpleName}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "GlanceAppWidgetManager failed", e)
        }

        // 方式2：通过 AppWidgetManager 广播兜底更新
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            for (receiverClass in widgetReceivers) {
                try {
                    val widgetIds = appWidgetManager.getAppWidgetIds(
                        android.content.ComponentName(context, receiverClass)
                    )
                    if (widgetIds.isNotEmpty()) {
                        val intent = Intent(context, receiverClass).apply {
                            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                        }
                        context.sendBroadcast(intent)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Broadcast update failed for ${receiverClass.simpleName}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "AppWidgetManager broadcast failed", e)
        }
    }
}
