package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.notifications.FirebaseInstanceIDService;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.Messages;


public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG_LOG = RegistrationActivity.class.getSimpleName();

    private LocalUser mReceived;

    // UI
    private View mViewContainer;
    private TextView mFullname;
    private TextView mEmail;
    private TextView mPassword;
    private TextView mConfirmPassword;
    private View mRegistrationFormView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mViewContainer = findViewById(R.id.main_container);

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
        
        mRegistrationFormView = findViewById(R.id.registration_form);
        mProgressView = findViewById(R.id.registration_progress);
        
        //Button save = (Button) findViewById(R.id.activity_registration_confirm);
        
    }

    public void register(View view) {
        Button save = (Button) view;
        if(checkForm()){
            Log.d(TAG_LOG,"do registration");
            showProgress(true);
            sendRegistrationRequest();
        } else {
            Log.d(TAG_LOG,"do not do registration");
        }

    }

    private void sendRegistrationRequest() {
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.err_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    sendRegistrationRequest();
                }
            });
            showProgress(false);
            return;
        }

        String url = WebServiceUri.USERS_URI.toString();
        String [] keys = {"fullName", "email" , "password"};
        String fullname = mFullname.getText().toString();
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String [] values = {fullname, email, password};
        Map<String, String> params = WebServiceRequest.createParametersMap(keys, values);
        StringRequest req = WebServiceRequest.stringRequest(Request.Method.POST, url, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                UserModel um = null;
                try {
                    JSONObject obj = new JSONObject(response);
                    if(obj.isNull("success")){
                        um = UserModel.create(obj);
                        registerToken(um.getId(), FirebaseInstanceIDService.getToken());
                    } else {
                        //errore: utente già esistente
                        showProgress(false);
                        mEmail.setError(getString(R.string.error_email_not_available));
                        mEmail.requestFocus();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(um!=null){
                    //aggiungo l'utente al db per evitare foreign key exception
                    UserDAO dao = new UserDAO();
                    dao.open();
                    dao.insertUser(um);
                    dao.close();
                    //salvo shared preferences
                    LocalUser lu = LocalUser.create(um.getId()).withEmail(um.getEmail()).withFullName(um.getFullName());
                    lu.save(MyApplication.getAppContext());
                    //rimando a navigation activity
                    Intent i = new Intent(RegistrationActivity.this, NavigationActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    showProgress(false);
                    Toast.makeText(RegistrationActivity.this, getString(R.string.server_internal_error), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Toast.makeText(RegistrationActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
            }
        });
        MyApplication.getInstance().addToRequestQueue(req);
    }

    private void registerToken(final int id, final String token) {
        String[] keys = {"id","msg_token"};
        String[] values = {String.valueOf(id),token};

        Map<String,String> map = WebServiceRequest.createParametersMap(keys,values);

        StringRequest request = WebServiceRequest.stringRequest(Request.Method.POST, WebServiceUri.REGISTER_TOKEN_URI.toString(), map,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG_LOG,"Token registrato correttamente");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        MyApplication.getInstance().addToRequestQueue(request);
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
        } else if(!Validator.isFullNameValid(fullname)){
            mFullname.setError((getString(R.string.error_invalid_fullname)));
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

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegistrationFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
