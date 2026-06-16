package com.expiryreminder.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.expiryreminder.app.MainActivity

object ExpirySmallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val stats = WidgetDataLoader.loadStats(context, maxItems = 0)
        provideContent {
            SmallWidgetContent(stats)
        }
    }
}

@Composable
fun SmallWidgetContent(data: WidgetDataLoader.WidgetStats) {
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .cornerRadius(16.dp)
            .padding(14.dp)
    ) {
        // 标题
        Text(
            text = "即将到期",
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color(0xFF1F2329))
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // 大数字 7 + 天内 / 即将到期
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = "7",
                style = TextStyle(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFFFF4D4F))
                )
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Column(
                modifier = GlanceModifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = "天内",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ColorProvider(Color(0xFF1F2329))
                    )
                )
                Text(
                    text = "即将到期",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = ColorProvider(Color(0xFF86909C))
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        // 底部：共 X 项（可点击跳转即将到期页面）
        val reminderIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "detail/-2")
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Color(0xFFFFFFFF))
                .cornerRadius(10.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable(actionStartActivity(reminderIntent)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\uD83D\uDCDD",
                style = TextStyle(fontSize = 15.sp)
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = "共 ${data.expiringIn7Days} 项",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color(0xFF4E5969))
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "\u203A",
                style = TextStyle(
                    fontSize = 18.sp,
                    color = ColorProvider(Color(0xFFC9CDD4))
                )
            )
        }
    }
}

class ExpirySmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ExpirySmallWidget
}
