package com.duxtinto.kdiff

class DiffLine {
    enum class Type {
        DIFF_COMMAND,
        FROM_FILE,
        TO_FILE,
        HUNK_HEADER,
        HUNK_LINE_COMMON,
        HUNK_LINE_FROM,
        HUNK_LINE_TO
    }
}