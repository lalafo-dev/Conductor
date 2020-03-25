package com.lalafo.codegen.processor.injection

import com.google.auto.common.MoreElements
import com.google.auto.common.MoreTypes
import com.google.auto.service.AutoService
import com.lalafo.codegen.injection.InjectController
import com.lalafo.codegen.injection.ControllerBundle
import com.lalafo.codegen.processor.internal.*
import com.squareup.javapoet.JavaFile
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
@AutoService(Processor::class)
class ControllerInjectionProcessor : AbstractProcessor() {

    private lateinit var sourceVersion: SourceVersion
    private lateinit var messager: Messager
    private lateinit var filer: Filer
    private lateinit var elements: Elements
    private lateinit var types: Types

    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        sourceVersion = env.sourceVersion
        messager = env.messager
        filer = env.filer
        elements = env.elementUtils
        types = env.typeUtils
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(
        ControllerBundle::class.java.canonicalName,
        InjectController::class.java.canonicalName,
        InjectController.Factory::class.java.canonicalName
    )

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.findAllInjectionCandidates()
            .mapNotNull { it.toControllerInjectElementsOrNull() }
            .associateWith { it.toControllerInjectionOrNull() }
            .filterNotNullValues()
            .forEach(::writeControllerInjection)

        val controllerArgumentMethods = roundEnv.findElementsAnnotatedWith<ControllerBundle>()
            .map { it.enclosingElement as ExecutableElement }

        // Error any non-constructor usage of @ControllerBundle.
        controllerArgumentMethods
            .filterNot { it.simpleName.contentEquals("<init>") }
            .forEach {
                error("@ControllerBundle is only supported on constructor parameters", it)
            }

        // Error any constructor usage of @ControllerBundle which lacks method annotations.
        controllerArgumentMethods
            .filter { it.simpleName.contentEquals("<init>") }
            .filter { it.annotationMirrors.isEmpty() }
            .forEach {
                error(
                    "@ControllerBundle parameter use requires a constructor annotation such as @InjectController.",
                    it
                )
            }

        // Error any constructor usage of @ControllerBundle which also uses @Inject.
        controllerArgumentMethods
            .filter { it.simpleName.contentEquals("<init>") }
            .filter { it.hasAnnotation("javax.inject.Inject") }
            .forEach {
                error("@ControllerBundle parameter does not work with @Inject! Use @InjectController!", it)
            }

