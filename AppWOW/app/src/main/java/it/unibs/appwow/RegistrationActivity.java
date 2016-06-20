package it.unibs.appwow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.utils.Validator;


public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG_LOG = RegistrationActivity.class.getSimpleName();

    private LocalUser mReceived;
    private TextView mFullname;
    private TextView mEmail;
    private TextView mPassword;
    private TextView mConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mReceived = (LocalUser) getIntent().getExtras().getParcelable(LoginActivity.PASSING_USER_EXTRA);
        Log.d(TAG_LOG, "LocalUser: " + mReceived.toString());


        // Precompilo Forum
        mFullname = (TextView) findViewById(R.id.activity_registration_fullname);

        mEmail = (TextView) findViewById(R.id.activity_registration_email);
        mEmail.setText(mReceived.getEmail());

        mPassword = (TextView) findViewById(R.id.activity_registration_previous_password);
        mPassword.setText(mReceived.getPassword());

        mConfirmPassword = (TextView) findViewById(R.id.activity_registration_current_password);

        mConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String strPass1 = mPassword.getText().toString();
                String strPass2 = mConfirmPassword.getText().toString();
                if (!strPass1.equals(strPass2)) {
                    mConfirmPassword.setError(getString(R.string.error_password_matching));
                }
            }
        });
        
        //Button save = (Button) findViewById(R.id.activity_registration_confirm);
        // TODO: 18/05/2016 AGGIUNGERE LISTENER
    }

    public void register(View view) {
        Button save = (Button) view;
        if(checkForm()){
            Log.d(TAG_LOG,"do registration");
        } else {
            Log.d(TAG_LOG,"do not do registration");
        }

    }

    private boolean checkForm(){
        String fullname = mFullname.getText().toString();
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String confirmPassword = mConfirmPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid fullname
        if(TextUtils.isEmpty(fullname)){
            mFullname.setError((getString(R.string.error_field_required)));
            focusView = mFullname;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        } else if (!Validator.isEmailValid(email)) {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }

        // Check for a valid password, if the mUser entered one.
        if (!TextUtils.isEmpty(password) && !Validator.isPasswordValid(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }

        // Check if both passwords are the same.
        if(!password.equals(confirmPassword)){
            mConfirmPassword.setError(getString(R.string.error_password_matching));
            focusView = mConfirmPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            Log.d(TAG_LOG,"There was an error");
            return false;
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the mUser login attempt.
            Log.d(TAG_LOG,"Login!!");
            return true;
        }
    }
}
