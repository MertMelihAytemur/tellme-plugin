package com.tellme.tellmeplugin.ui.tab

import com.intellij.openapi.project.Project
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.tabs.JBTabsFactory
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.TabsListener
import com.tellme.tellmeplugin.ui.session.Session
import javax.swing.JComponent
import javax.swing.JPanel

class TabManager(
    project: Project,
    parentDisposable: com.intellij.openapi.Disposable,
    private val onTabSelected: (String?) -> Unit
) {
    private val tabs: JBTabs = JBTabsFactory.createTabs(project, parentDisposable)

    private val tabInfoByKey = HashMap<String, TabInfo>()
    private val tabKeyByInfo = HashMap<TabInfo, String>()

    init {
        tabs.addListener(object : TabsListener {
            override fun selectionChanged(oldSelection: TabInfo?, newSelection: TabInfo?) {
                val key = if (newSelection != null) tabKeyByInfo[newSelection] else null
                onTabSelected(key)
            }
        })
    }

    /**
     * Gets the tabs component for embedding in UI.
     */
    fun getComponent(): JComponent = tabs.component

    /**
     * Adds a new tab for a session.
     */
    fun addTab(session: Session, onClose: (String) -> Unit) {
        val tabInfo = TabInfo(JPanel())
        tabInfo.setText(session.fileName)
        tabInfo.setIcon(TabIcons.getIconForFile(session.fileName))

        tabs.addTab(tabInfo)

        tabInfoByKey[session.key] = tabInfo
        tabKeyByInfo[tabInfo] = session.key
    }

    /**
     * Selects a tab by session key.
     */
    fun selectTab(key: String) {
        val tabInfo = tabInfoByKey[key] ?: return
        tabs.select(tabInfo, false)
    }

    /**
     * Removes a tab by session key.
     */
    fun removeTab(key: String) {
        val tabInfo = tabInfoByKey.remove(key) ?: return
        tabKeyByInfo.remove(tabInfo)
        tabs.removeTab(tabInfo)
    }
}
