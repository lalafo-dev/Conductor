package com.bluelinelabs.conductor.support.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.Controller;

import androidx.annotation.NonNull;

public class TestController extends Controller {

    @Override @NonNull
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return new FrameLayout(inflater.getContext());
    }

}
