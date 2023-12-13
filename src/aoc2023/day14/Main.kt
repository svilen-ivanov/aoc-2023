package aoc2023.day14

import readInput

fun main() {
    val day = "14"

    fun part1(input: List<String>): Any {
        return input.size
    }

    fun part2(input: List<String>): Any {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(day, "Day${day}_test")

    val part1Expected = ""
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = ""
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput(day, "Day${day}")

    val part1Real = part1(input)
    println("(Real) Part 1: $part1Real")

    val part2Real = part2(input)
    println("(Real) Part 2: $part2Real")
}
