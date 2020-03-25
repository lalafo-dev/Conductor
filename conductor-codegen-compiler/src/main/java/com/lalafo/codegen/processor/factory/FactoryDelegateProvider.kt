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