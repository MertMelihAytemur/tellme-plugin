package com.tellme.tellmeplugin.ui.render

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import javax.swing.JComponent

class CefRenderer(private val parentDisposable: Disposable) {

    private var browser: JBCefBrowser? = null
    private var baseLoaded = false
    private var domReady = false
    @Volatile
    private var pendingHtml: String? = null
    var onLinkClicked: ((String) -> Unit)? = null

    private val loadHandler = object : CefLoadHandlerAdapter() {
        override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
            domReady = true
            pendingHtml?.let { html ->
                pendingHtml = null
                updateBodyNow(html)
            }
        }
    }

    val isSupported: Boolean = JBCefApp.isSupported()

    fun getComponent(): JComponent? {
        if (!isSupported) return null

        if (browser == null) {
            browser = JBCefBrowser()
            browser!!.jbCefClient.addLoadHandler(loadHandler, browser!!.cefBrowser)

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

    fun isShowing(): Boolean = browser?.component?.isShowing == true

    fun ensureBaseLoaded() {
        val b = browser ?: return
        if (baseLoaded) return

        baseLoaded = true
        domReady = false
        b.loadHTML(HtmlTemplates.cefBaseHtml())
    }

    fun updateBody(innerHtml: String) {
        if (!domReady) {
            pendingHtml = innerHtml
            return
        }
        updateBodyNow(innerHtml)
    }

    private fun updateBodyNow(innerHtml: String) {
        val b = browser ?: return
        val js = "window.__setContent(${HtmlTemplates.jsString(innerHtml)});"
        b.cefBrowser.executeJavaScript(js, b.cefBrowser.url, 0)
    }

    fun scrollToTop() {
        val b = browser ?: return
        b.cefBrowser.executeJavaScript("window.__scrollToTop();", b.cefBrowser.url, 0)
    }

    fun showReady() {
        ensureBaseLoaded()
        updateBody(HtmlTemplates.readyHtml())
    }

    fun showSkeleton(label: String) {
        pendingHtml = HtmlTemplates.skeletonInnerHtml(label)
        if (isShowing()) {
            ensureBaseLoaded()
            updateBody(pendingHtml!!)
        }
    }

    fun renderMarkdown(markdown: String, showCaret: Boolean) {
        val html = MarkdownRenderer.renderToHtml(markdown, showCaret)
        ensureBaseLoaded()
        updateBody(html)
    }

    fun showSelectionScreen(fileName: String) {
        ensureBaseLoaded()
        updateBody(HtmlTemplates.selectionScreenHtml(fileName))
    }

    fun showOnboarding(ollamaRunning: Boolean, modelDownloaded: Boolean) {
        ensureBaseLoaded()
        updateBody(HtmlTemplates.onboardingHtml(ollamaRunning, modelDownloaded))
    }

    fun dispose() {
        try {
            browser?.jbCefClient?.removeLoadHandler(loadHandler, browser!!.cefBrowser)
        } catch (_: Throwable) {

        }
    }
}
