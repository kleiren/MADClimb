package es.kleiren.leviathan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class ZoneListFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ZoneDataAdapter adapter;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private StorageReference mStorageRef;

    public ZoneListFragment() {
    }


    public static ZoneListFragment newInstance() {
        ZoneListFragment fragment = new ZoneListFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View zoneView = inflater.inflate(R.layout.fragment_zones, container, false);

        initViews(zoneView);

        searchView = (SearchView) zoneView.findViewById(R.id.searchView);

        search(searchView);


        return zoneView;
    }

    private final String zone_names[] = {
            "Donut",
            "Eclair",
            "Froyo",
            "Gingerbread",
            "Honeycomb",
            "Ice Cream Sandwich",
            "Jelly Bean",
            "KitKat",
            "Lollipop",
            "Marshmallow"
    };

    private final int zone_image_resource[] = {
            R.raw.yelmo,
            R.raw.yelmo, R.raw.yelmo, R.raw.yelmo, R.raw.yelmo, R.raw.yelmo, R.raw.yelmo, R.raw.yelmo, R.raw.yelmo, R.raw.yelmo

    };


    private ArrayList prepareData() {

        ArrayList aZone = new ArrayList<>();
        for (int i = 0; i < zone_names.length; i++) {
            Zone zone = new Zone();
            zone.setName(zone_names[i]);
            zone.setResource(zone_image_resource[i]);
            aZone.add(zone);
        }
        return aZone;
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

        ArrayList zones = prepareData();
        adapter = new ZoneDataAdapter(zones, getActivity());
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
