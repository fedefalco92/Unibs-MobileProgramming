package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unibs.appwow.fragments.GroupListFragment;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.services.VolleyMultipartHelper;
import it.unibs.appwow.services.VolleyMultipartRequest;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.CapturePhotoUtils;
import it.unibs.appwow.utils.CropOption;
import it.unibs.appwow.utils.FileUtils;
import it.unibs.appwow.utils.graphicTools.Messages;
import it.unibs.appwow.utils.graphicTools.PermissionUtils;
import it.unibs.appwow.utils.graphicTools.SquareImageView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ImageViewFullscreenActivity extends AppCompatActivity {
    private static final String TAG_LOG = ImageViewFullscreenActivity.class.getSimpleName();

    private static final int SELECT_PICTURE_INTENT = 1;
    private static final int PICK_FROM_CAMERA_INTENT = 2;
    private static final int CROP_INTENT = 3;
    public static final String PHOTO_UPDATED_BOOLEAN_EXTRA = "changed";
    private static final int PHOTO_SIZE_PX =400;

    // UI
    private View mViewContainer;
    private SquareImageView mPhotoView;
    private View mProgressView;

    private GroupModel mGroup;

    private Uri mPhotoUri;
    private String mFileName;
    private Bitmap mBitmap;

    private boolean mNoPhoto;
    private boolean mPhotoUpdated;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_view_fullscreen);
        mViewContainer = findViewById(R.id.main_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black));

        mGroup = getIntent().getParcelableExtra(GroupListFragment.PASSING_GROUP_TAG);
        setTitle(mGroup.getGroupName());

        mPhotoView = (SquareImageView) findViewById(R.id.photo);

        mProgressView = findViewById(R.id.progress_bar);

        mPhotoUpdated = false;
        mFileName = "";

        mNoPhoto = false;
        mBitmap = FileUtils.readGroupImage(mGroup.getId(), this);
        if (mBitmap==null) {
            //showProgress(true);
            mNoPhoto = true;
            selectImage();
        } else {
            mPhotoView.setImageBitmap(mBitmap);
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent data = new Intent();
            data.putExtra(PHOTO_UPDATED_BOOLEAN_EXTRA, mPhotoUpdated);
            Log.d(TAG_LOG, "PHOTO UPDATED:" + mPhotoUpdated);
            if (getParent() == null) {
                setResult(Activity.RESULT_OK, data);
            } else {
                getParent().setResult(Activity.RESULT_OK, data);
            }
            finish();
            return true;
        }

        if (id == R.id.edit) {
            selectImage();
        }

        if (id == R.id.crop) {
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, "temp_" + System.currentTimeMillis(), null);
            mPhotoUri = Uri.parse(path);
            doCrop();
        }

        if (id == R.id.download) {
            //Toast.makeText(ImageViewFullscreenActivity.this, "GESTIRE CARICAMENTO FOTO", Toast.LENGTH_SHORT).show();
            String title = mGroup.getGroupName() + "_photo_" + System.currentTimeMillis() + ".png";
            String newfile = CapturePhotoUtils.insertImage(getContentResolver(), mBitmap, title, "");
            Log.d(TAG_LOG, "new file: " + newfile);
            Toast.makeText(ImageViewFullscreenActivity.this, getString(R.string.success_image_saved_to_gallery), Toast.LENGTH_SHORT).show();
        }


        if (id == R.id.share) {

            Bitmap bitmap = FileUtils.readGroupImage(mGroup.getId(),this);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "temp_" + System.currentTimeMillis(), null);
            Uri bitmapUri = Uri.parse(path);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");

            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
            shareIntent.setType("image/png");

            startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share_image)));
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectImage() {
        final String takePhoto = getString(R.string.message_take_photo);
        final String choosePhoto =  getString(R.string.message_choose_photo_from_library);
        final String cancelPhoto =  getString(R.string.action_cancel);
        final CharSequence[] items = { takePhoto,choosePhoto, cancelPhoto};
        AlertDialog.Builder builder = new AlertDialog.Builder(ImageViewFullscreenActivity.this);
        builder.setTitle(getString(R.string.message_select_image));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = PermissionUtils.checkPermission(ImageViewFullscreenActivity.this);
                if (items[item].equals(takePhoto)) {
                    if(result)
                        cameraIntent();
                } else if (items[item].equals(choosePhoto)) {
                    if(result)
                        galleryIntent();
                } else if (items[item].equals(cancelPhoto)) {
                    if(mNoPhoto){
                        finish();
                    } else {
                        dialog.dismiss();
                    }
                }
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void cameraIntent() {
       /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, PICK_FROM_CAMERA_INTENT);*/
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //mPhotoUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".png"));
        try {
            mFileName = createImageFile().getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mPhotoUri);
        try {
            //intent.putExtra("return-data", true);
            startActivityForResult(intent, PICK_FROM_CAMERA_INTENT);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mPhotoUri = Uri.parse("file:" + image.getAbsolutePath());
        return image;
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);//
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.prompt_group_select_image)), SELECT_PICTURE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (mNoPhoto) {

            if(data == null) {
                finish();
                return;
            }
            if (resultCode != RESULT_OK) {
                finish();
                return;
            }
        }
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PICK_FROM_CAMERA_INTENT:
                try {
                    Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mPhotoUri);
                    Log.d(TAG_LOG, "FILE NAME RETURNED: " + mFileName);
                    showProgress(true);
                    sendPostRequest();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case SELECT_PICTURE_INTENT:
                mPhotoUri = data.getData();
                mFileName = getRealPathFromURI(this, mPhotoUri);
                Log.d(TAG_LOG, "PHOTOURI: " + mPhotoUri);
                Log.d(TAG_LOG, "FILE NAME RETURNED: " + mFileName);
                //try {
                    //Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mPhotoUri);
                    showProgress(true);
                    sendPostRequest();
