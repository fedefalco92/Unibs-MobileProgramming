package it.unibs.appwow.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import it.unibs.appwow.R;


/**
 * Settings Fragment
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getString(R.string.action_settings));
    }

}
