package com.lalafo.codegen.processor.internal

fun <K, V : Any> Map<K, V?>.filterNotNullValues(): Map<K, V> {
    @Suppress("UNCHECKED_CAST")
    return filterValues { it != null } as Map<K, V>
}
fun <K, V : Any> Map<K?, V>.filterNotNullKeys(): Map<K, V> {
    @Suppress("UNCHECKED_CAST")
    return filterKeys { it != null } as Map<K, V>
}

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T> Any.cast(): T = this as T

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T> Iterable<*>.castEach() = map { it as T }

inline fun <T : Any, I> T.applyEach(items: Iterable<I>, func: T.(I) -> Unit): T {
    items.forEach { item -> func(item) }
    return this
}

