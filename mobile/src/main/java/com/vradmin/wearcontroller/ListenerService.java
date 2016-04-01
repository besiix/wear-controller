package com.vradmin.wearcontroller;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {
    public static final String TAG = ListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Log.d(TAG, "Message received!");
        if (messageEvent.getPath().equals("/message_path")) {
            final String message = new String(messageEvent.getData());
            Log.d(TAG, "Message reads: " + message);

            // Broadcast message
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);

        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}