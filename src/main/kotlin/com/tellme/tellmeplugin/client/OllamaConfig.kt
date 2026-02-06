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

    /**
     * Types of prompts supported by the plugin.
     */
    enum class PromptType {
        EXPLAIN,
        REFACTOR
    }

    /** System prompt template for code analysis */
    fun buildPrompt(fileName: String, fileContent: String, type: PromptType = PromptType.EXPLAIN): String {
        val clippedContent = if (fileContent.length > MAX_CONTENT_LENGTH) {
            fileContent.take(MAX_CONTENT_LENGTH)
        } else {
            fileContent
        }

        return when (type) {
            PromptType.EXPLAIN -> """
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

            PromptType.REFACTOR -> """
                Sen bir Android/Kotlin uzmanısın.
                Aşağıdaki kodu daha temiz, performanslı ve Kotlin best practice'lerine uygun şekilde refactor et.
                Önce yapılan değişikliklerin kısa bir özetini ver, sonra refactor edilmiş kodun tamamını bir kerede paylaş.
                Çıktıyı Markdown olarak yaz.

                Dosya adı: $fileName

                --- DOSYA ---
                $clippedContent
            """.trimIndent()
        }
    }
}
