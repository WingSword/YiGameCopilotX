package org.walks.gamecopilot

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 *  Created by Wing at 10:26 on 2025/5/7
 *  快速游戏模式中的随机数
 */
class QuickRandom {

    // 取当前时间戳（毫秒），然后舍去小于一小时的部分
    @OptIn(ExperimentalTime::class)
    val timeNow by lazy {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        // 每小时的毫秒数为：3600000
        (nowMs / 3600000L) * 3600000L
    }

    val playerNumber = 0
    val totalPlayer = 0
    val badNumber = 0
    val playTime = 0
    fun getSeed(): String {
        return "" + timeNow + totalPlayer + badNumber + playerNumber + playTime + ""
    }

    fun optNumber(seed: Long) {
        // 创建随机数生成器
        val randomGenerator = Random(seed)
        // 生成多个随机数，方便对比验证重复性
        println("随机浮点数 (0.0~1.0): ${randomGenerator.nextDouble()}")
        println("随机整数 (0~99): ${randomGenerator.nextInt(100)}")
        // 如果需要生成更多随机数，可以继续调用 randomGenerator.nextXXX() 方法
        println("随机整数 (0~999): ${randomGenerator.nextInt(1000)}")
    }


}