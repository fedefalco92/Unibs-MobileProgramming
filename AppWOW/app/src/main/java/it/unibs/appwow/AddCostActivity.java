package it.unibs.appwow;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.fragments.CostsFragment;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.AmountDetailsUtils;
import it.unibs.appwow.utils.DecimalDigitsInputFilter;
import it.unibs.appwow.utils.PositionUtils;
import it.unibs.appwow.utils.Validator;

public class AddCostActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private final String TAG_LOG = AddCostActivity.class.getSimpleName();

    private static final int REQUEST_PLACE_PICKER = 1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;

    private LocalUser mUser;
    private GroupModel mGroup;
    private Place mPlace;
    private HashMap<Integer,UserModel> mGroupUsers;

    private EditText mCostName;
    private EditText mCostAmount;
    private EditText mCostNotes;
    private EditText mCostPositionText;
    private Button mAddPositionButton;
    private MapFragment mMapFragment;
    private GoogleMap mMap;

    //private Button mAddCostButton;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cost);
        setTitle(getString(R.string.add_cost_activity_title));

        mUser = LocalUser.load(this);
        mGroup = getIntent().getParcelableExtra(CostsFragment.PASSING_GROUP_TAG);

        UserGroupDAO dao = new UserGroupDAO();
        dao = new UserGroupDAO();
        dao.open();
        mGroupUsers = dao.getAllUsers(mGroup.getId());
        dao.close();

        mPlace = null;

        mCostName = (EditText) findViewById(R.id.add_cost_name);

        mCostAmount = (EditText) findViewById(R.id.add_cost_amount);
        mCostAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});

        mCostNotes = (EditText) findViewById(R.id.add_cost_notes);

        mCostPositionText = (EditText) findViewById(R.id.add_cost_position_text);

        mAddPositionButton = (Button) findViewById(R.id.add_cost_add_position_button);
        mAddPositionButton.setOnClickListener(this);

        mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.add_cost_position_map_fragment));
        mMapFragment.getView().setVisibility(View.GONE);

        //mAddCostButton = (Button) findViewById(R.id.add_cost_add_button);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_cost, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.add_cost_menu_item:
                boolean nomeOk = Validator.isCostNameValid(mCostName.getText().toString());
                boolean amountOk = Validator.isAmountValid(mCostAmount.getText().toString());
                if(nomeOk && amountOk){
                    sendPostRequest();
                    // TODO: 22/06/2016 IMPLEMENTARE CARICAMENTO SU SERVER con richiesta volley
                    Toast.makeText(AddCostActivity.this, "eheh pensavi che funzionasse...",
                            Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    if(!nomeOk){
                        mCostName.setError(getString(R.string.error_invalid_cost_name));
                        mCostName.requestFocus();
                    }
                    if(!amountOk){
                        mCostAmount.setError(getString(R.string.error_invalid_amount));
                        mCostAmount.requestFocus();
                    }
                }

            default:
                return true;
        }
    }

    private void sendPostRequest() {
        String[] keys = {"idGroup", "idUser", "amount", "name", "notes", "position", "amount_details"};
        String idGroup = String.valueOf(mGroup.getId());
        String idUser = String.valueOf(mUser.getId());
        double amountDouble = new Double(mCostAmount.getText().toString());
        String amount = String.valueOf(amountDouble);
        String name = mCostName.getText().toString();
        String notes = mCostNotes.getText().toString();
        String position;
        if(mPlace!= null){
            position = PositionUtils.encodePositionId(mPlace.getId());
        } else {
            position = mCostPositionText.getText().toString();
        }
        String amount_details = computeAmountDetails(mUser.getId(), amountDouble);
        String[] values = {idGroup, idUser, amount, name, notes, position, amount_details};

        Map<String, String> requestParams = WebServiceRequest.createParametersMap(keys, values);
        StringRequest postRequest = WebServiceRequest.
                stringRequest(Request.Method.POST, WebServiceUri.COSTS_URI.toString(), requestParams, responseListener(), responseErrorListener());
        MyApplication.getInstance().addToRequestQueue(postRequest);
    }

    private Response.Listener<String> responseListener() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // TODO: 29/06/2016 SUCCESSO --> annullare progressbar
                if (!response.isEmpty()) {
                    Toast.makeText(AddCostActivity.this, "COSTO INSERITO CON SUCCESSO", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddCostActivity.this, "ERRORE NELLA RICHIESTA (RISPOSTA VUOTA)", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private Response.ErrorListener responseErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e("Error",error.getMessage());
                Toast.makeText(AddCostActivity.this, "Unable to process the request, try again!", Toast.LENGTH_SHORT).show();
            }
        };
    }





    /**
     * versione base
     * @param amount
     * @return
     */
    private String computeAmountDetails(int id_pagante, double amount){
        int npartecipants = mGroupUsers.size();
        double perperson = amount/npartecipants;
        HashMap<Integer, Double> amount_details = new HashMap<Integer, Double>();

        Set<Integer> userSet = mGroupUsers.keySet();
        Iterator iterator = userSet.iterator();
        while(iterator.hasNext()){
            int id = (int) iterator.next();
            double value = 0;
            if(id == id_pagante){
                value = (npartecipants-1)*perperson;
            } else {
                value = -perperson;
            }
            amount_details.put(id,value);
        }
        return AmountDetailsUtils.encodeAmountDetails(amount_details);
    }


    public void onPickButtonClick(View v) {
        // Construct an intent for the place picker
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the intent by requesting a result,
            // identified by a request code.

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG_LOG, "NO PERMISSIONS");
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return;
            }
            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public void findPlace(View view) {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE).build();
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setFilter(typeFilter)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        /*
        if (requestCode == REQUEST_PLACE_PICKER){
            if(resultCode == Activity.RESULT_OK) {

                // The user has selected a place. Extract the name and address.
                final Place place = PlacePicker.getPlace(this, data);
                mPlace = place.freeze();
                final CharSequence name = mPlace.getName();
                final CharSequence address = mPlace.getAddress();
            } else {
                // The user canceled the operation.
            }

        } else */ if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mPlace = PlaceAutocomplete.getPlace(this, data).freeze();
                mCostPositionText.setEnabled(false);
                mCostPositionText.setText(mPlace.getName());
                mMapFragment.getView().setVisibility(View.VISIBLE);
                mMapFragment.getMapAsync(AddCostActivity.this);
                mAddPositionButton.setText(R.string.action_add_cost_delete_position);
                Log.i(TAG_LOG, "Place: " + mPlace.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG_LOG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
/*
    public void onAddCostClick(View view){
        // TODO: 22/06/2016 IMPLEMENTARE CARICAMENTO SU SERVER con richiesta volley
        Toast.makeText(AddCostActivity.this, "eheh pensavi che funzionasse...",
                Toast.LENGTH_LONG).show();
    }*/

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    @Override
    public void onClick(View v) {
        Button buttonPosition = (Button) v;
        String addPosition = getResources().getString(R.string.action_add_cost_add_position);
        String deletePosition = getResources().getString(R.string.action_add_cost_delete_position);
        if(buttonPosition.getText().toString().equalsIgnoreCase(addPosition)){
            findPlace(v);
        } else if (buttonPosition.getText().toString().equalsIgnoreCase(deletePosition)){
            mPlace = null;
            mCostPositionText.setEnabled(true);
            mCostPositionText.setText("");
            mAddPositionButton.setText(R.string.action_add_cost_add_position);
            mMapFragment.getView().setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMap();
        LatLng position = mPlace.getLatLng();
        String name = mPlace.getName().toString();
        String snippet = mPlace.getAddress().toString();

        Marker marker = mMap.addMarker(
                new MarkerOptions().position(position).title(name).snippet(snippet)
        );
        poitToPosition();
    }

    private void poitToPosition() {
        //Build camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mPlace.getLatLng())
                .zoom(13).build();
        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
