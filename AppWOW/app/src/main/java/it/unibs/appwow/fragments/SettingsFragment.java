package it.unibs.appwow.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import it.unibs.appwow.R;


/**
 * Created by federicofalcone on 12/07/16.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private final String TAG_LOG = SettingsFragment.class.getSimpleName();

    SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_LOG,"onCreate");

    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        Log.d(TAG_LOG,"onCreatePreferences");
        //add xml
        addPreferencesFromResource(R.xml.preferences);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

}
