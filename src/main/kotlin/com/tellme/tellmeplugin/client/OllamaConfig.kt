package com.tellme.tellmeplugin.client

import java.time.Duration

/**
 * Configuration for Ollama API client.
 * Centralizes all hardcoded values for easy modification.
 */
object OllamaConfig {
    /** Ollama API endpoint */
    const val ENDPOINT = "http://localhost:11434/api/generate"

    /** Model to use for code analysis */
    const val MODEL = "qwen2.5-coder:7b"

    /** Maximum content length to send (characters) */
    const val MAX_CONTENT_LENGTH = 6_000

    /** Connection timeout */
    val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(10)

    /** Read timeout for streaming responses */
    val READ_TIMEOUT: Duration = Duration.ofMinutes(5)

    /** Write timeout */
    val WRITE_TIMEOUT: Duration = Duration.ofMinutes(5)

    /** Call timeout */
    val CALL_TIMEOUT: Duration = Duration.ofMinutes(5)

    /** System prompt template for code analysis */
    fun buildPrompt(fileName: String, fileContent: String): String {
        val clippedContent = if (fileContent.length > MAX_CONTENT_LENGTH) {
            fileContent.take(MAX_CONTENT_LENGTH)
        } else {
            fileContent
        }

        return """
        Sen bir Android/Kotlin code reviewer'sın.
        Aşağıdaki dosyayı projedeki rolü açısından açıkla.
        Çıktıyı Markdown olarak yaz. Başlıklar ###, listeler '-' ile olsun.
        Format:
        1) Ne yapıyor?
        2) Önemli parçalar
        3) Riskler
        4) İyileştirme

        Dosya adı: $fileName

        --- DOSYA ---
        $clippedContent
    """.trimIndent()
    }
}
