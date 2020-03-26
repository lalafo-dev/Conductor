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

package com.bluelinelabs.conductor.lint;

import com.android.SdkConstants;
import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UParameter;
import org.jetbrains.uast.UTypeReferenceExpression;

import java.util.Collections;
import java.util.List;

public final class ControllerIssueDetector extends Detector implements Detector.UastScanner {

    public static final Issue ISSUE =
            Issue.create("ValidController", "Controller not instantiatable",
                    "Non-abstract Controller instances must have a default or single-argument constructor"
                            + " that takes a Bundle in order for the system to re-create them in the"
                            + " case of the process being killed.", Category.CORRECTNESS, 6, Severity.FATAL,
                    new Implementation(ControllerIssueDetector.class, Scope.JAVA_FILE_SCOPE));

    private static final String CLASS_NAME = "com.bluelinelabs.conductor.Controller";

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.<Class<? extends UElement>>singletonList(UClass.class);
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        final JavaEvaluator evaluator = context.getEvaluator();

        return new UElementHandler() {
            @Override
            public void visitClass(UClass node) {
                if (evaluator.isAbstract(node)) {
                    return;
                }

                boolean hasSuperType = false;
                for (UTypeReferenceExpression superType : node.getUastSuperTypes()) {
                    if (CLASS_NAME.equals(superType.asRenderString())) {
                        hasSuperType = true;
                        break;
                    }
                }
                if (!hasSuperType) {
                    return;
                }

                if (!evaluator.isPublic(node)) {
                    String message = String.format("This Controller class should be public (%1$s)", node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation((UElement) node), message);
                    return;
                }

                if (node.getContainingClass() != null && !evaluator.isStatic(node)) {
                    String message = String.format("This Controller inner class should be static (%1$s)", node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation((UElement) node), message);
                    return;
                }

                boolean hasConstructor = false;
                boolean hasDefaultConstructor = false;
                boolean hasBundleConstructor = false;
                for (UMethod method : node.getMethods()) {
                    if (method.isConstructor()) {
                        hasConstructor = true;
                        if (evaluator.isPublic(method)) {
                            List<UParameter> parameters = method.getUastParameters();
                            if (parameters.size() == 0) {
                                hasDefaultConstructor = true;
                                break;
                            } else if (parameters.size() == 1 &&
                                    (parameters.get(0).getType().equalsToText(SdkConstants.CLASS_BUNDLE)) ||
                                    parameters.get(0).getType().equalsToText("Bundle")) {
                                hasBundleConstructor = true;
                            }
                        }
                    }
                }

                if (hasConstructor && !hasDefaultConstructor && !hasBundleConstructor) {
                    String message = String.format(
                            "This Controller needs to have either a public default constructor or a" +
                                    " public single-argument constructor that takes a Bundle. (`%1$s`)",
                            node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation((UElement) node), message);
                }
            }
        };
    }
    
}
