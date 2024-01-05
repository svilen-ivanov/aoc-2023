package aoc2023.day19

import aoc2023.prev.readInput
import org.apache.commons.math3.util.ArithmeticUtils
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DirectedMultigraph
import org.jgrapht.graph.EdgeReversedGraph
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import org.jgrapht.traverse.BreadthFirstIterator
import java.io.StringWriter
import kotlin.io.path.Path


data class Workflow(val name: String, val rules: List<Rule>)

sealed class Operator {
    data object LessThan : Operator()
    data object GreaterThan : Operator()
}

sealed class Rule {
    class Condition(val field: String, val operator: Operator, val value: Long, val outcome: Rule) : Rule() {
        fun evaluate(part: Part): Boolean {
            val partValue = when (field) {
                "x" -> part.x
                "m" -> part.m
                "a" -> part.a
                "s" -> part.s
                else -> error("Unknown field: $field")
            }
            return when (operator) {
                is Operator.LessThan -> partValue < value
                is Operator.GreaterThan -> partValue > value
            }
        }

        fun reverse(): Condition {
            return Condition(
                field, when (operator) {
                    is Operator.LessThan -> Operator.GreaterThan
                    is Operator.GreaterThan -> Operator.LessThan
                }, value + (
                        when (operator) {
                            is Operator.LessThan -> -1
                            is Operator.GreaterThan -> 1
                        }
                        ), outcome
            )
        }

        override fun toString(): String {
            return "$field${
                when (operator) {
                    is Operator.LessThan -> "<"
                    is Operator.GreaterThan -> ">"
                }
            }${"%4d".format(value)}"
        }
    }

    data object Accept : Rule()
    data object Reject : Rule()
    data class Jump(val nextWorkflow: String) : Rule()
}

data class Part(
    val x: Long,
    val m: Long,
    val a: Long,
    val s: Long,
) {
    val rating = x + m + a + s
}

class CountRange(val rangeList: List<Rule.Condition>) {
    init {
        require(rangeList.size == 2)
        require(rangeList[0].operator == Operator.GreaterThan)
        require(rangeList[1].operator == Operator.LessThan)
        require(rangeList[0].value < rangeList[1].value) {
            "Invalid range: $rangeList"
        }
    }

    val size: Long
        get() {
            return if (rangeList[0].value < rangeList[1].value) {
                rangeList[1].value - rangeList[0].value - 1
            } else {
                0
            }
        }


}

sealed class Vertex {
    data object Start : Vertex()
    data object Accept : Vertex()
    data object Reject : Vertex()
    data class Condition(val condition: Rule.Condition) : Vertex()
}

class Edge(val cond: Boolean) : DefaultEdge()

class Solver(val workflows: Map<String, Workflow>) {
    val g = DirectedMultigraph<Vertex, Edge>(Edge::class.java)
    val reversedGraph: EdgeReversedGraph<Vertex, Edge>

