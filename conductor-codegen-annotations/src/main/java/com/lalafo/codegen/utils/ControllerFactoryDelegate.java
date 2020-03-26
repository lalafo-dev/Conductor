/*
 * Copyright (c) 2020 Lalafo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package  com.lalafo.codegen.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This interface is used for implementing ControllerFactory code generation with factory delegation.
 * This allowing us to optimize code generation process within incremental annotation processing environment.
 * <p>
 * Each class annotated with {@link com.lalafo.codegen.injection.InjectController} under the hood
 * generates the {@code <YourControllerClassName>_ControllerFactoryDelegate}, that implements
 * {@link ControllerFactoryDelegate ControllerFactoryDelegate&#60;YourControllerClassName&#62;}.
 * <p>
 * Implemented delegates are injected as providers to master {@code ControllerFactory}, annotated with
 * {@link com.lalafo.codegen.factory.ConductorFactory}. As soon as master ConductorFactory finds
 * the matching class to generate it calls {@link ControllerFactoryDelegate#newInstanceWithArguments(Object)}
 * with instantiation arguments.
 * <p>
 * <b>Keep in mind</b>: this interface is <b>not</b> for in project usage outside of generated code.
 * No illegal usage of this will be reported in any form during the compilation process.
 *
 * @param <T> Controller class to instantiate withing the delegate.
 *
 * @author Artyom Dorosh [<a href="mailto:artyom.dorosh@outlook.com">artyom.dorosh@outlook.com</a>]
 * @since 0.1.0
 * @version 1.0
 *
 * @see com.lalafo.codegen.injection.InjectController
 * @see com.lalafo.codegen.factory.ConductorFactory
 */
public interface ControllerFactoryDelegate<T> {

  /**
   * Instantiate new {@code Controller} object with given instantiation params.
   *
   * @param instantiationArguments params to instantiate the new controller with.
   * @return new instance of controller type of T
   */
  @NonNull
  T newInstanceWithArguments(@Nullable Object instantiationArguments);
}