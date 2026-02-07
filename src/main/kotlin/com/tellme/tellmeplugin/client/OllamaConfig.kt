package com.tellme.tellmeplugin.client

import java.time.Duration

object OllamaConfig {
    const val ENDPOINT = "http://localhost:11434/v1/messages"

    const val MODEL = "qwen2.5-coder:7b"

    const val MAX_CONTENT_LENGTH = 6_000

    const val MAX_TOKENS = 4096

    val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(10)

    val READ_TIMEOUT: Duration = Duration.ofMinutes(5)

    val WRITE_TIMEOUT: Duration = Duration.ofMinutes(5)

    val CALL_TIMEOUT: Duration = Duration.ofMinutes(5)

    enum class PromptType {
        EXPLAIN,
        REFACTOR
    }

    fun buildPrompt(fileName: String, fileContent: String, type: PromptType = PromptType.EXPLAIN): String {
        val clippedContent = if (fileContent.length > MAX_CONTENT_LENGTH) {
            fileContent.take(MAX_CONTENT_LENGTH)
        } else {
            fileContent
        }

        return when (type) {
            PromptType.EXPLAIN -> { """
                You are an experienced software engineer and code reviewer.
                   Analyze the following file in terms of its purpose and responsibility within the project.

                When relevant, briefly reference:
                - Clean Code principles (naming, readability, single responsibility)
                - SOLID principles
                - Clean Architecture (layers, boundaries, dependency direction)
                - Design Patterns (only if they genuinely apply)
                
                Produce the output in Markdown format.
                - Use `###` for headings
                - Use `-` for lists
                
                Follow this structure:
                
                ### 1) What does it do?
                Explain the file’s primary responsibility and role in the system.
                
                ### 2) Key components
                - Important classes, functions, or logic blocks
                - External dependencies or integrations
                - High-level data flow (input → processing → output)
                
                ### 3) Architecture & code quality assessment
                - Are responsibilities well separated? (SRP)
                - Are dependencies pointing in the right direction? (DIP / Clean Architecture)
                - Extensibility and testability (OCP, seams, abstractions)
                
                ### 4) Risks
                - Potential bug sources or edge cases
                - Performance or resource-usage concerns
                - Security or stability risks (if applicable)
                
                ### 5) Improvement suggestions
                - 3–6 high-impact, prioritized recommendations
                - Use small code snippets only if helpful
                
                File name: $fileName
                
                --- FILE ---
                $clippedContent
                """.trimIndent()
            }

            PromptType.REFACTOR -> { """
                You are an experienced software engineer acting as a technical lead.
                
                Refactor the following code **without changing its behavior**.
                Focus on:
                - Clean Code (readability, naming, small functions, reduced duplication)
                - SOLID principles (especially SRP, DIP, OCP where applicable)
                - Clean Architecture boundaries (if relevant)
                - Design Patterns only when they provide clear value (avoid overengineering)
                
                Produce the output in Markdown format and follow this order:
                
                ### Summary of changes
                - What was changed?
                - Why was it changed?
                - What problem does it solve?
                
                ### Refactored code
                - Provide the complete refactored code in a single block
                - Avoid unnecessary explanations inside the code
                - Do not break existing behavior or public APIs
                - Keep the solution minimal and pragmatic
                
                File name: $fileName
                
                --- FILE ---
                $clippedContent
                """.trimIndent()
            }
        }
    }
}
