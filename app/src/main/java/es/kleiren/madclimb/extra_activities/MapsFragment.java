package es.kleiren.madclimb.extra_activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import es.kleiren.madclimb.R;

import static es.kleiren.madclimb.util.IconUtils.getBitmapDescriptor;

public class MapsFragment extends Fragment {


    MapView mMapView;
    private GoogleMap mMap;
    String location, name;

    private static final String ARG_LOC = "location";
    private static final String ARG_NAME = "name";


    public static MapsFragment newInstance(String location, String name) {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOC, location);
        args.putString(ARG_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            location = getArguments().getString(ARG_LOC);
            name = getArguments().getString(ARG_NAME);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        final String[] latlon = location.split(",");
        final LatLng loc = new LatLng(Double.parseDouble(latlon[0]), Double.parseDouble(latlon[1]));

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(googleMap -> {
            mMap = googleMap;
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.maps_style));
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    mMap.addMarker(new MarkerOptions().position(loc).icon(getBitmapDescriptor(getActivity(), R.drawable.map_marker_colored)));
                else
                    mMap.addMarker(new MarkerOptions().position(loc));
            } catch (Exception e) {
            }
            CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });

        view.findViewById(R.id.openMaps).setOnClickListener(v -> {
            Uri uri = Uri.parse("geo:0,0?q=" + loc.latitude + "," + loc.longitude + " (" + name + ")");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        view.findViewById(R.id.changeMode).setOnClickListener(v -> {
            if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL)
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            else
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
