package it.unibs.appwow.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.R;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.views.adapters.PersonalInfoAdapter;


public class PersonalInfoFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private final String TAG_LOG = PersonalInfoFragment.class.getSimpleName();

    private GoogleApiClient mClient;
    private static final String ARG_USER= "user";


    // UI
    private GoogleMap mGoogleMap;
    private SupportMapFragment mMapFragment;
    private MapView mMapView;

    private String [] mPlaceIds;
    private List<Place> mPlacesObj;

    private LocalUser mLocalUser;

    // ADAPTER
    private PersonalInfoAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private int mDataSetTypes[] = {PersonalInfoAdapter.PERSONAL_INFO_SUMMARY, PersonalInfoAdapter.PERSONAL_INFO_MAPS, PersonalInfoAdapter.PERSONAL_INFO_MAPS};
    //private String[] mDataset = {"29 degrees", "Seahawks 24 - 27 Bengals",
    //        "Flash missing, vanishes in crisis", "Half Life 3 announced"};
    //private int mDatasetTypes[] = {WEATHER, SCORE, NEWS, NEWS}; //view types

    public PersonalInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param localUser LocalUser.
     * @return A new instance of fragment PersonalInfoFragment.
     */
    public static PersonalInfoFragment newInstance(LocalUser localUser) {
        PersonalInfoFragment fragment = new PersonalInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, localUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLocalUser = getArguments().getParcelable(ARG_USER);
        }

        mClient = new GoogleApiClient
                .Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

        mPlacesObj = new ArrayList<Place>();
        //fetchUserPlaces();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_personal_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //mMapView = (MapView) view.findViewById(R.id.fragment_personal_info_map);
        //mMapView.getMapAsync(this);
        /*
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PersonalInfoAdapter(getActivity(), mDataSetTypes);
        mRecyclerView.setAdapter(mAdapter);*/

        fetchUserPlaces();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_personal_info_map);
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.stopAutoManage(getActivity());
        mClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClient.stopAutoManage(getActivity());
        mClient.disconnect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mClient.stopAutoManage(getActivity());
        mClient.disconnect();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            mGoogleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    public void fetchUserPlaces() {
        String uri = WebServiceUri.getUserPlaceUri(mLocalUser.getId()).toString();
        JsonObjectRequest req = WebServiceRequest.objectRequest(Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    if(status.equalsIgnoreCase("success")){
                        JSONArray data = response.getJSONArray("data");
                        Log.d(TAG_LOG, data.toString());
                        mPlaceIds = data.join(",").replace("\"","").split(",");

                        Places.GeoDataApi.getPlaceById(mClient, mPlaceIds)
                                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                    @Override
                                    public void onResult(PlaceBuffer places) {
                                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                            //final Place myPlace = places.get(0);
                                            for (Place myPlace : places) {
                                                Log.i(TAG_LOG, "Place found: " + myPlace.getName());
                                                mPlacesObj.add(myPlace.freeze());
                                                //mAdapter.addPlaceObj(myPlace.freeze());
                                            }
                                            Log.d(TAG_LOG,"mPlacesObj size: " + mPlacesObj.size());
                                            addMarkerAndCenter();
                                            //mAdapter.notifyDataSetChanged();
                                            //mAdapter.notifyItemChanged(1);
                                        } else {
                                            Log.e(TAG_LOG, "Place not found");
                                        }
                                        places.release();
                                    }
                                });



                    } else {
                        Log.e(TAG_LOG,response.toString());
                        Toast.makeText(getContext(), R.string.server_internal_error,Toast.LENGTH_SHORT);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MyApplication.getInstance().addToRequestQueue(req);
    }

    private void addMarkerAndCenter() {
        if(mGoogleMap != null){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Place p: mPlacesObj){
                Marker marker = mGoogleMap.addMarker(
                        new MarkerOptions().position(p.getLatLng())
                                .title(p.getName().toString())
                                .snippet(p.getAddress().toString())
                );

                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int padding = 150; // offset from edges of the map in pixels
            CameraUpdate cameraPosition = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            //Zoom in and animate the camera.
            mGoogleMap.animateCamera(cameraPosition);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(), R.string.error_google_maps, Toast.LENGTH_SHORT).show();

    }
}