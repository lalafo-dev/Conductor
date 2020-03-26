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

package com.bluelinelabs.conductor.changehandler;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.internal.ClassUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A base {@link ControllerChangeHandler} that facilitates using {@link android.transition.Transition}s to replace Controller Views.
 * If the target device is running on a version of Android that doesn't support transitions, a fallback {@link ControllerChangeHandler} will be used.
 */
public class TransitionChangeHandlerCompat extends ControllerChangeHandler {

    private static final String KEY_CHANGE_HANDLER_CLASS = "TransitionChangeHandlerCompat.changeHandler.class";
    private static final String KEY_HANDLER_STATE = "TransitionChangeHandlerCompat.changeHandler.state";

    @Nullable
    private ControllerChangeHandler changeHandler;

    public TransitionChangeHandlerCompat() { }

    /**
     * Constructor that takes a {@link TransitionChangeHandler} for use with compatible devices, as well as a fallback
     * {@link ControllerChangeHandler} for use with older devices.
     *
     * @param transitionChangeHandler The change handler that will be used on API 21 and above
     * @param fallbackChangeHandler The change handler that will be used on APIs below 21
     */
    public TransitionChangeHandlerCompat(@NonNull TransitionChangeHandler transitionChangeHandler, @NonNull ControllerChangeHandler fallbackChangeHandler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            changeHandler = transitionChangeHandler;
        } else {
            changeHandler = fallbackChangeHandler;
        }
    }

    @Override
    public void performChange(@NonNull final ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, @NonNull final ControllerChangeCompletedListener changeListener) {
        if (changeHandler != null) {
            changeHandler.performChange(container, from, to, isPush, changeListener);
        }
    }

    @Override
    public void saveToBundle(@NonNull Bundle bundle) {
        super.saveToBundle(bundle);

        if (changeHandler != null) {
            bundle.putString(KEY_CHANGE_HANDLER_CLASS, changeHandler.getClass().getName());
        }

        Bundle stateBundle = new Bundle();
        if (changeHandler != null) {
            changeHandler.saveToBundle(stateBundle);
        }
        bundle.putBundle(KEY_HANDLER_STATE, stateBundle);
    }

    @Override
    public void restoreFromBundle(@NonNull Bundle bundle) {
        super.restoreFromBundle(bundle);

        String className = bundle.getString(KEY_CHANGE_HANDLER_CLASS);
        changeHandler = ClassUtils.newInstance(className);
        //noinspection ConstantConditions
        changeHandler.restoreFromBundle(bundle.getBundle(KEY_HANDLER_STATE));
    }

    @Override
    public boolean removesFromViewOnPush() {
        if (changeHandler != null) {
            return changeHandler.removesFromViewOnPush();
        }
        return true;
    }

    @Override @NonNull
    public ControllerChangeHandler copy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new TransitionChangeHandlerCompat((TransitionChangeHandler)changeHandler.copy(), null);
        } else {
            return new TransitionChangeHandlerCompat(null, changeHandler.copy());
        }
    }

    @Override
    public void onAbortPush(@NonNull ControllerChangeHandler newHandler, @Nullable Controller newTop) {
        if (changeHandler != null) {
            changeHandler.onAbortPush(newHandler, newTop);
        }
    }

    @Override
    public void completeImmediately() {
        if (changeHandler != null) {
            changeHandler.completeImmediately();
        }
    }

    @Override
    public void setForceRemoveViewOnPush(boolean force) {
        if (changeHandler != null) {
            changeHandler.setForceRemoveViewOnPush(force);
        }
    }

    @Override
    protected void onEnd() {
        changeHandler = null;
    }
}