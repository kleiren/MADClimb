package es.kleiren.madclimb.sector_activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.extra_activities.InfoActivity;
import es.kleiren.madclimb.util.Miscellaneous;

public class SectorIndexActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {


    @BindView(R.id.sectorAct_tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.sectorAct_toolbar)
    Toolbar toolbar;
    @BindView(R.id.sectorAct_pager)
    ViewPager viewPager;

    private NavigationAdapter mSectionsPagerAdapter;
    private ArrayList<Sector> sectorsFromFirebase;
    private ArrayList<String> sectorTitles = new ArrayList<>();
    private Zone zone;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sector);

        ButterKnife.bind(this);

        zone = (Zone) getIntent().getSerializableExtra("zone");
        int currentSectorPosition = getIntent().getIntExtra("currentSectorPosition", 0);
        sectorsFromFirebase = (ArrayList<Sector>) getIntent().getSerializableExtra("sectors");

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        setTitle(zone.getName());

        for (Sector sector : sectorsFromFirebase) sectorTitles.add(sector.getName());

        mSectionsPagerAdapter = new NavigationAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);
        viewPager.setCurrentItem(currentSectorPosition);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sector_activity, menu);
        for (int i = 0; i < menu.size(); ++i) {
            final MenuItem item = menu.getItem(i);
            item.getIcon().setColorFilter(getResources().getColor(R.color.colorSecondary_text), PorterDuff.Mode.SRC_ATOP);
        }
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

        if (id == R.id.error) {
            Miscellaneous.sendReport(this, "SectorIndexActivity " + zone.getName());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public class NavigationAdapter extends FragmentPagerAdapter {

        NavigationAdapter(FragmentManager fm) {
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
            Sector sector = sectorsFromFirebase.get(position);
            if (sector.hasSubSectors())
                f = SectorIndexListFragment.newInstance(zone, sector);
            else
                f = RouteListFragment.newInstance(sector);
            return f;
        }
    }
}
