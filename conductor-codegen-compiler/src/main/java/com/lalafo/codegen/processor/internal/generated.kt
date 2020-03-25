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