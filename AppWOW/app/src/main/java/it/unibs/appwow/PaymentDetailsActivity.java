package it.unibs.appwow;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
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

import java.net.URL;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.fragments.PaymentsFragment;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.MyPlace;
import it.unibs.appwow.models.Payment;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.DateUtils;
import it.unibs.appwow.utils.IdEncodingUtils;

public class PaymentDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    // TODO: 06/07/2016  AGGIUNGERE NEI DETTAGLI DELLA SPESA I SINGOLI AMOUNT

    private final String TAG_LOG = PaymentDetailsActivity.class.getSimpleName();

    private Payment mPayment;
    private GroupModel mGroup;
    private LocalUser mUser;

    private View mRootLayout;
    private TextView mPaymentName;
    private TextView mFullName;
    private TextView mEmail;
    private TextView mAmount;
    private TextView mCurrency;
    private TextView mDate;
    private TextView mNotes;
    private TextView mNotesLabel;
    private TextView mPositionText;
    private TextView mPositionLabel;
    //private Place mPlace;
    private MyPlace mPlace;
    private GoogleMap mMap;
    private MapFragment mMapFragment;

    private ImageButton mMapButton;
    private LinearLayout mAmountDetailContainer;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        mUser = LocalUser.load(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mRootLayout = findViewById(R.id.activity_payment_details_container);
        mPayment = getIntent().getParcelableExtra(PaymentsFragment.PASSING_PAYMENT_TAG);
        mGroup = getIntent().getParcelableExtra(PaymentsFragment.PASSING_GROUP_TAG);
        setTitle(mPayment.getName());


        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mPaymentName = (TextView) findViewById(R.id.cost_detail_name);
        mPaymentName.setText(mPayment.getName());

        mFullName = (TextView) findViewById(R.id.cost_detail_user_fullName);
        mFullName.setText(mPayment.getFullName());
        mEmail = (TextView) findViewById(R.id.cost_detail_email);
        mEmail.setText("(" + mPayment.getEmail() + ")");

        mAmount = (TextView) findViewById(R.id.cost_detail_amount);
        mAmount.setText(String.format(Locale.ROOT, "%.2f", mPayment.getAmount()));
        mAmount.setText(Amount.getAmountString(mPayment.getAmount()));
        mCurrency = (TextView) findViewById(R.id.payment_detail_currency);
        Currency curr = Currency.getInstance(mPayment.getCurrency());
        mCurrency.setText(curr.getSymbol());

        mNotes = (TextView) findViewById(R.id.cost_detail_notes_text);
        if(mPayment.getNotes() == null || mPayment.getNotes().isEmpty()){
            mNotesLabel = (TextView) findViewById(R.id.cost_detail_notes_label);
            mNotesLabel.setVisibility(View.GONE);
            mNotes.setVisibility(View.GONE);
        } else {
            mNotes.setText(mPayment.getNotes());
        }

        mDate = (TextView) findViewById(R.id.cost_detail_date);
        mDate.setText(DateUtils.dateLongToString(mPayment.getDate()));

        mPositionText = (TextView) findViewById(R.id.cost_detail_position_text);
        mPositionLabel = (TextView) findViewById(R.id.cost_detail_position_label);
        String stringaPosizione = mPayment.getPosition();
        mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.cost_detail_position_map));
        mMapButton = (ImageButton) findViewById(R.id.payment_detail_map_button);

        if(stringaPosizione!=null && !stringaPosizione.isEmpty()){
           mPositionText.setText(stringaPosizione);
        } else {
            mPositionText.setVisibility(View.GONE);
            mPositionLabel.setVisibility(View.GONE);
        }

        final String position_id = mPayment.getPositionId();
        if(position_id!= null && !position_id.isEmpty()){
            /*
            Places.GeoDataApi.getPlaceById(mClient, position_id).setResultCallback(new ResultCallback<PlaceBuffer>() {
                @Override
                public void onResult(PlaceBuffer places) {
                    Log.d("RESUL CALLBACK", "sono entrato nel result callback di getPlaceById");
                    if (places.getStatus().isSuccess() && places.getCount() > 0) {
                        final Place myPlace = places.get(0);
                        mPlace = myPlace.freeze();
                        Log.i(TAG_LOG, "Place found: " + mPlace.getName());
                    } else {
                        Log.e(TAG_LOG, "Place not found");
                    }
                    mPositionText.setText(mPlace.getName());
                    mMapFragment.getMapAsync(PaymentDetailsActivity.this);
                    places.release();
                }
            });*/
            Log.d(TAG_LOG, "POSITION ID: " + position_id);
            sendPlaceDetailRequest(position_id);
        } else {
            mMapButton.setVisibility(View.INVISIBLE);
            mMapFragment.getView().setVisibility(View.GONE);
        }




        if(mPayment.isExchange()){
            LinearLayout userToContainer = (LinearLayout) findViewById(R.id.payment_detail_user_to_container);
            userToContainer.setVisibility(View.VISIBLE);

            UserDAO dao = new UserDAO();
            dao.open();
            String [] user_to_info = dao.getSingleUserInfo(mPayment.getIdUserTo());
            dao.close();

            String fullname = user_to_info[0];
            String email = user_to_info[1];

            TextView fullNameTV = (TextView) userToContainer.findViewById(R.id.payment_detail_user_to_fullName);
            TextView emailTV = (TextView) userToContainer.findViewById(R.id.payment_detail_user_to_email);

            fullNameTV.setText(fullname);
            emailTV.setText("(" + email+ ")");
        } else {
            //details
            mAmountDetailContainer = (LinearLayout) findViewById(R.id.payment_detail_amount_details_container);

            String ad = mPayment.getAmountDetails();
            List<Amount> amounts = IdEncodingUtils.decodeAmountDetails(ad, mPayment.getIdUser(), mPayment.getAmount());

            Amount first = null;
            for(Amount a: amounts){
                if(a.getUserId() == mUser.getId()){
                    first = a;
                    break;
                }
            }

            mAmountDetailContainer.addView(buildView(first));
            for(Amount a: amounts){
                if(a.getUserId() !=  mUser.getId()) mAmountDetailContainer.addView(buildView(a));
            }
        }
    }

    private View buildView(Amount a){
        View view = getLayoutInflater().inflate(R.layout.payment_slider_item, null, false);
        TextView fullName = (TextView) view.findViewById(R.id.payment_slider_item_fullname);
        TextView email = (TextView) view.findViewById(R.id.payment_slider_item_email);
        EditText amount = (EditText) view.findViewById(R.id.payment_slider_item_amount);
        amount.setEnabled(false);
        ProgressBar seekBar = (ProgressBar) view.findViewById(R.id.payment_slider_item_slider);
        fullName.setText(a.getFullName() + ((mUser.getId() == a.getUserId())?" (you) ":""));
        email.setText(a.getEmail());
        amount.setText(a.getAmountString());
        seekBar.setProgress(computeSeekBarLevel(a.getAmount(),mPayment.getAmount()));
        return view;
    }

    public int computeSeekBarLevel(double amount, double total){
        return (int) Math.round((amount/total)*100);
    }

    private void sendPlaceDetailRequest(final String place_id) {
        String uri = WebServiceUri.getPlaceDetailsUri(this, place_id);
        Log.d(TAG_LOG, "URI: " + uri);
        StringRequest req = new StringRequest(uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String place_url = "";
                        String place_name = "";
                        String place_address = "";
                        String status = "";
                        LatLng latLng;
                        try {
                            JSONObject rispostajs = new JSONObject(response);
                            status = rispostajs.getString("status");
                            Log.d(TAG_LOG, "RISPOSTA:" +  rispostajs.toString(1));
                            if(status.equalsIgnoreCase("OK")){
                                JSONObject placejs = new JSONObject(response).getJSONObject("result");
                                place_url = placejs.getString("url");
                                place_name = placejs.getString("name");
                                place_address = placejs.getString("formatted_address");
                                JSONObject location = placejs.getJSONObject("geometry").getJSONObject("location");
                                String lat = location.getString("lat");
                                String lng = location.getString("lng");
                                latLng = new LatLng(new Double(lat), new Double (lng));
                                mPlace = new MyPlace(place_name, place_address, place_url, latLng);

                                Log.d(TAG_LOG,"Setting map button on details");
                                //setto il bottone di gmaps
                                mMapButton.setVisibility(View.VISIBLE);
                                mMapButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Uri uri = Uri.parse(mPlace.getUrl());
                                        Intent gmaps = new Intent(Intent.ACTION_VIEW, uri);
                                        gmaps.setPackage("com.google.android.apps.maps");
                                        startActivity(gmaps);
                                    }
                                });

                                //setto il fragment con la mappa
                                mPositionText.setText(mPlace.getName());
                                mMapFragment.getMapAsync(PaymentDetailsActivity.this);
                            } else {
                                mMapButton.setVisibility(View.INVISIBLE);
                                mMapFragment.getView().setVisibility(View.GONE);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MyApplication.getAppContext(),error.toString(),Toast.LENGTH_SHORT);
                    }
                });
        MyApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_payment_details, menu);
        //la modifica può essere effettuata da chi ha pagato o, nel caso di un debito, dal creditore

        boolean isPayer = (mPayment.getIdUser() == mUser.getId());
        Integer idUserTo = mPayment.getIdUserTo();
        boolean isUserTo = false;
        if(idUserTo != null){
            if(idUserTo == mUser.getId()){
                isUserTo = true;
            }
        }

        boolean isAdmin = (mUser.getId() == mGroup.getIdAdmin());

        Log.d(TAG_LOG, "ispayer: " + isPayer + ", isUserTo: " + isUserTo + "isAdmin: " + isAdmin);
        if(isPayer || isUserTo || isAdmin){
            menu.findItem(R.id.menu_payment_details_edit).setVisible(true);
            menu.findItem(R.id.menu_payment_details_delete).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_payment_details_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.payment_delete_title));
                builder.setMessage(String.format(getString(R.string.payment_delete_message), mPayment.getName()));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        //progress dialog
                        showProgressDialog();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
            case R.id.menu_payment_details_edit:
                Intent editPayment = new Intent(PaymentDetailsActivity.this, AddEditPaymentActivity.class);
                editPayment.putExtra(PaymentsFragment.PASSING_GROUP_TAG, mGroup);
                editPayment.putExtra(PaymentsFragment.PASSING_PAYMENT_TAG, mPayment);
                startActivity(editPayment);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.payment_deleting));
        progressDialog.setCancelable(false);
        progressDialog.show();
        sendDeleteRequest(progressDialog);

    }

    private void sendDeleteRequest(final ProgressDialog dialog) {
        URL url = WebServiceUri.uriToUrl(WebServiceUri.getDeletePaymentUri(mPayment.getId()));
        StringRequest req = WebServiceRequest.stringRequest(Request.Method.DELETE, url.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                boolean result = false;
                try {
                    JSONObject obj = new JSONObject(response);
                    String stringresult = obj.getString("success");
                    result = Boolean.parseBoolean(stringresult);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(result){
                    dialog.dismiss();
                    finish();
                } else {
                    dialog.dismiss();
                    showUnableToRemoveSnackbar(WebServiceUri.SERVER_ERROR);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG_LOG, "VOLLEY ERROR: " + error);
                dialog.dismiss();
                showUnableToRemoveSnackbar(WebServiceUri.NETWORK_ERROR);
            }
        });
        MyApplication.getInstance().addToRequestQueue(req);
    }

    private void showUnableToRemoveSnackbar(int errorType){
        String msg = "";
        switch (errorType){
            case WebServiceUri.SERVER_ERROR:
                msg = String.format(getResources().getString(R.string.payment_delete_unsuccess_server_error), mPayment.getName());
                break;
            case WebServiceUri.NETWORK_ERROR:
                msg = String.format(getResources().getString(R.string.payment_delete_unsuccess_network_error), mPayment.getName());
        }
        final Snackbar snackbar = Snackbar.make(mRootLayout, msg , Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.retry, new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showProgressDialog();
            }
        });
        snackbar.show();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMap();
        LatLng position = mPlace.getLatLng();
        String name = mPlace.getName();
        String snippet = mPlace.getAddress();

        Marker marker = mMap.addMarker(
                new MarkerOptions().position(position).title(name).snippet(snippet)
        );
        poitToPosition();
    }

    private void poitToPosition() {
        //Build camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mPlace.getLatLng())
                .zoom(14).build();
        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        /*try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient.connect();
        /*
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CostDetails Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://it.unibs.appwow/http/host/path")
        );
        AppIndex.AppIndexApi.start(mClient, viewAction);*/
    }

    @Override
    public void onStop() {
        super.onStop();
/*
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CostDetails Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://it.unibs.appwow/http/host/path")
        );
        AppIndex.AppIndexApi.end(mClient, viewAction);*/
        mClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(PaymentDetailsActivity.this, "Connection to google maps failed", Toast.LENGTH_SHORT).show();
    }
}
