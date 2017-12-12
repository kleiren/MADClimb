package es.kleiren.madclimb.sector_activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.extra_activities.ImageViewerActivity;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Route;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.root.GlideApp;

import static android.content.Context.MODE_PRIVATE;


public class RouteListFragment extends Fragment {

    @BindView(R.id.card_route_view)
    ObservableRecyclerView recyclerRoute;

    private ArrayList<Route> routesFromFirebase;
    private RouteDataAdapter adapter;
    private Activity parentActivity;
    private Sector sector;

    @BindView(R.id.route_imgCroquis)
    ImageView imgCroquis;

    public RouteListFragment() {
    }

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

        final StorageReference load = mStorageRef.child(sector.getCroquis());

        GlideApp.with(getActivity())
                .load(load)
                .placeholder(R.drawable.mountain_placeholder)
                .into(imgCroquis);


        imgCroquis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
                intent.putExtra("image", sector.getCroquis());
                intent.putExtra("title", sector.getName());
                startActivityForResult(intent, 1);
            }
        });

        routesFromFirebase = new ArrayList<>();

        prepareData();

        return routeView;
    }


    private void prepareData() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("zones/" + sector.getZone_id() + "/sectors/" + sector.getId() + "/routes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //  Log.i("FIREBASE", dataSnapshot.getValue().toString());

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
        recyclerRoute.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.container));

        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            recyclerRoute.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

        recyclerRoute.setAdapter(adapter);

        recyclerRoute.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

            });

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
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
