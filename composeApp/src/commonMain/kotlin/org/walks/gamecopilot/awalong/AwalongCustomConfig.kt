package org.walks.gamecopilot.awalong

/**
 * 自定义阿瓦隆游戏配置
 * 允许玩家自由选择蓝方/红方/中立方角色卡牌以及人数
 */
data class AwalongCustomConfig(
    // 蓝方角色配置
    val blueRoles: List<AwalongRole> = listOf(AwalongRole.MEILING),
    val blueCount: Int = 1,

    // 红方角色配置
    val redRoles: List<AwalongRole> = listOf(AwalongRole.MOGANNA),
    val redCount: Int = 1,

    // 中立方角色配置
    val neutralRoles: List<AwalongRole> = emptyList(),
    val neutralCount: Int = 0
) {
    // 总玩家数
    val totalPlayers: Int get() = blueCount + redCount + neutralCount

    // 游戏流程配置（根据玩家数自动计算）
    val process: List<Int> get() = calculateProcess(totalPlayers)

    /**
     * 生成完整的角色列表
     */
    fun generateRoleList(): List<AwalongRole> {
        val roles = mutableListOf<AwalongRole>()

        // 添加蓝方角色
        val blueRolesToAdd = if (blueRoles.size > blueCount) {
            blueRoles.take(blueCount)
        } else {
            blueRoles + List(blueCount - blueRoles.size) { AwalongRole.ZHONGCHEN }
        }
        roles.addAll(blueRolesToAdd)

        // 添加红方角色
        val redRolesToAdd = if (redRoles.size > redCount) {
            redRoles.take(redCount)
        } else {
            redRoles + List(redCount - redRoles.size) { AwalongRole.ZHONGCHEN }
        }
        roles.addAll(redRolesToAdd)

        // 添加中立方角色
        val neutralRolesToAdd = if (neutralRoles.size > neutralCount) {
            neutralRoles.take(neutralCount)
        } else {
            neutralRoles + List(neutralCount - neutralRoles.size) { AwalongRole.ZHONGCHEN }
        }
        roles.addAll(neutralRolesToAdd)

        // 如果角色数量不足总玩家数，补充忠臣
        val remainingPlayers = totalPlayers - roles.size
        if (remainingPlayers > 0) {
            repeat(remainingPlayers) {
                roles.add(AwalongRole.ZHONGCHEN)
            }
        }

        return roles.take(totalPlayers)
    }

    /**
     * 验证配置是否有效
     */
    fun isValid(): Boolean {
        // 必须包含梅林
        if (!blueRoles.contains(AwalongRole.MEILING)) {
            return false
        }

        // 必须包含莫甘娜
        if (!redRoles.contains(AwalongRole.MOGANNA)) {
            return false
        }

        // 总玩家数必须在5-10人之间
        if (totalPlayers < 5 || totalPlayers > 10) {
            return false
        }

        // 蓝方人数必须至少为3人
        if (blueCount < 3) {
            return false
        }

        // 红方人数必须至少为2人
        if (redCount < 2) {
            return false
        }

        return true
    }

    /**
     * 获取配置描述
     */
    fun getDescription(): String {
        return "蓝方${blueCount}人（${blueRoles.joinToString("、") { it.title }}） + " +
                "红方${redCount}人（${redRoles.joinToString("、") { it.title }}）" +
                if (neutralCount > 0) " + 中立方${neutralCount}人（${neutralRoles.joinToString("、") { it.title }}）" else ""
    }
}

/**
 * 根据玩家数计算游戏流程
 */
private fun calculateProcess(totalPlayers: Int): List<Int> {
    return when (totalPlayers) {
        5 -> listOf(2, 3, 2, 3) // 5人游戏：4轮任务
        6 -> listOf(2, 3, 4, 3, 4) // 6人游戏：5轮任务
        7 -> listOf(2, 3, 3, 4, 4) // 7人游戏：5轮任务
        8 -> listOf(3, 4, 4, 5, 5) // 8人游戏：5轮任务
        9 -> listOf(3, 4, 4, 5, 5) // 9人游戏：5轮任务
        10 -> listOf(3, 4, 4, 5, 5) // 10人游戏：5轮任务
        else -> listOf(2, 3, 2, 3) // 默认5人配置
    }
}

/**
 * 默认配置
 */
val DefaultCustomConfig = AwalongCustomConfig(
    blueRoles = listOf(AwalongRole.MEILING, AwalongRole.PAIXIWEIWEIER, AwalongRole.ZHONGCHEN),
    blueCount = 3,
    redRoles = listOf(AwalongRole.MOGANNA, AwalongRole.CISHA),
    redCount = 2
)

/**
 * 预定义的一些常用配置
 */
val PredefinedConfigs = listOf(
    // 5人标准配置
    AwalongCustomConfig(
        blueRoles = listOf(AwalongRole.MEILING, AwalongRole.PAIXIWEIWEIER, AwalongRole.ZHONGCHEN),
        blueCount = 3,
        redRoles = listOf(AwalongRole.MOGANNA, AwalongRole.CISHA),
        redCount = 2
    ),

    // 6人配置（带圆桌骑士）
    AwalongCustomConfig(
        blueRoles = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.SIR_GALAHAD,
            AwalongRole.ZHONGCHEN
        ),
        blueCount = 4,
        redRoles = listOf(AwalongRole.MOGANNA, AwalongRole.CISHA),
        redCount = 2
    ),

    // 7人配置（带预言者）
    AwalongCustomConfig(
        blueRoles = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.PROPHET,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN
        ),
        blueCount = 5,
        redRoles = listOf(AwalongRole.MOGANNA, AwalongRole.CISHA),
        redCount = 2
    ),

    // 8人配置（带变形者）
    AwalongCustomConfig(
        blueRoles = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.PROPHET,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN
        ),
        blueCount = 6,
        redRoles = listOf(AwalongRole.MOGANNA, AwalongRole.CISHA, AwalongRole.SHAPESHIFTER),
        redCount = 2
    ),

    // 9人配置（带多个扩展角色）
    AwalongCustomConfig(
        blueRoles = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.PROPHET,
            AwalongRole.SIR_GALAHAD,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN
        ),
        blueCount = 6,
        redRoles = listOf(AwalongRole.MOGANNA, AwalongRole.CISHA, AwalongRole.MODELEDE),
        redCount = 3
    )
)
