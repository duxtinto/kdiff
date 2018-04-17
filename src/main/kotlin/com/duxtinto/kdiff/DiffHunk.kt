package com.duxtinto.kdiff

data class DiffHunk (
        val from: Part = Part(),
        val to: Part = Part()
) {
    data class Part(
            val start: Int = 0,
            val count: Int = 0,
            val lines: MutableMap<Int, String> = mutableMapOf()
    )
}