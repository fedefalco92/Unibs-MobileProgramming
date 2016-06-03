package it.unibs.appwow;

import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import it.unibs.appwow.fragments.AmountsFragment;
import it.unibs.appwow.fragments.CostsFragment;
import it.unibs.appwow.fragments.TransactionsFragment;
import it.unibs.appwow.utils.dummy.DummyTransactionContent;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.Cost;
import it.unibs.appwow.models.parc.Group;

public class GroupDetailsActivity extends AppCompatActivity implements CostsFragment.OnListFragmentInteractionListener, AmountsFragment.OnListFragmentInteractionListener, TransactionsFragment.OnListFragmentInteractionListener {

    private final String TAG_LOG = GroupDetailsActivity.class.getSimpleName();

    private Group mGroup;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGroup = getIntent().getParcelableExtra("group");
        setTitle(mGroup.getGroupName());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

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
    public void onListFragmentInteraction(Cost item) {
        // TODO: 07/05/2016 Qui va implementato l'evento da gestire alla selezione dell'item
        Toast.makeText(GroupDetailsActivity.this, "Item: " + item.id, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onListFragmentInteraction(Amount item) {
        // TODO: 10/05/2016  Qui va implementato l'evento da gestire alla selezione dell'item
        Toast.makeText(GroupDetailsActivity.this, "Item: " + item.id, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListFragmentInteraction(DummyTransactionContent.Transaction item) {
        // TODO: 10/05/2016  Qui va implementato l'evento da gestire alla selezione dell'item
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
                    return CostsFragment.newInstance(1);
                case 1:
                    return AmountsFragment.newInstance(1);
                case 2:
                    return TransactionsFragment.newInstance(1);
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
