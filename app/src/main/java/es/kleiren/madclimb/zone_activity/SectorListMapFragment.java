/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.kleiren.madclimb.zone_activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.root.GlideApp;
import es.kleiren.madclimb.sector_activity.SectorActivity;

public class SectorListMapFragment extends Fragment implements OnMapReadyCallback, LocationSource.OnLocationChangedListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {


    private SectorDataAdapter adapter;
    private ArrayList<Sector> sectorsFromFirebase = new ArrayList<>();
    private Activity parentActivity;
    private Zone zone;
    private static final String ARG_ZONE = "zone";
    private ObservableSectorList observableSectorList;

    MapView mMapView;
    private GoogleMap mMap;

    private ArrayList<Marker> markers = new ArrayList<>();


    private ArrayList<Sector> sectors;
    private Observer sectorListChanged = new Observer() {
        @Override
        public void update(Observable o, Object newValue) {

            sectors = (ArrayList<Sector>) newValue;
            adapter = new SectorDataAdapter(sectors, getActivity());
            adapter.notifyDataSetChanged();

        }
    };

    public static SectorListMapFragment newInstance(Zone zone) {
        SectorListMapFragment fragment = new SectorListMapFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ZONE, zone);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();

        if (getArguments() != null) {
            zone = (Zone) getArguments().getSerializable(ARG_ZONE);
        }

        if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        final ObservableScrollView scrollView = view.findViewById(R.id.mapFrag_scroll);
        final Activity parentActivity = getActivity();
        scrollView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.container));

        view.findViewById(R.id.openMaps).setVisibility(View.GONE);
        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            scrollView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);
        prepareData();


        return view;
    }


    private void prepareData() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("zones/" + zone.getId() + "/sectors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Sector sector = postSnapshot.getValue(Sector.class);
                    sectorsFromFirebase.add(sector);
                    String[] latlon = sector.getLoc().split(",");
                    LatLng loc = new LatLng(Double.parseDouble(latlon[0]), Double.parseDouble(latlon[1]));
                    try {
                        markers.add(mMap.addMarker(new MarkerOptions().position(loc).title(sector.getName())));
                    } catch (Exception e) {
                    }
                }

                observableSectorList = new ObservableSectorList();
                observableSectorList.addObserver(sectorListChanged);

                centerMapOnMarkers();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mMap.setMyLocationEnabled(true);


        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker arg0) {

                View v = getLayoutInflater().inflate(R.layout.maps_info_window, null);

                ((TextView) v.findViewById(R.id.mapsInfo_txtName)).setText(arg0.getTitle());

                GlideApp.with(getContext())
                        .load(FirebaseStorage.getInstance().getReference().child(sectorsFromFirebase.get(markers.indexOf(arg0)).getImg()))
                        .placeholder(R.drawable.mountain_placeholder_small)
                        .override(400, 200)
                        .centerCrop()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                if (!dataSource.equals(DataSource.MEMORY_CACHE))
                                    arg0.showInfoWindow();
                                return false;
                            }
                        })
                        .into((ImageView) v.findViewById(R.id.imageView3));

                return v;

            }
        });

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

    @Override
    public void onLocationChanged(Location location) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(location.getLatitude(),
                location.getLongitude()));

        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        markers.add(mMap.addMarker(markerOptions));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location
                        .getLongitude()), 20));

    }

    private void centerMapOnMarkers() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }

        if (markers.isEmpty()) return;

        LatLngBounds bounds = builder.build();

        int padding = 500;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        try {
            mMap.moveCamera(cu);
            mMap.animateCamera(cu);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        Intent intent = new Intent(getActivity(), SectorActivity.class);
        intent.putExtra("zone", zone);
        intent.putExtra("sectors", sectorsFromFirebase);
        intent.putExtra("currentSectorPosition", markers.indexOf(marker));
        startActivity(intent);

    }
}
