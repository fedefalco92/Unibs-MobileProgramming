package it.unibs.appwow.notifications;


import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;


public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private final String TAG_LOG = FirebaseInstanceIDService.class.getSimpleName();

    private static String mToken;

    @Override
    public void onCreate() {
        Log.d(TAG_LOG,"ID service created");
        super.onCreate();
    }

    @Override
    public void onTokenRefresh() {
        Log.d(TAG_LOG,"Token generated");
        mToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG_LOG,"Token generated: " + mToken);
       /* String token2 = FirebaseInstanceId.getInstance().getToken(); --> Sono uguali, fino al momento del delete instance
        Log.d(TAG_LOG,"Token generated 2: " + token2);*/
        LocalUser localUser = LocalUser.load(MyApplication.getAppContext());
        if(localUser != null){
            registerToken(localUser.getId(),mToken);
        }

    }

    private void registerToken(int user_id, String token) {
        String[] keys = {"id","msg_token"};
        String[] values = {String.valueOf(user_id),token};

        Map<String,String> map = WebServiceRequest.createParametersMap(keys,values);

        //StringRequest request = WebServiceRequest.stringRequest(Request.Method.POST,"api.bresciawebproject.it/register.php",map,null,null);

        StringRequest request = WebServiceRequest.stringRequest(Request.Method.POST, WebServiceUri.REGISTER_TOKEN_URI.toString(), map,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG_LOG,"Token registrato correttamente");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        MyApplication.getInstance().addToRequestQueue(request);
    }

    @Override
    public boolean stopService(Intent name) {
        /*try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        Log.d("LOG","Service token Stopped");
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("InstanceID", "Destroyed");
    }

    public static String getToken() {
        return mToken;
    }
}
