package it.unibs.appwow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;


public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        
        Button save = (Button) findViewById(R.id.button_save_fullname);
        // TODO: 18/05/2016 AGGIUNGERE LISTENER
    }
}
