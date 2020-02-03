package es.kleiren.madclimb.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.zone_activity.ZoneActivity;

public class ZoneListFragment extends Fragment {

    private ZoneDataAdapter adapter;
    private SearchView searchView;
    private DatabaseReference mDatabase;
    private ArrayList<Zone> zonesFromFirebase;
    private Zone zone;

    @BindView(R.id.card_recycler_view_zones)
    RecyclerView recyclerView;

    public ZoneListFragment() {
    }

    public static ZoneListFragment newInstance() {
        return new ZoneListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View zoneView = inflater.inflate(R.layout.fragment_zone_list, container, false);
        ButterKnife.bind(this, zoneView);
        zonesFromFirebase = new ArrayList<>();
        prepareData();
        initViews();
        return zoneView;
    }

    private void prepareData() {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("zones").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                zonesFromFirebase.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Zone zone = postSnapshot.getValue(Zone.class);
                    zone.numberOfSectors = ((int) postSnapshot.child("sectors").getChildrenCount());
                    for (DataSnapshot sector : postSnapshot.child("sectors").getChildren()) {
                        zone.numberOfRoutes = zone.numberOfRoutes + ((int) sector.child("routes").getChildrenCount());
                        for (DataSnapshot subSector : sector.child("sub_sectors").getChildren()) {
                            zone.numberOfRoutes = zone.numberOfRoutes + ((int) subSector.child("routes").getChildrenCount());
                        }
                    }
                    zonesFromFirebase.add(zone);
                }
                Collections.sort(zonesFromFirebase, (o1, o2) -> {
                    if (o1.getPosition() != null && o2.getPosition() != null)
                        return o1.getPosition().compareTo(o2.getPosition());
                    else
                        return o1.getName().compareTo(o2.getName());
                });
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
            }
        });
    }

    private void search(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return 0;
    }

    private void initViews() {
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(getActionBarHeight() + 40));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ZoneDataAdapter(zonesFromFirebase, getActivity(), (AppCompatActivity) getActivity(), false);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem searchViewMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchViewMenuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        search(searchView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.search) {
            if (searchView.getVisibility() == View.VISIBLE)
                searchView.setVisibility(View.GONE);
            else if (searchView.getVisibility() == View.GONE) {
                searchView.setFocusableInTouchMode(true);
                searchView.setVisibility(View.VISIBLE);
                searchView.requestFocus();
                searchView.onActionViewExpanded();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpaceHeight;

        VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = verticalSpaceHeight;
            }
        }
    }
}
