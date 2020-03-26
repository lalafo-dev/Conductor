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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;

/**
 * A FrameLayout implementation that can be used to block user interactions while
 * {@link ControllerChangeHandler}s are performing changes. It is not required to use this
 * ViewGroup, but it can be helpful.
 */
public class ChangeHandlerFrameLayout extends FrameLayout implements ControllerChangeListener {

    private int inProgressTransactionCount;

    public ChangeHandlerFrameLayout(Context context) {
        super(context);
    }

    public ChangeHandlerFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChangeHandlerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChangeHandlerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return (inProgressTransactionCount > 0) || super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onChangeStarted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
        inProgressTransactionCount++;
    }

    @Override
    public void onChangeCompleted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
        inProgressTransactionCount--;
    }

}
