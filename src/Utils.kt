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
fun Any?.println() = println(this)

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
