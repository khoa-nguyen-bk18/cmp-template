package com.devindie.vaulty.domain.model.index

internal fun matchPathWithDoubleStar(pathSegments: List<String>, patternSegments: List<String>): Boolean = when {
    patternSegments.isEmpty() -> pathSegments.isEmpty()
    patternSegments.size == 1 && patternSegments[0] == "**" -> true
    else -> matchPathSegmentsIteratively(pathSegments, patternSegments)
}

@Suppress("ReturnCount")
private fun matchPathSegmentsIteratively(pathSegments: List<String>, patternSegments: List<String>): Boolean {
    var pathIndex = 0
    var patIndex = 0
    while (patIndex < patternSegments.size) {
        val segment = patternSegments[patIndex]
        when {
            segment == "**" && patIndex == patternSegments.lastIndex -> return true
            segment == "**" ->
                return matchDoubleStarMidPath(pathSegments, pathIndex, patternSegments, patIndex)
            pathIndex >= pathSegments.size -> return false
            !matchGlob(pathSegments[pathIndex], segment) -> return false
            else -> {
                pathIndex++
                patIndex++
            }
        }
    }
    return pathIndex == pathSegments.size
}

private fun matchDoubleStarMidPath(
    pathSegments: List<String>,
    pathIndex: Int,
    patternSegments: List<String>,
    patIndex: Int,
): Boolean {
    val remaining = patternSegments.subList(patIndex + 1, patternSegments.size)
    return (pathIndex..pathSegments.size).any { index ->
        matchPathWithDoubleStar(pathSegments.subList(index, pathSegments.size), remaining)
    }
}

internal fun matchGlob(text: String, pattern: String): Boolean = when {
    pattern == "**" -> true
    !pattern.contains('*') -> text == pattern
    else -> matchGlobWithWildcards(text, pattern)
}

private fun matchGlobWithWildcards(text: String, pattern: String): Boolean {
    val parts = pattern.split('*')
    if (parts.size == 1) return text == pattern

    var index = 0
    var valid = true
    for (i in parts.indices) {
        if (valid) {
            val part = parts[i]
            if (part.isNotEmpty()) {
                val found = text.indexOf(part, index)
                val partMatches = found >= 0 && !(i == 0 && !pattern.startsWith("*") && found != 0)
                if (partMatches) {
                    index = found + part.length
                } else {
                    valid = false
                }
            }
        }
    }
    return valid && (pattern.endsWith("*") || index == text.length)
}
