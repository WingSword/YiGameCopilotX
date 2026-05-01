package org.walks.gamecopilot.data

/**
 * 词库导入引擎
 * 支持简单文本格式批量导入，提供校验和错误提示
 *
 * 谁是卧底词库格式（每行一对，用 - 分隔）：
 *   牛奶 - 豆浆
 *   橙子 - 橘子
 *
 * 你画我猜词库格式（每行一个词）：
 *   苹果
 *   香蕉
 *   芭蕾舞
 */
object WordImportEngine {

    // 校验规则常量
    private const val MIN_WORD_LENGTH = 1
    private const val MAX_WORD_LENGTH = 20
    private const val MIN_PAIR_COUNT = 3
    private const val MIN_SINGLE_COUNT = 5
    private const val MAX_IMPORT_COUNT = 200

    // 不合规字符正则（过滤特殊符号、HTML标签等）
    private val INVALID_CHARS = Regex("""[<>&{}\\^~\[\]`|]""")
    private val HTML_TAG = Regex("""<[^>]+>""")
    private val EXCESSIVE_WHITESPACE = Regex("""\s{3,}""")

    /**
     * 导入结果密封类
     */
    sealed class ImportResult {
        data class SpyPairs(
            val pairs: Map<String, String>,
            val warnings: List<String> = emptyList()
        ) : ImportResult()

        data class DrawWords(
            val words: List<String>,
            val warnings: List<String> = emptyList()
        ) : ImportResult()

        data class Error(
            val message: String,
            val lineErrors: List<LineError> = emptyList()
        ) : ImportResult()
    }

    /**
     * 行级错误
     */
    data class LineError(
        val lineNumber: Int,
        val content: String,
        val reason: String
    )

    /**
     * 解析谁是卧底词汇对
     * 格式：每行一对，用 " - " 或 "—" 或 ":" 分隔
     */
    fun parseSpyPairs(text: String): ImportResult {
        val lines = text.lines()
            .mapIndexed { index, line -> index + 1 to line.trim() }
            .filter { (_, line) -> line.isNotBlank() }

        if (lines.isEmpty()) {
            return ImportResult.Error("内容为空，请粘贴词库内容")
        }

        if (lines.size > MAX_IMPORT_COUNT) {
            return ImportResult.Error("一次最多导入 $MAX_IMPORT_COUNT 对词汇，当前 ${lines.size} 行")
        }

        val errors = mutableListOf<LineError>()
        val pairs = mutableMapOf<String, String>()
        val warnings = mutableListOf<String>()
        val seenWords = mutableSetOf<String>()

        for ((lineNum, line) in lines) {
            // 尝试多种分隔符
            val parts = splitPair(line)
            if (parts == null) {
                errors.add(LineError(lineNum, line, "格式错误：请使用 \"词汇A - 词汇B\" 格式"))
                continue
            }

            val (wordA, wordB) = parts

            // 校验单个词汇
            val errorA = validateWord(wordA, lineNum, "左侧")
            val errorB = validateWord(wordB, lineNum, "右侧")
            if (errorA != null) { errors.add(errorA); continue }
            if (errorB != null) { errors.add(errorB); continue }

            // 检查重复
            if (wordA in seenWords || wordB in seenWords) {
                warnings.add("第 $lineNum 行：词汇 \"$wordA\" 或 \"$wordB\" 重复出现，已跳过")
                continue
            }

            // 检查两个词是否相同
            if (wordA == wordB) {
                errors.add(LineError(lineNum, line, "两个词汇不能相同"))
                continue
            }

            seenWords.add(wordA)
            seenWords.add(wordB)
            pairs[wordA] = wordB
        }

        if (errors.isNotEmpty()) {
            return ImportResult.Error(
                message = "发现 ${errors.size} 处错误",
                lineErrors = errors
            )
        }

        if (pairs.size < MIN_PAIR_COUNT) {
            return ImportResult.Error("有效词汇对不足 $MIN_PAIR_COUNT 对（当前 ${pairs.size} 对），请补充更多词汇")
        }

        if (warnings.isNotEmpty()) {
            warnings.add(0, "已导入 ${pairs.size} 对词汇，以下问题已自动跳过：")
        }

        return ImportResult.SpyPairs(pairs, warnings)
    }

