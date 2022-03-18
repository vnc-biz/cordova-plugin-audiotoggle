package com.dooble.audiotoggle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.util.Log;

public class HeadsetActionButtonReceiver extends BroadcastReceiver {
    private static String TAG = "HeadsetActionButtonReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive1" + intent.getAction();
        if (intent == null || !Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction()))
            return;

        KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
        Log.d(TAG, "onReceive2" + keyEvent.getAction());
    }
}
