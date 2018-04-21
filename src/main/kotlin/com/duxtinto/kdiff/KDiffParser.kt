package com.duxtinto.kdiff

class KDiffParser {
    fun parse(diffs: String): List<Diff> {
        val parsedDiffs = mutableListOf<Diff>()
        var currentParsedDiff: Diff? = null
        var currentParsedHunk: DiffHunk? = null
        var numLine = 0

        for (line in diffs.lines()) {
            when {
                line.matches(DiffLine.Type.DIFF_COMMAND) -> {
                    if (currentParsedDiff != null) {
                        if (currentParsedHunk != null) {
                            currentParsedDiff.hunks.add(currentParsedHunk)
                            currentParsedHunk = null
                        }

                        parsedDiffs.add(currentParsedDiff)
                    }

                    currentParsedDiff = Diff()
                    numLine = 0
                }

                line.matches(DiffLine.Type.FROM_FILE) -> {
                    currentParsedDiff = currentParsedDiff!!.copy(from = parseFromFile(line))
                }

                line.matches(DiffLine.Type.TO_FILE) -> {
                    currentParsedDiff = currentParsedDiff!!.copy(to = parseToFile(line))
                }

                currentParsedDiff != null && line.matches(DiffLine.Type.HUNK_HEADER) -> {
                    if (currentParsedHunk != null) {
                        currentParsedDiff.hunks.add(currentParsedHunk)
                    }

                    currentParsedHunk = createHunkFromHeader(line)
                    numLine++
                }

                currentParsedHunk != null && line.matches(DiffLine.Type.HUNK_LINE_COMMON) -> {
                    with (parseHunkline(line)!!) {
                        currentParsedHunk.from.lines.put(numLine, this)
                        currentParsedHunk.to.lines.put(numLine, this)
                    }
                    numLine++
                }

                currentParsedHunk != null && line.matches(DiffLine.Type.HUNK_LINE_FROM) -> {
                    currentParsedHunk.from.lines.put(numLine, parseHunkline(line)!!)
                    numLine++
                }

                currentParsedHunk != null && line.matches(DiffLine.Type.HUNK_LINE_TO) -> {
                    currentParsedHunk.to.lines.put(numLine, parseHunkline(line)!!)
                    numLine++
                }
            }
        }

        if (currentParsedDiff != null) {
            if (currentParsedHunk != null) {
                currentParsedDiff.hunks.add(currentParsedHunk)
            }
            parsedDiffs.add(currentParsedDiff)
        }

        return parsedDiffs
    }

    private fun parseFromFile(line: String): String? {
        return Regex("--- a/(.*)").matchEntire(line)?.groups?.get(1)?.value
    }

    private fun parseToFile(line: String): String? {
        return Regex("""\+\+\+ b/(.*)""").matchEntire(line)?.groups?.get(1)?.value
    }

    private fun createHunkFromHeader(line: String): DiffHunk {
        val matches = Regex("""@@ -(?<fromStart>\d*)(,(?<fromCount>\d*))? \+(?<toStart>\d*)(,(?<toCount>\d*))? @@""").matchEntire(line)?.groups
                ?: return DiffHunk()
        return DiffHunk(
                from = DiffHunk.Part(
                        matches.get("fromStart")?.value?.toInt() ?: 0,
                        matches.get("fromCount")?.value?.toInt() ?: 0),
                to = DiffHunk.Part(
                        matches.get("toStart")?.value?.toInt() ?: 0,
                        matches.get("toCount")?.value?.toInt() ?: 0)
        )
    }

    private fun parseHunkline(line: String): String? {
        return Regex("""[-+ ](.*)""").matchEntire(line)?.groups?.get(1)?.value
    }
}
