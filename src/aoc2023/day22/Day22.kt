package aoc2023.day22

import readInput

data class Point(val x: Long, val y: Long, val z: Long) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y, z + other.z)
    }
}

data class Brick(val p1: Point, val p2: Point)

fun main() {
    val day = "22"

    fun parseInput(input: List<String>): List<Brick> {
        return buildList {
            for (line in input) {
                val (p1, p2) = line.split("~").map {
                    val (x, y, z) = it.split(",").map { it.toLong() }
                    Point(x, y, z)
                }
                add(Brick(p1, p2))
            }
        }
    }

    fun part1(input: List<String>): Any {
        val bricks = parseInput(input)
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

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")
//
//    val input = readInput(day, "Day${day}")
//
//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
