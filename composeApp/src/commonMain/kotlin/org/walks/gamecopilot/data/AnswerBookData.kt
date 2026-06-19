package org.walks.gamecopilot.data

import kotlin.random.Random

/**
 * 答案类别
 */
enum class AnswerCategory {
    POSITIVE,   // 积极
    NEUTRAL,    // 中性
    NEGATIVE    // 消极
}

/**
 * 单条答案
 */
data class AnswerBookEntry(
    val text: String,
    val category: AnswerCategory
)

/**
 * 答案之书数据集
 * 包含 105 条中文答案，正面/中性/负面各约 1/3
 */
object AnswerBookData {

    val answers: List<AnswerBookEntry> = listOf(
        // ==================== 正面答案（35 条）====================
        AnswerBookEntry("毫无疑问，是的！", AnswerCategory.POSITIVE),
        AnswerBookEntry("命运已经为你铺好了路", AnswerCategory.POSITIVE),
        AnswerBookEntry("一切皆有可能", AnswerCategory.POSITIVE),
        AnswerBookEntry("勇敢去做吧，未来可期", AnswerCategory.POSITIVE),
        AnswerBookEntry("星光不负赶路人", AnswerCategory.POSITIVE),
        AnswerBookEntry("答案就在你心中", AnswerCategory.POSITIVE),
        AnswerBookEntry("机不可失，时不再来", AnswerCategory.POSITIVE),
        AnswerBookEntry("你的直觉是对的", AnswerCategory.POSITIVE),
        AnswerBookEntry("大胆前行，前路光明", AnswerCategory.POSITIVE),
        AnswerBookEntry("这绝对是个好主意", AnswerCategory.POSITIVE),
        AnswerBookEntry("相信自己的选择", AnswerCategory.POSITIVE),
        AnswerBookEntry("万事俱备，只欠东风", AnswerCategory.POSITIVE),
        AnswerBookEntry("宇宙正在为你安排最好的", AnswerCategory.POSITIVE),
        AnswerBookEntry("坚持下去，胜利属于你", AnswerCategory.POSITIVE),
        AnswerBookEntry("前方有一段美妙的旅程", AnswerCategory.POSITIVE),
        AnswerBookEntry("你值得拥有最好的", AnswerCategory.POSITIVE),
        AnswerBookEntry("今天就是最好的时机", AnswerCategory.POSITIVE),
        AnswerBookEntry("一切都会如你所愿", AnswerCategory.POSITIVE),
        AnswerBookEntry("你比你想象的更强大", AnswerCategory.POSITIVE),
        AnswerBookEntry("好运正在向你走来", AnswerCategory.POSITIVE),
        AnswerBookEntry("这是一个值得把握的机会", AnswerCategory.POSITIVE),
        AnswerBookEntry("勇敢说出来吧", AnswerCategory.POSITIVE),
        AnswerBookEntry("此刻的行动将改变一切", AnswerCategory.POSITIVE),
        AnswerBookEntry("你正走在正确的道路上", AnswerCategory.POSITIVE),
        AnswerBookEntry("成功就在不远处", AnswerCategory.POSITIVE),
        AnswerBookEntry("放手去做，一切都会好的", AnswerCategory.POSITIVE),
        AnswerBookEntry("你的付出终将得到回报", AnswerCategory.POSITIVE),
        AnswerBookEntry("此时不搏，更待何时", AnswerCategory.POSITIVE),
        AnswerBookEntry("命运之轮正在转向你", AnswerCategory.POSITIVE),
        AnswerBookEntry("这是一个肯定的答案", AnswerCategory.POSITIVE),
        AnswerBookEntry("相信过程，结果不会让你失望", AnswerCategory.POSITIVE),
        AnswerBookEntry("美好的事情即将发生", AnswerCategory.POSITIVE),
        AnswerBookEntry("你的坚持终将开花结果", AnswerCategory.POSITIVE),
        AnswerBookEntry("机会之门已经打开", AnswerCategory.POSITIVE),
        AnswerBookEntry("去做吧，不要犹豫", AnswerCategory.POSITIVE),

        // ==================== 中性答案（35 条）====================
        AnswerBookEntry("再想想吧", AnswerCategory.NEUTRAL),
        AnswerBookEntry("答案取决于你的选择", AnswerCategory.NEUTRAL),
        AnswerBookEntry("时机还未成熟", AnswerCategory.NEUTRAL),
        AnswerBookEntry("静观其变", AnswerCategory.NEUTRAL),
        AnswerBookEntry("这个问题需要更多时间思考", AnswerCategory.NEUTRAL),
        AnswerBookEntry("答案并不唯一", AnswerCategory.NEUTRAL),
        AnswerBookEntry("一切要看缘分", AnswerCategory.NEUTRAL),
        AnswerBookEntry("现在下结论还为时尚早", AnswerCategory.NEUTRAL),
        AnswerBookEntry("也许换个角度看会有新发现", AnswerCategory.NEUTRAL),
        AnswerBookEntry("你需要更多信息来决定", AnswerCategory.NEUTRAL),
        AnswerBookEntry("天机不可泄露", AnswerCategory.NEUTRAL),
        AnswerBookEntry("有些事急不得", AnswerCategory.NEUTRAL),
        AnswerBookEntry("答案藏在你忽略的细节里", AnswerCategory.NEUTRAL),
        AnswerBookEntry("此路可行，但并非唯一", AnswerCategory.NEUTRAL),
        AnswerBookEntry("先把手头的事做好再说", AnswerCategory.NEUTRAL),
        AnswerBookEntry("等待一个更好的时机", AnswerCategory.NEUTRAL),
        AnswerBookEntry("命运在犹豫中前行", AnswerCategory.NEUTRAL),
        AnswerBookEntry("事情的真相尚未完全显现", AnswerCategory.NEUTRAL),
        AnswerBookEntry("进退皆可，关键在你", AnswerCategory.NEUTRAL),
        AnswerBookEntry("这件事需要更多耐心", AnswerCategory.NEUTRAL),
        AnswerBookEntry("答案在风中飘荡", AnswerCategory.NEUTRAL),
        AnswerBookEntry("先放一放，回头再看", AnswerCategory.NEUTRAL),
        AnswerBookEntry("有些路走了才知道对不对", AnswerCategory.NEUTRAL),
        AnswerBookEntry("不要急，让事情自然发展", AnswerCategory.NEUTRAL),
        AnswerBookEntry("这个答案需要你自己去寻找", AnswerCategory.NEUTRAL),
        AnswerBookEntry("也许有一天你会明白的", AnswerCategory.NEUTRAL),
        AnswerBookEntry("一切随缘，顺其自然", AnswerCategory.NEUTRAL),
        AnswerBookEntry("答案就在下一个转角", AnswerCategory.NEUTRAL),
        AnswerBookEntry("此时此刻，宜静不宜动", AnswerCategory.NEUTRAL),
        AnswerBookEntry("真正的答案需要时间来验证", AnswerCategory.NEUTRAL),
        AnswerBookEntry("有些事说不准，走着瞧吧", AnswerCategory.NEUTRAL),
        AnswerBookEntry("不着急，慢慢来", AnswerCategory.NEUTRAL),
        AnswerBookEntry("你的心里其实已经有了答案", AnswerCategory.NEUTRAL),
        AnswerBookEntry("这个问题只有时间能回答", AnswerCategory.NEUTRAL),
        AnswerBookEntry("不妨先试试看", AnswerCategory.NEUTRAL),

        // ==================== 负面答案（35 条）====================
        AnswerBookEntry("恐怕答案是否定的", AnswerCategory.NEGATIVE),
        AnswerBookEntry("现在不是时候", AnswerCategory.NEGATIVE),
        AnswerBookEntry("建议你放弃这个念头", AnswerCategory.NEGATIVE),
        AnswerBookEntry("这条路走不通", AnswerCategory.NEGATIVE),
        AnswerBookEntry("别白费力气了", AnswerCategory.NEGATIVE),
        AnswerBookEntry("你确定要继续吗？三思而后行", AnswerCategory.NEGATIVE),
        AnswerBookEntry("这不是一个好的选择", AnswerCategory.NEGATIVE),
        AnswerBookEntry("现实可能不如你想象的美好", AnswerCategory.NEGATIVE),
        AnswerBookEntry("也许该换个方向了", AnswerCategory.NEGATIVE),
        AnswerBookEntry("前方的路布满荆棘", AnswerCategory.NEGATIVE),
        AnswerBookEntry("暂时不要做出决定", AnswerCategory.NEGATIVE),
        AnswerBookEntry("这个计划风险太大了", AnswerCategory.NEGATIVE),
        AnswerBookEntry("不要把期望放得太高", AnswerCategory.NEGATIVE),
        AnswerBookEntry("此时沉默是金", AnswerCategory.NEGATIVE),
        AnswerBookEntry("有些事注定不会如愿", AnswerCategory.NEGATIVE),
        AnswerBookEntry("退一步海阔天空", AnswerCategory.NEGATIVE),
        AnswerBookEntry("这不是属于你的机会", AnswerCategory.NEGATIVE),
        AnswerBookEntry("再等等吧，现在不适合", AnswerCategory.NEGATIVE),
        AnswerBookEntry("你的想法需要重新考虑", AnswerCategory.NEGATIVE),
        AnswerBookEntry("时机不对，另寻他路吧", AnswerCategory.NEGATIVE),
        AnswerBookEntry("保持距离可能是最好的选择", AnswerCategory.NEGATIVE),
        AnswerBookEntry("有些坚持未必有意义", AnswerCategory.NEGATIVE),
        AnswerBookEntry("不如意事十之八九", AnswerCategory.NEGATIVE),
        AnswerBookEntry("命运此刻在摇头", AnswerCategory.NEGATIVE),
        AnswerBookEntry("有时候放弃也是一种勇气", AnswerCategory.NEGATIVE),
        AnswerBookEntry("风暴即将来临，请做好准备", AnswerCategory.NEGATIVE),
        AnswerBookEntry("逆风而行未必是好事", AnswerCategory.NEGATIVE),
        AnswerBookEntry("你可能在自欺欺人", AnswerCategory.NEGATIVE),
        AnswerBookEntry("也许是你想多了", AnswerCategory.NEGATIVE),
        AnswerBookEntry("梦境与现实往往相反", AnswerCategory.NEGATIVE),
        AnswerBookEntry("请降低你的期待", AnswerCategory.NEGATIVE),
        AnswerBookEntry("这条路可能越走越窄", AnswerCategory.NEGATIVE),
        AnswerBookEntry("当心脚下的陷阱", AnswerCategory.NEGATIVE),
        AnswerBookEntry("有时最好的选择是不选择", AnswerCategory.NEGATIVE),
        AnswerBookEntry("真相可能令你失望", AnswerCategory.NEGATIVE),
    )

    /**
     * 获取随机答案
     */
    fun getRandomAnswer(): AnswerBookEntry = answers.random(Random)

    /**
     * 获取排除上一条索引的随机答案
     * @param lastIndex 上一次答案的索引，避免连续抽到相同答案
     */
    fun getRandomAnswerExcluding(lastIndex: Int): AnswerBookEntry {
        var index: Int
        do {
            index = Random.nextInt(answers.size)
        } while (index == lastIndex && answers.size > 1)
        return answers[index]
    }
}
