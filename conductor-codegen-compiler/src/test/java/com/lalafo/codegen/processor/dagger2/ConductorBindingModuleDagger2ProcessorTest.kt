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

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Before
import org.junit.Test
import javax.tools.JavaFileObject

private val GENERATED_TYPE = try {
    Class.forName("javax.annotation.processing.Generated")
    "javax.annotation.processing.Generated"
} catch (_: ClassNotFoundException) {
    "javax.annotation.Generated"
}

private const val GENERATED_ANNOTATION = """
@Generated(
    value = "com.lalafo.codegen.processor.dagger2.ConductorBindingModuleDagger2Processor",
    comments = "https://yallaclassifieds.atlassian.net/browse/DV-5886"
)
"""

class ConductorBindingModuleDagger2ProcessorTest {

    private lateinit var controllerFile: JavaFileObject
    private lateinit var controllerArgsFile: JavaFileObject
    private lateinit var controllerFactoryFile: JavaFileObject

    @Before
    fun setup() {
        controllerFile = JavaFileObjects.forSourceString(
            "com.bluelinelabs.conductor.Controller", """
            package com.bluelinelabs.conductor;
            
            public class Controller {
                private final ControllerArgs controllerArgs;
            
                protected Controller(ControllerArgs controllerArgs) {
                    this.controllerArgs = controllerArgs;
                }
            }
        """.trimIndent()
        )

        controllerArgsFile = JavaFileObjects.forSourceString(
            "com.bluelinelabs.conductor.ControllerArgs", """
            package com.bluelinelabs.conductor;

            public interface ControllerArgs {} 
        """.trimIndent()
        )

        controllerFactoryFile = JavaFileObjects.forSourceString(
            "com.bluelinelabs.conductor.ControllerFactory", """
            package com.bluelinelabs.conductor;
            
            public class ControllerFactory {

                public static Class<? extends Controller> loadControllerClass(ClassLoader classLoader, String className) {
                    return null;
                }
                  
                public Controller newInstance(ClassLoader classLoader, String className, Object args) {
                    return null;
                }
            }
        """.trimIndent()
        )
    }

