package com.tellme.tellmeplugin.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class TellMeToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val ui = TellMeToolWindow(project)
        Disposer.register(project, ui as Disposable)
        TellMeToolWindow.registerInstance(project, ui)

        val content = ContentFactory.getInstance().createContent(ui.getContent(), null, false)
        content.isCloseable = false
        content.preferredFocusableComponent = ui.getContent()
        
        toolWindow.contentManager.addContent(content)
        
        toolWindow.setTitleActions(ui.getTitleActions())
        
        toolWindow.component.putClientProperty("HideIdLabel", true)
    }
}
