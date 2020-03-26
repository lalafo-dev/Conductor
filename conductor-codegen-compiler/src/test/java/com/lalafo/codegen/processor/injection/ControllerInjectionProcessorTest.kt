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

package com.lalafo.codegen.processor.injection

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
    value = "ControllerInjectionProcessor",
    comments = "https://yallaclassifieds.atlassian.net/browse/DV-5886"
)
"""

class ControllerInjectionProcessorTest {

    private lateinit var controllerFile: JavaFileObject
    private lateinit var controllerArgsFile: JavaFileObject

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
    }

    @Test
    fun simple() {
        val input = JavaFileObjects.forSourceString(
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

        val expected = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.IllegalArgumentException;
            import java.lang.Long;
            import java.lang.Object;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;
            
            $GENERATED_ANNOTATION
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController> {
            
                @NonNull
                private final Provider<Long> foo;
                
                @Inject
                public TestController_ControllerFactoryDelegate(@NonNull final Provider<Long> foo) {
                    this.foo = foo;
                }
                
                @NonNull
                @Override
                public TestController newInstanceWithArguments(@Nullable final Object instantiationArguments) {
                    if (instantiationArguments instanceof ControllerArgs) {
                        ControllerArgs arguments = (ControllerArgs) instantiationArguments;
                        return new TestController(foo.get(), arguments);
                    } else {
                        throw new IllegalArgumentException("Expected " + ControllerArgs.class.getName() + ", but got '" + instantiationArguments + "' instead.");
                    }
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun customFactory() {
        val input = JavaFileObjects.forSourceString(
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

        val expected = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.IllegalArgumentException;
            import java.lang.Long;
            import java.lang.Object;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;
            
            $GENERATED_ANNOTATION
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController>, TestController.Factory {
            
                @NonNull
                private final Provider<Long> foo;
                
                @Inject
                public TestController_ControllerFactoryDelegate(@NonNull final Provider<Long> foo) {
                    this.foo = foo;
                }
                
                @NonNull
                @Override
                public TestController newInstanceWithArguments(@Nullable final Object instantiationArguments) {
                    if (instantiationArguments instanceof ControllerArgs) {
                        ControllerArgs arguments = (ControllerArgs) instantiationArguments;
                        return new TestController(foo.get(), arguments);
                    } else {
                        throw new IllegalArgumentException("Expected " + ControllerArgs.class.getName() + ", but got '" + instantiationArguments + "' instead.");
                    }
                }
                
                @NonNull
                @Override
                public TestController newInstance(@NonNull final ControllerArgs arguments) {
                    return new TestController(foo.get(), arguments);
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun provider() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            import javax.inject.Provider;

            
            class TestController extends Controller {
                @InjectController
                TestController(Provider<Long> foo, @ControllerBundle ControllerArgs arguments) {
                    super(arguments);
                }
            }
        """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.IllegalArgumentException;
            import java.lang.Long;
            import java.lang.Object;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;
            
            $GENERATED_ANNOTATION
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController> {
            
                @NonNull
                private final Provider<Long> foo;
                
                @Inject
                public TestController_ControllerFactoryDelegate(@NonNull final Provider<Long> foo) {
                    this.foo = foo;
                }
                
                @NonNull
                @Override
                public TestController newInstanceWithArguments(@Nullable final Object instantiationArguments) {
                    if (instantiationArguments instanceof ControllerArgs) {
                        ControllerArgs arguments = (ControllerArgs) instantiationArguments;
                        return new TestController(foo, arguments);
                    } else {
                        throw new IllegalArgumentException("Expected " + ControllerArgs.class.getName() + ", but got '" + instantiationArguments + "' instead.");
                    }
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun primitive() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(long foo, @ControllerBundle ControllerArgs arguments) {
                    super(arguments);
                }
            }
        """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.IllegalArgumentException;
            import java.lang.Long;
            import java.lang.Object;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;
            
            $GENERATED_ANNOTATION
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController> {
            
                @NonNull
                private final Provider<Long> foo;
                
                @Inject
                public TestController_ControllerFactoryDelegate(@NonNull final Provider<Long> foo) {
                    this.foo = foo;
                }
                
                @NonNull
                @Override
                public TestController newInstanceWithArguments(@Nullable final Object instantiationArguments) {
                    if (instantiationArguments instanceof ControllerArgs) {
                        ControllerArgs arguments = (ControllerArgs) instantiationArguments;
                        return new TestController(foo.get(), arguments);
                    } else {
                        throw new IllegalArgumentException("Expected " + ControllerArgs.class.getName() + ", but got '" + instantiationArguments + "' instead.");
                    }
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun simpleWithoutControllerBundleAnnotation() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            
            class TestController extends Controller {
                @InjectController
                TestController(Long foo, ControllerArgs arguments) {
                    super(arguments);
                }
            }
        """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.IllegalArgumentException;
            import java.lang.Long;
            import java.lang.Object;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;
            
            $GENERATED_ANNOTATION
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController> {
            
                @NonNull
                private final Provider<Long> foo;
                
                @Inject
                public TestController_ControllerFactoryDelegate(@NonNull final Provider<Long> foo) {
                    this.foo = foo;
                }
                
                @NonNull
                @Override
                public TestController newInstanceWithArguments(@Nullable final Object instantiationArguments) {
                    if (instantiationArguments instanceof ControllerArgs) {
                        ControllerArgs arguments = (ControllerArgs) instantiationArguments;
                        return new TestController(foo.get(), arguments);
                    } else {
                        throw new IllegalArgumentException("Expected " + ControllerArgs.class.getName() + ", but got '" + instantiationArguments + "' instead.");
                    }
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
            .withWarningContaining("Controller bundle argument 'arguments' is not annotated with @ControllerBundle")
    }

    @Test
    fun withoutControllerBundle() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.lalafo.codegen.injection.InjectController;
            
            class TestController extends Controller {
                @InjectController
                TestController(Long foo) {
                    super(null);
                }
            }
        """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.Long;
            import java.lang.Object;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;
            
            $GENERATED_ANNOTATION
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController> {
            
                @NonNull
                private final Provider<Long> foo;
                
                @Inject
                public TestController_ControllerFactoryDelegate(@NonNull final Provider<Long> foo) {
                    this.foo = foo;
                }
                
                @NonNull
                @Override
                public TestController newInstanceWithArguments(@Nullable final Object instantiationArguments) {
                    return new TestController(foo.get());
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun withoutInjectedDependencies() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs arguments) {
                    super(arguments);
                }
            }
        """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.IllegalArgumentException;
            import java.lang.Object;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            
            $GENERATED_ANNOTATION
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController> {
                
                @Inject
                public TestController_ControllerFactoryDelegate() {
                }
                
                @NonNull
                @Override
                public TestController newInstanceWithArguments(@Nullable final Object instantiationArguments) {
                    if (instantiationArguments instanceof ControllerArgs) {
                        ControllerArgs arguments = (ControllerArgs) instantiationArguments;
                        return new TestController(arguments);
                    } else {
                        throw new IllegalArgumentException("Expected " + ControllerArgs.class.getName() + ", but got '" + instantiationArguments + "' instead.");
                    }
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun customFactoryWithInheritance() {
        val input = JavaFileObjects.forSourceString(
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
                public interface Factory extends GenericFactoryInterface<TestController, ControllerArgs> {}
                
                public interface GenericFactoryInterface<C, B> {
                    C newInstance(B arguments);
                }
            }
        """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.IllegalArgumentException;
            import java.lang.Long;
            import java.lang.Object;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;
            
            $GENERATED_ANNOTATION
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController>, TestController.Factory {
            
                @NonNull
                private final Provider<Long> foo;
                
                @Inject
                public TestController_ControllerFactoryDelegate(@NonNull final Provider<Long> foo) {
                    this.foo = foo;
                }
                
                @NonNull
                @Override
                public TestController newInstanceWithArguments(@Nullable final Object instantiationArguments) {
                    if (instantiationArguments instanceof ControllerArgs) {
                        ControllerArgs arguments = (ControllerArgs) instantiationArguments;
                        return new TestController(foo.get(), arguments);
                    } else {
                        throw new IllegalArgumentException("Expected " + ControllerArgs.class.getName() + ", but got '" + instantiationArguments + "' instead.");
                    }
                }
                
                @NonNull
                @Override
                public TestController newInstance(@NonNull final ControllerArgs arguments) {
                    return new TestController(foo.get(), arguments);
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun customFactoryWithPrivateInheritance() {
        val input = JavaFileObjects.forSourceString(
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
                public interface Factory extends GenericFactoryInterface<TestController, ControllerArgs> {}
                
                private interface GenericFactoryInterface<C, B> {
                    C newInstance(B arguments);
                }
            }
        """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.IllegalArgumentException;
            import java.lang.Long;
            import java.lang.Object;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;
            
            $GENERATED_ANNOTATION
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController>, TestController.Factory {
            
                @NonNull
                private final Provider<Long> foo;
                
                @Inject
                public TestController_ControllerFactoryDelegate(@NonNull final Provider<Long> foo) {
                    this.foo = foo;
                }
                
                @NonNull
                @Override
                public TestController newInstanceWithArguments(@Nullable final Object instantiationArguments) {
                    if (instantiationArguments instanceof ControllerArgs) {
                        ControllerArgs arguments = (ControllerArgs) instantiationArguments;
                        return new TestController(foo.get(), arguments);
                    } else {
                        throw new IllegalArgumentException("Expected " + ControllerArgs.class.getName() + ", but got '" + instantiationArguments + "' instead.");
                    }
                }
                
                @NonNull
                @Override
                public TestController newInstance(@NonNull final ControllerArgs arguments) {
                    return new TestController(foo.get(), arguments);
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun twoBundleFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs bundle, @ControllerBundle ControllerArgs arguments) {
                    super(arguments);
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("Found more than one controller bundle arguments at @InjectController-annotated constructor")
            .`in`(input).onLine(10)
    }

    @Test
    fun forbiddenNameFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs instantiationArguments) {
                    super(arguments);
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("\"instantiationArguments\" argument name for @InjectController-annotated controller is forbidden, try another one.")
            .`in`(input).onLine(10)
    }

    @Test
    fun multipleConstructorsFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs args) {
                    super(args);
                }
                
                 @InjectController
                 TestController() {
                     super(null);
                 }
                
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("Multiple @InjectController constructs found.")
            .`in`(input).onLine(8)
    }

    @Test
    fun customFactoryWithNoConstructorFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                TestController(ControllerArgs args) {
                    super(args);
                }
                
                @InjectController.Factory
                public interface Factory {
                    TestController newInstance(ControllerArgs args);
                }
                
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("Controller injection requires an @InjectController-annotated constructor.")
            .`in`(input).onLine(8)
    }

    @Test
    fun privateCustomFactoryFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs args) {
                    super(args);
                }
                
                @InjectController.Factory
                private interface Factory {
                    TestController newInstance(ControllerArgs args);
                }
                
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("@InjectController.Factory must be not private.")
            .`in`(input).onLine(15)
    }

    @Test
    fun nonInterfaceCustomFactoryFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs args) {
                    super(args);
                }
                
                @InjectController.Factory
                public abstract class Factory {
                    TestController newInstance(ControllerArgs args);
                }
                
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("@InjectController.Factory must be an interface.")
            .`in`(input).onLine(15)
    }

    @Test
    fun multipleCustomFactoriesFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs args) {
                    super(args);
                }
                
                @InjectController.Factory
                public interface Factory {
                    TestController newInstance(ControllerArgs args);
                }
                
                @InjectController.Factory
                public interface Factory2 {
                    TestController newInstance(ControllerArgs args);
                }
                
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("Multiple controller factories annotated with @InjectController.Factory found.")
            .`in`(input).onLine(8)
    }

    @Test
    fun noMethodFactoryFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs args) {
                    super(args);
                }
                
                @InjectController.Factory
                public interface Factory {
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("Factory interface does not define a factory method.")
            .`in`(input).onLine(15)
    }

    @Test
    fun tooMuchMethodsCustomFactoryFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs args) {
                    super(args);
                }
                
                @InjectController.Factory
                public interface Factory {
                    TestController newInstance(ControllerArgs args);
                    TestController newInstance2(ControllerArgs args);
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("Factory interface defines multiple factory methods.")
            .`in`(input).onLine(15)
    }

    @Test
    fun forbiddenNameOnCustomFactoryFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs args) {
                    super(args);
                }
                
                @InjectController.Factory
                public interface Factory {
                    TestController newInstanceWithArguments(ControllerArgs args);
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining(
                "\"newInstanceWithArguments\" method name for @InjectController.Factory-annotated " +
                        "interface is forbidden, try another name."
            )
            .`in`(input).onLine(16)
    }

    @Test
    fun differentNamingInConstructorAndFactoryFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs args) {
                    super(args);
                }
                
                @InjectController.Factory
                public interface Factory {
                    TestController newInstance(ControllerArgs arguments);
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining(
                """
          Factory method parameters do not match constructor @ControllerBundle parameters. Both parameter type and name must match.
            Declared by constructor, unmatched in factory method:
             * com.bluelinelabs.conductor.ControllerArgs args
            Declared by factory method, unmatched in constructor:
             * com.bluelinelabs.conductor.ControllerArgs arguments
        """.trimIndent()
            )
            .`in`(input).onLine(16)
    }

    @Test
    fun differentTypesInConstructorAndFactoryFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle ControllerArgs args) {
                    super(args);
                }
                
                @InjectController.Factory
                public interface Factory {
                    TestController newInstance(String args);
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining(
                """
          Factory method parameters do not match constructor @ControllerBundle parameters. Both parameter type and name must match.
            Declared by constructor, unmatched in factory method:
             * com.bluelinelabs.conductor.ControllerArgs args
            Declared by factory method, unmatched in constructor:
             * java.lang.String args
        """.trimIndent()
            )
            .`in`(input).onLine(16)
    }

    @Test
    fun nonControllerUsageFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController {
                @InjectController
                TestController(@ControllerBundle ControllerArgs args) {
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("Using @InjectController annotation on non Controller class.")
            .`in`(input).onLine(9)
    }

    @Test
    fun innerControllerFails() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController {
                class Test extends Controller {
                    @InjectController
                    Test(@ControllerBundle ControllerArgs args) {
                        super(args);
                    }
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("Nested @InjectController-using types must be static")
            .`in`(input).onLine(9)
    }

    @Test
    fun staticInnerControllerCompiles() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            public class TestController {
                public static class Test extends Controller {
                    @InjectController
                    Test(Long foo, @ControllerBundle ControllerArgs args) {
                        super(args);
                    }
                }
            }
        """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.TestController${'$'}Test_ControllerFactoryDelegate", """
            package test;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.IllegalArgumentException;
            import java.lang.Long;
            import java.lang.Object;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;
            
            $GENERATED_ANNOTATION
            public final class TestController${'$'}Test_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController.Test> {
            
                @NonNull
                private final Provider<Long> foo;
                
                @Inject
                public TestController${'$'}Test_ControllerFactoryDelegate(@NonNull final Provider<Long> foo) {
                    this.foo = foo;
                }
                
                @NonNull
                @Override
                public TestController.Test newInstanceWithArguments(@Nullable final Object instantiationArguments) {
                    if (instantiationArguments instanceof ControllerArgs) {
                        ControllerArgs args = (ControllerArgs) instantiationArguments;
                        return new TestController.Test(foo.get(), args);
                    } else {
                        throw new IllegalArgumentException("Expected " + ControllerArgs.class.getName() + ", but got '" + instantiationArguments + "' instead.");
                    }
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }

    @Test
    fun injectedTypeAsBundle() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            
            class TestController extends Controller {
                @InjectController
                TestController(@ControllerBundle Long foo, @ControllerBundle ControllerArgs arguments) {
                    super(arguments);
                }
            }
        """.trimIndent()
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .failsToCompile()
            .withErrorContaining("Using @ControllerBundle annotation on injection provided type.")
            .`in`(input).onLine(10)
    }

    @Test fun qualifiedInjectCompiles() {
        val input = JavaFileObjects.forSourceString(
            "test.TestController", """
            package test;
            
            import com.bluelinelabs.conductor.Controller;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.injection.InjectController;
            import com.lalafo.codegen.injection.ControllerBundle;
            import javax.inject.Qualifier;
            
            class TestController extends Controller {
                @InjectController
                TestController(@Id Long foo, @ControllerBundle ControllerArgs arguments) {
                    super(arguments);
                }
            }
            
            @Qualifier
            @interface Id {}
        """.trimIndent()
        )

        val expected = JavaFileObjects.forSourceString(
            "test.TestController_ControllerFactoryDelegate", """
            package test;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import com.bluelinelabs.conductor.ControllerArgs;
            import com.lalafo.codegen.utils.ControllerFactoryDelegate;
            import java.lang.IllegalArgumentException;
            import java.lang.Long;
            import java.lang.Object;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;
            
            $GENERATED_ANNOTATION
            public final class TestController_ControllerFactoryDelegate implements ControllerFactoryDelegate<TestController> {
            
                @NonNull
                private final Provider<Long> foo;
                
                @Inject
                public TestController_ControllerFactoryDelegate(@NonNull @Id final Provider<Long> foo) {
                    this.foo = foo;
                }
                
                @NonNull
                @Override
                public TestController newInstanceWithArguments(@Nullable final Object instantiationArguments) {
                    if (instantiationArguments instanceof ControllerArgs) {
                        ControllerArgs arguments = (ControllerArgs) instantiationArguments;
                        return new TestController(foo.get(), arguments);
                    } else {
                        throw new IllegalArgumentException("Expected " + ControllerArgs.class.getName() + ", but got '" + instantiationArguments + "' instead.");
                    }
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(controllerFile, controllerArgsFile, input))
            .processedWith(ControllerInjectionProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expected)
    }
}