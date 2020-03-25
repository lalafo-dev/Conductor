package com.lalafo.codegen.processor.factory

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
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
    value = "ConductorFactoryProcessor",
    comments = "https://yallaclassifieds.atlassian.net/browse/DV-5886"
)
"""

class ConductorFactoryProcessorTest {
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
    fun simple() {
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
                class TestControllerFactory extends ControllerFactory { }
            """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.TestControllerFactory_GeneratedConductorFactory", """
            package test;
            
            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.Controller;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.Class;
            import java.lang.ClassLoader;
            import java.lang.Object;
            import java.lang.Override;
            import java.lang.String;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            
            $GENERATED_ANNOTATION
            public final class TestControllerFactory_GeneratedConductorFactory extends TestControllerFactory {
                @NonNull
                private final ControllerFactoryDelegate<TestController> test_TestController;
                
                @Inject
                public TestControllerFactory_GeneratedConductorFactory(
                    @NonNull final TestController_ControllerFactoryDelegate test_TestController) {
                    super();
                    this.test_TestController = test_TestController;
                }
                
                @NonNull
                @Override
                public Controller newInstance(@NonNull ClassLoader classLoader, @NonNull String className, @Nullable Object args) {
                    Class<? extends Controller> clazz = loadControllerClass(classLoader, className);
                    if (TestController.class == clazz) {
                        return test_TestController.newInstanceWithArguments(args);
                    }
                    // Instantiate controller from reflection with super
                    return super.newInstance(classLoader, className, args);
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(
                listOf(
                    controllerFactoryFile, controllerArgsFile, controllerFile,
                    controller, controllerFactoryDelegate, appControllerFactory
                )
            )
            .processedWith(ConductorFactoryProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun abstractCompiles() {
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

        val expected = JavaFileObjects.forSourceString(
            "test.TestControllerFactory_GeneratedConductorFactory", """
            package test;
            
            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.Controller;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.Class;
            import java.lang.ClassLoader;
            import java.lang.Object;
            import java.lang.Override;
            import java.lang.String;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            
            $GENERATED_ANNOTATION
            public final class TestControllerFactory_GeneratedConductorFactory extends TestControllerFactory {
                @NonNull
                private final ControllerFactoryDelegate<TestController> test_TestController;
                
                @Inject
                public TestControllerFactory_GeneratedConductorFactory(
                    @NonNull final TestController_ControllerFactoryDelegate test_TestController) {
                    super();
                    this.test_TestController = test_TestController;
                }
                
                @NonNull
                @Override
                public Controller newInstance(@NonNull ClassLoader classLoader, @NonNull String className, @Nullable Object args) {
                    Class<? extends Controller> clazz = loadControllerClass(classLoader, className);
                    if (TestController.class == clazz) {
                        return test_TestController.newInstanceWithArguments(args);
                    }
                    // Instantiate controller from reflection with super
                    return super.newInstance(classLoader, className, args);
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(
                listOf(
                    controllerFactoryFile, controllerArgsFile, controllerFile,
                    controller, controllerFactoryDelegate, appControllerFactory
                )
            )
            .processedWith(ConductorFactoryProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun twoControllers() {
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

        val controllerTwo = JavaFileObjects.forSourceString(
            "test.SecondController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class SecondController extends Controller {
                @InjectController
                SecondController(Long foo, @ControllerBundle ControllerArgs arguments) {
                    super(arguments);
                }
            }
        """.trimIndent()
        )

        val controllerFactoryDelegateTwo = JavaFileObjects.forSourceString(
            "test.SecondController_ControllerFactoryDelegate", """
            package test;

            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.Object;
            import java.lang.Override;
            
            public final class SecondController_ControllerFactoryDelegate implements ControllerFactoryDelegate<SecondController> {

                @Override
                public SecondController newInstanceWithArguments(final Object instantiationArguments) {
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
                class TestControllerFactory extends ControllerFactory { }
            """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.TestControllerFactory_GeneratedConductorFactory", """
            package test;
            
            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.Controller;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.Class;
            import java.lang.ClassLoader;
            import java.lang.Object;
            import java.lang.Override;
            import java.lang.String;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            
            $GENERATED_ANNOTATION
            public final class TestControllerFactory_GeneratedConductorFactory extends TestControllerFactory {
                @NonNull
                private final ControllerFactoryDelegate<TestController> test_TestController;
                
                @NonNull
                private final ControllerFactoryDelegate<SecondController> test_SecondController;
                
                @Inject
                public TestControllerFactory_GeneratedConductorFactory(
                    @NonNull final TestController_ControllerFactoryDelegate test_TestController,
                    @NonNull final SecondController_ControllerFactoryDelegate test_SecondController
                ) {
                    super();
                    this.test_TestController = test_TestController;
                    this.test_SecondController = test_SecondController;
                }
                
                @NonNull
                @Override
                public Controller newInstance(@NonNull ClassLoader classLoader, @NonNull String className, @Nullable Object args) {
                    Class<? extends Controller> clazz = loadControllerClass(classLoader, className);
                    if (TestController.class == clazz) {
                        return test_TestController.newInstanceWithArguments(args);
                    } else if (SecondController.class == clazz) {
                        return test_SecondController.newInstanceWithArguments(args);
                    }
                    // Instantiate controller from reflection with super
                    return super.newInstance(classLoader, className, args);
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(
                listOf(
                    controllerFactoryFile, controllerArgsFile, controllerFile,
                    controller, controllerFactoryDelegate,
                    controllerTwo, controllerFactoryDelegateTwo,
                    appControllerFactory
                )
            )
            .processedWith(ConductorFactoryProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun multipleFactoriesFails() {
        val appControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                @ConductorFactory
                class TestControllerFactory extends ControllerFactory { }
            """.trimIndent()
        )

        val secondControllerFactory = JavaFileObjects.forSourceString(
            "test.SecondControllerFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                @ConductorFactory
                class SecondControllerFactory extends ControllerFactory { }
            """.trimIndent()
        )

        assertAbout(javaSources())
            .that(
                listOf(
                    controllerFactoryFile, controllerArgsFile, controllerFile,
                    appControllerFactory, secondControllerFactory
                )
            )
            .processedWith(ConductorFactoryProcessor())
            .failsToCompile()
            .withErrorContaining("Multiple @ConductorFactory-annotated classes found.")
            .`in`(appControllerFactory).onLine(7)
    }

    @Test
    fun privateFactoryConstructorFails() {
        val appControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                @ConductorFactory
                class TestControllerFactory extends ControllerFactory { 
                    private TestControllerFactory() {
                        super();
                    }
                }
            """.trimIndent()
        )

        assertAbout(javaSources())
            .that(
                listOf(
                    controllerFactoryFile, controllerArgsFile, controllerFile,
                    appControllerFactory
                )
            )
            .processedWith(ConductorFactoryProcessor())
            .failsToCompile()
            .withErrorContaining("@ConductorFactory-annotated type should contain a non-private empty constructor.")
            .`in`(appControllerFactory).onLine(7)
    }

    @Test
    fun nonEmptyConstuctorFails() {
        val appControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                @ConductorFactory
                class TestControllerFactory extends ControllerFactory { 
                    TestControllerFactory(String foo) {
                        super();
                    }
                }
            """.trimIndent()
        )

        assertAbout(javaSources())
            .that(
                listOf(
                    controllerFactoryFile, controllerArgsFile, controllerFile,
                    appControllerFactory
                )
            )
            .processedWith(ConductorFactoryProcessor())
            .failsToCompile()
            .withErrorContaining("@ConductorFactory-annotated type should contain a non-private empty constructor.")
            .`in`(appControllerFactory).onLine(7)
    }

    @Test
    fun controllerFactoryWithoutInheritanceFails() {
        val appControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                @ConductorFactory
                class TestControllerFactory { }
            """.trimIndent()
        )

        assertAbout(javaSources())
            .that(
                listOf(
                    controllerFactoryFile, controllerArgsFile, controllerFile,
                    appControllerFactory
                )
            )
            .processedWith(ConductorFactoryProcessor())
            .failsToCompile()
            .withErrorContaining("Using @ConductorFactory annotation on non ControllerFactory class.")
            .`in`(appControllerFactory).onLine(7)
    }

    @Test
    fun finalControllerFactoryFails() {
        val appControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                @ConductorFactory
                final class TestControllerFactory extends ControllerFactory { 
                    TestControllerFactory() {
                        super();
                    }
                }
            """.trimIndent()
        )

        assertAbout(javaSources())
            .that(
                listOf(
                    controllerFactoryFile, controllerArgsFile, controllerFile,
                    appControllerFactory
                )
            )
            .processedWith(ConductorFactoryProcessor())
            .failsToCompile()
            .withErrorContaining("@ConductorFactory-using types must be defined as non-final classes.")
            .`in`(appControllerFactory).onLine(7)
    }

    @Test
    fun abstractFactoryWithMethodFails() {
        val appControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                @ConductorFactory
                abstract class TestControllerFactory extends ControllerFactory { 
                    TestControllerFactory() {
                        super();
                    }
                    
                    abstract String baz();
                }
            """.trimIndent()
        )

        assertAbout(javaSources())
            .that(
                listOf(
                    controllerFactoryFile, controllerArgsFile, controllerFile,
                    appControllerFactory
                )
            )
            .processedWith(ConductorFactoryProcessor())
            .failsToCompile()
            .withErrorContaining("@ConductorFactory-annotated type must not contain any abstract methods.")
            .`in`(appControllerFactory).onLine(12)
    }

    @Test
    fun notStaticNestedFails() {
        val appControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                class Test {
                    @ConductorFactory
                    class TestControllerFactory extends ControllerFactory { 
                        TestControllerFactory() {
                            super();
                        }
                    }
                }
            """.trimIndent()
        )

        assertAbout(javaSources())
            .that(
                listOf(
                    controllerFactoryFile, controllerArgsFile, controllerFile,
                    appControllerFactory
                )
            )
            .processedWith(ConductorFactoryProcessor())
            .failsToCompile()
            .withErrorContaining("Nested @ConductorFactory-using types must be static.")
            .`in`(appControllerFactory).onLine(8)
    }

    @Test
    fun staticPrivateNestedFails() {
        val appControllerFactory = JavaFileObjects.forSourceString(
            "test.TestControllerFactory", """
                package test;
                
                import com.bluelinelabs.conductor.ControllerFactory;
                import com.lalafo.codegen.factory.ConductorFactory;
                
                class Test {
                    @ConductorFactory
                    private static class TestControllerFactory extends ControllerFactory { 
                        TestControllerFactory() {
                            super();
                        }
                    }
                }
            """.trimIndent()
        )

        assertAbout(javaSources())
            .that(
                listOf(
                    controllerFactoryFile, controllerArgsFile, controllerFile,
                    appControllerFactory
                )
            )
            .processedWith(ConductorFactoryProcessor())
            .failsToCompile()
            .withErrorContaining("@ConductorFactory-using types must be not private")
            .`in`(appControllerFactory).onLine(8)
    }
}