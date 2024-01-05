package aoc2023.prev

fun LongRange.size(): Long {
    require(first <= last) {
        "first must be <= last: $this"
    }
    return last - first + 1
}

fun main() {
    val day = "05"

    val seeds = Regex("seeds: (?<s>.+)")
    val header = Regex("(?<m>.+?)\\s+map:")

    data class RemapResult(val remapped: LongRange?, val rest: List<LongRange>)

    data class RangeMap(
        val sourceRange: LongRange,
        val targetRange: LongRange,
    ) {
        init {
            require(sourceRange.last - sourceRange.first == targetRange.last - targetRange.first)
            require(sourceRange.first <= sourceRange.last) {
                "first must be <= last: $this"
            }
            require(targetRange.first <= targetRange.last) {
                "first must be <= last: $this"
            }
        }

        fun validRange(first: Long, last: Long): LongRange {
            require(first <= last) {
                "first must be <= last: $first <= $last"
            }
            return first..last
        }

        fun remap(value: Long): Long? {
            return if (value in sourceRange) {
                val offset = value - sourceRange.first
                targetRange.first + offset
            } else {
                null
            }
        }

        fun clamp(range: LongRange): LongRange? {
            require(range.first <= range.last) {
                "limits.first must be <= limits.last: $range"
            }
            if (sourceRange.last < range.first || range.last < sourceRange.first) {
                return null
            }
            return validRange(maxOf(range.first, sourceRange.first), minOf(range.last, sourceRange.last))
        }

        fun translate(range: LongRange): LongRange {
            require(range.first in sourceRange && range.last in sourceRange) {
                "range must be within sourceRange: $range in $sourceRange"
            }
            val startOffset = range.first - sourceRange.first
            val endOffset = range.last - sourceRange.first
            return validRange(targetRange.first + startOffset, targetRange.first + endOffset).also {
                require(it.size() == range.size()) {
                    "size must be preserved: $it != $range"
                }
            }
        }


        fun remapRange(range: LongRange): RemapResult {
            val clampedRange = clamp(range) ?: return RemapResult(null, listOf(range))
            val translatedRange = translate(clampedRange)

            return RemapResult(
                translatedRange,
                buildList {
                    if (range.first < clampedRange.first) {
                        add(validRange(range.first, clampedRange.first - 1))
                    }
                    if (clampedRange.last < range.last) {
                        add(validRange(clampedRange.last + 1, range.last))
                    }
                }
            )
        }

        fun reverseMap(value: Long): Long? {
            return if (value in targetRange) {
                val offset = value - targetRange.first
                sourceRange.first + offset
            } else {
                null
            }
        }
    }

    class Map(
        val name: String, val ranges: List<RangeMap>
    ) {
        fun remap(value: Long): Long {
            return ranges.firstNotNullOfOrNull { it.remap(value) } ?: value
        }

        fun remapRange(toRemap: List<LongRange>): List<LongRange> {
            var rest = toRemap
            val remappedRanges = mutableListOf<LongRange>()
//            println("* Remapping: $value")
            for (range in ranges) {
//                println("** Range: $range")
                val newRest = mutableListOf<LongRange>()
                for (r in rest) {
//                    println("*** Remapping: $r")
                    val result = range.remapRange(r)
                    if (result.remapped != null) {
                        remappedRanges.add(result.remapped)
//                        println("**** Remapped: ${result.remapped}")
                    }
//                    println("*** Rest: ${result.rest}")
                    newRest.addAll(result.rest)
                }
                rest = newRest
            }
            val reamppedResult = remappedRanges + rest
            println("* Final remapping: $toRemap -> ${reamppedResult}")
            val inputSize = toRemap.sumOf { it.size() }
            val rempSize = reamppedResult.sumOf { it.size() }
            require(inputSize == rempSize) {
                "Size must be preserved: $inputSize != $rempSize"
            }
            return reamppedResult
        }

        fun reverseRemap(value: Long): Long {
            return ranges.reversed().firstNotNullOfOrNull { it.reverseMap(value) } ?: value
        }
    }


    class MapBuilder(val name: String) {
        private val maps: MutableList<RangeMap> = mutableListOf()

        fun addMap(sourceRange: LongRange, targetRange: LongRange) {
            maps.add(RangeMap(sourceRange, targetRange))
        }

        fun build(): Map = Map(name, maps)
    }


    class AllMaps(
        val maps: List<Map>
    ) {
        fun remap(value: Long): Long {
            return maps.fold(value) { acc, map -> map.remap(acc) }
        }

        fun reverseRemap(value: Long): Long {
            return maps.reversed().fold(value) { acc, map -> map.reverseRemap(acc) }
        }

        fun remap(value: LongRange): Long {
            var remappedRanges = listOf(value)
            for (map in maps) {
                remappedRanges = map.remapRange(remappedRanges)
            }
            val inputSize = value.size()
            val rempSize = remappedRanges.sumOf { it.size() }
            require(inputSize == rempSize) {
                "Size must be preserved: $inputSize != $rempSize"
            }
            println("Remapped range: $value to $remappedRanges")
            return remappedRanges.minOf { it.first }
        }
    }

    fun part1(input: List<String>): Long {
        val inputSeeds = mutableListOf<Long>()
        val inputMaps = mutableListOf<MapBuilder>()

        for (line in input) {
            if (line.isBlank()) continue
            val seedsMatch = seeds.find(line)
            if (seedsMatch != null) {
                val (s) = seedsMatch.destructured
                inputSeeds += s.split(" ").map { it.trim().toLong() }
                println(s)
                continue
            }
            val map = header.find(line)
            if (map != null) {
                val (name) = map.destructured
                inputMaps.add(MapBuilder(name))
                continue
            }
            val lastMap = inputMaps.last()
            val (sourceRangeStr, targetRangeStr, length) = line.split(" ").map { it.trim().toLong() }
            lastMap.addMap(
                targetRangeStr..targetRangeStr + length,
                sourceRangeStr..sourceRangeStr + length,
            )
        }
        val allMaps = AllMaps(inputMaps.map { it.build() })

        val result = inputSeeds.minOf { seed ->
            val x = allMaps.remap(seed..seed)
//            println("XXXX $seed -> $x")
//            x.start
            x
        }
        println(result)
//        val x = allMaps.reverseRemap(35)

        return result
    }

    fun part2(input: List<String>): Long {
        val inputSeeds = mutableListOf<LongRange>()
        val inputMaps = mutableListOf<MapBuilder>()

        for (line in input) {
            if (line.isBlank()) continue
            val seedsMatch = seeds.find(line)
            if (seedsMatch != null) {
                val (s) = seedsMatch.destructured
                val seedRanges =
                    s.split(" ").map { it.trim().toLong() }.chunked(2).map { it[0]..(it[0] + it[1] - 1.toLong()) }
                inputSeeds += seedRanges;
                println(s)
                continue
            }
            val map = header.find(line)
            if (map != null) {
                val (m) = map.destructured
                inputMaps.add(MapBuilder(m))
                continue
            }
            val lastMap = inputMaps.last()
            val (sourceRangeStr, targetRangeStr, length) = line.split(" ").map { it.trim().toLong() }
            lastMap.addMap(
                targetRangeStr..targetRangeStr + length - 1,
                sourceRangeStr..sourceRangeStr + length - 1,
            )
        }
        val allMaps = AllMaps(inputMaps.map { it.build() })

//        val naiveResult = inputSeeds.minOf {
//            it.minOf {
//                allMaps.remap(it)
//            }
//        }

//        val x = allMaps.reverseRemap(46)
        val x = inputSeeds.minOf {
            allMaps.remap(it)

        }
        return x
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")
//
//    val part1Expected = "35"
//    val part1 = part1(testInput)
//    println("(Test) Part 1: expected: $part1Expected, got: $part1")
//
    val part2Expected = "46"
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")
//
    val input = readInput("Day${day}")
//
//    val part1Real = part1(input)
//    println("(Real) Part 1: 1181555926 $part1Real")


    val x = RangeMap(10L..20L, 100L..110L)
    val r1 = x.remapRange(0L..100L)
    val r2 = x.remapRange(0L..15L)
    val r3 = x.remapRange(15L..25L)
    val r4 = x.remapRange(30L..40L)
    val r5 = x.remapRange(10..20L)
    val r6 = x.remapRange(5..10L)
    val r7 = x.remapRange(10..15L)
    val r8 = x.remapRange(30..45L)
    println(r1)
    println(r2)
    println(r3)
    println(r4)
    println(r5)
    println(r6)
    println(r7)
    println(r8)

    val part2Real = part2(input)
    println("(Real) Part 2: $part2Real")
}
