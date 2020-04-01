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

package com.lalafo.conductor.glide;

import android.view.View;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.manager.RequestManagerTreeNode;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class BaseGlideControllerSupport<T extends RequestManager> implements RequestManagerTreeNode {

  private WeakReference<Controller> controller;
  private @Nullable ControllerLifecycle lifecycle = null;
  private @Nullable T glideRequests = null;

  private boolean destroyGlide = true;

  public void setDestroyGlide(boolean destroy) {
    destroyGlide = destroy;
  }

  protected abstract T getGlideRequest(@NonNull ControllerLifecycle lifecycle, RequestManagerTreeNode requestManagerTreeNode);

  public BaseGlideControllerSupport(Controller controller) {
    this.controller = new WeakReference<>(controller);

    controller.addLifecycleListener(new Controller.LifecycleListener() {
      @Override public void preCreateView(@NonNull Controller controller) {
        initGlide();
      }

      @Override public void postAttach(@NonNull Controller controller, @NonNull View view) {
        if (lifecycle != null) {
          lifecycle.onStart();
        }
      }

      @Override public void preDetach(@NonNull Controller controller, @NonNull View view) {
        if (lifecycle != null) {
          lifecycle.onStop();
        }
      }

      @Override public void postDestroyView(@NonNull Controller controller) {
        destroyGlide();
      }

      @Override public void postDestroy(@NonNull Controller controller) {
        destroyGlide();
      }
    });
  }

  private void destroyGlide() {
    if (destroyGlide){
      if (lifecycle != null) {
        lifecycle.onDestroy();
      }
      lifecycle = null;
      glideRequests = null;
    }
  }

  public T getGlide() {
    initGlide();
    return glideRequests;
  }

  private void initGlide() {
    if (glideRequests == null) {
      lifecycle = new ControllerLifecycle();
      glideRequests = getGlideRequest(lifecycle, this);
    }
  }

  @NonNull @Override public Set<RequestManager> getDescendants() {
    Set<RequestManager> collected = new HashSet<>();
    collectRequestManagers(controller.get(), collected);
    return collected;
  }

  /**
   * Recursively gathers the [RequestManager]s of a given [Controller] and all its child controllers.
   * The [Controller]s in the hierarchy must implement [GlideProvider] in order for their
   * request managers to be collected.
   */
  private void collectRequestManagers(@Nullable Controller controller, Set<RequestManager> collected) {
    if (controller != null) {
      if (!controller.isDestroyed() && !controller.isBeingDestroyed()) {
        if (controller instanceof GlideProvider) {
          collected.add(((GlideProvider) controller).getGlide());
        }

        for (Router router : controller.getChildRouters()) {
          for (RouterTransaction transaction : router.getBackstack()) {
            Controller c = transaction.controller();
            collectRequestManagers(c, collected);
          }
        }
      }
    }
  }
}
