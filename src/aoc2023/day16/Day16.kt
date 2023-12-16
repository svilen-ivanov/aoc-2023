package aoc2023.day16

import readInput

data class Point(val x: Int, val y: Int)

sealed class Item {
    abstract val position: Point
    abstract val symbol: String

    data class Empty(override val position: Point) : Item() {
        override val symbol = "."
    }

    sealed class Mirror : Item()
    data class LeftMirror(override val position: Point) : Mirror() {
        override val symbol = "\\"
    }

    data class RightMirror(override val position: Point) : Mirror() {
        override val symbol = "/"
    }

    sealed class Splitter : Item()
    data class VerticalSplit(override val position: Point) : Splitter() {
        override val symbol = "|"
    }

    data class HorizontalSplit(override val position: Point) : Splitter() {
        override val symbol = "-"
    }
}

data class Contraption(val map: MutableMap<Point, Item>) {
    var dim = Point(0, 0)
    fun add(item: Item) {
        map[item.position] = item
        dim = Point(maxOf(dim.x, item.position.x), maxOf(dim.y, item.position.y))
    }

    override fun toString(): String {
        return buildString {
            for (y in 0..dim.y) {
                for (x in 0..dim.x) {
                    val item = map.getValue(Point(x, y))
                    append(item.symbol)
                }
                appendLine()
            }
        }

    }
}

fun main() {
    val day = "16"

    fun parse(input: List<String>): Contraption {
        val contraption = Contraption(mutableMapOf())
        for ((y, line) in input.withIndex()) {
            for ((x, char) in line.withIndex()) {
                val point = Point(x, y)
                val item = when (char) {
                    '.' -> Item.Empty(point)
                    '\\' -> Item.LeftMirror(point)
                    '/' -> Item.RightMirror(point)
                    '|' -> Item.VerticalSplit(point)
                    '-' -> Item.HorizontalSplit(point)
                    else -> error("Unknown char $char")
                }
                contraption.add(item)
            }
        }
        return contraption
    }
    fun part1(input: List<String>): Any {
        val contraption = parse(input)
        println(contraption)
        return  contraption
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
