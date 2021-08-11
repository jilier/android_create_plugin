package com.ttt.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.unity3d.player.UnityPlayerActivity;

public class AppReceiver extends BroadcastReceiver {
    String act = "android.intent.action.BOOT_COMPLETED";

    public AppReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, intent.getAction(), Toast.LENGTH_SHORT).show();

        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (act.equals(intent.getAction())){
            Intent startIndent = new Intent(context, UnityPlayerActivity.class);
            startIndent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startIndent);
        }
    }
}