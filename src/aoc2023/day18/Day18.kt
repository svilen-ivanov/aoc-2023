package aoc2023.day18

import readInput
import kotlin.math.abs

sealed class Direction {
    abstract val symbol: String
    abstract fun vector(distance: Long): Point

    data object Up : Direction() {
        override val symbol = "U"
        override fun vector(distance: Long): Point {
            return Point(0, -distance)
        }
    }

    data object Down : Direction() {
        override val symbol = "D"
        override fun vector(distance: Long): Point {
            return Point(0, distance)
        }
    }

    data object Right : Direction() {
        override val symbol = "R"
        override fun vector(distance: Long): Point {
            return Point(distance, 0)
        }
    }

    data object Left : Direction() {
        override val symbol = "L"
        override fun vector(distance: Long): Point {
            return Point(-distance, 0)
        }
    }
}

data class Color(val hex: String)

data class Command(
    val direction: Direction,
    val distance: Long,
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

data class Point(val x: Long, val y: Long) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    operator fun minus(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    operator fun times(other: Long): Point {
        return Point(x * other, y * other)
    }
}

data class Line(val p1: Point, val p2: Point) {
    init {
        require(p1 != p2) {
            "Line must have two different points: $p1 == $p2"
        }
        require(p1.x == p2.x || p1.y == p2.y) {
            "Line must be horizontal or vertical: $p1 -> $p2"
        }
    }

    fun liesOnLine(point: Point): Boolean {
        return if (p1.x == p2.x) {
            point.x == p1.x && point.y in minOf(p1.y, p2.y)..maxOf(p1.y, p2.y)
        } else {
            point.y == p1.y && point.x in minOf(p1.x, p2.x)..maxOf(p1.x, p2.x)
        }
    }
}

fun extra(oldDir: Direction, newDir: Direction): Point? {
    return if (oldDir == Direction.Right) {
        when (newDir) {
            is Direction.Up -> return Point(0, 0)
            is Direction.Down -> return Point(1, 0)
            else -> return Point(0, 0)
        }
    } else {
        null
    }
}


class ExecuteDigPlan(val digPlan: DigPlan) {
    var edges = mutableListOf<Point>()
    var lines = mutableListOf<Line>()
//    lateinit var verticalLines: List<Line>
    var topLeft: Point = Point(0, 0)
    var bottomRight: Point = Point(0, 0)
    fun execute() {
        for ((i, command) in digPlan.commands.withIndex()) {
            if (i == 0) {
                edges += Point(0, 0)
            }
            val prev = edges.last()
            val next = prev + command.direction.vector(command.distance)
            val line = Line(prev, next)
            lines += line
            edges += next
            bottomRight = Point(maxOf(bottomRight.x, next.x), maxOf(bottomRight.y, next.y))
            topLeft = Point(minOf(topLeft.x, next.x), minOf(topLeft.y, next.y))
        }
        require(edges.last() == edges.first()) {
            "Path must end where it started: ${edges.last()} != ${edges.first()}"
        }
//        verticalLines = lines.filter { it.p1.x == it.p2.x }
    }


    fun printPlan() {
        for (y in topLeft.y..bottomRight.y) {
            for (x in topLeft.x..bottomRight.x) {
                val point = Point(x, y)
                val onLine = lines.any { it.liesOnLine(point) }
                if (!onLine) {
                    print(".")
                } else {
                    print("#")
                }
            }
            println()
        }
    }

    fun length(): Long {
        return edges.windowed(2).sumOf { (a, b) ->
            val r = (b - a)
            if (r.x == 0L) {
                abs(r.y)
            } else if (r.y == 0L) {
                abs(r.x)
            } else {
                error("Unknown direction: $r")
            }
        }
    }

    fun area(): Double {
        return edges.windowed(2).sumOf { (a, b) ->
            (a.y + b.y) * (a.x - b.x).toDouble()
        } / 2
    }
}

fun main() {
    val day = "18"

    val space = Regex("\\s+")
    val hex = Regex("\\(#([0-9a-fA-F]{6})\\)")
    fun parse(input: List<String>): DigPlan {
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
                distance = distance.toLong(),
                color = Color(color)
            )
            digPlan.addCommand(command)
        }
        return digPlan
    }

    fun part1(input: List<String>): Any? {
        val digPlan = parse(input)
        val digPlanExecute = ExecuteDigPlan(digPlan)
        digPlanExecute.execute()
        digPlanExecute.printPlan()
        return 0
    }

    fun part2(input: List<String>): Any? {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(day, "Day${day}_test")

    val part1Expected = "62"
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")

//    val input = readInput(day, "Day${day}")

//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")

//    val part2Real = measureTimedValue { part2(input) }
//    println("(Real) Part 2: $part2Real")
}
