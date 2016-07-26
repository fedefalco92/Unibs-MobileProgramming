package it.unibs.appwow.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.GroupDetailsActivity;
import it.unibs.appwow.NavigationActivity;
import it.unibs.appwow.R;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.fragments.GroupListFragment;
import it.unibs.appwow.models.parc.GroupModel;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private final String TAG_LOG = FirebaseMessagingService.class.getSimpleName();

    private static String KEY_GROUP = "key";
    private static int notificationID = 0;

    private static List<String> messages = new ArrayList<>();

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
        String type = remoteMessage.getData().get("type");
        try {
            JSONObject obj = new JSONObject(remoteMessage.getData().get("message"));
            showNotification(type, obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showNotification(String type, JSONObject msgObj) throws JSONException{
        String title = new String();
        String message = new String();
        String param = new String();
        Resources res = getResources();

        Intent i = new Intent(this, NavigationActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(NavigationActivity.class);
        stackBuilder.addNextIntent(i);

        // Preparazione gestione intent
        GroupDAO dao = new GroupDAO();
        GroupModel gm;
        Intent iGroupDetailsActivity = new Intent(this, GroupDetailsActivity.class);

        Intent updateIntentBroadcast = null;

        NotificationTypes t = NotificationTypes.valueOf(type);
        switch (t){
            case GROUP_CREATED:
                title = res.getString(R.string.notification_group_created);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_group_created_msg, Html.fromHtml("<br>" + param + "</br>"));
                updateIntentBroadcast = new Intent(NotificationReceiver.NOTIFICATION_RECEIVER_GROUPS_UPDATER);
                sendBroadcast(updateIntentBroadcast);
                break;
            case GROUP_MODIFIED:
                title = res.getString(R.string.notification_group_modified);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_group_modified_msg, Html.fromHtml("<br>" + param + "</br>"));
                updateIntentBroadcast = new Intent(NotificationReceiver.NOTIFICATION_RECEIVER_GROUPS_UPDATER);
                sendBroadcast(updateIntentBroadcast);
                break;
            case GROUP_DELETED:
                title = res.getString(R.string.notification_group_deleted);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_group_deleted_msg, Html.fromHtml("<br>" + param + "</br>"));
                updateIntentBroadcast = new Intent(NotificationReceiver.NOTIFICATION_RECEIVER_GROUPS_UPDATER);
                sendBroadcast(updateIntentBroadcast);
                break;
            case PAYMENT_CREATED:
                title = res.getString(R.string.notification_payment_created);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_payment_created_msg, Html.fromHtml("<br>" + param + "</br>"));

                // Gestione Intent
                dao.open();
                gm = dao.getSingleGroup(msgObj.getInt("idGroup"));
                dao.close();

                iGroupDetailsActivity.putExtra(GroupListFragment.PASSING_GROUP_TAG, gm);
                stackBuilder.addNextIntent(iGroupDetailsActivity);

                updateIntentBroadcast = new Intent(NotificationReceiver.NOTIFICATION_RECEIVER_GROUP_DETAILS_UPDATER);
                sendBroadcast(updateIntentBroadcast);
                break;
            case PAYMENT_MODIFIED:
                // FIXME: 26/07/16 Problema con pagamento #
                title = res.getString(R.string.notification_payment_modified);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_payment_modified_msg, Html.fromHtml("<br>" + param + "</br>"));

                // Gestione Intent
                dao.open();
                gm = dao.getSingleGroup(msgObj.getInt("idGroup"));
                dao.close();

                iGroupDetailsActivity.putExtra(GroupListFragment.PASSING_GROUP_TAG, gm);
                stackBuilder.addNextIntent(iGroupDetailsActivity);

                updateIntentBroadcast = new Intent(NotificationReceiver.NOTIFICATION_RECEIVER_GROUP_DETAILS_UPDATER);
                sendBroadcast(updateIntentBroadcast);
                break;
            case PAYMENT_DELETED:
                title = res.getString(R.string.notification_payment_deleted);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_payment_deleted_msg, Html.fromHtml("<br>" + param + "</br>"));

                // Gestione Intent
                dao.open();
                gm = dao.getSingleGroup(msgObj.getInt("idGroup"));
                dao.close();

                iGroupDetailsActivity.putExtra(GroupListFragment.PASSING_GROUP_TAG, gm);
                stackBuilder.addNextIntent(iGroupDetailsActivity);

                updateIntentBroadcast = new Intent(NotificationReceiver.NOTIFICATION_RECEIVER_GROUP_DETAILS_UPDATER);
                sendBroadcast(updateIntentBroadcast);
                break;
            case DEBT_SOLVED:
                title = res.getString(R.string.notification_debt_solved);
                message = res.getString(R.string.notification_debt_solved_msg);

                // Gestione Intent
                dao.open();
                gm = dao.getSingleGroup(msgObj.getInt("idGroup"));
                dao.close();

                iGroupDetailsActivity.putExtra(GroupListFragment.PASSING_GROUP_TAG, gm);
                stackBuilder.addNextIntent(iGroupDetailsActivity);

                updateIntentBroadcast = new Intent(NotificationReceiver.NOTIFICATION_RECEIVER_GROUP_DETAILS_UPDATER);
                sendBroadcast(updateIntentBroadcast);
                break;

            case BROADCAST_NOTIFICATION:
                break;
            default:
                break;
        }

        // Grouping notifications
        messages.add(message);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for(String str: messages){
            inboxStyle.addLine(Html.fromHtml(str));
        }
        boolean summary = notificationID == 0;
        inboxStyle.setBigContentTitle(getString(R.string.app_name));
        inboxStyle.setSummaryText(getResources().getQuantityString(R.plurals.notification_counter,notificationID+1,notificationID+1));


        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setTicker(getResources().getString(R.string.app_name))
                .setDefaults(Notification.DEFAULT_ALL)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_new))
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(title)
                .setSubText(message)
                //.setGroup(group)
                .setGroupSummary(summary)
                .setStyle(inboxStyle)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setSmallIcon(R.mipmap.ic_launcher_new)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean switchOn = sp.getBoolean("pref_key_notification",true);
        if(switchOn){
            notificationID++;
            notificationManager.notify(0, notification);
            // vibration for 800 milliseconds
            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(100);
        }

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

