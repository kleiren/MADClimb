package es.kleiren.leviathan;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;


public class RouteListFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private Button btnAddRoute;
    private DatabaseReference mDatabase;

    public RouteListFragment() {
    }


    public static RouteListFragment newInstance() {
        RouteListFragment fragment = new RouteListFragment();

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
        View routeView = inflater.inflate(R.layout.fragment_routes, container, false);

        btnAddRoute = (Button) routeView.findViewById(R.id.btnAddRoute);

        btnAddRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Route newRoute = new Route("Pedriza", "Placas del Halcon", "1", 3, 5);

                UploadHelper.uploadRoute(newRoute, mDatabase );
            }
        });

        initViews(routeView);
        return routeView;
    }

    private final String route_names[] = {
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
            5,5,5,5,5,5,5,5,5,5

    };


    private ArrayList prepareData() {

        ArrayList aRoute = new ArrayList<>();
        for (int i = 0; i < route_names.length; i++) {
            Route route = new Route();
            route.setName(route_names[i]);
            route.setGrade(zone_image_resource[i]);
            aRoute.add(route);
        }
        return aRoute;
    }

    private void initViews(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.card_route_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        ArrayList routes = prepareData();
        RouteDataAdapter adapter = new RouteDataAdapter(routes, getActivity() );
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
