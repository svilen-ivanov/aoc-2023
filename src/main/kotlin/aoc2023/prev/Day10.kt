package aoc2023.prev

data class Point2(val x: Int, val y: Int) {
    operator fun plus(other: Point2): Point2 {
        return Point2(x + other.x, y + other.y)
    }

    fun isInvalid(fieldMap: FieldMap): Boolean {
        return x < 0 || y < 0 || x > fieldMap.dim.x || y > fieldMap.dim.y
    }

    fun reverse(): Point2 {
        return Point2(-x, -y)
    }
}

sealed class Tile(val tile: String, val tileChar: String, val pos: Point2) {
    var mark: String? = null
    class Ground(pos: Point2) : Tile(".", ".", pos)
    class Start(pos: Point2) : Tile("S", "S", pos) {
        fun getStart(fieldMap: FieldMap): List<Pipe> {
            return buildList {
                val points = listOf(
                    Point2(0, -1),
                    Point2(0, 1),
                    Point2(-1, 0),
                    Point2(1, 0),
                )
                for (point in points) {
                    val neighbour = point + pos
                    if (neighbour.isInvalid(fieldMap)) {
                        continue
                    }
                    val tile = fieldMap.tiles.getValue(neighbour)
                    if (tile is Pipe) {
                        val point1 = tile.conn1 + tile.pos
                        val point2 = tile.conn2 + tile.pos
//                        println("$tile: Point1: $point1, point2: $point2")
                        if (point1 == pos || point2 == pos) {
                            add(tile)
                        }
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
    val conn1: Point2,
    val conn2: Point2,
    pos: Point2,
) : Tile(tile, tileChar, pos) {
    class Vertical(pos: Point2) : Pipe("┃", "|", Point2(0, -1), Point2(0, 1), pos)
    class Horizontal(pos: Point2) : Pipe("━", "-", Point2(-1, 0), Point2(1, 0), pos)
    class NorthEast(pos: Point2) : Pipe("┗", "L", Point2(0, -1), Point2(1, 0), pos)
    class NorthWest(pos: Point2) : Pipe("┛", "J", Point2(-1, 0), Point2(0, -1), pos)
    class SouthEast(pos: Point2) : Pipe("┏", "F", Point2(0, 1), Point2(1, 0), pos)
    class SouthWest(pos: Point2) : Pipe("┓", "7", Point2(-1, 0), Point2(0, 1), pos)
//    class Vertical(pos: Point) : Pipe("|", "|", Point(0, -1), Point(0, 1), pos)
//    class Horizontal(pos: Point) : Pipe("-", "-", Point(-1, 0), Point(1, 0), pos)
//    class NorthEast(pos: Point) : Pipe("\\", "L", Point(0, -1), Point(1, 0), pos)
//    class NorthWest(pos: Point) : Pipe("/", "J", Point(-1, 0), Point(0, -1), pos)
//    class SouthEast(pos: Point) : Pipe("/", "F", Point(0, 1), Point(1, 0), pos)
//    class SouthWest(pos: Point) : Pipe("\\", "7", Point(-1, 0), Point(0, 1), pos)


//    var next: Point? = null

//    fun getNext(fieldMap: FieldMap): Pipe? {
//        require(next != null)
//        val nextTile = fieldMap.tiles.getValue(next)
//        return if (nextTile is Pipe) {
//            nextTile
//        } else {
//            null
//        }
//    }
}

data class FieldMap(val tiles: Map<Point2, Tile>, val dim: Point2) {
    fun closeLoop(): FieldMap {
        val start = start
        val startTiles = start.getStart(this@FieldMap)
        val tiles = buildMap<Point2, Tile> {
            putAll(tiles)
            val c1 = findConnection(start.pos, startTiles[0])
            val c2 = findConnection(start.pos, startTiles[1])
            for (newTile in (listOf(
                    Pipe.Vertical(start.pos),
                    Pipe.Horizontal(start.pos),
                    Pipe.NorthEast(start.pos),
                    Pipe.NorthWest(start.pos),
                    Pipe.SouthEast(start.pos),
                    Pipe.SouthWest(start.pos),
                ))) {
                if ((newTile.conn1 == c1 && newTile.conn1 == c2) || (newTile.conn1 == c2 || newTile.conn1 == c1)) {
                    put(newTile.pos, newTile)
                }
            }
            println("Start: $start, c1: $c1, c2: $c2")
        }
        return FieldMap(tiles, dim)
    }

    private fun findConnection(pos: Point2, pipe: Pipe): Any {
        if (pos == pipe.pos + pipe.conn1) {
            return pipe.conn1.reverse()
        } else if (pos == pipe.pos + pipe.conn2) {
            return pipe.conn2.reverse()
        } else {
            error("Invalid pipe: $pipe")
        }
    }

    fun printField() {
        for (y in 0..dim.y) {
            for (x in 0..dim.x) {
                val point = Point2(x, y)
                val tile = tiles.getValue(point)
//                print(tile.mark ?: tile.tile)
                print(tile.mark ?: when (tile) {
//                    is Pipe -> "▉"
                    else -> tile.tile
                })
            }
            print("\n")
        }
    }

    val start: Tile.Start by lazy {
        (tiles.values.first { it is Tile.Start } as Tile.Start).also {
            require(tiles.values.count { it is Tile.Start } == 1)
        }
    }
    val startPipes by lazy { start.getStart(this) }

//    val byRow by lazy { tiles.entries.groupBy({ it.key.y }) {
//        it.value
//    }
}

class TraversePipe(val pipe: Pipe, val from: Point2) {
    val next = when (from) {
        pipe.pos + pipe.conn1 -> pipe.pos + pipe.conn2
        pipe.pos + pipe.conn2 -> pipe.pos + pipe.conn1
        else -> error("Invalid move from $from")
    }
}

class Traverse(val fieldMap: FieldMap, val startPipe: Pipe) {
    val traversed = mutableMapOf<Point2, Tile>()
    var steps = 0
    fun traverse(): Boolean {
        traversed.put(fieldMap.start.pos, fieldMap.start)
        var current = TraversePipe(startPipe, fieldMap.start.pos)
        while (true) {
            traversed.put(current.pipe.pos, current.pipe)
            steps++
            val nextPosition = current.next
            if (nextPosition.isInvalid(fieldMap)) {
                println("Invalid position: $nextPosition")
                return false
            }
            val nextTile = fieldMap.tiles.getValue(nextPosition)
//            println("------")
//            this.printField()
//            println("------")
            when {
                nextTile is Tile.Ground -> return false
                nextTile is Tile.Start -> {
                    println("Reached start")
                    return true
                }
                nextTile is Pipe ->
                    current = TraversePipe(nextTile, current.pipe.pos)

                else -> error("Invalid tile: $nextTile")
            }
        }
    }

    fun toFieldLoop(): FieldMap {
        val fieldLoop = mutableMapOf<Point2, Tile>()
        val dim = fieldMap.dim
        for (y in 0..dim.y) {
            for (x in 0..dim.x) {
                val point = Point2(x, y)
                val tile = traversed[point]
                fieldLoop[point] = tile ?: Tile.Ground(point)
            }
        }
        return FieldMap(fieldLoop, dim)
    }


    fun printField() {
        val dim = fieldMap.dim
        for (y in 0..dim.y) {
            for (x in 0..dim.x) {
                val point = Point2(x, y)
                val tile = traversed[point]
                print(tile?.tile ?: ".")
            }
            print("\n")
        }
        println("Steps: ${(steps + 1) / 2}")
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
        val tiles = mutableMapOf<Point2, Tile>()
        var dim: Point2? = null
        for ((y, line) in input.withIndex()) {
            for ((x, tileStr) in line.withIndex()) {
                val pos = Point2(x, y)
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
                    Point2(maxOf(dim.x, pos.x), maxOf(dim.y, pos.y))
                }
            }
        }
        requireNotNull(dim)
        return FieldMap(tiles, dim)
    }


    fun part1(input: List<String>): Any {
        val fieldMap = parseFieldMap(input)
//        fieldMap.printField()
        for (startPipe in fieldMap.startPipes) {
            println("Start pipe: $startPipe")
            val traverse = Traverse(fieldMap, startPipe)
            traverse.traverse()
            traverse.printField()
//            break
        }

        return 0
    }


    fun isInside(tile: Tile, loop: FieldMap): Boolean {
//        tile.mark = "X"
        if (tile !is Tile.Ground) {
            return false
        }
        var topCount = 0
        var bottomCount = 0
        val tilePoint = tile.pos
        for (x in (tilePoint.x + 1)..loop.dim.x) {
            val point = Point2(x, tilePoint.y)
            val tileAtPos = loop.tiles.getValue(point)
            if (tileAtPos is Tile.Ground) {
                continue
            } else {
                val hasTop = tileAtPos is Pipe.Vertical
                        || tileAtPos is Pipe.NorthEast
                        || tileAtPos is Pipe.NorthWest
                if (hasTop) {
                    topCount++
                }
                val hasBottom = tileAtPos is Pipe.Vertical
                        || tileAtPos is Pipe.SouthWest
                        || tileAtPos is Pipe.SouthEast
                if (hasBottom) {
                    bottomCount++
                }
            }
        }

        val inside = (topCount % 2 == 1) && (bottomCount % 2 == 1)
        println("Tile $tilePoint: $inside ($topCount, $bottomCount)")

        if (inside) {
            tile.mark = "▉"
        }

        return inside
    }


    fun findEnclosing(loop: FieldMap) {
//        loop.printField()

//        val tile1 = loop.tiles.getValue(Point(0, 0))
//        println("$tile1 -> ${isInside(tile1, loop)}")
//        loop.printField()
//
//        val tile2 = loop.tiles.getValue(Point(11, 4))
//        println("$tile2 -> ${isInside(tile2, loop)}")
//        loop.printField()

        var count = 0;
        for (y in 0..loop.dim.y) {
            for (x in 0..loop.dim.x) {
                val point = Point2(x, y)
                val tile = loop.tiles.getValue(point)
                if (isInside(tile, loop)) {
                    count++
                }
            }
        }
        println("Count: $count")
        loop.printField()
    }


    fun part2(input: List<String>): Any {
        val fieldMap = parseFieldMap(input)
//        fieldMap.printField()
        require(fieldMap.startPipes.size == 2)
        val startPipe = fieldMap.startPipes.first()
        val traverse = Traverse(fieldMap, startPipe)
        traverse.traverse()
        val newFieldMap = traverse.toFieldLoop()
        val m2 = newFieldMap.closeLoop()
//        m2.printField()
        findEnclosing(m2)
//
        return 0
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

//    val part1Expected = ""
//    val part1 = part1(testInput)
//    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = ""
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput("Day${day}")

//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")
//
    val part2Real = part2(input)
    println("(Real) Part 2: $part2Real")
}
