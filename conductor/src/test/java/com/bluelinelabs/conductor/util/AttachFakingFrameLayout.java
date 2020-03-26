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

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.io.FileDescriptor;

public class AttachFakingFrameLayout extends FrameLayout {

    final IBinder fakeWindowToken = new IBinder() {
        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return null;
        }

        @Override
        public boolean pingBinder() {
            return false;
        }

        @Override
        public boolean isBinderAlive() {
            return false;
        }

        @Override
        public IInterface queryLocalInterface(String descriptor) {
            return null;
        }

        @Override
        public void dump(FileDescriptor fd, String[] args) throws RemoteException {

        }

        @Override
        public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {

        }

        @Override
        public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return false;
        }

        @Override
        public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {

        }

        @Override
        public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
            return false;
        }
    };

    private boolean reportAttached;

    public AttachFakingFrameLayout(Context context) {
        super(context);
    }

    public AttachFakingFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachFakingFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public final IBinder getWindowToken() {
        return reportAttached ? fakeWindowToken : null;
    }

    public void setAttached(boolean attached) {
        setAttached(attached, true);
    }

    public void setAttached(boolean attached, boolean reportToViewUtils) {
        if (reportAttached != attached) {
            reportAttached = attached;
            if (reportToViewUtils) {
                ViewUtils.reportAttached(this, attached);
            }

            for (int i = 0; i < getChildCount(); i++) {
                ViewUtils.reportAttached(getChildAt(i), attached);
            }
        }
    }

    @Override
    public void onViewAdded(View child) {
        if (reportAttached) {
            ViewUtils.reportAttached(child, true);
        }
        super.onViewAdded(child);
    }

    @Override
    public void onViewRemoved(View child) {
        ViewUtils.reportAttached(child, false);
        super.onViewRemoved(child);
    }

}
