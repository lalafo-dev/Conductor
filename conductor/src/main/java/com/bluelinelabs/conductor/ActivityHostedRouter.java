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

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;
import com.bluelinelabs.conductor.internal.LifecycleHandler;
import com.bluelinelabs.conductor.internal.TransactionIndexer;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ActivityHostedRouter extends Router {

    private LifecycleHandler lifecycleHandler;
    private final TransactionIndexer transactionIndexer = new TransactionIndexer();

    public final void setHost(@NonNull LifecycleHandler lifecycleHandler, @NonNull ViewGroup container) {
        if (this.lifecycleHandler != lifecycleHandler || this.container != container) {
            if (this.container != null && this.container instanceof ControllerChangeListener) {
                removeChangeListener((ControllerChangeListener)this.container);
            }

            if (container instanceof ControllerChangeListener) {
                addChangeListener((ControllerChangeListener)container);
            }

            this.lifecycleHandler = lifecycleHandler;
            this.container = container;

            watchContainerAttach();
        }
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        super.saveInstanceState(outState);

        transactionIndexer.saveInstanceState(outState);
    }

    @Override
    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.restoreInstanceState(savedInstanceState);

        transactionIndexer.restoreInstanceState(savedInstanceState);
    }

    @Override @Nullable
    public Activity getActivity() {
        return lifecycleHandler != null ? lifecycleHandler.getLifecycleActivity() : null;
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        super.onActivityDestroyed(activity);
        lifecycleHandler = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        lifecycleHandler.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    void startActivity(@NonNull Intent intent) {
        lifecycleHandler.startActivity(intent);
    }

    @Override
    void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode) {
        lifecycleHandler.startActivityForResult(instanceId, intent, requestCode);
    }

    @Override
    void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode, @Nullable Bundle options) {
        lifecycleHandler.startActivityForResult(instanceId, intent, requestCode, options);
    }

    @Override
    void startIntentSenderForResult(@NonNull String instanceId, @NonNull IntentSender intent, int requestCode, @Nullable Intent fillInIntent,
      int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws SendIntentException {
        lifecycleHandler.startIntentSenderForResult(instanceId, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
    }

    @Override
    void registerForActivityResult(@NonNull String instanceId, int requestCode) {
        lifecycleHandler.registerForActivityResult(instanceId, requestCode);
    }

    @Override
    void unregisterForActivityResults(@NonNull String instanceId) {
        lifecycleHandler.unregisterForActivityResults(instanceId);
    }

    @Override
    void requestPermissions(@NonNull String instanceId, @NonNull String[] permissions, int requestCode) {
        lifecycleHandler.requestPermissions(instanceId, permissions, requestCode);
    }

    @Override
    public boolean hasHost() {
        return lifecycleHandler != null;
    }

    @Override @NonNull
    List<Router> getSiblingRouters() {
        return lifecycleHandler.getRouters();
    }

    @Override @NonNull
    public Router getRootRouter() {
        return this;
    }

    @Override @NonNull
    TransactionIndexer getTransactionIndexer() {
        return transactionIndexer;
    }

    @Override
    public void onContextAvailable() {
        super.onContextAvailable();
    }

}
