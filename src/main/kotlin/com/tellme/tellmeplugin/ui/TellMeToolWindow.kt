package com.tellme.tellmeplugin.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.tellme.tellmeplugin.client.OllamaClient
import com.tellme.tellmeplugin.client.OllamaConfig
import com.tellme.tellmeplugin.ui.render.CefRenderer
import com.tellme.tellmeplugin.ui.render.SwingRenderer
import com.tellme.tellmeplugin.ui.session.Session
import com.tellme.tellmeplugin.ui.session.SessionManager
import com.tellme.tellmeplugin.ui.session.UiState
import com.tellme.tellmeplugin.ui.tab.TabManager
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import java.util.concurrent.ConcurrentHashMap
import javax.swing.*

/**
 * Main UI component for the Tell Me tool window.
 * Coordinates sessions, tabs, and rendering.
 */
class TellMeToolWindow(private val project: Project) : Disposable {

    companion object {
        private val instances = ConcurrentHashMap<Project, TellMeToolWindow>()

        fun getInstance(project: Project): TellMeToolWindow? = instances[project]

        internal fun registerInstance(project: Project, instance: TellMeToolWindow) {
            instances[project] = instance
        }

        internal fun unregisterInstance(project: Project) {
            instances.remove(project)
        }
    }

    // Managers
    private val sessionManager = SessionManager.getOrCreate(project)
    private lateinit var tabManager: TabManager

    // UI Components
    // Components
    private val rootPanel = JPanel(BorderLayout())

