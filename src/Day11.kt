data class Point(val x: Int, val y: Int)

sealed class Kind {
    abstract val coord: Point
    abstract val char: String

    data class Space(override val char: String, override val coord: Point) : Kind()
    data class Galaxy(override val char: String, override val coord: Point) : Kind()
}

class Universe {
    var space = mutableMapOf<Point, Kind>()
    var dim: Point = Point(0, 0)

    fun printSpace() {
        for (y in 0..dim.y) {
            for (x in 0..dim.x) {
                val point = Point(x, y)
                val kind = space.getValue(point)
                print(kind.char)
            }
            print("\n")
        }
    }

    fun shiftRow(y: Int) {
        space = space.mapKeysTo(mutableMapOf()) { (point) ->
            if (point.y >= y) {
                Point(point.x, point.y + 1)
            } else {
                point
            }
        }
    }

    fun shiftColumn(x: Int) {
        space = space.mapKeysTo(mutableMapOf()) { (point) ->
            if (point.x >= x) {
                Point(point.x + 1, point.y)
            } else {
                point
            }
        }
    }

    companion object {
        fun expand(source: Universe): Universe {
            val newUniverse = Universe()
            val space = mutableMapOf<Point, Kind>() + source.space
            for (y in newUniverse.dim.y downTo 0) {
                val isAllSpaceY =
                    (0..newUniverse.dim.x).map { x -> Point(x, y) }.map { p -> space[p] }.all { it is Kind.Space }
                println("$y: $isAllSpaceY")
                if (isAllSpaceY) {
                    newUniverse.shiftRow(y)
                    newUniverse.space += (0..newUniverse.dim.x).map { x ->
                        val p = Point(x, y)
                        p to Kind.Space(".", p)
                    }
                }
            }
            newUniverse.dim = Point(space.keys.maxOf { it.x }, space.keys.maxOf { it.y })
            for (x in newUniverse.dim.x downTo 0) {
                val isAllSpaceX =
                    (0..newUniverse.dim.y).map { y -> Point(x, y) }.map { p -> space[p] }.all { it is Kind.Space }
                println("$x: $isAllSpaceX")

                if (isAllSpaceX) {
                    newUniverse.shiftColumn(x)
                    newUniverse.space += (0..newUniverse.dim.y).map { y ->
                        val p = Point(x, y)
                        p to Kind.Space(".", p)
                    }
                }
            }
            newUniverse.dim = Point(space.keys.maxOf { it.x }, space.keys.maxOf { it.y })

            return newUniverse
        }
    }

    fun main() {
        val day = "11"

        fun createUniverse(input: List<String>): Universe {
            val universe = Universe()
            var p: Point? = null
            for ((y, line) in input.withIndex()) {
                for ((x, char) in line.withIndex()) {
                    p = Point(x, y)
                    when (char) {
                        '.' -> universe.space[p] = Kind.Space(char.toString(), p)
                        '#' -> universe.space[p] = Kind.Galaxy(char.toString(), p)
                        else -> error("Unknown char $char")
                    }
                }
            }
            universe.dim = p!!
            return universe
        }

        fun part1(input: List<String>): Any {
            val universe = createUniverse(input)
            val expandedUniverse = Universe.expand(universe)
            expandedUniverse.printSpace()
            return input.size
        }

        fun part2(input: List<String>): Any {
            return input.size
        }

        // test if implementation meets criteria from the description, like:
        val testInput = readInput("Day${day}_test")

        val part1Expected = ""
        val part1 = part1(testInput)
        println("(Test) Part 1: expected: $part1Expected, got: $part1")

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")
//
//    val input = readInput("Day${day}")
//
//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
    }
}
