package aoc2023.prev

fun main() {
    val day = "06"

    fun parseLine(line: String): List<Long> {
        val (_, timesPart) = line.split(":")
        val times = timesPart.trim().split(Regex("\\s+")).map { it.trim().toLong() }
        return times
    }

    data class Race(
        val time: Long,
        val distance: Long,
    ) {
        fun holding(): List<Long> {
            val times = buildList {
                for (holdTime in 0..time) {
                    val remainingTime = time - holdTime

                    val distance = holdTime * remainingTime
                    println("distance: $distance")
                    add(distance)
                }
            }
            return times.filter { it > distance }
        }

        fun waysToBeat(): Long {
            var ways = 0L
            for (holdTime in 0..time) {
                val remainingTime = time - holdTime
                val distance = holdTime * remainingTime

                if (distance > this.distance) {
                    ways++
                }
            }
            return ways
        }
    }

    fun part1(input: List<String>): String {
        val times = parseLine(input[0])
        val distances = parseLine(input[1])
        val races = times.indices.map { Race(times[it], distances[it]) }
        println("races: $races")
        val waysToBeat = races.map { it.waysToBeat() }
//        println(waysToBeat)
        val res = waysToBeat.mul()
        return res.toString()
    }

    fun part2(input: List<String>): String {
        val time = input[0].replace(Regex("\\D+"), "").toLong()
        val distance = input[1].replace(Regex("\\D+"), "").toLong()
        val race = Race(time, distance)
        println(race)
        return race.waysToBeat().toString()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

    val part1Expected = "633080"
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = ""
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput("Day${day}")

    val part1Real = part1(input)
    println("(Real) Part 1: 633080 $part1Real")

    val part2Real = part2(input)
    println("(Real) Part 2: $part2Real")
}
