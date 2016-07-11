package it.unibs.appwow.models;

import android.net.Uri;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;
import java.util.Locale;

/**
 * Created by Alessandro on 11/07/2016.
 */
public class MyPlace{
    private String mName;
    private String mAddress;
    private String mUrl;
    private LatLng mLatLng;

    public MyPlace(String name, String address, String url, LatLng latLng) {
        mName = name;
        mAddress = address;
        mUrl = url;
        mLatLng = latLng;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }
}
