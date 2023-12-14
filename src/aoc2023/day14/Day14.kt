package aoc2023.day14

import readInput

sealed class DishItem {
    abstract val char: String

    object Empty : DishItem() {
        override val char = "."
    }

    object Cube : DishItem() {
        override val char = "#"
    }

    object Round : DishItem() {
        override val char = "O"
    }

    override fun toString(): String {
        return char
    }
}

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun unaryMinus() = Point(-x, -y)

    override fun toString(): String {
        return "($x, $y)"
    }
}

class Dish {
    var dim = Point(0, 0)
    fun add(point: Point, item: DishItem) {
        dish[point] = item
        dim = Point(maxOf(dim.x, point.x), maxOf(dim.y, point.y))
    }

    val dish = mutableMapOf<Point, DishItem>()

    override fun toString(): String {
        return buildString {
            for (y in 0..dim.y) {
                for (x in 0..dim.x) {
                    val p = Point(x, y)
                    val item = dish[p]
                    append("${item}")
                }
                appendLine()
            }
        }
    }

    fun getLoad(): Long {
        val rounds = dish.entries.filter {  (p, i) -> i is DishItem.Round }.map { (p, i) -> p
            dim.y - p.y + 1
        }.sum()
        return rounds.toLong()
    }
}

fun main() {
    val day = "14"

    fun part1(input: List<String>): Any {
        val dish = Dish()
        for (y in input.indices) {
            for (x in input[y].indices) {
                val point = Point(x, y)
                dish.add(
                    point, when (input[y][x]) {
                        '.' -> DishItem.Empty
                        '#' -> DishItem.Cube
                        'O' -> DishItem.Round
                        else -> throw Exception("Unknown item")
                    }
                )
            }
        }

        println(dish)

        val result = dish.getLoad()
        return result
    }

    fun part2(input: List<String>): Any {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(day, "Day${day}_test")

    val part1Expected = "136"
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
