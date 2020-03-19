package com.lalafo.codegen.lint.constructor

import com.android.tools.lint.detector.api.*
import com.lalafo.codegen.lint.Constants
import java.util.*


internal val IMPLEMENTATION = Implementation(
    ControllerConstructorIssueDetector::class.java,
    EnumSet.of(Scope.JAVA_FILE))

internal val WARNING_NO_BUNDLE_ANNOTATION = Issue.create(
    id = "NoControllerBundleAnnotation",
    briefDescription = "No @ControllerBundle annotation for @InjectController " +
            "annotated constructor assisted param.",
    explanation = "By not annotating the controller assisted arguments with " +
            "@ControllerBundle you are decreasing code readability for other developers.",
    category = Category.CORRECTNESS,
    priority = 5,
    severity = Severity.WARNING,
    implementation = IMPLEMENTATION
)

internal val ERROR_TOO_MUCH_CONSTRUCTORS = Issue.create(
    id = "MultipleControllerConstructors",
    briefDescription = "Controller using @InjectController argument can have only one primary constructor.",
    explanation = "Multiple constructors are not supported for single Controller-inheriting class used " +
            "with ${Constants.LIB_NAME} to generate master ControllerFactory.",
    category = Category.CORRECTNESS,
    priority = 9,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_TOO_MUCH_BUNDLES = Issue.create(
    id = "MultipleControllerBundles",
    briefDescription = "Controller using @InjectController must have 1 or least assisted bundles",
    explanation = "${Constants.LIB_NAME} supports only 1 or less controller bundles. It's wont compile otherwise.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_NULLABLE_BUNDLE = Issue.create(
    id = "NullableControllerBundle",
    briefDescription = "Controller bundle is nullable.",
    explanation = "For now ${Constants.LIB_NAME} is not supporting nullable Controller params. Use non nullable instantiation args.",
    category = Category.CORRECTNESS,
    priority = 6,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_PRIVATE_CONSTRUCTOR = Issue.create(
    id = "PrivateControllerInjectionConstructor",
    briefDescription = "Controller injection constructor is private.",
    explanation = "${Constants.LIB_NAME} is not supporting private injection Controller constructors.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_JAVAX_INJECT_USAGE= Issue.create(
    id = "ControllerConstructorJavaXInject",
    briefDescription = "Controller constructor annotated with javax.inject @Inject annotation",
    explanation = "${Constants.LIB_NAME} doesn't allow you to use @Inject annotation in same time with @InjectController. Use only " +
            "@InjectController annotation!",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_PRIVATE_CONTROLLER_CLASS= Issue.create(
    id = "InjectControllerUsingClassPrivate",
    briefDescription = "Controller class using @InjectController constructor using Controller class is private.",
    explanation = "Controller class using @InjectController constructor using Controller class is private.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_ABSTRACT_CONTROLLER_CLASS= Issue.create(
    id = "InjectControllerUsingClassAbstract",
    briefDescription = "Controller class using @InjectController constructor using Controller class is abstract or sealed.",
    explanation = "Controller class using @InjectController constructor using Controller class is abstract or sealed.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)

internal val ERROR_INNER_CONTROLLER_CLASS= Issue.create(
    id = "InjectControllerUsingClassInner",
    briefDescription = "Controller class using @InjectController constructor using Controller class is inner.",
    explanation = "Controller class using @InjectController constructor using Controller class is inner.",
    category = Category.CORRECTNESS,
    priority = 8,
    severity = Severity.ERROR,
    implementation = IMPLEMENTATION
)