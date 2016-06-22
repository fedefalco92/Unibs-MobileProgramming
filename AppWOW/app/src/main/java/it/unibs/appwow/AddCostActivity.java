package it.unibs.appwow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import it.unibs.appwow.fragments.CostsFragment;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;

public class AddCostActivity extends AppCompatActivity {

    private LocalUser mUser;
    private GroupModel mGroup;

    private TextView mName;
    private TextView mAmount;
    private TextView mNotes;
    private Button mAddPosition;
    private Button mAddCost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cost);

        mUser = LocalUser.load(this);
        mGroup = getIntent().getParcelableExtra(CostsFragment.PASSING_GROUP_TAG);

        mName = (TextView) findViewById(R.id.add_cost_name);

        mAmount = (TextView)findViewById(R.id.add_cost_amount);

        mNotes = (TextView) findViewById(R.id.add_cost_notes);

        mAddPosition = (Button) findViewById(R.id.add_cost_add_position_button);
        mAddPosition.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: 22/06/2016 IMPLEMENTARE AGGIUNTA POSIZIONE
                        Toast.makeText(AddCostActivity.this, "eheh pensavi che funzionasse...",
                                Toast.LENGTH_LONG).show();
                    }
                }
        );

        mAddCost = (Button) findViewById(R.id.add_cost_add_button);
        mAddCost.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: 22/06/2016 IMPLEMENTARE CARICAMENTO SU SERVER
                        Toast.makeText(AddCostActivity.this, "eheh pensavi che funzionasse...",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
        );

    }
}
