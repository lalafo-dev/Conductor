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

package com.lalafo.codegen.lint.factory

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.util.InheritanceUtil
import com.lalafo.codegen.lint.Constants
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.toUElement

class ControllerFactoryDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf(UClass::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return Handler(
            context
        )
    }

    private class Handler(
        private val javaContext: JavaContext
    ) : UElementHandler() {

        override fun visitClass(node: UClass) {
            if (!InheritanceUtil.isInheritor(node, Constants.Conductor.CONTROLLER)) {
                return
            }

            val constructor = getInjectionConstructorOrNull(node) ?: return
            val bundle = constructor.getControllerBundleOrNull()
            val factory = getFactoryOrNull(node) ?: return
            val factoryMethod = factory.getFactoryMethodOrNull() ?: return

            val params = factoryMethod.parameterList
            val expectedArgumentSize = if (bundle != null) 1 else 0

            if (params.parameters.size != expectedArgumentSize) {
                javaContext.report(
                    ERROR_FACTORY_METHOD_ILLEGAL_ARGUMENTS,
                    params,
                    javaContext.getLocation(params),
                    if (params.parameters.size > expectedArgumentSize)
                        "Too much parameters in factory method"
                    else
                        "Not enough params in factory method"
                )
                return
            }

            if (factoryMethod.hasAnnotation(Constants.Annotations.ANDROID_NULLABLE)) {
                val annotation = factoryMethod.getAnnotation(Constants.Annotations.ANDROID_NULLABLE) ?: return
                javaContext.report(
                    WARNING_FACTORY_METHOD_ILLEGAL_RETURN_TYPE,
                    annotation,
                    javaContext.getLocation(annotation),
                    "Factory method return type couldn't be Nullable"
                )
            } else if (factoryMethod.hasAnnotation(Constants.Annotations.KOTLIN_NULLABLE)) {
                val uMethod = factoryMethod.toUElement() ?: return
                javaContext.report(
                    WARNING_FACTORY_METHOD_ILLEGAL_RETURN_TYPE,
                    uMethod,
                    javaContext.getLocation(uMethod),
                    "Factory method return type couldn't be nullable."
                )
            }

            val factoryMethodParam = params.parameters.firstOrNull()

            val controllerType = JavaPsiFacade.getElementFactory(node.project)
                .createType(node)

            if (factoryMethod.returnType?.isAssignableFrom(controllerType) != true) {
                val uMethod = factoryMethod.toUElement() ?: return
                javaContext.report(
                    ERROR_FACTORY_METHOD_ILLEGAL_RETURN_TYPE,
                    uMethod,
                    javaContext.getLocation(uMethod),
                    "Factory method return type must be assignable from ${controllerType.canonicalText}"
                )
            }

            if (factoryMethodParam != null && bundle != null) {
                if (factoryMethodParam.hasAnnotation(Constants.Annotations.ANDROID_NULLABLE)
                    || factoryMethodParam.hasAnnotation(Constants.Annotations.KOTLIN_NULLABLE)
                ) {
                    val uParam = factoryMethodParam.toUElement() ?: return
                    javaContext.report(
                        ERROR_FACTORY_METHOD_NULLABLE_ARGUMENT,
                        uParam,
                        javaContext.getLocation(uParam),
                        "Factory method nullable arguments are not supported for now."
                    )
                }

                if (factoryMethodParam.name != bundle.name) {
                    val uParam = factoryMethodParam.toUElement() ?: return
                    javaContext.report(
                        ERROR_FACTORY_METHOD_ILLEGAL_ARGUMENTS,
                        uParam,
                        javaContext.getNameLocation(uParam),
                        "Param name supposed to be '${bundle.name}'"
                    )
                }

                if (factoryMethodParam.type != bundle.type) {
                    val type = factoryMethodParam.toUElement() ?: return
                    javaContext.report(
                        ERROR_FACTORY_METHOD_ILLEGAL_ARGUMENTS,
                        type,
                        javaContext.getLocation(type),
                        "Param type supposed to be '${bundle.type.canonicalText}'."
                    )
                }
            }
        }

        private fun getInjectionConstructorOrNull(node: UClass): PsiMethod? {
            val constructors = node.constructors
            val hasInjectionConstructor = constructors.any {
                it.hasAnnotation(Constants.Annotations.CONTROLLER_INJECT)
            }

            return if (hasInjectionConstructor && constructors.size == 1) {
                constructors.first()
            } else {
                null
            }
        }

        private fun PsiMethod.getControllerBundleOrNull(): PsiParameter? {
            val params = parameterList

            val bundles = params.parameters.filter {
                InheritanceUtil.isInheritor(
                    it.type,
                    Constants.Conductor.CONTROLLER_ARGS
                )
            }

            return bundles.firstOrNull()
        }

        private fun getFactoryOrNull(node: UClass): UClass? {
            val classes = node.allInnerClasses
            val factoryClasses = classes.filter {
                it.hasAnnotation(Constants.Annotations.CONTROLLER_INJECT_FACTORY)
            }

            val (interfaces, notInterfaces) = factoryClasses.partition {
                it.isInterface
            }

            if (notInterfaces.isNotEmpty()) {
                notInterfaces.forEach {
                    val uClass = it.toUElement() ?: return null
                    javaContext.report(
                        ERROR_FACTORY_NOT_INTERFACE,
                        uClass,
                        javaContext.getLocation(uClass),
                        "Not an interface."
                    )
                }

                return null
            }

            val factory = if (interfaces.size > 1) {
                interfaces.forEach {
                    val uClass = it.toUElement() ?: return null
                    javaContext.report(
                        ERROR_MULTIPLE_FACTORIES,
                        uClass,
                        javaContext.getLocation(uClass),
                        "Only one or less controller custom factories allowed per Controller class."
                    )
                }
                null
            } else {
                interfaces.firstOrNull()?.toUElement() as? UClass
            }

            val kotlinFactory = factory?.javaPsi as? KtClass
            if (factory?.hasModifier(JvmModifier.PUBLIC) == false ||
                (kotlinFactory != null && !kotlinFactory.isPublic)
            ) {
                javaContext.report(
                    ERROR_PRIVATE_FACTORY,
                    factory,
                    javaContext.getNameLocation(factory),
                    "Factory is not public."
                )
            }

            return factory
        }

        private fun UClass.getFactoryMethodOrNull(): PsiMethod? {
            val nonDefaultMethods = allMethods
                .filter {
                    // Filtering Java default methods
                    it.name !in listOf(
                        "equals",
                        "toString",
                        "clone",
                        "wait",
                        "notify",
                        "notifyAll",
                        "hashCode",
                        "finalize",
                        "getClass",
                        "Object"
                    )
                }
                .filter { method ->
                    method.children.none { child ->
                        child is PsiCodeBlock
                    }
                }

            if (nonDefaultMethods.size > 1) {
                nonDefaultMethods.forEach {
                    val uMethod = it.toUElement() ?: return null
                    javaContext.report(
                        ERROR_FACTORY_TOO_MUCH_METHODS,
                        uMethod,
                        javaContext.getLocation(uMethod),
                        "Too much methods in one factory."
                    )
                }
            } else if (nonDefaultMethods.isEmpty()) {
                javaContext.report(
                    ERROR_FACTORY_NO_METHODS,
                    this,
                    javaContext.getNameLocation(this),
                    "Too much methods should contain one factory method."
                )
            }

            return nonDefaultMethods.firstOrNull()
        }
    }


}