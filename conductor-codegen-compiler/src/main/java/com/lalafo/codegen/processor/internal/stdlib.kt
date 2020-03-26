/*
 * Copyright 2020 Lalafo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

