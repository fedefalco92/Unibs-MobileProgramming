package it.unibs.appwow;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import it.unibs.appwow.fragments.PaymentsFragment;
import it.unibs.appwow.models.parc.PaymentModel;
import it.unibs.appwow.utils.DateUtils;
import it.unibs.appwow.utils.PositionUtils;

public class PaymentDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    // TODO: 06/07/2016  AGGIUNGERE NEI DETTAGLI DELLA SPESA I SINGOLI AMOUNT

    private final String TAG_LOG = PaymentDetailsActivity.class.getSimpleName();

    private PaymentModel mCost;

    private TextView mName;
    private TextView mAmount;
    private TextView mDate;
    private TextView mNotes;
    private TextView mNotesLabel;
    private TextView mPositionText;
    private TextView mPositionLabel;
    private Place mPlace;
    private GoogleMap mMap;
    private MapFragment mMapFragment;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_details);
        mCost = getIntent().getParcelableExtra(PaymentsFragment.PASSING_PAYMENT_TAG);
        setTitle(mCost.getName());


        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mName = (TextView) findViewById(R.id.cost_detail_name);
        mName.setText(mCost.getName());

        mAmount = (TextView) findViewById(R.id.cost_detail_amount);
        mAmount.setText("EUR " + mCost.getAmount());

        mNotes = (TextView) findViewById(R.id.cost_detail_notes_text);
        if(mCost.getNotes() == null || mCost.getNotes().isEmpty()){
            mNotesLabel = (TextView) findViewById(R.id.cost_detail_notes_label);
            mNotesLabel.setVisibility(View.INVISIBLE);
            mNotes.setVisibility(View.INVISIBLE);
        } else {
            mNotes.setText(mCost.getNotes());
        }

        mDate = (TextView) findViewById(R.id.cost_detail_date);
        mDate.setText(DateUtils.dateLongToString(mCost.getUpdatedAt()));

        mPositionText = (TextView) findViewById(R.id.cost_detail_position_text);
        mPositionLabel = (TextView) findViewById(R.id.cost_detail_position_label);
        String stringaPosizione = mCost.getPosition();
        mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.cost_detail_position_map));

        /**
         * la stringa mPosition è una posizione fittizia tipo "casa mia" oppure un ID di google places preceduto da "###"
         */
        if (PositionUtils.isPositionId(stringaPosizione)) {
            String id = PositionUtils.decodePositionId(stringaPosizione);
            //riempire la mappa
            /*if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG_LOG, "NO PERMISSIONS");
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }*/

            Places.GeoDataApi.getPlaceById(mClient, id).setResultCallback(new ResultCallback<PlaceBuffer>() {
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
            });

            /* DUMMY PLACE FOR DEBUGGING
            mPlace = new Place() {
                @Override
                public String getUserId() {
                    return "ChIJZ8FwH8p3gUcR1pI7zb0RMwc";
                }

                @Override
                public List<Integer> getPlaceTypes() {
                    return null;
                }

                @Override
                public CharSequence getAddress() {
                    return "via branze";
                }

                @Override
                public Locale getLocale() {
                    return null;
                }

                @Override
                public CharSequence getName() {
                    return "unibs";
                }

                @Override
                public LatLng getLatLng() {
                    return new LatLng(45.0,10.0);
                }

                @Override
                public LatLngBounds getViewport() {
                    return null;
                }

                @Override
                public Uri getWebsiteUri() {
                    return null;
                }

                @Override
                public CharSequence getPhoneNumber() {
                    return null;
                }

                @Override
                public float getRating() {
                    return 0;
                }

                @Override
                public int getPriceLevel() {
                    return 0;
                }

                @Override
                public CharSequence getAttributions() {
                    return null;
                }

                @Override
                public Place freeze() {
                    return null;
                }

                @Override
                public boolean isDataValid() {
                    return false;
                }
            };
            mPositionText.setText(mPlace.getName());
            mMapFragment.getMapAsync(PaymentDetailsActivity.this);
            Log.d("CALLING getMapAsync","method called...");*/

        } else {
            mMapFragment.getView().setVisibility(View.INVISIBLE);
            if(stringaPosizione == null || stringaPosizione.isEmpty()){
                mPositionText.setVisibility(View.INVISIBLE);
                mPositionLabel.setVisibility(View.INVISIBLE);
            } else {
                mPositionText.setText(stringaPosizione);
            }
        }

        if (mMap != null) {
            // Marker position = mMap.addMarker(new MarkerOptions())

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
                .zoom(14).build();
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
