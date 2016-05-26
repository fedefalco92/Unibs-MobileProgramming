package it.unibs.appwow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;

import it.unibs.appwow.model.parc.User;


public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        User received = (User) getIntent().getExtras().getParcelable(LoginActivity.PASSING_USER_EXTRA);
        Log.d("UTENTE_RICEVUTO", received.toString());
        // TODO: 19/05/2016  precompilare form
        
        Button save = (Button) findViewById(R.id.activity_registration_confirm);
        // TODO: 18/05/2016 AGGIUNGERE LISTENER
    }
}
