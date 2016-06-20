package it.unibs.appwow.services;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.R;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.utils.DateUtils;

// TODO: 03/06/16 Magari da spostarla e farla diventare come sottoclasse... 
/**
 * Created by federicofalcone on 03/06/16.
 */
public class DownloadFromServerTask extends AsyncTask<Void, Void, Boolean> {

    private final String TAG_LOG = DownloadFromServerTask.class.getSimpleName(); // aggiungere static se si sposta

    private JSONArray mResjs = null;
    private boolean mConnError = false;
    private LocalUser mLocalUser = null;

    public DownloadFromServerTask(LocalUser localUser) {
        mLocalUser = localUser;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String response = "";
        Uri user_uri = Uri.withAppendedPath(WebServiceUri.USERS_URI, String.valueOf(mLocalUser.getId()));
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
                    Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }

            }
        }

        @Override
        protected void onCancelled() {
            // TODO: 27/05/2016 riempire
        }

}
