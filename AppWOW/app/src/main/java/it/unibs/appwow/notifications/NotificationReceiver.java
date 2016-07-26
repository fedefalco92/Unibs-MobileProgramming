package it.unibs.appwow.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import it.unibs.appwow.GroupDetailsActivity;
import it.unibs.appwow.NavigationActivity;
import it.unibs.appwow.fragments.GroupListFragment;

/**
 * Created by federicofalcone on 26/07/16.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private final String TAG_LOG = NotificationReceiver.class.getSimpleName();
    public final static String NOTIFICATION_RECEIVER_GROUP_DETAILS_UPDATER = "it.unibs.appwow.notificationReceiver.GroupDetailsUpdater";
    public final static String NOTIFICATION_RECEIVER_GROUPS_UPDATER = "it.unibs.appwow.notificationReceiver.GroupsUpdater";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG_LOG,"Intent received: " + intent.getAction());
        String intentAction = intent.getAction();
        switch (intentAction){
            case NOTIFICATION_RECEIVER_GROUP_DETAILS_UPDATER:
                if(context instanceof GroupDetailsActivity){
                    // posso richiamare dei metodi specifici di GroupDetailsActivity
                    GroupDetailsActivity activity = (GroupDetailsActivity) context;
                    Log.d(TAG_LOG,"Notification received: " + NOTIFICATION_RECEIVER_GROUP_DETAILS_UPDATER);
                    //Toast.makeText(activity, "Notification received", Toast.LENGTH_SHORT).show();
                    activity.onUpdate();
                }
                break;
            case NOTIFICATION_RECEIVER_GROUPS_UPDATER:
                if(context instanceof NavigationActivity){
                    Fragment visibleFragment = ((NavigationActivity) context).getVisibleFragment();
                    Log.d(TAG_LOG,"Notification received: " + NOTIFICATION_RECEIVER_GROUPS_UPDATER);
                    if(visibleFragment instanceof GroupListFragment){
                        visibleFragment.onResume();
                    }
                }
                break;
        }
    }
}
