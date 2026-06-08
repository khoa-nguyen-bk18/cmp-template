/**
 * Generates Compose compiler reports for :shared and writes a filtered view with
 * unstable / non-skippable composables listed first.
 *
 * Usage:
 *   ./gradlew composeCompilerReports
 */

data class ComposableBlock(
    val lines: List<String>,
    val header: String,
    val isSkippable: Boolean,
    val isRestartable: Boolean,
    val unstableParams: List<String>,
    val unknownStabilityParams: List<String>,
) {
    val hasUnstableParams: Boolean = unstableParams.isNotEmpty()
    val isNonSkippable: Boolean = isRestartable && !isSkippable
    val isUnstable: Boolean = isNonSkippable || hasUnstableParams || unknownStabilityParams.isNotEmpty()

    val sortKey: String =
        buildString {
            append(if (isNonSkippable) "0" else "1")
            append(if (hasUnstableParams) "0" else "1")
            append(if (unknownStabilityParams.isNotEmpty()) "0" else "1")
            append(header)
        }
}

fun isIndentedParameterLine(line: String): Boolean {
    if (!line.startsWith(" ") && !line.startsWith("\t")) return false
    val trimmed = line.trimStart()
    if (trimmed.startsWith(")")) return false
    return trimmed.startsWith("stable ") ||
        trimmed.startsWith("unstable ") ||
        trimmed.startsWith("runtime ") ||
        trimmed.startsWith("unused ") ||
        trimmed.startsWith("@static") ||
        trimmed.startsWith("@dynamic") ||
        trimmed.contains(":")
}

fun parseComposableBlocks(content: String): List<ComposableBlock> {
    val blocks = mutableListOf<ComposableBlock>()
    var currentLines = mutableListOf<String>()

    fun flush() {
        if (currentLines.isEmpty()) return
        val header = currentLines.first()
        val bodyLines = currentLines.drop(1).filter { it.trim().isNotEmpty() && !it.trimStart().startsWith(")") }
        val paramLines = bodyLines.filter { isIndentedParameterLine(it) }
        val unstableParams = paramLines.filter { it.trimStart().startsWith("unstable ") }
        val unknownStabilityParams =
            paramLines.filter { line ->
                val trimmed = line.trimStart()
                !trimmed.startsWith("stable ") &&
                    !trimmed.startsWith("unstable ") &&
                    !trimmed.startsWith("runtime ") &&
                    !trimmed.startsWith("unused ")
            }
        blocks +=
            ComposableBlock(
                lines = currentLines.toList(),
                header = header,
                isSkippable = header.contains(" skippable") || header.startsWith("skippable"),
                isRestartable = header.contains("restartable"),
                unstableParams = unstableParams,
                unknownStabilityParams = unknownStabilityParams,
            )
        currentLines = mutableListOf()
    }

    content.lineSequence().forEach { line ->
        if (line.isBlank()) return@forEach
        val isHeader = line.contains(" fun ") && !isIndentedParameterLine(line)
        if (isHeader && currentLines.isNotEmpty()) {
            flush()
        }
        currentLines.add(line)
    }
    flush()
    return blocks
}

fun extractFunctionSignature(header: String): String =
    header.substringAfter(" fun ").substringBefore("(").trim()

