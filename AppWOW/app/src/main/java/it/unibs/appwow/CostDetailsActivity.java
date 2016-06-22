package it.unibs.appwow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import it.unibs.appwow.fragments.CostsFragment;
import it.unibs.appwow.models.parc.CostModel;
import it.unibs.appwow.utils.DateUtils;

public class CostDetailsActivity extends AppCompatActivity {

    private CostModel mCost;

    private TextView mName;
    private TextView mAmount;
    private TextView mDate;
    private TextView mNotes;

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

        mNotes = (TextView) findViewById(R.id.cost_detail_notes);
        mNotes.setText(mCost.getNotes());

        mDate = (TextView) findViewById(R.id.cost_detail_date);
        mDate.setText(DateUtils.dateLongToString(mCost.getUpdatedAt()));
    }
}
