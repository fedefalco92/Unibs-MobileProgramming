package it.unibs.appwow;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.util.List;

import it.unibs.appwow.graphicTools.GroupAdapter;

public class GroupActivity extends AppCompatActivity {

    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        gridView = (GridView)findViewById(R.id.gridview_groups);
        gridView.setAdapter(new GroupAdapter(this));

        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //Toast.makeText(GroupActivity.this, "Posizione" + position,Toast.LENGTH_SHORT).show();
                final Intent i = new Intent(GroupActivity.this, GroupDetailsActivity.class);
                i.putExtra("GroupID", gridView.getAdapter().getItemId(position));
                startActivity(i);
                //finish();
            }
        });


/*
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(GroupActivity.this, "Item position:" + position, Toast.LENGTH_SHORT).show();

            }
        });*/
     /*  CardView cardView = (CardView) findViewById(R.id.card_view);
        TextView textView = new TextView(this);
        textView.setText("Prova 2");

        cardView.addView(textView);

        TextView textView2 = new TextView(this);
        textView.setText("Prova 3");

        cardView.addView(textView2);*/




    }

}
