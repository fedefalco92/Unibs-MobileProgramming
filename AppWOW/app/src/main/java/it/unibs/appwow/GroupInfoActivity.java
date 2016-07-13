package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.BufferedWriter;
import java.util.Collections;
import java.util.List;

import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.fragments.GroupListFragment;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.AmountComparator;
import it.unibs.appwow.utils.FileUtils;
import it.unibs.appwow.utils.graphicTools.RoundedImageView;

public class GroupInfoActivity extends AppCompatActivity {
    private LocalUser mLocalUser;
    private GroupModel mGroup;
    private List<Amount> mMembers;
    private GroupDAO dao;

    private View mGroupInfoContainerView;
    private View mProgressView;
    private RoundedImageView mGroupImageView;
    private TextView mGroupNameTextView;

    private TextView mMembersNumberTextView;
    private LinearLayout mMembersListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        mLocalUser = LocalUser.load(this);
        mGroup = getIntent().getParcelableExtra(GroupListFragment.PASSING_GROUP_TAG);
        dao = new GroupDAO();
        dao.open();
        mMembers = dao.getAllAmounts(mGroup.getId());
        dao.close();
        Collections.sort(mMembers, new AmountComparator(mLocalUser.getId()));
        Collections.reverse(mMembers);

        mGroupInfoContainerView = findViewById(R.id.group_info_container);
        mProgressView = findViewById(R.id.group_info_progress);


        Bitmap imageGroup =FileUtils.readGroupImage(mGroup.getId(), this);
        if(imageGroup!= null){
            mGroupImageView = (RoundedImageView) findViewById(R.id.group_info_group_photo);
            mGroupImageView.setImageBitmap(imageGroup);
        }


        mGroupNameTextView = (TextView) findViewById(R.id.group_info_group_name);
        mGroupNameTextView.setText(mGroup.getGroupName());

        mMembersNumberTextView = (TextView) findViewById(R.id.group_info_members_number);
        mMembersNumberTextView.setText(String.format(getString(R.string.group_info_members_number), mMembers.size()));

        mMembersListView = (LinearLayout) findViewById(R.id.group_info_lista_partecipanti);
        for(Amount a:mMembers){
            inflateAmount(a);
        }

        if(mLocalUser.getId() == mGroup.getIdAdmin()){
            Button deleteButton = (Button) findViewById(R.id.group_info_delete_button);
            deleteButton.setVisibility(View.VISIBLE);
        }
    }

    private void inflateAmount(Amount a){
        View v = getLayoutInflater().inflate(R.layout.user_info, null);
        TextView fullname = (TextView) v.findViewById(R.id.user_fullname);
        TextView email = (TextView) v.findViewById(R.id.user_email);
        
        String you = "";
        if(mLocalUser.getId() == a.getUserId()){
            you =  " (" +getString(R.string.you) + ")";
        }
        fullname.setText(a.getFullName() + you);
        email.setText(a.getEmail());
        if(mGroup.getIdAdmin() == a.getUserId()){
            TextView admintv = (TextView) v.findViewById(R.id.user_is_admin);
            admintv.setVisibility(View.VISIBLE);
        }

        mMembersListView.addView(v);
    }

    public void deleteGroup(View view){
        Button deleteButton = (Button) view;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.group_info_dialog_delete_title));
        builder.setMessage(getString(R.string.group_info_dialog_delete_message));
        //builder.setMessage(String.format(getString(R.string.payment_delete_message), selectedItem.getName()));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                showProgress(true);
                sendDeleteRequest();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mGroupInfoContainerView.setVisibility(show ? View.GONE : View.VISIBLE);
            mGroupInfoContainerView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mGroupInfoContainerView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mGroupInfoContainerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void sendDeleteRequest(){
        String url = WebServiceUri.getGroupUri(mGroup.getId()).toString();
        StringRequest req = WebServiceRequest.stringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    if(response.equals("1")){
                        showProgress(false);
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.group_info_group_deleted_successfully), Toast.LENGTH_SHORT).show();
                        Intent goToNavigationActivity = new Intent(GroupInfoActivity.this,NavigationActivity.class);
                        goToNavigationActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(goToNavigationActivity);
                        finish();
                    }
                    else{
                        showProgress(false);
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.group_info_group_not_present), Toast.LENGTH_SHORT).show();
                        Intent goToNavigationActivity = new Intent(GroupInfoActivity.this,NavigationActivity.class);
                        goToNavigationActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(goToNavigationActivity);
                        finish();
                    }
                }
                else{
                    showProgress(false);
                    Toast.makeText(GroupInfoActivity.this, getString(R.string.server_internal_error), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Toast.makeText(GroupInfoActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
            }
        });
        MyApplication.getInstance().addToRequestQueue(req);

    }

    public void startViewImageIntent(View v){
        /*Bitmap image= mGroupImageView.getDrawingCache();

        Bundle extras = new Bundle();
        extras.putParcelable("imagebitmap", image);
        Intent showImage = new Intent(Intent.ACTION_VIEW);
        showImage.setType("image/*");
        showImage.putExtras(extras);
        startActivity(showImage);*/
    }
}
