package com.lalafo.codegen.processor.injection

import com.lalafo.codegen.processor.internal.hasAnnotation
import com.lalafo.codegen.processor.internal.toTypeName
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

data class Key(
    val type: TypeName,
    val qualifier: AnnotationSpec? = null
) : Comparable<Key> {

    override fun toString(): String {
        return qualifier?.let { "$it $type" } ?: type.toString()
    }

    override fun compareTo(other: Key): Int {
        return keyComparator.compare(this, other)
    }

    companion object {
        private val keyComparator = compareBy<Key>(
            { it.type.toString() },
            { it.qualifier == null }
        )
    }
}

fun VariableElement.asKey(mirror: TypeMirror = asType()) = Key(
    type = mirror.toTypeName(),
    qualifier = annotationMirrors.find {
        it.annotationType.asElement().hasAnnotation("javax.inject.Qualifier")
    }?.let { AnnotationSpec.get(it) }
)