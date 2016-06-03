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
import it.unibs.appwow.models.parc.Group;
import it.unibs.appwow.models.parc.User;
import it.unibs.appwow.utils.DateUtils;

/**
 * Created by Alessandro on 27/05/2016.
 */
public class SyncDataBase {
    private static final String TAG_LOG = SyncDataBase.class.getSimpleName();
}
