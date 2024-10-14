import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

interface QuillContentStyle {
    val fontSize: TextUnit
    val header1FontSize: TextUnit
    val header2FontSize: TextUnit
    val lineHeightMultiple: Float
    val underlineStyle: TextDecoration
    val textColor: Color?
    val backgroundColor: Color
    val linkColor: Color
    val bulletPointSymbol: String
    val orderedListNumberFormat: (Int) -> String
    val indentSizeFromIndentLevel: (Int) -> Float
    val orderedListNumberFormatter: (Int) -> String
}

class DefaultQuillStyle : QuillContentStyle {
    override val fontSize: TextUnit = 14.sp
    override val header1FontSize: TextUnit = 24.sp
    override val header2FontSize: TextUnit = 20.sp
    override val lineHeightMultiple: Float = 1.2f
    override val underlineStyle: TextDecoration = TextDecoration.Underline
    override val textColor: Color? = Color.Black
    override val backgroundColor: Color = Color.White
    override val linkColor: Color = Color.Blue
    override val bulletPointSymbol: String = "\u2022"
    override val orderedListNumberFormat: (Int) -> String = { "$it." }
    override val indentSizeFromIndentLevel: (Int) -> Float = { it * 2.0f * fontSize.value }
    override val orderedListNumberFormatter: (Int) -> String = { "$it " }
}

data class QuillAttributes(
    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val underline: Boolean? = null,
    val strike: Boolean? = null,
    val link: String? = null,
    val header: Int? = null,
    val indent: Int? = null,
    val list: String? = null,
    val blockquote: Boolean? = null,
)

data class QuillOp(
    val insert: String,
    val attributes: QuillAttributes? = null,
)

data class QuillJSContent(
    val ops: List<QuillOp>,
)

data class QuillViewRenderObject(
    val content: AnnotatedString,
    val isQuote: Boolean,
)

class QuillParser(
    private val style: QuillContentStyle = DefaultQuillStyle(),
) {
    fun parseQuillJS(json: String): List<QuillViewRenderObject> {
        val content =
            parseJsonContent(json)
                ?: return listOf(QuillViewRenderObject(AnnotatedString(json), false))

        val result = mutableListOf<QuillViewRenderObject>()
        var line = mutableListOf<QuillOp>()
        val currentIndentMap = mutableMapOf<Int, Int>()

        for (op in content.ops) {
            val texts = op.insert.split("\n")
            val isBlock =
                op.attributes?.header != null || op.attributes?.list != null || op.attributes?.blockquote == true

            if (isBlock) {
                line.add(QuillOp("", op.attributes))
                var blockquote = false
                result.add(stringFromLines(line, currentIndentMap, blockquote))
                line.clear()
            } else {
                for ((index, text) in texts.withIndex()) {
                    line.add(QuillOp(text, op.attributes))
                    if (op.insert.contains("\n") && index < texts.size - 1) {
                        var blockquote = false
                        result.add(stringFromLines(line, currentIndentMap, blockquote))
                        line.clear()
                    }
                }
            }
        }
        return result
    }

    private fun parseJsonContent(json: String): QuillJSContent? =
        try {
            val jsonObject = JSONObject(json)
            val opsArray = jsonObject.getJSONArray("ops")
            val opsList = mutableListOf<QuillOp>()

            for (i in 0 until opsArray.length()) {
                val opObject = opsArray.getJSONObject(i)
                val insert = opObject.getString("insert")
                val attributes =
                    opObject.optJSONObject("attributes")?.let {
                        QuillAttributes(
                            bold = it.optBoolean("bold"),
                            italic = it.optBoolean("italic"),
                            underline = it.optBoolean("underline"),
                            strike = it.optBoolean("strike"),
                            link = it.optString("link"),
                            header = it.optInt("header"),
                            indent = it.optInt("indent"),
                            list = it.optString("list"),
                            blockquote = it.optBoolean("blockquote"),
                        )
                    }
                opsList.add(QuillOp(insert, attributes))
            }
            QuillJSContent(opsList)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    private fun stringFromLines(
        lines: List<QuillOp>,
        currentIndentMap: MutableMap<Int, Int>,
        blockquote: Boolean,
    ): QuillViewRenderObject {
        var blockquote = blockquote
        val builder = AnnotatedString.Builder()
        var headerLevel = 0
        var indentLevel = 0
        var textIndent = 0f

        val blockAttr = lines.last().attributes
        blockAttr?.let {
            if (it.header != null) {
                headerLevel = it.header
            }
            if (it.indent != null) {
                indentLevel = it.indent
                if (indentLevel > 0) {
                    textIndent = style.indentSizeFromIndentLevel(indentLevel)
                }
            }
            when (it.list) {
                "ordered" -> {
                    val number = orderListNumber(currentIndentMap, indentLevel)
                    builder.append("${style.orderedListNumberFormat(number)} ")
                }

                "bullet" -> builder.append("${style.bulletPointSymbol} ")
                else -> currentIndentMap.clear()
            }
            if (it.blockquote == true) {
                blockquote = true
            }
        }

        lines.forEach { line ->
            val attributes = line.attributes
            val spanStyle =
                SpanStyle(
                    fontWeight = if (attributes?.bold == true) FontWeight.Bold else null,
                    fontStyle = if (attributes?.italic == true) FontStyle.Italic else null,
                    fontSize =
                        when (headerLevel) {
                            1 -> style.header1FontSize
                            2 -> style.header2FontSize
                            else -> TextUnit.Unspecified
                        },
                    textDecoration =
                        when {
                            attributes?.underline == true || attributes?.link != null -> TextDecoration.Underline
                            attributes?.strike == true -> TextDecoration.LineThrough
                            else -> null
                        },
                    color = if (attributes?.link != null) style.linkColor else Color.Unspecified,
                )

            builder.withStyle(spanStyle) {
                if (attributes?.link != null) {
                    val start = builder.length
                    append(line.insert)
                    val end = builder.length
                    builder.addStringAnnotation(
                        tag = "URL",
                        annotation = attributes.link,
                        start = start,
                        end = end,
                    )
                } else {
                    append(line.insert)
                }
            }
        }

        if (textIndent > 0) {
            // builder.addStyle(SpanStyle(textIndent = ), 0, builder.length)
            builder.addStyle(
                ParagraphStyle(
                    textIndent =
                        TextIndent(
                            firstLine = textIndent.sp,
                            restLine = textIndent.sp,
                        ),
                ),
                0,
                builder.length,
            )
        }

        return QuillViewRenderObject(content = builder.toAnnotatedString(), isQuote = blockquote)
    }

    private fun orderListNumber(
        currentIndentMap: MutableMap<Int, Int>,
        indent: Int,
    ): Int {
        val result = currentIndentMap.getOrDefault(indent, 1)
        currentIndentMap[indent] = result + 1
        return result
    }
}

fun String.convertStringToQuillJson(): String {
    val opsArray =
        JSONArray().apply {
            put(
                JSONObject().apply {
                    put("insert", "${this@convertStringToQuillJson}\n")
                },
            )
        }

    return JSONObject()
        .apply {
            put("ops", opsArray)
        }.toString()
}
