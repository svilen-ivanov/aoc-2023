fun main() {
    val day = "08"


    data class Node(val name: String, val left: String, val right: String)

    fun part1(input: List<String>): Int {
        val instructions = input[0]
        val rest = input.subList(2, input.size)
        val split = Regex("\\W+")
        val nodes = rest.map { line ->
            val (name, left, right) = line.split(split)
            require(name.length == 3)
            require(left.length == 3)
            require(right.length == 3)
            Node(name, left, right)
        }
        val nodesByName = nodes.associateBy { it.name }
        var steps = 0
        var currentNode = nodesByName.getValue("AAA")
        while (true) {
            for (instruction in instructions) {
                steps++
                val next = when (instruction) {
                    'L' -> {
                        nodesByName.getValue(currentNode.left)
                    }

                    'R' -> {
                        nodesByName.getValue(currentNode.right)
                    }

                    else -> {
                        error("Unknown instruction: $instruction")
                    }
                }
                if (next.name == "ZZZ") {
                    return steps
                } else {
                    currentNode = next
                }

            }
        }
    }

    class Navigator(val nodesByName: Map<String, Node>, val startNode: Node) {
        var currentNode = startNode
        var steps = 0

        init {
            require(currentNode.name.endsWith("A"))
        }

        fun move(instruction: Char): Boolean {
            steps++
            val next = when (instruction) {
                'L' -> nodesByName.getValue(currentNode.left)
                'R' -> nodesByName.getValue(currentNode.right)
                else -> error("Unknown instruction: $instruction")
            }
            currentNode = next
            val isEnd = currentNode.name.endsWith("Z")
            return isEnd
        }
    }

    fun factorize(number: Int): List<Int> {
        var n = number
        val factors = mutableListOf<Int>()
        var i = 2
        while (i <= n) {
            if (n % i == 0) {
                factors.add(i)
                n /= i
            } else {
                i++
            }
        }
        require(factors.mul() == number)
        return factors
    }
    fun part2(input: List<String>): Int {
        val instructions = input[0]
        val rest = input.subList(2, input.size)
        val split = Regex("\\W+")
        val nodes = rest.map { line ->
            val (name, left, right) = line.split(split)
            require(name.length == 3)
            require(left.length == 3)
            require(right.length == 3)
            Node(name, left, right)
        }
        val nodesByName = nodes.associateBy { it.name }
        val startNodes = nodesByName.filter { (_, v) -> v.name.endsWith("A") }
        println("startNodes: $startNodes")
        val stepsToReachEnd = startNodes
            .map { (_, v) -> Navigator(nodesByName, v) }
            .map {
                while (true) {
                    for (instruction in instructions) {
                        val isEnd = it.move(instruction)
                        if (isEnd) {
                            return@map it.steps
                        }
                    }
                }
                error("")
            }
        println(stepsToReachEnd)
        stepsToReachEnd.map { factorize(it) }.flatten().distinct().map { it.toLong() }.mul().println()
        stepsToReachEnd.map { factorize(it) }.println()
        stepsToReachEnd.map { factorize(it) }.flatten().sorted().println()
        stepsToReachEnd.map { factorize(it) }.flatten().sorted().distinct().println()
        val t = stepsToReachEnd.map { factorize(it) }.flatten().sorted().distinct().map { it.toLong() }.mul()

        stepsToReachEnd.forEach { s ->
            println("$s -> $t / $s -> ${t / s}")
        }

        return 0
    }





    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${day}_test")

//    val part1Expected = ""
//    val part1 = part1(testInput)
//    println("(Test) Part 1: expected: $part1Expected, got: $part1")

    val part2Expected = "6"
    val part2 = part2(testInput)
    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput("Day${day}")

//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")

    val part2Real = part2(input)
    println("(Real) Part 2: $part2Real")
}
