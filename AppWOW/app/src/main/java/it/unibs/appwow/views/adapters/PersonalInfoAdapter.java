package it.unibs.appwow.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.R;

/**
 * Created by federicofalcone on 22/07/16.
 */
public class PersonalInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG_LOG = PersonalInfoAdapter.class.getSimpleName();

    // View Type
    public static final int PERSONAL_INFO_SUMMARY = 1;
    public static final int PERSONAL_INFO_MAPS = 2;

    private Context mContext;
    private LayoutInflater mInflater;
    private int[] mDataSetTypes;
    private List<Place> mPlacesObj;

    public PersonalInfoAdapter(Context context, int[] dataSetTypes) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDataSetTypes = dataSetTypes;
        mPlacesObj = new ArrayList<Place>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        switch (viewType) {
            case PERSONAL_INFO_SUMMARY:
                View vSummary = mInflater.inflate(R.layout.fragment_personal_info_summary, parent, false);
                vh = new PersonalInfoSummaryViewHolder(vSummary);
                break;
            case PERSONAL_INFO_MAPS:
                View vMap = mInflater.inflate(R.layout.fragment_personal_info_map_card, parent, false);
                vh = new PersonalInfoMapsViewHolder(vMap);
                break;
            default:
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case PERSONAL_INFO_SUMMARY:
                PersonalInfoSummaryViewHolder itemSummaryHolder = (PersonalInfoSummaryViewHolder) holder;
                break;

            case PERSONAL_INFO_MAPS:
                PersonalInfoMapsViewHolder itemMapsHolder = (PersonalInfoMapsViewHolder) holder;
                itemMapsHolder.initializeMapView();
                break;

            default:

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDataSetTypes.length;
    }

    @Override
    public int getItemViewType(int position) {
        return mDataSetTypes[position];
    }

    public void setPlacesObj(List<Place> placesObj) {
        mPlacesObj = placesObj;
    }

    public List<Place> getPlacesObj() {
        return mPlacesObj;
    }

    public void addPlaceObj(Place p){
        Log.d(TAG_LOG,"addPlaceObj: " + p.getName().toString());
        mPlacesObj.add(p);
    }

    public class PersonalInfoSummaryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        //public TextView paymentName;

        public PersonalInfoSummaryViewHolder(View itemView) {
            super(itemView);
            //itemView.setOnClickListener(this);
            //itemView.setOnLongClickListener(this);
            //paymentName = (TextView) itemView.findViewById(R.id.payment_fragment_item_costname);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG_LOG, "onClick position: " + getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG_LOG, "onLongClick position: " + getAdapterPosition());
            return true;
        }
    }

    public class PersonalInfoMapsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, OnMapReadyCallback, GoogleMap.OnMapLoadedCallback{
        private MapView mapView;
        private GoogleMap map;

        public PersonalInfoMapsViewHolder(View itemView) {
            super(itemView);
            //itemView.setOnClickListener(this);
            //itemView.setOnLongClickListener(this);
            mapView = (MapView) itemView.findViewById(R.id.fragment_personal_info_map);
            //mapView.getMapAsync(this);
            //mSupportMapFragment.getMapAsync(this);

            //addMarkerAndCenter();

            /*if(mPlacesObj != null){
                addMarkerAndCenter();
            }*/
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG_LOG, "onClick position: " + getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG_LOG, "onLongClick position: " + getAdapterPosition());
            return true;
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(mContext);
            map = googleMap;
            //map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //map.getUiSettings().setZoomControlsEnabled(true);
            try {
                map.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        /**
         * Initialises the MapView by calling its lifecycle methods.
         */
        public  void initializeMapView(){
            if(mapView != null) {
                // Initialise the MapView
                mapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
            }
        }

        private void addMarkerAndCenter() {
            if(map != null){
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                if(!mPlacesObj.isEmpty()){
                    for (Place p: mPlacesObj){
                        Marker marker = map.addMarker(
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
                    map.animateCamera(cameraPosition);
                }
            }
        }

        @Override
        public void onMapLoaded() {

        }
    }
}
