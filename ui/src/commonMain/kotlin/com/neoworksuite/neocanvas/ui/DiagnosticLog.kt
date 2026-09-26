package com.neoworksuite.neocanvas.ui

/** A small, local-only persistent event trail for TestFlight fault reports. */
internal class DiagnosticLog(
    initial: String = "",
    private val persist: (String) -> Unit = {},
) {
    private val entries = initial.lineSequence()
        .map(::clean)
        .filter(String::isNotBlank)
        .toList()
        .takeLast(MAX_ENTRIES)
        .toMutableList()

    val text: String
        get() = entries.joinToString("\n")

    fun append(message: String) {
        val entry = clean(message)
        if (entry.isBlank()) return
        entries += entry
        while (entries.size > MAX_ENTRIES || entries.sumOf { it.length + 1 } > MAX_CHARACTERS) {
            entries.removeFirst()
        }
        persist(text)
    }

    fun clear() {
        entries.clear()
        entries += "Diagnostic log cleared"
        persist(text)
    }

    companion object {
        const val MAX_ENTRIES = 200
        private const val MAX_CHARACTERS = 24_000

        private fun clean(value: String): String = value
            .replace('\n', ' ')
            .replace('\r', ' ')
            .trim()
    }
}
