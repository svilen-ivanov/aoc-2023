package aoc2023.day21

import readInput
import kotlin.math.absoluteValue

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    fun move(direction: Direction): Point {
        return this + direction.vector
    }

    fun translate(dim: Point): Point {
        val newX = if (x >= 0) {
            x.mod(dim.x + 1)
        } else {
            val absX = (x + 1).absoluteValue.mod(dim.x + 1)
            dim.x - absX
        }
        val newY = if (y >= 0) {
            y.mod(dim.y + 1)
        } else {
            val absY = (y + 1).absoluteValue.mod(dim.y + 1)
            dim.y - absY
        }
        return Point(newX, newY)
    }
}

sealed class Direction {
    abstract val vector: Point
    abstract val symbol: String

    data object North : Direction() {
        override val vector = Point(0, -1)
        override val symbol = "^"
    }

    data object South : Direction() {
        override val vector = Point(0, 1)
        override val symbol = "v"
    }

    data object East : Direction() {
        override val vector = Point(1, 0)
        override val symbol = ">"
    }

    data object West : Direction() {
        override val vector = Point(-1, 0)
        override val symbol = "<"
    }

    companion object {
        val all by lazy { listOf(North, South, East, West) }
    }
}

sealed class Item {
    class Rock : Item()
    class Garden : Item()
}

class PuzzleMap {
    val matrix = mutableMapOf<Point, Item>()
    var dim = Point(0, 0)
    var start = mutableSetOf<Point>()
    var reachable = mutableSetOf<Point>()

    fun add(point: Point, item: Item) {
        matrix[point] = item
        dim = Point(maxOf(dim.x, point.x), maxOf(dim.y, point.y))
    }

    fun move(): PuzzleMap {
        for (direction in Direction.all) {
            val newPoints = start.map { it.move(direction) }
            for (newPoint in newPoints) {
                val translatedPoint = newPoint.translate(dim)
                val item = matrix.getValue(translatedPoint)
                if (item is Item.Garden) {
                    reachable += newPoint
                }
            }
        }
        return reset()
    }

    private fun reset(): PuzzleMap {
        val newMap = PuzzleMap()
        newMap.matrix.putAll(matrix)
        newMap.dim = dim
        newMap.start = reachable
        newMap.reachable.clear()
        return newMap
    }

    override fun toString(): String {
        return buildString {
            for (y in 0..dim.y) {
                for (x in 0..dim.x) {
                    val point = Point(x, y)
                    val char = if (point in start) {
                        'S'
                    } else if (point in reachable) {
                        'O'
                    } else {
                        when (matrix[point]) {
                            is Item.Rock -> '#'
                            is Item.Garden -> '.'
                            else -> error("Unknown item: ${matrix[point]}")
                        }
                    }
                    append(char)
                }
                append('\n')
            }
        }
    }
}

fun main() {
    val day = "21"

    fun parseInput(input: List<String>): PuzzleMap {
        val puzzleMap = PuzzleMap()
        for ((y, line) in input.withIndex()) {
            for ((x, char) in line.withIndex()) {
                val point = Point(x, y)
                when (char) {
                    '#' -> puzzleMap.add(point, Item.Rock())
                    '.' -> puzzleMap.add(point, Item.Garden())
                    'S' -> {
                        puzzleMap.add(point, Item.Garden())
                        puzzleMap.start += point
                    }

                    else -> error("Unknown char: $char")
                }
            }
        }
        return puzzleMap
    }

    fun part1(input: List<String>): Any {
        val steps = 500
        var puzzleMap = parseInput(input)
//        println("Initial")
//        println(puzzleMap)

        for (i in 1..steps) {
//            println("After iteration: $i")
            puzzleMap = puzzleMap.move()
//            println(puzzleMap)
        }

        return puzzleMap.start.size
    }

    fun part2(input: List<String>): Any {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(day, "Day${day}_test")

    val part1Expected = "16"
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
