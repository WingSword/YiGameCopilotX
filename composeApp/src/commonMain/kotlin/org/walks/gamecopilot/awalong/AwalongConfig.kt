package org.walks.gamecopilot.awalong

import androidx.compose.ui.semantics.Role
import org.walks.gamecopilot.ui.page.random.optimizedShuffle

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
    AOBOLUN(
        roleType = BAD_PERSON,
        title = "奥伯伦",
        description = "属于坏人阵营，但与其他坏人互相看不到，独立行动。他的存在增加了游戏的复杂性和不确定性，因为其他坏人和好人都不知道他的身份，他需要自己判断局势并采取行动来帮助坏人阵营获胜。"
    ),
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
                if (awalongRole.roleType == BAD_PERSON && awalongRole != AOBOLUN && awalongRole != this) {
                    map[index] = awalongRole
                }
            }
            return map
        }
        return mapOf()
    }

}