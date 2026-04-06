package com.moneytrace.service

import com.moneytrace.data.database.AppDatabase

/**
 * 智能分类引擎：根据商家名称自动判断消费类别
 */
class CategoryClassifier {

    // 关键词 → 分类名称 的映射
    private val keywordMap = mapOf(
        // 餐饮
        "餐饮美食" to listOf(
            "餐厅", "饭店", "外卖", "美食", "美团", "饿了么", "麦当劳", "肯德基", "汉堡王",
            "必胜客", "星巴克", "奶茶", "咖啡", "面馆", "火锅", "烧烤", "小吃", "早餐",
            "午餐", "晚餐", "蛋糕", "甜品", "饮品", "瑞幸", "奈雪", "喜茶", "沙县", "兰州"
        ),
        // 交通
        "交通出行" to listOf(
            "滴滴", "高铁", "地铁", "公交", "加油", "停车", "共享单车", "哈啰", "美团单车",
            "飞机", "航空", "机票", "火车", "出租", "摩拜", "青桔", "神州", "曹操", "T3",
            "高速", "过路费", "停车场", "加油站", "中石化", "中石油"
        ),
        // 购物
        "购物消费" to listOf(
            "淘宝", "天猫", "京东", "拼多多", "超市", "商场", "便利店", "沃尔玛", "家乐福",
            "永辉", "盒马", "物美", "大润发", "711", "全家", "罗森", "抖音", "快手",
            "唯品会", "苏宁", "国美", "宜家", "无印良品", "优衣库"
        ),
        // 娱乐
        "娱乐休闲" to listOf(
            "电影", "影院", "KTV", "游戏", "视频", "爱奇艺", "优酷", "腾讯视频", "哔哩哔哩",
            "网易音乐", "QQ音乐", "spotify", "netflix", "游乐园", "景区", "门票", "演出",
            "健身", "游泳", "网吧", "桌游", "密室", "剧本杀"
        ),
        // 医疗
        "医疗健康" to listOf(
            "药店", "医院", "诊所", "体检", "药房", "大药房", "医疗", "挂号", "问诊",
            "平安健康", "京东健康", "叮当快药"
        ),
        // 教育
        "教育学习" to listOf(
            "书店", "课程", "培训", "学费", "教育", "新东方", "好未来", "学而思",
            "知乎", "得到", "樊登", "教材", "文具", "打印"
        ),
        // 生活服务
        "生活服务" to listOf(
            "水费", "电费", "燃气", "物业", "话费", "宽带", "房租", "快递", "洗衣",
            "理发", "美容", "家政", "维修", "搬家", "中国移动", "中国联通", "中国电信"
        ),
        // 金融
        "金融理财" to listOf(
            "还款", "保险", "基金", "理财", "贷款", "花呗", "借呗", "信用卡", "转账"
        )
    )

    /**
     * 根据商家名称分类，返回数据库中对应的分类ID
     */
    suspend fun classify(merchant: String, amount: Double, db: AppDatabase): Long {
        val merchantLower = merchant.lowercase()

        // 关键词匹配
        for ((categoryName, keywords) in keywordMap) {
            if (keywords.any { merchantLower.contains(it.lowercase()) }) {
                // 从数据库查找对应分类ID
                val categories = mutableListOf<com.moneytrace.data.database.entity.CategoryEntity>()
                db.categoryDao().getCategoriesByType("EXPENSE").collect { list ->
                    categories.addAll(list)
                    return@collect
                }
                val matched = categories.find { it.name == categoryName }
                if (matched != null) return matched.id
            }
        }

        // 未匹配到，返回"其他支出"分类（通常是最后一个）
        val allExpense = mutableListOf<com.moneytrace.data.database.entity.CategoryEntity>()
        db.categoryDao().getCategoriesByType("EXPENSE").collect { list ->
            allExpense.addAll(list)
            return@collect
        }
        return allExpense.lastOrNull()?.id ?: 1L
    }
}
