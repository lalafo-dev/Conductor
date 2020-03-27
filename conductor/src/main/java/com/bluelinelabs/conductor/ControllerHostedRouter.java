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
import com.bluelinelabs.conductor.internal.TransactionIndexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ControllerHostedRouter extends Router {

    private final String KEY_HOST_ID = "ControllerHostedRouter.hostId";
    private final String KEY_TAG = "ControllerHostedRouter.tag";

    private Controller hostController;

    @IdRes private int hostId;
    private String tag;
    private boolean isDetachFrozen;

    ControllerHostedRouter() { }

    ControllerHostedRouter(int hostId, @Nullable String tag) {
        this.hostId = hostId;
        this.tag = tag;
    }

    public final void setHost(@NonNull Controller controller, @NonNull ViewGroup container) {
        if (hostController != controller || this.container != container) {
            removeHost();

            if (container instanceof ControllerChangeListener) {
                addChangeListener((ControllerChangeListener)container);
            }

            hostController = controller;
            this.container = container;

            for (RouterTransaction transaction : backstack) {
                transaction.controller.setParentController(controller);
            }

            watchContainerAttach();
        }
    }

    public final void removeHost() {
        if (container != null && container instanceof ControllerChangeListener) {
            removeChangeListener((ControllerChangeListener)container);
        }

        final List<Controller> controllersToDestroy = new ArrayList<>(destroyingControllers);
        for (Controller controller : controllersToDestroy) {
            if (controller.getView() != null) {
                controller.detach(controller.getView(), true, false);
            }
        }
        for (RouterTransaction transaction : backstack) {
            if (transaction.controller.getView() != null) {
                transaction.controller.detach(transaction.controller.getView(), true, false);
            }
        }

        prepareForContainerRemoval();
        hostController = null;
        container = null;
    }

    final void setDetachFrozen(boolean frozen) {
        isDetachFrozen = frozen;
        for (RouterTransaction transaction : backstack) {
            transaction.controller.setDetachFrozen(frozen);
        }
    }

    @Override
    void destroy(boolean popViews) {
        setDetachFrozen(false);
        super.destroy(popViews);
    }

    @Override
    protected void pushToBackstack(@NonNull RouterTransaction entry) {
        if (isDetachFrozen) {
            entry.controller.setDetachFrozen(true);
        }
        super.pushToBackstack(entry);
    }

    @Override
    public void setBackstack(@NonNull List<RouterTransaction> newBackstack, @Nullable ControllerChangeHandler changeHandler) {
        if (isDetachFrozen) {
            for (RouterTransaction transaction : newBackstack) {
                transaction.controller.setDetachFrozen(true);
            }
        }
        super.setBackstack(newBackstack, changeHandler);
    }

    @Override @Nullable
    public Activity getActivity() {
        return hostController != null ? hostController.getActivity() : null;
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        super.onActivityDestroyed(activity);

        removeHost();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    void startActivity(@NonNull Intent intent) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().startActivity(intent);
        }
    }

    @Override
    void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().startActivityForResult(instanceId, intent, requestCode);
        }
    }

    @Override
    void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode, @Nullable Bundle options) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().startActivityForResult(instanceId, intent, requestCode, options);
        }
    }

    @Override
    void startIntentSenderForResult(@NonNull String instanceId, @NonNull IntentSender intent, int requestCode, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws SendIntentException {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().startIntentSenderForResult(instanceId, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
        }
    }

    @Override
    void registerForActivityResult(@NonNull String instanceId, int requestCode) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().registerForActivityResult(instanceId, requestCode);
        }
    }

    @Override
    void unregisterForActivityResults(@NonNull String instanceId) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().unregisterForActivityResults(instanceId);
        }
    }

    @Override
    void requestPermissions(@NonNull String instanceId, @NonNull String[] permissions, int requestCode) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().requestPermissions(instanceId, permissions, requestCode);
        }
    }

    @Override
    public boolean hasHost() {
        return hostController != null;
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        super.saveInstanceState(outState);

        outState.putInt(KEY_HOST_ID, hostId);
        outState.putString(KEY_TAG, tag);
    }

    @Override
    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.restoreInstanceState(savedInstanceState);

        hostId = savedInstanceState.getInt(KEY_HOST_ID);
        tag = savedInstanceState.getString(KEY_TAG);
    }

    @Override
    void setControllerRouter(@NonNull Controller controller) {
        controller.setParentController(hostController);
        super.setControllerRouter(controller);
    }

    int getHostId() {
        return hostId;
    }

    @Nullable
    String getTag() {
        return tag;
    }

    @Override @NonNull
    List<Router> getSiblingRouters() {
        List<Router> list = new ArrayList<>();
        if (hasHost()){
            list.addAll(hostController.getChildRouters());
            list.addAll(hostController.getRouter().getSiblingRouters());
        }
        return list;
    }

    @Override @NonNull
    public Router getRootRouter() {
        if (hostController != null && hostController.getRouter() != null) {
            return hostController.getRouter().getRootRouter();
        } else {
            return this;
        }
    }

    @Override @NonNull
    TransactionIndexer getTransactionIndexer() {
        Router rootRouter = getRootRouter();
        if (rootRouter == this) {
            String debugInfo;
            if (hostController != null) {
                debugInfo = String.format(Locale.ENGLISH, "%s (attached? %b, destroyed? %b, parent: %s)",
                  hostController.getClass().getSimpleName(), hostController.isAttached(), hostController.isBeingDestroyed, hostController.getParentController());
            } else {
                debugInfo = "null host controller";
            }
            throw new IllegalStateException("Unable to retrieve TransactionIndexer from " + debugInfo);
        } else {
            return getRootRouter().getTransactionIndexer();
        }
    }

}
