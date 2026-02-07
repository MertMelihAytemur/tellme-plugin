package com.tellme.tellmeplugin.ui.tab

import com.intellij.icons.AllIcons
import javax.swing.Icon

object TabIcons {

    fun getIconForFile(fileName: String): Icon {
        val extension = fileName.substringAfterLast('.', "").lowercase()

        return when (extension) {
            "kt", "kts" -> AllIcons.FileTypes.Any_type
            "java" -> AllIcons.FileTypes.Java

            "xml" -> AllIcons.FileTypes.Xml
            "html", "htm" -> AllIcons.FileTypes.Html
            "css" -> AllIcons.FileTypes.Css
            "js", "mjs" -> AllIcons.FileTypes.JavaScript
            "ts" -> AllIcons.FileTypes.JavaScript
            "json" -> AllIcons.FileTypes.Json

            "yaml", "yml" -> AllIcons.FileTypes.Yaml
            "properties" -> AllIcons.FileTypes.Properties
            "md", "markdown" -> AllIcons.FileTypes.Text
            "txt" -> AllIcons.FileTypes.Text

            "sql" -> AllIcons.FileTypes.Any_type
            "sh", "bash" -> AllIcons.FileTypes.Any_type
            "gradle" -> AllIcons.FileTypes.Any_type

            else -> AllIcons.FileTypes.Any_type
        }
    }
}
