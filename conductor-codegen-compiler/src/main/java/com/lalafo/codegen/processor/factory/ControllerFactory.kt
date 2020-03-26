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

package com.lalafo.codegen.processor.factory

import com.lalafo.codegen.processor.internal.applyEach
import com.lalafo.codegen.processor.internal.peerClassWithReflectionNesting
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

private val NON_NULL = ClassName.get("androidx.annotation", "NonNull")
private val NULLABLE = ClassName.get("androidx.annotation", "Nullable")

private val CONTROLLER = ClassName.get("com.bluelinelabs.conductor", "Controller")
private val JAVAX_INJECT = ClassName.get("javax.inject", "Inject")

private val KLASS = ClassName.get("java.lang", "Class")

internal data class ControllerFactory(
    val factoryType: TypeElement,
    val factoryName: ClassName,
    val delegates: List<FactoryDelegateProvider>,
    /** An optional `@Generated` annotation marker. */
    val generatedAnnotation: AnnotationSpec? = null
) {
    val generatedType = factoryName.controllerFactoryName()

    fun brewJava(): TypeSpec {
        return TypeSpec.classBuilder(generatedType)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .superclass(factoryName)
            .apply {
                if (generatedAnnotation != null) {
                    addAnnotation(generatedAnnotation)
                }
            }
            .applyEach(delegates) {
                addField(FieldSpec.builder(it.factoryDelegateType, it.propertyName, Modifier.PRIVATE, Modifier.FINAL)
                    .addAnnotation(NON_NULL)
                    .build())
            }
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(JAVAX_INJECT)
                .addStatement("super()")
                .applyEach(delegates) {
                    addParameter(ParameterSpec.builder(it.bindingType.withoutAnnotations(), it.propertyName, Modifier.FINAL)
                        .addAnnotation(NON_NULL)
                        .build())
                    addStatement("this.$1N = $1N", it.propertyName)
                }
                .build())
            .addMethod(MethodSpec.methodBuilder("newInstance")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(NON_NULL)
                .addAnnotation(Override::class.java)
                .returns(CONTROLLER)
                .addParameter(
                    ParameterSpec.builder(ClassLoader::class.java, "classLoader")
                        .addAnnotation(NON_NULL)
                        .build()
                )
                .addParameter(
                    ParameterSpec.builder(String::class.java, "className")
                        .addAnnotation(NON_NULL)
                        .build()
                )
                .addParameter(
                    ParameterSpec.builder(Object::class.java, "args")
                        .addAnnotation(NULLABLE)
                        .build()
                )
                .addStatement(
                    "$1T clazz = loadControllerClass(classLoader, className)",
                    ParameterizedTypeName.get(KLASS, WildcardTypeName.subtypeOf(CONTROLLER))
                )
                .apply {
                    if (delegates.isEmpty()) {
                        addStatement("return super.newInstance(classLoader, className, args)")
                        return@apply
                    }

                    var useElse = false
                    delegates.forEach {
                        val useElseCopy = useElse
                        if (!useElse) useElse = true
                        val statement = (if (useElseCopy) "else " else "") + "if ($1T.class == clazz)"
                        if (!useElseCopy) {
                            beginControlFlow(statement, it.targetType)
                        } else {
                            nextControlFlow(statement, it.targetType)
                        }
                        addStatement("return ${it.propertyName}.newInstanceWithArguments(args)")
                    }
                    endControlFlow()
                    addComment("Instantiate controller from reflection with super")
                    addStatement("return super.newInstance(classLoader, className, args)")
                }
                .build()
            )
            .build()
    }
}

fun ClassName.controllerFactoryName(): ClassName {
    return peerClassWithReflectionNesting("${simpleNames().joinToString(separator = "_")}_GeneratedConductorFactory")
}