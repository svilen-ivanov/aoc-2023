import java.math.BigInteger

fun main() {
    val day = "05"

    val seeds = Regex("seeds: (?<s>.+)")
    val header = Regex("(?<m>.+?)\\s+map:")

    data class RangeMap(
        val sourceRange: ClosedRange<BigInteger>,
        val targetRange: ClosedRange<BigInteger>,
    ) {
        fun remap(value: BigInteger): BigInteger? {
            return if (value in sourceRange) {
                val offset = value - sourceRange.start
                targetRange.start + offset
            } else {
                null
            }
        }
    }

    data class Map(
        val name: String,
        val maps: MutableList<RangeMap>,
    ) {
        fun remap(value: BigInteger): BigInteger {
            for (rangeMap in maps) {
                val remapped = rangeMap.remap(value)
                if (remapped != null) {
                    return remapped
                }
            }
            return value
        }
    }

    fun remap(seed: BigInteger, inputMaps: MutableList<Map>): BigInteger {
        var x = seed
        inputMaps.forEach {
            x = it.remap(x)
        }
        return x
    }

    fun part1(input: List<String>): BigInteger {
        val inputSeeds = mutableListOf<BigInteger>()
        val inputMaps = mutableListOf<Map>()

        for (line in input) {
            if (line.isBlank()) continue
            val seedsMatch = seeds.find(line)
            if (seedsMatch != null) {
                val (s) = seedsMatch.destructured
                inputSeeds += s.split(" ").map { it.trim().toBigInteger() }
                println(s)
                continue
            }
            val map = header.find(line)
            if (map != null) {
                val (m) = map.destructured
                inputMaps.add(Map(m, mutableListOf()))
                println(m)
                continue
            }
            val lastMap = inputMaps.last()
            val (sourceRangeStr, targetRangeStr, length) = line.split(" ").map { it.trim().toBigInteger() }
            lastMap.maps.add(
                RangeMap(
                    targetRangeStr..targetRangeStr + length,
                    sourceRangeStr..sourceRangeStr + length,
                )
            )
        }

        val x = inputSeeds.minOf { seed ->
            remap(seed, inputMaps)
        }
        println(x)
        return x
    }

    fun part2(input: List<String>): BigInteger {
        val inputSeeds = mutableListOf<ClosedRange<BigInteger>>()
        val inputMaps = mutableListOf<Map>()

        for (line in input) {
            if (line.isBlank()) continue
            val seedsMatch = seeds.find(line)
            if (seedsMatch != null) {
                val (s) = seedsMatch.destructured
                val seedRanges = s.split(" ").map { it.trim().toBigInteger() }
                    .chunked(2).map { it[0]..(it[0] + it[1] - 1.toBigInteger()) }
                inputSeeds += seedRanges;
                println(s)
                continue
            }
            val map = header.find(line)
            if (map != null) {
                val (m) = map.destructured
                inputMaps.add(Map(m, mutableListOf()))
                println(m)
                continue
            }
            val lastMap = inputMaps.last()
            val (sourceRangeStr, targetRangeStr, length) = line.split(" ").map { it.trim().toBigInteger() }
            lastMap.maps.add(
                RangeMap(
                    targetRangeStr..targetRangeStr + length,
                    sourceRangeStr..sourceRangeStr + length,
                )
            )
        }

//        val x = inputSeeds.sumOf { it.endInclusive - it.start + 1.toBigInteger() }

        val x = inputSeeds.minOf {
            var min: BigInteger? = null
            var i = it.start
            while (i in it) {
                val location = remap(i, inputMaps)
                min = minOf(min ?: location, location)
                i++
            }
            min!!

        }
        return x
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

//    val part1Expected = "35"
//    val part1 = part1(testInput)
//    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = "46"
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput("Day${day}")

//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")

    val part2Real = part2(input)
    println("(Real) Part 2: $part2Real")
}
