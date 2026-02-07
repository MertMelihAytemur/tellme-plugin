package com.tellme.tellmeplugin.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.ToolWindowManager
import com.tellme.tellmeplugin.client.OllamaConfig
import com.tellme.tellmeplugin.ui.TellMeToolWindow

class TellMeAction : AnAction("Tell Me") {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Tell Me") ?: return
        toolWindow.activate {
            val clipped = editor.document.text.take(OllamaConfig.MAX_CONTENT_LENGTH)

            fun withToolWindowInstance(triesLeft: Int, block: (TellMeToolWindow) -> Unit) {
                val tw = TellMeToolWindow.getInstance(project)
                if (tw != null) {
                    block(tw)
                    return
                }
                if (triesLeft <= 0) return
                ApplicationManager.getApplication().invokeLater {
                    withToolWindowInstance(triesLeft - 1, block)
                }
            }

            withToolWindowInstance(20) { tw ->
                tw.openOrCreateTabAndMaybeStart(
                    fileName = file.name,
                    filePath = file.path,
                    clipped = clipped
                )
            }
        }
    }
}
