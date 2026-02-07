package com.tellme.tellmeplugin.client

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object OllamaClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(OllamaConfig.CONNECT_TIMEOUT)
        .readTimeout(OllamaConfig.READ_TIMEOUT)
        .writeTimeout(OllamaConfig.WRITE_TIMEOUT)
        .callTimeout(OllamaConfig.CALL_TIMEOUT)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun checkStatus(): Pair<Boolean, Boolean> {
        val running = isOllamaRunning()
        if (!running) return false to false
        
        val modelReady = isModelDownloaded(OllamaConfig.MODEL)
        return true to modelReady
    }

    private fun isOllamaRunning(): Boolean {
        val request = Request.Builder()
            .url("http://localhost:11434/api/tags")
            .get()
            .build()
        
        return try {
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    private fun isModelDownloaded(modelName: String): Boolean {
        val request = Request.Builder()
            .url("http://localhost:11434/api/tags")
            .get()
            .build()
            
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return false
                val body = response.body?.string() ?: return false
                body.contains("\"name\":\"$modelName\"") || body.contains("\"name\":\"${modelName.substringBefore(":")}\"")
            }
        } catch (_: Exception) {
            false
        }
    }

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

    private fun jsonString(s: String): String =
        "\"" + s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\""

    fun explainFileStream(
        fileName: String,
        fileContent: String,
        promptType: OllamaConfig.PromptType = OllamaConfig.PromptType.EXPLAIN,
        onToken: (String) -> Unit
    ) {
        val prompt = OllamaConfig.buildPrompt(fileName, fileContent, promptType)

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
                    
                    if (data.contains("\"content_block_delta\"") || data.contains("\"text\"")) {
                        val chunk = extractDeltaText(data)
                        if (!chunk.isNullOrEmpty()) onToken(chunk)
                    }
                }
            }
        }
    }
}
