package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.VolleyMultipartHelper;
import it.unibs.appwow.services.VolleyMultipartRequest;
import it.unibs.appwow.utils.FileUtils;
import it.unibs.appwow.utils.IdEncodingUtils;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.DividerItemDecoration;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.graphicTools.Messages;
import it.unibs.appwow.views.adapters.GroupMembersAdapter;

public class AddGroupMembersActivity extends AppCompatActivity implements GroupMembersAdapter.OnItemLongClickListener {

    private static final String TAG_LOG = AddGroupMembersActivity.class.getSimpleName();

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mMembersListView;
    private GroupMembersAdapter mAdapter;
    private View mViewContainer;


    private Button mAddMemberButton;
    private Button mCreateGroupButton;
    private EditText mEmailTextView;
    private GroupModel mGroup;
    private LocalUser mLocalUser;

    private View mProgressView;
    private View mAddMemberProgressView;
    private View mAddGroupFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //retrieving group from intent extras
        this.mGroup = getIntent().getExtras().getParcelable(AddGroupActivity.PASSING_GROUP_EXTRA);
        mLocalUser = LocalUser.load(MyApplication.getAppContext());

        setContentView(R.layout.activity_add_group_members);
        setTitle(getString(R.string.title_activity_add_group_members));
        mViewContainer = findViewById(R.id.container);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mMembersListView = (RecyclerView) findViewById(R.id.listView_members);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a grid layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mMembersListView.setLayoutManager(mLayoutManager);
        mLayoutManager.setSmoothScrollbarEnabled(true);

        mAdapter = new GroupMembersAdapter(this);
        mAdapter.setOnItemLongClickListener(this);

        mMembersListView.setAdapter(mAdapter);
        mMembersListView.setItemAnimator(new DefaultItemAnimator());
        mMembersListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        mAddMemberButton = (Button) findViewById(R.id.button_add_member);
        mCreateGroupButton = (Button) findViewById(R.id.add_group_button);
        toggleButtonAddGroupEnabled(false);

        mEmailTextView = (EditText) findViewById(R.id.email);

        mProgressView = findViewById(R.id.add_group_members_post_request_progress);
        mAddMemberProgressView = findViewById(R.id.add_group_members_add_member_pb);
        mAddGroupFormView = findViewById(R.id.add_group_members_form_container);

