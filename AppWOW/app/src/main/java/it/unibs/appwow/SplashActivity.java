package it.unibs.appwow;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    private static final long MIN_WAIT_INTERVAL = 2000L;

    private static final String START_TIME_KEY = "it.unibs.appwow.key.START_TIME_KEY";

    private static final String TAG_LOG = SplashActivity.class.getName();

    private long mStartTime = -1L; // first visualization instant

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putLong(START_TIME_KEY, mStartTime);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        this.mStartTime = savedInstanceState.getLong(START_TIME_KEY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(savedInstanceState != null)
        {
            this.mStartTime = savedInstanceState.getLong(START_TIME_KEY);
        }

        final ImageView logoImageView = (ImageView)findViewById(R.id.splash_imageview);

        logoImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG_LOG, "ImageView touched!");
                long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
                if(elapsedTime >= MIN_WAIT_INTERVAL)
                {
                    Log.d(TAG_LOG, "OK! Let's go ahead...");
                    goAhead();
                }
                else
                {
                    Log.d(TAG_LOG, "Too much early! ");
                }
                return false;
            }
        });
        Log.d(TAG_LOG, "Activity created");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if(mStartTime == -1L)
        {
            mStartTime = SystemClock.uptimeMillis();
        }

        Log.d(TAG_LOG, "Activity started");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG_LOG, "Activity destroyed");
    }

    private void goAhead()
    {
       /* // check if the user is already logged
        final UserModel userModel = UserModel.load(this);
        Class destinationActivity = null;
        if(userModel == null) {
            // user not yet logged
            destinationActivity = FirstAccessActivity.class;
        } else {
            // user already logged
            destinationActivity = MenuActivity.class;
        }*/
        final Intent intent = new Intent(this,GroupActivity.class);
        startActivity(intent);

        /*final Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);*/
        finish();
    }

}
