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

import com.google.auto.service.AutoService
import com.lalafo.codegen.injection.InjectController
import com.lalafo.codegen.dagger2.ConductorBindingModule
import com.lalafo.codegen.factory.ConductorFactory
import com.lalafo.codegen.processor.internal.*
import com.squareup.javapoet.JavaFile
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
@AutoService(Processor::class)
class ConductorBindingModuleDagger2Processor : AbstractProcessor() {

    private lateinit var sourceVersion: SourceVersion
    private lateinit var messager: Messager
    private lateinit var filer: Filer
    private lateinit var elements: Elements

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedAnnotationTypes() = setOf(
        InjectController.Factory::class.java.canonicalName,
        ConductorFactory::class.java.canonicalName,
        ConductorBindingModule::class.java.canonicalName
    )

    private var unprocessedCustomBindingsNames = mutableListOf<Name>()
    private var unprocessedControllerFactoryNames = mutableListOf<Name>()
    private var userModule: String? = null

    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        sourceVersion = env.sourceVersion
        messager = env.messager
        filer = env.filer
        elements = env.elementUtils
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        unprocessedCustomBindingsNames.addAll(roundEnv.findElementsAnnotatedWith<InjectController.Factory>()
            .castEach<TypeElement>()
            .mapNotNull { it.qualifiedName }
        )

        unprocessedControllerFactoryNames.addAll(
            roundEnv.findElementsAnnotatedWith<ConductorFactory>()
                .castEach<TypeElement>()
                .map { it.qualifiedName }
        )

        val controllerFactoryModuleElements = roundEnv.findControllerFactoryModuleOrNull()
        if (controllerFactoryModuleElements != null) {
            val moduleType = controllerFactoryModuleElements.moduleType

            val userModuleCopy = userModule
            if (userModuleCopy != null) {
                val userModuleType = elements.getTypeElement(userModuleCopy)
                error("Multiple @ConductorBindingModule-annotated modules found.", userModuleType)
                error("Multiple @ConductorBindingModule-annotated modules found.", moduleType)
                userModule = null
            } else {
                userModule = moduleType.qualifiedName.toString()

                val controllerFactoryModule = controllerFactoryModuleElements.toGeneratedControllerFactoryModule()
                writeFactoryModule(controllerFactoryModuleElements, controllerFactoryModule)
            }
        }

        // Wait until processing is ending to validate that the @AssistedModule's @Module annotation
        // includes the generated type.
        if (roundEnv.processingOver()) {
            val userModuleCopy = userModule
            if (userModuleCopy != null) {
                // In the processing round in which we handle the @ConductorBindingModule the @Module annotation's
                // includes contain an <error> type because we haven't generated the assisted module yet.
                // As a result, we need to re-lookup the element so that its referenced types are available.
                val userModule = elements.getTypeElement(userModuleCopy)

                // Previous validation guarantees this annotation is present.
                val moduleAnnotation = userModule.getAnnotation("dagger.Module")!!
                // Dagger guarantees this property is present and is an array of types or errors.
                val includes = moduleAnnotation.getValue("includes", elements)!!
                    .cast<MirrorValue.Array>()
                    .filterIsInstance<MirrorValue.Type>()

                val generatedModuleName = userModule.toClassName().controllerFactoryModuleName()
                val referencesGeneratedModule = includes
                    .map { it.toTypeName() }
                    .any { it == generatedModuleName }
                if (!referencesGeneratedModule) {
                    error(
                        "@ConductorBindingModule's @Module must include ${generatedModuleName.simpleName()}",
                        userModule
                    )
                }
            }
        }

        return false
    }


    private fun RoundEnvironment.findControllerFactoryModuleOrNull(): ControllerFactoryModuleElements? {
        val controllerFactoryModules = findElementsAnnotatedWith<ConductorBindingModule>()
            .castEach<TypeElement>()

        if (controllerFactoryModules.isEmpty()) {
            return null
        }

        if (controllerFactoryModules.size > 1) {
            controllerFactoryModules.forEach {
                error("Multiple @ConductorBindingModule-annotated modules found.", it)
            }
            return null
        }

        val factoryModule = controllerFactoryModules.single()
        if (!factoryModule.hasAnnotation("dagger.Module")) {
            error("@ConductorBindingModule must also be annotated as a Dagger @Module", factoryModule)
            return null
        }

        val targetTypeToFactoryType = unprocessedCustomBindingsNames
            .map(elements::getTypeElement)
            .associateBy { it.enclosingElement as? TypeElement }
            .filterNotNullKeys()

        val factoryTypeElements = unprocessedControllerFactoryNames
            .map(elements::getTypeElement)

        return ControllerFactoryModuleElements(factoryModule, factoryTypeElements, targetTypeToFactoryType)
    }

    private fun ControllerFactoryModuleElements.toGeneratedControllerFactoryModule(): GeneratedControllerFactoryModule {
        val moduleName = moduleType.toClassName()
        val targetControllerFactories = targetControllerFactories
        val targetBindings = targetTypeToFactoryType
            .map { (target, factory) -> target.asType().toTypeName() to factory.toClassName() }
            .toMap(TreeMap())
        val public = Modifier.PUBLIC in moduleType.modifiers
        val generatedAnnotation =
            createGeneratedAnnotation(sourceVersion, elements)
        return GeneratedControllerFactoryModule(
            moduleName, public, targetControllerFactories, targetBindings,
            generatedAnnotation
        )
    }

    private fun writeFactoryModule(
        elements: ControllerFactoryModuleElements,
        module: GeneratedControllerFactoryModule
    ) {
        val generatedTypeSpec = module.brewJava()
            .toBuilder()
            .addOriginatingElement(elements.moduleType)
            .applyEach(elements.targetControllerFactories) {
                addOriginatingElement(it)
            }
            .build()
        JavaFile.builder(module.generatedType.packageName(), generatedTypeSpec)
            .addFileComment("Generated by @ConductorBindingModule. Do not modify!")
            .build()
            .writeTo(filer)
    }


    private fun error(message: String, element: Element? = null) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element)
    }

    private data class ControllerFactoryModuleElements(
        val moduleType: TypeElement,
        val targetControllerFactories: List<TypeElement>,
        val targetTypeToFactoryType: Map<TypeElement, TypeElement>
    )
}