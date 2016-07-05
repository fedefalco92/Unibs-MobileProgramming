package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.VolleyMultipartHelper;
import it.unibs.appwow.services.VolleyMultipartRequest;
import it.unibs.appwow.utils.FileUtils;
import it.unibs.appwow.utils.IdEncodingUtils;
import it.unibs.appwow.views.adapters.GroupMembersAdapter;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;

public class AddGroupMembersActivity extends AppCompatActivity{

    private static final String TAG_LOG = AddGroupMembersActivity.class.getSimpleName();

    private ListView membersList;
    private GroupMembersAdapter mAdapter;
    //private TextView matchLabel;
    //private TextView matchText;
    private Button mAddMemberButton;
    private MenuItem mCreateGroupButton;
    private EditText mEmailTextView;
    private GroupModel mGroup;
    private LocalUser mLocalUser;
    //private ArrayList<UserModel> mDisplayedUsers;
    private Set<UserModel> mSelectedItems;

    private View mProgressView;
    private View mAddGroupFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //retrieving group from intent extras
        this.mGroup = getIntent().getExtras().getParcelable(AddGroupActivity.PASSING_GROUP_EXTRA);
        //IMPORTANT: parceling does not save the HashMap mUsers which will be null after getParcelable(...)
        mLocalUser = LocalUser.load(MyApplication.getAppContext());
        //mLocalUser.setIsGroupAdmin();
        //mGroup.addUser(currentUser);

        mSelectedItems = new HashSet<UserModel>();
        //mDisplayedUsers = new ArrayList<UserModel>();

        setContentView(R.layout.activity_add_group_members);
        setTitle(getString(R.string.add_group_members_activity_title));

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        membersList = (ListView) findViewById(R.id.listView_members);
        //matchLabel = (TextView) findViewById(R.id.match_label);
        //matchText = (TextView) findViewById(R.id.username_found);
        mAddMemberButton = (Button) findViewById(R.id.button_add_member);
        mEmailTextView = (EditText) findViewById(R.id.email);

        mProgressView = findViewById(R.id.add_group_members_post_request_progress);
        mAddGroupFormView = findViewById(R.id.add_group_members_form_container);

        membersList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //membersList.setAdapter(new ArrayAdapter<LocalUser>(this,android.R.layout.simple_list_item_multiple_choice,mDisplayedUsers));
        //mAdapter = new GroupMembersAdapter(this, mDisplayedUsers);
        mAdapter = new GroupMembersAdapter(this);
        membersList.setAdapter(mAdapter);
        /*
        if(LocalUser.load(this) != null){
            LocalUser loggedUser = LocalUser.load(this);
            loggedUser.setmAdmin();
            ((GroupMembersAdapter)membersList.getAdapter()).add(loggedUser);
        }*/

        //displaying the current user as the admin of group
        UserModel u = UserModel.create(mLocalUser);
        mAdapter.add(u);

        //LocalUser adminUser = mGroup.getAdminUser();
        //adminUser.setIsGroupAdmin();
        //((GroupMembersAdapter)membersList.getAdapter()).add(adminUser);

        membersList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        membersList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB

                GroupMembersAdapter adapter = (GroupMembersAdapter) membersList.getAdapter();
                String title = "";
                if(checked){
                    mSelectedItems.add((UserModel)adapter.getItem(position));
                }
                else{
                    mSelectedItems.remove((UserModel)adapter.getItem(position));
                }

