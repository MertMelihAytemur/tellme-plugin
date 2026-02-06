package com.tellme.tellmeplugin.ui.session

import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages file analysis sessions for a project.
 * Handles session lifecycle and provides access to the current session.
 */
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

    /** All active sessions indexed by their unique key */
    private val sessions = LinkedHashMap<String, Session>()

    /** Currently selected session key */
    var currentKey: String? = null
        private set

    /**
     * Gets a session by its key.
     */
    fun getSession(key: String): Session? = sessions[key]

    /**
     * Gets the currently selected session.
     */
    fun getCurrentSession(): Session? = currentKey?.let { sessions[it] }

    /**
     * Gets all sessions.
     */
    fun getAllSessions(): Collection<Session> = sessions.values

    /**
     * Checks if a session exists for the given key.
     */
    fun hasSession(key: String): Boolean = sessions.containsKey(key)

    /**
     * Creates and registers a new session.
     */
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

    /**
     * Removes a session by its key.
     */
    fun removeSession(key: String) {
        sessions.remove(key)
        if (currentKey == key) {
            currentKey = null
        }
    }

    /**
     * Sets the currently selected session.
     */
    fun selectSession(key: String) {
        if (sessions.containsKey(key)) {
            currentKey = key
        }
    }

    /**
     * Clears the current selection.
     */
    fun clearSelection() {
        currentKey = null
    }

    /**
     * Clears all sessions.
     */
    fun clear() {
        sessions.clear()
        currentKey = null
    }
}
