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

package com.lalafo.codegen.processor.dagger2

import com.lalafo.codegen.processor.injection.bindingName
import com.lalafo.codegen.processor.factory.controllerFactoryName
import com.lalafo.codegen.processor.internal.applyEach
import com.lalafo.codegen.processor.internal.peerClassWithReflectionNesting
import com.lalafo.codegen.processor.internal.rawClassName
import com.lalafo.codegen.processor.internal.toClassName
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier.*
import javax.lang.model.element.TypeElement


data class GeneratedControllerFactoryModule(
    val moduleName: ClassName,
    val public: Boolean,
    val targetFactoryNames: List<TypeElement>,
    val targetBindingsNames: Map<TypeName, ClassName>,

    /** An optional `@Generated` annotation marker. */
    val generatedAnnotation: AnnotationSpec? = null
) {
    val generatedType = moduleName.controllerFactoryModuleName()

    fun brewJava(): TypeSpec {
        return TypeSpec.classBuilder(generatedType)
            .addAnnotation(MODULE)
            .apply {
                if (generatedAnnotation != null) {
                    addAnnotation(generatedAnnotation)
                }
            }
            .addModifiers(ABSTRACT)
            .apply {
                if (public) {
                    addModifiers(PUBLIC)
                }
            }
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PRIVATE)
                    .build()
            )
            .applyEach(targetFactoryNames) { targetName ->
                val rawTargetName = targetName.toClassName().rawClassName()
                val targetClass = targetName.toClassName()
                val factoryClass = targetClass.controllerFactoryName()
                addMethod(
                    MethodSpec.methodBuilder(rawTargetName.bindMethodName())
                        .addAnnotation(BINDS)
                        .addModifiers(ABSTRACT)
                        .returns(targetClass)
                        .addParameter(factoryClass, "factory")
                        .build()
                )
            }
            .applyEach(targetBindingsNames) { targetName, factoryName ->
                val rawTargetName = targetName.rawClassName()
                addMethod(
                    MethodSpec.methodBuilder(rawTargetName.bindMethodName())
                        .addAnnotation(BINDS)
                        .addModifiers(ABSTRACT)
                        .returns(factoryName)
                        .addParameter(rawTargetName.bindingName(), "factory")
                        .build()
                )
            }
            .build()
    }

    companion object {
        private val MODULE = ClassName.get("dagger", "Module")
        private val BINDS = ClassName.get("dagger", "Binds")
    }
}


private fun ClassName.bindMethodName() = "bind_" + reflectionName().replace('.', '_')


private inline fun <T : Any, K, V> T.applyEach(items: Map<K, V>, func: T.(K, V) -> Unit): T {
    items.forEach { (key, value) -> func(key, value) }
    return this
}

fun ClassName.controllerFactoryModuleName(): ClassName {
    return peerClassWithReflectionNesting("${simpleNames().joinToString(separator = "_")}_ConductorBindingModule")
}