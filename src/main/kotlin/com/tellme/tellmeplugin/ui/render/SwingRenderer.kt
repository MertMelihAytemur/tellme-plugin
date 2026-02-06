package com.tellme.tellmeplugin.ui.render

import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.text.html.HTMLEditorKit
import javax.swing.event.HyperlinkEvent

/**
 * Fallback renderer using Swing JEditorPane.
 * Used when JCEF is not available.
 */
class SwingRenderer {

    /** Callback for when a tellme:// link is clicked */
    var onLinkClicked: ((String) -> Unit)? = null

    private val editorPane: JEditorPane = JEditorPane().apply {
        isEditable = false
        contentType = "text/html"
        editorKit = HTMLEditorKit()
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        border = JBUI.Borders.empty(10)
        isOpaque = false
        
        addHyperlinkListener { e ->
            if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                val url = e.description
                if (url?.startsWith("tellme://") == true) {
                    onLinkClicked?.invoke(url)
                }
            }
        }
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

    fun showSelectionScreen(fileName: String) {
        editorPane.text = HtmlTemplates.swingSafeHtml(HtmlTemplates.selectionScreenHtml(fileName))
        editorPane.caretPosition = 0
    }

    fun showOnboarding(ollamaRunning: Boolean, modelDownloaded: Boolean) {
        editorPane.text = HtmlTemplates.swingSafeHtml(HtmlTemplates.onboardingHtml(ollamaRunning, modelDownloaded))
        editorPane.caretPosition = 0
    }

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
        if (editorPane.document.length > 0) {
            editorPane.caretPosition = 0
        }
        scrollPane.verticalScrollBar.value = 0
    }

    /**
     * Shows the "Ready" state.
     */
    fun showReady() {
        editorPane.text = HtmlTemplates.swingSafeHtml(HtmlTemplates.readyHtml())
        editorPane.caretPosition = 0
    }

    /**
     * Shows the loading skeleton (simplified for Swing).
     */
    fun showSkeleton(label: String) {
        editorPane.text = HtmlTemplates.swingSafeHtml(HtmlTemplates.skeletonInnerHtml(label))
        editorPane.caretPosition = 0
    }

    /**
     * Renders markdown content.
     */
    fun renderMarkdown(markdown: String, showCaret: Boolean) {
        val htmlBody = MarkdownRenderer.renderToHtml(markdown, showCaret)
        updateContent(htmlBody)
    }
}
