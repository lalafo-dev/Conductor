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

import android.os.Bundle;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerArgs;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * A base {@link Controller} that can be used to expose lifecycle events using RxJava
 */
public abstract class RxController extends Controller implements LifecycleProvider<ControllerEvent> {
    private final BehaviorSubject<ControllerEvent> lifecycleSubject;

    public RxController(){
        this((Bundle) null);
    }

    public RxController(@Nullable Bundle args) {
        super(args);
        lifecycleSubject = ControllerLifecycleSubjectHelper.create(this);
    }

    public RxController(@Nullable ControllerArgs args) {
        super(args);
        lifecycleSubject = ControllerLifecycleSubjectHelper.create(this);
    }

    @Override
    @NonNull
    @CheckResult
    public final Observable<ControllerEvent> lifecycle() {
        return lifecycleSubject.hide();
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull ControllerEvent event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxControllerLifecycle.bindController(lifecycleSubject);
    }
}
