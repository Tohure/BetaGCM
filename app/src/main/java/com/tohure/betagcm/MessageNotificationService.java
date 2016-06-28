package com.tohure.betagcm;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class MessageNotificationService extends Service {

    private String link = "", image = "", cuerpo = "",title = "", type_alert = "";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

       if (intent != null){
           Bundle extras = intent.getExtras();
           for (String key : extras.keySet()){
               switch (key){
                   case "cuerpo": cuerpo = extras.getString(key); break;
                   case "imagen": image = extras.getString(key); break;
                   case "titulo": title = extras.getString(key); break;
                   case "link": link  = extras.getString(key); break;
                   case "tipo": type_alert = extras.getString(key); break;
               }
           }


           if (title != null && !title.equals("") && image != null && !image.equals("")){
               launchNotificationWithImage();
               stopSelf();
           }else{
               if (title != null && !title.equals("") && cuerpo != null && !cuerpo.equals("")){
                   launchNotificationWithText();
                   stopSelf();
               }
           }
       }
        return START_STICKY;
    }

    private void launchNotificationWithText() {

    }

    private void launchNotificationWithImage() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
