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
