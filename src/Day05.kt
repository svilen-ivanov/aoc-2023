import java.math.BigDecimal

fun main() {
    val day = "05"

    val seeds = Regex("seeds: (?<s>.+)")
    val header = Regex("(?<m>.+?)\\s+map:")

    data class RangeMap(
        val sourceRange: ClosedRange<BigDecimal>,
        val targetRange: ClosedRange<BigDecimal>,
    ) {
        fun remap(value: BigDecimal): BigDecimal? {
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
        fun remap(value: BigDecimal): BigDecimal {
            for (rangeMap in maps) {
                val remapped = rangeMap.remap(value)
                if (remapped != null) {
                    return remapped
                }
            }
            return value
        }
    }

    fun part1(input: List<String>): BigDecimal {
        val inputSeeds = mutableListOf<BigDecimal>()
        val inputMaps = mutableListOf<Map>()

        for (line in input) {
            if (line.isBlank()) continue
            val seedsMatch = seeds.find(line)
            if (seedsMatch != null) {
                val (s) = seedsMatch.destructured
                inputSeeds += s.split(" ").map { it.trim().toBigDecimal() }
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
            val (sourceRangeStr, targetRangeStr, length) = line.split(" ").map { it.trim().toBigDecimal() }
            lastMap.maps.add(
                RangeMap(targetRangeStr..targetRangeStr + length,
                    sourceRangeStr..sourceRangeStr + length,
                    )
            )
        }

        val x= inputSeeds.minOf { seed ->
            var x = seed
            inputMaps.forEach {
                x = it.remap(x)
            }
            x
        }
        println(x)
        return x
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

    val part1Expected = "35"
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = ""
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput("Day${day}")

    val part1Real = part1(input)
    println("(Real) Part 1: $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
