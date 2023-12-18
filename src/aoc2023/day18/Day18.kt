package aoc2023.day18

import readInput
import kotlin.time.measureTimedValue

sealed class Direction {
    abstract val symbol: String
    data object Up : Direction() {
        override val symbol = "U"
    }
    data object Down : Direction() {
        override val symbol = "D"
    }
    data object Right : Direction() {
        override val symbol = "R"
    }
    data object Left : Direction() {
        override val symbol = "L"
    }
}

data class Color(val hex: String)

data class Command(
    val direction: Direction,
    val distance: Int,
    val color: Color
)

class DigPlan {
    val commands = mutableListOf<Command>()
    fun addCommand(command: Command) {
        commands += command
    }

    override fun toString(): String {
        return buildString {
            for (command in commands) {
                appendLine("${command.direction.symbol} ${command.distance} (#${command.color.hex})")
            }
        }
    }
}

fun main() {
    val day = "18"

    val space = Regex("\\s+")
    val hex = Regex("\\(#([0-9a-fA-F]{6})\\)")
    fun part1(input: List<String>): Any? {
        val digPlan = DigPlan()
        for (line in input) {
            val (direction, distance, colorStr) = line.split(space)
            val (color) = hex.find(colorStr)!!.destructured
            val command = Command(
                direction = when (direction) {
                    "U" -> Direction.Up
                    "D" -> Direction.Down
                    "R" -> Direction.Right
                    "L" -> Direction.Left
                    else -> error("Unknown direction: $direction")
                },
                distance = distance.toInt(),
                color = Color(color)
            )
            digPlan.addCommand(command)
        }
        println(digPlan)
        return input.size
    }

    fun part2(input: List<String>): Any? {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(day, "Day${day}_test")

    val part1Expected = "102"
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")
//
//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")
//
//    val input = readInput(day, "Day${day}")
//
//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")
//
//    val part2Real = measureTimedValue { part2(input) }
//    println("(Real) Part 2: $part2Real")
}
