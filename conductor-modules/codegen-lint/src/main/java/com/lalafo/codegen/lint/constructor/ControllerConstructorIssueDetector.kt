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

package com.lalafo.codegen.lint.constructor

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.PsiClass
import com.intellij.psi.util.InheritanceUtil
import com.lalafo.codegen.lint.Constants
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.toUElement

class ControllerConstructorIssueDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf(UClass::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return Handler(
            context
        )
    }

    private class Handler(
        private val context: JavaContext
    ) : UElementHandler() {

        override fun visitClass(node: UClass) {
            if (!InheritanceUtil.isInheritor(
                    node,
                    Constants.Conductor.CONTROLLER
                )
            ) {
                return
            }

            val constructors = node.constructors
            val hasInjectionConstructor = constructors.any {
                it.hasAnnotation(Constants.Annotations.CONTROLLER_INJECT)
            }

            if (constructors.size > 1 && hasInjectionConstructor) {
                constructors.forEach {
                    val uMethod = it.toUElement() ?: return
                    context.report(
                        ERROR_TOO_MUCH_CONSTRUCTORS,
                        uMethod, context.getLocation(uMethod),
                        "Only one Controller constructor allowed in Controller for conductor-codegen usage."
                    )
                }
            } else if (!hasInjectionConstructor) return

            val constructor = constructors.first()

            if (constructor.hasModifier(JvmModifier.PRIVATE)) {
                context.report(
                    ERROR_PRIVATE_CONSTRUCTOR,
                    constructor,
                    context.getLocation(constructor.modifierList),
                    "Controller injection constructor is private."
                )
            }

            if (constructor.hasAnnotation(Constants.Annotations.INJECT)) {
                val annotation = constructor.getAnnotation(Constants.Annotations.INJECT)
                if (annotation != null) {
                    context.report(
                        ERROR_JAVAX_INJECT_USAGE,
                        annotation,
                        context.getLocation(annotation),
                        "Using @Inject in couple with @InjectController."
                    )
                }
            }

            val ktClass = node.sourcePsi as? KtClass

            if (node.hasModifier(JvmModifier.PRIVATE) || ktClass?.isPrivate() == true) {
                context.report(
                    ERROR_PRIVATE_CONTROLLER_CLASS,
                    node,
                    context.getNameLocation(node),
                    "@InjectController-using class is private."
                )
            }

            if (node.hasModifier(JvmModifier.ABSTRACT) || ktClass?.isAbstract() == true
                || ktClass?.isSealed() == true) {
                if (ktClass == null) {
                    context.report(
                        ERROR_ABSTRACT_CONTROLLER_CLASS,
                        node,
                        context.getNameLocation(node),
                        "@InjectController-using class is abstract."
                    )
                } else {
                    context.report(
                        ERROR_ABSTRACT_CONTROLLER_CLASS,
                        node,
                        context.getNameLocation(node),
                        "@InjectController-using class is ${if (ktClass.isSealed()) "sealed" else "abstract"}."
                    )
                }
            }

            if (ktClass?.isInner() == true || (!node.hasModifier(JvmModifier.STATIC) && node.parent is PsiClass)) {
                if (ktClass != null) {
                    context.report(
                        ERROR_INNER_CONTROLLER_CLASS,
                        node,
                        context.getNameLocation(node),
                        "@InjectController-using class is inner."
                    )
                } else {
                    context.report(
                        ERROR_INNER_CONTROLLER_CLASS,
                        node,
                        context.getNameLocation(node),
                        "@InjectController-using class is a non-static inner type."
                    )
                }
            }

            val params = constructor.parameterList

            val bundles = params.parameters.filter {
                InheritanceUtil.isInheritor(
                    it.type,
                    Constants.Conductor.CONTROLLER_ARGS
                )
            }

            if (bundles.size > 1) {
                bundles.forEach {
                    val uBundle = it.toUElement() ?: return
                    context.report(
                        ERROR_TOO_MUCH_BUNDLES,
                        uBundle, context.getLocation(uBundle),
                        "Too much controller bundles in single Controller constructor. " +
                                "1 or less allowed for now."
                    )
                }
            } else if (bundles.isEmpty()) return

            val bundle = bundles.first()

            if (!bundle.hasAnnotation(Constants.Annotations.CONTROLLER_BUNDLE)) {
                val uBundle = bundle.toUElement() ?: return
                context.report(
                    WARNING_NO_BUNDLE_ANNOTATION, uBundle,
                    context.getLocation(uBundle),
                    "No @ControllerBundle annotation on ControllerArgs-implementing param."
                )
                return
            }

            if (bundle.hasAnnotation(Constants.Annotations.KOTLIN_NULLABLE) ||
                bundle.hasAnnotation(Constants.Annotations.ANDROID_NULLABLE)
            ) {
                val uBundle = bundle.toUElement() ?: return
                context.report(
                    ERROR_NULLABLE_BUNDLE, uBundle,
                    context.getLocation(uBundle),
                    "Controller bundle is nullable."
                )
            }
        }
    }
}