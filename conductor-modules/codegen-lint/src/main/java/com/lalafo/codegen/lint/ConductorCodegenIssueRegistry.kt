package com.lalafo.codegen.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.lalafo.codegen.lint.constructor.*
import com.lalafo.codegen.lint.factory.*

class ConductorCodegenIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(
            // Constructor
            WARNING_NO_BUNDLE_ANNOTATION,
            ERROR_TOO_MUCH_BUNDLES,
            ERROR_PRIVATE_CONSTRUCTOR,
            ERROR_TOO_MUCH_CONSTRUCTORS,
            ERROR_NULLABLE_BUNDLE,
            ERROR_JAVAX_INJECT_USAGE,
            ERROR_PRIVATE_CONTROLLER_CLASS,
            ERROR_INNER_CONTROLLER_CLASS,
            ERROR_ABSTRACT_CONTROLLER_CLASS,
            // Factory
            ERROR_FACTORY_NOT_INTERFACE,
            ERROR_MULTIPLE_FACTORIES,
            ERROR_PRIVATE_FACTORY,
            ERROR_FACTORY_TOO_MUCH_METHODS,
            ERROR_FACTORY_NO_METHODS,
            ERROR_FACTORY_METHOD_ILLEGAL_ARGUMENTS,
            WARNING_FACTORY_METHOD_ILLEGAL_RETURN_TYPE,
            ERROR_FACTORY_METHOD_NULLABLE_ARGUMENT,
            ERROR_FACTORY_METHOD_ILLEGAL_RETURN_TYPE
        )

    override val api: Int = CURRENT_API
}
