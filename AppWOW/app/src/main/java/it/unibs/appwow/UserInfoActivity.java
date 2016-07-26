package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.Messages;

public class UserInfoActivity extends AppCompatActivity {

    private static final String TAG_LOG = UserInfoActivity.class.getSimpleName();

    // UI
    private Toolbar mToolbar;
    private EditText mFullname;
    private EditText mEmail;

    private LocalUser mLocalUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.preferences_option_account));

        mLocalUser = LocalUser.load(this);

        mFullname = (EditText) findViewById(R.id.activity_edit_user_fullname);
        mFullname.setText(mLocalUser.getFullName());

        mEmail = (EditText) findViewById(R.id.activity_edit_user_email);
        mEmail.setText(mLocalUser.getEmail());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }



    public void editFullName(View view) {
        Intent editFullName = new Intent(UserInfoActivity.this, EditFullNameActivity.class);
        startActivity(editFullName);
    }

    public void changePassword(View view) {
        Intent changePassword = new Intent(UserInfoActivity.this, EditPasswordActivity.class);
        startActivity(changePassword);
    }
}
