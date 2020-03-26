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
 * {@link ControllerBundle @ControllerBundle} is a helper annotation for code readability.
 * Each ControllerBundle annotated constructor parameter must implement {@code ControllerArgs} in order
 * to be recognized.
 * <p>
 * This Conductor code generation tool doesn't require you to annotate bundle with this particular annotation.
 * But the tool welcomes you to annotate each. By annotating you enhance the code transparency and readability for
 * your teammates.
 * <p>
 * If you supply a {@code ControllerArgs}-implementing param to your Controller constructor, the
 * annotation processing tool will warn you with message, that you forgot to annotate your controller
 * argument with {@code @ControllerBundle} annotation. Breaking this rule won't abort your build
 * or code generation.
 *
 * @author Artyom Dorosh [<a href="mailto:artyom.dorosh@outlook.com">artyom.dorosh@outlook.com</a>]
 * @since 0.1.0
 * @version 1.0
 *
 * @see InjectController
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.CLASS)
public @interface ControllerBundle {}
