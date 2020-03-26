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

package com.lalafo.codegen.factory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation used to automatically generate master {@code ControllerFactory} in project.
 * <p>
 * Only one or less {@code @ControllerFactory}-annotated class are allowed to declare in project.
 * Annotated class must be public and non-final. It also must contain one non-private empty constructor.
 * If you break any of these limitations, the processing tool will abort the compilation.
 * <p>
 * The generated class is injectable for JSR-330 DI and could accessed with binding inside via
 * {@link com.lalafo.codegen.dagger2.ConductorBindingModule @ConductorBindingModule}-annotated module.
 * <p>
 * The annotation processing tool searches for any {@code Controller} annotated constructor within the project
 * and injects {@link com.lalafo.codegen.utils.ControllerFactoryDelegate ControllerFactoryDelegate}s inside the
 * implementation to delegate the Controller instantiation. This allows to support both effective incremental
 * annotation processing and keep generated {@code ControllerFactory} small and easy to read.
 *
 * @author Artyom Dorosh [<a href="mailto:artyom.dorosh@outlook.com">artyom.dorosh@outlook.com</a>]
 * @since 0.1.0
 * @version 1.0
 *
 * @see com.lalafo.codegen.injection.InjectController
 * @see com.lalafo.codegen.dagger2.ConductorBindingModule
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ConductorFactory {}
