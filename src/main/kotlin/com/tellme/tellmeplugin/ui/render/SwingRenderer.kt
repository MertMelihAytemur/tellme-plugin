package com.tellme.tellmeplugin.ui.render

import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.text.html.HTMLEditorKit

/**
 * Fallback renderer using Swing JEditorPane.
 * Used when JCEF is not available.
 */
class SwingRenderer {

    private val editorPane: JEditorPane = JEditorPane().apply {
        isEditable = false
        contentType = "text/html"
        editorKit = HTMLEditorKit()
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        border = JBUI.Borders.empty(10)
        isOpaque = false
        text = HtmlTemplates.swingSafeHtml("<b>Ready…</b>")
    }

    private val scrollPane: JBScrollPane = JBScrollPane(editorPane).apply {
        border = JBUI.Borders.empty()
        viewport.isOpaque = true
        viewport.background = UIUtil.getPanelBackground()
    }

    /**
     * Gets the component for embedding in Swing layout.
     */
    fun getComponent(): JComponent = scrollPane

    /**
     * Updates the content with raw HTML body.
     */
    fun updateContent(htmlBody: String) {
        val viewport = scrollPane.viewport
        val viewRect = viewport.viewRect
        val contentHeight = editorPane.height
        
        // Smart scroll: only follow if already at the bottom
        val isAtBottom = (viewRect.y + viewRect.height) >= (contentHeight - 50)
        
        editorPane.text = HtmlTemplates.swingSafeHtml(htmlBody)
        
        if (isAtBottom) {
            editorPane.caretPosition = editorPane.document.length
        }
    }

    /**
     * Scrolls the view to the top.
     */
    fun scrollToTop() {
        editorPane.caretPosition = 0
        scrollPane.verticalScrollBar.value = 0
    }

    /**
     * Shows the "Ready" state.
     */
    fun showReady() {
        editorPane.text = HtmlTemplates.swingSafeHtml("<b>Ready…</b>")
    }

    /**
     * Shows the loading skeleton (simplified for Swing).
     */
    fun showSkeleton(label: String) {
        editorPane.text = HtmlTemplates.swingSafeHtml("<b>$label</b>")
    }

    /**
     * Renders markdown content.
     */
    fun renderMarkdown(markdown: String, showCaret: Boolean) {
        val html = MarkdownRenderer.renderToHtml(markdown, showCaret)
        updateContent(html)
    }
}
