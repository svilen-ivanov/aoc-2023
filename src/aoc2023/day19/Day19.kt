package aoc2023.day19

import readInput

data class Workflow(val name: String, val rules: List<Rule>)

sealed class Operator {
    data object LessThan : Operator()
    data object GreaterThan : Operator()
}

sealed class Rule {
    data class Condition(val field: String, val operator: Operator, val value: Long, val outcome: Rule) : Rule() {
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
//    val input = readInput(day, "Day${day}")
//
//    val part1Real = part1(input)
//    println("(Real) Part 1: $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