                if(mSelectedItems.size() == 1){
                    title = "1 selected item";
                }
                else{
                    title = mSelectedItems.size()+" selected items";
                }
                mode.setTitle(title);
            }

            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu_selection, menu);
                // toolbar.setVisibility(View.GONE); // FIXME: 24/05/16 trovare soluzione piu' furba?
                return true;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()){
                    case R.id.context_delete:
                        Iterator iterator = mSelectedItems.iterator();
                        while(iterator.hasNext()){
                            UserModel toRemove = (UserModel) iterator.next();
                            //mDisplayedUsers.remove(toRemove);
                            mAdapter.remove(toRemove);
                            //mGroup.removeUser(toRemove);
                            //Log.d(TAG_LOG,"UTENTE RIMOSSO: " + toRemove + "; mGroup.size = " + mGroup.getUsersCount());
                            //AGGIORNO IL MENU
                            invalidateOptionsMenu();
                        }
                        ((GroupMembersAdapter)membersList.getAdapter()).notifyDataSetChanged();
                        mode.finish();
                        return true;
                    default:
                        mode.finish();
                        return true;
                }
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                // toolbar.setVisibility(View.);
                //GroupMembersAdapter adapter = (GroupMembersAdapter)membersList.getAdapter();
                mSelectedItems.clear();
                //adapter.notifyDataSetChanged();
                mAdapter.notifyDataSetChanged();
            }

            });

        mAddMemberButton = (Button) findViewById(R.id.button_add_member);
        mAddMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(WebServiceRequest.checkNetwork()) {
                    String[] keys = {"email"};
                    String[] values = {mEmailTextView.getText().toString()};
                    Map<String, String> requestParams = WebServiceRequest.createParametersMap(keys, values);
                    StringRequest userRequest = WebServiceRequest.
                            stringRequest(Request.Method.POST, WebServiceUri.CHECK_USER_URI.toString(), requestParams, responseListenerUser(), responseErrorListenerUser());
                    MyApplication.getInstance().addToRequestQueue(userRequest);
                } else {
                    Toast.makeText(AddGroupMembersActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private boolean minMemberNumberReached() {
        //return mDisplayedUsers.size() >= 2;
        return mAdapter.minMemberNumberReached();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_group, menu);
        mCreateGroupButton = (MenuItem) menu.findItem(R.id.create_group);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(minMemberNumberReached()){
            mCreateGroupButton.setEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.create_group:
                sendPostRequest();
                return true;
            default:
                return true;
        }
    }

    private void sendPostRequest() {
        showProgress(true);
        String[] keys = {"name", "idAdmin", "users"};
        String name = mGroup.getGroupName();
        String idAdmin = String.valueOf(mGroup.getIdAdmin());
        String users = IdEncodingUtils.encodeIds(mAdapter.getItems());
        String[] values = {name, idAdmin, users,};


        Map<String, String> requestParams = WebServiceRequest.createParametersMap(keys, values);


        /*StringRequest postRequest = WebServiceRequest.
                stringRequest(Request.Method.POST, WebServiceUri.GROUPS_URI.toString(), requestParams, responseListenerAddGroup(), responseErrorListenerAddGroup());*/
        VolleyMultipartRequest postRequest = new VolleyMultipartRequest(Request.Method.POST, WebServiceUri.GROUPS_URI.toString(),
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = new String(response.data);
                        if (!resultResponse.isEmpty()) {
                            Toast.makeText(AddGroupMembersActivity.this, R.string.add_group_success, Toast.LENGTH_SHORT).show();
                            Intent navigationActivity = new Intent(AddGroupMembersActivity.this, NavigationActivity.class);
                            navigationActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(navigationActivity);
                        } else {
                            showProgress(false);
                            Log.d(TAG_LOG, "EMPTY RESPONSE ################################################");
                            Toast.makeText(AddGroupMembersActivity.this, R.string.add_group_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Log.e(TAG_LOG,"VOLLEY ERROR " + error.getMessage());
                Toast.makeText(AddGroupMembersActivity.this, R.string.server_connection_error, Toast.LENGTH_SHORT).show();
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
                    // file name could found file base or direct access from real path
                    // for now just get bitmap data from ImageView
                    params.put("photo", new DataPart(mGroup.getPhotoFileName(), VolleyMultipartHelper.getFileDataFromBitmap(FileUtils.readBitmap(mGroup.getPhotoFileName(), getBaseContext())), "image/png"));


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
                    Toast.makeText(AddGroupMembersActivity.this, R.string.add_group_success, Toast.LENGTH_SHORT).show();
                    Intent navigationActivity = new Intent(AddGroupMembersActivity.this, NavigationActivity.class);
                    navigationActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(navigationActivity);
                } else {
                    showProgress(false);
                    Log.d(TAG_LOG, "EMPTY RESPONSE ################################################");
                    Toast.makeText(AddGroupMembersActivity.this, R.string.add_group_error, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AddGroupMembersActivity.this, R.string.server_connection_error, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private Response.Listener<String> responseListenerUser(){
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
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
                            Toast.makeText(AddGroupMembersActivity.this, R.string.add_group_members_user_already_added, Toast.LENGTH_SHORT).show();
                        } else {
                            //((GroupMembersAdapter)membersList.getAdapter()).add(retrievedUser);
                            mAdapter.add(retrievedUser);
                        }


                        //matchText.setVisibility(View.INVISIBLE);
                        //matchLabel.setVisibility(View.INVISIBLE);
                        //mAddMemberButton.setVisibility(View.INVISIBLE);
                        mEmailTextView.setText("");
                        //AGGIORNO IL MENU
                        invalidateOptionsMenu();
                        //   }
                       // });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else{
                    //Toast.makeText(AddGroupMembersActivity.this, "LocalUser not found", Toast.LENGTH_SHORT).show();
                    mEmailTextView.requestFocus();
                    mEmailTextView.setError(getString(R.string.add_group_members_user_not_found));
                    //matchText.setVisibility(View.INVISIBLE);
                    //matchLabel.setVisibility(View.INVISIBLE);
                    //mAddMemberButton.setVisibility(View.INVISIBLE);
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
                //Log.e("Error",error.getMessage());
                Toast.makeText(AddGroupMembersActivity.this, "Unable to process the request, try again!", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
