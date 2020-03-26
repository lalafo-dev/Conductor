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

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import androidx.annotation.NonNull;

public class StringSparseArrayParceler implements Parcelable {

    private final SparseArray<String> stringSparseArray;

    public StringSparseArrayParceler(@NonNull SparseArray<String> stringSparseArray) {
        this.stringSparseArray = stringSparseArray;
    }

    StringSparseArrayParceler(@NonNull Parcel in) {
        stringSparseArray = new SparseArray<>();

        final int size = in.readInt();

        for (int i = 0; i < size; i++) {
            stringSparseArray.put(in.readInt(), in.readString());
        }
    }

    @NonNull
    public SparseArray<String> getStringSparseArray() {
        return stringSparseArray;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        final int size = stringSparseArray.size();

        out.writeInt(size);

        for (int i = 0; i < size; i++) {
            int key = stringSparseArray.keyAt(i);

            out.writeInt(key);
            out.writeString(stringSparseArray.get(key));
        }
    }

    public static final Parcelable.Creator<StringSparseArrayParceler> CREATOR = new Parcelable.Creator<StringSparseArrayParceler>() {
        @Override
        public StringSparseArrayParceler createFromParcel(Parcel in) {
            return new StringSparseArrayParceler(in);
        }

        @Override
        public StringSparseArrayParceler[] newArray(int size) {
            return new StringSparseArrayParceler[size];
        }
    };

}
