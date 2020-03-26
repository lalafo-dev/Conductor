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

import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import androidx.annotation.IdRes;

public class ActivityProxy {

    private ActivityController<TestActivity> activityController;
    private AttachFakingFrameLayout view;

    public ActivityProxy() {
        activityController = Robolectric.buildActivity(TestActivity.class);

        @IdRes int containerId = 4;
        view = new AttachFakingFrameLayout(activityController.get());
        view.setId(containerId);
    }

    public ActivityProxy create(Bundle savedInstanceState) {
        activityController.create(savedInstanceState);
        return this;
    }

    public ActivityProxy start() {
        activityController.start();
        view.setAttached(true);
        return this;
    }

    public ActivityProxy resume() {
        activityController.resume();
        return this;
    }

    public ActivityProxy pause() {
        activityController.pause();
        return this;
    }

    public ActivityProxy saveInstanceState(Bundle outState) {
        activityController.saveInstanceState(outState);
        return this;
    }

    public ActivityProxy stop(boolean detachView) {
        activityController.stop();

        if (detachView) {
            view.setAttached(false);
        }

        return this;
    }

    public ActivityProxy destroy() {
        activityController.destroy();
        view.setAttached(false);
        return this;
    }

    public ActivityProxy rotate() {
        getActivity().isChangingConfigurations = true;
        activityController.configurationChange();
        return this;
    }

    public TestActivity getActivity() {
        return activityController.get();
    }

    public AttachFakingFrameLayout getView() {
        return view;
    }
}
