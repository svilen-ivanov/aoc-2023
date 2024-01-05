package aoc2023.day17

import aoc2023.prev.readInput

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    operator fun minus(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }
}

data class HeatLoss(val heat: Long)

sealed class Direction {
    abstract val vector: Point
    abstract val symbol: String

    data object Up : Direction() {
        override val vector = Point(0, -1)
        override val symbol = "^"
    }

    data object Down : Direction() {
        override val vector = Point(0, 1)
        override val symbol = "v"
    }

    data object Right : Direction() {
        override val vector = Point(1, 0)
        override val symbol = ">"
    }

    data object Left : Direction() {
        override val vector = Point(-1, 0)
        override val symbol = "<"
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

    fun printPath(path: Path) {
        buildString {
            for (y in 0..dim.y) {
                for (x in 0..dim.x) {
                    val point = Point(x, y)
                    val item = map.getValue(point)
                    append(path.symbol(point) ?: item.heat)
                }
                appendLine()
            }
        }.also {
            println("------------")
            print(it)
//            val x = path.lastSteps.sumOf { map.getValue(it.point).heat }
//            println("Heat: $x")
            println("------------")
        }
    }
}

data class Vector(val point: Point, val direction: Direction) {
    override fun toString(): String {
        return when (direction) {
            Direction.Up -> "↑"
            Direction.Down -> "↓"
            Direction.Left -> "←"
            Direction.Right -> "→"
        }
    }
}

class Path(
    val current: Point,
    val lastSteps: List<Direction>,
    val visited: Set<Vector>,
    val dim: Point
) {

    private fun <T> List<T>.allTheSame(): Boolean {
        require(this.isNotEmpty())
        return this.all { it == this.first() }
    }

    constructor(current: Point, dim: Point) : this(current, emptyList(), emptySet(),  dim)

    fun move(direction: Direction): Path {
        val next = current + direction.vector
        val lastSteps = lastSteps + direction
        return Path(next, lastSteps.takeLast(3), visited + Vector(current, direction), dim)
    }

    fun symbol(point: Point): String? {
        return null
    }

    private val possibleDirs = listOf(Direction.Right, Direction.Down, Direction.Up, Direction.Left)

    fun possiblePaths(): List<Direction> {
        if (isEnd()) {
            error("Reached end")
        }
        return buildList {
            for (dir in possibleDirs) {
                if (lastSteps.lastOrNull() == dir) {
                    continue
                }
                if (visited.contains(Vector(current, dir))) {
                    continue
                }
                val next = current + dir.vector
                // outside
                if (next.x < 0 || next.y < 0 || next.x > dim.x || next.y > dim.y) {
                    continue
                }
                // repeats 3 times
                if (lastSteps.size >= 3) {
                    val last3 = lastSteps.takeLast(3) + dir
                    if (last3.allTheSame()) {
                        continue
                    }
                }
                add(dir)
            }
        }
    }

    fun isEnd(): Boolean {
        return current == dim
    }
}

data class CacheKey(val current: Point, val dir: Direction)

class Solver(val contraption: Contraption) {
    val cache = mutableMapOf<CacheKey, Long?>()
    fun solveStart(start: Path): Long? {
        return start.possiblePaths().mapNotNull { direction ->
            solve(start.current, direction)
        }.minOrNull()
    }

    fun solve(point: Point, direction: Direction): Long? {
        val key = CacheKey(point, direction)
        return if (key in cache) {
            cache[key]
        } else {
            val result = solveRec(point, direction)
            cache[key] = result
            result
        }
    }

    private fun solveRec(point: Point, direction: Direction): Long? {
        val path = Path(point, contraption.dim)
        path.possiblePaths().forEach {

        }
        TODO("Not yet implemented")
    }
}

fun main() {
    val day = "17"

    fun part1(input: List<String>): Any? {
        val contraption = Contraption(mutableMapOf())
        for (y in input.indices) {
            for (x in input[y].indices) {
                val heatStr = input[y][x]
                val heat = HeatLoss(heatStr.toString().toLong())
                contraption.add(Point(x, y), heat)
            }
        }
        println(contraption)
        println("----")
        val path = Path(contraption.start, contraption.dim)
        val solver = Solver(contraption)
        val r = solver.solveStart(path)

//        contraption.printPath(path)

        return r
    }

    fun part2(input: List<String>): Any {
        return 0
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(day, "Day${day}_test")

    val part1Expected = "102"
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput(day, "Day${day}")

    //978
    // 974
//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")

//    val part2Real = measureTimedValue { part2(input) }
//    println("(Real) Part 2: $part2Real")
}
