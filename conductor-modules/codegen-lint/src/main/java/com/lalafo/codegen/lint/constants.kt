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
