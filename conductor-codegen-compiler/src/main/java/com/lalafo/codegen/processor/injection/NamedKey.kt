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

package com.lalafo.codegen.processor.injection

import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

data class NamedKey(
    val key: Key,
    val name: String
) : Comparable<NamedKey> {

    override fun toString(): String {
        return "$key $name"
    }

    override fun compareTo(other: NamedKey): Int {
        return namedKeyComparator.compare(this, other)
    }

    companion object {
        private val namedKeyComparator = compareBy<NamedKey>(
            { it.key },
            { it.name }
        )
    }
}

fun VariableElement.asNamedKey(mirror: TypeMirror = asType()) = NamedKey(
    key = asKey(mirror),
    name = simpleName.toString()
)