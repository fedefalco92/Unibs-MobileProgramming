package it.unibs.appwow.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

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

        NotificationTypes t = NotificationTypes.valueOf(type);
        switch (t){
            case GROUP_CREATED:
                title = res.getString(R.string.notification_group_created);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_group_created_msg, param);
                break;
            case GROUP_MODIFIED:
                title = res.getString(R.string.notification_group_modified);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_group_modified_msg, param);
                break;
            case GROUP_DELETED:
                title = res.getString(R.string.notification_group_deleted);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_group_deleted_msg, param);
                break;
            case PAYMENT_CREATED:
                title = res.getString(R.string.notification_payment_created);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_payment_created_msg, param);

                // Gestione Intent
                dao.open();
                gm = dao.getSingleGroup(msgObj.getInt("idGroup"));
                dao.close();

                iGroupDetailsActivity.putExtra(GroupListFragment.PASSING_GROUP_TAG, gm);
                stackBuilder.addNextIntent(iGroupDetailsActivity);

                break;
            case PAYMENT_MODIFIED:
                title = res.getString(R.string.notification_payment_modified);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_payment_modified_msg, param);

                // Gestione Intent
                dao.open();
                gm = dao.getSingleGroup(msgObj.getInt("idGroup"));
                dao.close();

                iGroupDetailsActivity.putExtra(GroupListFragment.PASSING_GROUP_TAG, gm);
                stackBuilder.addNextIntent(iGroupDetailsActivity);

                break;
            case PAYMENT_DELETED:
                title = res.getString(R.string.notification_payment_deleted);
                param = msgObj.getString("name");
                message = res.getString(R.string.notification_payment_deleted_msg, param);

                // Gestione Intent
                dao.open();
                gm = dao.getSingleGroup(msgObj.getInt("idGroup"));
                dao.close();

                iGroupDetailsActivity.putExtra(GroupListFragment.PASSING_GROUP_TAG, gm);
                stackBuilder.addNextIntent(iGroupDetailsActivity);

                break;
            case DEBT_SOLVED:
                title = res.getString(R.string.notification_debt_solved);

                // Ritorno un special payment
                /*
                String paramFrom = msgObj.getString("idUser");
                Double amount = msgObj.getDouble("amount");
                String currency = msgObj.getString("currency");
                String paramTo = msgObj.getString("idTo");
                message = res.getString(R.string.notification_debt_solved_msg, paramFrom, amount, currency, paramTo);
                */

                message = res.getString(R.string.notification_debt_solved_msg);

                // Gestione Intent
                dao.open();
                gm = dao.getSingleGroup(msgObj.getInt("idGroup"));
                dao.close();

                iGroupDetailsActivity.putExtra(GroupListFragment.PASSING_GROUP_TAG, gm);
                stackBuilder.addNextIntent(iGroupDetailsActivity);
                break;

            case BROADCAST_NOTIFICATION:
                break;
            default:
                break;
        }

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
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setSmallIcon(R.mipmap.ic_launcher_new)
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

