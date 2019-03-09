package es.kleiren.madclimb.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
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
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.zone_activity.ZoneActivity;

import static android.content.Context.MODE_PRIVATE;

public class ZoneListFavFragment extends Fragment {

    private ZoneDataAdapter adapter;
    private SearchView searchView;
    private DatabaseReference mDatabase;
    private ArrayList<Zone> zonesFromFirebase;
    private Zone zone;
    private ArrayList<Zone> zoneList = new ArrayList<>();
    private ObservableZoneList observableZoneList;

    @BindView(R.id.card_recycler_view_zones)
    RecyclerView recyclerView;
    @BindView(R.id.emptyFavView)
    View emptyFavView;

    public ZoneListFavFragment() {
    }

    public static ZoneListFavFragment newInstance() {
        return new ZoneListFavFragment();
    }

    private Observer zoneListChanged = new Observer() {
        @Override
        public void update(Observable o, Object newValue) {
            zoneList = (ArrayList<Zone>) newValue;
            adapter = new ZoneDataAdapter(zoneList, getActivity());
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    };

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
        emptyFavView.setVisibility(View.GONE);
        return zoneView;
    }

    private void prepareData() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("zones").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Zone zone = postSnapshot.getValue(Zone.class);
                    if (getActivity().getSharedPreferences("FAVOURITES", MODE_PRIVATE).getBoolean(zone.getId(), false))
                        zonesFromFirebase.add(zone);
                }
                if(zonesFromFirebase.isEmpty())
                    emptyFavView.setVisibility(View.VISIBLE);
                else
                    emptyFavView.setVisibility(View.GONE);
                observableZoneList = new ObservableZoneList();
                observableZoneList.getZonesFromFirebaseZoneList(zonesFromFirebase, getActivity());
                observableZoneList.addObserver(zoneListChanged);
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

    private void initViews() {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ZoneDataAdapter(zonesFromFirebase, getActivity());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

            });

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && gestureDetector.onTouchEvent(e)) {
                    int position = rv.getChildAdapterPosition(child);
                    zone = adapter.getZone(position);
                    Intent intent = new Intent(getActivity(), ZoneActivity.class);
                    intent.putExtra("zone", zone);
                    startActivityForResult(intent, 1);
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        zonesFromFirebase.clear();
        prepareData();
        initViews();
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
        search(searchView);
    }

}
