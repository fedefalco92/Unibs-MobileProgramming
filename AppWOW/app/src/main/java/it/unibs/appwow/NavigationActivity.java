package it.unibs.appwow;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import it.unibs.appwow.database.AppDB;
import it.unibs.appwow.fragments.GroupListFragment;
import it.unibs.appwow.fragments.OfflineGroupListFragment;
import it.unibs.appwow.fragments.SettingsFragment;
import it.unibs.appwow.models.parc.LocalUser;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,GroupListFragment.OnFragmentInteractionListener,OfflineGroupListFragment.OnFragmentInteractionListener {

    private final String TAG_LOG = NavigationActivity.class.getSimpleName();

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private NavigationView navigationView;
    private LocalUser mLocalUser;

    private String TAG_ONLINE = NavigationActivity.class.getSimpleName().concat("_ONLINE_FRAGMENT");
    private String TAG_OFFLINE = NavigationActivity.class.getSimpleName().concat("_OFFLINE_FRAGMENT");
    private String TAG_SETTINGS = NavigationActivity.class.getSimpleName().concat("_SETTINGS_FRAGMENT");

   // private String MENU_ONLINE = NavigationActivity.class.getSimpleName().concat(".MENU_ONLINE");
   // private String MENU_OFFLINE = NavigationActivity.class.getSimpleName().concat(".MENU_OFFLINE");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //controllo presenza utente per disabilitare eventualmente il logout
        mLocalUser = LocalUser.load(MyApplication.getAppContext());
        if(mLocalUser == null){
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
        } else {
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
            TextView fullname = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_fullname);
            TextView email = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);
            fullname.setText(mLocalUser.getFullName());
            email.setText(mLocalUser.getEmail());
        }


        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView, GroupListFragment.newInstance(mLocalUser),TAG_ONLINE).commit();

        //Per impostare selezionato il tab dei gruppi online (nella barra laterale)
        navigationView.getMenu().findItem(R.id.nav_online_groups).setChecked(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 12/05/2016 Gestione aggiunta gruppo online oppure offline modificando la classe di destinazione
                Class destinationClass = null;
                Fragment onlineFragment = mFragmentManager.findFragmentByTag(TAG_ONLINE);
                Fragment offlineFragment = mFragmentManager.findFragmentByTag(TAG_OFFLINE);
                if(onlineFragment!= null && onlineFragment.isVisible()){
                    destinationClass = AddGroupActivity.class;
                    Log.d(TAG_LOG,"ONLINE fragment visible");
                }
                else if(offlineFragment!= null && offlineFragment.isVisible()){
                    destinationClass = null;
                    Log.d(TAG_LOG,"OFFLINE fragment visible");
                }
                Intent createIntent = new Intent(NavigationActivity.this,destinationClass);
                startActivity(createIntent);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG.setAction("Action", null).show();
            }
        });



        
    }
/**
 * UNCOMMENT THESE LINES IF YOU WANT TO MANAGE THE STATE: PRESERVE THE FRAGMENT VISUALIZED IF
 * THE SCREEN IS ROTATED
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        boolean onlineChecked = navigationView.getMenu().findItem(R.id.nav_online_groups).isChecked();
        boolean offlineChecked = navigationView.getMenu().findItem(R.id.nav_offline_groups).isChecked();
        outState.putBoolean(MENU_ONLINE,onlineChecked);
        outState.putBoolean(MENU_OFFLINE,offlineChecked);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean onlineChecked = savedInstanceState.getBoolean(MENU_ONLINE);
        if(onlineChecked){
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.containerView,new GroupListFragment(),TAG_ONLINE).commit();
        }
        else{
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.containerView,new OfflineGroupListFragment(),TAG_OFFLINE).commit();
        }
    }
*/
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit_group) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_online_groups) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.containerView, GroupListFragment.newInstance(mLocalUser),TAG_ONLINE).commit();

            // Handle the camera action
        } else if (id == R.id.nav_offline_groups) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.containerView,new OfflineGroupListFragment(),TAG_OFFLINE).commit();

        } else if (id == R.id.nav_settings) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            SettingsFragment prefs = new SettingsFragment();
            fragmentTransaction.replace(R.id.containerView,prefs,TAG_SETTINGS).commit();

        } else if (id == R.id.nav_logout) {
            // TODO: 19/05/2016 PULIRE IL DATABASE QUANDO SI FA LOGOUT
            LocalUser currentUser = LocalUser.load(MyApplication.getAppContext());
            currentUser.logout(MyApplication.getAppContext());
            deleteDatabase(AppDB.DATABASE_NAME); // Elimina il db
            Intent login = new Intent(NavigationActivity.this, LoginActivity.class);
            login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(login);
            finish();
        } else if (id == R.id.nav_login){
            Intent login = new Intent(NavigationActivity.this, LoginActivity.class);
            login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(login);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