    private val headerPanel = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.empty(0, 0, 0, 0)
        background = UIUtil.getPanelBackground()
    }

    private val statusIcon = JBLabel(AnimatedIcon.Default())
    private val statusLabel = JBLabel("Ready")
    
    private val statusPanel = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.empty(2, 6, 2, 6)
        background = UIUtil.getPanelBackground()
        
        val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 6, 0)).apply {
            isOpaque = false
            add(statusIcon)
            add(statusLabel)
        }
        val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 6, 0)).apply {
            isOpaque = false
        }
        
        add(leftPanel, BorderLayout.WEST)
        add(rightPanel, BorderLayout.EAST)
    }

    // Renderers
    private val cefRenderer = CefRenderer(this)
    private val swingRenderer = SwingRenderer()
    private val useJcef = cefRenderer.isSupported

    // Timers
    private val renderDebounceMs = 50
    private val idleDoneMs = 650L
    private val maxLoadingMs = 60_000L  // 60 seconds timeout for slow models

    private val renderTimer: Timer = Timer(renderDebounceMs) { renderCurrentNow() }.apply {
        isRepeats = false
    }

    private val tickTimer: Timer = Timer(250) { tickSessions() }.apply {
        isRepeats = true
        start()
    }

    private var startedWhenShowing = false

    init {
        // Initialize TabManager
        tabManager = TabManager(project, this) { key ->
            sessionManager.selectSession(key ?: "")
            if (key == null) sessionManager.clearSelection()
            renderCurrentSessionImmediately()
            scrollToTop() // Switch to top on tab change
        }

        // Header with tabs
        headerPanel.add(tabManager.getComponent(), BorderLayout.CENTER)
        rootPanel.add(headerPanel, BorderLayout.NORTH)

        // Viewer component
        val viewerComponent: JComponent = if (useJcef) {
            cefRenderer.getComponent()!!
        } else {
            swingRenderer.getComponent()
        }
        rootPanel.add(viewerComponent, BorderLayout.CENTER)
        rootPanel.add(statusPanel, BorderLayout.SOUTH)

        setStatusReady("Ready")
        showReady()

        // Bind renderer link clicks
        cefRenderer.onLinkClicked = { url: String -> handleLinkClick(url) }
        swingRenderer.onLinkClicked = { url: String -> handleLinkClick(url) }

        // Initial check
        checkOllamaStatus()

        // JCEF hierarchy listener
        if (useJcef) {
            cefRenderer.getComponent()!!.addHierarchyListener {
                val component = cefRenderer.getComponent()!!
                if (!startedWhenShowing && component.isShowing) {
                    startedWhenShowing = true
                    cefRenderer.ensureBaseLoaded()
                    checkOllamaStatus()
                } else if (component.isShowing) {
                    renderSelectedOrReady()
                }
            }
        }
    }

    private var ollamaRunning = false
    private var modelDownloaded = false

    private fun checkOllamaStatus(silent: Boolean = false) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val status = OllamaClient.checkStatus()
            ollamaRunning = status.first
            modelDownloaded = status.second
            
            ApplicationManager.getApplication().invokeLater {
                renderSelectedOrReady()
            }
        }
    }

    fun getContent(): JComponent = rootPanel

    /**
     * Opens or creates a tab for the given file and starts analysis if needed.
     */
    fun openOrCreateTabAndMaybeStart(fileName: String, filePath: String, clipped: String) {
        val key = Session.generateKey(filePath, clipped)

        // If session already exists, just select it and render its current state
        if (sessionManager.hasSession(key)) {
            sessionManager.selectSession(key)
            tabManager.selectTab(key)
            // Force immediate render of selected session's state
            renderCurrentSessionImmediately()
            return
        }

        // Create new session and tab
        val session = sessionManager.createSession(fileName, filePath, clipped)
        tabManager.addTab(session) { closeKey -> closeTab(closeKey) }
        tabManager.selectTab(key)
        sessionManager.selectSession(key)

        // Don't start analysis immediately - show selection screen first
        session.state = UiState.WAITING_FOR_SELECTION
        renderSelectedOrReady()
    }

    private fun closeTab(key: String) {
        sessionManager.removeSession(key)
        tabManager.removeTab(key)
        renderSelectedOrReady()
    }

    // ---- Analysis ----

    private fun startAnalysis(session: Session, type: OllamaConfig.PromptType = OllamaConfig.PromptType.EXPLAIN) {
        session.state = UiState.LOADING
        session.hasToken = false
        session.lastPromptType = type
        session.buffer.setLength(0)
        session.requestId += 1

        val now = System.currentTimeMillis()
        session.loadingStartedAtMs = now
        session.lastTokenAtMs = now

        if (sessionManager.currentKey == session.key) {
            val statusText = if (type == OllamaConfig.PromptType.REFACTOR) "Refactoring…" else "Analyzing…"
            setStatusLoading(statusText)
            showSkeleton(statusText)
        }

        val requestId = session.requestId
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                OllamaClient.explainFileStream(
                    fileName = session.fileName,
                    fileContent = session.clipped,
                    promptType = type,
                    onToken = { chunk ->
                        ApplicationManager.getApplication().invokeLater {
                            val s = sessionManager.getSession(session.key) ?: return@invokeLater
                            if (s.requestId != requestId) return@invokeLater

                            s.buffer.append(chunk)
                            s.hasToken = true
                            s.lastTokenAtMs = System.currentTimeMillis()

                            if (sessionManager.currentKey == s.key) scheduleRender()
                        }
                    }
                )
            } catch (t: Throwable) {
                ApplicationManager.getApplication().invokeLater {
                    val s = sessionManager.getSession(session.key) ?: return@invokeLater
                    if (s.requestId != requestId) return@invokeLater

                    s.state = UiState.ERROR
                    s.showCaret = false
                    s.buffer.setLength(0)
                    s.buffer.append("Error: ${t.message ?: "Unknown error"}")

                    if (sessionManager.currentKey == s.key) {
                        setStatusReady("Ready")
                        scheduleRender()
                    }
                }
            }
        }
    }

    private fun refreshCurrent() {
        val session = sessionManager.getCurrentSession() ?: return
        // Reset session state and buffer to show selection screen again
        session.requestId += 1
        session.buffer.setLength(0)
        session.hasToken = false
        session.state = UiState.WAITING_FOR_SELECTION
        renderSelectedOrReady()
    }

    private fun refactorCurrent() {
        val session = sessionManager.getCurrentSession() ?: return
        startAnalysis(session, OllamaConfig.PromptType.REFACTOR)
    }

    private fun copyCurrentMarkdown() {
        val session = sessionManager.getCurrentSession() ?: return
        val text = session.buffer.toString()
        if (text.isBlank()) return
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }

    // ---- Rendering ----

    private fun scheduleRender() {
        if (SwingUtilities.isEventDispatchThread()) {
            if (!renderTimer.isRunning) {
                renderTimer.start()
            }
        } else {
            SwingUtilities.invokeLater {
                if (!renderTimer.isRunning) {
                    renderTimer.start()
                }
            }
        }
    }

    private fun renderSelectedOrReady() {
        if (!ollamaRunning || !modelDownloaded) {
            showOnboarding()
            return
        }

        val session = sessionManager.getCurrentSession()

        if (session == null) {
            setStatusReady("Ready")
            showReady()
            return
        }

        when (session.state) {
            UiState.LOADING -> {
                val statusText = if (session.lastPromptType == OllamaConfig.PromptType.REFACTOR) "Refactoring…" else "Analyzing…"
                setStatusLoading(statusText)
                if (!session.hasToken && session.buffer.isEmpty()) {
                    showSkeleton(statusText)
                } else {
                    // Render the current session's buffer
                    renderCurrentNow()
                }
            }
            UiState.WAITING_FOR_SELECTION -> {
                setStatusReady("Ready")
                showSelectionScreen(session.fileName)
            }
            UiState.DONE, UiState.ERROR -> {
                setStatusReady("Ready")
                renderCurrentNow()
            }
            UiState.IDLE, UiState.WAITING -> {
                setStatusReady("Ready")
                showReady()
            }
        }
    }

    private fun renderCurrentSessionImmediately() {
        if (!ollamaRunning || !modelDownloaded) {
            showOnboarding()
            return
        }

        val session = sessionManager.getCurrentSession()

        if (session == null) {
            setStatusReady("Ready")
            showReady()
            return
        }

        when (session.state) {
            UiState.LOADING -> {
                val statusText = if (session.lastPromptType == OllamaConfig.PromptType.REFACTOR) "Refactoring…" else "Analyzing…"
                setStatusLoading(statusText)
                if (!session.hasToken && session.buffer.isEmpty()) {
                    showSkeleton(statusText)
                } else {
                    // Render current buffer immediately
                    renderCurrentNow()
                }
            }
            UiState.WAITING_FOR_SELECTION -> {
                setStatusReady("Ready")
                showSelectionScreen(session.fileName)
            }
            UiState.DONE, UiState.ERROR -> {
                setStatusReady("Ready")
                renderCurrentNow()
            }
            UiState.IDLE, UiState.WAITING -> {
                setStatusReady("Ready")
                showReady()
            }
        }
    }

    private fun renderCurrentNow() {
        val session = sessionManager.getCurrentSession()
        if (session == null) {
            showReady()
            return
        }

        if (session.state == UiState.LOADING && !session.hasToken && session.buffer.isEmpty()) {
            val label = if (session.lastPromptType == OllamaConfig.PromptType.REFACTOR) "Refactoring…" else "Analyzing…"
            showSkeleton(label)
            return
        }

        val markdown = session.buffer.toString()
        if (useJcef) {
            cefRenderer.renderMarkdown(markdown, session.showCaret)
        } else {
            swingRenderer.renderMarkdown(markdown, session.showCaret)
        }
        
        // Keep rendering while loading for typewriter effect
        if (session.state == UiState.LOADING) {
            scheduleRender()
        }
    }

    // ---- Tick (finish / timeout) ----

    private fun tickSessions() {
        val now = System.currentTimeMillis()

        for (session in sessionManager.getAllSessions()) {
            if (session.state != UiState.LOADING) continue

            // No token timeout -> ERROR
            if (!session.hasToken) {
                val started = session.loadingStartedAtMs
                if (started != 0L && now - started >= maxLoadingMs) {
                    // Increment requestId to ignore any late-arriving tokens
                    session.requestId += 1
                    session.state = UiState.ERROR
                    session.showCaret = false
                    session.buffer.setLength(0)
                    session.buffer.append("No response (timeout). Press Refresh.")

                    if (sessionManager.currentKey == session.key) {
                        setStatusReady("Ready")
                        scheduleRender()
                    }
                }
                continue
            }

            // Idle -> DONE
            if (now - session.lastTokenAtMs >= idleDoneMs) {
                session.state = UiState.DONE
                session.showCaret = false

                if (sessionManager.currentKey == session.key) {
                    setStatusReady("Ready")
                    scheduleRender()
                }
            }
        }
    }

    // ---- Status helpers ----

    private fun setStatusReady(text: String) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater { setStatusReady(text) }
            return
        }
        statusIcon.isVisible = false
        statusLabel.text = text
    }
    
    private fun setStatusLoading(text: String) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater { setStatusLoading(text) }
            return
        }
        statusIcon.isVisible = true
        statusLabel.text = text
    }
    
    /**
     * Gets actions for the ToolWindow title bar.
     */
    fun getTitleActions(): List<com.intellij.openapi.actionSystem.AnAction> {
        return listOf(
            object : com.intellij.openapi.actionSystem.AnAction("Refactor", "Refactor target file", AllIcons.Actions.RefactoringBulb) {
                override fun actionPerformed(e: com.intellij.openapi.actionSystem.AnActionEvent) = refactorCurrent()
            },
            object : com.intellij.openapi.actionSystem.AnAction("Refresh", "Reset session", AllIcons.Actions.Refresh) {
                override fun actionPerformed(e: com.intellij.openapi.actionSystem.AnActionEvent) = refreshCurrent()
            },
            object : com.intellij.openapi.actionSystem.AnAction("Copy Markdown", "Copy to clipboard", AllIcons.Actions.Copy) {
                override fun actionPerformed(e: com.intellij.openapi.actionSystem.AnActionEvent) = copyCurrentMarkdown()
            }
        )
    }

    // ---- Viewer helpers ----

    private fun showOnboarding() {
        if (useJcef) {
            cefRenderer.showOnboarding(ollamaRunning, modelDownloaded)
        } else {
            swingRenderer.showOnboarding(ollamaRunning, modelDownloaded)
        }
    }

    private fun showReady() {
        if (useJcef) {
            cefRenderer.showReady()
        } else {
            swingRenderer.showReady()
        }
    }

    private fun showSkeleton(label: String) {
        if (useJcef) {
            cefRenderer.showSkeleton(label)
        } else {
            swingRenderer.showSkeleton(label)
        }
    }

    private fun showSelectionScreen(fileName: String) {
        if (useJcef) {
            cefRenderer.showSelectionScreen(fileName)
        } else {
            swingRenderer.showSelectionScreen(fileName)
        }
    }

    private fun handleLinkClick(url: String) {
        if (url == "tellme://check") {
            checkOllamaStatus()
            return
        }

        val session = sessionManager.getCurrentSession() ?: return
        
        // Before any action, verify Ollama is still there
        ApplicationManager.getApplication().executeOnPooledThread {
            val status = OllamaClient.checkStatus()
            ollamaRunning = status.first
            modelDownloaded = status.second
            
            ApplicationManager.getApplication().invokeLater {
                if (!ollamaRunning || !modelDownloaded) {
                    showOnboarding()
                } else {
                    when (url) {
                        "tellme://explain" -> startAnalysis(session, OllamaConfig.PromptType.EXPLAIN)
                        "tellme://refactor" -> startAnalysis(session, OllamaConfig.PromptType.REFACTOR)
                    }
                }
            }
        }
    }

    private fun scrollToTop() {
        if (useJcef) {
            cefRenderer.scrollToTop()
        } else {
            swingRenderer.scrollToTop()
        }
    }

    override fun dispose() {
        tickTimer.stop()
        renderTimer.stop()
        cefRenderer.dispose()
        SessionManager.unregister(project)
        unregisterInstance(project)
    }
}
