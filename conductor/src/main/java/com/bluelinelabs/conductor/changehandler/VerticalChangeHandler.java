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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An {@link AnimatorChangeHandler} that will slide either slide a new View up or slide an old View down,
 * depending on whether a push or pop change is happening.
 */
public class VerticalChangeHandler extends AnimatorChangeHandler {

    public VerticalChangeHandler() { }

    public VerticalChangeHandler(boolean removesFromViewOnPush) {
        super(removesFromViewOnPush);
    }

    public VerticalChangeHandler(long duration) {
        super(duration);
    }

    public VerticalChangeHandler(long duration, boolean removesFromViewOnPush) {
        super(duration, removesFromViewOnPush);
    }

    @Override @NonNull
    protected Animator getAnimator(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, boolean toAddedToContainer) {
        AnimatorSet animator = new AnimatorSet();
        List<Animator> viewAnimators = new ArrayList<>();

        if (isPush && to != null) {
            viewAnimators.add(ObjectAnimator.ofFloat(to, View.TRANSLATION_Y, to.getHeight(), 0));
        } else if (!isPush && from != null) {
            viewAnimators.add(ObjectAnimator.ofFloat(from, View.TRANSLATION_Y, from.getHeight()));
        }

        animator.playTogether(viewAnimators);
        return animator;
    }

    @Override
    protected void resetFromView(@NonNull View from) { }

    @Override @NonNull
    public ControllerChangeHandler copy() {
        return new VerticalChangeHandler(getAnimationDuration(), removesFromViewOnPush());
    }

}
