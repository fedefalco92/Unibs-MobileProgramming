package it.unibs.appwow;

import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import it.unibs.appwow.database.BalancingDAO;
import it.unibs.appwow.database.CostsDAO;
import it.unibs.appwow.database.GroupDAO;
import it.unibs.appwow.database.TransactionDAO;
import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.fragments.AmountsFragment;
import it.unibs.appwow.fragments.CostsFragment;
import it.unibs.appwow.fragments.GroupListFragment;
import it.unibs.appwow.fragments.TransactionsFragment;
import it.unibs.appwow.models.BalancingModel;
import it.unibs.appwow.models.Transaction;
import it.unibs.appwow.models.parc.CostModel;
import it.unibs.appwow.models.TransactionModel;
import it.unibs.appwow.models.UserGroupModel;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.GroupModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.DateUtils;
import it.unibs.appwow.models.Amount;

public class GroupDetailsActivity extends AppCompatActivity implements CostsFragment.OnListFragmentInteractionListener,
        AmountsFragment.OnListFragmentInteractionListener,
        TransactionsFragment.OnListFragmentInteractionListener,
        SwipeRefreshLayout.OnRefreshListener{

    private final String TAG_LOG = GroupDetailsActivity.class.getSimpleName();

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

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LocalUser mLocalUser;
    //private int mRequestPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mRequestPending = 0;
        
        setContentView(R.layout.activity_group_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.single_group_swipe_refresh_layout);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(1);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.d(TAG_LOG,"Page Scrolled : "+position);
            }

            @Override
            public void onPageSelected(int position) {
                //Log.d(TAG_LOG,"Page Selected : "+position);
                GroupDetailsActivity.this.invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mLocalUser = LocalUser.load(MyApplication.getAppContext());
        mGroup = (GroupModel) getIntent().getParcelableExtra(GroupListFragment.PASSING_GROUP_TAG);
        setTitle(mGroup.getGroupName());
        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        mSwipeRefreshLayout.post(new Runnable() {
                                     @Override
                                     public void run() {
                                         mSwipeRefreshLayout.setRefreshing(true);
                                         fetchGroupDetails();
                                     }
                                 }
        );

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(CostModel item) {
        // TODO: 07/05/2016 Qui va implementato l'evento da gestire alla selezione dell'item
        Toast.makeText(GroupDetailsActivity.this, "Item: " + item.getId(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onListFragmentInteraction(Amount item) {
        // TODO: 10/05/2016  Qui va implementato l'evento da gestire alla selezione dell'item
        Toast.makeText(GroupDetailsActivity.this, "Item: " + item.getUserId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListFragmentInteraction(Transaction item) {
        // TODO: 10/05/2016  Qui va implementato l'evento da gestire alla selezione dell'item
    }

    @Override
    public void onRefresh() {
        fetchGroupDetails();
    }

    private void fetchGroupDetails(){
        Log.d(TAG_LOG,"fetching group details");
        // showing refresh animation before making http call

        mSwipeRefreshLayout.setRefreshing(true);
        fetchUsers();
        //mRequestPending = 0;
        /**
         * ATTENZIONE QUI SI AGGIORNA L'INTERO GRUPPO COMPRENDENDO:
         * Users
         * Costs
         * Transactions
         * Balancings
         */
        //CONTROLLO che il gruppo sia da aggiornare
        Uri groupUri = WebServiceUri.getGroupUri(mGroup.getId());
        URL url = WebServiceUri.uriToUrl(groupUri);
        JsonObjectRequest groupRequest = new JsonObjectRequest(url.toString(), null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG_LOG, "GROUP response = " + response.toString());
                    GroupDAO dao = new GroupDAO();
                    dao.open();
                    try{
                        String server_updated_at_string = response.getString("updated_at");
                        long server_updated_at = DateUtils.dateStringToLong(server_updated_at_string);
                        long local_updated_at = dao.getUpdatedAt(mGroup.getId());

                        if (server_updated_at > local_updated_at) {
                            fetchCosts();
                        } else {
                            //se il gruppo locale è più aggiornato di quello del server?
                            Log.d(TAG_LOG, "group up to date");
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    } catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG_LOG, "VOLLEY_ERROR - " + "Server Error: " + error.getMessage());
                    Toast.makeText(MyApplication.getAppContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    // stopping swipe refresh
                    mSwipeRefreshLayout.setRefreshing(false);
                }
        });
        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(groupRequest);
        //mRequestPending++;

        //mSwipeRefreshLayout.setRefreshing(false);
    }

    private void fetchUsers(){
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
                                //int id = userJs.getInt("id");
                                //String server_updated_at_string = userJs.getString("updated_at");
                                //long server_updated_at = DateUtils.dateStringToLong(server_updated_at_string);
                                long server_updated_at = user.getUpdatedAt();
                                long local_updated_at = udao.getUpdatedAt(user.getId());
                                //aggiorno lo user solo se subito modifiche
                                if (server_updated_at > local_updated_at) {
                                    /*String fullName = userJs.getString("fullName");
                                    String email = userJs.getString("email");
                                    String created_at_string = userJs.getString("created_at");
                                    long created_at = DateUtils.dateStringToLong(created_at_string);
                                    UserModel u = UserModel.create(id).withFullName(fullName)
                                            .withEmail(email)
                                            .withCreatedAt(created_at)
                                            .withUpdatedAt(server_updated_at);
                                    udao.insertUser(u);*/
                                    udao.insertUser(user);
                                    Log.d(TAG_LOG, "INSERTED USER -> " + user);
                                } else {
                                   Log.d(TAG_LOG, "LocalUser -> " + user + " up to date");
                                }

                                JSONObject pivot = userJs.getJSONObject("pivot");
                                UserGroupModel piv = UserGroupModel.create(pivot);
                                if(!piv.isUpdated()){
                                    ugdao.insertUserGroup(piv);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        udao.close();
                        ugdao.close();
                        /*// mAdapter.notifyDataSetChanged();
                        mAdapter = new GroupAdapter(getActivity());
                        mGridView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();*/

                    } else {
                       // Toast.makeText(MyApplication.getAppContext(), "ERRORE SCONOSCIUTO", Toast.LENGTH_LONG).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG_LOG, "VOLLEY_ERROR - " + "Server Error: " + error.getMessage());
                    Toast.makeText(MyApplication.getAppContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
        });
        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(usersRequest);
    }

    private void fetchCosts(){
        Log.d(TAG_LOG, "fetching costs");
        Uri groupCostsUri = WebServiceUri.getGroupCostsUri(mGroup.getId());
        URL url = WebServiceUri.uriToUrl(groupCostsUri);

        JsonArrayRequest costsRequest = new JsonArrayRequest(url.toString(),
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG_LOG, "COSTS response = " + response.toString());
                        if(response.length() > 0){
                            CostsDAO dao = new CostsDAO();
                            dao.open();
                            dao.resetAllCosts(mGroup.getId());
                            for(int i = 0; i<response.length();i++){
                                try{
                                    JSONObject costJs = response.getJSONObject(i);
                                    CostModel cost = CostModel.create(costJs);
                                    dao.insertCost(cost);
                                } catch(JSONException e){
                                    e.printStackTrace();
                                }
                            }
                            dao.close();
                        }

                        //fetchBalacings(); // FIXME: 15/06/2016 SCOMMENTARE
                        // FIXME: 15/06/2016 SPOSTARE IN FETCHBALANCINGS
                        //AGGIORNO LA DATA DI MODIFICA DEL GRUPPO IN LOCALE
                        GroupDAO dao = new GroupDAO();
                        dao.open();
                        mGroup.setUpdatedAt(System.currentTimeMillis());
                        dao.insertGroup(mGroup);
                        dao.close();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG_LOG, "VOLLEY_ERROR - " + "Server Error: " + error.getMessage());
                        Toast.makeText(MyApplication.getAppContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
        MyApplication.getInstance().addToRequestQueue(costsRequest);
    }

    private void fetchBalacings() {

        Uri groupBalancingsUri = WebServiceUri.getGroupBalancingsUri(mGroup.getId());
        URL url = WebServiceUri.uriToUrl(groupBalancingsUri);

        JsonArrayRequest balancingsRequest = new JsonArrayRequest(url.toString(),
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        
                        if(response.length() > 0){
                            BalancingDAO bdao = new BalancingDAO();
                            bdao.open();
                            TransactionDAO tdao = new TransactionDAO();
                            tdao.open();

                            // FIXME: 20/06/2016 UNCOMMENT
                            //bdao.resetAllBalancings(mGroup.getUserId()); //cancella anche tutte le transactions se funziona on delete cascade
                            for(int i = 0; i<response.length();i++){
                                try{
                                    JSONObject balJs = response.getJSONObject(i);
                                    BalancingModel b = BalancingModel.create(balJs);
                                    bdao.insertBalancing(b);
                                    JSONArray transactions = balJs.getJSONArray("transactions");
                                    for(int j = 0; j<transactions.length(); j++){
                                        JSONObject tjs = transactions.getJSONObject(j);
                                        TransactionModel t = TransactionModel.create(tjs);
                                        tdao.insertTransaction(t);
                                    }
                                } catch(JSONException e){
                                    e.printStackTrace();
                                }
                            }
                            bdao.close();
                            tdao.close();
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG_LOG, "VOLLEY_ERROR - " + "Server Error: " + error.getMessage());
                        Toast.makeText(MyApplication.getAppContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
        MyApplication.getInstance().addToRequestQueue(balancingsRequest);


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
                    return CostsFragment.newInstance(1, mGroup);
                case 1:
                    return AmountsFragment.newInstance(1, mGroup, mLocalUser.getId());
                case 2:
                    return TransactionsFragment.newInstance(1, mGroup);
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
                    return "SPESE";
                case 1:
                    return "RIASSUNTO";
                case 2:
                    return "SCAMBI";
            }
            return null;
        }
    }
}
