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
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.sector_activity.SectorIndexActivity;
import es.kleiren.madclimb.zone_activity.ObservableSectorList;
import es.kleiren.madclimb.zone_activity.SectorDataAdapter;
import es.kleiren.madclimb.zone_activity.ZoneActivity;

public class ZoneListFragment extends Fragment {

    private ZoneDataAdapter zoneDataAdapter;
    private SectorDataAdapter sectorDataAdapter;
    private SearchView searchView;
    private DatabaseReference mDatabase;
    private ArrayList<Zone> zonesFromFirebase = new ArrayList<>();
    private ArrayList<Sector> sectorsFromFirebase = new ArrayList<>();
    private ArrayList<Zone> zoneList = new ArrayList<>();
    private ArrayList<Sector> sectorList = new ArrayList<>();
    private ObservableZoneList observableZoneList;
    private ObservableSectorList observableSectorList;

    @BindView(R.id.card_recycler_view_zones)
    RecyclerView recyclerViewZones;
    @BindView(R.id.card_recycler_view_sectors)
    RecyclerView recyclerViewSectors;
    @BindView(R.id.textView_searchZones)
    TextView textViewSearchZones;
    @BindView(R.id.layout_sectorList)
    View sectorListView;

    public ZoneListFragment() {
    }

    public static ZoneListFragment newInstance() {
        return new ZoneListFragment();
    }

    private Observer zoneListChanged = new Observer() {
        @Override
        public void update(Observable o, Object newValue) {
            zoneList = (ArrayList<Zone>) newValue;
            zoneDataAdapter = new ZoneDataAdapter(zoneList, getActivity());
            recyclerViewZones.setAdapter(zoneDataAdapter);
            zoneDataAdapter.notifyDataSetChanged();
        }
    };

    private Observer sectorListChanged = new Observer() {
        @Override
        public void update(Observable o, Object newValue) {
            sectorList = (ArrayList<Sector>) newValue;
            sectorDataAdapter = new SectorDataAdapter(sectorList, getActivity());
            recyclerViewSectors.setAdapter(sectorDataAdapter);
            sectorDataAdapter.notifyDataSetChanged();
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
        prepareData(false);
        zoneDataAdapter = new ZoneDataAdapter(zonesFromFirebase, getActivity());
        initViews(recyclerViewZones, zoneDataAdapter, "zone");

        textViewSearchZones.setVisibility(View.GONE);
        sectorListView.setVisibility(View.GONE);
        return zoneView;
    }

    private void prepareData(final boolean full) {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("zones").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Zone zone = postSnapshot.getValue(Zone.class);
                    zonesFromFirebase.add(zone);

                    if (full) {
                        for (DataSnapshot postPostSnapshot : postSnapshot.child("sectors").getChildren()) {
                            Sector sector = postPostSnapshot.getValue(Sector.class);
                            sectorsFromFirebase.add(sector);
                        }
                        Collections.sort(sectorsFromFirebase, new Comparator<Sector>() {
                            public int compare(Sector o1, Sector o2) {
                                if (o1.getPosition() != null && o2.getPosition() != null)
                                    return o1.getPosition().compareTo(o2.getPosition());
                                else
                                    return o1.getName().compareTo(o2.getName());
                            }
                        });
                    }

                }
                Collections.sort(zonesFromFirebase, new Comparator<Zone>() {
                    public int compare(Zone o1, Zone o2) {
                        if (o1.getPosition() != null && o2.getPosition() != null)
                            return o1.getPosition().compareTo(o2.getPosition());
                        else
                            return o1.getName().compareTo(o2.getName());
                    }
                });
                observableZoneList = new ObservableZoneList();
                observableZoneList.getZonesFromFirebaseZoneList(zonesFromFirebase, getActivity());
                observableZoneList.addObserver(zoneListChanged);
                if (full) {
                    observableSectorList = new ObservableSectorList();
                    observableSectorList.getSectorImagesFromFirebase(sectorsFromFirebase, getActivity());
                    observableSectorList.addObserver(sectorListChanged);
                    sectorDataAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
            }
        });
    }

    private void prepareSectorData() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("*/sectors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Sector sector = postSnapshot.getValue(Sector.class);
                    sectorsFromFirebase.add(sector);
                }
                Collections.sort(sectorsFromFirebase, new Comparator<Sector>() {
                    public int compare(Sector o1, Sector o2) {
                        if (o1.getPosition() != null && o2.getPosition() != null)
                            return o1.getPosition().compareTo(o2.getPosition());
                        else
                            return o1.getName().compareTo(o2.getName());
                    }
                });
                observableSectorList = new ObservableSectorList();
                observableSectorList.getSectorImagesFromFirebase(sectorsFromFirebase, getActivity());
                observableSectorList.addObserver(sectorListChanged);
                sectorDataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
            }
        });
    }

    private void search(SearchView searchView) {

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                textViewSearchZones.setVisibility(View.GONE);
                sectorListView.setVisibility(View.GONE);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewSearchZones.setVisibility(View.VISIBLE);
                sectorListView.setVisibility(View.VISIBLE);

                prepareData(true);
                sectorDataAdapter = new SectorDataAdapter(sectorsFromFirebase, getActivity());
                initViews(recyclerViewSectors, sectorDataAdapter, "sector");
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                zoneDataAdapter.getFilter().filter(newText);
                sectorDataAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void initViews(RecyclerView recyclerView, final RecyclerView.Adapter adapter, final String type) {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
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
                    Intent intent;
                    if (type.equals("zone")) {
                        Zone zone = ((ZoneDataAdapter) adapter).getZone(position);
                        intent = new Intent(getActivity(), ZoneActivity.class);
                        intent.putExtra("zone", zone);
                    } else {
                        intent = new Intent(getActivity(), SectorIndexActivity.class);
                        intent.putExtra("sector", sectorsFromFirebase.get(position));
                        intent.putExtra("sectors", sectorsFromFirebase);
                        intent.putExtra("currentSectorPosition", position);
                    }
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
