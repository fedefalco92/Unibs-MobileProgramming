package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.util.HashMap;
import java.util.Map;

import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.Messages;

public class EditGroupNameActivity extends AppCompatActivity {

    public static final String GROUP_NAME_EXTRA = "group_name";
    public static final String GROUP_ID_EXTRA = "group_id";

    private int mGroupId;

    // UI
    private View mProgressView;
    private View mContainer;
    private EditText mNewNameEditText;
    private View mViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_name);
        mViewContainer = findViewById(R.id.main_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String name =getIntent().getStringExtra(GROUP_NAME_EXTRA);
        mGroupId = getIntent().getIntExtra(GROUP_ID_EXTRA, -1);
        mNewNameEditText = (EditText) findViewById(R.id.new_group_name);
        mNewNameEditText.setText(name);

        mProgressView =  findViewById(R.id.save_name_progress_bar);
        mContainer = findViewById(R.id.container);
        setTitle(getString(R.string.edit_group_name_title));
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
        if(mGroupId!= -1 && Validator.isGroupNameValid(name)){
            showProgress(true);
            sendPostRequest();
        } else {
            mNewNameEditText.setError(getString(R.string.error_invalid_group_name));
            mNewNameEditText.requestFocus();
        }

    }

    private void sendPostRequest() {
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.err_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    sendPostRequest();

                }
            });
            showProgress(false);
            return;
        }

        String [] keys = {"name"};
        String [] values = {mNewNameEditText.getText().toString()};
        Map<String, String> params =  WebServiceRequest.createParametersMap(keys, values);
        String url = WebServiceUri.getGroupUri(mGroupId).toString();

        StringRequest req = WebServiceRequest.stringRequest(Request.Method.POST, url, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    String newGroupName = "";
                    try {
                        JSONObject resjs = new JSONObject(response);
                        GroupModel gm = GroupModel.create(resjs);
                        newGroupName = gm.getGroupName();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (!newGroupName.isEmpty()) {
                        Intent data = new Intent();
                        data.putExtra(GROUP_NAME_EXTRA, newGroupName);
                        if (getParent() == null) {
                            setResult(Activity.RESULT_OK, data);
                        } else {
                            getParent().setResult(Activity.RESULT_OK, data);
                        }
                        finish();
                    } else {
                        showProgress(false);
                        Toast.makeText(EditGroupNameActivity.this, getString(R.string.server_internal_error), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    showProgress(false);
                    Toast.makeText(EditGroupNameActivity.this, getString(R.string.server_internal_error), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Toast.makeText(EditGroupNameActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
            }
        });

        MyApplication.getInstance().addToRequestQueue(req);
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
    
    
}