    init {
        for (workflow in workflows.values) {
            var isStart = false
            if (workflow.name == "in") {
                g.addVertex(Vertex.Start)
                isStart = true
            }

            workflow.rules.windowed(2, partialWindows = true) { window ->
                val rule = window.first()
                when (rule) {
                    is Rule.Jump -> {

                    }

                    is Rule.Accept -> {

                    }

                    is Rule.Reject -> {

                    }

                    is Rule.Condition -> {
                        val trueDest = when (rule.outcome) {
                            is Rule.Accept -> Vertex.Accept
                            is Rule.Reject -> Vertex.Reject
                            is Rule.Jump -> Vertex.Condition(workflows[rule.outcome.nextWorkflow]!!.rules.first() as Rule.Condition)
                            is Rule.Condition -> error("Unexpected condition")
                        }
                        val source = Vertex.Condition(rule)
                        g.addVertex(source)
                        if (isStart) {
                            g.addVertex(Vertex.Start)
                            g.addEdge(Vertex.Start, source, Edge(true))
                        }

                        g.addVertex(trueDest)
                        g.addEdge(source, trueDest, Edge(true))

                        require(window.size == 2)
//                        println(workflow.name)
                        val falseDest = when (val nextRule = window[1]) {
                            is Rule.Accept -> Vertex.Accept
                            is Rule.Reject -> Vertex.Reject
                            is Rule.Jump -> Vertex.Condition(workflows[nextRule.nextWorkflow]!!.rules.first() as Rule.Condition)
                            is Rule.Condition -> Vertex.Condition(nextRule)
                        }
                        g.addVertex(falseDest)
                        g.addEdge(source, falseDest, Edge(false))
                    }
                }
            }
        }

        val exporter = DOTExporter<Vertex, Edge>()
        exporter.setEdgeAttributeProvider { edge ->
            buildMap {
                this["label"] = DefaultAttribute.createAttribute(
                    if (edge.cond) "Yes" else "No"
                )
            }
        }

        exporter.setVertexAttributeProvider { vertex ->
            buildMap {
                this["label"] = DefaultAttribute.createAttribute(
                    when (vertex) {
                        is Vertex.Start -> "Start"
                        is Vertex.Accept -> "Accept"
                        is Vertex.Reject -> "Reject"
                        is Vertex.Condition -> vertex.condition.toString()
                    }
                )
            }
        }

        for (v in BreadthFirstIterator(g)) {
            if (v == Vertex.Start || v == Vertex.Accept || v == Vertex.Reject) {
                continue
            }
            val outgoingVert = g.outgoingEdgesOf(v).map { g.getEdgeTarget(it) }
            require(outgoingVert.size == 2) {
                "Vertex $v has ${outgoingVert.size} outgoing edges: ${g.outgoingEdgesOf(v)}}"
            }
        }

        var hasDuplicates: Boolean
        do {
            hasDuplicates = false
            for (v in BreadthFirstIterator(g)) {
                if (v == Vertex.Start || v == Vertex.Accept || v == Vertex.Reject) {
                    continue
                }
                val outgoingVert = g.outgoingEdgesOf(v).map { g.getEdgeTarget(it) }
                require(outgoingVert.size == 2) {
                    "Vertex $v has ${outgoingVert.size} outgoing edges: ${g.outgoingEdgesOf(v)}}"
                }
                val allTheSame = outgoingVert.all { it == outgoingVert.first() }
                if (allTheSame) {
                    val newDest = outgoingVert.first()
                    for (inEdge in g.incomingEdgesOf(v)) {
                        g.addEdge(g.getEdgeSource(inEdge), newDest, Edge(inEdge.cond))
                    }
                    g.removeVertex(v)
                    hasDuplicates = true
                    break
                }
            }

        } while (hasDuplicates)

        reversedGraph = EdgeReversedGraph(g)
        val writer = StringWriter()
        exporter.exportGraph(g, writer)
        Path("src/aoc2023/day19/graph.dot").toFile().writeText(writer.toString())

        val writerReverse = StringWriter()
        exporter.exportGraph(reversedGraph, writerReverse)
        Path("src/aoc2023/day19/graph_reverse.dot").toFile().writeText(writerReverse.toString())

    }

    fun collectRules(vertex: Vertex, rules: List<Rule.Condition>, allRules: MutableList<List<Rule.Condition>>) {
        if (vertex == Vertex.Start) {
            allRules.add(rules.reversed())
            return
        }
        reversedGraph.outgoingEdgesOf(vertex).forEach { edge ->
            val nextVertex = reversedGraph.getEdgeTarget(edge)
            val newRules = when (nextVertex) {
                is Vertex.Condition -> rules +
                        if (edge.cond) {
                            nextVertex.condition
                        } else {
                            nextVertex.condition.reverse()
                        }

                else -> rules
            }
            collectRules(nextVertex, newRules, allRules)
        }
    }

    fun runMe() {
        val allRules = mutableListOf<List<Rule.Condition>>()
        collectRules(Vertex.Accept, emptyList(), allRules)
        allRules.forEach {
            println(it)
        }
        val sorted = allRules.map {
            val newRules = listOf(
                Rule.Condition("x", Operator.GreaterThan, 0, Rule.Accept),
                Rule.Condition("x", Operator.LessThan, 4001, Rule.Accept),
                Rule.Condition("m", Operator.GreaterThan, 0, Rule.Accept),
                Rule.Condition("m", Operator.LessThan, 4001, Rule.Accept),
                Rule.Condition("a", Operator.GreaterThan, 0, Rule.Accept),
                Rule.Condition("a", Operator.LessThan, 4001, Rule.Accept),
                Rule.Condition("s", Operator.GreaterThan, 0, Rule.Accept),
                Rule.Condition("s", Operator.LessThan, 4001, Rule.Accept),
            ) + it
            val sorted = newRules.groupBy { it.field }.mapValues {
                val sorted = it.value.sortedWith(compareBy<Rule.Condition> {
                    when (it.operator) {
                        is Operator.GreaterThan -> 1
                        is Operator.LessThan -> 2
                    }
                }.then(compareBy { it.value }))

                println(sorted)
                listOf(
                    sorted.last { it.operator == Operator.GreaterThan },
                    sorted.first { it.operator == Operator.LessThan })

            }
            println("$it -> $sorted")
//            println("$sorted")
            sorted
        }
        val x = sorted.sumOf {
            val combinations = it.entries.fold(1L) { a, (k, v) ->
                val c = CountRange(v)
                ArithmeticUtils.mulAndCheck(a, c.size)
            }
            val x = it.entries.joinToString(", ") { (k, v) ->
                val c = CountRange(v)
                "$k: $v (${c.size})"
            }
            println("$it -> $x -> $combinations")
            combinations
        }
        println(x)
        println(Long.MAX_VALUE)
//        println(sorted)
//        val s = sorted.map { it["s"]!! }
//        println(s)
//        var count = 0
//        for (i in 1..4000) {
//            for (rangeList in s) {
//                require(rangeList.size == 2)
//                require(rangeList[0].operator == Operator.GreaterThan)
//                require(rangeList[1].operator == Operator.LessThan)
//                if (i > rangeList[0].value && i < rangeList[1].value) {
//                    count++
//                    break
//                }
//            }
//        }
        // 167409079868000
        // 150616375868000
        //  26599296000000
//        println(count)
    }
}

