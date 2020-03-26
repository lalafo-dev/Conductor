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

package com.lalafo.codegen.processor.internal

import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.util.Elements

fun Processor.createGeneratedAnnotation(
    sourceVersion: SourceVersion,
    elements: Elements
): com.squareup.javapoet.AnnotationSpec? {
    val annotationTypeName = when {
        sourceVersion <= SourceVersion.RELEASE_8 -> "javax.annotation.Generated"
        else -> "javax.annotation.processing.Generated"
    }
    val generatedType = elements.getTypeElement(annotationTypeName) ?: return null
    return com.squareup.javapoet.AnnotationSpec.builder(generatedType.toClassName())
        .addMember("value", "\$S", javaClass.name)
        .addMember("comments", "\$S", "https://yallaclassifieds.atlassian.net/browse/DV-5886")
        .build()
}