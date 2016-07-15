package it.unibs.appwow;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.utils.CropOption;
import it.unibs.appwow.utils.CropOptionAdapter;
import it.unibs.appwow.utils.FileUtils;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.PermissionUtils;

public class AddGroupActivity extends AppCompatActivity {

    private static final String TAG_LOG = AddGroupActivity.class.getSimpleName();

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

    private int mChoosenPhotoTask;
    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        setTitle(getString(R.string.add_group_activity_title));
        mGroupImage = (ImageView) findViewById(R.id.imageView2);
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
        mGroup = GroupModel.create("");
        mFileName = "";
        //mPhotoUri = "";
    }

    public GroupModel getGroup() {
        return mGroup;
    }

    public void setGroup(GroupModel group) {
        mGroup = group;
    }

    public TextView getGroupNameView() {
        return mGroupNameView;
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

    private void selectImage() {
        final String takePhoto = getString(R.string.add_group_take_photo);
        final String choosePhoto =  getString(R.string.add_group_choose_photo_from_library);
        final String cancelPhoto =  getString(R.string.add_group_cancel);
        final CharSequence[] items = { takePhoto,choosePhoto, cancelPhoto};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddGroupActivity.this);
        builder.setTitle(getString(R.string.add_group_add_photo_title));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = PermissionUtils.checkPermission(AddGroupActivity.this);
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

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.go_ahead:
                String insertedName = getInsertedName();
                if(Validator.isGroupNameValid(insertedName)){
                    final Intent addMembersIntent = new Intent(AddGroupActivity.this, AddGroupMembersActivity.class);
                    //startActivityForResult(registrationIntent, REGISTRATION_REQUEST_ID);
                    LocalUser currentUser = LocalUser.load(MyApplication.getAppContext());
                    mGroup.setIdAdmin(currentUser.getId());
                    mGroup.setGroupName(insertedName);
                    mGroup.setPhotoFileName(mFileName);
                    addMembersIntent.putExtra(PASSING_GROUP_EXTRA, mGroup);
                    startActivity(addMembersIntent);
                    return true;
                } else {
                    setNameError();
                }

            default:
                return true;
        }
    }*/

    public void onGoAheadButtonClick(View v){
        String insertedName = getInsertedName();
        if(Validator.isGroupNameValid(insertedName)){
            //final Intent addMembersIntent = new Intent(AddGroupActivity.this, AddGroupMembersActivity.class);
            final Intent addMembersIntent = new Intent(AddGroupActivity.this, AddGroupMembersActivityRecyclerView.class);
            //startActivityForResult(registrationIntent, REGISTRATION_REQUEST_ID);
            LocalUser currentUser = LocalUser.load(MyApplication.getAppContext());
            mGroup.setIdAdmin(currentUser.getId());
            mGroup.setGroupName(insertedName);
            mGroup.setPhotoFileName(mFileName);
            addMembersIntent.putExtra(PASSING_GROUP_EXTRA, mGroup);
            startActivity(addMembersIntent);
        } else {
            setNameError();
        }
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

    private String getPath(Uri uri){
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri,projection,null,null,null);
        if(cursor==null){
            return null;
        }
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(columnIndex);
        cursor.close();
        return s;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mChoosenPhotoTask == TAKE_PHOTO)
                        cameraIntent();
                    else if(mChoosenPhotoTask == CHOOSE_PHOTO)
                        galleryIntent();
                } else {
                    //code for deny
                    Log.d(TAG_LOG, "PERMISSION DENIED");
                }
                break;
        }
    }

    private void doCrop() {
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        int size = list.size();
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

    @Override
    protected void onDestroy() {
        deleteCache(this);
        super.onDestroy();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
