package es.kleiren.madclimb.zone_activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.extra_activities.ImageViewerActivity;
import es.kleiren.madclimb.extra_activities.InfoActivity;
import es.kleiren.madclimb.root.GlideApp;

public class ZoneActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener, ViewPager.OnPageChangeListener {

    @BindView(R.id.zoneAct_tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.zoneAct_toolbar)
    Toolbar toolbar;
    @BindView(R.id.zoneAct_appbar)
    AppBarLayout collapsingAppbar;
    @BindView(R.id.zoneAct_collapsingToolbarLayout)
    CollapsingToolbarLayout mCollapsing;
    @BindView(R.id.zoneAct_pager)
    ViewPager viewPager;
    @BindView(R.id.zoneAct_nestedScroll)
    NestedScrollView nestedScrollView;
    @BindView(R.id.zoneAct_imageZone)
    View imageZone;
    @BindView(R.id.zoneAct_progressBar)
    ProgressBar progressBar;

    private static final int PERCENTAGE_TO_SHOW_IMAGE = 20;
    private int mMaxScrollSize;
    private boolean mIsImageHidden;
    private Zone zone;
    private NavigationAdapter navigationAdapter;
    private boolean isFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone);

        ButterKnife.bind(this);

        makeStatusBarTransparent();


        zone = (Zone) getIntent().getSerializableExtra("zone");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {

            CollapsingToolbarLayout.LayoutParams lp = new CollapsingToolbarLayout.LayoutParams(CollapsingToolbarLayout.LayoutParams.MATCH_PARENT,
                    getActionBarHeight());
            lp.setMargins(0, getStatusBarHeight(), 0, 0);
            toolbar.setLayoutParams(lp);
        }

        ViewGroup.LayoutParams params2 = toolbar.getLayoutParams();
        CollapsingToolbarLayout.LayoutParams newParams;
        if (params2 instanceof CollapsingToolbarLayout.LayoutParams) {
            newParams = (CollapsingToolbarLayout.LayoutParams) params2;
        } else {
            newParams = new CollapsingToolbarLayout.LayoutParams(params2);
        }
        newParams.setCollapseMode(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN);
        toolbar.setLayoutParams(newParams);
        toolbar.requestLayout();

        collapsingAppbar.addOnOffsetChangedListener(this);
        mCollapsing.setTitle(zone.getName());
        navigationAdapter = new NavigationAdapter(getSupportFragmentManager());
        viewPager.setAdapter(navigationAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(this);

        isFavourite = getSharedPreferences("FAVOURITES", MODE_PRIVATE).getBoolean(zone.getId(), false);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) collapsingAppbar.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference load = mStorageRef.child(zone.getImg());

        GlideApp.with(getApplicationContext())
                .load(load)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into((ImageView) imageZone);

        imageZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ImageViewerActivity.class);
                intent.putExtra("image", zone.getImg());
                intent.putExtra("title", zone.getName());
                startActivityForResult(intent, 1);
            }
        });
    }

    private void makeStatusBarTransparent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            // Necessary for transparent statusBar
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        }
    }

    private int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public void disableScroll() {
        final SectorListMapFragment mSupportMapFragment;
        mSupportMapFragment = (SectorListMapFragment) navigationAdapter.getFragment(1);
        if (mSupportMapFragment != null)
            mSupportMapFragment.setListener(new SectorListMapFragment.OnTouchListener() {
                @Override
                public void onTouch() {
                    nestedScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0) mMaxScrollSize = appBarLayout.getTotalScrollRange();
        int currentScrollPercentage = (Math.abs(i)) * 100 / mMaxScrollSize;
        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE && !mIsImageHidden)
            mIsImageHidden = true;
        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE && mIsImageHidden)
            mIsImageHidden = false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 1) {
            collapsingAppbar.setExpanded(false, true);
        } else {
            collapsingAppbar.setExpanded(true, true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_zone_activity, menu);
        setFavouriteStar(menu.getItem(0), isFavourite);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.fav) {
            isFavourite = getSharedPreferences("FAVOURITES", MODE_PRIVATE).getBoolean(zone.getId(), false);
            isFavourite = setFavourite(!isFavourite);
            setFavouriteStar(item, isFavourite);
            return true;
        }
        if (id == R.id.info) {
            Intent intent = new Intent(this, InfoActivity.class);
            intent.putExtra("type", "zone");
            intent.putExtra("datum", zone);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean setFavourite(boolean add) {
        getSharedPreferences("FAVOURITES", MODE_PRIVATE)
                .edit()
                .putBoolean(zone.getId(), add)
                .apply();
        return add;
    }

    private void setFavouriteStar(MenuItem item, boolean isFavourite) {
        if (isFavourite)
            item.setIcon(R.drawable.ic_star);
        else
            item.setIcon(R.drawable.ic_star_outline);
    }

    private class NavigationAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = new String[]{getString(R.string.sectors), getString(R.string.map)};
        private Map<Integer, String> mFragmentTags;
        private FragmentManager mFragmentManager;

        NavigationAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            mFragmentManager = fm;
            mFragmentTags = new HashMap<>();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f;
            final int pattern = position % 5;
            switch (pattern) {
                case 0:
                default:
                    f = SectorListFragment.newInstance(zone);
                    break;
                case 1:
                    f = SectorListMapFragment.newInstance(zone);
                    break;
            }
            return f;
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object obj = super.instantiateItem(container, position);
            if (obj instanceof Fragment) {
                Fragment f = (Fragment) obj;
                String tag = f.getTag();
                mFragmentTags.put(position, tag);
            }
            return obj;
        }

        Fragment getFragment(int position) {
            String tag = mFragmentTags.get(position);
            if (tag == null)
                return null;
            return mFragmentManager.findFragmentByTag(tag);
        }
    }
}
