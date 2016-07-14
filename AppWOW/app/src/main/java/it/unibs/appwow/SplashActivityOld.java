package it.unibs.appwow;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.DownloadFromServerTask;

public class SplashActivityOld extends AppCompatActivity {

    private static final String TAG_LOG = SplashActivityOld.class.getSimpleName();

    private static final long MIN_WAIT_INTERVAL = 1000L;

    private static final String START_TIME_KEY = "it.unibs.appwow.key.START_TIME_KEY";

    private long mStartTime = -1L; // first visualization instant

    private DownloadFromServerTask mDownloadTask;

    private LocalUser mUserModel;
    private boolean mDownloaded = false; // Serve?

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
        mUserModel = LocalUser.load(MyApplication.getAppContext());
        Log.d(TAG_LOG, "LocalUser " + mUserModel==null?"null":"not null");
        /*if(mUserModel != null){
            mDownloadTask = new DownloadFromServerTask(mUserModel);
            mDownloadTask.execute((Void) null);
            Log.d(TAG_LOG, "Everything is downloaded. Go ahead... ");
            // In questo modo non appena ha caricato va ai gruppi. Dà l'impressione di essere più veloce.
            goAhead();
        }*/

        logoImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG_LOG, "ImageView touched!");
                long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
                if(elapsedTime >= MIN_WAIT_INTERVAL) {
                    Log.d(TAG_LOG, "OK! Let's go ahead...");
                    goAhead();
                } else {
                    Log.d(TAG_LOG, "Too much early! ");
                }
                return false;
            }
        });


        Log.d(TAG_LOG, "Activity created");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mStartTime == -1L)
        {
            mStartTime = SystemClock.uptimeMillis();
        }

        Log.d(TAG_LOG, "Activity started");

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
       // db.close();
        Log.d(TAG_LOG, "Activity destroyed");
    }

    private void goAhead() {
        //final LocalUser userModel = LocalUser.load(this);
        //final LocalUser userModel = LocalUser.create(1);
        Class destinationActivity = null;
        if(mUserModel == null) {
            // user not yet logged
            destinationActivity = LoginActivity.class;
        } else {
            // user already logged
            destinationActivity = NavigationActivity.class;
        }

        final Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
        finish();
    }

    /*
    public class DownloadFromServerTask extends AsyncTask<Void, Void, Boolean> {
        private final String TAG_LOG = DownloadFromServerTask.class.getSimpleName(); // aggiungere static se si sposta

        private JSONArray mResjs = null;
        private boolean mConnError = false;
        private LocalUser mUser = null;

        DownloadFromServerTask(LocalUser user) {
            mUser = user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String response = "";
            Uri user_uri = Uri.withAppendedPath(WebServiceUri.USERS_URI, String.valueOf(mUser.getUserId()));
            Uri groups_uri = Uri.withAppendedPath(user_uri, "groups");
            try {

                URL url = new URL(groups_uri.toString());
                //Log.d(TAG_LOG,"URL" + url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                //conn.setDoOutput(true);
                /*
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
                */
                /*
                conn.connect();
                int responseCode = conn.getResponseCode();
                Log.d(TAG_LOG,"Response code = " + responseCode);
                if(responseCode == HttpURLConnection.HTTP_OK){
                    String line = "";
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                } else {
                    mConnError = true;
                    response = "";
                }
                if(!response.isEmpty()){
                    //response = response.substring(1,response.length()-1);
                    mResjs = new JSONArray(response);
                    Log.d(TAG_LOG,"response=" + mResjs.toString(1));
                    //riempio il database locale
                    GroupDAO dao = new GroupDAO();
                    dao.open();
                    for(int i = 0; i < mResjs.length(); i++){
                        JSONObject groupJs = mResjs.getJSONObject(i);
                        int id = groupJs.getInt("id");
                        String server_updated_at_string = groupJs.getString("updated_at");
                        long server_updated_at = DateUtils.dateStringToLong(server_updated_at_string);
                        long local_updated_at = dao.getUpdatedAt(id);
                        //aggiorno il gruppo solo se ha subito modifiche
                        if(server_updated_at > local_updated_at){
                            String name = groupJs.getString("name");
                            int idAdmin = groupJs.getInt("idAdmin");
                            String created_at_string = groupJs.getString("created_at");
                            long created_at = DateUtils.dateStringToLong(created_at_string);
                            //JSONObject pivot = groupJs.getJSONObject("pivot");
                            GroupModel group = GroupModel.create(name).withId(id).withAdmin(idAdmin);
                            group.setCreatedAt(created_at);
                            group.setUpdatedAt(server_updated_at);
                            group.highlight();
                            dao.insertGroup(group);
                            //boolean highlighted = dao.highlightGroup(id);
                            //if(highlighted) Log.d(TAG_LOG,"HIGHLIGHT -"+ "Gruppo " + id + " highlighted");
                        } else{
                            //per ora non faccio niente
                        }
                    }
                    dao.close();
                } else {
                    return false;
                }
                Log.d(TAG_LOG, "RISPOSTA_STRING" + response);

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
            if (success) {
                // FIXME: 03/06/2016 cosa fare in caso di success? per esempio mostrare un messaggio con il numero di gruppi aggiornati
            } else {
                if(!mConnError){
                    // TODO: 03/06/2016 differenziare errori
                    Toast.makeText(getBaseContext(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }

            }

            mDownloaded = true;
        }

        @Override
        protected void onCancelled() {
            // TODO: 27/05/2016 riempire
        }

    }
*/


}