    @Test
    fun simpleNoFactory() {
        val controller = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(Long foo, @ControllerBundle ControllerArgs arguments) {
                    super(arguments);
                }
                
                @InjectController.Factory
                public interface Factory {
                    TestController newInstance(ControllerArgs arguments);
                }
            }
        """.trimIndent()
        )

        val controllerFactoryDelegate = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.Object;
            import java.lang.Override;
            
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController>, TestController.Factory {

                @Override
                public TestController newInstanceWithArguments(final Object instantiationArguments) {
                    return null;
                }
                
                @Override
                public TestController newInstance(ControllerArgs arguments) {
                    return null;
                }
            }
        """
        )

        val module = JavaFileObjects.forSourceString(
            "" +
                    "test.ControllerModule",
            """
                package test;
                
                import com.lalafo.codegen.dagger2.ConductorBindingModule;
                import dagger.Module;
                
                @ConductorBindingModule
                @Module(includes = ControllerModule_ConductorBindingModule.class)
                public abstract class ControllerModule {
                }
            """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.ControllerModule_ConductorBindingModule",
            """
                package test;
                
                import dagger.Binds;
                import dagger.Module;
                import $GENERATED_TYPE;

                @Module
                $GENERATED_ANNOTATION
                public abstract class ControllerModule_ConductorBindingModule {
                    private ControllerModule_ConductorBindingModule() {
                    }
                    
                    @Binds
                    abstract TestController.Factory bind_test_TestController(
                        TestController_ControllerFactoryDelegate factory);
                }
            """
        )

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(
                basicDependencies() + controller + controllerFactoryDelegate + module
            )
            .processedWith(ConductorBindingModuleDagger2Processor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun simpleWithFactory() {
        val controller = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(Long foo, @ControllerBundle ControllerArgs arguments) {
                    super(arguments);
                }
                
                @InjectController.Factory
                public interface Factory {
                    TestController newInstance(ControllerArgs arguments);
                }
            }
        """.trimIndent()
        )

        val controllerFactoryDelegate = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.Object;
            import java.lang.Override;
            
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController>, TestController.Factory {

                @Override
                public TestController newInstanceWithArguments(final Object instantiationArguments) {
                    return null;
                }
                
                @Override
                public TestController newInstance(ControllerArgs arguments) {
                    return null;
                }
            }
        """
        )

        val appControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                @ConductorFactory
                abstract class TestControllerFactory extends ControllerFactory { }
            """.trimIndent()
        )

        val generatedAppControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory_GeneratedConductorFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                public final class TestControllerFactory_GeneratedConductorFactory extends TestControllerFactory {
                }
            """.trimIndent()
        )

        val module = JavaFileObjects.forSourceString(
            "" +
                    "test.ControllerModule",
            """
                package test;
                
                import com.lalafo.codegen.dagger2.ConductorBindingModule;
                import dagger.Module;
                
                @ConductorBindingModule
                @Module(includes = ControllerModule_ConductorBindingModule.class)
                public abstract class ControllerModule {
                }
            """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.ControllerModule_ConductorBindingModule",
            """
                package test;
                
                import dagger.Binds;
                import dagger.Module;
                import $GENERATED_TYPE;

                @Module
                $GENERATED_ANNOTATION
                public abstract class ControllerModule_ConductorBindingModule {
                    private ControllerModule_ConductorBindingModule() {
                    }
                    
                    @Binds
                    abstract TestControllerFactory bind_test_TestControllerFactory(
                        TestControllerFactory_GeneratedConductorFactory factory);
                    
                    @Binds
                    abstract TestController.Factory bind_test_TestController(
                        TestController_ControllerFactoryDelegate factory);
                }
            """
        )

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(
                basicDependencies() +
                        controller + controllerFactoryDelegate +
                        appControllerFactory + generatedAppControllerFactory +
                        module
            )
            .processedWith(ConductorBindingModuleDagger2Processor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun simpleFactoryOnly() {
        val controller = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(Long foo, @ControllerBundle ControllerArgs arguments) {
                    super(arguments);
                }
            }
        """.trimIndent()
        )

        val controllerFactoryDelegate = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.Object;
            import java.lang.Override;
            
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController> {

                @Override
                public TestController newInstanceWithArguments(final Object instantiationArguments) {
                    return null;
                }
            }
        """
        )

        val appControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                @ConductorFactory
                abstract class TestControllerFactory extends ControllerFactory { }
            """.trimIndent()
        )

        val generatedAppControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory_GeneratedConductorFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                public final class TestControllerFactory_GeneratedConductorFactory extends TestControllerFactory {
                }
            """.trimIndent()
        )

        val module = JavaFileObjects.forSourceString(
            "" +
                    "test.ControllerModule",
            """
                package test;
                
                import com.lalafo.codegen.dagger2.ConductorBindingModule;
                import dagger.Module;
                
                @ConductorBindingModule
                @Module(includes = ControllerModule_ConductorBindingModule.class)
                public abstract class ControllerModule {
                }
            """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.ControllerModule_ConductorBindingModule",
            """
                package test;
                
                import dagger.Binds;
                import dagger.Module;
                import $GENERATED_TYPE;

                @Module
                $GENERATED_ANNOTATION
                public abstract class ControllerModule_ConductorBindingModule {
                    private ControllerModule_ConductorBindingModule() {
                    }
                    
                    @Binds
                    abstract TestControllerFactory bind_test_TestControllerFactory(
                        TestControllerFactory_GeneratedConductorFactory factory);
                }
            """
        )

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(
                basicDependencies() +
                        controller + controllerFactoryDelegate +
                        appControllerFactory + generatedAppControllerFactory +
                        module
            )
            .processedWith(ConductorBindingModuleDagger2Processor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun emptyModule() {
        val module = JavaFileObjects.forSourceString(
            "" +
                    "test.ControllerModule",
            """
                package test;
                
                import com.lalafo.codegen.dagger2.ConductorBindingModule;
                import dagger.Module;
                
                @ConductorBindingModule
                @Module(includes = ControllerModule_ConductorBindingModule.class)
                public abstract class ControllerModule {
                }
            """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.ControllerModule_ConductorBindingModule",
            """
                package test;

                import dagger.Module;
                import $GENERATED_TYPE;

                @Module
                $GENERATED_ANNOTATION
                public abstract class ControllerModule_ConductorBindingModule {
                    private ControllerModule_ConductorBindingModule() {
                    }
                }
            """
        )

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(basicDependencies() + module)
            .processedWith(ConductorBindingModuleDagger2Processor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun nonPublicEmptyModule() {
        val module = JavaFileObjects.forSourceString(
            "" +
                    "test.ControllerModule",
            """
                package test;
                
                import com.lalafo.codegen.dagger2.ConductorBindingModule;
                import dagger.Module;
                
                @ConductorBindingModule
                @Module(includes = ControllerModule_ConductorBindingModule.class)
                abstract class ControllerModule {
                }
            """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.ControllerModule_ConductorBindingModule",
            """
                package test;

                import dagger.Module;
                import $GENERATED_TYPE;

                @Module
                $GENERATED_ANNOTATION
                abstract class ControllerModule_ConductorBindingModule {
                    private ControllerModule_ConductorBindingModule() {
                    }
                }
            """
        )

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(basicDependencies() + module)
            .processedWith(ConductorBindingModuleDagger2Processor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun noModuleAnnotationFails() {
        val module = JavaFileObjects.forSourceString(
            "" +
                    "test.ControllerModule",
            """
                package test;
                
                import com.lalafo.codegen.dagger2.ConductorBindingModule;
                
                @ConductorBindingModule
                public abstract class ControllerModule {
                }
            """.trimIndent()
        )

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(basicDependencies() + module)
            .processedWith(ConductorBindingModuleDagger2Processor())
            .failsToCompile()
            .withErrorContaining("@ConductorBindingModule must also be annotated as a Dagger @Module")
            .`in`(module).onLine(6)
    }

    @Test
    fun moduleNoIncludesElement() {
        val module = JavaFileObjects.forSourceString(
            "" +
                    "test.ControllerModule",
            """
                package test;
                
                import com.lalafo.codegen.dagger2.ConductorBindingModule;
                import dagger.Module;
                
                @ConductorBindingModule
                @Module
                public abstract class ControllerModule {
                }
            """.trimIndent()
        )

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(basicDependencies() + module)
            .processedWith(ConductorBindingModuleDagger2Processor())
            .failsToCompile()
            .withErrorContaining("@ConductorBindingModule's @Module must include ControllerModule_ConductorBindingModule")
            .`in`(module).onLine(8)
    }

    private fun basicDependencies() = listOf(controllerFile, controllerArgsFile, controllerFactoryFile)

    @Test
    fun twoModuleFails() {
        val module = JavaFileObjects.forSourceString(
            "" +
                    "test.ControllerModule",
            """
                package test;
                
                import com.lalafo.codegen.dagger2.ConductorBindingModule;
                import dagger.Module;
                
                @ConductorBindingModule
                @Module(includes = ControllerModule_ConductorBindingModule.class)
                abstract class ControllerModule {
                }
            """.trimIndent()
        )

        val module2 = JavaFileObjects.forSourceString(
            "" +
                    "test.ControllerModule2",
            """
                package test;
                
                import com.lalafo.codegen.dagger2.ConductorBindingModule;
                import dagger.Module;
                
                @ConductorBindingModule
                @Module(includes = ControllerModule2_ConductorBindingModule.class)
                abstract class ControllerModule2 {
                }
            """.trimIndent()
        )

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(basicDependencies() + module + module2)
            .processedWith(ConductorBindingModuleDagger2Processor())
            .failsToCompile()
            .apply {
                withErrorContaining("Multiple @ConductorBindingModule-annotated modules found.")
                    .`in`(module).onLine(8)

                withErrorContaining("Multiple @ConductorBindingModule-annotated modules found.")
                    .`in`(module2).onLine(8)
            }
    }

}