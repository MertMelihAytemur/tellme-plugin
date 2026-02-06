package com.tellme.tellmeplugin.ui.render

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import javax.swing.JComponent

/**
 * Renders HTML content using JCEF (Chromium Embedded Framework).
 * Provides a rich, modern rendering experience.
 */
class CefRenderer(private val parentDisposable: Disposable) {

    private var browser: JBCefBrowser? = null
    private var baseLoaded = false
    private var domReady = false
    @Volatile
    private var pendingHtml: String? = null

    /** Callback for when a tellme:// link is clicked */
    var onLinkClicked: ((String) -> Unit)? = null

    private val loadHandler = object : CefLoadHandlerAdapter() {
        override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
            domReady = true
            pendingHtml?.let { html ->
                pendingHtml = null
                // Execute immediately on the same thread to avoid race conditions with subsequent updates
                updateBodyNow(html)
            }
        }
    }

    /**
     * Whether JCEF is supported on this platform.
     */
    val isSupported: Boolean = JBCefApp.isSupported()

    /**
     * Gets the browser component for embedding in Swing layout.
     * Returns null if JCEF is not supported.
     */
    fun getComponent(): JComponent? {
        if (!isSupported) return null

        if (browser == null) {
            browser = JBCefBrowser()
            browser!!.jbCefClient.addLoadHandler(loadHandler, browser!!.cefBrowser)
            
            // Fix initial zero-size issues when IDE opens
            browser!!.component.addComponentListener(object : java.awt.event.ComponentAdapter() {
                override fun componentResized(e: java.awt.event.ComponentEvent?) {
                    ApplicationManager.getApplication().invokeLater {
                        val comp = browser?.component ?: return@invokeLater
                        browser?.cefBrowser?.wasResized(comp.width, comp.height)
                        browser?.component?.revalidate()
                        browser?.component?.repaint()
                        // Force layout update inside the browser
                        browser?.cefBrowser?.executeJavaScript("window.dispatchEvent(new Event('resize'));", "", 0)
                    }
                }
                override fun componentShown(e: java.awt.event.ComponentEvent?) {
                    ApplicationManager.getApplication().invokeLater {
                        val comp = browser?.component ?: return@invokeLater
                        browser?.cefBrowser?.wasResized(comp.width, comp.height)
                        browser?.component?.revalidate()
                        browser?.component?.repaint()
                        browser?.cefBrowser?.executeJavaScript("window.dispatchEvent(new Event('resize'));", "", 0)
                    }
                }
            })
            
            // Handle tellme:// protocol links
            browser!!.jbCefClient.addRequestHandler(object : org.cef.handler.CefRequestHandlerAdapter() {
                override fun onBeforeBrowse(
                    browser: CefBrowser?,
                    frame: CefFrame?,
                    request: org.cef.network.CefRequest?,
                    user_gesture: Boolean,
                    is_redirect: Boolean
                ): Boolean {
                    val url = request?.url ?: return false
                    if (url.startsWith("tellme://")) {
                        onLinkClicked?.invoke(url)
                        return true // Block actual navigation
                    }
                    return false
                }
            }, browser!!.cefBrowser)
        }

        return browser!!.component
    }

    /**
     * Checks if the component is currently showing on screen.
     */
    fun isShowing(): Boolean = browser?.component?.isShowing == true

    /**
     * Ensures the base HTML template is loaded.
     */
    fun ensureBaseLoaded() {
        val b = browser ?: return
        if (baseLoaded) return

        baseLoaded = true
        domReady = false
        b.loadHTML(HtmlTemplates.cefBaseHtml())
    }

    /**
     * Updates the body content of the page.
     * If DOM is not ready yet, the content is queued.
     */
    fun updateBody(innerHtml: String) {
        if (!domReady) {
            pendingHtml = innerHtml
            return
        }
        updateBodyNow(innerHtml)
    }

    /**
     * Immediately updates the body content via JavaScript.
     */
    private fun updateBodyNow(innerHtml: String) {
        val b = browser ?: return
        val js = "window.__setContent(${HtmlTemplates.jsString(innerHtml)});"
        b.cefBrowser.executeJavaScript(js, b.cefBrowser.url, 0)
    }

    /**
     * Scrolls the view to the top.
     */
    fun scrollToTop() {
        val b = browser ?: return
        b.cefBrowser.executeJavaScript("window.__scrollToTop();", b.cefBrowser.url, 0)
    }

    /**
     * Shows the "Ready" state.
     */
    fun showReady() {
        ensureBaseLoaded()
        updateBody(HtmlTemplates.readyHtml())
    }

    /**
     * Shows the loading skeleton.
     */
    fun showSkeleton(label: String) {
        pendingHtml = HtmlTemplates.skeletonInnerHtml(label)
        if (isShowing()) {
            ensureBaseLoaded()
            updateBody(pendingHtml!!)
        }
    }

    /**
     * Renders markdown content.
     */
    fun renderMarkdown(markdown: String, showCaret: Boolean) {
        val html = MarkdownRenderer.renderToHtml(markdown, showCaret)
        ensureBaseLoaded()
        updateBody(html)
    }

    /**
     * Shows the selection screen (Tell Me vs Refactor).
     */
    fun showSelectionScreen(fileName: String) {
        ensureBaseLoaded()
        updateBody(HtmlTemplates.selectionScreenHtml(fileName))
    }

    /**
     * Shows the onboarding setup screen.
     */
    fun showOnboarding(ollamaRunning: Boolean, modelDownloaded: Boolean) {
        ensureBaseLoaded()
        updateBody(HtmlTemplates.onboardingHtml(ollamaRunning, modelDownloaded))
    }

    /**
     * Cleans up resources.
     */
    fun dispose() {
        try {
            browser?.jbCefClient?.removeLoadHandler(loadHandler, browser!!.cefBrowser)
        } catch (_: Throwable) {
            // Ignore disposal errors
        }
    }
}
