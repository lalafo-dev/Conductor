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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A simple controller subclass that changes the onCreateView signature to include a saved view state parameter.
 * This is necessary for some third party libraries like Google Maps, which require passing in a saved state
 * bundle at the time of creation.
 */
abstract public class RestoreViewOnCreateController extends Controller {

    /**
     * Convenience constructor for use when no arguments are needed.
     */
    protected RestoreViewOnCreateController() {
        super((Bundle) null);
    }

    /**
     * Constructor that takes arguments that need to be retained across restarts.
     *
     * @param args Any arguments that need to be retained.
     */
    protected RestoreViewOnCreateController(@Nullable Bundle args) {
        super(args);
    }

    /**
     * Constructor that takes {@link ControllerArgs}  that need to be retained for {@link ControllerFactory}.
     *
     * @param controllerArgs any argument that have to be stored.
     * @see ControllerArgs
     * @see ControllerFactory#newInstance(ClassLoader, String, Object)
     */
    protected RestoreViewOnCreateController(@Nullable ControllerArgs controllerArgs) {
        super(controllerArgs);
    }

    @Override @NonNull
    protected final View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return onCreateView(inflater, container, viewState == null ? null : viewState.getBundle(KEY_VIEW_STATE_BUNDLE));
    }

    /**
     * Called when the controller is ready to display its view. A valid view must be returned. The standard body
     * for this method will be {@code return inflater.inflate(R.layout.my_layout, container, false);}, plus
     * any binding and state restoration code.
     *
     * @param inflater       The LayoutInflater that should be used to inflate views
     * @param container      The parent view that this Controller's view will eventually be attached to.
     *                       This Controller's view should NOT be added in this method. It is simply passed in
     *                       so that valid LayoutParams can be used during inflation.
     * @param savedViewState A bundle for the view's state, which would have been created in {@link #onSaveViewState(View, Bundle)},
     *                       or {@code null} if no saved state exists.
     */
    @NonNull
    protected abstract View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedViewState);

}
