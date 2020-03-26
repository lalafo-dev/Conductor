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

package com.lalafo.codegen.processor.factory

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import javax.lang.model.element.TypeElement

private val JAVAX_PROVIDER = ClassName.get("javax.inject", "Provider")
private val CONTROLLER_FACTORY_DELEGATE=  ClassName.get("com.lalafo.codegen.utils", "ControllerFactoryDelegate")

internal data class FactoryDelegateProvider(
    val targetElement: TypeElement,
    val targetType: ClassName,
    val bindingType: ClassName
) {

    val propertyName get() = targetType.reflectionName().replace('.', '_')
    val factoryDelegateType get() = ParameterizedTypeName.get(CONTROLLER_FACTORY_DELEGATE, targetType)
}