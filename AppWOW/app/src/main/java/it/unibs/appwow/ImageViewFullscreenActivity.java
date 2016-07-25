package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import java.util.ArrayList;
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
import it.unibs.appwow.utils.CropOptionAdapter;
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
    private static final int CROP_FROM_CAMERA = 3;
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
            showProgress(true);
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

        if (id == R.id.download) {
            // TODO: 22/07/2016 VERIFICARE
            //Toast.makeText(ImageViewFullscreenActivity.this, "GESTIRE CARICAMENTO FOTO", Toast.LENGTH_SHORT).show();
            String title = mGroup.getGroupName() + "_photo_" + System.currentTimeMillis() + ".png";
            String newfile = CapturePhotoUtils.insertImage(getContentResolver(), mBitmap, title, "");
            Log.d(TAG_LOG, "new file: " + newfile);
            Toast.makeText(ImageViewFullscreenActivity.this, getString(R.string.image_saved_to_gallery), Toast.LENGTH_SHORT).show();
        }


        if (id == R.id.share) {

            //Bitmap bitmap = FileUtils.readGroupImage(mGroup.getId(), this);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            //ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            //File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.png");
           /*
            try {
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            File f = FileUtils.getGroupImageFile(mGroup.getId(), this);
            //shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Image"));




            //shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        }
        return super.onOptionsItemSelected(item);
    }

    private void selectImage() {
        final String takePhoto = getString(R.string.add_group_take_photo);
        final String choosePhoto =  getString(R.string.add_group_choose_photo_from_library);
        final String cancelPhoto =  getString(R.string.add_group_cancel);
        final CharSequence[] items = { takePhoto,choosePhoto, cancelPhoto};
        AlertDialog.Builder builder = new AlertDialog.Builder(ImageViewFullscreenActivity.this);
        builder.setTitle(getString(R.string.add_group_add_photo_title));
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
        intent.setAction(Intent.ACTION_PICK);//
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
                    // TODO: 04/07/2016 caricare l'immagine subito
                    showProgress(true);
                    sendPostRequest(photo);

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
            intent.putExtra("outputX", PHOTO_SIZE_PX);
            intent.putExtra("outputY", PHOTO_SIZE_PX);
            intent.putExtra("aspectX", PHOTO_SIZE_PX);
            intent.putExtra("aspectY", PHOTO_SIZE_PX);
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

    private void sendPostRequest(final Bitmap photo) {

        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mViewContainer,R.string.err_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    sendPostRequest(photo);
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
                                showProgress(false);
                                //DELETING TEMPORARY FILE
                                FileUtils.deleteTemporaryFile(mFileName, getBaseContext());
                                boolean res = FileUtils.writeGroupImage(mGroup.getId(), photo, ImageViewFullscreenActivity.this);
                                Log.d(TAG_LOG, "NEW PHOTO WRITTEN :" + res);
                                mPhotoView.setImageBitmap(photo);
                                mPhotoUpdated = true;
                            } else {
                                showProgress(false);
                                Toast.makeText(ImageViewFullscreenActivity.this, R.string.server_internal_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                Toast.makeText(ImageViewFullscreenActivity.this, R.string.server_connection_error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if (!mFileName.isEmpty()) {
                    // file name could found file base or direct access from real path
                    // for now just get bitmap data from ImageView
                    params.put("photo", new DataPart(mFileName, VolleyMultipartHelper.getFileDataFromBitmap(FileUtils.readTemporaryBitmap(mFileName, getBaseContext())), "image/png"));
                }
                return params;
            }
        };


        MyApplication.getInstance().addToRequestQueue(postRequest);

    }

}
