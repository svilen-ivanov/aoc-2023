package aoc2023.day16

import readInput

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }
}

data class Vector(
    val direction: Compass,
    val point: Point,
) {
    fun next() = Vector(direction, point + direction.next)
}

enum class Compass(val next: Point) {
    NORTH(Point(0, -1)),
    EAST(Point(1, 0)),
    SOUTH(Point(0, 1)),
    WEST(Point(-1, 0));

    fun vector() = Vector(this, next)
}

typealias DirectionLambda = (Compass) -> Compass

val right: DirectionLambda = {
    when (it) {
        Compass.WEST -> Compass.SOUTH
        Compass.SOUTH -> Compass.WEST
        Compass.EAST -> Compass.NORTH
        Compass.NORTH -> Compass.EAST
    }
}

val left: DirectionLambda = {
    when (it) {
        Compass.WEST -> Compass.NORTH
        Compass.NORTH -> Compass.WEST
        Compass.EAST -> Compass.SOUTH
        Compass.SOUTH -> Compass.EAST
    }
}

sealed class Item {
    abstract val position: Point
    abstract val symbol: String

    abstract fun changeDirection(compass: Compass): List<Compass>
    data class Empty(override val position: Point) : Item() {
        override val symbol = "."
        override fun changeDirection(compass: Compass) = listOf(compass)
    }

    sealed class Mirror : Item()
    data class LeftMirror(override val position: Point) : Mirror() {

        override fun changeDirection(compass: Compass): List<Compass> {
            return listOf(left(compass))
        }

        override val symbol = "\\"
    }

    data class RightMirror(override val position: Point) : Mirror() {
        override val symbol = "/"
        override fun changeDirection(compass: Compass): List<Compass> {
            return listOf(right(compass))
        }
    }

    sealed class Splitter : Item()
    data class VerticalSplit(override val position: Point) : Splitter() {
        override val symbol = "|"

        override fun changeDirection(compass: Compass): List<Compass> {
            return when (compass) {
                Compass.NORTH, Compass.SOUTH -> listOf(compass)
                Compass.EAST, Compass.WEST -> listOf(left(compass), right(compass))
            }
        }

    }

    data class HorizontalSplit(override val position: Point) : Splitter() {
        override val symbol = "-"
        override fun changeDirection(compass: Compass): List<Compass> {
            return when (compass) {
                Compass.NORTH, Compass.SOUTH -> listOf(left(compass), right(compass))
                Compass.EAST, Compass.WEST -> listOf(compass)
            }
        }
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

class Solve(val contraption: Contraption) {
    val visitedPoints = mutableMapOf<Point, Compass>()
    val visited = mutableSetOf<Vector>()
    fun solve(current: Vector) {
        while (true) {
            val item = contraption.map[current.point]
            if (item == null) {
                println("Out of bounds")
                return
            }
            visited.add(current)
            visitedPoints[current.point] = current.direction
            println(this)

            val newDirections = item.changeDirection(current.direction)
            for (it in newDirections.take(1)) {
                val newVector = Vector(it, current.point + it.next)
                if (visited.contains(newVector)) {
                    println("Loop")
                    return
                }
                solve(newVector)
            }
        }
    }

    override fun toString(): String {
        return buildString {
            for (y in 0..contraption.dim.y) {
                for (x in 0..contraption.dim.x) {
                    val point = Point(x, y)
                    val symbol = contraption.map.getValue(point).symbol

                    append(visitedPoints[point]?.let{ "#" } ?: symbol)
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
        val solve = Solve(contraption)
        solve.solve(Vector(Compass.EAST, Point(0, 0)))
        return contraption
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

    val input = readInput(day, "Day${day}")

//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")

//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
