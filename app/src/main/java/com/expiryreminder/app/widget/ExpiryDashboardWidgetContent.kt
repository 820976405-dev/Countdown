package com.expiryreminder.app.widget

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
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.expiryreminder.app.MainActivity

@Composable
fun ExpiryDashboardWidgetContent(data: DashboardWidgetData) {
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(intent))
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "到期提醒",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color(0xFF1F2329))
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "查看全部 →",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(Color(0xFF86909C))
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

            // 三个统计卡片
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 五天内到期
                StatCard(
                    count = data.expiringIn5Days,
                    label = "五天内到期",
                    countColor = Color(0xFFFF4D4F),
                    bgColor = Color(0xFFFFF1F0),
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                // 七天内到期
                StatCard(
                    count = data.expiringIn7Days,
                    label = "七天内到期",
                    countColor = Color(0xFFFF9500),
                    bgColor = Color(0xFFFFF7E6),
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                // 全部物品
                StatCard(
                    count = data.totalItems,
                    label = "全部物品",
                    countColor = Color(0xFF00B42A),
                    bgColor = Color(0xFFF0FFF4),
                    modifier = GlanceModifier.defaultWeight()
                )
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

            // 即将到期列表
            if (data.expiringItems.isNotEmpty()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "即将到期",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFF1F2329))
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "${data.expiringItemCount}项",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(Color(0xFF86909C))
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                LazyColumn(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                ) {
                    items(data.expiringItems) { item ->
                        DashboardExpiringItemRow(item)
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }
                }
            } else {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无即将到期的物品",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = ColorProvider(Color(0xFF86909C)),
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    count: Int,
    label: String,
    countColor: Color,
    bgColor: Color,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .background(bgColor)
            .cornerRadius(12.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(countColor)
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.sp,
                color = ColorProvider(Color(0xFF86909C)),
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun DashboardExpiringItemRow(item: ExpiringItem) {
    val daysColor = when {
        item.daysUntilExpiry <= 0 -> Color(0xFFFF4D4F)
        item.daysUntilExpiry <= 3 -> Color(0xFFFF4D4F)
        item.daysUntilExpiry <= 5 -> Color(0xFFFF9500)
        else -> Color(0xFFFF9500)
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF))
            .cornerRadius(8.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // emoji 图标
        Text(
            text = item.emoji,
            style = TextStyle(
                fontSize = 18.sp
            )
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = item.name,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(Color(0xFF1F2329))
                ),
                maxLines = 1
            )
        }
        Spacer(modifier = GlanceModifier.width(6.dp))
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = item.daysText,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(daysColor)
                )
            )
            Text(
                text = item.expireDateText,
                style = TextStyle(
                    fontSize = 9.sp,
                    color = ColorProvider(Color(0xFF86909C))
                )
            )
        }
    }
}