fun main() {
    val day = "19"

    val workflowRegex = Regex("(\\w+)\\{(.+?)}")
    val conditionRegex = Regex("([xmas])([<>])(\\d+)")

    fun parseRules(rules: String): List<Rule> {
        return buildList {
            for (rule in rules.split(",")) {
                if (rule.contains(":")) {
                    val (condition, outcome) = rule.split(":")
                    val (field, operator, value) = conditionRegex.find(condition)!!.destructured
                    val outcomeRule = when (outcome) {
                        "A" -> Rule.Accept
                        "R" -> Rule.Reject
                        else -> Rule.Jump(outcome)
                    }
                    add(
                        Rule.Condition(
                            field,
                            when (operator) {
                                "<" -> Operator.LessThan
                                ">" -> Operator.GreaterThan
                                else -> error("Unknown operator: $operator")
                            },
                            value.toLong(),
                            outcomeRule
                        )
                    )
                } else {
                    when (rule) {
                        "A" -> add(Rule.Accept)
                        "R" -> add(Rule.Reject)
                        else -> add(Rule.Jump(rule))
                    }
                }
            }
        }
    }

    fun parseWorkflow(line: String): Workflow {
        val (name, rules) = workflowRegex.find(line)!!.destructured
        return Workflow(name, parseRules(rules))
    }

    val partRegex = Regex("x=(\\d+),m=(\\d+),a=(\\d+),s=(\\d+)")
    fun parseParts(line: String): Part {
        val (x, m, a, s) = partRegex.find(line)!!.destructured
        return Part(x.toLong(), m.toLong(), a.toLong(), s.toLong())
    }

    fun evaluateWorkflow(workflows: Map<String, Workflow>, part: Part): Boolean {
        val workflow = workflows["in"]!!
        val rules = ArrayDeque(workflow.rules)
        while (rules.isNotEmpty()) {
            when (val rule = rules.removeFirst()) {
                is Rule.Condition -> {
                    if (rule.evaluate(part)) {
                        rules.addFirst(rule.outcome)
                    }
                }

                is Rule.Accept -> return true
                is Rule.Reject -> return false
                is Rule.Jump -> {
                    val nextWorkflow = workflows[rule.nextWorkflow]!!
                    rules.let {
                        it.clear()
                        it.addAll(nextWorkflow.rules)
                    }
                }
            }
        }
        error("No accept/reject rule found")
    }

    fun part1(input: List<String>): Any {
        val workflows = mutableMapOf<String, Workflow>()
        val parts = mutableListOf<Part>()
        var parseWorkflow = true
        for (line in input) {
            if (line == "") {
                parseWorkflow = false
                continue
            }

            if (parseWorkflow) {
                parseWorkflow(line).let {
                    workflows[it.name] = it
                }
            } else {
                parts += parseParts(line)
            }
        }

        val solver = Solver(workflows)
        solver.runMe()

        var total = 0L
        for (part in parts) {
            if (evaluateWorkflow(workflows, part)) {
                total += part.rating
            }
        }

        return total
    }

    fun part2(input: List<String>): Any {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(day, "Day${day}_test")

    val part1Expected = "19114"
    val part1 = part1(testInput)
    println("(Test) Part 1: expected: $part1Expected, got: $part1")

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")
//
    val input = readInput(day, "Day${day}")

    val part1Real = part1(input)
    println("(Real) Part 1: $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
