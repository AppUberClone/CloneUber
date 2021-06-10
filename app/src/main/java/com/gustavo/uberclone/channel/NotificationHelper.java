package com.gustavo.uberclone.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.gustavo.uberclone.R;

public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_ID = "com.gustavo.uberclone";
    private  static  final String CHANNEL_NAME = "UberClone";

    private NotificationManager manager;


    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannels();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels(){
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(android.R.color.background_dark);
        getManager().createNotificationChannel(notificationChannel);
    }

    public   NotificationManager getManager(){
        if (manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return  manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification(String tittle, String body, PendingIntent intent, Uri soundUri){
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                 .setContentTitle(tittle)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.icon_car)
                .setStyle(new Notification.BigTextStyle().bigText(body).setBigContentTitle(tittle));

    }

    public NotificationCompat.Builder getNotificationOldApi(String tittle, String body, PendingIntent intent, Uri soundUri){
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(tittle)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.icon_car)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(tittle));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificationActions(String tittle, String body,  Uri soundUri, Notification.Action acceptAction, Notification.Action cancelAction){
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(tittle)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setSmallIcon(R.drawable.icon_car)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(new Notification.BigTextStyle().bigText(body).setBigContentTitle(tittle));
    }
    public NotificationCompat.Builder getNotificationOldApiActions(String tittle, String body,  Uri soundUri, NotificationCompat.Action acceptAction, NotificationCompat.Action cancelAction){
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(tittle)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setSmallIcon(R.drawable.icon_car)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(tittle));
    }

}
