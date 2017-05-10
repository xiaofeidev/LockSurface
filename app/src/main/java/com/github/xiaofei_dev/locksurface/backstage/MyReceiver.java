package com.github.xiaofei_dev.locksurface.backstage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.xiaofei_dev.locksurface.backstage.LockService;


public final class MyReceiver extends BroadcastReceiver {
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    public static final String TAG = "MyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean isCheckedBoot = mSharedPreferences.getBoolean("IS_CHECKED_BOOT",true);
        Long time = mSharedPreferences.getLong(LockService.TIME,-1);
        if (intent.getAction().equals(ACTION) && isCheckedBoot && time > 0) {
            Intent lockIntent = new Intent(context,LockService.class);
            lockIntent.putExtra(LockService.TYPE,TAG);
            context.startService(lockIntent);
        }
    }
}
