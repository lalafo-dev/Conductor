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