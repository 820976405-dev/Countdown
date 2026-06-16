package com.expiryreminder.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class MainCategory(
    val id: Long,
    val name: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color,
    val description: String,
    val subCategories: List<SubCategory>
)

data class SubCategory(
    val id: Long,
    val name: String,
    val items: List<TemplateItem>
)

data class TemplateItem(
    val id: Long,
    val name: String,
    val iconEmoji: String,
    val description: String = ""
)

val mainCategoriesList = listOf(
    MainCategory(
        id = 1,
        name = "食品厨房",
        icon = Icons.Default.Restaurant,
        iconColor = Color(0xFFFF9500),
        bgColor = Color(0xFFFFF3E0),
        description = "生鲜、零食、调料",
        subCategories = listOf(
            SubCategory(
                id = 101,
                name = "生鲜食品",
                items = listOf(
                    TemplateItem(1001, "牛奶", "🥛", "冷藏保质期7天"),
                    TemplateItem(1002, "酸奶", "🥛", "冷藏保质期21天"),
                    TemplateItem(1003, "肉类", "🥩", "冷藏3-5天"),
                    TemplateItem(1004, "海鲜", "🦐", "冷藏1-2天"),
                    TemplateItem(1005, "蔬菜", "🥬", "冷藏3-7天"),
                    TemplateItem(1006, "水果", "🍎", "常温3-7天"),
                    TemplateItem(1007, "鸡蛋", "🥚", "冷藏21天"),
                    TemplateItem(1008, "豆腐", "🧈", "冷藏2-3天")
                )
            ),
            SubCategory(
                id = 102,
                name = "加工食品",
                items = listOf(
                    TemplateItem(1101, "面包", "🍞", "室温3-5天"),
                    TemplateItem(1102, "蛋糕", "🎂", "冷藏3-5天"),
                    TemplateItem(1103, "饼干", "🍪", "常温1-3个月"),
                    TemplateItem(1104, "零食", "🍿", "见包装日期"),
                    TemplateItem(1105, "罐头", "🥫", "未开封1-2年"),
                    TemplateItem(1106, "方便面", "🍜", "常温6个月"),
                    TemplateItem(1107, "速冻食品", "❄️", "冷冻3-12个月"),
                    TemplateItem(1108, "火腿肠", "🌭", "常温6个月")
                )
            ),
            SubCategory(
                id = 103,
                name = "调味品",
                items = listOf(
                    TemplateItem(1201, "食用油", "🫒", "开封后3个月"),
                    TemplateItem(1202, "盐", "🧂", "长期保存"),
                    TemplateItem(1203, "酱油", "🫗", "开封后3-6个月"),
                    TemplateItem(1204, "醋", "🫗", "开封后6-12个月"),
                    TemplateItem(1205, "蚝油", "🦪", "开封后冷藏3-6个月"),
                    TemplateItem(1206, "豆瓣酱", "🌶️", "开封后冷藏6个月"),
                    TemplateItem(1207, "鸡精", "🐔", "开封后12-18个月"),
                    TemplateItem(1208, "香料", "🌿", "干燥处1-2年")
                )
            ),
            SubCategory(
                id = 104,
                name = "饮料酒水",
                items = listOf(
                    TemplateItem(1301, "果汁", "🧃", "开封后冷藏3-5天"),
                    TemplateItem(1302, "碳酸饮料", "🥤", "未开封6-9个月"),
                    TemplateItem(1303, "茶饮料", "🍵", "未开封12个月"),
                    TemplateItem(1304, "咖啡", "☕", "未开封12个月"),
                    TemplateItem(1305, "奶茶", "🧋", "冷藏24小时"),
                    TemplateItem(1306, "啤酒", "🍺", "未开封6-12个月"),
                    TemplateItem(1307, "红酒", "🍷", "开瓶后3-5天"),
                    TemplateItem(1308, "白酒", "🍶", "长期保存")
                )
            ),
            SubCategory(
                id = 105,
                name = "奶粉辅食",
                items = listOf(
                    TemplateItem(1401, "奶粉", "🍼", "开封后1个月"),
                    TemplateItem(1402, "婴儿辅食", "👶", "见包装说明"),
                    TemplateItem(1403, "孕妇营养品", "🤰", "见包装日期"),
                    TemplateItem(1404, "运动补剂", "💪", "开封后6个月")
                )
            ),
            SubCategory(
                id = 106,
                name = "特殊食品",
                items = listOf(
                    TemplateItem(1501, "保健品", "💊", "见包装日期"),
                    TemplateItem(1502, "营养补充剂", "🥗", "见包装日期")
                )
            )
        )
    ),
    MainCategory(
        id = 2,
        name = "个人护理",
        icon = Icons.Default.Face,
        iconColor = Color(0xFFFF4081),
        bgColor = Color(0xFFFCE4EC),
        description = "美妆、洗护、用品",
        subCategories = listOf(
            SubCategory(
                id = 201,
                name = "护肤品",
                items = listOf(
                    TemplateItem(2001, "面霜", "🧴", "开封后6-12个月"),
                    TemplateItem(2002, "乳液", "🧴", "开封后6-12个月"),
                    TemplateItem(2003, "精华液", "💧", "开封后6-12个月"),
                    TemplateItem(2004, "爽肤水", "💦", "开封后6-12个月"),
                    TemplateItem(2005, "眼霜", "👁️", "开封后6个月"),
                    TemplateItem(2006, "面膜", "😷", "开封后立即使用"),
                    TemplateItem(2007, "防晒霜", "☀️", "开封后12个月"),
                    TemplateItem(2008, "卸妆油", "🫧", "开封后12个月")
                )
            ),
            SubCategory(
                id = 202,
                name = "化妆品",
                items = listOf(
                    TemplateItem(2101, "口红", "💄", "开封后18-24个月"),
                    TemplateItem(2102, "粉底液", "🎨", "开封后12-18个月"),
                    TemplateItem(2103, "眼影", "👁️", "开封后24-36个月"),
                    TemplateItem(2104, "睫毛膏", "👁️", "⚠️ 开封后仅3-6个月"),
                    TemplateItem(2105, "眼线笔", "✏️", "⚠️ 开封后仅3-6个月"),
                    TemplateItem(2106, "腮红", "😊", "开封后18-24个月"),
                    TemplateItem(2107, "眉笔", "✏️", "开封后24-36个月"),
                    TemplateItem(2108, "指甲油", "💅", "开封后12-18个月")
                )
            ),
            SubCategory(
                id = 203,
                name = "洗护用品",
                items = listOf(
                    TemplateItem(2201, "洗发水", "🧴", "开封后12-18个月"),
                    TemplateItem(2202, "护发素", "🧴", "开封后12-18个月"),
                    TemplateItem(2203, "沐浴露", "🧴", "开封后12-18个月"),
                    TemplateItem(2204, "洗手液", "🧼", "开封后12个月"),
                    TemplateItem(2205, "牙膏", "🦷", "开封后12个月"),
                    TemplateItem(2206, "香皂", "🧼", "开封后12-18个月"),
                    TemplateItem(2207, "身体乳", "🧴", "开封后12-18个月"),
                    TemplateItem(2208, "剃须刀刀片", "🪒", "使用1-3个月更换")
                )
            ),
            SubCategory(
                id = 204,
                name = "口腔护理",
                items = listOf(
                    TemplateItem(2301, "牙刷", "🪥", "⚠️ 每3个月更换"),
                    TemplateItem(2302, "牙线", "🧵", "长期保存"),
                    TemplateItem(2303, "漱口水", "🫧", "开封后6-12个月"),
                    TemplateItem(2304, "牙套清洁片", "🦷", "见包装日期")
                )
            ),
            SubCategory(
                id = 205,
                name = "隐形眼镜",
                items = listOf(
                    TemplateItem(2401, "日抛隐形眼镜", "👁️", "当日使用"),
                    TemplateItem(2402, "周抛隐形眼镜", "👁️", "使用1周"),
                    TemplateItem(2403, "月抛隐形眼镜", "👁️", "使用1个月"),
                    TemplateItem(2404, "年抛隐形眼镜", "👁️", "使用1年"),
                    TemplateItem(2405, "护理液", "💧", "开封后3个月")
                )
            ),
            SubCategory(
                id = 206,
                name = "其他用品",
                items = listOf(
                    TemplateItem(2501, "毛巾", "🧺", "⚠️ 每3个月更换"),
                    TemplateItem(2502, "梳子", "🪮", "长期使用"),
                    TemplateItem(2503, "镜子", "🪞", "长期使用")
                )
            )
        )
    ),
    MainCategory(
        id = 3,
        name = "健康医疗",
        icon = Icons.Default.LocalHospital,
        iconColor = Color(0xFFE91E63),
        bgColor = Color(0xFFFCE4EC),
        description = "药品、保健、医疗",
        subCategories = listOf(
            SubCategory(
                id = 301,
                name = "药品类",
                items = listOf(
                    TemplateItem(3001, "处方药", "💊", "按医嘱使用"),
                    TemplateItem(3002, "感冒药", "💊", "开封后2-3年"),
                    TemplateItem(3003, "退烧药", "💊", "开封后3-5年"),
                    TemplateItem(3004, "消炎药", "💊", "开封后2-3年"),
                    TemplateItem(3005, "止痛药", "💊", "开封后3-5年"),
                    TemplateItem(3006, "外用药", "🩹", "开封后1-2年"),
                    TemplateItem(3007, "抗生素", "💊", "按疗程使用"),
                    TemplateItem(3008, "维生素", "💊", "开封后2年")
                )
            ),
            SubCategory(
                id = 302,
                name = "保健品",
                items = listOf(
                    TemplateItem(3101, "维生素片", "💊", "开封后1-2年"),
                    TemplateItem(3102, "钙片", "💊", "开封后2年"),
                    TemplateItem(3103, "鱼油", "🐟", "开封后1-2年"),
                    TemplateItem(3104, "蛋白粉", "🥛", "开封后6个月"),
                    TemplateItem(3105, "益生菌", "🦠", "开封后6-12个月"),
                    TemplateItem(3106, "阿胶", "🟤", "长期保存"),
                    TemplateItem(3107, "胶原蛋白", "✨", "开封后1-2年"),
                    TemplateItem(3108, "其他保健品", "💊", "见包装说明")
                )
            ),
            SubCategory(
                id = 303,
                name = "医疗用品",
                items = listOf(
                    TemplateItem(3201, "创可贴", "🩹", "有效期3年"),
                    TemplateItem(3202, "消毒水", "🧴", "开封后12个月"),
                    TemplateItem(3203, "酒精棉片", "🩹", "密封保存2年"),
                    TemplateItem(3204, "口罩", "😷", "未开封2-3年"),
                    TemplateItem(3205, "一次性手套", "🧤", "密封保存"),
                    TemplateItem(3206, "体温计", "🌡️", "长期使用"),
                    TemplateItem(3207, "血压计", "🩺", "长期使用"),
                    TemplateItem(3208, "血糖仪", "🩸", "长期使用")
                )
            ),
            SubCategory(
                id = 304,
                name = "医疗服务",
                items = listOf(
                    TemplateItem(3301, "疫苗接种", "💉", "按计划接种"),
                    TemplateItem(3302, "体检报告", "📋", "有效期内参考"),
                    TemplateItem(3303, "牙医预约", "🦷", "定期检查"),
                    TemplateItem(3304, "验光配镜", "👓", "每1-2年"),
                    TemplateItem(3305, "医保卡", "💳", "注意有效期"),
                    TemplateItem(3306, "病历本", "📕", "长期保存")
                )
            )
        )
    ),
    MainCategory(
        id = 4,
        name = "证件文件",
        icon = Icons.Default.Description,
        iconColor = Color(0xFF9C27B0),
        bgColor = Color(0xFFF3E5F5),
        description = "证件、合同、证书",
        subCategories = listOf(
            SubCategory(
                id = 401,
                name = "个人证件",
                items = listOf(
                    TemplateItem(4001, "身份证", "🆔", "⚠️ 有效期20年"),
                    TemplateItem(4002, "护照", "🛂", "⚠️ 有效期10年"),
                    TemplateItem(4003, "港澳通行证", "📖", "⚠️ 有效期10年"),
                    TemplateItem(4004, "台湾通行证", "📖", "⚠️ 有效期5年"),
                    TemplateItem(4005, "驾驶证", "🚗", "⚠️ 有效期6年/10年"),
                    TemplateItem(4006, "行驶证", "🚗", "随车辆"),
                    TemplateItem(4007, "居住证", "🏠", "⚠️ 有效期1年"),
                    TemplateItem(4008, "结婚证", "💍", "长期有效")
                )
            ),
            SubCategory(
                id = 402,
                name = "职业证件",
                items = listOf(
                    TemplateItem(4101, "学历证书", "🎓", "长期有效"),
                    TemplateItem(4102, "职业资格证", "📜", "⚠️ 注意有效期"),
                    TemplateItem(4103, "健康证", "🏥", "⚠️ 有效期1年"),
                    TemplateItem(4104, "工作证", "💼", "在职期间有效"),
                    TemplateItem(4105, "退休证", "👴", "长期有效")
                )
            ),
            SubCategory(
                id = 403,
                name = "法律文件",
                items = listOf(
                    TemplateItem(4201, "租房合同", "📄", "合同期限内"),
                    TemplateItem(4202, "购房合同", "🏠", "长期保存"),
                    TemplateItem(4203, "贷款合同", "📋", "还款期间"),
                    TemplateItem(4204, "劳动合同", "📝", "合同期限内"),
                    TemplateItem(4205, "保险单", "📄", "保险期间内")
                )
            ),
            SubCategory(
                id = 404,
                name = "企业证件",
                items = listOf(
                    TemplateItem(4301, "营业执照", "🏢", "⚠️ 定期年检"),
                    TemplateItem(4302, "税务登记证", "📊", "长期有效"),
                    TemplateItem(4303, "经营许可证", "📋", "⚠️ 注意有效期"),
                    TemplateItem(4304, "商标注册证", "®️", "⚠️ 有效期10年")
                )
            )
        )
    ),
    MainCategory(
        id = 5,
        name = "金融保险",
        icon = Icons.Default.AccountBalance,
        iconColor = Color(0xFF3F51B5),
        bgColor = Color(0xFFE8EAF6),
        description = "贷款、保险、理财",
        subCategories = listOf(
            SubCategory(
                id = 501,
                name = "信用卡贷款",
                items = listOf(
                    TemplateItem(5001, "信用卡还款日", "💳", "每月固定日期"),
                    TemplateItem(5002, "账单日", "📅", "每月固定日期"),
                    TemplateItem(5003, "信用卡有效期", "💳", "⚠️ 通常3-5年"),
                    TemplateItem(5004, "房贷还款日", "🏠", "每月固定日期"),
                    TemplateItem(5005, "车贷还款日", "🚗", "每月固定日期"),
                    TemplateItem(5006, "消费贷还款日", "💰", "按合同约定")
                )
            ),
            SubCategory(
                id = 502,
                name = "保险类",
                items = listOf(
                    TemplateItem(5101, "车险", "🚗", "⚠️ 每年续费"),
                    TemplateItem(5102, "寿险", "🛡️", "按保单约定"),
                    TemplateItem(5103, "医疗险", "🏥", "每年续费"),
                    TemplateItem(5104, "意外险", "⚡", "每年续费"),
                    TemplateItem(5105, "重疾险", "❤️", "长期保障"),
                    TemplateItem(5106, "宠物保险", "🐾", "每年续费")
                )
            ),
            SubCategory(
                id = 503,
                name = "理财类",
                items = listOf(
                    TemplateItem(5201, "定期存款", "🏦", "到期自动转存"),
                    TemplateItem(5202, "理财产品", "📈", "⚠️ 注意到期日"),
                    TemplateItem(5203, "基金", "💹", "可随时赎回"),
                    TemplateItem(5204, "国债", "📊", "到期还本付息")
                )
            ),
            SubCategory(
                id = 504,
                name = "生活缴费",
                items = listOf(
                    TemplateItem(5301, "水电燃气费", "💡", "每月缴纳"),
                    TemplateItem(5302, "物业费", "🏢", "按季度/年度"),
                    TemplateItem(5303, "电话费", "📱", "每月充值"),
                    TemplateItem(5304, "宽带费", "🌐", "按月/年缴费"),
                    TemplateItem(5305, "有线电视费", "📺", "月费/年费")
                )
            )
        )
    ),
    MainCategory(
        id = 6,
        name = "会员订阅",
        icon = Icons.Default.CardMembership,
        iconColor = Color(0xFF00BCD4),
        bgColor = Color(0xFFE0F7FA),
        description = "会员、订阅、服务",
        subCategories = listOf(
            SubCategory(
                id = 601,
                name = "娱乐会员",
                items = listOf(
                    TemplateItem(6001, "视频会员（腾讯）", "📺", "⚠️ 自动续费"),
                    TemplateItem(6002, "视频会员（爱奇艺）", "📺", "⚠️ 自动续费"),
                    TemplateItem(6003, "视频会员（优酷）", "📺", "⚠️ 自动续费"),
                    TemplateItem(6004, "音乐会员（QQ）", "🎵", "⚠️ 自动续费"),
                    TemplateItem(6005, "音乐会员（网易云）", "🎵", "⚠️ 自动续费"),
                    TemplateItem(6006, "游戏会员", "🎮", "⚠️ 自动续费")
                )
            ),
            SubCategory(
                id = 602,
                name = "云存储",
                items = listOf(
                    TemplateItem(6101, "百度网盘", "☁️", "⚠️ 自动续费"),
                    TemplateItem(6102, "阿里云盘", "☁️", "⚠️ 自动续费"),
                    TemplateItem(6103, "iCloud", "☁️", "⚠️ 自动续费"),
                    TemplateItem(6104, "OneDrive", "☁️", "⚠️ 自动续费")
                )
            ),
            SubCategory(
                id = 603,
                name = "软件订阅",
                items = listOf(
                    TemplateItem(6201, "Office 365", "📝", "⚠️ 年度订阅"),
                    TemplateItem(6202, "Adobe全家桶", "🎨", "⚠️ 月度/年度订阅"),
                    TemplateItem(6203, "设计软件", "🎯", "⚠️ 按需订阅"),
                    TemplateItem(6204, "效率工具", "⚡", "⚠️ 可能自动续费")
                )
            ),
            SubCategory(
                id = 604,
                name = "线下会员卡",
                items = listOf(
                    TemplateItem(6301, "健身房会员", "💪", "⚠️ 年卡/季卡"),
                    TemplateItem(6302, "瑜伽馆会员", "🧘", "⚠️ 按期付费"),
                    TemplateItem(6303, "美容院卡", "💆", "⚠️ 有限期使用"),
                    TemplateItem(6304, "洗车卡", "🚗", "⚠️ 次数/时间限制"),
                    TemplateItem(6305, "理发卡", "✂️", "⚠️ 有效期限制"),
                    TemplateItem(6306, "餐饮会员卡", "🍽️", "⚠️ 余额/期限")
                )
            )
        )
    ),
    MainCategory(
        id = 7,
        name = "数码设备",
        icon = Icons.Default.Computer,
        iconColor = Color(0xFF607D8B),
        bgColor = Color(0xFFECEFF1),
        description = "数码、电子、耗材",
        subCategories = listOf(
            SubCategory(
                id = 701,
                name = "保修期",
                items = listOf(
                    TemplateItem(7001, "手机保修", "📱", "通常1-2年"),
                    TemplateItem(7002, "电脑保修", "💻", "通常1-3年"),
                    TemplateItem(7003, "平板保修", "📱", "通常1-2年"),
                    TemplateItem(7004, "电视保修", "📺", "通常1-3年"),
                    TemplateItem(7005, "冰箱保修", "❄️", "通常1-3年"),
                    TemplateItem(7006, "洗衣机保修", "🌀", "通常1-3年"),
                    TemplateItem(7007, "空调保修", "❄️", "通常1-3年")
                )
            ),
            SubCategory(
                id = 702,
                name = "更换周期",
                items = listOf(
                    TemplateItem(7101, "手机电池", "🔋", "⚠️ 2-3年寿命"),
                    TemplateItem(7102, "笔记本电池", "💻", "⚠️ 2-3年寿命"),
                    TemplateItem(7103, "遥控器电池", "📺", "⚠️ 每年更换"),
                    TemplateItem(7104, "充电宝", "🔋", "⚠️ 2-3年寿命")
                )
            ),
            SubCategory(
                id = 703,
                name = "耗材类",
                items = listOf(
                    TemplateItem(7201, "打印机墨盒", "🖨️", "⚠️ 见墨量提示"),
                    TemplateItem(7202, "硒鼓", "🖨️", "⚠️ 见页数提示"),
                    TemplateItem(7203, "投影仪灯泡", "💡", "⚠️ 2000-5000小时"),
                    TemplateItem(7204, "相机电池", "📷", "⚠️ 充电循环次数"),
                    TemplateItem(7205, "存储卡", "💾", "长期使用")
                )
            ),
            SubCategory(
                id = 704,
                name = "网络服务",
                items = listOf(
                    TemplateItem(7301, "路由器更换", "📡", "⚠️ 3-5年更换"),
                    TemplateItem(7302, "硬盘备份", "💾", "⚠️ 定期备份"),
                    TemplateItem(7303, "域名到期", "🌐", "⚠️ 每年续费"),
                    TemplateItem(7304, "服务器续费", "🖥️", "⚠️ 按月/年付费")
                )
            )
        )
    ),
    MainCategory(
        id = 8,
        name = "家居维护",
        icon = Icons.Default.Home,
        iconColor = Color(0xFF795548),
        bgColor = Color(0xFFEFEBE9),
        description = "家电、滤芯、安全",
        subCategories = listOf(
            SubCategory(
                id = 801,
                name = "滤芯滤网",
                items = listOf(
                    TemplateItem(8001, "净水器滤芯", "💧", "PP棉3-6个月，RO膜1-2年"),
                    TemplateItem(8002, "空气净化器滤网", "🌬️", "HEPA 3-12个月"),
                    TemplateItem(8003, "空调滤网", "❄️", "⚠️ 每季度清洗"),
                    TemplateItem(8004, "加湿器滤网", "💨", "⚠️ 1-3个月更换")
                )
            ),
            SubCategory(
                id = 802,
                name = "家电维护",
                items = listOf(
                    TemplateItem(8101, "热水器镁棒", "🔧", "⚠️ 2-3年更换"),
                    TemplateItem(8102, "燃气灶电池", "🔋", "⚠️ 定期更换"),
                    TemplateItem(8103, "油烟机清洗", "🍳", "⚠️ 每半年1次"),
                    TemplateItem(8104, "洗衣机清洗", "🌀", "⚠️ 每半年1次")
                )
            ),
            SubCategory(
                id = 803,
                name = "安全用品",
                items = listOf(
                    TemplateItem(8201, "干粉灭火器", "🧯", "⚠️ 有效期5年"),
                    TemplateItem(8202, "烟雾报警器电池", "🔔", "⚠️ 每年更换"),
                    TemplateItem(8203, "燃气报警器电池", "🔔", "⚠️ 每年更换")
                )
            ),
            SubCategory(
                id = 804,
                name = "家居清洁",
                items = listOf(
                    TemplateItem(8301, "窗帘清洗", "🪟", "⚠️ 半年-1年"),
                    TemplateItem(8302, "地毯清洗", "🧹", "⚠️ 季度-半年"),
                    TemplateItem(8303, "沙发清洗", "🛋️", "⚠️ 半年-1年"),
                    TemplateItem(8304, "水管检查", "🔧", "⚠️ 年度检查"),
                    TemplateItem(8305, "电路检查", "⚡", "⚠️ 年度检查")
                )
            )
        )
    ),
    MainCategory(
        id = 9,
        name = "宠物相关",
        icon = Icons.Default.Pets,
        iconColor = Color(0xFFFF9800),
        bgColor = Color(0xFFFFF3E0),
        description = "宠物食品、用品、医疗",
        subCategories = listOf(
            SubCategory(
                id = 901,
                name = "宠物食品",
                items = listOf(
                    TemplateItem(9001, "狗粮", "🐕", "⚠️ 开封后1-2个月"),
                    TemplateItem(9002, "猫粮", "🐱", "⚠️ 开封后1-2个月"),
                    TemplateItem(9003, "零食", "🦴", "见包装日期"),
                    TemplateItem(9004, "罐头", "🥫", "⚠️ 开封后冷藏1-2天"),
                    TemplateItem(9005, "宠物奶粉", "🍼", "⚠️ 开封后1个月")
                )
            ),
            SubCategory(
                id = 902,
                name = "宠物用品",
                items = listOf(
                    TemplateItem(9101, "猫砂", "🐈", "⚠️ 定期更换"),
                    TemplateItem(9102, "狗尿垫", "🐕", "⚠️ 每日更换"),
                    TemplateItem(9103, "宠物沐浴露", "🧴", "开封后12-18个月"),
                    TemplateItem(9104, "宠物玩具", "🎾", "视磨损程度"),
                    TemplateItem(9105, "牵引绳", "🦮", "视使用情况")
                )
            ),
            SubCategory(
                id = 903,
                name = "医疗保健",
                items = listOf(
                    TemplateItem(9201, "疫苗接种", "💉", "⚠️ 每年接种"),
                    TemplateItem(9202, "体内外驱虫", "💊", "⚠️ 每月/季度"),
                    TemplateItem(9203, "体检", "🏥", "⚠️ 每年1次"),
                    TemplateItem(9204, "绝育手术", "🏥", "一次性")
                )
            ),
            SubCategory(
                id = 904,
                name = "其他服务",
                items = listOf(
                    TemplateItem(9301, "宠物保险", "🛡️", "⚠️ 每年续费"),
                    TemplateItem(9302, "宠物寄养", "🏠", "⚠️ 预约制"),
                    TemplateItem(9303, "宠物美容", "✂️", "⚠️ 定期预约")
                )
            )
        )
    ),
    MainCategory(
        id = 10,
        name = "出行旅游",
        icon = Icons.Default.Flight,
        iconColor = Color(0xFF03A9F4),
        bgColor = Color(0xFFE1F5FE),
        description = "交通、住宿、签证",
        subCategories = listOf(
            SubCategory(
                id = 1001,
                name = "交通票务",
                items = listOf(
                    TemplateItem(10001, "机票", "✈️", "⚠️ 注意改签/退票截止日"),
                    TemplateItem(10002, "火车票", "🚆", "⚠️ 开车前可退改"),
                    TemplateItem(10003, "汽车票", "🚌", "⚠️ 发车前可退"),
                    TemplateItem(10004, "船票", "🚢", "⚠️ 提前到达"),
                    TemplateItem(10005, "高铁票", "🚄", "⚠️ 改签规则")
                )
            ),
            SubCategory(
                id = 1002,
                name = "住宿预订",
                items = listOf(
                    TemplateItem(10101, "酒店预订", "🏨", "⚠️ 入住/取消政策"),
                    TemplateItem(10102, "民宿预订", "🏠", "⚠️ 确认入住时间"),
                    TemplateItem(10103, "度假村", "🏖️", "⚠️ 套餐有效期")
                )
            ),
            SubCategory(
                id = 1003,
                name = "证件签证",
                items = listOf(
                    TemplateItem(10201, "签证有效期", "🛂", "⚠️ 提前办理"),
                    TemplateItem(10202, "护照有效期", "🛂", "⚠️ 至少6个月以上"),
                    TemplateItem(10203, "国际驾照", "🚗", "⚠️ 目的地要求")
                )
            ),
            SubCategory(
                id = 1004,
                name = "其他出行",
                items = listOf(
                    TemplateItem(10301, "旅游保险", "🛡️", "⚠️ 行程期间有效"),
                    TemplateItem(10302, "景点门票", "🎫", "⚠️ 使用日期限制"),
                    TemplateItem(10303, "租车合同", "🚗", "⚠️ 归还日期")
                )
            )
        )
    ),
    MainCategory(
        id = 11,
        name = "工作学习",
        icon = Icons.Default.Work,
        iconColor = Color(0xFF607D8B),
        bgColor = Color(0xFFECEFF1),
        description = "工作事项、学习计划",
        subCategories = listOf(
            SubCategory(
                id = 1101,
                name = "工作事项",
                items = listOf(
                    TemplateItem(11001, "合同到期日", "📄", "⚠️ 提前准备续签"),
                    TemplateItem(11002, "项目截止日", "📋", "⚠️ 关注进度"),
                    TemplateItem(11003, "发票报销", "🧾", "⚠️ 报销期限"),
                    TemplateItem(11004, "年假到期", "🏖️", "⚠️ 及时安排休假")
                )
            ),
            SubCategory(
                id = 1102,
                name = "学习事项",
                items = listOf(
                    TemplateItem(11101, "考试报名", "📝", "⚠️ 截止日期"),
                    TemplateItem(11102, "考试日期", "📅", "⚠️ 提前复习"),
                    TemplateItem(11103, "课程截止", "📚", "⚠️ 完成进度"),
                    TemplateItem(11104, "论文截止", "📜", "⚠️ 提交日期")
                )
            ),
            SubCategory(
                id = 1103,
                name = "其他事项",
                items = listOf(
                    TemplateItem(11201, "图书馆借书", "📖", "⚠️ 归还日期"),
                    TemplateItem(11202, "会议时间", "📅", "⚠️ 准时参加"),
                    TemplateItem(11203, "培训截止", "🎓", "⚠️ 完成培训"),
                    TemplateItem(11204, "个税确认", "💰", "⚠️ 每年12月确认")
                )
            )
        )
    ),
    MainCategory(
        id = 12,
        name = "其他事项",
        icon = Icons.Default.MoreHoriz,
        iconColor = Color(0xFF9E9E9E),
        bgColor = Color(0xFFF5F5F5),
        description = "积分、卡券、生活",
        subCategories = listOf(
            SubCategory(
                id = 1201,
                name = "积分类",
                items = listOf(
                    TemplateItem(12001, "超市积分", "🛒", "⚠️ 注意有效期"),
                    TemplateItem(12002, "信用卡积分", "💳", "⚠️ 可能有失效期"),
                    TemplateItem(12003, "航空公司里程", "✈️", "⚠️ 部分会过期"),
                    TemplateItem(12004, "酒店积分", "🏨", "⚠️ 账户活跃度影响"),
                    TemplateItem(12005, "APP积分", "📱", "⚠️ 经常清理过期积分")
                )
            ),
            SubCategory(
                id = 1202,
                name = "卡券类",
                items = listOf(
                    TemplateItem(12101, "电商优惠券", "🎫", "⚠️ 过期作废"),
                    TemplateItem(12102, "实体店代金券", "💵", "⚠️ 使用期限"),
                    TemplateItem(12103, "红包", "🧧", "⚠️ 24小时内使用"),
                    TemplateItem(12104, "礼品卡", "🎁", "⚠️ 注意余额和期限"),
                    TemplateItem(12105, "购物卡", "💳", "⚠️ 部分有有效期"),
                    TemplateItem(12106, "加油卡", "⛽", "⚠️ 长期有效")
                )
            ),
            SubCategory(
                id = 1203,
                name = "生活类",
                items = listOf(
                    TemplateItem(12201, "鲜花保鲜", "🌸", "⚠️ 3-7天"),
                    TemplateItem(12202, "礼物赠送", "🎁", "⚠️ 特殊日期前"),
                    TemplateItem(12203, "快递取件", "📦", "⚠️ 保管期限3-7天"),
                    TemplateItem(12204, "干洗衣物", "👔", "⚠️ 取衣期限")
                )
            ),
            SubCategory(
                id = 1204,
                name = "特殊事项",
                items = listOf(
                    TemplateItem(12301, "车牌年检", "🚗", "⚠️ 每年/两年"),
                    TemplateItem(12302, "车辆保养", "🔧", "⚠️ 5000公里/半年"),
                    TemplateItem(12303, "驾照换证", "🚗", "⚠️ 有效期6年/10年"),
                    TemplateItem(12304, "身份证换证", "🆔", "⚠️ 有效期20年"),
                    TemplateItem(12305, "居住证续签", "🏠", "⚠️ 有效期1年")
                )
            )
        )
    )
)