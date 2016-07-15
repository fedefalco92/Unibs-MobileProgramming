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
import it.unibs.appwow.services.WebServiceRequest;


public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private final String TAG_LOG = FirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(TAG_LOG,"ID service created");
        super.onCreate();
    }

    @Override
    public void onTokenRefresh() {
        Log.d(TAG_LOG,"Token generated");
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG_LOG,"Token generated: " + token);
       /* String token2 = FirebaseInstanceId.getInstance().getToken(); --> Sono uguali, fino al momento del delete instance
        Log.d(TAG_LOG,"Token generated 2: " + token2);*/
        registerToken(token);
    }

    private void registerToken(String token) {
        String[] keys = {"Token"};
        String[] values = {token};

        Map<String,String> map = WebServiceRequest.createParametersMap(keys,values);

        //StringRequest request = WebServiceRequest.stringRequest(Request.Method.POST,"api.bresciawebproject.it/register.php",map,null,null);

        StringRequest request = WebServiceRequest.stringRequest(Request.Method.POST, "http://charmed92.altervista.org/fcm/register.php", map,
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
}
