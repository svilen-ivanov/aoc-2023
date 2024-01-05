package aoc2023.day20

import aoc2023.prev.readInput
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DirectedMultigraph
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import java.io.StringWriter
import kotlin.io.path.Path

sealed class Pulse {
    data object Low : Pulse() {
        override fun toString(): String {
            return "-low->"
        }
    }

    data object High : Pulse() {
        override fun toString(): String {
            return "-high->"
        }
    }
}

data class SendPulse(val pulse: Pulse, val from: Module, val iter: Long)

data class Counter(var lows: Long, var highs: Long, var pushes: Long) {
    override fun toString(): String {
        return "Counter(lows=$lows, highs=$highs): ${lows * highs}, pushes=$pushes"
    }
}

class PulseSender(val counter: Counter) {
    private val affectedModules = mutableSetOf<Module>()


    fun sendPulse(from: Module, pulse: Pulse, dest: Module) {
        if (pulse == Pulse.Low) {
            counter.lows++
        } else {
            counter.highs++
        }
        dest.inbox.addLast(SendPulse(pulse, from, counter.pushes))
//        println("${from.name} $pulse ${dest.name}")
        affectedModules += dest
    }

    private fun getNext() = PulseSender(counter)

    fun processInbox() {
        val next = getNext()
        affectedModules.forEach {
            it.processInbox(next)
        }
        affectedModules.clear()
        if (next.affectedModules.isNotEmpty()) {
            next.processInbox()
        }
    }
}

fun List<Module>.printMe() = if (isEmpty()) "<none>" else joinToString(", ") { it.name }

sealed class Module {
    abstract fun processInbox(pulseSender: PulseSender)

    val inbox = ArrayDeque<SendPulse>()
    var inputs: List<Module> = emptyList()
    var outputs: List<Module> = emptyList()
    abstract val outputStrList: List<String>
    abstract val name: String

    class Button : Module() {
        val counter = Counter(0, 0, 0)

        override val name = "button"

        override val outputStrList = listOf("broadcaster")

        override fun toString(): String {
            return "$name -> ${outputs.printMe()} (from: ${inputs.printMe()})"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            return true
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }

        fun push() {
            counter.pushes++
            val pulseSender = PulseSender(counter)
            outputs.forEach {
                pulseSender.sendPulse(this, Pulse.Low, it)
            }
            pulseSender.processInbox()
//            println(counter)
        }

        override fun processInbox(pulseSender: PulseSender) {
            error("Bottons can't process inbox")
        }
    }

    class Output : Module() {
        override val name = "output"

        override val outputStrList = emptyList<String>()

        override fun toString(): String {
            return "$name -> ${outputs.printMe()} (from: ${inputs.printMe()})"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            return true
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }

        override fun processInbox(pulseSender: PulseSender) {
//            println("Processing inbox for $name: $inbox")
            // Do nothing
            if (inbox.any { it.pulse == Pulse.Low }) {
                println("Output: ${pulseSender.counter}")
                error("")
            }
            inbox.clear()
        }
    }


    class Broadcaster(override val outputStrList: List<String>) : Module() {
        override val name = "broadcaster"

        override fun toString(): String {
            return "$name -> ${outputs.printMe()} (from: ${inputs.printMe()})"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            return true
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }

        override fun processInbox(pulseSender: PulseSender) {
            while (inbox.isNotEmpty()) {
                val (pulse, _) = inbox.removeFirst()
                outputs.forEach {
                    pulseSender.sendPulse(this, pulse, it)
                }
            }
        }

    }

    class FlipFlop(override val name: String, override val outputStrList: List<String>) : Module() {
        var state = false

        override fun toString(): String {
            return "%$name -> ${outputs.printMe()} (from: ${inputs.printMe()})"
        }

