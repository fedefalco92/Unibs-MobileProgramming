package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.fragments.GroupListFragment;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.AmountComparator;
import it.unibs.appwow.utils.FileUtils;
import it.unibs.appwow.utils.graphicTools.Messages;
import it.unibs.appwow.utils.graphicTools.SquareImageView;

public class GroupInfoActivity extends AppCompatActivity {

    private static final String TAG_LOG = GroupInfoActivity.class.getSimpleName();
    private static final int EDIT_GROUP_NAME_INTENT = 1;
    private static final int OPEN_IMAGE_INTENT = 2;
    public static final String NO_PHOTO_EXTRA = "no_photo";

    //server errors
    private static final String PAYMENT_PRESENT = "PAYMENT_PRESENT";
    private static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    private static final String GROUP_NOT_FOUND = "GROUP_NOT_FOUND";

    private LocalUser mLocalUser;
    private GroupModel mGroup;
    private List<Amount> mMembers;
    private GroupDAO dao;

    private View mGroupInfoContainerView;
    private View mProgressView;



    //UI
    private View mViewContainer;
    private CoordinatorLayout mCoordinatorLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private boolean mScrollingDisabled;
    private SquareImageView mGroupImageView;
    private TextView mGroupNameTextView;
    private TextView mMembersNumberTextView;
    private LinearLayout mMembersListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        mViewContainer = findViewById(R.id.main_container);

        mLocalUser = LocalUser.load(this);
        mGroup = getIntent().getParcelableExtra(GroupListFragment.PASSING_GROUP_TAG);
        loadMembers();

        initActivityTransitions();
        mScrollingDisabled = false;

        mGroupInfoContainerView = findViewById(R.id.group_info_container);
        mProgressView = findViewById(R.id.group_info_progress);


        /*
        Bitmap imageGroup =FileUtils.readGroupImage(mGroup.getId(), this);
        if(imageGroup!= null){
            mGroupImageView = (RoundedImageView) findViewById(R.id.group_info_group_photo);
            mGroupImageView.setImageBitmap(imageGroup);
        }*/

