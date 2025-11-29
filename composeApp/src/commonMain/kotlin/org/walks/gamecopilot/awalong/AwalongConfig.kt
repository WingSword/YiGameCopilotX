package org.walks.gamecopilot.awalong

/**
 *  Created by Wing at 16:06 on 2025/5/19
 *
 */
enum class AwalongConfig(
    val title: String,
    val playerNum: Int = 5,
    val role: List<AwalongRole> = listOf(
        AwalongRole.MEILING,
        AwalongRole.PAIXIWEIWEIER,
        AwalongRole.ZHONGCHEN,
        AwalongRole.MOGANNA,
        AwalongRole.CISHA,
    ),
    val process: List<Int> = listOf(2, 3, 2, 3),
    val description: String = ""

) {

    Standard_5(
        title = "5人标准",
        playerNum = 5,
        role = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.ZHONGCHEN,
            AwalongRole.MOGANNA,
            AwalongRole.CISHA,
        ),
        process = listOf(2, 3, 2, 3),
        description = "" +
                "1.角色特性：\n" +
                "梅林(S)：可看到除 莫德雷德 外的所有坏人（即能识别莫甘娜、刺客、爪牙）。\n" +
                "派西维尔（S）：能看到梅林和莫甘娜，但无法区分两人身份（需通过发言判断谁是真梅林）。\n" +
                "忠臣(S)：没有特殊能力，主要职责是在投票过程中认真思考，支持团队的决策，通过观察和推理帮助团队找出坏人，保护梅林。\n" +
                "刺客（E）：好人完成 3 次任务后，可单独选择刺杀 1 人，若选中梅林则坏人胜。\n" +
                "莫甘娜(E)：可以冒充梅林的身份，迷惑派西维尔和其他好人，让好人难以判断梅林的真实身份，从而干扰好人阵营的决策。\n" +
                "2.任务投票：每轮任务需队长组队，全员投票是否通过组队，超过半数同意则组队成功，否则由下一位玩家担任队长重新组队，若反对车队的次数达到上限，则会进入强制轮，强制轮队长选择的人员将强制出征。\n" +
                "3.任务失败条件：每轮任务中 1 张坏票即失败；" +
                "4.胜利条件：\n" +
                "【S】：3 次任务成功且梅林未被刺杀\n" +
                "【E】：2 次任务失败或刺杀梅林成功"

    ),
    Standard_6(
        title = "6人标准",
        playerNum = 6,
        role = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.ZHONGCHEN,
            AwalongRole.MOGANNA,
            AwalongRole.ZHONGCHEN,
            AwalongRole.CISHA,
        ),
        process = listOf(2, 3, 4, 3, 4)
    ),

    // 扩展包配置
    Standard_5_Extension(
        title = "5人扩展",
        playerNum = 5,
        role = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.ZHONGCHEN,
            AwalongRole.MOGANNA,
            AwalongRole.CISHA,
        ),
        process = listOf(2, 3, 2, 3, 3),
        description = "5人扩展场：蓝方(梅林+派西维尔+忠臣) vs 红方(莫德雷德+莫甘娜+刺客)，可选湖中仙女替换忠臣"
    ),

    Standard_6_Extension(
        title = "6人扩展",
        playerNum = 6,
        role = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.ZHONGCHEN,
            AwalongRole.MOGANNA,
            AwalongRole.CISHA,
            AwalongRole.LADY_OF_LAKE,
        ),
        process = listOf(2, 3, 4, 3, 4),
        description = "6人扩展场：蓝方(梅林+派西维尔+忠臣+湖中仙女) vs 红方(莫德雷德+莫甘娜+刺客)"
    ),

    Standard_7(
        title = "7人标准",
        playerNum = 7,
        role = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN,
            AwalongRole.PROPHET,
            AwalongRole.MOGANNA,
            AwalongRole.CISHA,
        ),
        process = listOf(2, 3, 3, 4, 4),
        description = "7人标准场：蓝方(梅林+派西维尔+2忠臣+预言者) vs 红方(莫德雷德+莫甘娜+刺客)"
    ),

    Standard_7_Extension(
        title = "7人扩展",
        playerNum = 7,
        role = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN,
            AwalongRole.PROPHET,
            AwalongRole.MOGANNA,
            AwalongRole.CISHA,
        ),
        process = listOf(2, 3, 3, 4, 4),
        description = "7人扩展场：蓝方(梅林+派西维尔+2忠臣+预言者/湖中仙女) vs 红方(莫德雷德+莫甘娜+刺客)，可选奥伯伦"
    ),

    Standard_8(
        title = "8人标准",
        playerNum = 8,
        role = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN,
            AwalongRole.PROPHET,
            AwalongRole.MOGANNA,
            AwalongRole.CISHA,
            AwalongRole.ZHONGCHEN,
        ),
        process = listOf(3, 4, 4, 5, 5),
        description = "8人标准场：蓝方(梅林+派西维尔+3忠臣+预言者) vs 红方(莫德雷德+莫甘娜+刺客+忠臣)"
    ),

    Standard_8_Extension(
        title = "8人扩展",
        playerNum = 8,
        role = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN,
            AwalongRole.PROPHET,
            AwalongRole.MOGANNA,
            AwalongRole.CISHA,
            AwalongRole.SHAPESHIFTER,
        ),
        process = listOf(3, 4, 4, 5, 5),
        description = "8人扩展场：蓝方(梅林+派西维尔+3忠臣+预言者) vs 红方(莫德雷德+莫甘娜+刺客+变形者)，可选奥伯伦"
    ),

    Standard_9(
        title = "9人高级",
        playerNum = 9,
        role = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN,
            AwalongRole.PROPHET,
            AwalongRole.LADY_OF_LAKE,
            AwalongRole.MOGANNA,
            AwalongRole.CISHA,
        ),
        process = listOf(3, 4, 4, 5, 5),
        description = "9人高级场：蓝方(梅林+派西维尔+3忠臣+预言者+湖中仙女) vs 红方(莫德雷德+莫甘娜+刺客+红方士兵)"
    ),

    Standard_10(
        title = "10人高级",
        playerNum = 10,
        role = listOf(
            AwalongRole.MEILING,
            AwalongRole.PAIXIWEIWEIER,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN,
            AwalongRole.ZHONGCHEN,
            AwalongRole.PROPHET,
            AwalongRole.LADY_OF_LAKE,
            AwalongRole.MOGANNA,
            AwalongRole.CISHA,
            AwalongRole.SHAPESHIFTER,
        ),
        process = listOf(3, 4, 4, 5, 5),
        description = "10人高级场：蓝方(梅林+派西维尔+3忠臣+预言者+湖中仙女) vs 红方(莫德雷德+莫甘娜+刺客+红方士兵+变形者)，奥伯伦独立阵营"
    );


}

