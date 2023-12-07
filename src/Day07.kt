fun main() {
    val day = "07"

    val cards = listOf(
        "A", "K", "Q", "T", "9", "8", "7", "6", "5", "4", "3", "2", "J"
    ).reversed()

    data class MyEntry(override val key: String, override val value: Int) : Map.Entry<String, Int>

    data class Hand(val handStr: String) : Comparable<Hand> {
        val pairs = buildMap<String, Int> {
            for (card in handStr) {
                put(card.toString(), getOrDefault(card.toString(), 0) + 1)
            }
        }.entries.sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .then(compareByDescending { cards.indexOf(it.key) })
        )

        fun calcType(pairs: List<Map.Entry<String, Int>>): Int {
            if (pairs[0].value == 5) {
                require(pairs.size == 1)
                return 6
            } else if (pairs[0].value == 4) {
                require(pairs.size == 2)
                return 5
            } else if (pairs[0].value == 3 && pairs[1].value == 2) {
                require(pairs.size == 2)
                return 4
            } else if (pairs[0].value == 3) {
                require(pairs.size == 3)
                return 3
            } else if (pairs[0].value == 2 && pairs[1].value == 2) {
                require(pairs.size == 3)
                return 2
            } else if (pairs[0].value == 2) {
                require(pairs.size == 4)
                return 1
            } else {
                require(pairs.size == 5)
                return 0
            }
        }


        val pairs2= remap(pairs)
        val type = calcType(pairs2)

        private fun remap(pairs: List<Map.Entry<String, Int>>): List<Map.Entry<String, Int>> {
            val jokerPair = pairs.firstOrNull { it.key == "J" }
            val mut = pairs.filter { it.key != "J" }.toMutableList()
            if (mut.isEmpty()) {
                return pairs
            }
            if (jokerPair?.key == "J") {
                mut.set(0, MyEntry(mut[0].key, mut[0].value + jokerPair.value))
            }
            return mut
        }

        override fun compareTo(other: Hand): Int {
            val typeResult = type.compareTo(other.type)
            if (typeResult != 0) {
                return typeResult
            } else {
                for (i in handStr.indices) {
                    val thisCard = handStr[i]
                    val otherCard = other.handStr[i]
                    val thisCardIndex = cards.indexOf(thisCard.toString())
                    val otherCardIndex = cards.indexOf(otherCard.toString())
                    val cardResult = thisCardIndex.compareTo(otherCardIndex)
                    if (cardResult != 0) {
                        return cardResult
                    }
                }
                error("")
            }
        }

        override fun toString(): String {

            return "Hand(handStr='$handStr', pairs=$pairs, pairs2=$pairs2 type=$type)"
        }

    }

    data class HandBid(val hand: Hand, val bid: Int)

    fun part1(input: List<String>): Int {
        val x = input.map { line ->
            val (hand, bidStr) = line.split(" ")
            val bid = bidStr.toInt()
            HandBid(Hand(hand), bid)

        }
            .sortedBy { it.hand }
            .let {
//                println(it)
                for (hand in it) {
                    println(hand)
                }
                it
            }
            .mapIndexed { index, handBid ->
                val rank = index + 1
                rank * handBid.bid
            }.sum()
        return x
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

//    val part1Expected = "6440"
//    val part1 = part1(testInput)
//    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = "5905"
    val part2 = part1(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")
//
    val input = readInput("Day${day}")

//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")

    val part2Real = part1(input)
    println("(Real) Part 2: $part2Real")
}
