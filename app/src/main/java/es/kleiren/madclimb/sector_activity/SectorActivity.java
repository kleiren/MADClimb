package es.kleiren.madclimb.sector_activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import es.kleiren.madclimb.extra_activities.InfoActivity;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.util.SlidingTabLayout;
import es.kleiren.madclimb.data_classes.Zone;

public class SectorActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ArrayList<Sector> sectorsFromFirebase;

    private ViewPager mViewPager;
    private DatabaseReference mDatabase;

    private ArrayList<String> sectorTitles = new ArrayList<>();

    private Zone zone;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sector);

        zone = (Zone) getIntent().getSerializableExtra("zone");
        int currentSectorPosition = getIntent().getIntExtra("currentSectorPosition", 0);
        sectorsFromFirebase = (ArrayList<Sector>) getIntent().getSerializableExtra("sectors");

        Toolbar toolbar = findViewById(R.id.sectorAct_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setTitle(zone.getName());

        for (Sector sector : sectorsFromFirebase) sectorTitles.add(sector.getName());

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(currentSectorPosition);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.colorAccent));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mViewPager);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sector_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.info) {

            Intent intent = new Intent(this, InfoActivity.class);
            intent.putExtra("type", "zone");
            intent.putExtra("datum", zone);
            startActivity(intent);
            return true;
        }

        if (id == R.id.error){
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","madclimbapp@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MADClimb error");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                return true;

        }
        return super.onOptionsItemSelected(item);
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
        public int getCount() {
            return sectorTitles.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return sectorTitles.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f;
            f = RouteListFragment.newInstance(sectorsFromFirebase.get(position));
            return f;
        }
    }
}
