package es.kleiren.madclimb.sector_activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.extra_activities.ImageViewerActivity;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Route;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.root.GlideApp;


public class RouteListFragment extends Fragment {


    private ArrayList<Route> routesFromFirebase;
    private RouteDataAdapter adapter;
    private Activity parentActivity;
    private Sector sector;

    @BindView(R.id.card_route_view)
    RecyclerView recyclerRoute;
    @BindView(R.id.route_imgCroquis)
    ImageView imgCroquis;
    @BindView(R.id.route_initial_progress)
    ProgressBar initialProgress;

    public RouteListFragment() {}

    private static final String ARG_SECTOR = "sector";

    public static RouteListFragment newInstance(Sector sector) {
        RouteListFragment fragment = new RouteListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SECTOR, sector);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sector = (Sector) getArguments().getSerializable(ARG_SECTOR);
        }
        parentActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View routeView = inflater.inflate(R.layout.fragment_route_list, container, false);

        ButterKnife.bind(this, routeView);

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        final StorageReference load = mStorageRef.child(sector.getImg());

        GlideApp.with(getActivity())
                .load(load)
                .placeholder(R.drawable.mountain_placeholder)
                .into(imgCroquis);

        imgCroquis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
                intent.putExtra("image", sector.getImg());
                intent.putExtra("title", sector.getName());
                startActivityForResult(intent, 1);
            }
        });

        routesFromFirebase = new ArrayList<>();

        (new LoadData()).execute();

        return routeView;
    }

    private void prepareData() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference child;
        if (sector.getParentSector() != null)
            child = mDatabase.child("zones/" + sector.getZone_id() + "/sectors/" + sector.getParentSector() + "/sub_sectors/" + sector.getId() + "/routes");
        else
            child = mDatabase.child("zones/" + sector.getZone_id() + "/sectors/" + sector.getId() + "/routes");
        child.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Route route = postSnapshot.getValue(Route.class);
                    routesFromFirebase.add(route);
                    adapter = new RouteDataAdapter(routesFromFirebase, getActivity(), sector);
                }
                initViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
            }
        });
    }

    private void initViews() {
        recyclerRoute.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerRoute.setHasFixedSize(false);
        recyclerRoute.setAdapter(adapter);
    }

    private class LoadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            prepareData();
            return "Executed";
        }

        @Override
        protected void onPreExecute() {
            initialProgress.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            initialProgress.setVisibility(View.GONE);
            super.onPostExecute(s);
        }
    }
}
