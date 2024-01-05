package aoc2023.prev

fun main() {
    val day = "01"

    fun findDigits(line: String): List<Int> {
        val numbers = listOf("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")

        val digits = mutableListOf<Int>()
        for (i in line.indices) {
            val sub = line.substring(i)
            if (sub[0].isDigit()) {
                digits.add(sub[0].toString().toInt())
            } else {
                for (number in numbers) {
                    if (sub.startsWith(number)) {
                        digits.add(numbers.indexOf(number))
                    }
                }
            }
        }

        return digits
    }

    fun part1(input: List<String>): Int {
        return input.sumOf { line ->
            val digits = findDigits(line)
            val first = digits.first()
            val second = digits.last()
            val num = first * 10 + second
            num
        }
    }


    fun part2(input: List<String>): Int {
        return input.sumOf { line ->
            val digits = findDigits(line)
            val first = digits.first()
            val second = digits.last()
            val num = first * 10 + second
            num
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = aoc2023.prev.readInput("Day${day}_test")

    val part1Expected = "281"
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = "281"
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = aoc2023.prev.readInput("Day${day}")

    val part1RealExpected = "281"
    val part1Real = part1(input)
    println("(Real) Part 1: expected: $part1RealExpected, got: $part1Real")

    val part2RealExpected = "281"
    val part2Real = part2(input)
    println("(Real) Part 2: expected: $part2RealExpected, got: $part2Real")
}
