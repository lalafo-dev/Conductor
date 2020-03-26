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

package com.bluelinelabs.conductor;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.internal.LifecycleHandler;
import com.bluelinelabs.conductor.internal.ThreadUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

/**
 * Point of initial interaction with Conductor. Used to attach a {@link Router} to your Activity.
 */
public final class Conductor {

    @NonNull
    private static ControllerFactory controllerFactory = new ControllerFactory();

    private Conductor() {}

    /**
     * Conductor will create a {@link Router} that has been initialized for your Activity and containing ViewGroup.
     * If an existing {@link Router} is already associated with this Activity/ViewGroup pair, either in memory
     * or in the savedInstanceState, that router will be used and rebound instead of creating a new one with
     * an empty backstack.
     *
     * @param activity The Activity that will host the {@link Router} being attached.
     * @param container The ViewGroup in which the {@link Router}'s {@link Controller} views will be hosted
     * @param savedInstanceState The savedInstanceState passed into the hosting Activity's onCreate method. Used
     *                           for restoring the Router's state if possible.
     * @return A fully configured {@link Router} instance for use with this Activity/ViewGroup pair.
     */
    @NonNull @UiThread
    public static Router attachRouter(@NonNull Activity activity, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        ThreadUtils.ensureMainThread();

        LifecycleHandler lifecycleHandler = LifecycleHandler.install(activity);

        Router router = lifecycleHandler.getRouter(container, savedInstanceState);
        router.rebindIfNeeded();

        return router;
    }

    @NonNull @UiThread
    public static ControllerFactory getControllerFactory() {
        ThreadUtils.ensureMainThread();
        return controllerFactory;
    }

    @UiThread
    public static void setControllerFactory(@NonNull ControllerFactory factory) {
        ThreadUtils.ensureMainThread();

        if (factory == null) {
            return;
        }

        controllerFactory = factory;
    }
}
