package com.warframealertapp.other_utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.warframealertapp.R;

/**
 * Created by Cody on 11/21/2017.
 */

public class NotificationLauncher {
    private NotificationCompat.Builder mBuilder;
    private Context context;
    private int numberOfNotificationsPlayed;

    public NotificationLauncher(Context context){
        mBuilder = new NotificationCompat.Builder(context);
        this.context = context;
        this.numberOfNotificationsPlayed = 0;
    }

    //launch notification for item in an alert
    public void launchAlertNotification(String itemName){
        mBuilder.setContentTitle(context.getString(R.string.alert));
        mBuilder.setSmallIcon(R.drawable.credits);
        //requires API 16 or higher
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
        }
        mBuilder.setWhen(System.currentTimeMillis());
        Uri soundUri = RingtoneManager.getDefaultUri(Notification.DEFAULT_SOUND);
        mBuilder.setSound(soundUri);
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        //do nothing onClick
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(), 0);
        mBuilder.setContentIntent(pi);
        String contentText = itemName + " " + context.getString(R.string.available_now);
        mBuilder.setContentText(contentText);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(numberOfNotificationsPlayed, mBuilder.build());
        //numberOfNotificationsPlayed increases to give each a unique ID
        numberOfNotificationsPlayed++;
    }

    //launch notification for an item in an invasion
    public void launchInvasionNotification(String itemName){
        mBuilder.setContentTitle(context.getString(R.string.invasion));
        mBuilder.setSmallIcon(R.drawable.credits);
        //requires API 16 or higher
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
        }
        mBuilder.setWhen(System.currentTimeMillis());
        Uri soundUri = RingtoneManager.getDefaultUri(Notification.DEFAULT_SOUND);
        mBuilder.setSound(soundUri);
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        //do nothing onClick
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(), 0);
        mBuilder.setContentIntent(pi);
        String contentText = itemName + " " + context.getString(R.string.available_now);
        mBuilder.setContentText(contentText);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(numberOfNotificationsPlayed, mBuilder.build());
        //numberOfNotificationsPlayed increases to give each a unique ID
        numberOfNotificationsPlayed++;
    }
}
