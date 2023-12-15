package aoc2023.day15

import readInput

sealed class Op {
    abstract val boxHash: Int
    abstract val label: String

    fun hashIt(str: String): Int {
        var hash = 0
        for (char in str) {
            require(char.code in 1..255)
            hash += char.code
            hash = hash * 17
            hash = hash % 256
        }
        require(hash in 0..255)
        return hash
    }

    data class Remove(override val label: String) : Op() {
        override val boxHash = hashIt(label)
        override fun toString(): String {
            return "Remove: $label"
        }

    }

    data class Add(override val label: String, val focalLength: Int) : Op() {
        override val boxHash = hashIt(label)

        init {
            require(focalLength in 1..9)
        }

        override fun toString(): String {
            return "Add: [$label $focalLength]"
        }
    }
}

data class Lens(val focalLength: Int, val label: String) {
    override fun toString(): String {
        return "[$label $focalLength]"
    }
}

fun main() {
    val day = "15"

    fun hashIt(str: String): Int {
        var hash = 0
        for (char in str) {
            require(char.code in 1..255)
            hash += char.code
            hash = hash * 17
            hash = hash % 256
        }
        require(hash in 0..255)
        return hash
    }

    fun updateBoxes(boxes: MutableMap<Int, MutableList<Lens>>, op: Op) {
        val n = op.boxHash
        val lens = boxes.getOrPut(n) { mutableListOf() }
        when (op) {
            is Op.Remove -> {
                lens.removeIf { it.label == op.label }
            }

            is Op.Add -> {
                val index = lens.indexOfFirst { l -> l.label == op.label }
                val newLens = Lens(op.focalLength, op.label)
                if (index >= 0) {
                    lens[index] = newLens
                } else {
                    lens.add(newLens)
                }
            }
        }
    }

    data class Config(val boxNum: Int, val slot: Int)

    fun calcConfig(boxes: MutableMap<Int, MutableList<Lens>>): Long {
        val lensMap = mutableMapOf<Lens, MutableList<Config>>()
        for ((boxNum, lens) in boxes) {
            for ((slot, l) in lens.withIndex()) {
                lensMap.getOrPut(l) { mutableListOf() }.add(Config(boxNum + 1, slot + 1))
            }
        }

        return lensMap.entries.sumOf { (lensMap, config) ->
            require(config.size == 1)
            val (boxNum, slot) = config[0]
            (boxNum.toLong() * slot * lensMap.focalLength)
        }
    }

    fun part1(input: List<String>): Any {
        require(input.size == 1)
        val initSeq = input[0]
        val steps = initSeq.split(",")
        val boxes = mutableMapOf<Int, MutableList<Lens>>()
        for (step in steps) {
            val op = if (step.endsWith("-")) {
                Op.Remove(step.dropLast(1))
            } else {
                val (labelStr, opStr) = step.split("=")
                Op.Add(labelStr, opStr.toInt())
            }
            updateBoxes(boxes, op)
            println("---------------")
            println("Op: $op")
            boxes.forEach { (boxNum, ops) ->
                println("Box $boxNum: ${ops.joinToString(" ")}")
            }
            println("---------------")
        }

        return calcConfig(boxes)


//        return returninput.size
    }

    fun part2(input: List<String>): Any {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(day, "Day${day}_test")

    val part1Expected = ""
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = ""
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput(day, "Day${day}")

    val part1Real = part1(input)
    println("(Real) Part 1: $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}

