package com.tellme.tellmeplugin.ui.render

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser

/**
 * Converts Markdown text to HTML using Flexmark.
 */
object MarkdownRenderer {

    private val parser: Parser = Parser.builder().build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder().build()

    /**
     * Renders markdown text to HTML.
     *
     * @param markdown The markdown text to render
     * @param showCaret If true, appends a typing caret at the end
     * @return HTML string
     */
    fun renderToHtml(markdown: String, showCaret: Boolean = false): String {
        val text = if (showCaret) {
            "$markdown\n${HtmlTemplates.CARET_CHAR}"
        } else {
            markdown
        }

        return try {
            val document = parser.parse(text)
            renderer.render(document)
        } catch (_: Throwable) {
            "<pre>${HtmlTemplates.escapeHtml(text)}</pre>"
        }
    }
}
