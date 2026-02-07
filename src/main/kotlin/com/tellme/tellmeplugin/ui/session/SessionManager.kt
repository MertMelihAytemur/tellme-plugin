package com.tellme.tellmeplugin.ui.session

import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

class SessionManager {

    companion object {
        private val instances = ConcurrentHashMap<Project, SessionManager>()

        fun getInstance(project: Project): SessionManager? = instances[project]

        fun getOrCreate(project: Project): SessionManager {
            return instances.getOrPut(project) { SessionManager() }
        }

        fun unregister(project: Project) {
            instances.remove(project)
        }
    }

    private val sessions = LinkedHashMap<String, Session>()

    var currentKey: String? = null
        private set

    fun getSession(key: String): Session? = sessions[key]

    fun getCurrentSession(): Session? = currentKey?.let { sessions[it] }

    fun getAllSessions(): Collection<Session> = sessions.values

    fun hasSession(key: String): Boolean = sessions.containsKey(key)

    fun createSession(fileName: String, filePath: String, clipped: String): Session {
        val key = Session.generateKey(filePath, clipped)
        val session = Session(
            key = key,
            fileName = fileName,
            filePath = filePath,
            clipped = clipped,
            state = UiState.LOADING
        )
        sessions[key] = session
        return session
    }

    fun removeSession(key: String) {
        sessions.remove(key)
        if (currentKey == key) {
            currentKey = null
        }
    }

    fun selectSession(key: String) {
        if (sessions.containsKey(key)) {
            currentKey = key
        }
    }

    fun clearSelection() {
        currentKey = null
    }

    fun clear() {
        sessions.clear()
        currentKey = null
    }
}
