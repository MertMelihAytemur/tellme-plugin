package com.tellme.tellmeplugin.ui.session

/**
 * Represents a single file analysis session.
 * Each tab in the tool window corresponds to one session.
 */
data class Session(
    /** Unique identifier for this session (based on file path and content hash) */
    val key: String,
    /** Display name of the file being analyzed */
    val fileName: String,
    /** Full path to the file */
    val filePath: String,
    /** Clipped content of the file (first N characters) */
    val clipped: String,
    /** Current UI state of the session */
    var state: UiState = UiState.IDLE,
    /** Request ID for tracking streaming responses */
    var requestId: Long = 0L,
    /** Accumulated response buffer */
    val buffer: StringBuilder = StringBuilder(),
    /** Whether to show typing caret during streaming */
    var showCaret: Boolean = false,
    /** Whether any token has been received */
    var hasToken: Boolean = false,
    /** Timestamp when loading started */
    var loadingStartedAtMs: Long = 0L,
    /** Timestamp of last received token */
    var lastTokenAtMs: Long = 0L,
    /** Last requested prompt type */
    var lastPromptType: com.tellme.tellmeplugin.client.OllamaConfig.PromptType? = null
) {
    companion object {
        /**
         * Generates a unique session key based on file path and content.
         */
        fun generateKey(filePath: String, clipped: String): String {
            return "${filePath}#${clipped.hashCode()}#${clipped.length}"
        }
    }
}
