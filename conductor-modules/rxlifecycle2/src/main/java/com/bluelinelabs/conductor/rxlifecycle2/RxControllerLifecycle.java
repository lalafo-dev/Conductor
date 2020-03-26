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

package com.bluelinelabs.conductor.rxlifecycle2;

import androidx.annotation.NonNull;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.OutsideLifecycleException;
import com.trello.rxlifecycle2.RxLifecycle;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class RxControllerLifecycle {

    /**
     * Binds the given source to a Controller lifecycle. This is the Controller version of
     * {@link com.trello.rxlifecycle2.android.RxLifecycleAndroid#bindFragment(Observable)}.
     *
     * @param lifecycle the lifecycle sequence of a Controller
     * @return a reusable {@link io.reactivex.ObservableTransformer} that unsubscribes the source during the Controller lifecycle
     */
    public static <T> LifecycleTransformer<T> bindController(@NonNull final Observable<ControllerEvent> lifecycle) {
        return RxLifecycle.bind(lifecycle, CONTROLLER_LIFECYCLE);
    }

    private static final Function<ControllerEvent, ControllerEvent> CONTROLLER_LIFECYCLE =
        new Function<ControllerEvent, ControllerEvent>() {
            @Override
            public ControllerEvent apply(ControllerEvent lastEvent) {
                switch (lastEvent) {
                    case CREATE:
                        return ControllerEvent.DESTROY;
                    case CONTEXT_AVAILABLE:
                        return ControllerEvent.CONTEXT_UNAVAILABLE;
                    case ATTACH:
                        return ControllerEvent.DETACH;
                    case CREATE_VIEW:
                        return ControllerEvent.DESTROY_VIEW;
                    case DETACH:
                        return ControllerEvent.DESTROY;
                    default:
                        throw new OutsideLifecycleException("Cannot bind to Controller lifecycle when outside of it.");
                }
            }
        };
}
