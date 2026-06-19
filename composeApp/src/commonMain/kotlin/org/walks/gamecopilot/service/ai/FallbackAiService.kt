package org.walks.gamecopilot.service.ai

import kotlinx.coroutines.delay

/**
 * 本地预设 AI 服务（降级方案）
 * 当外部 AI 不可用时，使用本地预设台词提供游戏辅助
 */
class FallbackAiService : AiService {

    /** 谁是卧底场景的主持人台词 */
    private val spyAnswers = listOf(
        "各位选手请注意，卧底可能就在你身边！请用一句话描述你的词语，注意不要直接说出关键词哦~",
        "精彩的描述！不过我总觉得有人的描述有点「巧妙」，大家要仔细分辨~",
        "投票环节到了！请大家仔细思考，谁是那个与众不同的卧底？",
        "这一轮的发言真是跌宕起伏，有人描述得太过含糊，有人又太直白了~",
        "恭喜成功找出卧底！平民阵营获胜！卧底的伪装已经暴露无遗~",
        "哎呀，居然投错了！卧底的演技真是可圈可点~",
        "温馨提示：注意每个人的用词细节，卧底的词语和你们不一样哦~",
        "这一轮发言结束，我来做个总结：大多数人描述方向一致，但有一个人似乎在刻意回避~",
        "游戏继续！未被淘汰的玩家请再次描述词语，卧底请继续隐藏~",
        "最终对决！现在只剩下最后几位玩家，请慎重投票~",
        "有人描述得太完美了，反而让人怀疑，你说是吧？~",
        "本轮最佳演技奖颁发给……先卖个关子，大家猜猜看~"
    )

    /** 阿瓦隆场景的顾问台词 */
    private val awalongAnswers = listOf(
        "骑士们，今日的任务至关重要！请慎重选择你的队友~",
        "忠诚的骑士啊，任务的成败取决于你们的信任与判断~",
        "暗影中潜伏着间谍，请仔细观察每个人的投票倾向~",
        "有人投了反对票却没有公开反对，这其中必有蹊跷~",
        "任务成功了！但请保持警惕，间谍可能还在潜伏~",
        "任务失败了……看来队伍中混入了间谍，下次组队要更加小心~",
        "我建议观察之前任务中每个人的投票模式，真相往往隐藏在细节中~",
        "梅林的预言已给出暗示，聪明的骑士们应该心领神会~",
        "刺杀环节到了！刺客能否找到梅林，将决定最终胜负~",
        "蓝方胜利！正义终将战胜邪恶~",
        "红方获胜！间谍的伪装天衣无缝~",
        "作为顾问，我只能给出模糊的提示：注意那些投了奇怪票的人~"
    )

    /** 你画我猜场景的评论台词 */
    private val drawguessAnswers = listOf(
        "这幅画……我只能说是抽象派大师的杰作！画的是灵魂不是形状~",
        "猜对了！不过说实话，这幅画让我想到了另外十个词~",
        "画家的画风独特，我给8分，扣掉的2分是因为猜的人可能需要超能力~",
        "这是什么？一朵云？一只羊？还是……哦原来是个苹果！",
        "毕加索看了都要流泪，这不是贬义，是真的太有创意了~",
        "猜词时间到！让我看看有谁能从这个「神秘符号」中看出端倪~",
        "这画得也太抽象了吧，不过别急，最抽象的画往往是最简单的词~",
        "这位画家的笔触大胆奔放，色彩运用……等等，只有黑色？",
        "恭喜猜中！你们的默契程度堪比多年老友~",
        "这轮画作简直是天书，但居然有人猜到了！佩服佩服~",
        "画家请下笔！记住，你的画要让队友秒懂，但对手一头雾水~",
        "我从未见过如此清奇的画风，建议画家改行当考古学家~"
    )

    /** 狼人杀场景的旁白台词 */
    private val werewolfAnswers = listOf(
        "夜幕降临，请所有人闭上眼睛……狼人请睁眼，选择你的猎物~",
        "天亮了，昨晚是一个平安夜，所有人都安然无恙~",
        "天亮了……昨晚有人永远地离开了我们，请默哀~",
        "预言家已完成查验，请记住你的判断，在合适的时候揭露真相~",
        "女巫，你有一瓶解药和一瓶毒药，请在关键时刻使用~",
        "猎人倒下了！但你还有最后一枪，请带走你怀疑的人~",
        "讨论环节开始！请大家根据自己的信息进行推理，找出狼人~",
        "投票环节到了！请投出你心中怀疑的那个人~",
        "狼人已被全部消灭！好人阵营获胜，正义再次得到伸张~",
        "狼人的数量已经超过了好人……黑暗笼罩了村庄~",
        "守卫，今晚你守护了谁？你的选择可能拯救了一条生命~",
        "白天的时间有限，请大家抓紧时间发言，真相只有一个~"
    )

    /** 通用台词 */
    private val defaultAnswers = listOf(
        "游戏开始了！请大家做好准备，精彩的时刻即将来临~",
        "这个局面真是越来越有趣了，让我来为大家分析一下~",
        "提醒大家注意观察细节，线索就隐藏在身边~",
        "游戏进行到关键时刻，每一步都至关重要~",
        "好的策略需要好的执行，请大家慎重思考~",
        "局势变幻莫测，谁能笑到最后还未可知~",
        "别忘了，团队合作才是取胜的关键~",
        "游戏规则很简单，但赢得游戏可不容易~",
        "胜负乃兵家常事，享受过程才是最重要的~",
        "现在的局面可以用四个字来形容：扑朔迷离~"
    )

    /** 按游戏类型分组的预设回答 */
    private val presetAnswers: Map<String, List<String>> = mapOf(
        "spy" to spyAnswers,
        "awalong" to awalongAnswers,
        "drawguess" to drawguessAnswers,
        "werewolf" to werewolfAnswers,
        "default" to defaultAnswers
    )

    /**
     * 从 systemPrompt 中检测游戏类型关键词
     * @param systemPrompt 系统提示词
     * @return 游戏类型标识
     */
    private fun detectGameType(systemPrompt: String): String {
        val lowerPrompt = systemPrompt.lowercase()
        return when {
            lowerPrompt.contains("卧底") || lowerPrompt.contains("spy") -> "spy"
            lowerPrompt.contains("阿瓦隆") || lowerPrompt.contains("avalon") || lowerPrompt.contains(
                "awalong"
            ) -> "awalong"

            lowerPrompt.contains("画") || lowerPrompt.contains("猜") || lowerPrompt.contains("draw") -> "drawguess"
            lowerPrompt.contains("狼人") || lowerPrompt.contains("werewolf") -> "werewolf"
            else -> "default"
        }
    }

    override suspend fun chat(request: AiRequest): AiResponse {
        // 从 systemPrompt 中检测游戏类型
        val gameType = detectGameType(request.systemPrompt)
        val answers = presetAnswers[gameType] ?: presetAnswers["default"]!!
        val answer = answers.random()

        // 模拟短暂延迟，让体验更自然
        delay(300)

        return AiResponse(
            content = answer,
            isSuccess = true,
            provider = AiProvider.FALLBACK
        )
    }

    override fun isAvailable(): Boolean = true

    override fun getProviderName(): String = AiProvider.FALLBACK.displayName
}
