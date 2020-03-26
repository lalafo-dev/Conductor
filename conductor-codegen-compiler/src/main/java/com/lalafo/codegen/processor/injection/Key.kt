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