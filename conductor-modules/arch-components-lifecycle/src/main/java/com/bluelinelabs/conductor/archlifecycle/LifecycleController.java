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

package com.bluelinelabs.conductor.archlifecycle;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerArgs;

public abstract class LifecycleController extends Controller implements LifecycleOwner {

    private final ControllerLifecycleOwner lifecycleOwner = new ControllerLifecycleOwner(this);

    public LifecycleController() {
        super();
    }

    public LifecycleController(@Nullable Bundle args) {
        super(args);
    }

    public LifecycleController(@Nullable ControllerArgs args) {
        super(args);
    }

    @Override @NonNull
    public Lifecycle getLifecycle() {
        return lifecycleOwner.getLifecycle();
    }

}
