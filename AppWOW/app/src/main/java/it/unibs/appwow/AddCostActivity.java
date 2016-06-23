package it.unibs.appwow;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;

import it.unibs.appwow.fragments.CostsFragment;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.utils.DecimalDigitsInputFilter;

public class AddCostActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG_LOG = AddCostActivity.class.getSimpleName();

    private static final int REQUEST_PLACE_PICKER = 1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;

    private LocalUser mUser;
    private GroupModel mGroup;
    private Place mPlace;

    private EditText mName;
    private EditText mAmount;
    private EditText mNotes;
    private EditText mPositionText;
    private Button mAddPositionButton;
    private Button mAddCostButton;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cost);

        mUser = LocalUser.load(this);
        mGroup = getIntent().getParcelableExtra(CostsFragment.PASSING_GROUP_TAG);
        mPlace = null;

        mName = (EditText) findViewById(R.id.add_cost_name);

        mAmount = (EditText) findViewById(R.id.add_cost_amount);
        mAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});

        mNotes = (EditText) findViewById(R.id.add_cost_notes);

        mPositionText = (EditText) findViewById(R.id.add_cost_position_text);

        mAddPositionButton = (Button) findViewById(R.id.add_cost_add_position_button);
        mAddPositionButton.setOnClickListener(this);

        mAddCostButton = (Button) findViewById(R.id.add_cost_add_button);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void onPickButtonClick(View v) {
        // Construct an intent for the place picker
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the intent by requesting a result,
            // identified by a request code.
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

        if (requestCode == REQUEST_PLACE_PICKER){
            if(resultCode == Activity.RESULT_OK) {

                // The user has selected a place. Extract the name and address.
                final Place place = PlacePicker.getPlace(this, data);

                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
            } else {
                // The user canceled the operation.
            }

        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mPlace = PlaceAutocomplete.getPlace(this, data);
                mPositionText.setEnabled(false);
                mPositionText.setText(mPlace.getName());
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

    public void onAddCostClick(View view){
        // TODO: 22/06/2016 IMPLEMENTARE CARICAMENTO SU SERVER con richiesta volley
        Toast.makeText(AddCostActivity.this, "eheh pensavi che funzionasse...",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "AddCost Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://it.unibs.appwow/http/host/path")
        );
        AppIndex.AppIndexApi.start(mClient, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "AddCost Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://it.unibs.appwow/http/host/path")
        );
        AppIndex.AppIndexApi.end(mClient, viewAction);
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
            mPositionText.setEnabled(true);
            mPositionText.setText("");
            mAddPositionButton.setText(R.string.action_add_cost_add_position);
        }
    }
}
