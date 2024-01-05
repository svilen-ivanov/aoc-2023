package aoc2023.day18

import aoc2023.prev.readInput
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

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
                appendLine("${command.direction.symbol} ${command.distance}")
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

    fun outline(): List<Point> {
        return listOf(
            this,
            Point(x + 1, y),
            Point(x + 1, y + 1),
            Point(x, y + 1),
        )
    }

    fun edge(dir: Direction): List<Point> {
        return when (dir) {
            Direction.Up -> listOf(this, Point(x + 1, y))
            Direction.Down -> listOf(Point(x, y + 1), Point(x + 1, y + 1))
            Direction.Right -> listOf(Point(x + 1, y), Point(x + 1, y + 1))
            Direction.Left -> listOf(this, Point(x, y + 1))
        }
    }

    fun reverseEdge(dir: Direction): List<Point> {
        return edge(
            when (dir) {
                Direction.Up -> Direction.Down
                Direction.Down -> Direction.Up
                Direction.Right -> Direction.Left
                Direction.Left -> Direction.Right
            }
        )
    }
}

class Trench {
    val topWall = mutableListOf<Point>()
    val bottomWall = mutableListOf<Point>()

    override fun toString(): String {
        return buildString {
            appendLine("---")
            appendLine("Trench:")
            appendLine("  top: $topWall")
            appendLine("  bottom: $bottomWall")
            appendLine("---")
        }
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

class ExecuteDigPlan(val digPlan: DigPlan) {
    var edges = mutableListOf<Point>()
    var lines = mutableListOf<Line>()
    val trenches = mutableListOf<Trench>()
    var topLeft: Point = Point(0, 0)
    var bottomRight: Point = Point(0, 0)
    fun execute() {
        edges += Point(0, 0)
        for (command in digPlan.commands) {
            val prev = edges.last()
            val next = prev + command.direction.vector(command.distance)

            val trench = Trench()
            val (top1, bottom1) = prev.edge(command.direction)
            trench.topWall += top1
            trench.bottomWall += bottom1

            val (top2, bottom2) = next.reverseEdge(command.direction)
            trench.topWall += top2
            trench.bottomWall += bottom2

            trenches += trench

            val line = Line(prev, next)
            lines += line
            edges += next
            topLeft = Point(minOf(topLeft.x, next.x), minOf(topLeft.y, next.y))
            bottomRight = Point(maxOf(bottomRight.x, next.x), maxOf(bottomRight.y, next.y))
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

    fun connectTrenches(): Long {
        val outline1 = mutableListOf<Point>()
        val outline2 = mutableListOf<Point>()
        for (i in 0 until trenches.size) {
            val current = trenches[i]

            if (outline1.isEmpty()) {
                outline1 += current.topWall
                outline2 += current.bottomWall
                continue
            }

            val r1 = appendOutline(outline1, current)
            val r2 = appendOutline(outline2, current)
            require(r1.isNotEmpty() || r2.isNotEmpty()) {
                "Trenches must be connected: $current"
            }

            if (r1.isNotEmpty()) {
                val last = outline2.last()
                val (common, start, end) = r1
                outline2 += join(common, last, start) + listOf(end)
            } else {
                val last = outline1.last()
                val (common, start, end) = r2
                outline1 += join(common, last, start) + listOf(end)
            }
        }

        println("Outline1: $outline1: ${areaOf(outline1)}}")
//        printOutline(outline1)
        println("Outline2: $outline2: ${areaOf(outline2)}}")

        return max(areaOf(outline1), areaOf(outline2))
//        printOutline(outline2)
    }

    fun areaOf(points: List<Point>): Long {
        return points.windowed(2).sumOf { (a, b) ->
            (a.y + b.y) * (a.x - b.x)
        } / 2
    }

    fun printOutline(outline: List<Point>) {
        for (y in topLeft.y - 10..bottomRight.y + 10) {
            for (x in topLeft.x - 10..bottomRight.x + 10) {
                val point = Point(x, y)
                val onLine = outline.contains(point)
                if (!onLine) {
                    print(".")
                } else {
                    print("#")
                }
            }
            println()
        }
    }


    private fun appendOutline(outline: MutableList<Point>, current: Trench): List<Point> {
        val last = outline.last()
        if (last == current.topWall[0]) {
            outline += current.topWall[1]
            return listOf(last, current.bottomWall[0], current.bottomWall[1])
        } else if (last == current.bottomWall[0]) {
            outline += current.bottomWall[1]
            return listOf(last, current.topWall[0], current.topWall[1])
        } else {
            return emptyList()
        }
    }

    private fun findMissing(common: Point, last: Point, start: Point): Point {
        require((last.x - start.x).absoluteValue == 1L && (last.y - start.y).absoluteValue == 1L)

        val p1 = Point(min(start.x, last.x), min(start.y, last.y))
        val p2 = Point(max(start.x, last.x), max(start.y, last.y))
        val p3 = Point(min(start.x, last.x), max(start.y, last.y))
        val p4 = Point(max(start.x, last.x), min(start.y, last.y))

        return listOf(p1, p2, p3, p4).first { it != common && it != start && it != last }
    }

    private fun join(common: Point, last: Point, start: Point): List<Point> {
        return listOf(
            findMissing(common, last, start),
            start
        )
    }


    fun fill(): Long {
        var count = 0L
        val verticalLines = lines.filter { it.p1.x == it.p2.x }
        val horizontalLines = lines.filter { it.p1.y == it.p2.y }
        for (y in topLeft.y..bottomRight.y) {
            var crossCount = 0L
            var x = topLeft.x
            while (x <= bottomRight.x) {
                val point = Point(x, y)
                val horizLine = horizontalLines.firstOrNull { it.liesOnLine(point) }
                if (horizLine != null) {
                    count += (horizLine.p1.x - horizLine.p2.x).absoluteValue.toInt() + 1
                    val vert1 =
                        verticalLines.first { (it.p1.y == horizLine.p1.y || it.p2.y == horizLine.p1.y) && (it.p1.x == horizLine.p1.x || it.p2.x == horizLine.p1.x) }
                    val vert2 =
                        verticalLines.first { (it.p1.y == horizLine.p1.y || it.p2.y == horizLine.p1.y) && (it.p1.x == horizLine.p2.x || it.p2.x == horizLine.p2.x) }
                    val minY = minOf(vert1.p1.y, vert1.p2.y, vert2.p1.y, vert2.p2.y)
                    val maxY = maxOf(vert1.p1.y, vert1.p2.y, vert2.p1.y, vert2.p2.y)
                    if (minY < y && y < maxY) {
                        crossCount++
                    }
                    x = maxOf(horizLine.p1.x, horizLine.p2.x) + 1
                } else {
                    val onVertLine = verticalLines.any { it.liesOnLine(point) }
                    if (onVertLine) {
                        if (crossCount % 2L == 1L) {
                            count++
                        }
                        crossCount++
                    }

                    if (crossCount % 2L == 1L) {
                        count++
                    }
                    x++
                }

            }
        }
        return count
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

    fun area(): Long {
        return edges.windowed(2).sumOf { (a, b) ->
            (a.y + b.y) * (a.x - b.x)
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

    fun convert(digPlanOld: DigPlan): DigPlan {
        val digPlan = DigPlan()
        digPlanOld.commands.forEach {
            val distance = it.color.hex.substring(0..4).toLong(16)
            val code = it.color.hex.last()
            val dir = when (code) {
                '0' -> Direction.Right
                '1' -> Direction.Down
                '2' -> Direction.Left
                '3' -> Direction.Up
                else -> error("Unknown direction: $code")
            }
            println(distance)
            digPlan.addCommand(Command(dir, distance, it.color))
        }
        println(digPlan)
        return digPlan
    }

    fun part1(input: List<String>): Any? {
        val digPlanOld = parse(input)
        val digPlan = convert(digPlanOld)
        val digPlanExecute = ExecuteDigPlan(digPlan)
//        val digPlanExecute = ExecuteDigPlan(digPlanOld)
        digPlanExecute.execute()
//        digPlanExecute.printPlan()
        val res = digPlanExecute.connectTrenches()
//        val res = digPlanExecute.area()
        return res
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

    val input = readInput(day, "Day${day}")

    val part1Real = part1(input)
    println("(Real) Part 1: $part1Real")

//    val part2Real = measureTimedValue { part2(input) }
//    println("(Real) Part 2: $part2Real")
}
