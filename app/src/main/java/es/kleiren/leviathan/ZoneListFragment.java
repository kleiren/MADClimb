package es.kleiren.leviathan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


public class ZoneListFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ZoneDataAdapter adapter;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private StorageReference mStorageRef;
    private FloatingActionButton fabAddZone;
    private DatabaseReference mDatabase;
    private AlertDialog dialog;
    private PullRefreshLayout pullLayout;
    private ArrayList<Zone> zonesFromFirebase;


    public ZoneListFragment() {
    }


    public static ZoneListFragment newInstance() {
        ZoneListFragment fragment = new ZoneListFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);



//        mStorageRef = FirebaseStorage.getInstance().getReference();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the pullLayout for this fragment
        View zoneView = inflater.inflate(R.layout.fragment_zones, container, false);


        zonesFromFirebase = new ArrayList<>();

        prepareData(zoneView);
        fabAddZone = (FloatingActionButton) zoneView.findViewById(R.id.fab_addZone);

        fabAddZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showNewZoneDialog(getActivity());

            }
        });

        pullLayout = (PullRefreshLayout) zoneView.findViewById(R.id.pullLayout);

// listen refresh event
        pullLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullLayout.setRefreshing(false);

                searchView.setFocusableInTouchMode(true);
                searchView.setVisibility(View.VISIBLE);
                searchView.requestFocus();
                searchView.onActionViewExpanded();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
                // start refresh

            }
        });

// refresh complete

        searchView = (SearchView) zoneView.findViewById(R.id.searchView);
        search(searchView);

        searchView.setVisibility(View.GONE);
        return zoneView;
    }

    private final String zone_names[] = {

    };

    private final int zone_image_resource[] = {

    };


    private void prepareData(final View zoneView) {

        mDatabase = FirebaseDatabase.getInstance().getReference();


        // Attach a listener to read the data at our posts reference
        mDatabase.child("zones").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("FIREBASE", dataSnapshot.getValue().toString());

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Zone zone = postSnapshot.getValue(Zone.class);

                    Log.i("FIREBASE", "=======name: "+postSnapshot.child("name").getValue());
                    Log.i("FIREBASE", "=======resource: "+postSnapshot.child("resource").getValue());

                    Log.i("FIREBASE", "=======zonename: "+zone.getName());
                    Log.i("FIREBASE", "=======zoneres: "+zone.getResource());

                    zonesFromFirebase.add(zone);

                    adapter = new ZoneDataAdapter(zonesFromFirebase, getActivity());
                    initViews(zoneView);


                }
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
        recyclerView = (RecyclerView) view.findViewById(R.id.card_recycler_view);
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

                    Intent intent = new Intent(getActivity(), ZoneActivity.class);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
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



    public void showNewZoneDialog(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                LayoutInflater inflater = activity.getLayoutInflater();
                final View newZoneView = inflater.inflate(R.layout.dialog_new_zone, null);

                builder.setPositiveButton("Upload Zone", null);
                builder.setView(newZoneView);
                dialog = builder.create();
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Zone zone = new Zone();

                                zone.setName(((TextView) newZoneView.findViewById(R.id.dia_zoneName)).getText().toString());

                                UploadHelper.uploadZone(zone);

                                    dialog.dismiss();
                            }
                        });
                    }
                });
                dialog.show();
            }
        });
    }


}
