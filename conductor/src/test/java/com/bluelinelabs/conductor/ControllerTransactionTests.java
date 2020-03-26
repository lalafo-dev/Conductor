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

import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bluelinelabs.conductor.util.TestController;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ControllerTransactionTests {

    @Test
    public void testRouterSaveRestore() {
        RouterTransaction transaction = RouterTransaction.with(new TestController())
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new VerticalChangeHandler())
                .tag("Test Tag");

        Bundle bundle = transaction.saveInstanceState();

        RouterTransaction restoredTransaction = new RouterTransaction(bundle);

        assertEquals(transaction.controller.getClass(), restoredTransaction.controller.getClass());
        assertEquals(transaction.pushChangeHandler().getClass(), restoredTransaction.pushChangeHandler().getClass());
        assertEquals(transaction.popChangeHandler().getClass(), restoredTransaction.popChangeHandler().getClass());
        assertEquals(transaction.tag(), restoredTransaction.tag());
    }

}
