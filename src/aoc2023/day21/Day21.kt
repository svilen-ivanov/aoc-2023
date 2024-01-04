package aoc2023.day21

import printme
import readInput
import kotlin.math.absoluteValue
import kotlin.time.measureTimedValue

data class Point(val x: Int, val y: Int) : Comparable<Point> {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    operator fun times(other: Point): Point {
        return Point(x * other.x, y * other.y)
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

    fun segment(dim: Point): Point {
        val newX = if (x >= 0) {
            x.div(dim.x + 1)
        } else {
            -1 - (x + 1).absoluteValue.div(dim.x + 1)
        }
        val newY = if (y >= 0) {
            y.div(dim.y + 1)
        } else {
            -1 - (y + 1).absoluteValue.div(dim.y + 1)
        }
        return Point(newX * (dim.x + 1), newY * (dim.y + 1))
    }

    private val comparator = compareBy<Point> { it.x }.thenBy { it.y }

    override fun compareTo(other: Point): Int {
        return comparator.compare(this, other)
    }

    override fun toString(): String {
        return "${Key.fromPoint(this)} Point($x, $y)"
    }
}

val ZERO_POINT = Point(0, 0)

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
    data object Rock : Item()
    data object Garden : Item()
}

data class Key(val x: Int, val y: Int) {
    companion object {
        fun fromPoint(point: Point): Key {
            return Key(
                toQuadrant(point.x),
                toQuadrant(point.y),
            )
        }

        private fun toQuadrant(n: Int): Int {
            return if (n > 0) {
                1
            } else if (n < 0) {
                -1
            } else {
                0
            }
        }
    }

    override fun toString(): String {
        val a = if (y > 0) {
            "S"
        } else if (y < 0) {
            "N"
        } else {
            ""
        }
        val b = if (x > 0) {
            "E"
        } else if (x < 0) {
            "W"
        } else {
            ""
        }
        return if (a.isEmpty() && b.isEmpty()) {
            "C"
        } else {
            "$a$b"
        }
    }
}

class SegmentStats {
    val inputs = mutableListOf<Set<Point>>()
    var loopFound = false

    override fun toString(): String {
        return "SegmentStats(inputs=${inputs.size}"
    }
}

fun pointsFromRange(xRange: IntRange, yRange: IntRange): Set<Point> {
    return xRange.flatMap { x ->
        yRange.map { y ->
            Point(x, y)
        }
    }.toSet()
}

class PuzzleMap {
    val stats0 = pointsFromRange(0..0, 0..0)
    val stats1 = pointsFromRange(-1..1, -1..1) + stats0
    val stats2 = pointsFromRange(-2..2, -2..2) + stats1
    val stats3 = pointsFromRange(-3..3, -3..3) + stats2

    var matrix = mutableMapOf<Point, Item>()
    var dim = ZERO_POINT
    val cache = mutableMapOf<Set<Point>, Set<Point>>()

    var state = mutableMapOf<Point, MutableSet<Point>>()
    var stats = mutableMapOf<Point, SegmentStats>()

    val countPerIteration = mutableMapOf<Int, Long>()
    val repeats = mutableListOf<Int>()

    fun add(point: Point, item: Item) {
        matrix[point] = item
        dim = Point(maxOf(dim.x, point.x), maxOf(dim.y, point.y))
    }

    fun calcReachable(start: Set<Point>): Set<Point> {
        require(start.all { it in matrix }) {
            "All start points must be in matrix"
        }
        val reachable = mutableSetOf<Point>()
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
        return reachable
    }

    var currentInterested: Set<Point>? = null
    var allInterested = mutableListOf<Set<Point>>()

    fun init() {
        allInterested = listOf(stats0, stats1, stats2, stats3).map {
            it.map { p -> p * Point(dim.x + 1, dim.y + 1) }.toSet()
        }.toMutableList()
        currentInterested = allInterested.removeFirst()
    }

