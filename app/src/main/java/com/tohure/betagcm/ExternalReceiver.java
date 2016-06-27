package com.tohure.betagcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ExternalReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Intent intentNotification = new Intent(context, MessageNotificationService.class);
            intentNotification.putExtras(intent.getExtras());
            context.startService(intentNotification);
        }
    }
}
