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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.utils.CropOption;
import it.unibs.appwow.utils.FileUtils;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.PermissionUtils;
import it.unibs.appwow.utils.graphicTools.SquareImageView;

public class AddGroupActivity extends AppCompatActivity {

    private static final String TAG_LOG = AddGroupActivity.class.getSimpleName();

    private static final int SELECT_PICTURE_INTENT = 1;
    private static final int PICK_FROM_CAMERA_INTENT = 2;
    private static final int CROP_FROM_CAMERA = 3;
    public static final String PASSING_GROUP_EXTRA = "group";
    private static final int PHOTO_SIZE_PX = 400;
    private SquareImageView mGroupImage;
    private TextInputEditText mGroupNameView;
    private ImageButton mRemovePhotoButton;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getString(R.string.title_activity_add_group));
        mGroupImage = (SquareImageView) findViewById(R.id.imageView2);
        /*mGroupImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                selectImage();
                //Intent intentPhoto = new Intent(Intent.ACTION_GET_CONTENT);
                //intentPhoto.setType("image/*");
                //startActivityForResult(Intent.createChooser(intentPhoto,getResources().getString(R.string.select_image_group)),SELECT_PICTURE_CODE);
                return false;
            }
        });*/
        mGroupNameView = (TextInputEditText) findViewById(R.id.group_name_field);
        mGroup = GroupModel.create("");
        mFileName = "";
        //mPhotoUri = "";

        mRemovePhotoButton = (ImageButton) findViewById(R.id.remove_photo_button);
        toggleRemovePhotoButton(false);
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
                Log.d(TAG_LOG,"mPhotoUri: " + mPhotoUri.getPath());
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
                    mGroupImage.setImageBitmap(photo);
                    toggleRemovePhotoButton(true);
                }
                File f = new File(mPhotoUri.getPath());
                if (f.exists()) f.delete();
                break;

        }
    }

    private void selectImage() {
        final String takePhoto = getString(R.string.message_take_photo);
        final String choosePhoto =  getString(R.string.message_choose_photo_from_library);
        final String cancelPhoto =  getString(R.string.action_cancel);
        final CharSequence[] items = { takePhoto,choosePhoto, cancelPhoto};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddGroupActivity.this);
        builder.setTitle(getString(R.string.message_select_image));
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
        intent.setAction(Intent.ACTION_PICK);//
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.prompt_group_select_image)), SELECT_PICTURE_INTENT);
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
            final Intent addMembersIntent = new Intent(AddGroupActivity.this, AddGroupMembersActivity.class);
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

                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {

                // FIXME: 27/07/16 In questo modo sceglie quello di default e non ha problemi ...
                Intent i = new Intent(cropIntent);
                ResolveInfo res = list.get(0);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                startActivityForResult(i, CROP_FROM_CAMERA);

                /* Exception on some mobile devices
                Log.d(TAG_LOG,"mPhotoUri " + mPhotoUri.getPath());
                List<Intent> allIntents = new  ArrayList<>();
                for (ResolveInfo res : list) {
                    Log.d(TAG_LOG,"res pckname: " + res.activityInfo.packageName + " - name: " + res.activityInfo.name);
                    Intent i = new Intent(cropIntent);
                    i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    allIntents.add(i);
                }

                Intent mainIntent = allIntents.get(allIntents.size()-1);
                allIntents.remove(mainIntent);

                Intent chooserIntent = Intent.createChooser(mainIntent,getString(R.string.choose_crop_app));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,allIntents.toArray(new Parcelable[allIntents.size()]));
                startActivityForResult(chooserIntent, CROP_FROM_CAMERA); */

                /* It should work but it does not :(
                Intent chooserIntent = Intent.createChooser(cropIntent,getString(R.string.choose_crop_app));
                Log.d(TAG_LOG,chooserIntent.toString());
                if(cropIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(chooserIntent,CROP_FROM_CAMERA);
                } else {
                    Log.d(TAG_LOG,"no resolved activity for cropIntent");
                } */

                /*OLD CODE WITH CUSTOM CHOOSER
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();
                    co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(cropIntent);
                    co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    cropOptions.add(co);
                }
                CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle(R.string.choose_crop_app);
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
                alert.show();*/
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

    public void removePhoto() {
        mGroupImage.setImageResource(R.drawable.ic_group_black_48dp);
        toggleRemovePhotoButton(false);
        //mPhotoUri = null;
        mFileName = "";
    }

    private void toggleRemovePhotoButton(boolean showBin) {
        if (showBin) {
            mRemovePhotoButton.setImageResource(R.drawable.ic_delete_black_24dp);
            mRemovePhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePhoto();
                }
            });
        } else {
            mRemovePhotoButton.setImageResource(R.drawable.ic_add_a_photo_black_24dp);
            mRemovePhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectImage();
                }
            });
        }
    }

    /*
    public void onInsertPhotoButtonClicked(View v){
        galleryIntent();
    }

    public void onAddPhotoButtonClicked(View v){
        cameraIntent();
    }*/

    /*public void onAddPhotoButtonClicked(View v){
        selectImage();
    }*/

}
