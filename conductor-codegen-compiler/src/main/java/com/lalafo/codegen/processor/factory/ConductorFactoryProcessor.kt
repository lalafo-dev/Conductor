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

import com.google.auto.common.MoreElements
import com.google.auto.service.AutoService
import com.lalafo.codegen.factory.ConductorFactory
import com.lalafo.codegen.injection.InjectController
import com.lalafo.codegen.processor.injection.bindingName
import com.lalafo.codegen.processor.internal.*
import com.squareup.javapoet.JavaFile
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
@AutoService(Processor::class)
class ConductorFactoryProcessor : AbstractProcessor() {

    private lateinit var sourceVersion: SourceVersion
    private lateinit var messager: Messager
    private lateinit var filer: Filer
    private lateinit var elements: Elements
    private lateinit var types: Types

    private val unprocessedBindingNames = mutableListOf<Name>()

    override fun getSupportedSourceVersion() = SourceVersion.latest()

    override fun getSupportedAnnotationTypes() = setOf(
        ConductorFactory::class.java.canonicalName,
        InjectController::class.java.canonicalName
    )

    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        sourceVersion = env.sourceVersion
        messager = env.messager
        filer = env.filer
        elements = env.elementUtils
        types = env.typeUtils
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        unprocessedBindingNames += roundEnv.findElementsAnnotatedWith<InjectController>()
            .map { it.enclosingElement as TypeElement }
            .map { it.qualifiedName }

        val factoryCandidate = roundEnv.findControllerFactoryCandidateOrNull() ?: return false
        val providerDelegates = findAllControllerInjectionCandidates()
            .map { it.toFactoryDelegateProvider() }

        val factory = factoryCandidate.toControllerFactoryOrNull(providerDelegates) ?: return false
        writeFactory(factory)

        return false
    }

    private fun findAllControllerInjectionCandidates(): List<TypeElement> {
        return unprocessedBindingNames
            .mapNotNull(elements::getTypeElement)
    }

    private fun TypeElement.toFactoryDelegateProvider(): FactoryDelegateProvider {
        // Skip validation, let the binding processor do this
        val targetName = toClassName()
        val bindingName = targetName.rawClassName().bindingName()
        return FactoryDelegateProvider(this, targetName, bindingName)
    }

    private fun RoundEnvironment.findControllerFactoryCandidateOrNull(): TypeElement? {
        val controllerFactories = findElementsAnnotatedWith<ConductorFactory>()
            .castEach<TypeElement>()

        if (controllerFactories.isEmpty()) {
            return null
        }

        if (controllerFactories.size > 1) {
            controllerFactories.forEach {
                error("Multiple @ConductorFactory-annotated classes found.", it)
            }
            return null
        }

        val controllerFactory = controllerFactories.single()

        // Check it implements
        val controllerFactoryMirror = elements.getTypeElement(
            "com.bluelinelabs.conductor.ControllerFactory"
        ).asType()

        val isControllerFactory = types.isAssignable(controllerFactory.asType(), controllerFactoryMirror)
        if (!isControllerFactory) {
            error(
                "Using @ConductorFactory annotation on non ControllerFactory class.",
                controllerFactory
            )
            return null
        }

        if (Modifier.PRIVATE in controllerFactory.modifiers) {
            error("@ConductorFactory-using types must be not private", controllerFactory)
            return null
        }

        if (controllerFactory.kind == ElementKind.CLASS && Modifier.FINAL in controllerFactory.modifiers) {
            error("@ConductorFactory-using types must be defined as non-final classes.", controllerFactory)
            return null
        }

        if (controllerFactory.enclosingElement.kind == ElementKind.CLASS && Modifier.STATIC !in controllerFactory.modifiers) {
            error("Nested @ConductorFactory-using types must be static.", controllerFactory)
            return null
        }

        val constructors = controllerFactory.enclosedElements
            .filter { it.kind == ElementKind.CONSTRUCTOR }
            .castEach<ExecutableElement>()
            .filter { Modifier.PRIVATE !in it.modifiers }
            .filter { it.parameters.isEmpty() }

        if (constructors.isEmpty()) {
            error(
                "@ConductorFactory-annotated type should contain a non-private empty constructor.",
                controllerFactory
            )
            return null
        }

        val abstractMethods = MoreElements.getLocalAndInheritedMethods(controllerFactory, types, elements)
            .filter { !it.isDefault }
            .filter { Modifier.ABSTRACT in it.modifiers }

        abstractMethods.forEach {
            error("@ConductorFactory-annotated type must not contain any abstract methods.", it)
        }

        if (abstractMethods.isNotEmpty()) {
            return null
        }

        return controllerFactory
    }

    private fun TypeElement.toControllerFactoryOrNull(
        providerDelegates: List<FactoryDelegateProvider>
    ): ControllerFactory? {
        val generatedAnnotation = createGeneratedAnnotation(sourceVersion, elements)
        return ControllerFactory(
            this,
            toClassName(),
            providerDelegates,
            generatedAnnotation
        )
    }

    private fun writeFactory(
        factory: ControllerFactory
    ) {
        val generatedTypeSpec = factory.brewJava()
            .toBuilder()
            .addOriginatingElement(factory.factoryType)
            .applyEach(factory.delegates) {
                addOriginatingElement(it.targetElement)
            }
            .build()

        JavaFile.builder(factory.generatedType.packageName(), generatedTypeSpec)
            .addFileComment("Generated by @ConductorFactory. Do not modify!")
            .build()
            .writeTo(filer)
    }

    private fun error(message: String, element: Element? = null) {
        messager.printMessage(ERROR, message, element)
    }
}