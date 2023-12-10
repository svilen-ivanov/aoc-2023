data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    fun isInvalid(): Boolean {
        return x < 0 || y < 0
    }
}

sealed class Tile(val tile: String, val tileChar: String, val pos: Point) {
    class Ground(pos: Point) : Tile(".", ".", pos)
    class Start(pos: Point) : Tile("S", "S", pos) {
        fun getStart(fieldMap: FieldMap): List<Pipe> {
            return buildList {
                val points = listOf(
                    Point(0, -1),
                    Point(0, 1),
                    Point(-1, 0),
                    Point(1, 0),
                )
                for (point in points) {
                    val neighbour = point + pos
                    if (neighbour.isInvalid()) {
                        continue
                    }
                    val tile = fieldMap.tiles.getValue(neighbour)
                    if (tile is Pipe) {
                        add(tile)
                    }
                }
            }
        }
    }

    override fun toString(): String {
        return "Tile(tile='$tile', tileChar='$tileChar', pos=$pos)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tile

        return pos == other.pos
    }

    override fun hashCode(): Int {
        return pos.hashCode()
    }


}

sealed class Pipe(
    tile: String,
    tileChar: String,
    val conn1: Point,
    val conn2: Point,
    pos: Point,
) : Tile(tile, tileChar, pos) {
    class Vertical(pos: Point) : Pipe("┃", "|", Point(0, -1), Point(0, -1), pos)
    class Horizontal(pos: Point) : Pipe("━", "-", Point(-1, 0), Point(1, 0), pos)
    class NorthEast(pos: Point) : Pipe("┗", "L", Point(0, -1), Point(1, 0), pos)
    class NorthWest(pos: Point) : Pipe("┛", "J", Point(-1, 0), Point(0, -1), pos)
    class SouthEast(pos: Point) : Pipe("┏", "F", Point(0, 1), Point(1, 0), pos)
    class SouthWest(pos: Point) : Pipe("┓", "7", Point(-1, 0), Point(0, 1), pos)

    var next: Point? = null

    fun moveFrom(from: Point) {
        require (next == null)
        next = when (from) {
            conn1 -> conn2
            conn2 -> conn1
            else -> error("Invalid move from $from")
        }
    }

    fun getNext(fieldMap: FieldMap): Pipe? {
        require(next != null)
        val nextTile = fieldMap.tiles.getValue(next)
        return if (nextTile is Pipe) {
            nextTile
        } else {
            null
        }
    }
}

data class FieldMap(val tiles: Map<Point, Tile>, val dim: Point) {
    fun printField() {
        for (y in 0..dim.y) {
            for (x in 0..dim.x) {
                val point = Point(x, y)
                val tile = tiles.getValue(point)
                print(tile.tile)
            }
            print("\n")
        }
    }

    val start: Tile.Start = (tiles.values.first { it is Tile.Start } as Tile.Start).also {
        require(tiles.values.count { it is Tile.Start } == 1)
    }
}

fun main() {
    val day = "10"

    fun fixTiles(input: String): String {
        return input
            .replace('|', '┃')
            .replace('-', '━')
            .replace('L', '┗')
            .replace('J', '┛')
            .replace('F', '┏')
            .replace('7', '┓')
    }

    fun parseFieldMap(input: List<String>): FieldMap {
        val tiles = mutableMapOf<Point, Tile>()
        var dim: Point? = null
        for ((y, line) in input.withIndex()) {
            for ((x, tileStr) in line.withIndex()) {
                val pos = Point(x, y)
                val tile = when (tileStr) {
                    '.' -> Tile.Ground(pos)
                    'S' -> Tile.Start(pos)
                    '|' -> Pipe.Vertical(pos)
                    '-' -> Pipe.Horizontal(pos)
                    'L' -> Pipe.NorthEast(pos)
                    'J' -> Pipe.NorthWest(pos)
                    'F' -> Pipe.SouthEast(pos)
                    '7' -> Pipe.SouthWest(pos)
                    else -> error("Unknown tile: $tileStr")
                }
                tiles[pos] = tile
                dim = if (dim == null) {
                    pos
                } else {
                    Point(maxOf(dim.x, pos.x), maxOf(dim.y, pos.y))
                }
            }
        }
        requireNotNull(dim)
        return FieldMap(tiles, dim)
    }

    class Traverse(val fieldMap: FieldMap, val start: Tile.Start) {

        val current
        fun tick() {
            val next = start.mapNotNull { it.getNext(fieldMap) }
            for (pipe in next) {
                pipe.moveFrom(start.first().pos)
            }
        }
    }

    fun part1(input: List<String>): Any {
        val fieldMap = parseFieldMap(input)
        fieldMap.printField()
        val startPipes = fieldMap.start.getStart(fieldMap)

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

//    val input = readInput("Day${day}")
//
//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
