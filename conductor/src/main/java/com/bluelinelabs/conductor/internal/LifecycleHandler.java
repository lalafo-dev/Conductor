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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ActivityHostedRouter;
import com.bluelinelabs.conductor.Router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class LifecycleHandler extends Fragment implements ActivityLifecycleCallbacks {

    private static final String FRAGMENT_TAG = "LifecycleHandler";

    private static final String KEY_PENDING_PERMISSION_REQUESTS = "LifecycleHandler.pendingPermissionRequests";
    private static final String KEY_PERMISSION_REQUEST_CODES = "LifecycleHandler.permissionRequests";
    private static final String KEY_ACTIVITY_REQUEST_CODES = "LifecycleHandler.activityRequests";
    private static final String KEY_ROUTER_STATE_PREFIX = "LifecycleHandler.routerState";

    private Activity activity;
    private boolean hasRegisteredCallbacks;
    private boolean destroyed;
    private boolean attached;
    private boolean hasPreparedForHostDetach;

    private static final Map<Activity, LifecycleHandler> activeLifecycleHandlers = new HashMap<>();
    private SparseArray<String> permissionRequestMap = new SparseArray<>();
    private SparseArray<String> activityRequestMap = new SparseArray<>();
    private ArrayList<PendingPermissionRequest> pendingPermissionRequests = new ArrayList<>();

    private final Map<Integer, ActivityHostedRouter> routerMap = new HashMap<>();

    public LifecycleHandler() {
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    private static LifecycleHandler findInActivity(@NonNull FragmentActivity activity) {
        LifecycleHandler lifecycleHandler = activeLifecycleHandlers.get(activity);
        if (lifecycleHandler == null) {
            lifecycleHandler = (LifecycleHandler)activity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        }
        if (lifecycleHandler != null) {
            lifecycleHandler.registerActivityListener(activity);
        }
        return lifecycleHandler;
    }

    @NonNull
    public static LifecycleHandler install(@NonNull FragmentActivity activity) {
        LifecycleHandler lifecycleHandler = findInActivity(activity);
        if (lifecycleHandler == null) {
            lifecycleHandler = new LifecycleHandler();
            activity.getSupportFragmentManager().beginTransaction().add(lifecycleHandler, FRAGMENT_TAG).commit();
        }
        lifecycleHandler.registerActivityListener(activity);
        return lifecycleHandler;
    }

    @NonNull
    public Router getRouter(@NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        ActivityHostedRouter router = routerMap.get(getRouterHashKey(container));
        if (router == null) {
            router = new ActivityHostedRouter();
            router.setHost(this, container);

            if (savedInstanceState != null) {
                Bundle routerSavedState = savedInstanceState.getBundle(KEY_ROUTER_STATE_PREFIX + router.getContainerId());
                if (routerSavedState != null) {
                    router.restoreInstanceState(routerSavedState);
                }
            }
            routerMap.put(getRouterHashKey(container), router);
        } else {
            router.setHost(this, container);
        }

        return router;
    }

    @NonNull
    public List<Router> getRouters() {
        return new ArrayList<Router>(routerMap.values());
    }

    @Nullable
    public Activity getLifecycleActivity() {
        return activity;
    }

    private static int getRouterHashKey(@NonNull ViewGroup viewGroup) {
        return viewGroup.getId();
    }

    private void registerActivityListener(@NonNull Activity activity) {
        this.activity = activity;

        if (!hasRegisteredCallbacks) {
            hasRegisteredCallbacks = true;
            activity.getApplication().registerActivityLifecycleCallbacks(this);

            // Since Fragment transactions are async, we have to keep an <Activity, LifecycleHandler> map in addition
            // to trying to find the LifecycleHandler fragment in the Activity to handle the case of the developer
            // trying to immediately get > 1 router in the same Activity. See issue #299.
            activeLifecycleHandlers.put(activity, this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            StringSparseArrayParceler permissionParcel = savedInstanceState.getParcelable(KEY_PERMISSION_REQUEST_CODES);
            permissionRequestMap = permissionParcel != null ? permissionParcel.getStringSparseArray() : new SparseArray<String>();

            StringSparseArrayParceler activityParcel = savedInstanceState.getParcelable(KEY_ACTIVITY_REQUEST_CODES);
            activityRequestMap = activityParcel != null ? activityParcel.getStringSparseArray() : new SparseArray<String>();

            ArrayList<PendingPermissionRequest> pendingRequests = savedInstanceState.getParcelableArrayList(KEY_PENDING_PERMISSION_REQUESTS);
            pendingPermissionRequests = pendingRequests != null ? pendingRequests : new ArrayList<PendingPermissionRequest>();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_PERMISSION_REQUEST_CODES, new StringSparseArrayParceler(permissionRequestMap));
        outState.putParcelable(KEY_ACTIVITY_REQUEST_CODES, new StringSparseArrayParceler(activityRequestMap));
        outState.putParcelableArrayList(KEY_PENDING_PERMISSION_REQUESTS, pendingPermissionRequests);
    }

    @Override public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        for (Router router : getRouters()) {
            router.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (activity != null) {
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            activeLifecycleHandlers.remove(activity);
            destroyRouters();
            activity = null;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        destroyed = false;
        setAttached();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        destroyed = false;
        setAttached();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        attached = false;
        destroyRouters();
    }

    private void setAttached() {
        if (!attached) {
            attached = true;

            for (int i = pendingPermissionRequests.size() - 1; i >= 0; i--) {
                PendingPermissionRequest request = pendingPermissionRequests.remove(i);
                requestPermissions(request.instanceId, request.permissions, request.requestCode);
            }
        }

        for (ActivityHostedRouter router : new ArrayList<>(routerMap.values())) {
            router.onContextAvailable();
        }
    }

    private void destroyRouters() {
        if (!destroyed) {
            destroyed = true;

            if (activity != null) {
                for (Router router : getRouters()) {
                    router.onActivityDestroyed(activity);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String instanceId = activityRequestMap.get(requestCode);
        if (instanceId != null) {
            for (Router router : getRouters()) {
                router.onActivityResult(instanceId, requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String instanceId = permissionRequestMap.get(requestCode);
        if (instanceId != null) {
            for (Router router : getRouters()) {
                router.onRequestPermissionsResult(instanceId, requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        for (Router router : getRouters()) {
            Boolean handled = router.handleRequestedPermission(permission);
            if (handled != null) {
                return handled;
            }
        }
        return super.shouldShowRequestPermissionRationale(permission);
    }

    public void registerForActivityResult(@NonNull String instanceId, int requestCode) {
        activityRequestMap.put(requestCode, instanceId);
    }

    public void unregisterForActivityResults(@NonNull String instanceId) {
        for (int i = activityRequestMap.size() - 1; i >= 0; i--) {
            if (instanceId.equals(activityRequestMap.get(activityRequestMap.keyAt(i)))) {
                activityRequestMap.removeAt(i);
            }
        }
    }

    public void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode) {
        registerForActivityResult(instanceId, requestCode);
        startActivityForResult(intent, requestCode);
    }

    public void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode, @Nullable Bundle options) {
        registerForActivityResult(instanceId, requestCode);
        startActivityForResult(intent, requestCode, options);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void startIntentSenderForResult(@NonNull String instanceId, @NonNull IntentSender intent, int requestCode,
      @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags,
      @Nullable Bundle options) throws IntentSender.SendIntentException {
        registerForActivityResult(instanceId, requestCode);
        startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(@NonNull String instanceId, @NonNull String[] permissions, int requestCode) {
        if (attached) {
            permissionRequestMap.put(requestCode, instanceId);
            requestPermissions(permissions, requestCode);
        } else {
            pendingPermissionRequests.add(new PendingPermissionRequest(instanceId, permissions, requestCode));
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
        if (this.activity == null && findInActivity((FragmentActivity) activity) == LifecycleHandler.this) {
            this.activity = activity;

            for (ActivityHostedRouter router : new ArrayList<>(routerMap.values())) {
                router.onContextAvailable();
            }
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (this.activity == activity) {
            hasPreparedForHostDetach = false;

            for (Router router : getRouters()) {
                router.onActivityStarted(activity);
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (this.activity == activity) {
            for (Router router : getRouters()) {
                router.onActivityResumed(activity);
            }
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (this.activity == activity) {
            for (Router router : getRouters()) {
                router.onActivityPaused(activity);
            }
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (this.activity == activity) {
            prepareForHostDetachIfNeeded();

            for (Router router : getRouters()) {
                router.onActivityStopped(activity);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        if (this.activity == activity) {
            prepareForHostDetachIfNeeded();

            for (Router router : getRouters()) {
                Bundle bundle = new Bundle();
                router.saveInstanceState(bundle);
                outState.putBundle(KEY_ROUTER_STATE_PREFIX + router.getContainerId(), bundle);
            }
        }
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        activeLifecycleHandlers.remove(activity);
    }

    private void prepareForHostDetachIfNeeded() {
        if (!hasPreparedForHostDetach) {
            hasPreparedForHostDetach = true;

            for (Router router : getRouters()) {
                router.prepareForHostDetach();
            }
        }
    }

    private static class PendingPermissionRequest implements Parcelable {
        final String instanceId;
        final String[] permissions;
        final int requestCode;

        PendingPermissionRequest(@NonNull String instanceId, @NonNull String[] permissions, int requestCode) {
            this.instanceId = instanceId;
            this.permissions = permissions;
            this.requestCode = requestCode;
        }

        PendingPermissionRequest(Parcel in) {
            instanceId = in.readString();
            permissions = in.createStringArray();
            requestCode = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(instanceId);
            out.writeStringArray(permissions);
            out.writeInt(requestCode);
        }

        public static final Parcelable.Creator<PendingPermissionRequest> CREATOR = new Parcelable.Creator<PendingPermissionRequest>() {
            @Override
            public PendingPermissionRequest createFromParcel(Parcel in) {
                return new PendingPermissionRequest(in);
            }

            @Override
            public PendingPermissionRequest[] newArray(int size) {
                return new PendingPermissionRequest[size];
            }
        };

    }
}
