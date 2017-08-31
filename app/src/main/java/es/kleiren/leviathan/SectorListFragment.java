package es.kleiren.leviathan;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class SectorListFragment extends BaseFragment {

    private OnFragmentInteractionListener mListener;
    private SectorDataAdapter adapter;
    private SearchView searchView;
    private ObservableRecyclerView recyclerView;
    private StorageReference mStorageRef;
    private FloatingActionButton btnAddSector;
    private DatabaseReference mDatabase;
    private ArrayList<Sector> sectorsFromFirebase;
    private Activity parentActivity;


    public SectorListFragment() {
    }


    public static SectorListFragment newInstance() {
        SectorListFragment fragment = new SectorListFragment();

        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mStorageRef = FirebaseStorage.getInstance().getReference();
        parentActivity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View zoneView = inflater.inflate(R.layout.fragment_recyclerview, container, false);


    //    btnAddSector = (FloatingActionButton) zoneView.findViewById(R.id.fab_addSector);

        prepareData(zoneView);

        sectorsFromFirebase = new ArrayList<>();

//        btnAddSector.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Sector newSector = new Sector("Placas del Halcon", "Pedriza", 3, 3);
//
//                UploadHelper.uploadSector(newSector);
//
//            }
//        });

//
//        searchView = (SearchView) zoneView.findViewById(R.id.searchView);
//
//        search(searchView);


        return zoneView;
    }




    private void prepareData(final View sectorView) {

        mDatabase = FirebaseDatabase.getInstance().getReference();


        // Attach a listener to read the data at our posts reference
        mDatabase.child("zones/"+ MainActivity.currentZone.getName() + "/sectors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("FIREBASE", dataSnapshot.getValue().toString());

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Sector sector = postSnapshot.getValue(Sector.class);

                    Log.i("FIREBASE", "=======name: " + postSnapshot.child("name").getValue());
                    Log.i("FIREBASE", "=======resource: " + postSnapshot.child("image").getValue());

                    Log.i("FIREBASE", "=======zonename: " + sector.getName());
                    Log.i("FIREBASE", "=======zoneres: " + sector.getImage());

                    sectorsFromFirebase.add(sector);

                    adapter = new SectorDataAdapter(sectorsFromFirebase, getActivity());

                }
                initViews(sectorView);

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

    private void initViews(View view) {

        recyclerView = (ObservableRecyclerView) view.findViewById(R.id.scroll);
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

                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && gestureDetector.onTouchEvent(e)) {
                    int position = rv.getChildAdapterPosition(child);
                    MainActivity.currentSector = sectorsFromFirebase.get(position);

                    Intent intent = new Intent(getActivity(), SectorActivity.class);
                    startActivity(intent);
                    //Toast.makeText(getActivity().getApplicationContext(), countries.get(position).toString(), Toast.LENGTH_SHORT).show();
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
