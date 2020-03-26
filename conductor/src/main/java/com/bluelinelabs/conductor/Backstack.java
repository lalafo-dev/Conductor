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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class Backstack implements Iterable<RouterTransaction> {

    private static final String KEY_ENTRIES = "Backstack.entries";

    private final Deque<RouterTransaction> backstack = new ArrayDeque<>();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isEmpty() {
        return backstack.isEmpty();
    }

    int size() {
        return backstack.size();
    }

    @Nullable
    RouterTransaction root() {
        return backstack.size() > 0 ? backstack.getLast() : null;
    }

    @Override @NonNull
    public Iterator<RouterTransaction> iterator() {
        return backstack.iterator();
    }

    @NonNull
    Iterator<RouterTransaction> reverseIterator() {
        return backstack.descendingIterator();
    }

    @NonNull
    List<RouterTransaction> popTo(@NonNull RouterTransaction transaction) {
        List<RouterTransaction> popped = new ArrayList<>();
        if (backstack.contains(transaction)) {
            while (backstack.peek() != transaction) {
                RouterTransaction poppedTransaction = pop();
                popped.add(poppedTransaction);
            }
        } else {
            throw new RuntimeException("Tried to pop to a transaction that was not on the back stack");
        }
        return popped;
    }

    @NonNull
    RouterTransaction pop() {
        RouterTransaction popped = backstack.pop();
        popped.controller.destroy();
        return popped;
    }

    @Nullable
    RouterTransaction peek() {
        return backstack.peek();
    }

    void push(@NonNull RouterTransaction transaction) {
        backstack.push(transaction);
    }

    @NonNull
    List<RouterTransaction> popAll() {
        List<RouterTransaction> list = new ArrayList<>();
        while (!isEmpty()) {
            list.add(pop());
        }
        return list;
    }

    void setBackstack(@NonNull List<RouterTransaction> backstack) {
        this.backstack.clear();
        for (RouterTransaction transaction : backstack) {
            this.backstack.push(transaction);
        }
    }

    boolean contains(@NonNull Controller controller) {
        for (RouterTransaction transaction : backstack) {
            if (controller == transaction.controller) {
                return true;
            }
        }
        return false;
    }

    void saveInstanceState(@NonNull Bundle outState) {
        ArrayList<Bundle> entryBundles = new ArrayList<>(backstack.size());
        for (RouterTransaction entry : backstack) {
            entryBundles.add(entry.saveInstanceState());
        }

        outState.putParcelableArrayList(KEY_ENTRIES, entryBundles);
    }

    void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        ArrayList<Bundle> entryBundles = savedInstanceState.getParcelableArrayList(KEY_ENTRIES);
        if (entryBundles != null) {
            Collections.reverse(entryBundles);
            for (Bundle transactionBundle : entryBundles) {
                backstack.push(new RouterTransaction(transactionBundle));
            }
        }
    }
}
