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
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Controller.LifecycleListener;

public class ControllerLifecycleOwner implements LifecycleOwner {

    private final LifecycleRegistry lifecycleRegistry;

    public <T extends Controller & LifecycleOwner> ControllerLifecycleOwner(@NonNull T lifecycleController) {
        lifecycleRegistry = new LifecycleRegistry(lifecycleController); // --> State.INITIALIZED

        lifecycleController.addLifecycleListener(new LifecycleListener() {
            @Override
            public void postContextAvailable(@NonNull Controller controller, @NonNull Context context) {
                lifecycleRegistry.handleLifecycleEvent(Event.ON_CREATE); // --> State.CREATED;
            }

            @Override
            public void postCreateView(@NonNull Controller controller, @NonNull View view) {
                lifecycleRegistry.handleLifecycleEvent(Event.ON_START); // --> State.STARTED;
            }

            @Override
            public void postAttach(@NonNull Controller controller, @NonNull View view) {
                lifecycleRegistry.handleLifecycleEvent(Event.ON_RESUME); // --> State.RESUMED;
            }

            @Override
            public void preDetach(@NonNull Controller controller, @NonNull View view) {
                lifecycleRegistry.handleLifecycleEvent(Event.ON_PAUSE); // --> State.STARTED;
            }

            @Override
            public void preDestroyView(@NonNull Controller controller, @NonNull View view) {
                lifecycleRegistry.handleLifecycleEvent(Event.ON_STOP); // --> State.CREATED;
            }

            @Override
            public void preContextUnavailable(@NonNull Controller controller, @NonNull Context context) {
                // do nothing
            }

            @Override
            public void preDestroy(@NonNull Controller controller) {
                lifecycleRegistry.handleLifecycleEvent(Event.ON_DESTROY); // --> State.DESTROYED;
            }

        });
    }

    @Override @NonNull
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

}