        //displaying the current user as the admin of group
        UserModel u = UserModel.create(mLocalUser);
        mAdapter.add(u);

    }

    private void toggleButtonAddGroupEnabled(boolean enabled){
        if(enabled){
            mCreateGroupButton.setEnabled(true);
            mCreateGroupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            mCreateGroupButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            mCreateGroupButton.setEnabled(false);
            mCreateGroupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.md_grey_300));
            mCreateGroupButton.setTextColor(ContextCompat.getColor(this, R.color.md_grey_400));
        }
    }
    private void refreshButtonAddGroupState(){
        if(minMemberNumberReached()){
            toggleButtonAddGroupEnabled(true);
        } else {
            toggleButtonAddGroupEnabled(false);
        }

    }


    private boolean minMemberNumberReached() {
        //return mDisplayedUsers.size() >= 2;
        return mAdapter.minMemberNumberReached();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return true;
        }
    }

    public void onAddGroupButtonClick(View v){
        showProgress(true);
        sendPostRequest();
    }

    public void onAddMemberButtonClick(View v){
        if(checkEmail()){
            showAddMemberProgress(true);
            sendCheckUserRequest();
        }
    }

    private boolean checkEmail() {
        String email = mEmailTextView.getText().toString();
        if(TextUtils.isEmpty(email) || !Validator.isEmailValid(email)) {
            mEmailTextView.setError(getString(R.string.error_invalid_email));
            mEmailTextView.requestFocus();
            return false;
        }
        return true;
    }

    private void sendCheckUserRequest(){
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.error_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //showAddMemberProgress(true);
                    onAddMemberButtonClick(v);
                }
            });
            showAddMemberProgress(false);
            return;
        }

        String[] keys = {"email"};
        String[] values = {mEmailTextView.getText().toString()};
        Map<String, String> requestParams = WebServiceRequest.createParametersMap(keys, values);
        StringRequest userRequest = WebServiceRequest.
                stringRequest(Request.Method.POST, WebServiceUri.CHECK_USER_URI.toString(), requestParams, responseListenerUser(), responseErrorListenerUser());

        MyApplication.getInstance().addToRequestQueue(userRequest);
    }

    private void sendPostRequest() {

        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.error_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    sendPostRequest();
                }
            });
            showProgress(false);
            return;
        }

        VolleyMultipartRequest postRequest = new VolleyMultipartRequest(Request.Method.POST, WebServiceUri.GROUPS_URI.toString(),
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = new String(response.data);
                        if (!resultResponse.isEmpty()) {
                            try {
                                GroupModel created = GroupModel.create(new JSONObject(resultResponse));
                                String fileTempPath = mGroup.getPhotoFileName();
                                if (!TextUtils.isEmpty(fileTempPath)) {
                                    Bitmap photo = FileUtils.readBitmapFromPath(fileTempPath, AddGroupMembersActivity.this);
                                    Bitmap resizedPhoto = FileUtils.resizeBitmap(photo);
                                    boolean success = FileUtils.writeGroupImage(created.getId(), resizedPhoto, AddGroupMembersActivity.this);
                                    if(success){
                                        created.setPhotoFileName(FileUtils.getGroupImageFileName(created.getId()));
                                    }
                                }
                                GroupDAO dao = new GroupDAO();
                                dao.open();
                                dao.insertGroup(created);
                                dao.close();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Toast.makeText(AddGroupMembersActivity.this, R.string.add_group_success, Toast.LENGTH_SHORT).show();
                            Messages.showSnackbar(mViewContainer,R.string.success_add_group);
                            Intent navigationActivity = new Intent(AddGroupMembersActivity.this, NavigationActivity.class);
                            navigationActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(navigationActivity);

                        } else {
                            showProgress(false);
                            Log.d(TAG_LOG, "EMPTY RESPONSE ################################################");
                            //Toast.makeText(AddGroupMembersActivity.this, R.string.add_group_error, Toast.LENGTH_SHORT).show();
                            Messages.showSnackbar(mViewContainer,R.string.error_add_group);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Log.e(TAG_LOG,"VOLLEY ERROR " + error.getMessage());
                //Toast.makeText(AddGroupMembersActivity.this, R.string.server_connection_error, Toast.LENGTH_SHORT).show();
                Messages.showSnackbar(mViewContainer,R.string.error_server_connection);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                String[] keys = {"name", "idAdmin", "users"};
                String name = mGroup.getGroupName();
                String idAdmin = String.valueOf(mGroup.getIdAdmin());
                String users = IdEncodingUtils.encodeIds(mAdapter.getItems());
                String[] values = {name, idAdmin, users,};
                Map<String, String> requestParams = WebServiceRequest.createParametersMap(keys, values);
                return requestParams;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                String photoFileName = mGroup.getPhotoFileName();
                if(!TextUtils.isEmpty(photoFileName)){
                    // file name could found file base or direct access from real path
                    //Bitmap photo = FileUtils.readTemporaryBitmap(mGroup.getPhotoFileName(), getBaseContext());
                    Bitmap photo = FileUtils.readBitmapFromPath(photoFileName, AddGroupMembersActivity.this);
                    if (photo != null) {
                        Bitmap resizedPhoto = FileUtils.resizeBitmap(photo);
                        params.put("photo", new DataPart(mGroup.getPhotoFileName(), VolleyMultipartHelper.getFileDataFromBitmap(resizedPhoto), "image/png"));
                    } else{
                        Log.d(TAG_LOG, "OPS....NULL PHOTO");
                    }

                }
                return params;
            }
        };


        MyApplication.getInstance().addToRequestQueue(postRequest);
    }

    private Response.Listener<String> responseListenerAddGroup() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    //Toast.makeText(AddGroupMembersActivity.this, R.string.add_group_success, Toast.LENGTH_SHORT).show();
                    Messages.showSnackbar(mViewContainer,R.string.success_add_group);

                    Intent navigationActivity = new Intent(AddGroupMembersActivity.this, NavigationActivity.class);
                    navigationActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(navigationActivity);
                } else {
                    showProgress(false);
                    Log.d(TAG_LOG, "EMPTY RESPONSE ################################################");
                    Messages.showSnackbar(mViewContainer,R.string.error_add_group);
                    //Toast.makeText(AddGroupMembersActivity.this, R.string.add_group_error, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private Response.ErrorListener responseErrorListenerAddGroup() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Log.e(TAG_LOG,"VOLLEY ERROR " + error.getMessage());
                //Toast.makeText(AddGroupMembersActivity.this, R.string.server_connection_error, Toast.LENGTH_SHORT).show();
                Messages.showSnackbar(mViewContainer,R.string.error_server_connection);
            }
        };
    }

    private Response.Listener<String> responseListenerUser(){
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showAddMemberProgress(false);
                if(!response.isEmpty()){
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        final int id = jsonObject.getInt("id");
                        final String fullname = jsonObject.getString("fullName");
                        final String email = jsonObject.getString("email");
                        final UserModel retrievedUser = UserModel.create(id).withEmail(email).withFullName(fullname);
                        //matchText.setText(fullname);
                        //matchText.setVisibility(View.VISIBLE);
                        //matchLabel.setVisibility(View.VISIBLE);
                        //mAddMemberButton.setVisibility(View.VISIBLE);
                        //mAddMemberButton.setOnClickListener(new View.OnClickListener() {
                        //    @Override
                        //    public void onClick(View v) {
                        //boolean userAlreadyExists = !mGroup.addUser(retrievedUser);
                        if(userAlreadyExists(retrievedUser)){
                            //Toast.makeText(AddGroupMembersActivity.this, R.string.add_group_members_user_already_added, Toast.LENGTH_SHORT).show();
                            Messages.showSnackbar(mViewContainer,R.string.error_user_already_added);
                        } else {
                            //((GroupMembersAdapter)mMembersListView.getAdapter()).add(retrievedUser);
                            mAdapter.add(retrievedUser);
                            //mAdapter.notifyDataSetChanged();
                        }
                        mEmailTextView.setText("");
                        //AGGIORNO IL MENU
                        //invalidateOptionsMenu();
                        refreshButtonAddGroupState();
                        mCreateGroupButton.requestFocus();
                        //   }
                        // });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else{
                    //Toast.makeText(AddGroupMembersActivity.this, "LocalUser not found", Toast.LENGTH_SHORT).show();
                    mEmailTextView.requestFocus();
                    mEmailTextView.setError(getString(R.string.error_user_not_found));
                }
            }
        };
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

            mAddGroupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mAddGroupFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAddGroupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mAddGroupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private boolean userAlreadyExists(UserModel retrievedUser) {
        //return mDisplayedUsers.contains(retrievedUser);
        return mAdapter.contains(retrievedUser);
    }

    private Response.ErrorListener responseErrorListenerUser(){
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showAddMemberProgress(false);
                //Log.e("Error",error.getMessage());
                //Toast.makeText(AddGroupMembersActivity.this, "Unable to process the request, try again!", Toast.LENGTH_SHORT).show();
                Messages.showSnackbar(mViewContainer,R.string.error_server_connection);
            }
        };
    }

    @Override
    public boolean onItemLongClicked(final View v, int position) {
        Log.d(TAG_LOG, "onItemLongClick position: " + position);
        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(vib.hasVibrator()) vib.vibrate(50);

        final UserModel selectedItem = (UserModel) mAdapter.getItem(position);
        final int pos = position;
        v.setSelected(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_add_group_members_member_delete_title));
        builder.setMessage(String.format(getString(R.string.message_add_group_members_member_delete), selectedItem.getFullName()));
        builder.setPositiveButton(getString(R.string.action_remove), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                mAdapter.remove(pos);
                v.setSelected(false);
            }
        });
        builder.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                v.setSelected(false);
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                v.setSelected(false);
            }
        });
        builder.show();
        return true;
    }

    private void showAddMemberProgress(boolean show){
            mAddMemberProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
