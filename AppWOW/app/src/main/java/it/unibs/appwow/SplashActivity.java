package it.unibs.appwow;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.notifications.FirebaseInstanceIDService;

/**
 * Created by Alessandro on 14/07/2016.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalUser user = LocalUser.load(this);
        Class destinationActivity = null;
        if(user == null) {
            // user not yet logged
            destinationActivity = LoginActivity.class;
        } else {
            // user already logged
            destinationActivity = NavigationActivity.class;
        }

        final Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
        //startService(new Intent(this, FirebaseInstanceIDService.class));
        finish();
    }

}
