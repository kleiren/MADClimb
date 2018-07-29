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

package es.kleiren.madclimb.main;

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
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.root.GlideApp;

import es.kleiren.madclimb.zone_activity.ZoneActivity;

public class ZoneListMapFragment extends Fragment implements OnMapReadyCallback, LocationSource.OnLocationChangedListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private ZoneDataAdapter adapter;
    private Activity parentActivity;
    private Zone zone;
    private ArrayList<Zone> zonesFromFirebase = new ArrayList<>();
    private ObservableZoneList observableZoneList;
    private ArrayList<Zone> zones;
    private ArrayList<Zone> zoneList = new ArrayList<>();
    MapView mMapView;
    private GoogleMap mMap;

    private ArrayList<Marker> markers = new ArrayList<>();

    private Observer zoneListChanged = new Observer() {
        @Override
        public void update(Observable o, Object newValue) {

            zoneList = (ArrayList<Zone>) newValue;
            adapter = new ZoneDataAdapter(zoneList, getActivity());

        }
    };

    public ZoneListMapFragment() {
    }

    public static ZoneListMapFragment newInstance() {
        return new ZoneListMapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();

        if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        view.findViewById(R.id.openMaps).setVisibility(View.GONE);
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

        mDatabase.child("zones").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Zone zone = postSnapshot.getValue(Zone.class);
                    zonesFromFirebase.add(zone);
                    String[] latlon = zone.getLoc().split(",");
                    LatLng loc = new LatLng(Double.parseDouble(latlon[0]), Double.parseDouble(latlon[1]));
                    try {
                        markers.add(mMap.addMarker(new MarkerOptions().position(loc).title(zone.getName())));
                    } catch (Exception e) {
                    }
                }

                observableZoneList = new ObservableZoneList();
                observableZoneList.getZonesFromFirebaseZoneList(zonesFromFirebase, getActivity());
                observableZoneList.addObserver(zoneListChanged);

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
            public View getInfoContents(final Marker marker) {

                View v = getLayoutInflater().inflate(R.layout.maps_info_window, null);

                ((TextView) v.findViewById(R.id.mapsInfo_txtName)).setText(marker.getTitle());

                GlideApp.with(getContext())
                        .load(FirebaseStorage.getInstance().getReference().child(zonesFromFirebase.get(markers.indexOf(marker)).getImg()))
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
                                    marker.showInfoWindow();
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
        int padding = 200;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);
        mMap.animateCamera(cu);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        zone = adapter.getZone(markers.indexOf(marker));
        Intent intent = new Intent(getActivity(), ZoneActivity.class);
        intent.putExtra("zone", zone);
        startActivityForResult(intent, 1);
    }
}
