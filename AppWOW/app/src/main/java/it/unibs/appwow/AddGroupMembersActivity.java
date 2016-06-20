package it.unibs.appwow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.LocalUser;
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
    private TextView mEmailTextView;
    private GroupModel mGroup;
    private LocalUser mLocalUser;
    private ArrayList<UserModel> mDisplayedUsers;
    private Set<UserModel> mSelectedItems;

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
        mDisplayedUsers = new ArrayList<UserModel>();

        setContentView(R.layout.activity_add_group_members);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        membersList = (ListView) findViewById(R.id.listView_members);
        //matchLabel = (TextView) findViewById(R.id.match_label);
        //matchText = (TextView) findViewById(R.id.username_found);
        mAddMemberButton = (Button) findViewById(R.id.button_add_member);
        mEmailTextView = (TextView) findViewById(R.id.email);

        membersList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //membersList.setAdapter(new ArrayAdapter<LocalUser>(this,android.R.layout.simple_list_item_multiple_choice,mDisplayedUsers));
        mAdapter = new GroupMembersAdapter(this, mDisplayedUsers);
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
                            LocalUser toRemove = (LocalUser) iterator.next();
                            mDisplayedUsers.remove(toRemove);
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
                GroupMembersAdapter adapter = (GroupMembersAdapter)membersList.getAdapter();
                mSelectedItems.clear();
                adapter.notifyDataSetChanged();
            }

            });

        /*
        membersList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // here you can do something when items are selected/de-selected
                // such as update the title in the CAB
                String title = "";
                if(checked){
                    mSelectedItems.add((LocalUser)mAdapter.getItem(position));
                }
                else{
                    mSelectedItems.remove((LocalUser)mAdapter.getItem(position));
                }

                if(mSelectedItems.size() == 1){
                    title = "1 item";
                }
                else{
                    title = mSelectedItems.size()+" items";
                }
                mode.setTitle(title);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.context_menu_selection, menu);
                mode.setTitle("selection");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // here you can perform updates to the CAB due to an invalidate() request
                return false;
            }


            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()){
                    case R.id.context_delete:
                        Iterator iterator = mSelectedItems.iterator();
                        while(iterator.hasNext()){
                            mDisplayedUsers.remove(iterator.next());
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
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        */

        //Button verifyEmail = ...
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
                }
            }
        });

    }

    private boolean minMemberNumberReached() {
        return mDisplayedUsers.size() >= 2;
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
            case R.id.create_group:
                // TODO: 19/05/2016 creare il gruppo
                Toast.makeText(AddGroupMembersActivity.this, "Creazione gruppo da implementare", Toast.LENGTH_SHORT).show();
                //final Intent registrationIntent = new Intent(AddGroupMembersActivity.this, AddGroupMembersActivity.class);
                //startActivityForResult(registrationIntent, REGISTRATION_REQUEST_ID);
                //startActivity(registrationIntent);
                return true;
            default:
                return true;
        }
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
                            Toast.makeText(AddGroupMembersActivity.this, "Utente già inserito", Toast.LENGTH_SHORT).show();
                        } else {
                            ((GroupMembersAdapter)membersList.getAdapter()).add(retrievedUser);
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
                }
                else{
                    //Toast.makeText(AddGroupMembersActivity.this, "LocalUser not found", Toast.LENGTH_SHORT).show();
                    mEmailTextView.requestFocus();
                    mEmailTextView.setError(getString(R.string.user_not_found));
                    //matchText.setVisibility(View.INVISIBLE);
                    //matchLabel.setVisibility(View.INVISIBLE);
                    //mAddMemberButton.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    private boolean userAlreadyExists(UserModel retrievedUser) {
        return mDisplayedUsers.contains(retrievedUser);
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
