package es.kleiren.leviathan.sector_activity;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.net.Uri;
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

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import es.kleiren.leviathan.extra_activities.ImageViewerActivity;
import es.kleiren.leviathan.R;
import es.kleiren.leviathan.data_classes.Route;
import es.kleiren.leviathan.data_classes.Sector;
import es.kleiren.leviathan.root.GlideApp;


public class RouteListFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ObservableRecyclerView recyclerView;
    private StorageReference mStorageRef;

    private FloatingActionButton btnAddRoute;
    private DatabaseReference mDatabase;
    private ArrayList<Route> routesFromFirebase;
    private RouteDataAdapter adapter;
    private Activity parentActivity;
    private Sector sector;

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
        View routeView = inflater.inflate(R.layout.fragment_routes, container, false);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference load = mStorageRef.child(sector.getCroquis());
        GlideApp.with(getActivity())
                .load(load).into((ImageView) routeView.findViewById(R.id.croquisView));

        routeView.findViewById(R.id.croquisView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
                intent.putExtra("image", sector.getCroquis());
                intent.putExtra("title", sector.getName());
                startActivityForResult(intent, 1);
            }
        });
        routesFromFirebase = new ArrayList<>();
        prepareData(routeView);

//        btnAddRoute = (FloatingActionButton) routeView.findViewById(R.id.fab_addRoute);
//
//        btnAddRoute.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        return routeView;
    }


    private void prepareData(final View view) {

        mDatabase = FirebaseDatabase.getInstance().getReference();



        // Attach a listener to read the data at our posts reference
        mDatabase.child("zones/"+ sector.getZone_id() + "/sectors/" + sector.getId() + "/routes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("FIREBASE", dataSnapshot.getValue().toString());

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Route route = postSnapshot.getValue(Route.class);
                    routesFromFirebase.add(route);
                    adapter = new RouteDataAdapter(routesFromFirebase, getActivity(), sector);
                }
                initViews(view);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
            }
        });


    }

    private void initViews(View view) {

        recyclerView = view.findViewById(R.id.card_route_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(false);
        recyclerView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.container));

        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            recyclerView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

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

//                View child = rv.findChildViewUnder(e.getX(), e.getY());
//                if (child != null && gestureDetector.onTouchEvent(e)) {
//                    int position = rv.getChildAdapterPosition(child);
//
//                    Intent intent = new Intent(getActivity(), ZoneActivity.class);
//                    startActivity(intent);
//                    //Toast.makeText(getActivity().getApplicationContext(), countries.get(position).toString(), Toast.LENGTH_SHORT).show();
//                }

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


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }







}
