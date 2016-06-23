package it.unibs.appwow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import it.unibs.appwow.fragments.CostsFragment;
import it.unibs.appwow.models.parc.CostModel;
import it.unibs.appwow.utils.DateUtils;
import it.unibs.appwow.utils.PositionUtils;

public class CostDetailsActivity extends AppCompatActivity {

    private CostModel mCost;

    private TextView mName;
    private TextView mAmount;
    private TextView mDate;
    private TextView mNotes;
    private TextView mPositionText;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_details);
        mCost = getIntent().getParcelableExtra(CostsFragment.PASSING_COST_TAG);
        setTitle(mCost.getName());

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

        if(PositionUtils.isPositionId(stringaPosizione)){
            String id = PositionUtils.decodePositionId(stringaPosizione);
            //riempire la mappa

        } else {
            mPositionText.setText(stringaPosizione);
        }
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.cost_detail_position_map)).getMap();
        if(mMap != null){
           // Marker position = mMap.addMarker(new MarkerOptions())

        }


    }
}