fun writeStabilitySummary(
    blocks: List<ComposableBlock>,
    sourceFile: java.io.File,
    outputFile: java.io.File,
) {
    val nonSkippable = blocks.filter { it.isNonSkippable }.sortedBy { it.header }
    val unstableParamsOnly =
        blocks
            .filter { !it.isNonSkippable && it.hasUnstableParams }
            .sortedBy { it.header }
    val unknownStabilityOnly =
        blocks
            .filter { !it.isNonSkippable && !it.hasUnstableParams && it.unknownStabilityParams.isNotEmpty() }
            .sortedBy { it.header }
    val stable =
        blocks
            .filter { !it.isUnstable }
            .sortedBy { it.header }

    outputFile.parentFile.mkdirs()
    outputFile.bufferedWriter().use { writer ->
        writer.appendLine("Compose Compiler Stability Summary")
        writer.appendLine("Source: ${sourceFile.relativeTo(rootProject.projectDir)}")
        writer.appendLine("Total composables: ${blocks.size}")
        writer.appendLine()

        writer.appendLine("=== Non-skippable composables (${nonSkippable.size}) ===")
        if (nonSkippable.isEmpty()) {
            writer.appendLine("(none)")
        } else {
            nonSkippable.forEach { block ->
                writer.appendLine(extractFunctionSignature(block.header))
                block.unstableParams.forEach { writer.appendLine("  - $it") }
                block.unknownStabilityParams.forEach { writer.appendLine("  - [unknown stability] $it") }
            }
        }
        writer.appendLine()

        writer.appendLine("=== Composables with unstable parameters (${unstableParamsOnly.size}) ===")
        if (unstableParamsOnly.isEmpty()) {
            writer.appendLine("(none)")
        } else {
            unstableParamsOnly.forEach { block ->
                writer.appendLine(extractFunctionSignature(block.header))
                block.unstableParams.forEach { writer.appendLine("  - $it") }
            }
        }
        writer.appendLine()

        writer.appendLine(
            "=== Composables with unknown-stability parameters (${unknownStabilityOnly.size}) ===",
        )
        if (unknownStabilityOnly.isEmpty()) {
            writer.appendLine("(none)")
        } else {
            unknownStabilityOnly.forEach { block ->
                writer.appendLine(extractFunctionSignature(block.header))
                block.unknownStabilityParams.forEach { writer.appendLine("  - $it") }
            }
        }
        writer.appendLine()

        writer.appendLine("=== Fully stable composables (${stable.size}) ===")
        stable.forEach { block ->
            writer.appendLine(extractFunctionSignature(block.header))
        }
    }
}

fun writeSortedComposableReport(
    blocks: List<ComposableBlock>,
    outputFile: java.io.File,
) {
    val sorted = blocks.sortedBy { it.sortKey }
    outputFile.parentFile.mkdirs()
    outputFile.bufferedWriter().use { writer ->
        writer.appendLine("# Unstable and non-skippable composables first")
        writer.appendLine("# Generated by ./gradlew composeCompilerReports")
        writer.appendLine()
        sorted.forEach { block ->
            block.lines.forEach { writer.appendLine(it) }
            writer.appendLine()
        }
    }
}

val generateComposeCompilerReports =
    tasks.register("generateComposeCompilerReports") {
        group = "compose"
        description = "Compiles :shared to emit Compose compiler reports under build/compose_compiler/"
        dependsOn(":shared:compileAndroidMain")
        notCompatibleWithConfigurationCache("Compose compiler report generation is an on-demand diagnostic task")
    }

val filterUnstableComposables =
    tasks.register("filterUnstableComposables") {
        group = "compose"
        description = "Parses composables.txt and writes unstable-first summary and sorted reports"
        dependsOn(generateComposeCompilerReports)
        notCompatibleWithConfigurationCache("Reads Compose compiler report files at execution time")

        val sharedProject = project(":shared")
        val reportDir = sharedProject.layout.buildDirectory.dir("compose_compiler")
        val summaryOutput = reportDir.map { it.file("unstable-composables-summary.txt") }
        val sortedOutput = reportDir.map { it.file("unstable-composables-first.txt") }

        doLast {
            val dir = reportDir.get().asFile
            require(dir.isDirectory) {
                "Compose compiler report directory not found: ${dir.path}. Run generateComposeCompilerReports first."
            }

            val composablesFile =
                dir
                    .listFiles()
                    ?.filter { it.isFile && it.name.endsWith("-composables.txt") }
                    ?.minByOrNull { it.name.length }
                    ?: error("No *-composables.txt file found in ${dir.path}")

            val blocks = parseComposableBlocks(composablesFile.readText())
            val unstableCount = blocks.count { it.isUnstable }

            writeStabilitySummary(blocks, composablesFile, summaryOutput.get().asFile)
            writeSortedComposableReport(blocks, sortedOutput.get().asFile)

            logger.lifecycle(
                """
                |
                |Compose compiler reports generated:
                |  Source:  ${composablesFile.relativeTo(rootProject.projectDir)}
                |  Summary: ${summaryOutput.get().asFile.relativeTo(rootProject.projectDir)}
                |  Sorted:  ${sortedOutput.get().asFile.relativeTo(rootProject.projectDir)}
                |
                |Found $unstableCount unstable/non-skippable composable(s) out of ${blocks.size} total.
                """.trimMargin(),
            )
        }
    }

tasks.register("composeCompilerReports") {
    group = "compose"
    description = "Generates Compose compiler reports and lists unstable composables first"
    dependsOn(filterUnstableComposables)
    notCompatibleWithConfigurationCache("Compose compiler report generation is an on-demand diagnostic task")
}
