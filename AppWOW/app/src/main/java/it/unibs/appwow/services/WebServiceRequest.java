package it.unibs.appwow.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import it.unibs.appwow.MyApplication;

/**
 * Created by Alessandro on 19/05/2016.
 */
public class WebServiceRequest {

    public static JsonObjectRequest objectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> onResponseListener, Response.ErrorListener errListener) {
        return new JsonObjectRequest(method, url, jsonRequest,onResponseListener ,errListener);
    }

    public static JsonArrayRequest arrayRequest(String url, Response.Listener<JSONArray> onResponseListener, Response.ErrorListener errListener) {
        return new JsonArrayRequest(url,onResponseListener ,errListener);
    }

    public static StringRequest stringRequest(int method, String url, Response.Listener<String> onResponseListener, Response.ErrorListener errListener){
        return new StringRequest(method,url,onResponseListener,errListener);
    }

    public static StringRequest stringRequest(int method, String url, final Map<String,String> params,  Response.Listener<String> onResponseListener, Response.ErrorListener errListener){
        return new StringRequest(method,url,onResponseListener,errListener){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
    }

    public static Map<String,String> createParametersMap(String[] keys, String[] values){
        Map<String,String> map = new HashMap<String,String>();

        if(keys.length != values.length)
            return null;

        for(int i=0; i<keys.length;i++){
            map.put(keys[i],values[i]);
        }

        return map;
    }

    public static boolean checkNetwork(){
        ConnectivityManager cm = (ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}

