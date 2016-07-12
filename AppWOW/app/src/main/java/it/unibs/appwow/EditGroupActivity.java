package it.unibs.appwow;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.fragments.GroupListFragment;
import it.unibs.appwow.models.Debt;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.VolleyMultipartHelper;
import it.unibs.appwow.services.VolleyMultipartRequest;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.CropOption;
import it.unibs.appwow.utils.CropOptionAdapter;
import it.unibs.appwow.utils.FileUtils;
import it.unibs.appwow.utils.IdEncodingUtils;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.PermissionUtils;

public class EditGroupActivity extends AppCompatActivity {
    private static final String TAG_LOG = EditGroupActivity.class.getSimpleName();

    private static final int SELECT_PICTURE_INTENT = 1;
    private static final int PICK_FROM_CAMERA_INTENT = 2;
    private static final int CROP_FROM_CAMERA = 3;
    public static final String PASSING_GROUP_EXTRA = "group";
    private ImageView mGroupImage;
    private TextView mGroupNameView;
    private GroupModel mGroup;
    //private Bitmap mThumbnail;
    private Uri mPhotoUri;
    private String mFileName;
    private String mPreviousGroupName;

    private int mChoosenPhotoTask;
    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        mGroup = getIntent().getParcelableExtra(GroupListFragment.PASSING_GROUP_TAG);
        setTitle(getString(R.string.group_edit_activity_title) + ": " + mGroup.getGroupName());
        mGroupImage = (ImageView) findViewById(R.id.imageView2);
        Bitmap bm = FileUtils.readGroupImage(mGroup.getId(), this);
        if(bm!=null){
            mGroupImage.setImageBitmap(bm);
        }
        mGroupImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                selectImage();
                /*Intent intentPhoto = new Intent(Intent.ACTION_GET_CONTENT);
                intentPhoto.setType("image/*");
                startActivityForResult(Intent.createChooser(intentPhoto,getResources().getString(R.string.select_image_group)),SELECT_PICTURE_CODE);*/
                return false;
            }
        });
        mGroupNameView = (TextView) findViewById(R.id.group_name_field);
        mGroupNameView.setText(mGroup.getGroupName());
        mPreviousGroupName = mGroup.getGroupName();
        mFileName = "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.edit_group:
                String groupName = mGroupNameView.getText().toString();
                if(Validator.isGroupNameValid(groupName)){
                    if(isGroupChanged(groupName)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getString(R.string.group_edit_dialog_title));
                        //builder.setMessage(String.format(getString(R.string.payment_delete_message), selectedItem.getName()));
                        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                showProgressDialog();
                            }
                        });
                        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                        return true;
                    } else {
                        Toast.makeText(EditGroupActivity.this, "Nothing changed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    setNameError();
                }

            default:
                return true;
        }
    }

    private boolean isGroupChanged(String groupName) {
        return !groupName.equals(mPreviousGroupName)||!mFileName.isEmpty();
    }

    protected String getInsertedName() {
        return mGroupNameView.getText().toString();
    }

    protected String getPhotoFileName(){
        return mGroup.getPhotoFileName();
    }

    public void setNameError(){
        mGroupNameView.setError(getString(R.string.error_invalid_group_name));
        mGroupNameView.requestFocus();
    }

    private void showProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.group_edit_saving_changes));
        progressDialog.setCancelable(false);
        progressDialog.show();
        sendEditRequest(progressDialog);
    }


    private void sendEditRequest(final ProgressDialog progressDialog) {

        /*StringRequest postRequest = WebServiceRequest.
                stringRequest(Request.Method.POST, WebServiceUri.GROUPS_URI.toString(), requestParams, responseListenerAddGroup(), responseErrorListenerAddGroup());*/
        VolleyMultipartRequest postRequest = new VolleyMultipartRequest(Request.Method.POST, WebServiceUri.getGroupUri(mGroup.getId()).toString(),
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = new String(response.data);
                        if (!resultResponse.isEmpty()) {
                            Log.d(TAG_LOG, "risposta server: " + resultResponse);
                            GroupModel g = null;
                            try {
                                JSONObject obj = new JSONObject(resultResponse);
                                g = GroupModel.create(obj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (g != null) {
                                progressDialog.dismiss();
                                //DELETING TEMPORARY FILE
                                FileUtils.deleteTemporaryFile(mFileName, getBaseContext());
                                finish();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(EditGroupActivity.this, R.string.server_connection_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(EditGroupActivity.this, R.string.server_connection_error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                String[] keys = {"name"};
                String name = getInsertedName();
                String[] values = {name};
                Map<String, String> requestParams = WebServiceRequest.createParametersMap(keys, values);
                return requestParams;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                String photoFileName = getPhotoFileName();
                if(!mFileName.isEmpty()){
                    // file name could found file base or direct access from real path
                    // for now just get bitmap data from ImageView
                    params.put("photo", new DataPart(mFileName, VolleyMultipartHelper.getFileDataFromBitmap(FileUtils.readTemporaryBitmap(mFileName, getBaseContext())), "image/png"));
                }
                return params;
            }
        };


        MyApplication.getInstance().addToRequestQueue(postRequest);
    }

    private void selectImage() {
        final String takePhoto = getString(R.string.add_group_take_photo);
        final String choosePhoto =  getString(R.string.add_group_choose_photo_from_library);
        final String cancelPhoto =  getString(R.string.add_group_cancel);
        final CharSequence[] items = { takePhoto,choosePhoto, cancelPhoto};
        AlertDialog.Builder builder = new AlertDialog.Builder(EditGroupActivity.this);
        builder.setTitle(getString(R.string.add_group_add_photo_title));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = PermissionUtils.checkPermission(EditGroupActivity.this);
                if (items[item].equals(takePhoto)) {
                    mChoosenPhotoTask=TAKE_PHOTO;
                    if(result)
                        cameraIntent();
                } else if (items[item].equals(choosePhoto)) {
                    mChoosenPhotoTask=CHOOSE_PHOTO;
                    if(result)
                        galleryIntent();
                } else if (items[item].equals(cancelPhoto)) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, PICK_FROM_CAMERA_INTENT);*/
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPhotoUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".png"));
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mPhotoUri);
        try {
            intent.putExtra("return-data", true);
            startActivityForResult(intent, PICK_FROM_CAMERA_INTENT);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image_group)), SELECT_PICTURE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case PICK_FROM_CAMERA_INTENT:
                doCrop();
                break;
            case SELECT_PICTURE_INTENT:
                mPhotoUri = data.getData();
                Log.d(TAG_LOG, "OnActivityResult (SELECT_PICTURE_INTENT): PHOTOURI=" + mPhotoUri);
                doCrop();
                break;
            case CROP_FROM_CAMERA:
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    //pulisco un eventuale file temporaneo precedente
                    if(!mFileName.isEmpty()){
                        FileUtils.deleteTemporaryFile(mFileName, this);
                    }
                    mFileName = FileUtils.writeTemporaryBitmap(photo, this);
                    Log.d(TAG_LOG, "FILE NAME RETURNED: " + mFileName);
                    //Bitmap readBitmap = FileUtils.readBitmap(mFileName, this);
                    //mGroupImage.setImageBitmap(photo);
                    // TODO: 04/07/2016 scommentare riga precedente e cancellare istruzione successiva
                    mGroupImage.setImageBitmap(photo);
                }
                File f = new File(mPhotoUri.getPath());
                if (f.exists()) f.delete();
                break;

        }
    }

    private void doCrop() {
        Log.d(TAG_LOG, "doCrop method");
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        int size = list.size();
        Log.d(TAG_LOG, "method doCrop() list size: " + size);
        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
            return;
        } else {
            intent.setData(mPhotoUri);
            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();
                    co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(intent);
                    co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    cropOptions.add(co);
                }
                CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("Choose Crop App");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        startActivityForResult(cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mPhotoUri != null) {
                            getContentResolver().delete(mPhotoUri, null, null);
                            mPhotoUri = null;
                        }
                    }
                });
                android.app.AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

}
