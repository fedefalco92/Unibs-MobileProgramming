package it.unibs.appwow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AddGroupMembersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_members);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.create_group:
                // TODO: 19/05/2016 creare il gruppo
                //final Intent registrationIntent = new Intent(AddGroupMembersActivity.this, AddGroupMembersActivity.class);
                //startActivityForResult(registrationIntent, REGISTRATION_REQUEST_ID);
                //startActivity(registrationIntent);
                return true;
            default:
                return true;
        }
    }
}
