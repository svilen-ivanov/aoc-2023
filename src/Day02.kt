import kotlin.math.max

fun main() {
    val day = "02"

    data class Cube(
        val number: Int,
        val color: String,
    )

    fun part1(input: List<String>): Int {
        return input.sumOf { line ->

            val (gameName, games) = line.split(": ")
            val (_, gameNumberStr) = gameName.split(" ")
            val gameNumber = gameNumberStr.toInt()

            val gamesSplit = games.split("; ")
            val isPossible = gamesSplit.all { game ->
                val cubes = game.split(", ")
                cubes.map { cube ->
                    val (number, color) = cube.split(" ")
                    Cube(number.toInt(), color)
                }.all { cube ->
                    when (cube.color) {
                        "red" -> cube.number <= 12
                        "green" -> cube.number <= 13
                        "blue" -> cube.number <= 14
                        else -> error("Unknown color: ${cube.color}")
                    }
                }
            }
            if (isPossible) {
                gameNumber
            } else {
                0
            }
        }
    }

    fun part2(input: List<String>): Int {
        return input.sumOf { line ->
            val (_, games) = line.split(": ")

            var red = 0
            var green = 0
            var blue = 0

            val gamesSplit = games.split("; ")
            gamesSplit.forEach { game ->
                val cubes = game.split(", ")
                cubes.map { cube ->
                    val (number, color) = cube.split(" ")
                    Cube(number.toInt(), color)
                }.forEach { cube ->
                    when (cube.color) {
                        "red" -> red = max(red, cube.number)
                        "green" -> green = max(green, cube.number)
                        "blue" -> blue = max(blue, cube.number)
                        else -> error("Unknown color: ${cube.color}")
                    }
                }
            }

            red * green * blue
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

    val part1Expected = "8"
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = "2286"
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput("Day${day}")

    val part1Real = part1(input)
    println("(Real) Part 1: $part1Real")

    val part2RealExpected = ""
    val part2Real = part2(input)
    println("(Real) Part 2: $part2Real")
}
