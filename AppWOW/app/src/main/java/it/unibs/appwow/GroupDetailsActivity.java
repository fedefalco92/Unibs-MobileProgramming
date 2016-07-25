package it.unibs.appwow;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import it.unibs.appwow.database.PaymentDAO;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.database.DebtDAO;
import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.fragments.AmountsFragment;
import it.unibs.appwow.fragments.DebtsFragment;
import it.unibs.appwow.fragments.GroupListFragment;
import it.unibs.appwow.fragments.PaymentsFragment;
import it.unibs.appwow.models.Debt;
import it.unibs.appwow.models.parc.PaymentModel;
import it.unibs.appwow.models.DebtModel;
import it.unibs.appwow.models.UserGroupModel;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.utils.graphicTools.Messages;

public class GroupDetailsActivity extends AppCompatActivity implements PaymentsFragment.OnListFragmentInteractionListener,
        AmountsFragment.OnListFragmentInteractionListener,
        DebtsFragment.OnListFragmentInteractionListener,
        SwipeRefreshLayout.OnRefreshListener{

    private final String TAG_LOG = GroupDetailsActivity.class.getSimpleName();
    private final String TAG_REQUEST_GROUP_DETAILS = "GROUP_DETAILS";

    /**
     * Gruppo ricevuto, già "pieno"
     */
    private GroupModel mGroup;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private CoordinatorLayout mContainer;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    private LocalUser mLocalUser;
    //private int mRequestPending;

    private FloatingActionButton mFab;

    private int mCurrentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_LOG,"onCreate");

        setContentView(R.layout.activity_group_details);
        mContainer = (CoordinatorLayout) findViewById(R.id.main_content);

        mLocalUser = LocalUser.load(MyApplication.getAppContext());
        mGroup = (GroupModel) getIntent().getParcelableExtra(GroupListFragment.PASSING_GROUP_TAG);
        setTitle(mGroup.getGroupName());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = (ProgressBar) findViewById(R.id.self_reload_progress_bar);

        mCurrentPage = 1;
        setFragmentAdapter();

        mFab = (FloatingActionButton) findViewById(R.id.fab_edit);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                final Intent i = new Intent(GroupDetailsActivity.this, AddEditPaymentActivity.class);
                i.putExtra(PaymentsFragment.PASSING_GROUP_TAG, mGroup);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        MyApplication.getInstance().cancelPendingRequests(TAG_REQUEST_GROUP_DETAILS);
        super.onSaveInstanceState(outState);
    }

    private void setFragmentAdapter() {
        Log.d(TAG_LOG,"setFragmentAdapter");
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.single_group_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Log.d(TAG_LOG,"mViewPager.setCurrentItem");
        mViewPager.setCurrentItem(mCurrentPage);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.d(TAG_LOG,"Page Scrolled : "+position);
                mCurrentPage = position;
            }

            @Override
            public void onPageSelected(int position) {
                //Log.d(TAG_LOG,"Page Selected : "+position);
                //GroupDetailsActivity.this.invalidateOptionsMenu();
                supportInvalidateOptionsMenu();
                mCurrentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                supportInvalidateOptionsMenu();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_show_group_info){
            Intent showGroupInfo = new Intent (this, GroupInfoActivity.class);
            //editGroupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            showGroupInfo.putExtra(GroupListFragment.PASSING_GROUP_TAG, mGroup);
            startActivity(showGroupInfo);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(PaymentModel item) {
        Toast.makeText(GroupDetailsActivity.this, "Item: " + item.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListFragmentInteraction(Amount item) {
        Toast.makeText(GroupDetailsActivity.this, "Item: " + item.getUserId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListFragmentInteraction(Debt item) {
    }

    @Override
    public void onRefresh() {
        Log.d(TAG_LOG,"onRefresh");
        mSwipeRefreshLayout.setRefreshing(true);
        fetchGroupDetails();
    }

    /**
     * ATTENZIONE QUI SI AGGIORNA L'INTERO GRUPPO COMPRENDENDO:
     * Users
     * Payments
     * Debts
     */
    private void fetchGroupDetails(){
        Log.d(TAG_LOG,"Fetching group details");
        if(!WebServiceRequest.checkNetwork()){
            Messages.showSnackbarWithAction(mContainer,R.string.err_no_connection,R.string.retry,new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    fetchGroupDetails();
                }
            });
            mSwipeRefreshLayout.setRefreshing(false);
            showProgress(false);
            return;
        } else {
            fetchUsers();
        }
    }

    private void fetchGroup(){
        Log.d(TAG_LOG,"fetch group method");
        //CONTROLLO che il gruppo sia da aggiornare
        Uri groupUri = WebServiceUri.getGroupUri(mGroup.getId());
        URL url = WebServiceUri.uriToUrl(groupUri);
        JsonObjectRequest groupRequest = new JsonObjectRequest(url.toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        GroupDAO dao = new GroupDAO();
                        dao.open();
                        try{
                            Log.d(TAG_LOG, "GROUP response = " + response.toString(1));
                            GroupModel gserver = GroupModel.create(response);
                            GroupModel glocal = dao.getSingleGroup(mGroup.getId()); //(sempre != null)
                            long server_updated_at = gserver.getUpdatedAt();
                            long local_updated_at = glocal.getUpdatedAt();
                            if (server_updated_at > local_updated_at) {
                                //aggiorno il title dell'activity
                                String newTitle = response.getString("name");
                                setTitle(newTitle);
                                //eseguo l'update
                                int id = glocal.getId();
                                int idAdmin = gserver.getIdAdmin();
                                String groupName = gserver.getGroupName();
                                String photoFileName = glocal.getPhotoFileName();
                                long photoUpdatedAt = glocal.getPhotoUpdatedAt();
                                long createdAt = gserver.getCreatedAt();
                                long updatedAt = gserver.getUpdatedAt();
                                int highlighted = GroupModel.NOT_HIGHLIGHTED;
                                dao.updateSingleGroup(id,idAdmin, groupName, photoFileName, photoUpdatedAt, createdAt, updatedAt, highlighted);

                                fetchCosts(server_updated_at);
                                /*
                                fetchDebts(server_updated_at); // Aggiunto DEBUG

                                //AGGIORNO LA DATA DI MODIFICA DEL GRUPPO IN LOCALE
                                mGroup.setUpdatedAt(server_updated_at);
                                dao.insertGroup(mGroup);
                                dao.close();

                                // TODO: 29/06/2016  DA OTTIMIZZARE
                                setFragmentAdapter();
                                mSwipeRefreshLayout.setRefreshing(false);*/
                            } else {
                                //se il gruppo locale è più aggiornato di quello del server
                                Log.d(TAG_LOG, "Group up to date");
                                setFragmentAdapter();
                                mSectionsPagerAdapter.notifyDataSetChanged();
                                mSwipeRefreshLayout.setRefreshing(false);
                                showProgress(false);
                            }
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                        dao.close();
                    }
                },
                errorResponseListener());
        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(groupRequest,TAG_REQUEST_GROUP_DETAILS);
    }

    private void fetchUsers(){
        Log.d(TAG_LOG,"fetchUsers method");
        /**
         * AGGIORNAMENTO USERS (USER_GROUP)
         */
        // Volley's json array request object
        Uri groupUsersUri = WebServiceUri.getGroupUsersUri(mGroup.getId());
        URL url = WebServiceUri.uriToUrl(groupUsersUri);
        JsonArrayRequest usersRequest = new JsonArrayRequest(url.toString(),
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Log.d(TAG_LOG, "USERS response = " + response.toString());

                    if (response.length() > 0) {
                        UserDAO udao = new UserDAO();
                        udao.open();
                        UserGroupDAO ugdao = new UserGroupDAO();
                        ugdao.open();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject userJs = response.getJSONObject(i);
                                UserModel user = UserModel.create(userJs);
                                long server_updated_at = user.getUpdatedAt();
                                long local_updated_at = udao.getUpdatedAt(user.getId());
                                //aggiorno lo user solo se subito modifiche
                                if (server_updated_at > local_updated_at) {
                                    udao.insertUser(user);
                                    Log.d(TAG_LOG, "INSERTED USER -> " + user);
                                } else {
                                   Log.d(TAG_LOG, "LocalUser -> " + user + " up to date");
                                }

                                JSONObject pivot = userJs.getJSONObject("pivot");
                                UserGroupModel piv = UserGroupModel.create(pivot);
                                //if(!piv.isUpdated()){
                                    ugdao.insertUserGroup(piv);
                                //}

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        udao.close();
                        ugdao.close();

                        fetchGroup();
                    } else {
                       // Toast.makeText(MyApplication.getAppContext(), "ERRORE SCONOSCIUTO", Toast.LENGTH_LONG).show();
                    }
                }
            },
                errorResponseListener());
        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(usersRequest,TAG_REQUEST_GROUP_DETAILS);
    }

    private void fetchCosts(final long server_updated_at){
        Log.d(TAG_LOG, "fetching costs methods");
        Uri groupCostsUri = WebServiceUri.getGroupPaymentsUri(mGroup.getId());
        URL url = WebServiceUri.uriToUrl(groupCostsUri);

        JsonArrayRequest costsRequest = new JsonArrayRequest(url.toString(),
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG_LOG, "COSTS response = " + response.toString());
                        PaymentDAO dao = new PaymentDAO();
                        dao.open();
                        dao.resetAllCosts(mGroup.getId());
                        if(response.length() > 0){
                            for(int i = 0; i<response.length();i++){
                                try{
                                    JSONObject costJs = response.getJSONObject(i);
                                    PaymentModel cost = PaymentModel.create(costJs);
                                    dao.insertPayment(cost);
                                } catch(JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        dao.close();
                        fetchDebts(server_updated_at); //rimosso DEBUG
                    }
                },
                errorResponseListener()
        );
        MyApplication.getInstance().addToRequestQueue(costsRequest,TAG_REQUEST_GROUP_DETAILS);
    }

    private void fetchDebts(final long server_updated_at) {

        Uri groupBalancingsUri = WebServiceUri.getGroupDebtsUri(mGroup.getId());
        URL url = WebServiceUri.uriToUrl(groupBalancingsUri);

        JsonArrayRequest debtsRequest = new JsonArrayRequest(url.toString(),
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG_LOG, "DEBTS RESPONSE: " + response);
                        DebtDAO dao = new DebtDAO();
                        dao.open();
                        dao.resetAllDebts(mGroup.getId()); //cancella anche tutte le transactions se funziona on delete cascade
                        if(response.length() > 0){
                            for(int i = 0; i<response.length();i++){
                                try{
                                    JSONObject debtsJs = response.getJSONObject(i);
                                    DebtModel d = DebtModel.create(debtsJs);
                                    DebtModel inserito  = dao.insertDebt(d);
                                    Log.d(TAG_LOG, "DEBT INSERITO: " +inserito);
                                } catch(JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        dao.close();

                        //AGGIORNO LA DATA DI MODIFICA DEL GRUPPO IN LOCALE
                        GroupDAO gdao = new GroupDAO();
                        gdao.open();
                        /*
                        mGroup.setUpdatedAt(server_updated_at);
                        dao.insertGroup(mGroup);*/
                        gdao.touchGroup(mGroup.getId(),server_updated_at);
                        gdao.close();

                        // TODO: 29/06/2016  DA OTTIMIZZARE*/
                        setFragmentAdapter();
                        mSectionsPagerAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                        showProgress(false);

                    }
                },
                errorResponseListener()
        );
        MyApplication.getInstance().addToRequestQueue(debtsRequest,TAG_REQUEST_GROUP_DETAILS);


    }

    private Response.ErrorListener errorResponseListener() {
        return new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG_LOG, "VOLLEY_ERROR - " + "Server Error: " + error.getMessage());
                //Toast.makeText(MyApplication.getAppContext(), getString(R.string.server_connection_error), Toast.LENGTH_LONG).show();
                Messages.showSnackbar(mContainer,R.string.server_connection_error);
                mSectionsPagerAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
                showProgress(false);
            }
        };
    }


    @Override
    protected void onResume() {

        Log.d(TAG_LOG,"onResume");
        showProgress(true);
        fetchGroupDetails();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        MyApplication.getInstance().cancelPendingRequests(TAG_REQUEST_GROUP_DETAILS);
        super.onDestroy();
    }

    public View getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1)
            switch (position) {
                case 0:
                    return PaymentsFragment.newInstance(1, mGroup);
                case 1:
                    return AmountsFragment.newInstance(1, mGroup, mLocalUser.getId());
                case 2:
                    return DebtsFragment.newInstance(1, mGroup);
            }
            return null;
            //Log.d(TAG_LOG,"Position: "+position);

        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // TODO: 10/05/2016  STRINGHE
            switch (position) {
                case 0:
                    return getString(R.string.payments).toUpperCase();
                case 1:
                    return getString(R.string.summary).toUpperCase();
                case 2:
                    return getString(R.string.debts).toUpperCase();
            }
            return null;
        }
    }

    private void showProgress(boolean show){
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
