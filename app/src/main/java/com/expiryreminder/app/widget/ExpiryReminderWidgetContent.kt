package com.expiryreminder.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.expiryreminder.app.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpiryReminderWidgetContent(data: ReminderWidgetData) {
    val context = LocalContext.current

    // 紧急数：<3天的物品
    val urgentCount = data.allItems.count { it.daysUntilExpiry in 0..2 }
    val dateText = SimpleDateFormat("M月d日 E", Locale.CHINESE).format(Date())

    val mainIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("navigate_to", "reminder_items")
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .cornerRadius(16.dp)
            .padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 8.dp)
    ) {
        // 顶部通栏
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity(mainIntent)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "即将到期",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF1F2329))
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            if (urgentCount > 0) {
                Text(
                    text = "${urgentCount}项紧急",
                    style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color(0xFFFF4D4F)))
                )
            }
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = dateText,
                style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color(0xFF1F2329)))
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (data.allItems.isEmpty()) {
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "暂无提醒物品",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = ColorProvider(Color(0xFF86909C)),
                    textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
        } else {
            // 左右分栏
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：数量显示面板
                Column(
                    modifier = GlanceModifier
                        .width(68.dp)
                        .height(120.dp)
                        .background(Color(0xFF00B42A))
                        .cornerRadius(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${data.allItems.size}",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.White)
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    Text(
                        text = "全部",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(Color.White)
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                // 右侧物品面板 - 统一白色面板，可滑动查看更多
                LazyColumn(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth()
                        .background(Color(0xFFFFFFFF))
                        .cornerRadius(12.dp)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    items(data.allItems) { item ->
                        ReminderItemRow(item)
                    }
                }
            }
        }
    }
}

/** 物品行 */
@Composable
fun ReminderItemRow(item: ReminderWidgetItem) {
    val statusColor = when {
        item.daysUntilExpiry < 0 -> Color(0xFF86909C)
        else -> Color(0xFFFF4D4F)
    }

    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.emoji, style = TextStyle(fontSize = 16.sp))
        Spacer(modifier = GlanceModifier.width(8.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = item.name,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(Color(0xFF1F2329))
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(1.dp))
            Text(
                text = run {
                    val parts = item.expireDateText.split("-")
                    if (parts.size >= 3) "${parts[1]}-${parts[2]} 到期" else item.expireDateText
                },
                style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFF86909C)))
            )
        }

        Text(
            text = item.daysText,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(statusColor)
            )
        )
    }
}
