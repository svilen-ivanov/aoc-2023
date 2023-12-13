sealed class Item {
    abstract val id: Int

    data class Ash(override val id: Int) : Item() {
        val x = 1
        override fun toString(): String {
//            return ". | ${"%03d".format(id)} | "
            return "."
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Ash

            return x == other.x
        }

        override fun hashCode(): Int {
            return x
        }
    }


    data class Rock(override val id: Int) : Item() {
        val x = 2
        override fun toString(): String {
//            return "# | ${"%03d".format(id)} | "
            return "#"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Rock

            return x == other.x
        }

        override fun hashCode(): Int {
            return x
        }


    }
}

data class Point(val x: Long, val y: Long) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)
    operator fun unaryMinus() = Point(-x, -y)

    override fun toString(): String {
        return "($x, $y)"
    }
}


data class Line(
    val items: MutableList<Item>,
) {
    override fun toString(): String {
        return items.joinToString("")
    }

}

class Mirror {
    lateinit var dim: Point
    fun update() {
        dim = Point(matrix.keys.maxOf { it.x }, matrix.keys.maxOf { it.y })
    }

    val matrix = mutableMapOf<Point, Item>()

    override fun toString(): String {
        return buildString {
            for (y in 0..dim.y) {
                for (x in 0..dim.x) {
                    val p = Point(x, y)
                    val item = matrix[p]
                    append("${item}")
                }
                appendLine()
            }
        }
    }

    fun equalRows(y1: Long, y2: Long): Boolean {
        for (x in 0..dim.x) {
            val p1 = Point(x, y1)
            val p2 = Point(x, y2)
            val item1 = matrix[p1]
            val item2 = matrix[p2]
            if (item1 != item2) {
                return false
            }
        }
        return true
    }

    fun equalCols(x1: Long, x2: Long): Boolean {
        for (y in 0..dim.y) {
            val p1 = Point(x1, y)
            val p2 = Point(x2, y)
            val item1 = matrix[p1]
            val item2 = matrix[p2]
            if (item1 != item2) {
                return false
            }
        }
        return true
    }

    fun findMirrorRows(): Long? {
        val pairs = (0L..dim.y).windowed(2)
        for ((l, r) in pairs) {
            if (findMirrorPivotRow(l, r)) {
                return l + 1
            }
        }
        return null
    }

    private fun findMirrorPivotRow(l: Long, r: Long): Boolean {
        var i = 0L
        while (true) {
            if ((l - i) < 0 || (r + i) > dim.y) {
                return true
            }
            if (!equalRows(l - i, r + i)) {
                return false
            }
            i++
        }
    }

    fun findMirrorCols(): Long? {
        val pairs = (0L..dim.x).windowed(2)
        for ((l, r) in pairs) {
            if (findMirrorPivotCols(l, r)) {
                return l + 1
            }
        }
        return null
    }

    fun findMirrorCols2(): List<Long> {
        val res = mutableListOf<Long>()
        val pairs = (0L..dim.x).windowed(2)
        for ((l, r) in pairs) {
            if (findMirrorPivotCols(l, r)) {
                res.add(l + 1)
            }
        }
        return res
    }

    fun findMirrorRows2(): List<Long> {
        val res = mutableListOf<Long>()

        val pairs = (0L..dim.y).windowed(2)
        for ((l, r) in pairs) {
            if (findMirrorPivotRow(l, r)) {
                res.add(l + 1)
            }
        }
        return res
    }

    private fun findMirrorPivotCols(l: Long, r: Long): Boolean {
        var i = 0L
        while (true) {
            if ((l - i) < 0 || (r + i) > dim.x) {
                return true
            }
            if (!equalCols(l - i, r + i)) {
                return false
            }
            i++
        }
    }

}

fun main() {
    val day = "13"

    fun parseMirrors(input: List<String>): MutableList<Mirror> {
        val mirrors = mutableListOf(Mirror())
        var y = 0
        var id = 0
        for (inputLine in input) {
            if (inputLine == "") {
                mirrors.add(Mirror())
                y = 0
                id = 0
            } else {
                val mirror = mirrors.last()
                for ((x, c) in inputLine.withIndex()) {
                    val item = when (c) {
                        '.' -> Item.Ash(id)
                        '#' -> Item.Rock(id)
                        else -> throw Exception("Unknown item: $c")
                    }
                    mirror.matrix[Point(x.toLong(), y.toLong())] = item
                    id++
                }
                y++
            }
        }

        mirrors.forEach { it.update() }

        return mirrors
    }

    fun fixSmudges(mirror: Mirror): Pair<Long?, Long?> {
        val reflectionLines = mutableListOf<Pair<Long?, Long?>>()
        val originalRow = mirror.findMirrorRows()
        val originalCol = mirror.findMirrorCols()
        require(originalRow == null || originalCol == null)
        require(originalRow != null || originalCol != null)

        val uniqLines = mutableSetOf<Pair<Long?, Long?>>()
//        return originalRow to originalCol
        var count = 0L
        try {
            for ((p, item) in mirror.matrix) {
                count++
                val newMatrix = mirror.matrix.toMutableMap()
                require(newMatrix !== mirror.matrix)
                when (item) {
                    is Item.Ash -> newMatrix[p] = Item.Rock(item.id)
                    is Item.Rock -> newMatrix[p] = Item.Ash(item.id)
                }
                require(newMatrix != mirror.matrix)

                val newMirror = Mirror()
                newMirror.matrix.putAll(newMatrix)
                newMirror.update()

                val newRow = newMirror.findMirrorRows2().firstOrNull { it != originalRow }
                val newCol = newMirror.findMirrorCols2().firstOrNull { it != originalCol }

                if ((newRow == null && newCol == null)) {
                    continue
                }

//                println("Found new line: $newRow, $newCol")
//                uniqLines.add(newRow to newCol)

                if (newRow != null) {
                    return newRow to null
                }
                if (newCol != null) {
                    return null to newCol
                }
            }
        } finally {
            println("($originalRow, $originalCol) Uniq:\n${uniqLines.joinToString("\n")}")
        }
        require(count == mirror.matrix.size.toLong())
//        println("Original: ($originalRow, $originalCol) Uniq:\n${uniqLines.joinToString("\n")}")
        // 21642
        error("No smudges found: Original row: $originalRow, original col: $originalCol ($count, ${mirror.dim})")
//        println("No smudges found: Original row: $originalRow, original col: $originalCol ($count, ${mirror.dim})")
//        return originalRow to originalCol
    }

    fun part1(input: List<String>): Any {
        val mirrors = parseMirrors(input)
        var sum = 0L
        for (mirror in mirrors) {
            println("Mirror:\n-----\n$mirror")
            val (row, col) = fixSmudges(mirror)
//            var row = mirror.findMirrorRows()
//            var cols = mirror.findMirrorCols()
            println("Rows: $row, Columns: $col")
            if (row != null) {
                sum += (row) * 100L
            }
            if (col != null) {
                sum += col
            }

        }
        return sum
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

    val input = readInput("Day${day}")

    val part1Real = part1(input)
    println("(Real) Part 1: $part1Real")

//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
