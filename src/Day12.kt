import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.coroutineContext

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
    var nest = 0L
    var solutions = 0L



    suspend fun solve(row: Row) {
        val outcome = checkCanBeValid(row)
        when (outcome) {
            is Outcome.Solution -> {
                solutions++
                return
            }

            is Outcome.Stop -> {
                return
            }

            is Outcome.Continue -> {
                solve(row, Spring.Damaged)
                solve(row, Spring.Operational)
            }
        }
    }

    private suspend fun solve(row: Row, spring: Spring) {
        require(spring is Spring.Damaged || spring is Spring.Operational)

        val newSprings = row.springs.toMutableList()
        val firstUnknown = newSprings.indexOfFirst { it is Spring.Unknown }
        newSprings[firstUnknown] = spring

        solve(Row(newSprings, row.contGroupDamaged))
    }

    fun checkCanBeValid(row: Row): Any {
//        println("Solving: $row")
        val damaged = mutableMapOf<Int, Int>()
        var index = -1
        var prev: Spring? = null
        var hasUnknown = false
        for (spring in row.springs) {
            if (spring is Spring.Unknown) {
                hasUnknown = true
                break
            }
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

        if (!hasUnknown) {
            if (damaged == row.damagedByIndex) {
                println("Solution: $row")
                return Outcome.Solution
            } else {
                return Outcome.Stop
            }
        } else {
            val expected = row.damagedByIndex
            val actual = damaged
            if (actual.size > expected.size) {
                return Outcome.Stop
            }

            for ((i, actualCount) in actual) {
                val expectedCount = expected.getValue(i)
                if (actualCount > expectedCount) {
                    return Outcome.Stop
                }
            }

            return Outcome.Continue
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
                solve.solve(row)
                println("Single row: solutions: ${solve.solutions}")

//                val rowUnfolded = row.unfold()
//                println("Line $i: $rowUnfolded")
//                val solveUnfolded = Solve()
//                solveUnfolded.solve(rowUnfolded)
//                println("Unfolder row: solutions: ${solveUnfolded.solutions}")

//                if (i >= 2) break
//                val times = solveUnfolded.solutions / solve.solutions
//
//                val total = solve.solutions * times * times * times * times
                sum += solve.solutions
                require(sum >= 0)
                require(solve.solutions >= 0)
                println("Solutions: $sum")
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
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")
//
    val input = readInput("Day${day}")
//
    val part1Real = part1(input)
    println("(Real) Part 1: 7173 $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
