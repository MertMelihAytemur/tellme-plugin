package com.tellme.tellmeplugin.ui.tab

import com.intellij.icons.AllIcons
import javax.swing.Icon

/**
 * Provides file type icons based on file extension.
 */
object TabIcons {

    /**
     * Gets the appropriate icon for a file based on its name.
     */
    fun getIconForFile(fileName: String): Icon {
        val extension = fileName.substringAfterLast('.', "").lowercase()

        return when (extension) {
            // JVM Languages
            "kt", "kts" -> AllIcons.FileTypes.Any_type // Kotlin uses Any_type in base IDE
            "java" -> AllIcons.FileTypes.Java

            // Web
            "xml" -> AllIcons.FileTypes.Xml
            "html", "htm" -> AllIcons.FileTypes.Html
            "css" -> AllIcons.FileTypes.Css
            "js", "mjs" -> AllIcons.FileTypes.JavaScript
            "ts" -> AllIcons.FileTypes.JavaScript // TypeScript shares JavaScript icon in base IDE
            "json" -> AllIcons.FileTypes.Json

            // Config & Text
            "yaml", "yml" -> AllIcons.FileTypes.Yaml
            "properties" -> AllIcons.FileTypes.Properties
            "md", "markdown" -> AllIcons.FileTypes.Text
            "txt" -> AllIcons.FileTypes.Text

            // Other
            "sql" -> AllIcons.FileTypes.Any_type
            "sh", "bash" -> AllIcons.FileTypes.Any_type
            "gradle" -> AllIcons.FileTypes.Any_type

            else -> AllIcons.FileTypes.Any_type
        }
    }
}
