package org.walks.gamecopilot

import org.walks.gamecopilot.data.wordsEasy
import org.walks.gamecopilot.data.wordsHard
import org.walks.gamecopilot.data.wordsMiddle


val wordMap by lazy {
    mutableMapOf(
        "牛奶" to "豆浆",
        "贵妃醉酒" to "黛玉葬花",
        "蝴蝶" to "蜜蜂",
        "汉堡包" to "肉夹馍",
        "水盆" to "水桶",
        "烤肉" to "涮肉",
        "南京" to "苏州",
        "太监" to "人妖",
        "蝴蝶" to "蜜蜂",
        "裸婚" to "闪婚",
        "吉他" to "琵琶",
        "公交" to "地铁",
        "警察" to "捕快",
        "男朋友" to "前男友",
    ).apply {
        putAll(wordsEasy)
        putAll(wordsMiddle)
        putAll(wordsHard)
    }
}
