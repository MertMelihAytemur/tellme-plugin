package com.tellme.tellmeplugin.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Factory for creating the Tell Me tool window.
 */
class TellMeToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val ui = TellMeToolWindow(project)
        Disposer.register(project, ui as Disposable)
        TellMeToolWindow.registerInstance(project, ui)

        // Create content without a display name to hide the content tab bar
        val content = ContentFactory.getInstance().createContent(ui.getContent(), null, false)
        content.isCloseable = false
        content.preferredFocusableComponent = ui.getContent()
        
        toolWindow.contentManager.addContent(content)
        
        // Add title actions (Refresh, Copy, etc.) to the tool window title bar
        toolWindow.setTitleActions(ui.getTitleActions())
        
        // Hide the content tab bar when there's only one content
        toolWindow.component.putClientProperty("HideIdLabel", true)
    }
}
