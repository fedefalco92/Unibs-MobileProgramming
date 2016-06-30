package it.unibs.appwow;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.PermissionUtils;

public class AddGroupActivity extends AppCompatActivity {

    private static final String TAG_LOG = AddGroupActivity.class.getSimpleName();

    private static final int SELECT_PICTURE_INTENT = 1;
    private static final int REQUEST_CAMERA_INTENT = 2;
    public static final String PASSING_GROUP_EXTRA = "group";
    private ImageView mGroupImage;
    private TextView mGroupNameView;
    private GroupModel mGroup;
    private Bitmap mThumbnail;
    private String mPhotoUri;

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
        mPhotoUri = "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE_INTENT)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA_INTENT)
                onCaptureImageResult(data);
        }
        /*if (requestCode == SELECT_PICTURE_INTENT) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                Log.d(TAG_LOG, "URI PHOTO: " + selectedImage.toString());
                String path = getPath(selectedImage);
                Drawable image = Drawable.createFromPath(path);
                mGroupImage.setImageDrawable(image);
                mGroup.setPhotoUri(selectedImage.toString());
                Toast.makeText(AddGroupActivity.this, "Path: "+path, Toast.LENGTH_SHORT).show();
            }
        }*/
    }

    private void onSelectFromGalleryResult(Intent data) {


        if (data != null) {
            try {
                mThumbnail = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mGroupImage.setImageBitmap(mThumbnail);
        mPhotoUri = data.getData().toString();
    }


    private void onCaptureImageResult(Intent data) {
        mThumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mThumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mGroupImage.setImageBitmap(mThumbnail);
        mPhotoUri = data.getData().toString();
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
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA_INTENT);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image_group)), SELECT_PICTURE_INTENT);
    }

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
                if(Validator.isGroupNameValid(mGroupNameView.getText().toString())){
                    final Intent addMembersIntent = new Intent(AddGroupActivity.this, AddGroupMembersActivity.class);
                    //startActivityForResult(registrationIntent, REGISTRATION_REQUEST_ID);
                    LocalUser currentUser = LocalUser.load(MyApplication.getAppContext());
                    mGroup.setIdAdmin(currentUser.getId());
                    mGroup.setGroupName(mGroupNameView.getText().toString());
                    addMembersIntent.putExtra(PASSING_GROUP_EXTRA, mGroup);
                    startActivity(addMembersIntent);
                    return true;
                } else {
                    mGroupNameView.setError(getString(R.string.error_invalid_group_name));
                    mGroupNameView.requestFocus();
                }

            default:
                return true;
        }
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
}
