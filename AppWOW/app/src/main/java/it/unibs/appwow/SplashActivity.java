package it.unibs.appwow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import it.unibs.appwow.database.AppSQLiteHelper;
import it.unibs.appwow.model.parc.User;
import it.unibs.appwow.services.WebServiceUri;

public class SplashActivity extends AppCompatActivity {

    private static final long MIN_WAIT_INTERVAL = 1000L;

    private static final String START_TIME_KEY = "it.unibs.appwow.key.START_TIME_KEY";

    private static final String TAG_LOG = SplashActivity.class.getName();

    private long mStartTime = -1L; // first visualization instant

    private DownloadFromServerTask mDownloadTask;

   // private AppSQLiteHelper database;
   // private SQLiteDatabase db;

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
       // db.close();
        Log.d(TAG_LOG, "Activity destroyed");
    }

    private void goAhead()
    {
        /* commento*/
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
        //final Intent intent = new Intent(this,GroupActivity.class);
        //database = new AppSQLiteHelper(getApplicationContext());
      //  db = database.getWritableDatabase();
      //  if(db != null) {
        /*Class destinationClass = null;
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        String userId = sharedPreferences.getString("logged_in",null);
        if(userId != null) {
            destinationClass = NavigationActivity.class;
        }
        else{
            destinationClass = LoginActivity.class;
        }
        final Intent intent = new Intent(this, destinationClass);
        startActivity(intent);
        finish();
        */
        final User userModel = User.load(this);
        //final User userModel = User.create(1);
        Class destinationActivity = null;
        if(userModel == null) {
            // user not yet logged
            destinationActivity = LoginActivity.class;
        } else {
            // user already logged
            destinationActivity = NavigationActivity.class;

            //// TODO: 27/05/2016 lettura database remoto
            mDownloadTask = new DownloadFromServerTask(userModel);
            mDownloadTask.execute((Void) null);
        }

        final Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
        finish();
    }

    public class DownloadFromServerTask extends AsyncTask<Void, Void, Boolean> {

        private JSONObject mResjs = null;
        private boolean mNewUser = false;
        private boolean mConnError = false;
        private User mUser = null;

        DownloadFromServerTask(User user) {
            mUser = user;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String response = "";
            Uri user_uri = Uri.withAppendedPath(WebServiceUri.USERS_URI, String.valueOf(mUser.getId()));
            Uri groups_uri = Uri.withAppendedPath(user_uri, "groups");
            try {
                URL url = new URL(groups_uri.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder();
                        //.appendQueryParameter("email", mEmail)
                        //.appendQueryParameter("password", mPassword);
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();
                int responseCode = conn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    String line = "";
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                } else {
                    response = "";
                }
                if(!response.isEmpty()){
                    //response = response.substring(1,response.length()-1);
                    mResjs = new JSONObject(response);
                    Log.d("risposta", mResjs.toString(1));
                } else {
                    return false;
                }
                Log.d("RISPOSTA_STRING", response);

                // TODO: 19/05/2016 salvare nel database locale


            } catch (MalformedURLException e){
                return false;
            } catch (IOException e){
                return false;
            } catch (JSONException e){
                e.printStackTrace();
                return false;
            }
            return true;


        }

        @Override
        protected void onPostExecute(final Boolean success) {
            // TODO: 27/05/2016 RIEMPIRE
            if (success) {

            } else {
                if(!mConnError){

                } else {
                    Toast.makeText(getBaseContext(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }

            }
        }

        @Override
        protected void onCancelled() {
            // TODO: 27/05/2016 riempire
        }

    }



}
