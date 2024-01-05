package aoc2023.day12

import aoc2023.prev.readInput
import java.math.BigDecimal
import kotlin.time.measureTimedValue

sealed class Spring {
    abstract val char: String

    object Operational : Spring() {
        override val char = "."
    }

    object Damaged : Spring() {
        override val char = "#"
    }

    object Unknown : Spring() {
        override val char = "?"
    }

    override fun toString(): String {
        return char
    }
}

data class Row(val springs: List<Spring>, val config: List<Int>) {
    override fun toString(): String {
        return springs.joinToString("") + " " + config.joinToString(",")
    }

    fun unfold(): Row {
        return Row(
            buildList {
                addAll(springs)
                add(Spring.Unknown)
                addAll(springs)
//                add(Spring.Unknown)
//                addAll(springs)
//                add(Spring.Unknown)
//                addAll(springs)
//                add(Spring.Unknown)
//                addAll(springs)

            },
            buildList {
                addAll(config)
                addAll(config)
//                addAll(config)
//                addAll(config)
//                addAll(config)
            }
        )
    }
}

class Solver {
    fun solve(row: Row): Long {
        val unknownIndex = row.springs.indexOfFirst { it == Spring.Unknown }
        if (unknownIndex == -1) {
            return if (getCounts(row.springs) == row.config) {
                1
            } else {
                0
            }
        } else {
            return solve(row, unknownIndex, Spring.Damaged) + solve(row, unknownIndex, Spring.Operational)
        }
    }

    private fun canSolve(row: Row): Boolean {
        val firstUnknown = row.springs.indexOfFirst { it == Spring.Unknown }
        if (firstUnknown == -1) {
            return true
        } else {
            val counts = getCounts(row.springs.subList(0, firstUnknown))
            val lastIndex = minOf(counts.size, row.config.size) - 1
            for (i in 0..<lastIndex - 1) {
                if (counts[i] != row.config[i]) {
                    return false
                }
            }
            if (counts.isNotEmpty() && row.config.isNotEmpty()) {
                return counts[lastIndex] <= row.config[lastIndex]
            }
            return true
        }
    }

    fun getCounts(springs: List<Spring>): List<Int> {
        val counts = mutableMapOf<Int, Int>()
        var i = -1
        var prev: Spring? = null
        for (spring in springs) {
            if (spring is Spring.Damaged) {
                if (prev == null || prev is Spring.Operational) {
                    i++
                }
                counts.compute(i) { _, v -> (v ?: 0) + 1 }
            }
            prev = spring
        }
        return counts.values.toList()
    }

    fun solve(row: Row, firstUnknown: Int, replacement: Spring): Long {
        val newSprings = row.springs.toMutableList()
        newSprings[firstUnknown] = replacement
        val newRow = row.copy(springs = newSprings)
        return if (canSolve(newRow)) {
            solve(newRow)
        } else {
            0
        }
    }

//    fun solveFor(row: Row, spring: Spring): Row {
//        require(spring == Spring.Operational || spring == Spring.Damaged)
//        val firstUnkmown = row.springs.indexOfFirst { it == Spring.Unknown }
//        when (spring) {
//            is Spring.Operational -> {
//                val newHeadValue = row.config.first()
//                val newConfig = if (newHeadValue == 1) {
//                    row.config.drop(1)
//                } else {
//                    row.config.mapIndexed { index, i -> if (index == 0) (i - 1) else i }
//                }
//                if (newHeadValue > 1) {
//                    return solveFor(Row(row.springs.drop(1), newConfig), Spring.Operational)
//                } else {
//
//                }
//            }
//        }
//    }
}

fun main() {
    val day = "12"

    fun parseSpringLine(line: String): Row {
        val (rowStr, damagedStr) = line.split(" ")
        val contDamageSprings = damagedStr.split(",").map { it.toInt() }
        val row = rowStr.map {
            when (it) {
                '#' -> Spring.Damaged
                '.' -> Spring.Operational
                '?' -> Spring.Unknown
                else -> throw Exception("Unknown spring type: $it")
            }
        }

        return Row(row, contDamageSprings)
    }

    fun solve(testInput: List<String>): Any? {
        var sum = BigDecimal.ZERO
        for ((i, line) in testInput.withIndex()) {
            val row = parseSpringLine(line)
            println("$i: Solving: $row")
            val solver = Solver()
            val result1 = solver.solve(row)
            val result2 = solver.solve(row.unfold())
            sum += (result2.toBigDecimal() / result1.toBigDecimal()).pow(4) * result1.toBigDecimal()
//            println("$result1 $result2")
//            if (i > 10) {
//                break
//            }
        }
        return sum
//        95164357883
//        21221421325320
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

    val testResult = solve(testInput)
    val testResultExpected = ""
    println("(Test) Part 1: expected: $testResultExpected, got: $testResult")

    val input = readInput("Day${day}")
    val resultReal = measureTimedValue { solve(input) }
    println("(Real) Part 1: 7173 $resultReal")
}

data class Around<T>(val prev: T?, val current: T, val next: T?)

fun <T> List<T>.around(index: Int): Around<T> {
    val prev = if (index == 0) null else this[index - 1]
    val next = if (index == this.size - 1) null else this[index + 1]
    return Around(prev, this[index], next)
}
