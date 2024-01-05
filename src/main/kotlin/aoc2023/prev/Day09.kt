package aoc2023.prev

fun main() {
    val day = "09"

    fun convertReadings(readings: List<Long>): MutableList<Long> {
        return buildList {
            var prev: Long? = null
            for ((i, r) in readings.withIndex()) {
                if (i == 0) {
                    prev = r
                    continue
                }
                require(prev != null)
                add(r - prev)
                prev = r
            }
        }.toMutableList().also {
            require(it.size == readings.size - 1)
        }
    }

    fun processReadings(readings: List<Long>): MutableList<MutableList<Long>> {
        val allReadings = mutableListOf<MutableList<Long>>()
        allReadings.add(readings.toMutableList())
        var curr = readings
        do {
            curr = convertReadings(curr)
            allReadings.add(curr)
            if (curr.all { it == 0L }) {
                break
            }
        } while (true)
        allReadings.printme()
        return allReadings
    }

    fun predict(p: MutableList<MutableList<Long>>): Long {
        var curr = p.last().first()
        for (i in p.size - 2 downTo 0) {
            val prev = p[i].first()
            curr = prev - curr
            p[i].add(0, curr)
        }
        for (x in p) {
            x.printme()
        }
        return curr.also { it.printme() }
    }

    val space = Regex("\\s+")
    fun part2(input: List<String>): Long {
        var sum = 0L
        for (line in input) {
            val readings = line.split(space).map { it.toLong() }
            val p = processReadings(readings)
            val x = predict(p)
            sum += x
        }

        return sum
    }

    fun part1(input: List<String>): Long {
        var sum = 0L
        for (line in input) {
            val readings = line.split(space).map { it.toLong() }
            val p = processReadings(readings)
            val x = predict(p)
            sum += x
        }
        return sum
    }



    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

//    val part1Expected = "114"
//    val part1 = part1(testInput)
//    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = "2"
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput("Day${day}")

//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")
//
    val part2Real = part2(input)
    println("(Real) Part 2: $part2Real")
}
