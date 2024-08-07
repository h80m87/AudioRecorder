package com.example.audiorecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean check = intent.getBooleanExtra("Start",false);

        Intent i = new Intent(context,MyService.class);
        i.putExtra("Start", !check);
        context.startForegroundService(i);

    }
}