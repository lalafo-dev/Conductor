package com.bluelinelabs.conductor.internal.lifecyclehandler;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface LifecycleHelper {

    void onDestroy();

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void startActivity(Intent intent);

    void startActivityForResult(Intent intent, int requestCode);

    void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options);

    void startIntentSenderForResult(IntentSender intent, int requestCode, @Nullable Intent fillInIntent,
      int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws IntentSender.SendIntentException;

    void requestPermissions(@NonNull String[] permissions, int requestCode);

    LifecycleHandler lifecycleHandler();

}