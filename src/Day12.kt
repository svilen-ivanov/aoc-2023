sealed class Spring {
    abstract val char: String

    class Operational(val index: Int) : Spring() {
        override val char = "."
    }

    class Damaged(val index: Int) : Spring() {
        override val char = "#"
    }

    class Unknown(val index: Int) : Spring() {
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

    val size = springs.size
    val ones = contGroupDamaged.sum()
    val minZeros = contGroupDamaged.size - 1
    val maxZeros = size - ones
}

fun main() {
    val day = "12"
    ?###???????? 12
    6 + 2 = 8

    fun parseSpringLine(line: String): Row {
        val (rowStr, damagedStr) = line.split(" ")
        val contDamageSprings = damagedStr.split(",").map { it.toInt() }
        val row = rowStr.mapIndexed { index, it ->
            when (it) {
                '#' -> Spring.Damaged(index)
                '.' -> Spring.Operational(index)
                '?' -> Spring.Unknown(index)
                else -> throw Exception("Unknown spring type: $it")
            }
        }

        return Row(row, contDamageSprings)

    }

    fun part1(input: List<String>): Any {
        for (line in input) {
            val row = parseSpringLine(line)
            println("$row: ${row.size}, ${row.minZeros} ~ ${row.maxZeros}")
        }
        return input.size
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

//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
