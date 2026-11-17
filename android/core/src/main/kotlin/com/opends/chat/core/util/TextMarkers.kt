package com.opends.chat.core.util

internal object TextMarkers {
    private val citationMark = Regex("""\[citation:\d+\]""")
    private val referenceMark = Regex("""\[reference:\d+\]""")

    fun stripCitations(text: String): String = citationMark.replace(text, "")

    fun stripReferences(text: String): String = referenceMark.replace(text, "")

    fun stripAll(text: String): String = stripReferences(stripCitations(text))
}
