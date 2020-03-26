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

internal object Constants {

    const val LIB_NAME = "'conductor-codegen'"

    object Annotations {
        const val CONTROLLER_BUNDLE = "com.lalafo.codegen.injection.ControllerBundle"
        const val CONTROLLER_INJECT = "com.lalafo.codegen.injection.InjectController"
        const val CONTROLLER_INJECT_FACTORY = "com.lalafo.codegen.injection.InjectController.Factory"

        const val KOTLIN_NULLABLE = "org.jetbrains.annotations.Nullable"
        const val ANDROID_NULLABLE = "androidx.annotation.Nullable"

        const val INJECT = "javax.inject.Inject"
    }

    object Conductor {
        const val CONTROLLER = "com.bluelinelabs.conductor.Controller"
        const val CONTROLLER_ARGS = "com.bluelinelabs.conductor.ControllerArgs"
    }
}
