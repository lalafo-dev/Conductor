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

import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;

import org.robolectric.util.ReflectionHelpers;

import java.util.List;

public class ViewUtils {

    public static void reportAttached(View view, boolean attached) {
        reportAttached(view, attached, true);
    }

    public static void reportAttached(View view, boolean attached, boolean propogateToChildren) {
        if (view instanceof AttachFakingFrameLayout) {
            ((AttachFakingFrameLayout) view).setAttached(attached, false);
        }

        List<OnAttachStateChangeListener> listeners = getAttachStateListeners(view);

        // Add, then remove an OnAttachStateChangeListener to initialize the attachStateListeners variable inside a view
        if (listeners == null) {
            OnAttachStateChangeListener tmpListener = new OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) { }

                @Override
                public void onViewDetachedFromWindow(View v) { }
            };
            view.addOnAttachStateChangeListener(tmpListener);
            view.removeOnAttachStateChangeListener(tmpListener);
            listeners = getAttachStateListeners(view);
        }

        for (OnAttachStateChangeListener listener : listeners) {
            if (attached) {
                listener.onViewAttachedToWindow(view);
            } else {
                listener.onViewDetachedFromWindow(view);
            }
        }

        if (propogateToChildren && view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                reportAttached(viewGroup.getChildAt(i), attached, true);
            }
        }

    }

    private static List<OnAttachStateChangeListener> getAttachStateListeners(View view) {
        Object listenerInfo = ReflectionHelpers.callInstanceMethod(view, "getListenerInfo");
        return ReflectionHelpers.getField(listenerInfo, "mOnAttachStateChangeListeners");
    }

}
