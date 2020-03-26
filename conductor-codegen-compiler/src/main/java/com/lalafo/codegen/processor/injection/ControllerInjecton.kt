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

import com.lalafo.codegen.processor.internal.*
import com.lalafo.codegen.utils.ControllerFactoryDelegate
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier

private val NON_NULL = ClassName.get("androidx.annotation", "NonNull")
private val NULLABLE = ClassName.get("androidx.annotation", "Nullable")

private val JAVAX_INJECT = ClassName.get("javax.inject", "Inject")

private val JAVAX_PROVIDER = ClassName.get("javax.inject", "Provider")

private val BINDING_INTERFACE = ClassName.get(ControllerFactoryDelegate::class.java)

data class ControllerInjection(
    // Target controller type name
    val targetType: TypeName,
    val dependencyRequests: List<DependencyRequest>,
    // Custom factory related stuff
    val factoryType: TypeName?,
    val factoryMethod: String?,
    val returnType: TypeName? = targetType,
    val factoryArguments: List<NamedKey> = dependencyRequests.filter { it.isControllerArgs }.map { it.namedKey },
    // Generated
    val generatedAnnotation: AnnotationSpec?
) {

    val generatedType = targetType.rawClassName().bindingName()

    private val providedKeys = dependencyRequests.filterNot { it.isControllerArgs }

    init {
        val requestKeys = dependencyRequests.filter { it.isControllerArgs }.map { it.namedKey }
        if (factoryType != null) {
            check(requestKeys.sorted() == factoryArguments.sorted()) {
                """
                arguments must contain the same elements as the controller dependency request.
                * factoryArguments:
                    $factoryArguments
                * controller dependencyRequests:
                    $requestKeys
            """.trimIndent()
            }
        }

        check(requestKeys.size <= 1) {
            "Only one or 0 requested arguments is supported for now.\n" +
                    "* controller dependencyRequests:\n" +
                    "\t$requestKeys"
        }
    }

    fun brewJava(): TypeSpec {
        return TypeSpec.classBuilder(generatedType)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(ParameterizedTypeName.get(BINDING_INTERFACE, targetType))
            .apply {
                if (factoryType != null) {
                    addSuperinterface(factoryType)
                }
                if (generatedAnnotation != null) {
                    addAnnotation(generatedAnnotation)
                }
            }
            .applyEach(providedKeys) {
                addField(FieldSpec.builder(it.providerType.withoutAnnotations(), it.name, Modifier.PRIVATE, Modifier.FINAL)
                    .addAnnotation(NON_NULL)
                    .build())
            }
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(JAVAX_INJECT)
                .applyEach(providedKeys) {
                    addParameter(ParameterSpec.builder(it.providerType, it.name, Modifier.FINAL)
                        .addAnnotation(NON_NULL)
                        .build())
                    addStatement("this.$1N = $1N", it.name)
                }
                .build()
            )
            .addMethod(buildBindingProviderMethod())
            .apply {
                if (factoryType != null) {
                    addMethod(buildFactoryMethod())
                }
            }
            .build()
    }


    private fun buildBindingProviderMethod(): MethodSpec {
        val methodArgumentsParam = "instantiationArguments"
        val controllerArgs = dependencyRequests.filter { it.isControllerArgs }
            .map { it.namedKey }

        val methodBuilder = MethodSpec.methodBuilder("newInstanceWithArguments")
            .addAnnotation(NON_NULL)
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(targetType)
            .addParameter(
                ParameterSpec.builder(Object::class.toClassName(), methodArgumentsParam, Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .build()
            )

        if (controllerArgs.isEmpty()) {
            methodBuilder.addReturnStatement()
        } else {
            val arg = controllerArgs.single()
            methodBuilder.beginControlFlow("if ($methodArgumentsParam instanceof $1T)", arg.key.type)
                .addStatement("$1T ${arg.name} = ($1T) $methodArgumentsParam", arg.key.type)
                .addReturnStatement()
                .nextControlFlow("else")
                .addStatement(
                    "throw new $1T($2S + $3T.class.getName() + $4S + $methodArgumentsParam + $5S)",
                    IllegalArgumentException::class.java,
                    "Expected ",
                    arg.key.type,
                    ", but got '",
                    "' instead."
                )
                .endControlFlow()

        }
        return methodBuilder.build()
    }

    private fun buildFactoryMethod(): MethodSpec {
        checkNotNull(factoryType)
        checkNotNull(factoryMethod)

        return MethodSpec.methodBuilder(factoryMethod)
            .addAnnotation(NON_NULL)
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(returnType)
            .apply {
                if (targetType is ParameterizedTypeName) {
                    addTypeVariables(targetType.typeArguments.filterIsInstance<TypeVariableName>())
                }
            }
            .applyEach(factoryArguments) { namedKey ->
                addParameter(ParameterSpec.builder(namedKey.key.type, namedKey.name, Modifier.FINAL)
                    .addAnnotation(NON_NULL)
                    .build()
                )
            }
            .addReturnStatement()
            .build()
    }

    private fun MethodSpec.Builder.addReturnStatement(): MethodSpec.Builder {
        return addStatement(
            "return new \$T(\n\$L)", targetType,
            dependencyRequests.map { it.argumentProvider }.joinToCode(",\n")
        )
    }
}

private val DependencyRequest.providerType: TypeName
    get() {
        val type = if (key.isProvider) {
            key.type // Do not wrap a Provider inside another Provider.
        } else {
            ParameterizedTypeName.get(JAVAX_PROVIDER, key.type.box())
        }
        key.qualifier?.let {
            return type.annotated(it)
        }
        return type
    }

private val DependencyRequest.argumentProvider
    get() = CodeBlock.of(if (isControllerArgs || key.isProvider) "\$N" else "\$N.get()", name)

private val Key.isProvider get() = type is ParameterizedTypeName && type.rawType == JAVAX_PROVIDER


fun ClassName.bindingName(): ClassName =
    peerClassWithReflectionNesting("${simpleName()}_ControllerFactoryDelegate")