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

package com.bluelinelabs.conductor.util;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MockChangeHandler extends ControllerChangeHandler {

    private static final String KEY_REMOVES_FROM_VIEW_ON_PUSH = "MockChangeHandler.removesFromViewOnPush";
    private static final String KEY_TAG = "MockChangeHandler.tag";

    public static class ChangeHandlerListener {
        public void willStartChange() { }
        public void didAttachOrDetach() { }
        public void didEndChange() { }
    }

    private final ChangeHandlerListener listener;
    private boolean removesFromViewOnPush;

    public View from;
    public View to;
    public String tag;

    public static MockChangeHandler defaultHandler() {
        return new MockChangeHandler(true, null, null);
    }

    public static MockChangeHandler noRemoveViewOnPushHandler() {
        return new MockChangeHandler(false, null, null);
    }

    public static MockChangeHandler noRemoveViewOnPushHandler(String tag) {
        return new MockChangeHandler(false, tag, null);
    }

    public static MockChangeHandler listeningChangeHandler(@NonNull ChangeHandlerListener listener) {
        return new MockChangeHandler(true, null, listener);
    }

    public static MockChangeHandler taggedHandler(String tag, boolean removeViewOnPush) {
        return new MockChangeHandler(removeViewOnPush, tag, null);
    }

    public MockChangeHandler() {
        listener = null;
    }

    private MockChangeHandler(boolean removesFromViewOnPush, String tag, ChangeHandlerListener listener) {
        this.removesFromViewOnPush = removesFromViewOnPush;
        this.tag = tag;

        if (listener == null) {
            this.listener = new ChangeHandlerListener() { };
        } else {
            this.listener = listener;
        }
    }

    @Override
    public void performChange(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
        this.from = from;
        this.to = to;

        listener.willStartChange();

        if (isPush) {
            if (to != null) {
                container.addView(to);
                listener.didAttachOrDetach();
            }

            if (removesFromViewOnPush && from != null) {
                container.removeView(from);
            }
        } else {
            container.removeView(from);
            listener.didAttachOrDetach();

            if (to != null) {
                container.addView(to);
            }

        }

        changeListener.onChangeCompleted();
        listener.didEndChange();
    }

    @Override
    public boolean removesFromViewOnPush() {
        return removesFromViewOnPush;
    }

    @Override
    public void saveToBundle(@NonNull Bundle bundle) {
        super.saveToBundle(bundle);
        bundle.putBoolean(KEY_REMOVES_FROM_VIEW_ON_PUSH, removesFromViewOnPush);
        bundle.putString(KEY_TAG, tag);
    }

    @Override
    public void restoreFromBundle(@NonNull Bundle bundle) {
        super.restoreFromBundle(bundle);
        removesFromViewOnPush = bundle.getBoolean(KEY_REMOVES_FROM_VIEW_ON_PUSH);
        tag = bundle.getString(KEY_TAG);
    }

    @NonNull
    @Override
    public ControllerChangeHandler copy() {
        return new MockChangeHandler(removesFromViewOnPush, tag, listener);
    }

    @Override
    public boolean isReusable() {
        return true;
    }
}
