package es.kleiren.madclimb.zone_activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
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
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.sector_activity.SectorActivity;
import es.kleiren.madclimb.data_classes.Zone;


public class SectorListFragment extends Fragment {

    private SectorDataAdapter adapter;
    private ArrayList<Sector> sectorsFromFirebase = new ArrayList<>();
    private Activity parentActivity;
    private Zone zone;
    private static final String ARG_ZONE = "zone";
    private ObservableRecyclerView recyclerView;
    private ObservableSectorList observableSectorList;

    @BindView(R.id.card_sector_view)
    ObservableRecyclerView recyclerSector;
    @BindView(R.id.sector_initial_progress)
    ConstraintLayout initialProgress;

    public SectorListFragment() {
    }

    private ArrayList<Sector> sectors;
    private Observer sectorListChanged = new Observer() {
        @Override
        public void update(Observable o, Object newValue) {

            sectors = (ArrayList<Sector>) newValue;
            adapter = new SectorDataAdapter(sectors, getActivity());
            recyclerSector.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            initialProgress.setVisibility(View.GONE);

        }
    };

    public static SectorListFragment newInstance(Zone zone) {
        SectorListFragment fragment = new SectorListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ZONE, zone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
        if (getArguments() != null) {
            zone = (Zone) getArguments().getSerializable(ARG_ZONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View zoneView = inflater.inflate(R.layout.fragment_sector_list, container, false);
        ButterKnife.bind(this, zoneView);


        prepareData();

        initViews();
        return zoneView;
    }


    private void prepareData() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("zones/" + zone.getId() + "/sectors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("FIREBASE", dataSnapshot.getValue().toString());

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Sector sector = postSnapshot.getValue(Sector.class);
                    sectorsFromFirebase.add(sector);
                }

                observableSectorList = new ObservableSectorList();
                observableSectorList.getSectorImagesFromFirebase(sectorsFromFirebase, getActivity());
                observableSectorList.addObserver(sectorListChanged);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
            }
        });


    }


    private void initViews() {

        recyclerSector.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerSector.setHasFixedSize(false);
        recyclerSector.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.container));

        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            recyclerSector.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

        adapter = new SectorDataAdapter(sectorsFromFirebase, getActivity());
        recyclerSector.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        recyclerSector.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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

                    Intent intent = new Intent(getActivity(), SectorActivity.class);
                    intent.putExtra("zone", zone);
                    intent.putExtra("sectors", sectorsFromFirebase);
                    intent.putExtra("currentSectorPosition", position);
                    startActivity(intent);
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

}