        return false
    }

    private fun RoundEnvironment.findAllInjectionCandidates(): List<TypeElement> {
        val constructors = findElementsAnnotatedWith<InjectController>()
            .map { it.enclosingElement as TypeElement }

        val (enclosed, orphaned) = findElementsAnnotatedWith<InjectController.Factory>()
            .partition { it.enclosingElement.kind == ElementKind.CLASS }

        orphaned.forEach {
            error("@InjectController.Factory must be declared as a nested type", it)
        }

        val factories = enclosed.map { it.enclosingElement as TypeElement }

        return (factories + constructors).distinctBy {
            MoreTypes.equivalence().wrap(it.asType())
        }
    }

    private fun TypeElement.toControllerInjectElementsOrNull(): ControllerInjectElements? {
        var valid = true

        if (Modifier.PRIVATE in modifiers) {
            error("@InjectController-using types must be not private", this)
            valid = false
        }

        if (enclosingElement.kind == ElementKind.CLASS && Modifier.STATIC !in modifiers) {
            error("Nested @InjectController-using types must be static", this)
            valid = false
        }

        val constructors = enclosedElements
            .filter { it.kind == ElementKind.CONSTRUCTOR }
            .filter { it.hasAnnotation<InjectController>() }
            .castEach<ExecutableElement>()

        if (constructors.isEmpty()) {
            error("Controller injection requires an @InjectController-annotated constructor.", this)
            valid = false
        } else if (constructors.size > 1) {
            error("Multiple @InjectController constructs found.", this)
            valid = false
        }

        val nestedFactoriesTypes = enclosedElements
            .filter { it.hasAnnotation<InjectController.Factory>() }
            .castEach<TypeElement>()

        if (nestedFactoriesTypes.size > 1) {
            error("Multiple controller factories annotated with @InjectController.Factory found.", this)
            valid = false
        }

        if (!valid) return null

        // Trying to construct elements here
        val injectionConstructor = constructors.single()
        if (Modifier.PRIVATE in injectionConstructor.modifiers) {
            error("@InjectController constructor  must be not private.", injectionConstructor)
            valid = false
        }

        val factoryType = nestedFactoriesTypes.firstOrNull()
        var factoryMethod: ExecutableElement? = null

        if (factoryType != null) {
            // Check factory here
            if (factoryType.kind != ElementKind.INTERFACE) {
                error("@InjectController.Factory must be an interface.", factoryType)
                valid = false
            }

            if (Modifier.PRIVATE in factoryType.modifiers) {
                error("@InjectController.Factory must be not private.", factoryType)
                valid = false
            }

            val factoryMethods = MoreElements.getLocalAndInheritedMethods(factoryType, types, elements)
                .filterNot { it.isDefault }

            if (factoryMethods.isEmpty()) {
                error("Factory interface does not define a factory method.", factoryType)
                valid = false
            } else if (factoryMethods.size > 1) {
                error("Factory interface defines multiple factory methods.", factoryType)
                valid = false
            }

            if (!valid) return null

            factoryMethod = factoryMethods.single()
        }

        return ControllerInjectElements(
            this,
            injectionConstructor,
            factoryType,
            factoryMethod
        )
    }

    private fun ControllerInjectElements.toControllerInjectionOrNull(): ControllerInjection? {
        var valid = true

        val controller = elements.getTypeElement("com.bluelinelabs.conductor.Controller").asType()
        val isController = types.isAssignable(targetType.asType(), controller)
        if (!isController) {
            error(
                "Using @InjectController annotation on non Controller class.",
                targetConstructor
            )
            valid = false
        }

        if (!valid) return null

        val controllerArgs = elements.getTypeElement("com.bluelinelabs.conductor.ControllerArgs").asType()
        val dependencyRequests = targetConstructor.parameters.map {
            it.asDependencyRequest(
                controllerArgs = controllerArgs,
                types = types
            )
        }

        val (argumentsRequests, providerRequests) =
            dependencyRequests.partition { it.isControllerArgs }

        providerRequests.filter { it.hasBundleAnnotation }.forEach {
            error("Using @ControllerBundle annotation on injection provided type.", targetConstructor)
        }

        if (argumentsRequests.size > 1) {
            error(
                "Found more than one controller bundle arguments at @InjectController-annotated constructor",
                targetConstructor
            )
            valid = false
        } else if (argumentsRequests.size == 1) {
            val argumentRequest = argumentsRequests.single()

            if (!argumentRequest.hasBundleAnnotation) {
                warn("Controller bundle argument '${argumentRequest.namedKey.name}' is not annotated with @ControllerBundle")
            }

            if (argumentRequest.name == "instantiationArguments") {
                error("\"instantiationArguments\" argument name for @InjectController-annotated " +
                        "controller is forbidden, try another one.", targetConstructor)
                valid = false
            }
        }

        var factoryExecutable: ExecutableType? = null
        val factoryArguments = mutableListOf<NamedKey>()
        if (factoryType != null && factoryMethod != null) {
            factoryExecutable = types.asMemberOf(
                factoryType.asType() as DeclaredType,
                factoryMethod
            ) as ExecutableType

            if (factoryMethod.simpleName.toString() == "newInstanceWithArguments") {
                error("\"newInstanceWithArguments\" method name for @InjectController.Factory-annotated " +
                        "interface is forbidden, try another name.", factoryMethod)
                valid = false
            }

            val expectedKeys = argumentsRequests.map { it.namedKey }.sorted()
            val factoryKeys = factoryMethod.parameters
                .zip(factoryExecutable.parameterTypes) { element, mirror -> element.asNamedKey(mirror) }
            factoryArguments.addAll(factoryKeys)
            val keys = factoryKeys.sorted()

            if (keys != expectedKeys) {
                val message = buildString {
                    append("Factory method parameters do not match constructor @ControllerBundle parameters. ")
                    append("Both parameter type and name must match.")

                    val missingKeys = expectedKeys - keys
                    if (missingKeys.isNotEmpty()) {
                        append(
                            missingKeys.joinToString(
                                "\n * ",
                                prefix = "\nDeclared by constructor, unmatched in factory method:\n * "
                            )
                        )
                    }
                    val unknownKeys = keys - expectedKeys
                    if (unknownKeys.isNotEmpty()) {
                        append(
                            unknownKeys.joinToString(
                                "\n * ",
                                prefix = "\nDeclared by factory method, unmatched in constructor:\n * "
                            )
                        )
                    }
                }
                error(
                    message,
                    if (factoryMethod.enclosingElement == factoryType) factoryMethod else targetConstructor
                )
                valid = false
            }
        }

        if (!valid) return null


        val targetType = targetType.asType().toTypeName()

        val factoryType = factoryType?.toClassName()
        val returnType = factoryExecutable?.returnType?.toTypeName()
        val methodName = factoryMethod?.simpleName?.toString()

        val generatedAnnotation =
            createGeneratedAnnotation(sourceVersion, elements)

        return ControllerInjection(
            targetType, dependencyRequests, factoryType, methodName, returnType,
            factoryArguments, generatedAnnotation
        )
    }

    private fun writeControllerInjection(
        elements: ControllerInjectElements,
        injection: ControllerInjection
    ) {
        val generatedTypeSpec = injection.brewJava()
            .toBuilder()
            .addOriginatingElement(elements.targetType)
            .build()

        JavaFile.builder(injection.generatedType.packageName(), generatedTypeSpec)
            .addFileComment("Generated by @InjectController. Do not modify!")
            .build()
            .writeTo(filer)
    }

    private fun warn(message: String, element: Element? = null) {
        messager.printMessage(Diagnostic.Kind.WARNING, message, element)
    }

    private fun error(message: String, element: Element? = null) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element)
    }

    private data class ControllerInjectElements(
        val targetType: TypeElement,
        val targetConstructor: ExecutableElement,
        val factoryType: TypeElement?,
        val factoryMethod: ExecutableElement?
    )
}