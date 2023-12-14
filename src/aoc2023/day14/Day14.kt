package aoc2023.day14

import readInput

data class Iteration(val i: Int, val load: Long)

sealed class DishItem {
    abstract val char: String

    object Empty : DishItem() {
        override val char = "."
    }

    object Cube : DishItem() {
        override val char = "#"
    }

    class Round(val i: Int) : DishItem() {
        var sequenceIndex: Int? = null

        //        val intermediatePositions = mutableListOf<Point>()
        val finalPositions = mutableListOf<Point>()
        override val char = "O"

        fun checkSequence() {
            if (sequenceIndex == null) {
                sequenceIndex = findBackToBackSequences(finalPositions)
            }
        }

        fun hasSequence() = sequenceIndex != null
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

    lateinit var rounds: List<DishItem.Round>

    fun complete() {
        rounds = dish.values.filterIsInstance<DishItem.Round>()
    }

    override fun toString(): String {
        return buildString {
            for (y in 0..dim.y) {
                for (x in 0..dim.x) {
                    val p = Point(x, y)
                    val item = dish.getValue(p)
//                    if (item is DishItem.Round) {
//                        append("(%03d)".format(item.i))
//                    } else {
//                        append("( ${item.char} )")
//                    }
                    append(item.char)
                }
                appendLine()
            }
        }
    }

    fun getLoad(): Long {
        val rounds = dish.entries.filter { (p, i) -> i is DishItem.Round }.map { (p, i) ->
            dim.y - p.y + 1
        }.sum()
        return rounds.toLong()
    }

    fun tilt(vector: Point) {
        if (vector.x == 0) {
            for (x in 0..dim.x) {
                val points = (0..dim.y).map { y -> Point(x, y) }
                sortAndMerge(points, vector.y)
            }
        } else {
            for (y in 0..dim.y) {
                val points = (0..dim.x).map { x -> Point(x, y) }
                sortAndMerge(points, vector.x)
            }
        }
    }

    val comparator1: (DishItem, DishItem) -> Int = { o1, o2 ->
        when {
            o1 is DishItem.Empty && o2 is DishItem.Round -> -1
            o1 is DishItem.Round && o2 is DishItem.Empty -> 1
            else -> 0
        }
    }

    private val comparator2: (DishItem, DishItem) -> Int = { o1, o2 ->
        comparator1(o1, o2) * -1
    }


    private fun sortAndMerge(points: List<Point>, dir: Int) {
        val values = points.mapTo(mutableListOf()) { p -> dish.getValue(p) }
        var lastCube = 0
        val comparator = if (dir > 0) comparator1 else comparator2
        for (i in values.indices) {
            if (values[i] is DishItem.Cube || i == values.size - 1) {
                values.subList(lastCube, i + 1).sortWith(comparator)
                lastCube = i + 1
            }
        }

        points.zip(values).forEach { (p, i) ->
            if (i is DishItem.Round || i is DishItem.Empty) {
                dish[p] = i
            }
        }
    }

    private fun recordPositionAfterTilt() {
        dish.entries.forEach { (p, i) ->
            if (i is DishItem.Round) {
                i.finalPositions.add(p)
//                i.checkSequence()
            }
        }
    }

    //    1_000_000_000
    val vectors = listOf(Point(0, -1), Point(-1, 0), Point(0, 1), Point(1, 0))
    fun tilt(): MutableSet<Point> {
        for (vector in vectors) {
            tilt(vector)
        }

        val positions = dish.entries.filter { (p, i) -> i is DishItem.Round }.mapTo(mutableSetOf()) { (p, i) -> p }
        return positions

//        recordPositionAfterTilt()
//        val hasAllSeq = rounds.all { round -> round.hasSequence() }
//        if (hasAllSeq) {
//            println("All has sequence")
//            return true
//        } else {
//            return false
//        }
    }
}

fun main() {
    val day = "14"

    fun part1(input: List<String>): Any {
        val dish = Dish()
        var count = 0
        for (y in input.indices) {
            for (x in input[y].indices) {
                val point = Point(x, y)
                dish.add(
                    point, when (input[y][x]) {
                        '.' -> DishItem.Empty
                        '#' -> DishItem.Cube
                        'O' -> DishItem.Round(count++)
                        else -> throw Exception("Unknown item")
                    }
                )
            }
        }
        dish.complete()
//        dish.tilt(Point(0, -1))


        val pos2 = mutableMapOf<Int, Set<Point>>()
        val currentPositions = mutableMapOf<Set<Point>, Iteration>()
        for (i in 1..1000) {
            val currentPos = dish.tilt()
            if (currentPositions.contains(currentPos)) {
                println("Found duplicate at $i")
                currentPositions.entries.sortedBy { (k, v) -> v.i }.forEach { (p, l) ->
                    println("$l")
                }
                currentPositions.clear()
            }
            currentPositions[currentPos] = Iteration(i, dish.getLoad())
//            println(dish)
        }
////        pos2.forEach {
////            println("${it.key}: ${it.value}")
////        }

        val result = dish.getLoad()
        return result
    }

    fun part2(input: List<String>): Any {
        return input.size
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput(day, "Day${day}_test")

//    val part1Expected = "136"
//    val part1 = part1(testInput)
//    println("(Test) Part 1: expected: $part1Expected, got: $part1")

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")
//
    val input = readInput(day, "Day${day}")
//
    val part1Real = part1(input)
    println("(Real) Part 1: 108641 $part1Real")

//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}

//fun test() {
//    val list = listOf(1, 10, 20, 10, 20, 10, 20, 10)
//    findBackToBackSequences(list)
//}
fun <T> findBackToBackSequences(list: List<T>): Int? {
    for (length in (2..(list.size / 2)).reversed()) {
        for (i in 0..(list.size - 2 * length)) {
            if (list.subList(i, i + length) == list.subList(i + length, i + 2 * length)) {
                println("Back-to-back sequence found: ${list.subList(i, i + length)} starting at index $i")
                return i
            }
        }
    }
//    println("No back-to-back sequences found")
    return null
}

