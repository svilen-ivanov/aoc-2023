import kotlin.math.abs

data class Point3(val x: Long, val y: Long) {
    fun distanceTo(other: Point3): Long {
        return abs(x - other.x) + abs(y - other.y) + (if (x != other.x) 1 else 0) + (if (y != other.y) 1 else 0)
    }
}

sealed class Kind {
    abstract var coord: Point3
    abstract val char: String
    var num = 0

    data class Space(override val char: String, override var coord: Point3) : Kind()
    data class Galaxy(override val char: String, override var coord: Point3) : Kind()
}

class Universe {
    var space = mutableMapOf<Point3, Kind>()

    //    val grow = 9
    val grow = (1_000_000 - 1).toLong()
    var dim: Point3 = Point3(0, 0)
    val galaxies by lazy { space.filterValues { it is Kind.Galaxy } as Map<Point3, Kind.Galaxy> }
    val galaxyByNumber by lazy { galaxies.values.associateBy { it.num } }

    val rowsWithPoints = mutableSetOf<Long>()
    val colsWithPoints = mutableSetOf<Long>()

    val pairs by lazy {
        val gList = galaxies.values.toList()
        val pairs = mutableListOf<Pair<Kind.Galaxy, Kind.Galaxy>>()
        for (g1 in 0..<gList.size) {
            for (g2 in (g1 + 1)..<gList.size) {
                pairs.add(Pair(gList[g1], gList[g2]))
            }
        }
        pairs.toList()
    }

    fun renumber() {
        var num = 1
        for (g in galaxies) {
            g.value.num = num
            num += 1
        }
    }

    fun printSpace() {
        for (y in 0..dim.y) {
            for (x in 0..dim.x) {
                val point = Point3(x, y)
                val kind = space[point]
                print(
                    when (kind) {
                        null, is Kind.Space -> "."
                        is Kind.Galaxy -> kind.num
                    }
                )
            }
            print("\n")
        }
    }

    fun shiftRow(y: Long) {
        space = space.mapKeysTo(mutableMapOf()) { (point) ->
            if (point.y >= y) {
                Point3(point.x, point.y + grow)
            } else {
                point
            }
        }.onEach { (p, k) ->
            k.coord = p
        }
        dim = Point3(dim.x, dim.y + grow)
        recalcPoints()
    }

    private fun recalcPoints() {
        rowsWithPoints.clear()
        colsWithPoints.clear()
        space.forEach { (p, k) ->
            if (k is Kind.Galaxy) {
                rowsWithPoints.add(p.y)
                colsWithPoints.add(p.x)
            }
        }
    }


    fun shiftColumn(x: Long) {
        space = space.mapKeysTo(mutableMapOf()) { (point) ->
            if (point.x >= x) {
                Point3(point.x + grow, point.y)
            } else {
                point
            }
        }.onEach { (p, k) ->
            k.coord = p
        }
        recalcPoints()
        dim = Point3(dim.x + grow, dim.y)
    }

    fun expand() {
        recalcPoints()

        var y = 0L
        while (y <= dim.y) {
//            val isAllSpaceY =
//                (0..dim.x).map { px -> Point(px, y) }.map { p -> space[p] }.all { it is Kind.Space || it == null }
//            println("$y: $isAllSpaceY")
            val isAllSpaceY = !rowsWithPoints.contains(y)
            if (isAllSpaceY) {
                shiftRow(y)
//                space += (0..dim.x).map { px ->
//                    val p = Point(px, y)
//                    p to Kind.Space(".", p)
//                }
                y += grow
            }
            y += 1
        }

        var x = 0L
        while (x <= dim.x) {
//            val isAllSpaceX =
//                (0..dim.y).map { py -> Point(x, py) }.map { p -> space[p] }.all { it is Kind.Space || it == null }
//            println("$x: $isAllSpaceX")
            val isAllSpaceX = !colsWithPoints.contains(x)

            if (isAllSpaceX) {
                shiftColumn(x)
//                space += (0..dim.y).map { py ->
//                    val p = Point(x, py)
//                    p to Kind.Space(".", p)
//                }
                x += grow
            }
            x += 1
        }

        renumber()
    }

    companion object {
        fun expand(source: Universe): Universe {
            val newUniverse = Universe()
            newUniverse.space = (mutableMapOf<Point3, Kind>() + source.space).toMutableMap()
            newUniverse.dim = source.dim
            newUniverse.expand()
            return newUniverse
        }
    }
}

fun main() {
    val day = "11"

    fun createUniverse(input: List<String>): Universe {
        val universe = Universe()
        var p: Point3? = null
        for ((y, line) in input.withIndex()) {
            for ((x, char) in line.withIndex()) {
                p = Point3(x.toLong(), y.toLong())
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
        val u1 = createUniverse(input)
        val universe = Universe.expand(u1)
//        universe.printSpace()
//        for ((g1, g2) in listOf(
//            universe.galaxyByNumber[5]!! to universe.galaxyByNumber[9]!!,
//            universe.galaxyByNumber[1]!! to universe.galaxyByNumber[7]!!,
//            universe.galaxyByNumber[3]!! to universe.galaxyByNumber[6]!!,
//            universe.galaxyByNumber[8]!! to universe.galaxyByNumber[9]!!,
//        )) {
//            println("${g1.num}, ${g2.num} -> ${g1.coord.distanceTo(g2.coord)}")
//        }
        val x = universe.pairs.sumOf { (g1, g2) ->
            g1.coord.distanceTo(g2.coord)
        }
        println("Sum: ${x}")
        return x
    }

    fun part2(input: List<String>): Any {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

    val part1Expected = "1030"
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")
//
    val input = readInput("Day${day}")
//
    val part1Real = part1(input)
    println("(Real) Part 1: 9684228 $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}