const val GOOD_PERSON = 1
const val BAD_PERSON = -1
const val NEUTRAL_PERSON = 0

enum class AwalongRole(
    val title: String,
    val description: String,
    val roleType: Int = GOOD_PERSON,
    var no: Int = 0
) {
    MEILING(
        title = "梅林",
        description = "能看到除莫德雷德外的所有坏人（即能识别莫甘娜、刺客、爪牙），需在不暴露自己身份的前提下，巧妙地为好人阵营提供信息，引导团队做出正确决策。\n"
    ) {
        override fun checkSkills(
            role: List<AwalongRole>,
        ): Map<Int, AwalongRole> {
            val map = mutableMapOf<Int, AwalongRole>()
            role.forEachIndexed { index, awalongRole ->
                if (awalongRole.roleType == BAD_PERSON && awalongRole != MODELEDE) {
                    map[index] = awalongRole
                }
            }
            return map
        }
    },

    PAIXIWEIWEIER(
        title = "派希维尔",
        description = "能看到梅林和莫甘娜，但无法区分两人身份，需要通过观察和分析两人的言行来判断谁是真正的梅林，从而保护梅林并带领好人阵营走向胜利。"
    ) {
        override fun checkSkills(
            role: List<AwalongRole>,
        ): Map<Int, AwalongRole> {
            val map = mutableMapOf<Int, AwalongRole>()
            role.forEachIndexed { index, awalongRole ->
                if (awalongRole == MEILING || awalongRole == MOGANNA) {
                    map[index] = awalongRole
                }
            }
            return map
        }
    },

    ZHONGCHEN(
        title = "忠臣",
        description = "没有特殊能力，主要职责是在投票过程中认真思考，支持团队的决策，通过观察和推理帮助团队找出坏人，保护梅林。"
    ),

    MOGANNA(
        roleType = BAD_PERSON,
        title = "莫甘娜",
        description = "可以冒充梅林的身份，迷惑派西维尔和其他好人，让好人难以判断梅林的真实身份，从而干扰好人阵营的决策。"
    ),
    MODELEDE(
        roleType = BAD_PERSON,
        title = "莫德雷德",
        description = "是混在好人堆里的坏人，梅林无法看到他，增加了梅林识别坏人的难度，也让坏人阵营有更多机会隐藏身份、破坏任务。"
    ),
    CISHA(
        roleType = BAD_PERSON,
        title = "刺客",
        description = "在好人阵营完成 3 次任务后，刺客可以单独选择刺杀一名玩家。如果选中梅林，则坏人阵营获胜；如果选错，则好人阵营获胜。"
    ),
    // 扩展包新角色 - 蓝方
    PROPHET(
        title = "预言者",
        description = "游戏开始时，可查看任意2名玩家的阵营（不分具体角色），无法查看莫德雷德，且获知的信息仅限于'好人/坏人'。"
    ) {
        override fun checkSkills(
            role: List<AwalongRole>,
        ): Map<Int, AwalongRole> {
            // 预言者能力：查看2名玩家阵营（游戏逻辑中实现）
            return mapOf()
        }
    },

    LADY_OF_LAKE(
        title = "湖中仙女",
        description = "在第2个任务完成后激活，可选择一名玩家秘密查看其阵营（好人/坏人），只能使用一次，且无法查看莫德雷德。"
    ),

    SIR_GALAHAD(
        title = "圆桌骑士",
        description = "在任意一轮投票中，可强制使自己的投票权重翻倍（相当于两票），整场游戏只能使用一次。"
    ),

    // 扩展包新角色 - 红方
    MORGUSE(
        roleType = BAD_PERSON,
        title = "莫高斯",
        description = "在任意一个任务中，可将1张成功卡变为失败卡（无论自己是否在队伍中），整场游戏只能使用一次，且无法在需要2张失败卡的任务中单独使用。"
    ),

    SHAPESHIFTER(
        roleType = BAD_PERSON,
        title = "变形者",
        description = "在游戏开始时，复制一名随机玩家的角色（获得相同能力，但阵营不变），无法复制莫德雷德，且复制后无法改变。"
    ) {
        override fun checkSkills(
            role: List<AwalongRole>,
        ): Map<Int, AwalongRole> {
            // 变形者可以看到其他坏人（除了奥伯伦）
            val map = mutableMapOf<Int, AwalongRole>()
            role.forEachIndexed { index, awalongRole ->
                if (awalongRole.roleType == BAD_PERSON && awalongRole != AOBOLUN && awalongRole != this) {
                    map[index] = awalongRole
                }
            }
            return map
        }
    },

    AOBOLUN(
        roleType = NEUTRAL_PERSON,
        title = "奥伯伦",
        description = "完全独立阵营，不属于蓝方也不属于红方，看不到其他任何阵营成员，其他阵营成员也看不到他，需单独胜利：至少破坏2个任务、在蓝方达成3胜后成功刺杀梅林、自己未被揭露身份。"
    ) {
        override fun checkSkills(
            role: List<AwalongRole>,
        ): Map<Int, AwalongRole> {
            // 奥伯伦看不到任何人，也看不到其他坏人
            return mapOf()
        }
    },

    // 特殊角色
    LANCELOT(
        roleType = GOOD_PERSON, // 初始为蓝方
        title = "兰斯洛特",
        description = "双面角色，初始阵营为蓝方，游戏中可通过特殊事件（如抽中'阵营转换卡'）转为红方，胜利条件随当前阵营变化。"
    ) {
        override fun checkSkills(
            role: List<AwalongRole>,
        ): Map<Int, AwalongRole> {
            // 兰斯洛特根据当前阵营决定能看到谁
            val map = mutableMapOf<Int, AwalongRole>()
            role.forEachIndexed { index, awalongRole ->
                // 如果还是蓝方，看不到坏人；如果已转换，可以看到坏人（除了莫德雷德和奥伯伦）
                // 这里简化处理，实际游戏中需要根据lancolotConverted状态判断
                if (awalongRole.roleType == BAD_PERSON && awalongRole != MODELEDE && awalongRole != AOBOLUN) {
                    // 暂时不返回，因为初始是蓝方
                }
            }
            return mapOf()
        }
    },

    EMPTY_ROLE(
        title = "未知",
        description = "未知"
    )
    ;


    open fun checkSkills(
        role: List<AwalongRole>,
    ): Map<Int, AwalongRole> {
        if (this.roleType == BAD_PERSON && this != AOBOLUN) {
            val map = mutableMapOf<Int, AwalongRole>()
            role.forEachIndexed { index, awalongRole ->
                // 坏人可以看到其他坏人，但看不到奥伯伦和自己
                if (awalongRole.roleType == BAD_PERSON && awalongRole != AOBOLUN && awalongRole != this) {
                    map[index] = awalongRole
                }
            }
            return map
        }
        return mapOf()
    }

}