    fun collectStats(segment: Point, points: Set<Point>): Boolean {
        val statsForThisSegment = stats.getOrPut(segment) { SegmentStats() }

        try {
            if (currentInterested == null) {
                return false
            }
            val currentInterested = currentInterested!!
            val isInterested = segment in currentInterested
            if (!isInterested) {
                return false
            }

            if (!statsForThisSegment.loopFound) {
                if (statsForThisSegment.inputs.contains(points)) {
                    // found loop
                    val indexOfRepeat = statsForThisSegment.inputs.indexOf(points)
//                    require(indexOfRepeat == statsForThisSegment.inputs.size - 2) {
//                        "Expected repeat to be ${statsForThisSegment.inputs.size - 2} element, but was $indexOfRepeat"
//                    }
                    statsForThisSegment.loopFound = true

                    val loops = stats.filter { (k, v) -> k in currentInterested && v.loopFound }
                    if (loops.size == currentInterested.size) {
                        println("Found loop for ${currentInterested}:")
                        for ((k, value) in loops) {
                            println(
                                "$k: (${value.inputs.size}) => ${
                                    value.inputs.withIndex().joinToString { (i, points) ->
                                        "${points.size}"
                                    }
                                }"
                            )
                        }
                        if (allInterested.isNotEmpty()) {
                            this.currentInterested = allInterested.removeFirst()
                        } else {
                            this.currentInterested = null
                        }
                        return true
                    }
                }
            }
        } finally {
            if (!statsForThisSegment.inputs.contains(points)) {
                statsForThisSegment.inputs.add(points)
            }
        }
        return false
    }

    fun move(i: Int) {
        val newState = mutableMapOf<Point, MutableSet<Point>>()
        state.forEach { (segment, points) ->
            val loop = collectStats(segment, points)
            if (loop) {
                repeats.add(i)
            }
            val reachable = cache.getOrPut(points) { calcReachable(points) }
            reachable.forEach { point ->
                val newSegment = point.segment(dim) + segment
                val newPoint = point.translate(dim)
                newState.getOrPut(newSegment) { mutableSetOf() }.add(newPoint)
            }
        }
        countPerIteration[i] = currentCount(state)
        state = newState

//        printMap(i)
    }

    fun currentCount(m: Map<Point, Set<Point>>) = m.values.sumOf { it.size.toLong() }

    fun printMap(iter: Int? = null) {
        if (iter != null) {
            println("------------- Iteration $iter ----------------------")
        } else {
            println("----------------------------------------------------")
        }
        val vert = state.keys.map { it.y }.distinct().sorted()
        val horiz = state.keys.map { it.x }.distinct().sorted()
        var sum = 0L
        for (y in vert) {
            print("${"%4d".format(y)}: ")
            for (x in horiz) {
                val p = Point(x, y)
                val points = state[p] ?: emptySet()
                val f = "${"%3d".format(points.size)} "
                if (p == ZERO_POINT) {
                    print("(${f})")
                } else {
                    print(" ${f} ")
                }

                sum += points.size
            }
            println()
        }
        require(sum == currentCount(state))
        println("Sum: $sum")
        println("----------------------------------------------------")
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
                    '#' -> puzzleMap.add(point, Item.Rock)
                    '.' -> puzzleMap.add(point, Item.Garden)
                    'S' -> {
                        puzzleMap.add(point, Item.Garden)
                        puzzleMap.state[ZERO_POINT] = mutableSetOf(point)
                    }

                    else -> error("Unknown char: $char")
                }
            }
        }
        puzzleMap.init()
        return puzzleMap
    }

    fun calcNum(a: Long, b: Long, coeff: Long, n: Long): Long {
        return a + (n - 1)* b + coeff * n * (n - 1) / 2
    }

    fun calcFinal(puzzleMap: PuzzleMap, iteration: Long): Long {
        val count = puzzleMap.repeats[2] - puzzleMap.repeats[1]

        val offsetIteration = iteration - puzzleMap.repeats[1]
        val indexIt = (offsetIteration % count).toInt()
        val valToCount = (offsetIteration / count).toLong()

        val start = puzzleMap.repeats[0]
        val i1 = start + count * 1 + indexIt + 1
        val i2 = start + count * 2 + indexIt + 1
        val i3 = start + count * 3 + indexIt + 1
        val a1 = puzzleMap.countPerIteration[i1]!!
        val a2 = puzzleMap.countPerIteration[i2]!!
        val a3 = puzzleMap.countPerIteration[i3]!!

        val coeff = (a3 - a2) - (a2 - a1)
        val startNum = (a2 - a1) - coeff

        return calcNum(a1, startNum, coeff,valToCount + 1 )
    }

    fun part1(input: List<String>): Any {
        val puzzleMap = parseInput(input)

//        val steps = 1200
        val steps = 1200
        for (i in 1..steps) {
            puzzleMap.move(i)
            if (puzzleMap.repeats.size == 3) {
                println("Found repeats: ${i}")
                puzzleMap.stats.clear()
            }
        }
//        println(puzzleMap.repeats)
//        println(puzzleMap.countPerIteration)

        val res = calcFinal(puzzleMap, 26501365)
//        val res = calcFinal(puzzleMap, 5000)
        println("Result: $res")
//        val count = puzzleMap.repeats[2] - puzzleMap.repeats[1]
//
//        for (i in 0..<count) {
//            val start = puzzleMap.repeats[0]
//            val i0 = start + i + 1
//            val i1 = start + count * 1 + i + 1
//            val i2 = start + count * 2 + i + 1
//            val i3 = start + count * 3 + i + 1
//            val a0 = puzzleMap.countPerIteration[i0]!!
//            val a1 = puzzleMap.countPerIteration[i1]!!
//            val a2 = puzzleMap.countPerIteration[i2]!!
//            val a3 = puzzleMap.countPerIteration[i3]!!
//
//            val coeff = (a3 - a2) - (a2 - a1)
//            val startNum = (a2 - a1) - coeff
//
//            println("Iteration $i: $a0 $a1 $a2 $a3 -> ${calcNum(a1, startNum, coeff, 1)} ${calcNum(a1, startNum, coeff,2)} ${calcNum(a1, startNum, coeff,3)}")
//        }


//        var prev: Long? = null
//        for (i in puzzleMap.countPerIteration.keys.sorted()) {
//            val marker = if (i in puzzleMap.repeats) {
//                "*"
//            } else {
//                " "
//            }
//
//            print("Iteration $i: $curr ")
//            if (prev != null) {
//                println("(${curr - prev}) $marker")
//            } else {
//                println()
//            }
//            prev = curr
//        }
//        puzzleMap.repeats.forEach { i ->
//            println("Iteration $i: ${puzzleMap.countPerIteration[i]}")
//        }

//        puzzleMap.printMap()
        val result = puzzleMap.state.values.sumOf { it.size }

        return result
    }

    fun part2(input: List<String>): Any {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
//    val testInput = readInput(day, "Day${day}_test")
//
//    val part1Expected = "16"
//    val part1 = measureTimedValue { part1(testInput) }
//    println("(Test) Part 1: expected: $part1Expected, got: $part1")

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")
//
    val input = readInput(day, "Day${day}")

    val part1Real = measureTimedValue { part1(input) }
    println("(Real) Part 1: $part1Real")

//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
