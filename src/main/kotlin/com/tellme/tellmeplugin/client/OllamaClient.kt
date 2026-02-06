package com.tellme.tellmeplugin.client

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * HTTP client for streaming responses from Ollama API.
 */
object OllamaClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(OllamaConfig.CONNECT_TIMEOUT)
        .readTimeout(OllamaConfig.READ_TIMEOUT)
        .writeTimeout(OllamaConfig.WRITE_TIMEOUT)
        .callTimeout(OllamaConfig.CALL_TIMEOUT)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Extracts text from Anthropic-style SSE event (content_block_delta).
     */
    private fun extractDeltaText(json: String): String? {
        val key = "\"text\":\""
        val start = json.indexOf(key)
        if (start == -1) return null

        var i = start + key.length
        val sb = StringBuilder()

        while (i < json.length) {
            val c = json[i]
            if (c == '"' && json[i - 1] != '\\') break
            sb.append(c)
            i++
        }

        return sb.toString()
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    /**
     * Escapes a string for JSON encoding.
     */
    private fun jsonString(s: String): String =
        "\"" + s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\""

    /**
     * Streams file analysis using Anthropic-compatible API.
     */
    fun explainFileStream(
        fileName: String,
        fileContent: String,
        promptType: OllamaConfig.PromptType = OllamaConfig.PromptType.EXPLAIN,
        onToken: (String) -> Unit
    ) {
        val prompt = OllamaConfig.buildPrompt(fileName, fileContent, promptType)

        // Anthropic Messages API format
        val body = """
        {
          "model": "${OllamaConfig.MODEL}",
          "messages": [
            {
              "role": "user",
              "content": ${jsonString(prompt)}
            }
          ],
          "max_tokens": ${OllamaConfig.MAX_TOKENS},
          "stream": true
        }
    """.trimIndent()

        val request = Request.Builder()
            .url(OllamaConfig.ENDPOINT)
            .post(body.toRequestBody(jsonMediaType))
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                onToken("Error: HTTP ${response.code}\n")
                onToken(response.body?.string().orEmpty())
                return
            }

            val source = response.body?.source() ?: return
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.startsWith("data: ")) {
                    val data = line.substring(6)
                    if (data == "[DONE]") break
                    
                    // Look for content_block_delta events
                    if (data.contains("\"content_block_delta\"") || data.contains("\"text\"")) {
                        val chunk = extractDeltaText(data)
                        if (!chunk.isNullOrEmpty()) onToken(chunk)
                    }
                }
            }
        }
    }
}
