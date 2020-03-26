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

package com.bluelinelabs.conductor.internal;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ClassUtils {

    @Deprecated
    @Nullable @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> classForName(@NonNull String className, boolean allowEmptyName) {
        if (allowEmptyName && TextUtils.isEmpty(className)) {
            return null;
        }

        try {
            return (Class<? extends T>) Class.forName(className);
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred while finding class for name " + className + ". " + e.getMessage());
        }
    }

    @Nullable @SuppressWarnings("unchecked")
    public static <T> T newInstance(@NonNull String className) {
        try {
            Class<? extends T> cls = classForName(className, true);
            return cls != null ? cls.newInstance() : null;
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred while creating a new instance of " + className + ". " + e.getMessage());
        }
    }

}
