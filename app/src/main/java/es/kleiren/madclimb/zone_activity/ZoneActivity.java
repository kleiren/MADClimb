/*
 * Copyright (C) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.kleiren.madclimb.zone_activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.extra_activities.ImageViewerActivity;
import es.kleiren.madclimb.extra_activities.InfoFragment;
import es.kleiren.madclimb.root.GlideApp;

public class ZoneActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener, ViewPager.OnPageChangeListener {

    @BindView(R.id.zoneAct_tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.zoneAct_toolbar)
    Toolbar toolbar;
    @BindView(R.id.zoneAct_appbar)
    AppBarLayout collapsingAppbar;
    @BindView(R.id.zoneAct_collapTollbarLayout)
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

        zone = (Zone) getIntent().getSerializableExtra("zone");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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

    public void collapseToolbar() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) collapsingAppbar.getLayoutParams();
        final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        params.setBehavior(behavior);
        if (behavior != null) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt();
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    behavior.setTopAndBottomOffset((Integer) animation.getAnimatedValue());
                    collapsingAppbar.requestLayout();
                    behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                        @Override
                        public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                            return false;
                        }
                    });
                }
            });
            valueAnimator.setIntValues(0, -900);
            valueAnimator.setDuration(600);
            valueAnimator.start();
        }
    }

    public void expandToolbar() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) collapsingAppbar.getLayoutParams();
        final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        params.setBehavior(behavior);
        if (behavior != null) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt();
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    behavior.setTopAndBottomOffset((Integer) animation.getAnimatedValue());
                    collapsingAppbar.requestLayout();
                    behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                        @Override
                        public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                            return false;
                        }
                    });
                }
            });
            valueAnimator.setIntValues(-900, 0);
            valueAnimator.setDuration(600);
            valueAnimator.start();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 1) {
            collapseToolbar();
        } else {
            expandToolbar();
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
        return super.onOptionsItemSelected(item);
    }

    private boolean setFavourite(boolean add){
        getSharedPreferences("FAVOURITES", MODE_PRIVATE)
                .edit()
                .putBoolean(zone.getId(), add)
                .apply();
        return add;
    }

    private void setFavouriteStar(MenuItem item, boolean isFavourite){
        if (isFavourite)
            item.setIcon(R.drawable.ic_star);
        else
            item.setIcon(R.drawable.ic_star_outline);
    }

    private class NavigationAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = new String[]{"Sectores", "Mapa", "Información"};
        private Map<Integer, String> mFragmentTags;
        private FragmentManager mFragmentManager;

        public NavigationAdapter(FragmentManager fm) {

            super(fm);
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
                case 2:
                    f = InfoFragment.newInstance("zone", zone);
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
