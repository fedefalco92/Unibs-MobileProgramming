package it.unibs.appwow.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Set;

import it.unibs.appwow.NavigationActivity;
import it.unibs.appwow.R;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private final String TAG_LOG = FirebaseInstanceIDService.class.getSimpleName();

    private static String KEY_GROUP = "key";
    private static int notificationID = 0;

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d(TAG_LOG,"onDeletedMessages()");
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.d(TAG_LOG,"onMessageSent()");
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
        Log.d(TAG_LOG,"onSendError()");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String text = new String();
        //showNotification(remoteMessage.getData().get("message"));
        Set<String> keys = remoteMessage.getData().keySet();
        for(String str : keys){
            text += str + " -> " + remoteMessage.getData().get(str) + "--";
        }
        Log.d("LOG", text);
        showNotification(text);
    }

    private void showNotification(String message) {

        Intent i = new Intent(this, NavigationActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle((notificationID)+" new messages")
                .setContentText(message)
                //.setGroup(KEY_GROUP)
                //.addLine(message)
                //.setGroupSummary((notificationID==0))
                //.setStyle(new NotificationCompat.InboxStyle()
                //  .addLine(message)
                //    .setBigContentTitle(notificationID+" new messages")
                //    .setSummaryText("Summary Text"))
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        notificationManager.notify(notificationID++, notification);


        // vibration for 800 milliseconds
        ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(100);

    }

    @Override
    public boolean stopService(Intent name) {
        Log.d("LOG","Service msg Stopped");
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("NotificationID", "Destroyed");
    }
}