        //mGroupNameTextView = (TextView) findViewById(R.id.group_info_group_name);
        //mGroupNameTextView.setText(mGroup.getGroupName());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbarLayout.setTitle(mGroup.getGroupName());
        mCollapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(this, android.R.color.white));

        mGroupImageView = (SquareImageView) findViewById(R.id.group_info_group_photo);
        reloadPhoto();
        //TextView title = (TextView) findViewById(R.id.group_info_group_name);
        //title.setText(mGroup.getGroupName());


        mMembersNumberTextView = (TextView) findViewById(R.id.group_info_members_number);

        mMembersListView = (LinearLayout) findViewById(R.id.group_info_lista_partecipanti);
        loadMembersList();

        if (mLocalUser.getId() == mGroup.getIdAdmin()) {
            Button deleteButton = (Button) findViewById(R.id.group_info_delete_button);
            deleteButton.setVisibility(View.VISIBLE);

            Button resetButton = (Button) findViewById(R.id.group_info_reset_button);
            resetButton.setVisibility(View.VISIBLE);

            View addMemberRow = findViewById(R.id.group_info_add_member_row);
            addMemberRow.setVisibility(View.VISIBLE);
            addMemberRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAddMemberActivity();
                }
            });
        }


    }

    private void loadMembers(){
        dao = new GroupDAO();
        dao.open();
        mMembers = dao.getAllAmounts(mGroup.getId());
        dao.close();
        Collections.sort(mMembers, new AmountComparator(mLocalUser.getId()));
        Collections.reverse(mMembers);
    }

    private void loadMembersList(){
        mMembersNumberTextView.setText(String.format(getString(R.string.group_info_members_number), mMembers.size()));
        mMembersListView.removeAllViews();
        for (Amount a : mMembers) {
            View v = inflateAmount(a);
            int idAdmin = mGroup.getIdAdmin();
            if (mMembers.size() > 2) {
                if (mLocalUser.getId() == idAdmin) {
                    if (a.getUserId() != idAdmin) {
                        v.setOnLongClickListener(onMemberLongClickListener(a));
                    }
                }
            } else {
                if (mLocalUser.getId() == idAdmin) {
                    if (a.getUserId() != idAdmin) {
                        v.setOnLongClickListener(onMemberErrorLongClickListener(a));
                    }
                }
            }
        }
    }

    private void reloadPhoto() {
        File file = FileUtils.getGroupImageFile(mGroup.getId(), this);
        if (file != null) {
            String fileUri = "file://" + file.getPath();
            Picasso.with(this).invalidate(fileUri);
            Picasso.with(this).load(fileUri).into(mGroupImageView, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap bitmap = ((BitmapDrawable) mGroupImageView.getDrawable()).getBitmap();
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            applyPalette(palette);
                        }
                    });
                }

                @Override
                public void onError() {
                    Log.d(TAG_LOG, "ERROR WHILE LOADING IMAGE");
                }
            });

            mGroupImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFullScreenImage();
                }
            });
        } else {
            disableScrolling();
            mScrollingDisabled = true;
        }
    }

    private View.OnLongClickListener onMemberLongClickListener(final Amount a) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                v.setSelected(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(getString(R.string.group_info_dialog_delete_member_title));
                builder.setMessage(String.format(getString(R.string.group_info_dialog_delete_member_message), a.getFullName(), a.getEmail()));
                //builder.setMessage(String.format(getString(R.string.payment_delete_message), selectedItem.getName()));
                builder.setPositiveButton(getString(R.string.remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        showProgress(true);
                        //sendRemoveMemberRequest((Integer) v.getTag());
                        sendRemoveMemberRequest(a);
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        v.setSelected(false);
                        dialog.dismiss();
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
        };
    }

    private View.OnLongClickListener onMemberErrorLongClickListener(final Amount a) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                v.setSelected(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(getString(R.string.group_info_dialog_delete_member_error_title));
                builder.setMessage(String.format(getString(R.string.group_info_dialog_delete_member_error_message), a.getFullName(), a.getEmail()));
                //builder.setMessage(String.format(getString(R.string.payment_delete_message), selectedItem.getName()));
                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {

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
        };
    }

    private void startAddMemberActivity() {
        Intent addSingleMember = new Intent(GroupInfoActivity.this, AddSingleMemberActivity.class);
        addSingleMember.putExtra(GroupListFragment.PASSING_GROUP_TAG, mGroup);
        startActivity(addSingleMember);

        //Toast.makeText(GroupInfoActivity.this, "Aggiungi membrooooo", Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void disableScrolling() {
        /*mCollapsingToolbarLayout.setActivated(false);
        mCollapsingToolbarLayout.setEnabled(false);
        mCollapsingToolbarLayout.setNestedScrollingEnabled(false);*/
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        mAppBarLayout.setExpanded(false, false);
        mAppBarLayout.setActivated(false);

        // mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        //mCoordinatorLayout.setNestedScrollingEnabled(false);
        //mGroupImageView.setVisibility(View.GONE);

        /*int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        lp.height = px;
        mAppBarLayout.setLayoutParams(lp);*/
        //mCollapsingToolbarLayout.setTitle(mGroup.getGroupName());
        //mCollapsingToolbarLayout.setTitleEnabled(false);
        //mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //mToolbar.setTitle(mGroup.getGroupName());
        getSupportActionBar().setTitle(mGroup.getGroupName());
    }

    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReturnTransition(transition);
        }
    }

    private void applyPalette(Palette palette) {
        int primaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        int primary = ContextCompat.getColor(this, R.color.colorPrimary);
        mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
        mCollapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));
        supportStartPostponedEnterTransition();
        /*updateBackground((FloatingActionButton) findViewById(R.id.fab_edit), palette);
        supportStartPostponedEnterTransition();

        Palette.Swatch swatch = palette.getVibrantSwatch();
        if(swatch != null){
            mCollapsingToolbarLayout.setExpandedTitleColor(swatch.getTitleTextColor());

        }*/
    }

    private void updateBackground(FloatingActionButton fab, Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(ContextCompat.getColor(this, android.R.color.white));
        int vibrantColor = palette.getVibrantColor(ContextCompat.getColor(this, R.color.colorAccent));

        fab.setRippleColor(lightVibrantColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        try {
            return super.dispatchTouchEvent(motionEvent);
        } catch (NullPointerException e) {
            return false;
        }
    }

    private View inflateAmount(Amount a) {
        View v = getLayoutInflater().inflate(R.layout.user_info, null);
        TextView fullname = (TextView) v.findViewById(R.id.user_fullname);
        TextView email = (TextView) v.findViewById(R.id.user_email);

        String you = "";
        if (mLocalUser.getId() == a.getUserId()) {
            you = " (" + getString(R.string.you) + ")";
        }
        fullname.setText(a.getFullName() + you);
        email.setText(a.getEmail());
        if (mGroup.getIdAdmin() == a.getUserId()) {
            TextView admintv = (TextView) v.findViewById(R.id.user_is_admin);
            admintv.setVisibility(View.VISIBLE);
        }

        mMembersListView.addView(v);
        return v;
    }

    public void deleteGroup(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.group_info_dialog_delete_title));
        builder.setMessage(getString(R.string.group_info_dialog_delete_message));
        //builder.setMessage(String.format(getString(R.string.payment_delete_message), selectedItem.getName()));
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                showProgress(true);
                sendDeleteRequest();
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

    public void resetGroup(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.group_info_dialog_reset_title));
        builder.setMessage(getString(R.string.group_info_dialog_reset_message));
        //builder.setMessage(String.format(getString(R.string.payment_delete_message), selectedItem.getName()));
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                showProgress(true);
                sendResetRequest();
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

    private void sendDeleteRequest() {
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.err_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    sendDeleteRequest();
                }
            });
            showProgress(false);
            return;
        }

        String url = WebServiceUri.getGroupUri(mGroup.getId()).toString();
        StringRequest req = WebServiceRequest.stringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    if (response.equals("1")) {
                        showProgress(false);
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.group_info_group_deleted_successfully), Toast.LENGTH_SHORT).show();
                        Intent goToNavigationActivity = new Intent(GroupInfoActivity.this, NavigationActivity.class);
                        goToNavigationActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(goToNavigationActivity);
                        finish();
                    } else {
                        showProgress(false);
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.group_info_group_not_present), Toast.LENGTH_SHORT).show();
                        Intent goToNavigationActivity = new Intent(GroupInfoActivity.this, NavigationActivity.class);
                        goToNavigationActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(goToNavigationActivity);
                        finish();
                    }
                } else {
                    serverInternalError();
                }

            }
        }, responseErrorListener());
        MyApplication.getInstance().addToRequestQueue(req);

    }

    private void sendResetRequest() {
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.err_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    sendResetRequest();
                }
            });
            showProgress(false);
            return;
        }

        String url = WebServiceUri.getGroupResetUri(mGroup.getId()).toString();
        StringRequest req = WebServiceRequest.stringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    String success = "";
                    try {
                        JSONObject resjs = new JSONObject(response);
                        success = resjs.getString("status");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (success.equals("success")) {
                        showProgress(false);
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.group_info_group_reset_successfully), Toast.LENGTH_SHORT).show();
                    } else {
                        showProgress(false);
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.group_info_group_not_present), Toast.LENGTH_SHORT).show();
                        Intent goToNavigationActivity = new Intent(GroupInfoActivity.this, NavigationActivity.class);
                        goToNavigationActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(goToNavigationActivity);
                        finish();
                    }
                } else {
                    serverInternalError();
                }

            }
        }, responseErrorListener());
        MyApplication.getInstance().addToRequestQueue(req);

    }

    private void sendRemoveMemberRequest(final Amount a) {
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.err_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    sendRemoveMemberRequest(a);
                }
            });
            showProgress(false);
            return;
        }

        String url = WebServiceUri.getRemoveGroupMemberUri(mGroup.getId()).toString();
        String[] keys = {"idUser"};
        String[] values = {String.valueOf(a.getUserId())};
        Map<String, String> params = WebServiceRequest.createParametersMap(keys, values);
        StringRequest req = WebServiceRequest.stringRequest(Request.Method.POST, url, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG_LOG, "RISPOSTA REMOVE MEMBER" + response);
                if (!response.isEmpty()) {
                    String status = "";
                    JSONObject resjs = null;
                    try {
                        resjs = new JSONObject(response);
                        status = resjs.getString("status");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (status.equals("success")) {

                        //rimuovo l'utente dalla tabella UserGroup LOCALE
                        UserGroupDAO ugdao = new UserGroupDAO();
                        ugdao.open();
                        int affectedRows = ugdao.removeUserFromGroup(a.getUserId(), mGroup.getId());
                        if (affectedRows == 1) {
                            //se l'utente non è più in nessun gruppo lo rimuovo
                            boolean groupPresent = ugdao.isUserInGroups(a.getUserId());
                            ugdao.close();
                            if (!groupPresent) {
                                UserDAO udao = new UserDAO();
                                udao.open();
                                udao.deleteSingleUser(a.getUserId());
                                udao.close();
                                Log.d(TAG_LOG, "user removed " + a.getUserId());
                            }
                        }
                        showProgress(false);
                        loadMembers();
                        loadMembersList();
                    } else {
                        if (!resjs.isNull("type")) {
                            String error = "";
                            try {
                                error = resjs.getString("type");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            switch (error) {
                                case PAYMENT_PRESENT:
                                    UserDAO dao = new UserDAO();
                                    dao.open();
                                    String [] userInfo = dao.getSingleUserInfo(a.getUserId());
                                    dao.close();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                                    builder.setTitle(getString(R.string.group_info_dialog_cannot_delete_member_title));
                                    builder.setMessage(getString(R.string.group_info_dialog_cannot_delete_member_message, userInfo[0]));
                                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int item) {
                                        }
                                    });
                                    builder.show();
                                    break;
                                case USER_NOT_FOUND:
                                    Toast.makeText(GroupInfoActivity.this, getString(R.string.group_info_user_not_present), Toast.LENGTH_SHORT).show();
                                    Intent goToGroupDetails = new Intent(GroupInfoActivity.this, GroupDetailsActivity.class);
                                    goToGroupDetails.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(goToGroupDetails);
                                    finish();

                                    break;
                                case GROUP_NOT_FOUND:
                                    Toast.makeText(GroupInfoActivity.this, getString(R.string.group_info_group_not_present), Toast.LENGTH_SHORT).show();
                                    Intent goToNavigationActivity = new Intent(GroupInfoActivity.this, NavigationActivity.class);
                                    goToNavigationActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(goToNavigationActivity);
                                    finish();
                                    break;
                            }
                        }
                        showProgress(false);
                    }
                } else {
                    serverInternalError();
                }
            }
        }, responseErrorListener());
        Log.d(TAG_LOG, "RICHIESTA REMOVE MEMBER: " + req);
        MyApplication.getInstance().addToRequestQueue(req);

    }

    private Response.ErrorListener responseErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Toast.makeText(GroupInfoActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void serverInternalError() {
        showProgress(false);
        Toast.makeText(GroupInfoActivity.this, getString(R.string.server_internal_error), Toast.LENGTH_SHORT).show();
    }

    private void openFullScreenImage() {
        Intent openFullScreen = new Intent(GroupInfoActivity.this, ImageViewFullscreenActivity.class);
        openFullScreen.putExtra(GroupListFragment.PASSING_GROUP_TAG, mGroup);
        this.startActivityForResult(openFullScreen, OPEN_IMAGE_INTENT);
        /*
        File file = FileUtils.getGroupImageFile(mGroup.getId(), this);
        if (file != null) {
            /*Log.d(TAG_LOG, "file path: " + file.getPath());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + file.getPath()),"image/*");
            startActivity(intent);
        }*/
        /*Bitmap image= mGroupImageView.getDrawingCache();

        Bundle extras = new Bundle();
        extras.putParcelable("imagebitmap", image);
        Intent showImage = new Intent(Intent.ACTION_VIEW);
        showImage.setType("image/*");
        showImage.putExtras(extras);
        startActivity(showImage);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.edit) {
            Intent editGroupNameIntent = new Intent(this, EditGroupNameActivity.class);
            //editGroupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            editGroupNameIntent.putExtra(EditGroupNameActivity.GROUP_ID_EXTRA, mGroup.getId());
            editGroupNameIntent.putExtra(EditGroupNameActivity.GROUP_NAME_EXTRA, mGroup.getGroupName());
            startActivityForResult(editGroupNameIntent, EDIT_GROUP_NAME_INTENT);
        }

        if (id == R.id.add_photo) {
            openFullScreenImage();
        }

        if (id == R.id.add_member) {
            startAddMemberActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_edit, menu);

        if(mScrollingDisabled) menu.findItem(R.id.add_photo).setVisible(true);
        if (mLocalUser.getId() == mGroup.getIdAdmin()) {
            menu.findItem(R.id.add_member).setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case EDIT_GROUP_NAME_INTENT:
                String newGroupName = data.getStringExtra(EditGroupNameActivity.GROUP_NAME_EXTRA);
                if (newGroupName != null) {
                    mGroup.setGroupName(newGroupName);
                    mCollapsingToolbarLayout.setTitle(mGroup.getGroupName());
                }
                break;
            case OPEN_IMAGE_INTENT:
                boolean photoUpdated = data.getBooleanExtra(ImageViewFullscreenActivity.PHOTO_UPDATED_BOOLEAN_EXTRA, false);
                Log.d(TAG_LOG, "PHOTO UPDATED: " + photoUpdated);
                if(photoUpdated){
                    if(mScrollingDisabled) recreate();
                    else reloadPhoto();
                }
                break;
        }
    }
}
