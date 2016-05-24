package it.unibs.appwow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.view.ActionMode;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
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

import it.unibs.appwow.graphicTools.GroupMembersAdapter;
import it.unibs.appwow.model.parc.User;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;

public class AddGroupMembersActivity extends AppCompatActivity{

    private ListView membersList;
    private TextView matchLabel;
    private TextView matchText;
    private Button addMember;
    private TextView emailTV;

    private ArrayList<User> users;

    private Set<User> mSelectedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedItems = new HashSet<User>();
        users = new ArrayList<User>();

        setContentView(R.layout.activity_add_group_members);

        membersList = (ListView) findViewById(R.id.listView_members);
        matchLabel = (TextView) findViewById(R.id.match_label);
        matchText = (TextView) findViewById(R.id.username_found);
        addMember = (Button) findViewById(R.id.button_add_member);
        emailTV = (TextView) findViewById(R.id.email);

        membersList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //membersList.setAdapter(new ArrayAdapter<User>(this,android.R.layout.simple_list_item_multiple_choice,users));
        membersList.setAdapter(new GroupMembersAdapter(this,users));

        if(User.load(this) != null){
            User loggedUser = User.load(this);
            loggedUser.setmAdmin();
            ((GroupMembersAdapter)membersList.getAdapter()).add(loggedUser);
        }

        membersList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {


            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // here you can do something when items are selected/de-selected
                // such as update the title in the CAB
                //Log.d("qui","qui");
                GroupMembersAdapter adapter = (GroupMembersAdapter)membersList.getAdapter();
                String title = "";
                if(checked){
                    mSelectedItems.add((User)adapter.getItem(position));
                }
                else{
                    mSelectedItems.remove((User)adapter.getItem(position));
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
                            users.remove(iterator.next());
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

        Button verifyMail = (Button) findViewById(R.id.verify_member);
        verifyMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(WebServiceRequest.checkNetwork()) {
                    String[] keys = {"email"};
                    String[] values = {emailTV.getText().toString()};
                    Map<String, String> requestParams = WebServiceRequest.createParametersMap(keys, values);
                    StringRequest userRequest = WebServiceRequest.
                            stringRequest(Request.Method.POST, WebServiceUri.CHECK_USER_URI.toString(), requestParams, responseListenerUser(), responseErrorListenerUser());
                    MyApplication.getInstance().addToRequestQueue(userRequest);

                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.create_group:
                // TODO: 19/05/2016 creare il gruppo
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
                //Log.d("Response",response);
                if(!response.isEmpty()){
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        final int id = jsonObject.getInt("id");
                        final String fullname = jsonObject.getString("fullName");
                        final String email = jsonObject.getString("email");
                        final User retrievedUser = User.create(id).withEmail(email).withFullName(fullname);
                        matchText.setText(fullname);
                        matchText.setVisibility(View.VISIBLE);
                        matchLabel.setVisibility(View.VISIBLE);
                        addMember.setVisibility(View.VISIBLE);
                        addMember.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((GroupMembersAdapter)membersList.getAdapter()).add(retrievedUser);
                                matchText.setVisibility(View.INVISIBLE);
                                matchLabel.setVisibility(View.INVISIBLE);
                                addMember.setVisibility(View.INVISIBLE);
                                emailTV.setText("");

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(AddGroupMembersActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    matchText.setVisibility(View.INVISIBLE);
                    matchLabel.setVisibility(View.INVISIBLE);
                    addMember.setVisibility(View.INVISIBLE);
                }
            }
        };
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
