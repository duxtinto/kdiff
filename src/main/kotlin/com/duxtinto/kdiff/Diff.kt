package com.duxtinto.kdiff

data class Diff(
        val from: String? = null,
        val to: String? = null,
        val hunks: MutableList<DiffHunk> = mutableListOf()
)