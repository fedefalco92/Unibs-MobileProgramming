package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.fragments.GroupListFragment;
import it.unibs.appwow.models.UserGroupModel;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.Messages;

public class AddSingleMemberActivity extends AppCompatActivity {

    public static final String GROUP_NAME_EXTRA = "group_name";
    public static final String GROUP_ID_EXTRA = "group_id";
    private static final String TAG_LOG = AddSingleMemberActivity.class.getSimpleName();

    private GroupModel mGroup;

    private View mProgressView;
    private View mContainer;
    private EditText mEmailEditText;
    private GroupModel mUser;
    private View mViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_single_member);
        mViewContainer = findViewById(R.id.main_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String name =getIntent().getStringExtra(GROUP_NAME_EXTRA);
        mGroup = getIntent().getParcelableExtra(GroupListFragment.PASSING_GROUP_TAG);
        mEmailEditText = (EditText) findViewById(R.id.new_member_email);
        mEmailEditText.setText(name);

        mProgressView =  findViewById(R.id.progress_bar);
        mContainer = findViewById(R.id.container);
        setTitle(getString(R.string.add_member));
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

    public void searchUser(View v){
        checkErrors();
    }

    private void checkErrors(){
        String email = mEmailEditText.getText().toString();
        if(Validator.isEmailValid(email)){
            showProgress(true);
            sendSearchRequest();
        } else {
            mEmailEditText.setError(getString(R.string.error_invalid_email));
            mEmailEditText.requestFocus();
        }

    }

    private void serverConnectionErrorToast(){
        showProgress(false);
        Messages.showSnackbar(mViewContainer,R.string.server_connection_error);
        //Toast.makeText(AddSingleMemberActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
    }

    private void serverInternalError(){
        showProgress(false);
        //Toast.makeText(AddSingleMemberActivity.this, getString(R.string.server_internal_error), Toast.LENGTH_SHORT).show();
        Messages.showSnackbar(mViewContainer,R.string.server_internal_error);
    }

    private void sendSearchRequest() {
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.err_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    sendSearchRequest();
                }
            });
            showProgress(false);
            return;
        }

        String[] keys = {"email"};
        String[] values = {mEmailEditText.getText().toString()};
        Map<String, String> requestParams = WebServiceRequest.createParametersMap(keys, values);
        StringRequest userRequest = WebServiceRequest.
                stringRequest(Request.Method.POST, WebServiceUri.CHECK_USER_URI.toString(), requestParams, responseListenerUser(),  responseErrorListener());
        MyApplication.getInstance().addToRequestQueue(userRequest);

    }

    private Response.Listener<String> responseListenerUser(){
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    try {
                        JSONObject resjs = new JSONObject(response);
                        UserModel user = UserModel.create(resjs);
                        if(userAlreadyExists(user)){
                            Toast.makeText(AddSingleMemberActivity.this, R.string.add_single_group_member_user_already_exists, Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        } else {
                            showProgress(false);
                            showConfirmDialog(user);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showProgress(false);
                    }
                } else{
                    showProgress(false);
                    mEmailEditText.requestFocus();
                    mEmailEditText.setError(getString(R.string.add_group_members_user_not_found));

                }
            }
        };
    }

    private Response.ErrorListener responseErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Log.e(TAG_LOG,"VOLLEY ERROR " + error.getMessage());
                serverConnectionErrorToast();
            }
        };
    }

    private boolean userAlreadyExists(UserModel user) {
        UserGroupDAO  dao = new UserGroupDAO();
        dao.open();
        HashMap<Integer,UserModel> users = dao.getAllUsers(mGroup.getId());
        dao.close();
        return users.containsKey(user.getId());
    }

    private void sendPostRequest(UserModel user, boolean include) {
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.err_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    sendSearchRequest();
                }
            });
            showProgress(false);
            return;
        }

        String [] keys = {"idUser", "include"};
        String [] values = {String.valueOf(user.getId()), String.valueOf(include?1:0)};
        Map<String, String> params =  WebServiceRequest.createParametersMap(keys, values);
        String url = WebServiceUri.getAddGroupMemberUri(mGroup.getId()).toString();

        StringRequest req = WebServiceRequest.stringRequest(Request.Method.POST, url, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG_LOG, "RISPOSTA: " + response);
                if(!response.isEmpty()){
                    String status = "";
                    UserModel user = null;
                    UserGroupModel pivot = null;
                    try {
                        JSONObject resjs = new JSONObject(response);
                        status = resjs.getString("status");
                        JSONObject userjs = resjs.getJSONObject("data");
                        user = UserModel.create(userjs);
                        pivot = UserGroupModel.create(userjs.getJSONObject("pivot"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (status.equals("success") && user != null && pivot != null ) {
                        UserDAO dao = new UserDAO();
                        dao.open();
                        dao.insertUser(user);
                        dao.close();

                        UserGroupDAO ugdao = new UserGroupDAO();
                        ugdao.open();
                        ugdao.insertUserGroup(pivot);
                        ugdao.close();

                        Intent groupInfoIntent = new Intent(AddSingleMemberActivity.this, GroupInfoActivity.class);
                        groupInfoIntent.putExtra(GroupListFragment.PASSING_GROUP_TAG, mGroup);
                        groupInfoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(groupInfoIntent);
                        finish();
                    } else {
                        serverInternalError();
                    }

                } else {
                    serverInternalError();
                }
            }
        },  responseErrorListener());

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

    private void showConfirmDialog(final UserModel user){
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setText(R.string.add_single_member_include);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(checkBox);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(linearLayout);
        builder.setTitle(getString(R.string.add_member));
        builder.setMessage(String.format(getString(R.string.add_single_member_dialog_message), user.getFullName(), user.getEmail()));
        builder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                showProgress(true);
                sendPostRequest(user, checkBox.isChecked());
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
            }
        });

        builder.show();
    }
}