    /**
     * 解析你画我猜词汇列表
     * 格式：每行一个词汇
     */
    fun parseDrawWords(text: String): ImportResult {
        val lines = text.lines()
            .mapIndexed { index, line -> index + 1 to line.trim() }
            .filter { (_, line) -> line.isNotBlank() }

        if (lines.isEmpty()) {
            return ImportResult.Error("内容为空，请粘贴词库内容")
        }

        if (lines.size > MAX_IMPORT_COUNT) {
            return ImportResult.Error("一次最多导入 $MAX_IMPORT_COUNT 个词汇，当前 ${lines.size} 行")
        }

        val errors = mutableListOf<LineError>()
        val words = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val seenWords = mutableSetOf<String>()

        for ((lineNum, line) in lines) {
            // 去掉可能的序号前缀（"1. 苹果" -> "苹果"）
            val cleaned = line.replace(Regex("""^\d+[\.\、\)\s]+"""), "").trim()

            val error = validateWord(cleaned, lineNum, "")
            if (error != null) { errors.add(error); continue }

            if (cleaned in seenWords) {
                warnings.add("第 $lineNum 行：词汇 \"$cleaned\" 重复出现，已跳过")
                continue
            }

            seenWords.add(cleaned)
            words.add(cleaned)
        }

        if (errors.isNotEmpty()) {
            return ImportResult.Error(
                message = "发现 ${errors.size} 处错误",
                lineErrors = errors
            )
        }

        if (words.size < MIN_SINGLE_COUNT) {
            return ImportResult.Error("有效词汇不足 $MIN_SINGLE_COUNT 个（当前 ${words.size} 个），请补充更多词汇")
        }

        if (warnings.isNotEmpty()) {
            warnings.add(0, "已导入 ${words.size} 个词汇，以下问题已自动跳过：")
        }

        return ImportResult.DrawWords(words, warnings)
    }

    /**
     * 分隔词汇对
     */
    private fun splitPair(line: String): Pair<String, String>? {
        val separators = listOf(" - ", " — ", " – ", "：", ":")
        for (sep in separators) {
            val idx = line.indexOf(sep)
            if (idx > 0 && idx < line.length - sep.length) {
                val a = line.substring(0, idx).trim()
                val b = line.substring(idx + sep.length).trim()
                if (a.isNotBlank() && b.isNotBlank()) return a to b
            }
        }
        return null
    }

    /**
     * 校验单个词汇
     */
    private fun validateWord(word: String, lineNum: Int, position: String): LineError? {
        return when {
            word.length < MIN_WORD_LENGTH ->
                LineError(lineNum, word, "${position}词汇过短")

            word.length > MAX_WORD_LENGTH ->
                LineError(lineNum, word, "${position}词汇过长（最多 $MAX_WORD_LENGTH 字）")

            INVALID_CHARS.containsMatchIn(word) ->
                LineError(lineNum, word, "${position}词汇包含不允许的特殊字符")

            HTML_TAG.containsMatchIn(word) ->
                LineError(lineNum, word, "${position}词汇包含 HTML 标签")

            EXCESSIVE_WHITESPACE.containsMatchIn(word) ->
                LineError(lineNum, word, "${position}词汇包含过多空白")

            word.all { it.isWhitespace() } ->
                LineError(lineNum, word, "${position}词汇不能全为空白")

            else -> null
        }
    }

    /**
     * 生成谁是卧底导入模板
     */
    fun spyTemplate(): String {
        return buildString {
            appendLine("=== 谁是卧底 自定义词库 ===")
            appendLine("格式说明：每行一对词汇，用 - 分隔")
            appendLine("示例：")
            appendLine("牛奶 - 豆浆")
            appendLine("橙子 - 橘子")
            appendLine("咖啡 - 浓茶")
            appendLine("面包 - 蛋糕")
            appendLine("=============")
            appendLine()
            appendLine("请在下方输入你的词库（每行一对）：")
            appendLine("词汇A - 词汇B")
        }
    }

    /**
     * 生成你画我猜导入模板
     */
    fun drawTemplate(): String {
        return buildString {
            appendLine("=== 你画我猜 自定义词库 ===")
            appendLine("格式说明：每行一个词汇")
            appendLine("示例：")
            appendLine("苹果")
            appendLine("香蕉")
            appendLine("长颈鹿")
            appendLine("摩天大楼")
            appendLine("=============")
            appendLine()
            appendLine("请在下方输入你的词库（每行一个）：")
        }
    }
}
