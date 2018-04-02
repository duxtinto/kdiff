package com.duxtinto.kdiff

internal fun String.matches(lineType: DiffLine.Type): Boolean {
    return when (lineType) {
        DiffLine.Type.DIFF_COMMAND -> this.matches(Regex("^diff --git(.*)"))
        DiffLine.Type.FROM_FILE -> this.matches(Regex("^--- (.*)"))
        DiffLine.Type.TO_FILE -> this.matches(Regex("""^\+\+\+ (.*)"""))
        DiffLine.Type.HUNK_HEADER -> this.matches(Regex("""@@ -\d*(,\d*)? \+\d*(,\d*)? @@"""))
        DiffLine.Type.HUNK_LINE_COMMON -> this.matches(Regex("^ (.*)"))
        DiffLine.Type.HUNK_LINE_FROM -> this.matches(Regex("^-(.*)"))
        DiffLine.Type.HUNK_LINE_TO -> this.matches(Regex("^+(.*)"))
    }
}
