package it.unibs.appwow.services;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Alessandro on 19/05/2016.
 */
public final class WebServiceUri {
    public final static Uri BASE_ADDRESS = Uri.parse("http://api.bresciawebproject.it");
    public final static Uri USERS_URI = Uri.withAppendedPath(BASE_ADDRESS, "users");
    public final static Uri LOGIN_URI = Uri.withAppendedPath(USERS_URI, "login");
    public final static Uri CHECK_USER_URI = Uri.withAppendedPath(USERS_URI, "check-user");
    public final static Uri GROUPS_URI = Uri.withAppendedPath(BASE_ADDRESS, "groups");

    public final static Uri getGroupUri (int idGroup){
        return Uri.withAppendedPath(GROUPS_URI, String.valueOf(idGroup));
    }

    public final static Uri getGroupUsersUri(int idGroup){
        Uri group_uri = Uri.withAppendedPath(GROUPS_URI, String.valueOf(idGroup));
        return Uri.withAppendedPath(group_uri,"users");
    }

    public final static Uri getGroupBalancingsUri(int idGroup){
        Uri group_uri = Uri.withAppendedPath(GROUPS_URI, String.valueOf(idGroup));
        return Uri.withAppendedPath(group_uri,"balancings");
    }

    public final static Uri getGroupCostsUri(int idGroup){
        Uri group_uri = Uri.withAppendedPath(GROUPS_URI, String.valueOf(idGroup));
        return Uri.withAppendedPath(group_uri,"costs");
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
}
