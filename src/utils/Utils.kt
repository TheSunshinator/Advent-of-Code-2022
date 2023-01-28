package utils

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Reads lines from the given input txt file.
 */
fun readInput(day: String, name: String): List<String> = File("src/day$day", "$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

inline fun <T, U, V> Pair<T, U>.mapFirst(transform: (T) -> V) = transform(first) to second
inline fun <T, U, V> Pair<T, U>.mapSecond(transform: (U) -> V) = first to transform(second)

infix fun Int.iterateTo(other: Int) = if (this <= other) rangeTo(other) else downTo(other)

fun parseLongSequence(input: List<String>) = input.first().splitToSequence(",").map(String::toLong)

fun <T> List<List<T>>.coordinates() = indices.asSequence().flatMap { i -> this[i].indices.map { j -> Point(i, j) } }
infix fun IntRange.cartesianProduct(other: IntRange) = asSequence().flatMap { i -> other.map { j -> Point(i, j) } }
infix fun <T, R> Iterable<T>.cartesianProduct(other: Iterable<R>) =
    asSequence().flatMap { i -> other.map { j -> i to j } }

infix fun <T, R> Sequence<T>.cartesianProduct(other: Sequence<R>) = flatMap { i -> other.map { j -> i to j } }

infix fun Int.plusOrMinus(n: Int) = minus(n)..plus(n)

operator fun <T> (T.() -> Boolean).not(): T.() -> Boolean = { !this@not() }

val IntRange.size
    get() = (last - first) / step

fun <T> Collection<T>.combinations(size: Int): Sequence<Set<T>> {
    return if (size == 1) asSequence().map(::setOf)
    else asSequence()
        .runningFold(toSet(), Set<T>::minus)
        .drop(1)
        .zip(asSequence()) { toCombine, current -> current to toCombine }
        .flatMap { (current, toCombine) ->
            toCombine.combinations(size - 1)
                .map { it + current }
        }
}

fun <T> Collection<T>.permutations(size: Int): Sequence<List<T>> {
    return if (size == 1) asSequence().map(::listOf)
    else asSequence()
        .map { it to this - it }
        .flatMap { (current, toPermute) ->
            toPermute.permutations(size - 1)
                .map { it + current }
        }
}

fun <T> Sequence<T>.cyclical() = generateSequence(this) { this }.flatten()
fun <T> Sequence<T>.continuing() = object : Sequence<T> {
    private val continuingIterator = this@continuing.iterator()
    override fun iterator() = continuingIterator
}

fun greaterCommonDivisor(a: Int, vararg values: Int): Int = when (values.size) {
    0 -> a
    1 -> values.single().let { b -> greaterCommonDivisor(b, a % b) }
    else -> greaterCommonDivisor(
        greaterCommonDivisor(a, values.first()),
        *IntArray(values.size - 1) { values[it + 1] }
    )
}

fun leastCommonMultiple(a: Int, vararg values: Int): Int = when (values.size) {
    0 -> a
    1 -> values.single().let { b -> a * b / greaterCommonDivisor(a, b) }
    else -> leastCommonMultiple(
        leastCommonMultiple(a, values.first()),
        *IntArray(values.size - 1) { values[it + 1] }
    )
}

fun <T : Comparable<T>> Iterable<T>.minMax(): Pair<T, T>? {
    val iterator = iterator()
    return if (iterator.hasNext()) iterator.asSequence().fold(iterator.next().let { it to it }) { (min, max), value ->
        if (value < min) value else {
            min
        } to if (max < value) value else max
    } else null
}

fun <T, R : Comparable<R>> Iterable<T>.minMaxOf(selector: (T) -> R): Pair<R, R>? {
    val iterator = iterator()
    return if (iterator.hasNext()) iterator.asSequence()
        .map(selector)
        .fold(iterator.next().let(selector).let { it to it }) { (min, max), value ->
            if (value < min) value else { min } to if (max < value) value else max
        }
    else null
}
