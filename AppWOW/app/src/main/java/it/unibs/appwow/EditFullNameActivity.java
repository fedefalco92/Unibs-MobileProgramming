package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.Messages;

public class EditFullNameActivity extends AppCompatActivity {
    private static final String TAG_LOG = EditFullNameActivity.class.getSimpleName();

    private static final int MAX_LENGTH = 30;

    private LocalUser mLocalUser;
    // UI
    private View mProgressView;
    private View mContainer;
    private EditText mNewNameEditText;
    private View mViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_full_name);
        mLocalUser = LocalUser.load(this);

        mViewContainer = findViewById(R.id.main_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNewNameEditText = (EditText) findViewById(R.id.new_fullname);
        LocalUser localUser = LocalUser.load(this);
        mNewNameEditText.setText(localUser.getFullName());

        mProgressView =  findViewById(R.id.save_name_progress_bar);
        mContainer = findViewById(R.id.container);
        setTitle(getString(R.string.title_activity_edit_full_name));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void cancel(View v){
        finish();
    }

    public void saveName(View v){
        checkErrors();
    }

    private void checkErrors(){
        String name = mNewNameEditText.getText().toString();
        if(Validator.isFullNameValid(name) && name.length() < MAX_LENGTH){
            showPasswordDialog();
        } else {
            mNewNameEditText.setError(getString(R.string.error_invalid_fullname));
            mNewNameEditText.requestFocus();
        }

    }

    private void showPasswordDialog(){
        // get prompts.xml view
        View promptView = getLayoutInflater().inflate(R.layout.password_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(promptView);

        final EditText password = (EditText) promptView.findViewById(R.id.password);
        // setup a dialog window
        builder.setCancelable(false)
                .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        showProgress(true);
                        sendEditFullnameRequest(password.getText().toString());
                    }
                })
                .setNegativeButton(R.string.action_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = builder.create();
        alert.show();
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mContainer.setVisibility(show ? View.GONE : View.VISIBLE);
            mContainer.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mContainer.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void sendEditFullnameRequest(final String password) {
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.error_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    sendEditFullnameRequest(password);
                }
            });
            showProgress(false);
            return;
        }

        String url = WebServiceUri.getUserUri(mLocalUser.getId()).toString();
        String [] keys = {"fullName", "password"};
        String fullName = mNewNameEditText.getText().toString();
        String [] values = {fullName, password};
        Map<String, String> params = WebServiceRequest.createParametersMap(keys, values);
        StringRequest req = WebServiceRequest.stringRequest(Request.Method.PUT, url, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                UserModel um = null;
                String status = "";
                try {
                    JSONObject obj = new JSONObject(response);
                    status = obj.getString("status");
                    if(status.equalsIgnoreCase("fail")){
                        if(!obj.isNull("type")){
                            showProgress(false);
                            Toast.makeText(EditFullNameActivity.this, getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
                        }else{
                            showProgress(false);
                            Toast.makeText(EditFullNameActivity.this, getString(R.string.error_server_internal_error), Toast.LENGTH_SHORT).show();
                        }
                    } else if(status.equalsIgnoreCase("success")){
                        JSONObject userObj = obj.getJSONObject("data");
                        um = UserModel.create(userObj);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    showProgress(false);
                    Toast.makeText(EditFullNameActivity.this, getString(R.string.error_server_internal_error), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(EditFullNameActivity.this, getString(R.string.error_app_internal), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Toast.makeText(EditFullNameActivity.this, getString(R.string.error_server_connection), Toast.LENGTH_SHORT).show();
            }
        });
        MyApplication.getInstance().addToRequestQueue(req);
    }
}
