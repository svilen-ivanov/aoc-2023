import kotlin.math.pow

fun main() {
    val day = "04"

    data class Card(
        val number: Int,
        val winningNumbers: List<Int>,
        val myNumbers: List<Int>,
    ) {
        val count = winningNumbers.intersect(myNumbers.toSet()).size
        var instances = 1

        fun copyFrom(card: Card) {
            instances += card.instances
        }
    }

    val space = Regex("\\s+")
    val digit = Regex("\\d+")

    fun part1(input: List<String>): Int {
        val sum = input.sumOf { line ->
            // parse this line into a Card
            // Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
            val card = line.split(": ").let { (cardName, numbers) ->
                val (_, cardNumberStr) = cardName.split(space)
                val cardNumber = cardNumberStr.toInt()

                val (winningNumbersStr, myNumbersStr) = numbers.split(" | ")
                val winningNumbers = winningNumbersStr.trim().split(space).map { it.trim().toInt() }
                val myNumbers = myNumbersStr.trim().split(space).map { it.trim().toInt() }

                Card(cardNumber, winningNumbers, myNumbers)
            }
            val winningNumbers = card.winningNumbers.intersect(card.myNumbers.toSet())
            val points = if (winningNumbers.isNotEmpty()) 2.toFloat().pow(winningNumbers.size - 1) else 0
            points.toInt()
        }
        return sum
    }

    fun part2(input: List<String>): Int {
        val allCards = input.map { line ->
            line.split(": ").let { (cardName, numbers) ->
                val (_, cardNumberStr) = cardName.split(space)
                val cardNumber = cardNumberStr.toInt()

                val (winningNumbersStr, myNumbersStr) = numbers.split(" | ")
                val winningNumbers = winningNumbersStr.matchAll(digit).map { it.toInt() }
                val myNumbers = myNumbersStr.matchAll(digit).map { it.toInt() }

                Card(cardNumber, winningNumbers, myNumbers)
            }
        }

        for ((i, card) in allCards.withIndex()) {
            if (card.count > 0) {
                val from = i + 1
                val to = from + card.count
                for (j in from until to) {
                    if (j < allCards.size) {
                        val nextCard = allCards[j]
                        nextCard.copyFrom(card)
                    }
                }
            }
        }

        return allCards.sumOf { card ->
            card.instances
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

    val part1Expected = "13"
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = "30"
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

//    val input = readInput("Day${day}")
//
//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
