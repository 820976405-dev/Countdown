# 到期提醒 APP 产品需求文档（PRD V1.1）

## 项目名称

到期提醒（Expire Reminder）

---

# 一、产品定位

帮助用户管理食品、药品、护肤品、证件、会员、保险等物品或事项的到期时间，并通过通知及桌面组件进行提醒。

核心功能：

* 添加物品
* 分类管理
* 到期提醒
* 搜索查询
* 数据统计
* 桌面小组件（Widget）

---

# 二、技术架构

## 开发技术

* Kotlin
* Jetpack Compose
* Material Design 3
* MVVM
* Room Database
* WorkManager
* Navigation Compose
* DataStore
* Hilt（依赖注入）
* Coil（图片加载）

支持：

* Android 8.0+
* 深色模式
* 本地离线运行

---

# 三、UI设计风格

## 风格关键词

* 极简
* 卡片化
* 白色背景
* 大圆角
* Material You
* 清新绿色主题

## 主题颜色

| 名称 | 色值 |
|------|------|
| Primary | #5FCF80 |
| Primary Dark | #43B86A |
| Danger | #FF4D4F |
| Warning | #FF9500 |
| Background | #F7F8FA |
| Card | #FFFFFF |
| Text | #1F2329 |
| Secondary Text | #86909C |

---

# 四、页面结构

## 底部导航

| Tab | 页面 |
|-----|------|
| 首页 | HomeScreen |
| 添加 | AddScreen |
| 我的 | ProfileScreen |

---

# 五、首页

## 顶部

* 标题：到期提醒
* 右侧：通知按钮

## 即将到期统计卡

显示：

* 即将到期数量
* 未来3~5天到期物品数

## 即将到期列表

条件：剩余天数 <= 7天

显示：

* 图片
* 名称
* 到期日期
* 剩余天数

颜色规则：

* ≤3天 红色
* ≤7天 橙色
* > 7天 绿色
* 已过期 灰色

## 全部物品

排序：按剩余天数升序

---

# 六、添加物品

## 字段

| 字段 | 说明 |
|------|------|
| 图片 | 支持相机拍照、相册选择 |
| 物品名称 | 文本输入 |
| 分类 | 分类选择 |
| 到期日期 | Material Date Picker |
| 购买日期 | Material Date Picker |
| 数量 | 数字输入 |
| 单位 | 文本输入 |
| 存放位置 | 文本输入 |
| 备注 | 文本输入 |
| 提醒规则 | 自定义提醒天数 |

## 图片上传

支持：

* 相机拍照
* 相册选择

## 日期选择

Material Date Picker

## 保存

* 保存至 Room 数据库
* 自动创建提醒任务

---

# 七、分类管理

一级分类：

* 食品厨房
* 个人护理
* 健康医疗
* 证件文件
* 金融保险
* 会员订阅
* 数码设备
* 家居维护
* 宠物用品
* 出行旅游
* 工作学习
* 其他

支持：

* 自定义分类
* 自定义图标

---

# 八、搜索

支持搜索：

* 名称
* 分类
* 备注

Tab：

* 全部
* 物品
* 事项
* 分类

---

# 九、物品详情

显示：

* 图片
* 名称
* 分类
* 到期日期
* 剩余天数
* 购买日期
* 数量
* 存放位置
* 备注

操作：

* 编辑
* 删除

---

# 十、编辑物品

支持修改：

* 所有字段
* 图片
* 分类
* 提醒规则

---

# 十一、我的页面

## 用户信息卡

* 头像
* 昵称
* 欢迎语

## 数据统计

显示：

* 总物品数
* 临期物品数
* 已过期数

## 功能菜单

* 分类管理
* 提醒设置
* 数据统计
* 帮助反馈
* 关于我们

---

# 十二、通知系统

使用：WorkManager

## 默认提醒规则

| 分类 | 提前提醒天数 |
|------|-------------|
| 食品 | 提前3天、提前1天、当天 |
| 药品 | 提前7天、提前3天、提前1天、当天 |
| 证件 | 提前90天、提前30天、提前7天 |
| 会员 | 提前7天、提前3天、当天 |
| 保险 | 提前30天、提前7天、提前1天 |

---

# 十三、桌面小组件（Widget）

技术实现：Glance App Widget，兼容 Android 8+

## Widget 1：即将到期组件（推荐）

* 尺寸：4×2
* 显示：即将到期 3项（未来3~5天到期）
* 示例：牛奶 3天、鸡蛋 4天、酸奶 5天
* 点击：跳转首页

## Widget 2：今日提醒组件

* 尺寸：2×2
* 显示：今日到期 2项
* 点击：跳转临期列表

## Widget 3：统计组件

* 尺寸：4×2
* 显示：总物品 / 临期 / 过期
* 示例：28 / 5 / 2
* 点击：跳转我的页面

## Widget 4：快捷添加组件

* 尺寸：2×1
* 显示：➕ 快速添加
* 点击：直接打开添加页面

## Widget 交互要求

支持自动刷新：

* 每天00:00
* 新增物品
* 编辑物品
* 删除物品

刷新方式：WorkManager + Widget Update

---

# 十四、数据库设计

## Item 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 物品名称 |
| categoryId | Long | 分类ID |
| imageUri | String? | 图片URI |
| expireDate | Long | 到期日期（时间戳） |
| purchaseDate | Long? | 购买日期（时间戳） |
| quantity | Int | 数量 |
| unit | String | 单位 |
| location | String | 存放位置 |
| note | String | 备注 |
| remindDays | String | 提醒规则（JSON） |
| createdAt | Long | 创建时间 |
| updatedAt | Long | 更新时间 |

## Category 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 分类名称 |
| icon | String | 图标 |
| parentId | Long? | 父分类ID |

---

# 十五、统计模块

统计内容：

* 总物品数
* 即将到期数量
* 已过期数量
* 分类占比
* 最近新增物品
* 最近到期物品

支持图表展示：

* 饼图
* 柱状图

---

# 十六、动画规范

| 场景 | 规范 |
|------|------|
| 页面切换 | 300ms |
| 按钮 | Ripple |
| 卡片 | Scale 0.98 |

---

# 十七、项目目录结构

```
app
├─ data
│  ├─ dao
│  ├─ entity
│  ├─ repository
│  └─ database
├─ domain
├─ ui
│  ├─ home
│  ├─ add
│  ├─ detail
│  ├─ edit
│  ├─ category
│  ├─ search
│  ├─ profile
│  └─ statistics
├─ widget
│  ├─ ExpireWidget
│  ├─ TodayWidget
│  ├─ StatisticsWidget
│  └─ QuickAddWidget
├─ worker
├─ notification
├─ navigation
├─ di
└─ utils
```

---

# 十八、Trae生成指令

请使用 Kotlin + Jetpack Compose + Material3 开发完整安卓应用《到期提醒》。

要求：

* MVVM
* Room
* WorkManager
* Navigation Compose
* Hilt
* Coil
* DataStore
* Material 3
* 深色模式
* Widget（Glance）

实现：

1. 首页
2. 添加物品
3. 分类管理
4. 搜索
5. 详情页
6. 编辑页
7. 我的页面
8. 数据统计
9. 通知提醒
10. 桌面小组件

请生成完整 Android Studio 工程源码，包括：

* Entity
* Dao
* Room
* Repository
* ViewModel
* UI
* Navigation
* Widget
* Notification
* WorkManager
* Hilt
* Material3 Theme

项目可直接编译运行。