/*
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                break;
            case CROP_INTENT:
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    mFileName = FileUtils.writeTemporaryBitmap(photo, this);
                    Log.d(TAG_LOG, "FILE NAME RETURNED: " + mFileName);
                    showProgress(true);
                    sendPostRequest();

                }
                break;
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void doCrop() {
        Log.d(TAG_LOG,"doCrop");
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setType("image/*");
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(cropIntent, 0);
        int size = list.size();
        Log.d(TAG_LOG, "CROP OPTIONS SIZE: " + size);
        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
            return;
        } else {
            cropIntent.setData(mPhotoUri);
            cropIntent.putExtra("outputX", PHOTO_SIZE_PX);
            cropIntent.putExtra("outputY", PHOTO_SIZE_PX);
            cropIntent.putExtra("aspectX", PHOTO_SIZE_PX);
            cropIntent.putExtra("aspectY", PHOTO_SIZE_PX);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("return-data", true);
            if (size == 1) {
                Intent i = new Intent(cropIntent);
                ResolveInfo res = list.get(0);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                startActivityForResult(i, CROP_INTENT);
            } else {
                Intent chooserIntent = Intent.createChooser(cropIntent,getString(R.string.action_choose_crop_app));
                Log.d(TAG_LOG,chooserIntent.toString());
                if(cropIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(chooserIntent, CROP_INTENT);
                } else {
                    Log.d(TAG_LOG,"no resolved activity for cropIntent");
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mPhotoView.setVisibility(show ? View.GONE : View.VISIBLE);
            mPhotoView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPhotoView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mPhotoView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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
                                Bitmap photo = FileUtils.readBitmapFromPath(mFileName, ImageViewFullscreenActivity.this);
                                if (photo != null) {
                                    Bitmap resizedPhoto = FileUtils.resizeBitmap(photo);
                                    boolean success = FileUtils.writeGroupImage(mGroup.getId(), resizedPhoto, ImageViewFullscreenActivity.this);
                                    if(success){
                                        mBitmap =resizedPhoto;
                                    }
                                    Log.d(TAG_LOG, "NEW PHOTO WRITTEN :" + success);
                                    mPhotoView.setImageBitmap(resizedPhoto);
                                    mPhotoUpdated = true;
                                } else {
                                    Log.d(TAG_LOG, "photo null!!!!!!! filename=" + mFileName);
                                }
                            } else {
                                Toast.makeText(ImageViewFullscreenActivity.this, R.string.error_server_internal_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                        showProgress(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Toast.makeText(ImageViewFullscreenActivity.this, R.string.error_server_connection, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if(!TextUtils.isEmpty(mFileName)){
                    // file name could found file base or direct access from real path
                    //Bitmap photo = FileUtils.readTemporaryBitmap(mGroup.getPhotoFileName(), getBaseContext());
                    Log.d(TAG_LOG, "sent file name: " +
                            mFileName);
                    Bitmap photo = FileUtils.readBitmapFromPath(mFileName, ImageViewFullscreenActivity.this);
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

}
