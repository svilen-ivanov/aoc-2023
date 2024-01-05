package aoc2023.prev

fun main() {
    val day = "03"

    data class Point(val x: Int, val y: Int)

    data class Line(val p1: Point, val p2: Point) {
        init {
            require(p1.x == p2.x || p1.y == p2.y)
            require(p1.x <= p2.x || p1.y <= p2.y)
        }
        fun isPointOnLine(p: Point): Boolean {
            return p1.x == p2.x && p.x == p1.x && p1.y <= p.y && p.y <= p2.y
                    || p1.y == p2.y && p.y == p1.y && p1.x <= p.x && p.x <= p2.x
        }
    }

    data class Symbol(val c: Char, val point: Point)

    data class PartNumber(val n: Int, val x: Int, private val y: Int) {
        val x1 = x - 1
        val x2 = x1 + n.toString().length + 1
        val y1 = y - 1
        val y2 = y + 1

        val outline= listOf(
            Line(Point(x1, y1), Point(x2, y1)),
            Line(Point(x2, y1), Point(x2, y2)),
            Line(Point(x1, y2), Point(x2, y2)),
            Line(Point(x1, y1), Point(x1, y2)),
        )

        fun isOnOutline(symbols: List<Symbol>): Boolean {
            return symbols.any { s->
                outline.any { l ->
                    l.isPointOnLine(s.point)
                }
            }
        }
    }

    val partRe = Regex("\\d+")
    val symbolRe = Regex("[^\\d.]")

    fun part1(input: List<String>): Int {
        val partNumbers = mutableListOf<PartNumber>()
        val symbols = mutableListOf<Symbol>()
        for ((y, line) in input.withIndex()) {
            partRe.findAll(line).forEach { m ->
                partNumbers.add(PartNumber(m.value.toInt(), m.range.first, y))
            }
            symbolRe.findAll(line).forEach { m ->
                symbols.add(Symbol(m.value[0], Point(m.range.first, y)))
            }
        }

        return partNumbers.filter { it.isOnOutline(symbols) }.sumOf { it.n  }
    }

    fun part2(input: List<String>): Int {
        val partNumbers = mutableListOf<PartNumber>()
        val symbols = mutableListOf<Symbol>()
        for ((y, line) in input.withIndex()) {
            partRe.findAll(line).forEach { m ->
                partNumbers.add(PartNumber(m.value.toInt(), m.range.first, y))
            }
            symbolRe.findAll(line).forEach { m ->
                symbols.add(Symbol(m.value[0], Point(m.range.first, y)))
            }
        }

        val gears = symbols.filter { it.c == '*' }
        return gears.sumOf {
            val gearRatio = partNumbers.filter { p -> p.isOnOutline(listOf(it)) }
            if (gearRatio.size < 2) {
                0
            } else if (gearRatio.size == 2) {
                println(gearRatio)
                gearRatio.fold(1) { acc, p -> acc * p.n }.toInt()

            } else {
                error("ERROR: more than 2 part numbers on outline of gear")
            }
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

    val part1Expected = "4361"
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = "467835"
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput("Day${day}")

    val part1Real = part1(input)
    println("(Real) Part 1: $part1Real")

    val part2Real = part2(input)
    println("(Real) Part 2: $part2Real")
}
