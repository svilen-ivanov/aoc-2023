package aoc2023.day17

import readInput

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }
}

data class HeatLoss(val heat: Int)

sealed class Direction {
    abstract val vector: Point
    data object Up : Direction() {
        override val vector = Point(0, -1)
    }
    data object Down : Direction() {
        override val vector = Point(0, 1)
    }
    data object Right : Direction() {
        override val vector = Point(1, 0)
    }
}
data class Contraption(val map: MutableMap<Point, HeatLoss>) {
    var dim = Point(0, 0)
    val end get() = dim
    val start = Point(0, 0)

    fun add(point: Point, item: HeatLoss) {
        map[point] = item
        dim = Point(maxOf(dim.x, point.x), maxOf(dim.y, point.y))
    }

    override fun toString(): String {
        return buildString {
            for (y in 0..dim.y) {
                for (x in 0..dim.x) {
                    val item = map.getValue(Point(x, y))
                    append(item.heat)
                }
                appendLine()
            }
        }
    }
}

class Solver(val contraption: Contraption) {
    val track = mutableListOf<Direction>()

    private fun <T> List<T>.allTheSame(): Boolean {
        require(this.isNotEmpty())
        return this.all { it == this.first() }
    }

    private val allDirections = listOf(Direction.Up, Direction.Down, Direction.Right)

    fun getPossibleDirections(point: Point): List<Direction> {
        val last3 = track.takeLast(3)
        val forbiddenDirection: Direction? = if(last3.size == 3 && last3.allTheSame()) {
            last3.first()
        } else {
            null
        }
        val possibleDirections = allDirections.filter { it != forbiddenDirection }.filter {
            val nextPoint = point + it.vector
            !(nextPoint.x < 0 || nextPoint.y < 0 || nextPoint.x > contraption.dim.x || nextPoint.y > contraption.dim.y)
        }
        return possibleDirections
    }

    fun solve(current: Point) {
        val r = getPossibleDirections(current)
        println(r)
    }
}

fun main() {
    val day = "17"

    fun part1(input: List<String>): Any {
        val contraption = Contraption(mutableMapOf())
        for (y in input.indices) {
            for (x in input[y].indices) {
                val heatStr = input[y][x]
                val heat = HeatLoss(heatStr.toString().toInt())
                contraption.add(Point(x, y), heat)
            }
        }
        println(contraption)
        val solver = Solver(contraption)
        solver.solve(contraption.start)
        return 0
    }

    fun part2(input: List<String>): Any {
        return 0
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(day, "Day${day}_test")

    val part1Expected = ""
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput(day, "Day${day}")

//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")

//    val part2Real = measureTimedValue { part2(input) }
//    println("(Real) Part 2: $part2Real")
}
