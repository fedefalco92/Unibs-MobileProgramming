package it.unibs.appwow.services;

import android.net.Uri;

/**
 * Created by Alessandro on 19/05/2016.
 */
public final class WebServiceUri {
    public final static Uri BASE_ADDRESS = Uri.parse("http://api.bresciawebproject.it");
    public final static Uri USERS_URI = Uri.withAppendedPath(BASE_ADDRESS, "users");
    public final static Uri LOGIN_URI = Uri.withAppendedPath(USERS_URI, "login");
}
