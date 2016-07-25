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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.notifications.FirebaseInstanceIDService;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.Messages;

public class EditUserActivity extends AppCompatActivity {

    private static final String TAG_LOG = EditUserActivity.class.getSimpleName();

    // UI
    private Toolbar mToolbar;
    private EditText mFullname;
    private EditText mEmail;
    private EditText mOldPassword;
    private EditText mNewPassword;
    private EditText mConfirmNewPassword;
    private ProgressBar mProgressView;
    private LinearLayout mLayoutForm;
    private View mViewContainer;

    private LocalUser mLocalUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_user);
        mViewContainer = findViewById(R.id.main_container);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLocalUser = LocalUser.load(this);


        // Precompilo Forum
        mFullname = (EditText) findViewById(R.id.activity_edit_user_fullname);
        mFullname.setText(mLocalUser.getFullName());

        mEmail = (EditText) findViewById(R.id.activity_edit_user_email);
        mEmail.setText(mLocalUser.getEmail());

        mOldPassword = (EditText) findViewById(R.id.activity_edit_user_old_password);

        mNewPassword = (EditText) findViewById(R.id.activity_edit_user_new_password);
        mConfirmNewPassword = (EditText) findViewById(R.id.activity_edit_user_new_password_confirm);

        mConfirmNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String strPass1 = mNewPassword.getText().toString();
                String strPass2 = mConfirmNewPassword.getText().toString();
                if (!strPass1.equals(strPass2)) {
                    mConfirmNewPassword.setError(getString(R.string.error_password_matching));
                }
            }
        });

        mProgressView = (ProgressBar) findViewById(R.id.activity_edit_user_progress);
        mLayoutForm = (LinearLayout) findViewById(R.id.activity_edit_user_form);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void save(View view) {
        Button save = (Button) view;
        if(checkForm()){
            showProgress(true);
            sendEditUserRequest();
        }
    }

    private boolean checkForm() {
        String fullname = mFullname.getText().toString();
        //String email = mEmail.getText().toString();
        String oldPassword = mOldPassword.getText().toString();
        String newPassword = mNewPassword.getText().toString();
        String confirmNewPassword = mConfirmNewPassword.getText().toString();

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

        // Check for a valid password, if the mUser entered one.
        if (!TextUtils.isEmpty(oldPassword) && !Validator.isPasswordValid(oldPassword)) {
            mOldPassword.setError(getString(R.string.error_invalid_password));
            focusView = mOldPassword;
            cancel = true;
        }

        // Check for a valid password, if the mUser entered one.
        if (!TextUtils.isEmpty(newPassword) && !Validator.isPasswordValid(newPassword)) {
            mNewPassword.setError(getString(R.string.error_invalid_password));
            focusView = mNewPassword;
            cancel = true;
        }

        // Check if both passwords are the same.
        if(!newPassword.equals(confirmNewPassword)){
            mConfirmNewPassword.setError(getString(R.string.error_password_matching));
            focusView = mConfirmNewPassword;
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


    private void sendEditUserRequest() {
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.err_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    sendEditUserRequest();
                }
            });
            showProgress(false);
            return;
        }

        String url = WebServiceUri.getUserUri(mLocalUser.getId()).toString();
        String [] keys = {"fullName", "oldPassword", "password"};
        String fullname = mFullname.getText().toString();
        String oldPassword = mOldPassword.getText().toString();
        String newPassword = mNewPassword.getText().toString();
        String [] values = {fullname, oldPassword, newPassword};
        Map<String, String> params = WebServiceRequest.createParametersMap(keys, values);
        StringRequest req = WebServiceRequest.stringRequest(Request.Method.PUT, url, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                UserModel um = null;
                String status = "";
                try {
                    Log.d(TAG_LOG,"Response: " + response);
                    JSONObject obj = new JSONObject(response);
                    status = obj.getString("status");
                    if(status.equalsIgnoreCase("fail")){
                        Log.e(TAG_LOG,"Error fail: " + obj.getString("error"));
                        if(!obj.isNull("type")){
                            showProgress(false);
                            Toast.makeText(EditUserActivity.this, getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
                        }else{
                            showProgress(false);
                            Toast.makeText(EditUserActivity.this, getString(R.string.server_internal_error), Toast.LENGTH_SHORT).show();
                        }
                    } else if(status.equalsIgnoreCase("success")){
                        JSONObject userObj = obj.getJSONObject("data");
                        um = UserModel.create(userObj);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    showProgress(false);
                    Toast.makeText(EditUserActivity.this, getString(R.string.server_internal_error), Toast.LENGTH_SHORT).show();
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
                    Log.d(TAG_LOG,lu.toString());
                    finish();
                } else if(um == null && status.equalsIgnoreCase("success")){
                    showProgress(false);
                    Toast.makeText(EditUserActivity.this, getString(R.string.app_internal_error), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Toast.makeText(EditUserActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
            }
        });
        MyApplication.getInstance().addToRequestQueue(req);
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

            mLayoutForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mLayoutForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLayoutForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLayoutForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
