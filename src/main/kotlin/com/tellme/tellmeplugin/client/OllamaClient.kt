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
     * Extracts the "response" field from Ollama's streaming JSON.
     */
    private fun extractResponse(json: String): String? {
        val key = "\"response\":\""
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
     * Streams file analysis from Ollama.
     *
     * @param fileName Name of the file being analyzed
     * @param fileContent Content of the file (will be clipped if too long)
     * @param onToken Callback for each received token
     */
    fun explainFileStream(
        fileName: String,
        fileContent: String,
        onToken: (String) -> Unit
    ) {
        val prompt = OllamaConfig.buildPrompt(fileName, fileContent)

        val body = """
        {
          "model": "${OllamaConfig.MODEL}",
          "prompt": ${jsonString(prompt)},
          "stream": true
        }
    """.trimIndent()

        val request = Request.Builder()
            .url(OllamaConfig.ENDPOINT)
            .post(body.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                onToken("Ollama error: HTTP ${response.code}\n")
                onToken(response.body?.string().orEmpty())
                return
            }

            val source = response.body?.source() ?: return
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                val chunk = extractResponse(line)
                if (!chunk.isNullOrEmpty()) onToken(chunk)
                if (line.contains("\"done\":true")) break
            }
        }
    }
}
