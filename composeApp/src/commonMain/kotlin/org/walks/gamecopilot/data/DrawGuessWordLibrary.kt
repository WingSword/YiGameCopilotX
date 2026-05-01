package org.walks.gamecopilot.data

import org.walks.gamecopilot.customDrawWordLists

object DrawGuessWordLibrary {
    val animals = listOf(
        "猫", "狗", "兔子", "老虎", "狮子", "大象", "长颈鹿", "熊猫", "企鹅", "海豚",
        "蝴蝶", "蜜蜂", "蚂蚁", "蜗牛", "乌龟", "鱼", "鲨鱼", "鲸鱼", "恐龙", "猴子"
    )

    val fruits = listOf(
        "苹果", "香蕉", "橙子", "葡萄", "西瓜", "草莓", "桃子", "梨", "樱桃", "芒果",
        "柠檬", "菠萝", "猕猴桃", "荔枝", "龙眼", "榴莲", "椰子", "石榴", "蓝莓", "火龙果"
    )

    val objects = listOf(
        "手机", "电脑", "电视", "冰箱", "洗衣机", "空调", "风扇", "台灯", "闹钟", "眼镜",
        "雨伞", "钥匙", "钱包", "背包", "鞋子", "帽子", "围巾", "手套", "手表", "镜子"
    )

    val food = listOf(
        "面条", "米饭", "饺子", "包子", "汉堡", "披萨", "蛋糕", "冰淇淋", "巧克力", "糖果",
        "爆米花", "薯条", "烤串", "火锅", "寿司", "三明治", "饼干", "面包", "油条", "豆浆"
    )

    val sports = listOf(
        "足球", "篮球", "乒乓球", "羽毛球", "网球", "排球", "游泳", "跑步", "跳绳", "滑板",
        "自行车", "滑雪", "登山", "瑜伽", "拳击", "举重", "射箭", "保龄球", "高尔夫", "棒球"
    )

    val vehicles = listOf(
        "汽车", "公交车", "地铁", "火车", "飞机", "轮船", "摩托车", "自行车", "电动车", "直升机",
        "坦克", "消防车", "救护车", "警车", "出租车", "卡车", "吊车", "热气球", "火箭", "帆船"
    )

    val buildings = listOf(
        "房子", "学校", "医院", "超市", "餐厅", "图书馆", "博物馆", "体育馆", "电影院", "酒店",
        "教堂", "城堡", "金字塔", "长城", "桥", "灯塔", "摩天大楼", "小木屋", "帐篷", "风车"
    )

    val weather = listOf(
        "晴天", "下雨", "下雪", "刮风", "打雷", "彩虹", "云朵", "月亮", "星星", "太阳",
        "雾", "冰雹", "龙卷风", "闪电", "霜", "露水", "沙尘暴", "台风", "阴天", "日出"
    )

    val actions = listOf(
        "跑步", "跳跃", "游泳", "跳舞", "唱歌", "画画", "写字", "看书", "睡觉", "吃饭",
        "喝水", "打篮球", "踢足球", "骑自行车", "滑冰", "爬山", "钓鱼", "做饭", "洗衣服", "扫地"
    )

    val professions = listOf(
        "医生", "老师", "警察", "消防员", "厨师", "司机", "飞行员", "农民", "画家", "音乐家",
        "科学家", "工程师", "程序员", "运动员", "演员", "歌手", "舞蹈家", "摄影师", "记者", "魔术师"
    )

    val allWords: List<String> by lazy {
        val builtin =
            animals + fruits + objects + food + sports + vehicles + buildings + weather + actions + professions
        val custom = customDrawWordLists.values.flatten()
        (builtin + custom).distinct()
    }

    val categories = mapOf(
        "动物" to animals,
        "水果" to fruits,
        "物品" to objects,
        "食物" to food,
        "运动" to sports,
        "交通" to vehicles,
        "建筑" to buildings,
        "天气" to weather,
        "动作" to actions,
        "职业" to professions
    )

    fun getRandomWord(): String {
        return allWords.random()
    }

    fun getRandomWords(count: Int): List<String> {
        return allWords.shuffled().take(count)
    }

    fun getWordsByCategory(category: String): List<String> {
        return categories[category] ?: emptyList()
    }

    fun getRandomWordFromCategory(category: String): String {
        return categories[category]?.random() ?: getRandomWord()
    }
}
