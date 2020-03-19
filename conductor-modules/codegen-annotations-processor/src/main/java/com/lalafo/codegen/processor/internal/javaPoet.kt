package com.lalafo.codegen.processor.internal

import com.squareup.javapoet.*
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import kotlin.reflect.KClass

fun TypeElement.toClassName(): ClassName = ClassName.get(this)
fun TypeMirror.toTypeName(): TypeName = TypeName.get(this)
fun KClass<*>.toClassName(): ClassName = ClassName.get(java)

fun Iterable<CodeBlock>.joinToCode(separator: String = ", ") = CodeBlock.join(this, separator)

/**
 * Like [ClassName.peerClass] except instead of honoring the enclosing class names they are
 * concatenated with `$` similar to the reflection name. `foo.Bar.Baz` invoking this function with
 * `Fuzz` will produce `foo.Baz$Fuzz`.
 */
fun ClassName.peerClassWithReflectionNesting(name: String): ClassName {
    var prefix = ""
    var peek = this
    while (true) {
        peek = peek.enclosingClassName() ?: break
        prefix = peek.simpleName() + "$" + prefix
    }
    return ClassName.get(packageName(), prefix + name)
}

fun TypeName.rawClassName(): ClassName = when (this) {
    is ClassName -> this
    is ParameterizedTypeName -> rawType
    else -> throw IllegalStateException("Cannot extract raw class name from $this")
}