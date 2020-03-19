package com.lalafo.codegen.lint.factory

import com.android.tools.lint.detector.api.*
import java.util.*

internal val IMPLEMENTATION = Implementation(
    ControllerFactoryDetector::class.java,
    EnumSet.of(Scope.JAVA_FILE)
)

internal val ERROR_FACTORY_NOT_INTERFACE = Issue.create(
    id = "InjectionFactoryNotInterface",
    briefDescription = "@InjectController.Factory annotated type must be an interface.",
    explanation = "@InjectController.Factory annotated type must be an interface.",
    category = Category.CORRECTNESS,
    priority = 10,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_MULTIPLE_FACTORIES = Issue.create(
    id = "MultipleInjectionFactories",
    briefDescription = "Controller supposed to have only one or less @InjectController.Factory-annotated types.",
    explanation = "Controller supposed to have only one or less @InjectController.Factory-annotated types.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_PRIVATE_FACTORY = Issue.create(
    id = "InjectionFactoryPrivate",
    briefDescription = "Custom controller factory couldn't be private.",
    explanation = "Custom controller factory couldn't be private.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_FACTORY_TOO_MUCH_METHODS = Issue.create(
    id = "InjectionFactoryTooMuchMethods",
    briefDescription = "Custom controller factory couldn't have too much methods.",
    explanation = "Custom controller factory couldn't have too much methods.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_FACTORY_NO_METHODS = Issue.create(
    id = "InjectionFactoryNoMethods",
    briefDescription = "Custom controller factory must have one factory method.",
    explanation = "Custom controller factory must have one factory method.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_FACTORY_METHOD_NULLABLE_ARGUMENT = Issue.create(
    id = "InjectionFactoryNullableArgument",
    briefDescription = "Custom controller factory nullable arguments are not supported for now.",
    explanation = "Custom controller factory should fit Controller constructor.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_FACTORY_METHOD_ILLEGAL_ARGUMENTS = Issue.create(
    id = "InjectionFactoryIllegalArguments",
    briefDescription = "Custom controller factory should fit Controller constructor.",
    explanation = "Custom controller factory should fit Controller constructor.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val WARNING_FACTORY_METHOD_ILLEGAL_RETURN_TYPE = Issue.create(
    id = "InjectionFactoryIllegalReturnType",
    briefDescription = "Custom controller factory method return type is invalid",
    explanation = "Custom controller factory should fit Controller class.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.WARNING,
    implementation = IMPLEMENTATION
)

internal val ERROR_FACTORY_METHOD_ILLEGAL_RETURN_TYPE = Issue.create(
    id = "ErrorInjectionFactoryIllegalReturnType",
    briefDescription = "Custom controller factory method return type is invalid",
    explanation = "Custom controller factory should fit Controller class.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)