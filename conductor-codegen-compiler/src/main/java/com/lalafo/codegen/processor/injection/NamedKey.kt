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