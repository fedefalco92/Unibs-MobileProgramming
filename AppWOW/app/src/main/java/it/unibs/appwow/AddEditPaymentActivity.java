package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.fragments.GroupListFragment;
import it.unibs.appwow.fragments.PaymentsFragment;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.Payment;
import it.unibs.appwow.models.SliderAmount;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.DatePickerFragment;
import it.unibs.appwow.utils.DateUtils;
import it.unibs.appwow.utils.IdEncodingUtils;
import it.unibs.appwow.utils.DecimalDigitsInputFilter;
import it.unibs.appwow.utils.TimePickerFragment;
import it.unibs.appwow.utils.Validator;

/**
 * permette di creare un nuovo payment o di modificarne uno esistente
 * (in questo caso il payment da modificare viene passato come parcelable extra)
 */
public class AddEditPaymentActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private final String TAG_LOG = AddEditPaymentActivity.class.getSimpleName();

    private final static int COLOR_LOCKED = R.color.colorAccent;
    private final static int COLOR_UNLOCKED = R.color.black;

    private static final int REQUEST_PLACE_PICKER = 1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    private static final int DELETE_PLACE_TAG = 1;
    private static final int FIND_PLACE_TAG = 2;

    private boolean EDIT_MODE;

    //approssimazione importi
    /*
    private static final String CENT = "CENT";
    private static final String TENCENTS = "TENCENTS";
    private String mApproxType;*/

    private LocalUser mUser;
    private GroupModel mGroup;
    private Place mPlace;
    private HashMap<Integer,UserModel> mGroupUsers;

    private Payment mToEditPayment;

    private EditText mPaymentNameEditText;
    private EditText mPaymentAmountEditText;
    //private Spinner mPaymentCurrency;
    private TextView mPaymentCurrency;
    private EditText mPaymentDateEditText;
    private EditText mPaymentTimeEditText;
    private EditText mPaymentNotesEditText;
    private EditText mPaymentPositionEditText;
    private Button mAddPaymentButton;
    private ImageButton mAddPositionButton;
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private CheckBox mIsTransferCheckBox;
    private Spinner mUserToSpinner;

    private View mProgressView;
    private View mAddPaymentContainerView;
    private LinearLayout mSliderListView;
    private List<SliderAmount> mSliderAmountList;
    private Set<SliderAmount> mLockedAmount;
    private Set<SliderAmount> mUnlockedAmount;
    private SliderAmount mLocalUserSliderAmount;

    private double mPaymentAmount;

    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_payment);
        mGroup = getIntent().getParcelableExtra(PaymentsFragment.PASSING_GROUP_TAG);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getString(R.string.add_payment_activity_title));

        EDIT_MODE = false;
        mToEditPayment = getIntent().getParcelableExtra(PaymentsFragment.PASSING_PAYMENT_TAG);
        if(mToEditPayment != null){
            EDIT_MODE = true;
            setTitle(mToEditPayment.getName());
            mPaymentAmount = mToEditPayment.getAmount();
        }

        mUser = LocalUser.load(this);
        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mApproxType = prefs.getString("pref_key_round_payments", CENT);
        Log.d(TAG_LOG, " APPROX TYPE: " + mApproxType);*/

        UserGroupDAO dao = new UserGroupDAO();
        dao = new UserGroupDAO();
        dao.open();
        mGroupUsers = dao.getAllUsers(mGroup.getId());
        mSliderAmountList = dao.getAllSliderAmounts(mGroup.getId());
        dao.close();

        mLockedAmount = new HashSet<SliderAmount>();
        mUnlockedAmount = new HashSet<SliderAmount>();

        mPlace = null;

        mPaymentNameEditText = (EditText) findViewById(R.id.add_payment_name);
        mPaymentAmountEditText = (EditText) findViewById(R.id.add_payment_amount);
        mPaymentAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(getMaxDecimalDigits())});
        if(EDIT_MODE) mPaymentAmountEditText.setText(Amount.getAmountString(mToEditPayment.getAmount()));
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

        mPaymentNotesEditText = (EditText) findViewById(R.id.add_payment_notes);
        mPaymentPositionEditText = (EditText) findViewById(R.id.add_payment_position_text);

        mAddPositionButton = (ImageButton) findViewById(R.id.add_payment_add_position_button);
        mAddPositionButton.setTag(FIND_PLACE_TAG);
        mAddPositionButton.setOnClickListener(this);
        if(EDIT_MODE){
            mAddPositionButton.setTag(DELETE_PLACE_TAG);
            mPaymentPositionEditText.setEnabled(false);
            mAddPositionButton.setImageResource(R.drawable.ic_highlight_off_black_24dp);
        }

        mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.add_payment_position_map_fragment));
        mMapFragment.getView().setVisibility(View.GONE);

        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        mProgressView = findViewById(R.id.add_payment_post_request_progress);
        mAddPaymentContainerView = findViewById(R.id.add_payment_container);

        mSliderListView = (LinearLayout) findViewById(R.id.add_payment_slider_listview);

        mLocalUserSliderAmount = null;
        for(final SliderAmount sa: mSliderAmountList){
            if(sa.getUserId() == mUser.getId()){
                mLocalUserSliderAmount = sa;
                break;
            }
        }

        mSliderListView.addView(buildView(mLocalUserSliderAmount));
        for(final SliderAmount sa: mSliderAmountList){
            if(sa.getUserId() !=  mUser.getId()) mSliderListView.addView(buildView(sa));
        }

        mAddPaymentButton = (Button) findViewById(R.id.add_payment_button);
        if(EDIT_MODE) mAddPaymentButton.setVisibility(View.GONE);

        mPaymentDateEditText = (EditText) findViewById(R.id.add_payment_date);
        mPaymentTimeEditText = (EditText) findViewById(R.id.add_payment_time);
        long now = System.currentTimeMillis();
        mPaymentDateEditText.setText(DateUtils.longToSimpleDateString(now));
        mPaymentTimeEditText.setText(DateUtils.longToSimpleTimeString(now));
        mPaymentDateEditText.setKeyListener(null);
        mPaymentTimeEditText.setKeyListener(null);

        /*
        mPaymentCurrency = (Spinner) findViewById(R.id.add_payment_currency);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPaymentCurrency.setAdapter(adapter);
        mPaymentCurrency.setSelection(0);*/

        mPaymentCurrency = (TextView) findViewById(R.id.add_payment_currency);
        mPaymentCurrency.setText("EUR");

        if(EDIT_MODE) {
            mPaymentNameEditText.setText(mToEditPayment.getName());
            //mPaymentCurrency.setSelection(adapter.getPosition(mToEditPayment.getCurrency()));
            mPaymentCurrency.setText(mToEditPayment.getCurrency());
            mPaymentNotesEditText.setText(mToEditPayment.getNotes());
            mPaymentPositionEditText.setText(mToEditPayment.getPosition());

            List<Amount> amounts = IdEncodingUtils.decodeAmountDetails(mToEditPayment.getAmountDetails(), mToEditPayment.getIdUser(), mToEditPayment.getAmount());
            for (Amount a : amounts) {
                for (SliderAmount s : mSliderAmountList) {
                    if (s.getUserId() == a.getUserId()) {
                        s.setAmount(a.getAmount());
                        s.setAmountText(a.getAmountString());
                        s.setSeekBarProgress(a.getAmount(), mToEditPayment.getAmount());
                        break;
                    }
                }
            }

            mPaymentDateEditText.setText(DateUtils.longToSimpleDateString(mToEditPayment.getDate()));
            mPaymentTimeEditText.setText(DateUtils.longToSimpleTimeString(mToEditPayment.getDate()));
        }


        mUserToSpinner = (Spinner) findViewById(R.id.add_payment_user_to_spinner);
        //seleziono gli utenti da mostrare nello spinner
        List<SliderAmount> spinnerItems = new ArrayList<SliderAmount>();
        int localUserId = mUser.getId();
        if(EDIT_MODE && mToEditPayment.isExchange()){
            int idUserTo = mToEditPayment.getIdUserTo();
            for(SliderAmount s: mSliderAmountList){
                if(s.getUserId() == idUserTo){
                    spinnerItems.add(s);
                    break;
                }
            }
        } else{
            for(SliderAmount s: mSliderAmountList){
                if(s.getUserId() != localUserId){
                    spinnerItems.add(s);
                }
            }
        }

        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUserToSpinner.setAdapter(spinnerArrayAdapter);

        mIsTransferCheckBox = (CheckBox) findViewById(R.id.add_payment_is_transfer_checkbox);
        if(!EDIT_MODE){
            mIsTransferCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        mSliderListView.setVisibility(View.GONE);
                        mUserToSpinner.setVisibility(View.VISIBLE);
                    } else {
                        mSliderListView.setVisibility(View.VISIBLE);
                        mUserToSpinner.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            mIsTransferCheckBox.setEnabled(false);
            if(mToEditPayment.isExchange()){
                mIsTransferCheckBox.setChecked(true);
                mSliderListView.setVisibility(View.GONE);
                mUserToSpinner.setVisibility(View.VISIBLE);
                int idUserTo = mToEditPayment.getIdUserTo();
                mUserToSpinner.setEnabled(false);
                mUserToSpinner.setSelection(0);
            }
        }




    }

    public void onAddPaymentButtonClicked(View v){
        checkErrors();
    }

    private void checkErrors(){
        boolean nomeOk = Validator.isCostNameValid(mPaymentNameEditText.getText().toString());
        boolean amountOk = Validator.isAmountValid(mPaymentAmountEditText.getText().toString());
        boolean paymentOk = verifyAmounts();
        if(nomeOk && amountOk && paymentOk){
            sendPostRequest();
        } else {
            if(!nomeOk){
                mPaymentNameEditText.setError(getString(R.string.error_invalid_cost_name));
                mPaymentNameEditText.requestFocus();
            }
            if(!amountOk){
                mPaymentAmountEditText.setError(getString(R.string.error_invalid_amount));
                mPaymentAmountEditText.requestFocus();
            }
            if(!paymentOk){
                mLocalUserSliderAmount.getAmountView().setError(getString(R.string.error_invalid_amount_user));
                mLocalUserSliderAmount.getAmountView().requestFocus();
            }
        }
    }

    private boolean verifyAmounts(){
        Log.d(TAG_LOG,"verifyAmounts");
        boolean res = true;
        Log.d(TAG_LOG,"mPayment Amount" + mPaymentAmount);
        Log.d(TAG_LOG,"mLocalUserSliderAmount" + mLocalUserSliderAmount.getAmount());
        if(mPaymentAmount == mLocalUserSliderAmount.getAmount()){
            res = false;
        }
        return res;
    }

    public void showDatePicker(View v){
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
        //mPaymentDateEditText.setClickable(false);
    }

    public void showTimePicker(View v){
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
        //mPaymentDateEditText.setClickable(false);
    }

    private View buildView(SliderAmount sa){
        mUnlockedAmount.add(sa);
        View view = getLayoutInflater().inflate(R.layout.payment_slider_item, null, false);
        TextView fullName = (TextView) view.findViewById(R.id.payment_slider_item_fullname);
        TextView email = (TextView) view.findViewById(R.id.payment_slider_item_email);
        EditText amount = (EditText) view.findViewById(R.id.payment_slider_item_amount);
        amount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(getMaxDecimalDigits())});
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
        //if(!text.isEmpty()) mPaymentAmount = new Double(text);

        if(!text.isEmpty()) mPaymentAmount = Double.parseDouble(text.replace(',','.'));
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
        if(EDIT_MODE){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_edit_payment, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.edit_payment_save_item:
               checkErrors();
            default:
                return true;
        }
    }

    private void sendPostRequest() {
        showProgress(true);
        Map<String, String> requestParams = new HashMap<String, String>();
        String name = mPaymentNameEditText.getText().toString();
        String notes = mPaymentNotesEditText.getText().toString();
        String position = mPaymentPositionEditText.getText().toString();
        String position_id = "";
        String currency = mPaymentCurrency.getText().toString();

        if(EDIT_MODE){
            if(mToEditPayment.getId() == -1){
                EDIT_MODE = false;
            }
        }

        if(!EDIT_MODE){
            //creazione payment
            String[] keys = {"idGroup", "idUser", "amount", "currency", "date", "name", "notes", "position", "position_id", "amount_details", "forAll", "isExchange"};
            String idGroup = String.valueOf(mGroup.getId());
            String idUser = String.valueOf(mUser.getId());
            //double amountDouble = new Double(mPaymentAmountEditText.getText().toString());
            double amountDouble = Double.parseDouble(mPaymentAmountEditText.getText().toString().replace(',','.'));
            String amount = String.valueOf(amountDouble);

            if(mPlace!=null){
                position_id = mPlace.getId();
            }

            String amount_details = computeAmountDetails();
            String dateLong = buildDateLongToString();
            //String currency = (String) mPaymentCurrency.getSelectedItem();


            boolean forAll = isForAll();

            boolean isExchange = mIsTransferCheckBox.isChecked();

            String[] values = {idGroup, idUser, amount, currency, dateLong, name, notes, position, position_id, amount_details, String.valueOf(forAll?1:0), String.valueOf(isExchange?1:0)};
            requestParams = WebServiceRequest.createParametersMap(keys, values);

            int idUserTo = -1;
            if(mIsTransferCheckBox.isChecked()){
                SliderAmount userTo = (SliderAmount) mUserToSpinner.getSelectedItem();
                idUserTo = userTo.getUserId();
            }
            if(idUserTo >= 0){
                requestParams.put("idUserTo", String.valueOf(idUserTo));
            }

        } else {
            String[] keys = {"date", "name", "notes", "position", "position_id"};

            position_id = mToEditPayment.getPositionId();
            if(position_id == null)
                position_id = "";
            if(mPlace!=null){
                position_id = mPlace.getId();
            }

            String dateLong = buildDateLongToString();

            String[] values = {dateLong, name, notes, position, position_id};
            requestParams = WebServiceRequest.createParametersMap(keys, values);


            if(mToEditPayment.getAmount() != mPaymentAmount){
                requestParams.put("amount",String.valueOf(mPaymentAmount));
            }

            //double amountDouble = new Double(mPaymentAmountEditText.getText().toString());
            double amountDouble = Double.parseDouble(mPaymentAmountEditText.getText().toString().replace(',','.'));

            String amount = String.valueOf(amountDouble);
            String amount_details = computeAmountDetails();
            if(!mToEditPayment.getAmountDetails().equals(amount_details)){
                requestParams.put("amount_details", amount_details);
            }

            //String currency = (String) mPaymentCurrency.getSelectedItem();

            Log.d(TAG_LOG, "CURRENCY" + currency);
            if(!currency.equals(mToEditPayment.getCurrency())){
                requestParams.put("currency", currency);
            }

            boolean forAll = isForAll();
            requestParams.put("forAll", String.valueOf(forAll?1:0));


        }


        String uri = WebServiceUri.PAYMENTS_URI.toString();
        if(EDIT_MODE) uri = WebServiceUri.getPaymentUri(mToEditPayment.getId()).toString();
        Log.d(TAG_LOG, "uri: " + uri);
        Log.d(TAG_LOG, "MAP " + requestParams.toString());
        StringRequest postRequest = WebServiceRequest.
                stringRequest(EDIT_MODE? Request.Method.PUT:Request.Method.POST, uri, requestParams, responseListener(), responseErrorListener());
        Log.d(TAG_LOG, "POSTREQUEST: " + postRequest.toString());
        MyApplication.getInstance().addToRequestQueue(postRequest);
    }

    private String buildDateLongToString(){
        String date_string = mPaymentDateEditText.getText().toString();
        String time_string = mPaymentTimeEditText.getText().toString();
        long date = DateUtils.buildDateLong(date_string, time_string);
        return (date!=0L)?String.valueOf(date):"";
    }

    private Response.Listener<String> responseListener() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                boolean success = false;
                if (!response.isEmpty()) {
                    Log.d(TAG_LOG, "RISPOSTA: " + response);
                    try {
                        JSONObject resjs = new JSONObject(response);
                        String status = resjs.getString("status");
                        success = status.equalsIgnoreCase("success");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if(success){
                    Toast.makeText(AddEditPaymentActivity.this, EDIT_MODE? R.string.edit_payment_success: R.string.add_payment_success, Toast.LENGTH_SHORT).show();
                    Intent toGroupDetails = new Intent(MyApplication.getAppContext(),GroupDetailsActivity.class);
                    toGroupDetails.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    toGroupDetails.putExtra(GroupListFragment.PASSING_GROUP_TAG,mGroup);
                    startActivity(toGroupDetails);
                } else {
                    showProgress(false);
                    Toast.makeText(AddEditPaymentActivity.this, R.string.server_internal_error, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private Response.ErrorListener responseErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Log.e("Error",error.getMessage());
                Toast.makeText(AddEditPaymentActivity.this, R.string.server_connection_error, Toast.LENGTH_SHORT).show();
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
     * @return
     */
    //private String computeAmountDetails(int id_pagante, double amount){
    private String computeAmountDetails(){
        int npartecipants = mGroupUsers.size();
        HashMap<Integer, Double> amount_details = new HashMap<Integer, Double>();
        List<SliderAmount> list = mSliderAmountList;
        if(!mIsTransferCheckBox.isChecked()){
            for(SliderAmount s: list){
                int id = s.getUserId();
                double value = 0;
                //if(id == id_pagante){
                if(id == mUser.getId()){
                    //value = amount-s.getAmount();
                    value = mPaymentAmount-s.getAmount();
                } else {
                    value = -s.getAmount();
                }
                if(value!=0) amount_details.put(id,value);
            }
        } else {
            int idTo = ((SliderAmount) mUserToSpinner.getSelectedItem()).getUserId();
            for(SliderAmount s: list){
                int id = s.getUserId();
                double value = 0;
                if(id == mUser.getId()){
                    value = mPaymentAmount;
                } else if(id == idTo){
                    value = -mPaymentAmount;
                }
                if(value!=0) amount_details.put(id,value);
            }
        }

        return IdEncodingUtils.encodeAmountDetails(amount_details);
    }

    private boolean isForAll(){
        boolean forAll = true;
        int size = mSliderAmountList.size();
        double each = mPaymentAmount/size;
        int approxMultiplier = getApproxMultiplier();
        each = Math.floor(each*approxMultiplier)/approxMultiplier;

        for(SliderAmount s:mSliderAmountList){
            if(s.getUserId()!=mUser.getId()){
                if(s.getAmount()!= each) {
                    forAll = false;
                    break;
                }
            }
        }
        Log.d(TAG_LOG, "FORALL: " + forAll);
        return forAll;
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
                mPaymentPositionEditText.setEnabled(false);
                mPaymentPositionEditText.setText(mPlace.getName());
                mMapFragment.getView().setVisibility(View.VISIBLE);
                mMapFragment.getMapAsync(AddEditPaymentActivity.this);
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
            if(EDIT_MODE) mToEditPayment.setPositionId("");
            mPaymentPositionEditText.setEnabled(true);
            mPaymentPositionEditText.setText("");
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
        /*switch (mApproxType){
            case CENT: multiplier = 100; break;
            case TENCENTS: multiplier = 10; break;
        }*/
        return multiplier;
    }

    private int getMaxDecimalDigits(){
        int max = 2;
        /*switch (mApproxType){
            case CENT: max = 2; break;
            case TENCENTS:  max = 1; break;
        }*/
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
                //if (!str.isEmpty()) partialAmount = new Double(str);
                if (!str.isEmpty()) partialAmount = Double.parseDouble(str.replace(',','.'));
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
