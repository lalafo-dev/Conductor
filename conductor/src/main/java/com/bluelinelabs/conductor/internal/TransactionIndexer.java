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

package com.bluelinelabs.conductor.internal;

import android.os.Bundle;

import androidx.annotation.NonNull;

public class TransactionIndexer {

    private static final String KEY_INDEX = "TransactionIndexer.currentIndex";

    private int currentIndex;

    public int nextIndex() {
        return ++currentIndex;
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putInt(KEY_INDEX, currentIndex);
    }

    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        currentIndex = savedInstanceState.getInt(KEY_INDEX);
    }

}
