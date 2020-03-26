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

import com.lalafo.codegen.injection.ControllerBundle
import com.lalafo.codegen.processor.internal.hasAnnotation
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

data class DependencyRequest(
    val namedKey: NamedKey,
    val hasBundleAnnotation: Boolean,
    val isControllerArgs: Boolean
) {
    val key get() = namedKey.key
    val name get() = namedKey.name

    override fun toString(): String {
        return (if (hasBundleAnnotation && isControllerArgs) "@ControllerBundle " else "") + "$key $name"
    }
}

fun VariableElement.asDependencyRequest(
    controllerArgs: TypeMirror,
    types: Types
): DependencyRequest {
    return DependencyRequest(
        namedKey = asNamedKey(),
        hasBundleAnnotation = hasAnnotation<ControllerBundle>(),
        isControllerArgs = types.isAssignable(asType(), controllerArgs)
    )
}