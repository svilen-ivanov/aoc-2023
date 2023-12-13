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
        val prev = lines.first()
        for (i in 1 ..< lines.size) {
            val line = lines[i]
            if (line.items == prev.items) {
                return i
            }
        }
        return -1
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
        for (mirror in mirrors) {
            println("Mirror: $mirror")
            val index = mirror.findMirror()
            println("Mirror index: $index")
        }
//        println("Mirrors: $mirrors")
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

    val input = readInput("Day${day}")

//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")

//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
