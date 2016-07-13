package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.fragments.PaymentsFragment;
import it.unibs.appwow.models.SliderAmount;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.IdEncodingUtils;
import it.unibs.appwow.utils.DecimalDigitsInputFilter;
import it.unibs.appwow.utils.Validator;

public class AddPaymentActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private final String TAG_LOG = AddPaymentActivity.class.getSimpleName();

    private final static int COLOR_LOCKED = R.color.colorAccent;
    private final static int COLOR_UNLOCKED = R.color.black;

    private static final int REQUEST_PLACE_PICKER = 1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    private static final int DELETE_PLACE_TAG = 1;
    private static final int FIND_PLACE_TAG = 2;

    //approssimazione importi
    private static final String CENT = "CENT";
    private static final String TENCENTS = "TENCENTS";
    private String mApproxType;

    private LocalUser mUser;
    private GroupModel mGroup;
    private Place mPlace;
    private HashMap<Integer,UserModel> mGroupUsers; // FIXME: 06/07/2016 da eliminare

    private EditText mPaymentName;
    private EditText mPaymentAmountEditText;
    private double mPaymentAmount;
    private EditText mPaymentNotes;
    private EditText mPaymentPositionText;
    //private Button mAddPositionButton;
    ImageButton mAddPositionButton;
    private MapFragment mMapFragment;
    private GoogleMap mMap;

    private View mProgressView;
    private View mAddPaymentContainerView;
    //private ListView mSliderListView;
    private LinearLayout mSliderListView;
    private List<SliderAmount> mSliderAmountList;
    private Set<SliderAmount> mLockedAmount;
    private Set<SliderAmount> mUnlockedAmount;
    //private SliderAmountAdapter mAdapter;

    //private Button mAddCostButton;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);
        setTitle(getString(R.string.add_payment_activity_title));

        mUser = LocalUser.load(this);
        mGroup = getIntent().getParcelableExtra(PaymentsFragment.PASSING_GROUP_TAG);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mApproxType = prefs.getString("pref_key_round_payments", CENT);
        Log.d(TAG_LOG, " APPROX TYPE: " + mApproxType);
        UserGroupDAO dao = new UserGroupDAO();
        dao = new UserGroupDAO();
        dao.open();
        mGroupUsers = dao.getAllUsers(mGroup.getId()); // FIXME: 06/07/2016 da eliminare
        mSliderAmountList = dao.getAllSliderAmounts(mGroup.getId());
        dao.close();

        mLockedAmount = new HashSet<SliderAmount>();
        mUnlockedAmount = new HashSet<SliderAmount>();

        mPlace = null;

        mPaymentName = (EditText) findViewById(R.id.add_payment_name);

        mPaymentAmountEditText = (EditText) findViewById(R.id.add_payment_amount);
        int maxDecimalDigits = getMaxDecimalDigits();
        mPaymentAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, maxDecimalDigits)});
        mPaymentAmountEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        retrieveAmountFromEditText();
                        //mAdapter.initializeAmount(mPaymentAmount);
                        initializeAmountAndSets();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                }
        );

        mPaymentNotes = (EditText) findViewById(R.id.add_payment_notes);

        mPaymentPositionText = (EditText) findViewById(R.id.add_payment_position_text);

        mAddPositionButton = (ImageButton) findViewById(R.id.add_payment_add_position_button);
        mAddPositionButton.setTag(FIND_PLACE_TAG);
        mAddPositionButton.setOnClickListener(this);

        mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.add_payment_position_map_fragment));
        mMapFragment.getView().setVisibility(View.GONE);

        //mAddCostButton = (Button) findViewById(R.id.add_cost_add_button);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        mProgressView = findViewById(R.id.add_payment_post_request_progress);
        //mAddCostFormView = findViewById(R.id.add_payment_form_container);
        //mAddCostFormView = findViewById(R.id.add_payment_form);
        mAddPaymentContainerView = findViewById(R.id.add_payment_container);

        //mSliderListView = (ListView) findViewById(R.id.add_payment_slider_listview);
        mSliderListView = (LinearLayout) findViewById(R.id.add_payment_slider_listview);

        SliderAmount first = null;
        for(final SliderAmount sa: mSliderAmountList){
            if(sa.getUserId() == mUser.getId()){
                first = sa;
                break;
            }
        }

        mSliderListView.addView(buildView(first));
        for(final SliderAmount sa: mSliderAmountList){
            if(sa.getUserId() !=  mUser.getId()) mSliderListView.addView(buildView(sa));
        }
    }

    private View buildView(SliderAmount sa){
        mUnlockedAmount.add(sa);
        View view = getLayoutInflater().inflate(R.layout.payment_slider_item, null, false);
        TextView fullName = (TextView) view.findViewById(R.id.payment_slider_item_fullname);
        TextView email = (TextView) view.findViewById(R.id.payment_slider_item_email);
        EditText amount = (EditText) view.findViewById(R.id.payment_slider_item_amount);
        amount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, getMaxDecimalDigits())});
        ProgressBar seekBar = (ProgressBar) view.findViewById(R.id.payment_slider_item_slider);

        fullName.setText(sa.getFullName() + ((mUser.getId() == sa.getUserId())?" (you) ":""));
        email.setText(sa.getEmail());
        amount.setText(sa.getAmountString());
        view.setTag(sa.getUserId());
        amount.setOnFocusChangeListener(new SliderAmountFocusChangeListener(sa));
        sa.setAmountView(amount);
        sa.setSeekBar(seekBar);
        return view;
    }

    public void retrieveAmountFromEditText(){
        String text = mPaymentAmountEditText.getText().toString();
        if(!text.isEmpty()) mPaymentAmount = new Double(text);
        else mPaymentAmount = 0;
    }

    public double getResidualAmount() {
        double lockedAmount = 0;
        for(SliderAmount s:mLockedAmount){
            lockedAmount+=s.getAmount();
        }
        return mPaymentAmount-lockedAmount;
    }


    public double getPaymentAmount() {
        return mPaymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {
        mPaymentAmount = paymentAmount;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_payment, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.add_cost_menu_item:
                boolean nomeOk = Validator.isCostNameValid(mPaymentName.getText().toString());
                boolean amountOk = Validator.isAmountValid(mPaymentAmountEditText.getText().toString());
                if(nomeOk && amountOk){
                    sendPostRequest();
                    return true;
                } else {
                    if(!nomeOk){
                        mPaymentName.setError(getString(R.string.error_invalid_cost_name));
                        mPaymentName.requestFocus();
                    }
                    if(!amountOk){
                        mPaymentAmountEditText.setError(getString(R.string.error_invalid_amount));
                        mPaymentAmountEditText.requestFocus();
                    }
                }

            default:
                return true;
        }
    }

    private void sendPostRequest() {
        showProgress(true);
        String[] keys = {"idGroup", "idUser", "amount", "name", "notes", "position", "position_id", "amount_details"};
        String idGroup = String.valueOf(mGroup.getId());
        String idUser = String.valueOf(mUser.getId());
        double amountDouble = new Double(mPaymentAmountEditText.getText().toString());
        String amount = String.valueOf(amountDouble);
        String name = mPaymentName.getText().toString();
        String notes = mPaymentNotes.getText().toString();
        String position = mPaymentPositionText.getText().toString();
        String position_id = "";
        if(mPlace!=null){
            position_id = mPlace.getId();
        }
        /*
        if(mPlace!= null){
            position = PositionUtils.encodePositionId(mPlace.getId());
        } else {
            position = mPaymentPositionText.getText().toString();
        }*/


        String amount_details = computeAmountDetails(mUser.getId(), amountDouble);
        String[] values = {idGroup, idUser, amount, name, notes, position, position_id, amount_details};

        Map<String, String> requestParams = WebServiceRequest.createParametersMap(keys, values);
        StringRequest postRequest = WebServiceRequest.
                stringRequest(Request.Method.POST, WebServiceUri.PAYMENTS_URI.toString(), requestParams, responseListener(), responseErrorListener());
        MyApplication.getInstance().addToRequestQueue(postRequest);
    }

    private Response.Listener<String> responseListener() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    Toast.makeText(AddPaymentActivity.this, R.string.add_cost_success, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    showProgress(false);
                    Toast.makeText(AddPaymentActivity.this, R.string.add_cost_error, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private Response.ErrorListener responseErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                //Log.e("Error",error.getMessage());
                Toast.makeText(AddPaymentActivity.this, R.string.add_cost_error, Toast.LENGTH_SHORT).show();
            }
        };
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mAddPaymentContainerView.setVisibility(show ? View.GONE : View.VISIBLE);
            mAddPaymentContainerView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAddPaymentContainerView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mAddPaymentContainerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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

        List<SliderAmount> list = mSliderAmountList;
        for(SliderAmount s: list){
            int id = s.getUserId();
            double value = 0;
            if(id == id_pagante){
                value = amount-s.getAmount();
            } else {
                value = -s.getAmount();
            }
            amount_details.put(id,value);
        }
        /*Set<Integer> userSet = mGroupUsers.keySet();
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
        }*/

        return IdEncodingUtils.encodeAmountDetails(amount_details);
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
                mPaymentPositionText.setEnabled(false);
                mPaymentPositionText.setText(mPlace.getName());
                mMapFragment.getView().setVisibility(View.VISIBLE);
                mMapFragment.getMapAsync(AddPaymentActivity.this);
                mAddPositionButton.setImageResource(R.drawable.ic_highlight_off_black_24dp);
                mAddPositionButton.setTag(DELETE_PLACE_TAG);
                //mAddPositionButton.setText(R.string.action_add_cost_delete_position);
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
        Toast.makeText(AddPaymentActivity.this, "eheh pensavi che funzionasse...",
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
        ImageButton buttonPosition = (ImageButton) v;
        if(buttonPosition.getTag().equals(FIND_PLACE_TAG)){
            findPlace(v);
        } else if (buttonPosition.getTag().equals(DELETE_PLACE_TAG)){
            mPlace = null;
            mPaymentPositionText.setEnabled(true);
            mPaymentPositionText.setText("");
            //mAddPositionButton.setText(R.string.action_add_payment_add_position);
            mAddPositionButton.setImageResource(R.drawable.ic_add_location_black_24dp);
            mAddPositionButton.setTag(FIND_PLACE_TAG);
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

    public void initializeAmountAndSets() {
        final int size = mSliderAmountList.size();
        double each = mPaymentAmount/size;
        int approxMultiplier = getApproxMultiplier();

        //gestione arrotondamenti
        each = Math.floor(each*approxMultiplier)/approxMultiplier;
        //Log.d(TAG_LOG, "each: " +each);
        double totalCalculated = each*size;
        //Log.d(TAG_LOG, "totalCalculated " +  totalCalculated);
        double delta = mPaymentAmount-totalCalculated;
        //Log.d(TAG_LOG, "delta " +  delta);
        for(SliderAmount s:mSliderAmountList){
            if(s.getUserId() == mUser.getId()) s.setAmount(each + delta);
            else s.setAmount(each);
            s.setAmountText(s.getAmountString());
            s.setSeekBarProgress(each, mPaymentAmount);
            EditText et = s.getAmountView();
            et.setTextColor(ContextCompat.getColor(this, COLOR_UNLOCKED));
            mUnlockedAmount.add(s);
            mLockedAmount.remove(s);
        }
    }

    private int getApproxMultiplier(){
        int multiplier = 100;
        switch (mApproxType){
            case CENT: multiplier = 100; break;
            case TENCENTS: multiplier = 10; break;
        }
        return multiplier;
    }

    private int getMaxDecimalDigits(){
        int max = 2;
        switch (mApproxType){
            case CENT: max = 2; break;
            case TENCENTS:  max = 1; break;
        }
        return max;

    }

    private class SliderAmountFocusChangeListener implements View.OnFocusChangeListener {

        private SliderAmount sa;

        SliderAmountFocusChangeListener(SliderAmount _sa) {
            this.sa = _sa;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                String str = ((EditText) v).getText().toString();
                double partialAmount = 0;
                if (!str.isEmpty()) partialAmount = new Double(str);
                double residualAmount = getResidualAmount();
                if (sa.getAmount() == partialAmount) {
                    return;
                }
                if (partialAmount > residualAmount) {
                    partialAmount = residualAmount;
                }

                if (mUnlockedAmount.size() > 1) {
                    //metto a residualAmount lo SliderAmount corrente
                    sa.setAmount(partialAmount);
                    sa.setAmountText(sa.getAmountString());
                    sa.setSeekBarProgress(partialAmount, mPaymentAmount);
                    // lo tolgo dall'insieme unlocked e lo metto in locked
                    mUnlockedAmount.remove(sa);
                    mLockedAmount.add(sa);
                    sa.getAmountView().setTextColor(ContextCompat.getColor(getBaseContext(), COLOR_LOCKED));
                    //cambio i valori di tutti gli unlocked
                    for (SliderAmount sa_local : mUnlockedAmount) {
                        double each = (residualAmount - partialAmount) / mUnlockedAmount.size();
                        sa_local.setAmount(each);
                        sa_local.setAmountText(sa_local.getAmountString());
                        sa_local.setSeekBarProgress(sa_local.getAmount(), mPaymentAmount);
                    }
                } else {
                    sa.setAmountText(sa.getAmountString());
                    sa.setSeekBarProgress(sa.getAmount(), mPaymentAmount);

                }

            } else {
                mLockedAmount.remove(sa);
                mUnlockedAmount.add(sa);
                sa.getAmountView().setTextColor(ContextCompat.getColor(getBaseContext(), COLOR_UNLOCKED));
            }
        }
    }
}
