package es.kleiren.madclimb.zone_activity;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.sector_activity.SectorIndexActivity;


public class SectorListFragment extends Fragment {

    private SectorDataAdapter adapter;
    private ArrayList<Sector> sectorsFromFirebase = new ArrayList<>();
    private Zone zone;
    private static final String ARG_ZONE = "zone";

    @BindView(R.id.card_sector_view)
    RecyclerView recyclerSector;
    @BindView(R.id.sector_initial_progress)
    ProgressBar initialProgress;

    public SectorListFragment() {
    }

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
        if (getArguments() != null) {
            zone = (Zone) getArguments().getSerializable(ARG_ZONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View zoneView = inflater.inflate(R.layout.fragment_sector_list, container, false);
        ButterKnife.bind(this, zoneView);

        if (sectorsFromFirebase.isEmpty())
            prepareData();
        initViews();

        return zoneView;
    }

    private void prepareData() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("zones/" + zone.getId() + "/sectors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sectorsFromFirebase.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Sector sector = postSnapshot.getValue(Sector.class);
                    sector.setZoneName(zone.getName());
                    sector.numberOfRoutes = ((int) postSnapshot.child("routes").getChildrenCount());
                    for (DataSnapshot subSector : postSnapshot.child("sub_sectors").getChildren()){
                        sector.numberOfRoutes = sector.numberOfRoutes + ((int) subSector.child("routes").getChildrenCount());
                    }
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
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    initialProgress.setVisibility(View.GONE);
                }

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
                    Intent intent = new Intent(getActivity(), SectorIndexActivity.class);
                    intent.putExtra("zone", zone);
                    intent.putExtra("sector", sectorsFromFirebase.get(position));
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
