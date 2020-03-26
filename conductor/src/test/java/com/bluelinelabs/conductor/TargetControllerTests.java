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
import android.view.ViewGroup;

import com.bluelinelabs.conductor.util.ActivityProxy;
import com.bluelinelabs.conductor.util.MockChangeHandler;
import com.bluelinelabs.conductor.util.TestController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TargetControllerTests {

    private Router router;

    public void createActivityController(Bundle savedInstanceState) {
        ActivityProxy activityProxy = new ActivityProxy().create(savedInstanceState).start().resume();
        router = Conductor.attachRouter(activityProxy.getActivity(), activityProxy.getView(), savedInstanceState);
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new TestController()));
        }
    }

    @Before
    public void setup() {
        createActivityController(null);
    }

    @Test
    public void testSiblingTarget() {
        final TestController controllerA = new TestController();
        final TestController controllerB = new TestController();

        assertNull(controllerA.getTargetController());
        assertNull(controllerB.getTargetController());

        router.pushController(RouterTransaction.with(controllerA)
          .pushChangeHandler(MockChangeHandler.defaultHandler())
          .popChangeHandler(MockChangeHandler.defaultHandler()));

        controllerB.setTargetController(controllerA);

        router.pushController(RouterTransaction.with(controllerB)
          .pushChangeHandler(MockChangeHandler.defaultHandler())
          .popChangeHandler(MockChangeHandler.defaultHandler()));

        assertNull(controllerA.getTargetController());
        assertEquals(controllerA, controllerB.getTargetController());
    }

    @Test
    public void testParentChildTarget() {
        final TestController controllerA = new TestController();
        final TestController controllerB = new TestController();

        assertNull(controllerA.getTargetController());
        assertNull(controllerB.getTargetController());

        router.pushController(RouterTransaction.with(controllerA)
          .pushChangeHandler(MockChangeHandler.defaultHandler())
          .popChangeHandler(MockChangeHandler.defaultHandler()));

        controllerB.setTargetController(controllerA);

        Router childRouter = controllerA.getChildRouter((ViewGroup)controllerA.getView().findViewById(TestController.VIEW_ID));
        childRouter.pushController(RouterTransaction.with(controllerB)
          .pushChangeHandler(MockChangeHandler.defaultHandler())
          .popChangeHandler(MockChangeHandler.defaultHandler()));

        assertNull(controllerA.getTargetController());
        assertEquals(controllerA, controllerB.getTargetController());
    }

    @Test
    public void testChildParentTarget() {
        final TestController controllerA = new TestController();
        final TestController controllerB = new TestController();

        assertNull(controllerA.getTargetController());
        assertNull(controllerB.getTargetController());

        router.pushController(RouterTransaction.with(controllerA)
          .pushChangeHandler(MockChangeHandler.defaultHandler())
          .popChangeHandler(MockChangeHandler.defaultHandler()));

        controllerA.setTargetController(controllerB);

        Router childRouter = controllerA.getChildRouter((ViewGroup)controllerA.getView().findViewById(TestController.VIEW_ID));
        childRouter.pushController(RouterTransaction.with(controllerB)
          .pushChangeHandler(MockChangeHandler.defaultHandler())
          .popChangeHandler(MockChangeHandler.defaultHandler()));

        assertNull(controllerB.getTargetController());
        assertEquals(controllerB, controllerA.getTargetController());
    }

}
