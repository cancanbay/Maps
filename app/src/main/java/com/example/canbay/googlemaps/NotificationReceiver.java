package com.example.canbay.googlemaps;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.support.v4.app.NotificationCompat;

/**
 * Created by canbay on 7.05.2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotification(context,"Are you around?","Do not forget to visit us!","Welcome to 2 Eylül Campus");
    }

    public void createNotification(Context context,String msg,String msgText,String msgAlert){
        PendingIntent notificationIntent = PendingIntent.getActivity(context,0,new Intent(context,MapsActivity.class),0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder((context))
                .setSmallIcon(R.drawable.ic_place_black_24dp)
                .setContentTitle(msg)
                .setTicker(msgAlert)
                .setContentText(msgText);
        mBuilder.setContentIntent(notificationIntent);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        mBuilder.setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,mBuilder.build());
    }
}
