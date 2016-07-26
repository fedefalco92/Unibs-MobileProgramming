package it.unibs.appwow.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import it.unibs.appwow.GroupDetailsActivity;
import it.unibs.appwow.NavigationActivity;
import it.unibs.appwow.fragments.GroupListFragment;

/**
 * Created by federicofalcone on 26/07/16.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private final String TAG_LOG = NotificationReceiver.class.getSimpleName();
    public final static String NOTIFICATION_RECEIVER = "it.unibs.appwow.notificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG_LOG,"Intent received: " + intent.getAction());
        String intentAction = intent.getAction();
        switch (intentAction){
            case NOTIFICATION_RECEIVER:
                if(context instanceof GroupDetailsActivity){
                    // posso richiamare dei metodi specifici di GroupDetailsActivity
                    GroupDetailsActivity activity = (GroupDetailsActivity) context;
                    Log.d(TAG_LOG,"Notification received");
                    Toast.makeText(activity, "Notification received", Toast.LENGTH_SHORT).show();
                    activity.onUpdate();
                } else if(context instanceof NavigationActivity){
                    Fragment visibleFragment = ((NavigationActivity) context).getVisibleFragment();
                    if(visibleFragment instanceof GroupListFragment){
                        visibleFragment.onResume();
                    }
                }
                break;
        }
    }
}
