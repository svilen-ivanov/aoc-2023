package aoc2023.prev

import kotlinx.coroutines.runBlocking

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

data class Row(val springs: List<Spring>, val contGroupDamaged: List<Int>) {
    override fun toString(): String {
        return springs.joinToString("") + " " + contGroupDamaged.joinToString(",")
    }

    val damagedByIndex = contGroupDamaged.withIndex().associateBy({ (i) -> i }) { (_, v) -> v }

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
                addAll(contGroupDamaged)
                addAll(contGroupDamaged)
//                addAll(contGroupDamaged)
//                addAll(contGroupDamaged)
//                addAll(contGroupDamaged)
            }
        )
    }
}

sealed class Outcome {
    data object Continue : Outcome()
    data object Stop : Outcome()
    data object Solution : Outcome()
}

class Solve() {
    fun solve(row: Row): Long {
//        println("Solving row: $row")
        val i = row.springs.indexOfFirst { it is Spring.Unknown }
        return if (i == -1) {
            if (isSolved(row)) {
                1
            } else {
                0
            }
        } else {
            solve(row, i, Spring.Damaged) + solve(row, i, Spring.Operational)
        }
    }

    private fun solve(row: Row, i: Int, spring: Spring): Long {
        require(spring is Spring.Damaged || spring is Spring.Operational)
        val newSprings = row.springs.toMutableList()
        newSprings[i] = spring

        val newContGroupDamaged = row.contGroupDamaged.toMutableList()
        if (spring is Spring.Damaged) {
//            if (newContGroupDamaged.isEmpty()) {
//                return 0
//            }
            require(newContGroupDamaged[0] > 0)
            newContGroupDamaged[0] = newContGroupDamaged[0] - 1
            var incr: Int
            if (newContGroupDamaged[0] == 0) {
                newContGroupDamaged.removeAt(0)
                incr = 2
            } else {
                incr = 1
            }
            if (i + incr > newSprings.size) {
                return 0
            }
            return solve(Row(newSprings.subList(i + incr, newSprings.size), newContGroupDamaged))
        } else {
            return solve(Row(newSprings, row.contGroupDamaged))
        }
    }

    fun isSolved(row: Row): Boolean {
//        println("Checking solution: $row")
        val damaged = mutableMapOf<Int, Int>()
        var index = -1
        var prev: Spring? = null
        for (spring in row.springs) {
            require(spring !is Spring.Unknown)
            if (spring is Spring.Damaged) {
                if (prev is Spring.Operational || prev == null) {
                    index++
                }
                require(index >= 0) {
                    "Index $index out of bounds for ${row.damagedByIndex}"
                }
                damaged.compute(index) { _, v -> (v ?: 0) + 1 }
            }
            prev = spring
        }

        if (damaged == row.damagedByIndex) {
            println("Solution: $row")
            return true
        } else {
            return false
        }
    }
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

    fun part1(input: List<String>): Any {
        var sum = 0L
        runBlocking {
            for ((i, line) in input.withIndex()) {
                val row = parseSpringLine(line)
                println("Line $i: $row")
                val solve = Solve()
                val solution = solve.solve(row)
                println("Line $i solutions: ${solution}")

//                val rowUnfolded = row.unfold()
//                println("Line $i: $rowUnfolded")
//                val solveUnfolded = Solve()
//                solveUnfolded.solve(rowUnfolded)
//                println("Unfolder row: solutions: ${solveUnfolded.solutions}")

//                if (i >= 2) break
//                val times = solveUnfolded.solutions / solve.solutions
//
//                val total = solve.solutions * times * times * times * times
                sum += solution
//                println("Solutions: $sum")
//            break
            }
        }
        return sum
    }

    fun part2(input: List<String>): Any {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

    val part1Expected = ""
    val part1 = part1(testInput)
    require(part1 == 21L)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")
//
    val input = readInput("Day${day}")
//
    val part1Real = part1(input)
    println("(Real) Part 1: 7173 $part1Real")
    require(part1Real == 7173L)
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
