sealed class Item {
    data object Ash : Item() {
        override fun toString(): String {
            return "."
        }
    }

    data object Rock : Item() {
        override fun toString(): String {
            return "#"
        }
    }
}

data class Line(
    val items: MutableList<Item>,
) {
    override fun toString(): String {
        return items.joinToString("")
    }

}

data class Mirror(
    val lines: MutableList<Line>,
) {
    override fun toString(): String {
        return lines.joinToString("\n")
    }

    fun findMirror(): Int {
        val pairs = generateSequence(0) { it + 1 }.take(lines.size - 1)
        for ((l, r) in pairs.windowed(2)) {
            val isMirror = findMirrorPivot(l, r)
            if (isMirror) {
                return l + 1
            }
        }
        return -1
    }

    private fun findMirrorPivot(l: Int, r: Int): Boolean {
        var i = 0
        while (true) {
            if ((l - i) < 0 || (r + i) >= lines.size) {
                return true
            }
            val left = lines[l - i]
            val right = lines[r + i]
            if (left != right) {
                return false
            }
            i++
        }
    }

//    private fun findMirror(pivot1: Int): Int {
//        for (i in 0 until lines.size) {
//            if (i == pivot) {
//                continue
//            }
//            val line = lines[i]
//            val prev = lines[i - 1]
//            val next = lines[i + 1]
//            if (line.items[pivot] == Item.Rock) {
//                if (prev.items[pivot] == Item.Ash && next.items[pivot] == Item.Ash) {
//                    return i
//                }
//            }
//        }
//    }

    fun transpose(): Mirror {
        val newLines = mutableListOf<Line>()
        for (i in 0 until lines.first().items.size) {
            val newLine = Line(mutableListOf())
            newLines.add(newLine)
            for (line in lines) {
                newLine.items.add(line.items[i])
            }
        }
        return Mirror(newLines)
    }
}

fun main() {
    val day = "13"

    fun parseMirrors(input: List<String>): MutableList<Mirror> {
        val mirrors = mutableListOf(
            Mirror(mutableListOf()),
        )
        for (inputLine in input) {
            if (inputLine == "") {
                mirrors.add(Mirror(mutableListOf()))
            } else {
                val mirror = mirrors.last()
                val line = Line(mutableListOf())
                mirror.lines.add(line)
                for (c in inputLine) {
                    val item = when (c) {
                        '.' -> Item.Ash
                        '#' -> Item.Rock
                        else -> throw Exception("Unknown item: $line")
                    }
                    line.items.add(item)
                }
            }
        }

        return mirrors
    }

    fun part1(input: List<String>): Any {
        val mirrors = parseMirrors(input)
        var sum = 0L
        for (mirror in mirrors) {
            val horizontal = mirror.findMirror()
            val mirrorX1 = mirror.transpose()
            val mirrorX2 = mirrorX1.transpose()
            val mirror2 = mirrorX2.transpose()
            val vertical = mirror2.findMirror()
            require(horizontal != 0)
            require(vertical != 0)
            require(!(horizontal > 0 && vertical > 0))
            if (horizontal >= 0) {
                sum += horizontal * 100L
            }

            if (vertical >= 0) {
                sum += vertical
            }

        }
//        println("Mirrors: $mirrors")
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