        override fun processInbox(pulseSender: PulseSender) {
//            println("Processing inbox for $name: $inbox")
            val affectedModules = mutableSetOf<Module>()
            while (inbox.isNotEmpty()) {
                val (pulse, _) = inbox.removeFirst()
                if (pulse == Pulse.Low) {
                    state = !state
                    val newPulse = if (state) {
                        Pulse.High
                    } else {
                        Pulse.Low
                    }
                    outputs.forEach {
                        pulseSender.sendPulse(this, newPulse, it)
                    }
                }

            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FlipFlop

            return name == other.name
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }

    class Conjunction(override val name: String, override val outputStrList: List<String>) : Module() {
        var state: MutableMap<Module, Pulse> = mutableMapOf()

        override fun toString(): String {
            return "&$name -> ${outputs.printMe()} (from: ${inputs.printMe()})"
        }

        override fun processInbox(pulseSender: PulseSender) {
//            println("Processing inbox for $name: $inbox")
            while (inbox.isNotEmpty()) {
                val (pulse, from, iter) = inbox.removeFirst()
                checkPulse(pulse, from, iter)
                require(from in inputs)
                state[from] = pulse
                val newPulse = if (state.values.all { it == Pulse.High }) {
                    Pulse.Low
                } else {
                    Pulse.High
                }
                outputs.forEach {
                    pulseSender.sendPulse(this, newPulse, it)
                }
            }
        }

        fun checkPulse(pulse: Pulse, from: Module, iter: Long) {
            if (name != "rm") {
                return
            }
            if (pulse == Pulse.High) {
                println("High pulse from ${from.name} on push $iter")
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Conjunction

            return name == other.name
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }

        fun initState() {
            inputs.forEach {
                state[it] = Pulse.Low
            }
        }
    }
}


class Configuration(val modules: List<Module>) {

    init {
        val byName = modules.groupBy { it.name }
        require(byName.all { it.value.size == 1 }) {
            "Duplicate module names: ${byName.filter { it.value.size > 1 }}"
        }
        for (module in modules) {
            module.outputs = module.outputStrList.map { outputStr ->
                byName.getValue(outputStr).single()
            }
            module.outputs.forEach { it.inputs += module }
        }
        modules.filterIsInstance<Module.Conjunction>().forEach { it.initState() }

//        modules.forEach {
//            println(it)
//        }
    }

    fun exportGraph() {
        val g = DirectedMultigraph<Module, DefaultEdge>(DefaultEdge::class.java)
        modules.forEach { source ->
            g.addVertex(source)
            source.outputs.forEach { dest ->
                g.addVertex(dest)
                g.addEdge(source, dest)
            }
        }
        val exporter = DOTExporter<Module, DefaultEdge>()
//        exporter.setEdgeAttributeProvider { edge ->
//            buildMap {
//                this["label"] = DefaultAttribute.createAttribute(
//                    if (edge.cond) "Yes" else "No"
//                )
//            }
//        }

        exporter.setVertexAttributeProvider { vertex ->
            buildMap {
                this["label"] = DefaultAttribute.createAttribute(
                    "${
                        when(vertex) {
                            is Module.Button -> ""
                            is Module.Output -> ""
                            is Module.Broadcaster -> ""
                            is Module.FlipFlop -> "%"
                            is Module.Conjunction -> "&"
                        }
                    }${vertex.name}"
                )
            }
        }
        val writer = StringWriter()
        exporter.exportGraph(g, writer)
        Path("src/aoc2023/day20/graph.dot").toFile().writeText(writer.toString())
    }

    fun pushButton() {
        val button = modules.filterIsInstance<Module.Button>().single()
        repeat(1000000) {
            button.push()
        }
//        println("--- Push 1 ---")
//        button.push()
//        println("--- Push 2 ---")
//        button.push()
//        println("--- Push 3 ---")
//        button.push()
    }
}

fun main() {
    val day = "20"

    fun part1(input: List<String>): Any {
        val modules = mutableListOf<Module>()
        modules.add(Module.Button())
        modules.add(Module.Output())
        for (line in input) {
            val (type, connectionsStr) = line.split(" -> ")
            val connections = connectionsStr.split(", ")
            if (type == "broadcaster") {
                modules.add(Module.Broadcaster(connections))
            } else if (type.startsWith("%")) {
                val name = type.drop(1)
                modules.add(Module.FlipFlop(name, connections))
            } else if (type.startsWith("&")) {
                val name = type.drop(1)
                modules.add(Module.Conjunction(name, connections))
            } else {
                throw IllegalArgumentException("Unknown module: $type")
            }
        }
        val c = Configuration(modules)
//        c.exportGraph()
        c.pushButton()
        return input.size
    }

    fun part2(input: List<String>): Any {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
//    val testInput = readInput(day, "Day${day}_test")
//
//    val part1Expected = ""
//    val part1 = part1(testInput)
//    println("(Test) Part 1: expected: $part1Expected, got: $part1")

//    val part2Expected = ""
//    val part2 = part2(testInput)
//    println("(Test) Part 2: expected: $part2Expected, got: $part2")

    val input = readInput(day, "Day${day}")

    val part1Real = part1(input)
    println("(Real) Part 1: $part1Real")
//
//    val part2Real = part2(input)
//    println("(Real) Part 2: $part2Real")
}
