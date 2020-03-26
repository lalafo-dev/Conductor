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

package com.lalafo.codegen.dagger2;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for generation Dagger2 Conductor generated bindings.
 * <p>
 * Only one or less {@code @ConductorBindingModule}-annotated class are allowed to declare in project.
 * Annotated class should be Dagger 2 {@code @Module} annotation and have generated module class as included
 * <p>
 * Example:
 * <pre>
 * &#64;ConductorBindingModule
 * &#64;Module(includes = YourDaggerModule_ConductorBindingModule.class)
 * public abstract class YourDaggerModule {}
 * </pre>
 *
 * @author Artyom Dorosh [<a href="mailto:artyom.dorosh@outlook.com">artyom.dorosh@outlook.com</a>]
 * @since 0.1.0
 * @version 1.0
 *
 * @see com.lalafo.codegen.injection.InjectController
 * @see com.lalafo.codegen.injection.InjectController.Factory
 * @see com.lalafo.codegen.factory.ConductorFactory
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ConductorBindingModule {}