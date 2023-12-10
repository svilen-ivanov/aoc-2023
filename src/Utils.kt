import org.apache.commons.math3.util.ArithmeticUtils
import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun <T> T.printme() = apply { println("> $this") }

fun String.matchAll(regex: Regex) = regex.findAll(this.trim()).map { it.value.trim() }.toList()
fun String.matchNamedGroups(regex: Regex) = regex.findAll(this.trim()).map {
    val groups = it.groups
    if (groups is MatchNamedGroupCollection) {
        groups
    } else {
        error("Not a named group")
    }

}.toList()

fun String.matchGroups(regex: Regex) = regex.findAll(this.trim()).map {
    it.groups
}.toList()

fun List<Long>.mul() = fold(1L) { acc, i -> ArithmeticUtils.mulAndCheck(acc, i) }
fun List<Int>.mul() = fold(1) { acc, i -> ArithmeticUtils.mulAndCheck(acc, i) }

fun List<BigInteger>.mul() = fold(BigInteger.ONE) { acc, i -> acc * i }
fun List<BigDecimal>.mul() = fold(BigDecimal.ONE) { acc, i -> acc * i }

fun <T: Number>List<T>.lcm(): Long {
    require(size >= 2)
    require(all { it is Long || it is Int })
    val nl = this
    var result = ArithmeticUtils.lcm(nl[0].toLong(), nl[1].toLong())
    var i = 2
    while (i < size) {
        result = ArithmeticUtils.lcm(result, nl[i].toLong())
        i++
    }
    return result
}
