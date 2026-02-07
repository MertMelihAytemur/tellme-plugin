package com.tellme.tellmeplugin.ui.session


data class Session(
    val key: String,
    val fileName: String,
    val filePath: String,
    val clipped: String,
    var state: UiState = UiState.IDLE,
    var requestId: Long = 0L,
    val buffer: StringBuilder = StringBuilder(),
    var showCaret: Boolean = false,
    var hasToken: Boolean = false,
    var loadingStartedAtMs: Long = 0L,
    var lastTokenAtMs: Long = 0L,
    var lastPromptType: com.tellme.tellmeplugin.client.OllamaConfig.PromptType? = null
) {
    companion object {
        fun generateKey(filePath: String, clipped: String): String {
            return "${filePath}#${clipped.hashCode()}#${clipped.length}"
        }
    }
}
