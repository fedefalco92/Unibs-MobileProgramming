package it.unibs.appwow.services;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.jar.Manifest;

import it.unibs.appwow.MyApplication;

/**
 * Created by Alessandro on 19/05/2016.
 */
public final class WebServiceUri {
    private static final String TAG_LOG = WebServiceUri.class.getSimpleName();

    public static final int SERVER_ERROR = 1;
    public static final int NETWORK_ERROR = 2;

    public final static Uri BASE_ADDRESS = Uri.parse("http://api.bresciawebproject.it");
    public final static Uri USERS_URI = Uri.withAppendedPath(BASE_ADDRESS, "users");
    public final static Uri LOGIN_URI = Uri.withAppendedPath(USERS_URI, "login");
    public final static Uri CHECK_USER_URI = Uri.withAppendedPath(USERS_URI, "check-user");
    public final static Uri GROUPS_URI = Uri.withAppendedPath(BASE_ADDRESS, "groups");
    public final static Uri PAYMENTS_URI = Uri.withAppendedPath(BASE_ADDRESS, "payments");
    public final static Uri REGISTER_TOKEN_URI = Uri.withAppendedPath(USERS_URI, "register-token");
    private final static String PLACES_DETAILS_BASE_URL = "https://maps.googleapis.com/maps/api/place/details/json?key=%s&placeid=%s&language=%s";

    public final static Uri getGroupUri (int idGroup){
        return Uri.withAppendedPath(GROUPS_URI, String.valueOf(idGroup));
    }

    public final static Uri getGroupUsersUri(int idGroup){
        Uri group_uri = Uri.withAppendedPath(GROUPS_URI, String.valueOf(idGroup));
        return Uri.withAppendedPath(group_uri,"users");
    }

    public final static Uri getGroupPaymentsUri(int idGroup){
        Uri group_uri = Uri.withAppendedPath(GROUPS_URI, String.valueOf(idGroup));
        return Uri.withAppendedPath(group_uri,"payments");
    }

    public final static Uri getGroupDebtsUri(int idGroup){
        Uri group_uri = Uri.withAppendedPath(GROUPS_URI, String.valueOf(idGroup));
        return Uri.withAppendedPath(group_uri,"debts");
    }

    public final static Uri getGroupPhotosUri(int idGroup){
        Uri group_uri = Uri.withAppendedPath(GROUPS_URI, String.valueOf(idGroup));
        return Uri.withAppendedPath(group_uri,"photo");
    }

    public final static Uri getGroupsUri(int idUser){
        Uri user_uri = Uri.withAppendedPath(WebServiceUri.USERS_URI, String.valueOf(idUser));
        return Uri.withAppendedPath(user_uri, "groups");
    }

    public final static Uri getPaymentUri(int idPayment){
        return Uri.withAppendedPath(PAYMENTS_URI, String.valueOf(idPayment));
    }

    public final static Uri getGroupResetUri(int idGroup){
        return Uri.withAppendedPath(getGroupUri(idGroup), "reset");
    }

    public final static Uri getAddGroupMemberUri(int idGroup){
        return Uri.withAppendedPath(getGroupUri(idGroup), "add-user");
    }

    public final static Uri getRemoveGroupMemberUri(int idGroup){
        return Uri.withAppendedPath(getGroupUri(idGroup), "remove-user");
    }

    public final static Uri getUserUri(int idUser){
        return Uri.withAppendedPath(USERS_URI,String.valueOf(idUser));
    }

    public final static Uri getUserPlaceUri(int idUser){
        Uri user_uri = Uri.withAppendedPath(WebServiceUri.USERS_URI, String.valueOf(idUser));
        return Uri.withAppendedPath(user_uri, "all-places");
    }

    public static URL uriToUrl(Uri uri){
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static Uri getDeletePaymentUri(int id) {
        return Uri.withAppendedPath(PAYMENTS_URI, String.valueOf(id));
    }

    public static String getPlaceDetailsUri(Context context, String placeId){
        String myApiKey = "";
        String language = "";
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            myApiKey = bundle.getString("com.google.android.geo.API_KEY");
            language = Locale.getDefault().getLanguage();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG_LOG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG_LOG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }

        return String.format(PLACES_DETAILS_BASE_URL, myApiKey, placeId, language);

    }


}
