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

package com.lalafo.codegen.injection;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a Controller constructor participating in assisted injection.
 * <p>
 * Injectable Controller constructors are annotated with {@code @InjectController} and accept one more
 * dependencies as arguments. {@code @InjectController} can apply to at most one constructor per
 * class.
 * <p>
 * Arguments can be a mix of <em>bundles</em> or <em>provided</em>. Bundle arguments are those that
 * both extend ControllerArgs and annotated or not with {@link ControllerBundle @ControllerBundle}.
 * It will be supplied by the caller. Provided arguments are all others and will be supplied by your
 * dependency injector. At most one ControllerBundle and one provided argument must be present.
 * <p>
 * Both bundle and provided arguments can have qualifier annotations. Since bundle and provider
 * arguments are resolved separately, the same qualifier can be used for both on a single
 * constructor.
 * <p>
 * Each type with an {@code @InjectController}-annotated constructor can also contain a nested
 * interface annotated with {@link Factory @InjectController.Factory}. The interface must have a
 * single method that returns the enclosing type and arguments which match the controller arguments
 * of the enclosing {@code @InjectController}-annotated constructor.
 * <p>
 * Each generated {@code InjectController.Factory}-annotated interface generated implementation
 * is compatible with JSR-330 DI and could accessed with binding inside via
 * {@link com.lalafo.codegen.dagger2.ConductorBindingModule @ConductorBindingModule}-annotated module.
 * <p>
 * <b>Note</b>: {@link Factory @InjectController.Factory} annotated interface factory name must not be named
 * as {@code T newInstanceWithArguments()}
 * due to {@link com.lalafo.codegen.utils.ControllerFactoryDelegate ControllerFactoryDelegate} limitations.
 * If you do so the annotations processor <b>will error you</b> with contextual message you
 * and <b>abort the build and code generation</b>.
 *
 * @author Artyom Dorosh [<a href="mailto:artyom.dorosh@outlook.com">artyom.dorosh@outlook.com</a>]
 * @since 0.1.0
 * @version 1.0
 *
 * @see com.lalafo.codegen.factory.ConductorFactory
 * @see com.lalafo.codegen.utils.ControllerFactoryDelegate
 */
@Documented
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.CLASS)
public @interface InjectController {

  /**
   * {@inheritDoc}
   * @see InjectController
   */
  @Documented
  @Target(ElementType.TYPE) @Retention(RetentionPolicy.CLASS)
  @interface Factory {}
}
