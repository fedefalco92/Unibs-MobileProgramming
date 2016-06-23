package it.unibs.appwow;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Locale;

import it.unibs.appwow.fragments.CostsFragment;
import it.unibs.appwow.models.parc.CostModel;
import it.unibs.appwow.utils.DateUtils;
import it.unibs.appwow.utils.PositionUtils;

public class CostDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG_LOG = CostDetailsActivity.class.getSimpleName();

    private CostModel mCost;

    private TextView mName;
    private TextView mAmount;
    private TextView mDate;
    private TextView mNotes;
    private TextView mPositionText;
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
        mCost = getIntent().getParcelableExtra(CostsFragment.PASSING_COST_TAG);
        setTitle(mCost.getName());


        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        //mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        /*mClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();*/

        mName = (TextView) findViewById(R.id.cost_detail_name);
        mName.setText(mCost.getName());

        mAmount = (TextView) findViewById(R.id.cost_detail_amount);
        mAmount.setText("EUR " + mCost.getAmount());

        mNotes = (TextView) findViewById(R.id.cost_detail_notes_text);
        mNotes.setText(mCost.getNotes());

        mDate = (TextView) findViewById(R.id.cost_detail_date);
        mDate.setText(DateUtils.dateLongToString(mCost.getUpdatedAt()));

        mPositionText = (TextView) findViewById(R.id.cost_detail_position_text);
        String stringaPosizione = mCost.getPosition();

        mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.cost_detail_position_map));

        /**
         * la stringa mPosition è una posizione fittizia tipo "casa mia" oppure un ID di google places preceduto da "###"
         */
        if (PositionUtils.isPositionId(stringaPosizione)) {
            String id = PositionUtils.decodePositionId(stringaPosizione);
            //riempire la mappa

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            PendingResult<PlaceBuffer> result = Places.GeoDataApi.getPlaceById(mClient, id);
            result.setResultCallback(new ResultCallback<PlaceBuffer>() {
                @Override
                public void onResult(PlaceBuffer places) {
                    if (places.getStatus().isSuccess() && places.getCount() > 0) {
                        mPlace = places.get(0);
                        Log.i(TAG_LOG, "Place found: " + mPlace.getName());
                    } else {
                        Log.e(TAG_LOG, "Place not found");
                    }
                    mPositionText.setText(mPlace.getName());
                    mMapFragment.getMapAsync(CostDetailsActivity.this);
                    places.release();
                }
            });
            /*
            Places.GeoDataApi.getPlaceById(mClient, id)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(PlaceBuffer places) {
                            if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                mPlace = places.get(0);
                                mPositionText.setText(mPlace.getName());
                                mMapFragment.getMapAsync(CostDetailsActivity.this);
                                Log.i(TAG_LOG, "Place found: " + mPlace.getName());
                            } else {
                                Log.e(TAG_LOG, "Place not found");
                            }
                            places.release();
                        }
                    });*/

            /*PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mClient, null);

            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {

                    for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                        Log.d(TAG_LOG, String.format("Place '%s' has likelihood: %g",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                    }

                    placeLikelihoods.release();
                }
            });*/



            /* DUMMY PLACE FOR DEBUGGING
            mPlace = new Place() {
                @Override
                public String getId() {
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
            };*/


        } else {
            mPositionText.setText(stringaPosizione);
            mMapFragment.getView().setVisibility(View.INVISIBLE);
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
        Toast.makeText(CostDetailsActivity.this, "Connection to google maps failed", Toast.LENGTH_SHORT).show();
    }
}
