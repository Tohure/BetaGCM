package com.tohure.betagcm;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MessageNotificationService extends Service {

    private String link = "", image = "", cuerpo = "",title = "", type_alert = "";
    Bitmap Images;

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

           new Thread(new Runnable() {
               public void run() {
                   if (title != null && !title.equals("") && image != null && !image.trim().equals("")){
                       getImageLink(image);
                       stopSelf();
                   }else{
                       if (title != null && !title.equals("") && cuerpo != null && !cuerpo.equals("")){
                           launchNotificationWithText();
                           stopSelf();
                       }
                   }
               }
           }).start();


       }
        return START_STICKY;
    }

    private void getImageLink(String link_image) {
        Images = getBitmapFromURL(link_image);
        launchNotificationWithImage(Images);
    }

    private void launchNotificationWithText() {
        MessageNotificationText.notify(this,title,cuerpo,link,0);
    }

    private void launchNotificationWithImage(Bitmap images) {
        MessageNotificationImage.notify(this,title,cuerpo,link,images,0);
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
