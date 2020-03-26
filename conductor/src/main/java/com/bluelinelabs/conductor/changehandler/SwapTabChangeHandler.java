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

import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A {@link ControllerChangeHandler} that will instantly swap Views with no animations or transitions.
 */
public class SwapTabChangeHandler extends SimpleSwapChangeHandler {

  public SwapTabChangeHandler() {
  }


  @Override
  public void performChange(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
    if (!canceled) {
      if (from != null) {
        container.removeView(from);
      }

      if (to != null && to.getParent() == null) {
        container.addView(to);
      }
    }

    if (container.getWindowToken() != null) {
      changeListener.onChangeCompleted();
    } else {
      this.changeListener = changeListener;
      this.container = container;
      container.addOnAttachStateChangeListener(this);
    }

  }
}
