package it.unibs.appwow;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import it.unibs.appwow.models.parc.Group;
import it.unibs.appwow.models.parc.User;

public class AddGroupActivity extends AppCompatActivity {

    private static final String TAG_LOG = AddGroupActivity.class.getSimpleName();

    private int SELECT_PICTURE_CODE = 100;
    public static final String PASSING_GROUP_EXTRA = "group";
    private ImageView mGroupImage;
    private TextView mGroupNameView;
    private Group mGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        mGroupImage = (ImageView) findViewById(R.id.imageView2);
        mGroupImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intentPhoto = new Intent(Intent.ACTION_GET_CONTENT);
                intentPhoto.setType("image/*");
                startActivityForResult(Intent.createChooser(intentPhoto,getResources().getString(R.string.select_image_group)),SELECT_PICTURE_CODE);
                return false;
            }
        });
        mGroupNameView = (TextView) findViewById(R.id.group_name_field);
        mGroup = Group.create("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE_CODE) {
                Uri selectedImage = data.getData();
                String path = getPath(selectedImage);
                Drawable image = Drawable.createFromPath(path);
                mGroupImage.setImageDrawable(image);
                mGroup.setPhotoUri(selectedImage.toString());
                Toast.makeText(AddGroupActivity.this, "Path: "+path, Toast.LENGTH_SHORT).show();
            }
        }
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
                if(isGroupNameValid()){
                    final Intent addMembersIntent = new Intent(AddGroupActivity.this, AddGroupMembersActivity.class);
                    //startActivityForResult(registrationIntent, REGISTRATION_REQUEST_ID);
                    User currentUser = User.load(MyApplication.getAppContext());
                    mGroup.setIdAdmin(currentUser.getId());
                    mGroup.setGroupName(mGroupNameView.getText().toString());
                    addMembersIntent.putExtra(PASSING_GROUP_EXTRA, mGroup);
                    startActivity(addMembersIntent);
                    return true;
                } else {
                    mGroupNameView.setError(getString(R.string.invalid_group_name));
                    mGroupNameView.requestFocus();
                }

            default:
                return true;
        }
    }

    private boolean isGroupNameValid() {
        String groupName = mGroupNameView.getText().toString();
        if(!groupName.isEmpty()){
            return true;
        } else {
            return false;
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
}
