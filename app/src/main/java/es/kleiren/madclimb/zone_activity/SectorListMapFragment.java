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
import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.root.GlideApp;
import es.kleiren.madclimb.sector_activity.SectorActivity;
import es.kleiren.madclimb.sector_activity.SectorIndexActivity;

import static android.content.Context.MODE_PRIVATE;
import static es.kleiren.madclimb.util.IconUtils.getBitmapDescriptor;


public class SectorListMapFragment extends Fragment implements OnMapReadyCallback, LocationSource.OnLocationChangedListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private SectorDataAdapter adapter;
    private ArrayList<Sector> sectorsFromFirebase = new ArrayList<>();
    private Activity parentActivity;
    private Zone zone;
    private static final String ARG_ZONE = "zone";
    private ObservableSectorList observableSectorList;
    private OnTouchListener mListener;
    MapView mMapView;
    private GoogleMap mMap;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<Sector> sectors;
    private boolean hasParkings = false;
    private String[] parkings;
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
        if (!parentActivity.getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("locAsked", false)) {
            if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(parentActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                parentActivity.getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("locAllowed", true).apply();
            }
            parentActivity.getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("locAsked", true).apply();
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
        ((ZoneActivity) getActivity()).disableScroll();

        ImageView transparentImageView = (ImageView) view.findViewById(R.id.transparent_image);

        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mListener.onTouch();
                        return false;
                    case MotionEvent.ACTION_UP:
                        mListener.onTouch();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        return false;
                    default:
                        return true;
                }
            }
        });

        view.findViewById(R.id.changeMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                else
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

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
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                            markers.add(mMap.addMarker(new MarkerOptions().position(loc).title(sector.getName()).icon(getBitmapDescriptor(parentActivity, R.drawable.map_marker_colored))));
                        else
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
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setOnInfoWindowClickListener(this);

        parkings = zone.getParkings();
        if (parkings != null) {
            hasParkings = !parkings[0].isEmpty();
            if (hasParkings) {
                for (String parking : parkings) {
                    String[] latlon = parking.split(",");
                    LatLng parkingLocation = new LatLng(Double.parseDouble(latlon[0]), Double.parseDouble(latlon[1]));
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                            markers.add(mMap.addMarker(new MarkerOptions().position(parkingLocation).title("Parking").icon(getBitmapDescriptor(parentActivity, R.drawable.ic_parking_marker))));
                        else
                            markers.add(mMap.addMarker(new MarkerOptions().position(parkingLocation).title("Parking")));
                    } catch (Exception e) {
                    }
                }
            }
        }

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {

                View v = getLayoutInflater().inflate(R.layout.maps_info_window, null);

                ((TextView) v.findViewById(R.id.mapsInfo_txtName)).setText(marker.getTitle());

                if (hasParkings && markers.indexOf(marker) < parkings.length) return null;
                GlideApp.with(getContext())
                        .load(FirebaseStorage.getInstance().getReference().child(sectorsFromFirebase.get(hasParkings ? markers.indexOf(marker) - parkings.length : markers.indexOf(marker)).getImg()))
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
        if (markers.isEmpty()) return;
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
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
        if (hasParkings && markers.indexOf(marker) < parkings.length) return;
        Intent intent = new Intent(getActivity(), SectorIndexActivity.class);
        intent.putExtra("zone", zone);
        intent.putExtra("sector", sectorsFromFirebase.get(hasParkings ? markers.indexOf(marker) - parkings.length : markers.indexOf(marker)));
        intent.putExtra("sectors", sectorsFromFirebase);
        intent.putExtra("currentSectorPosition", hasParkings ? markers.indexOf(marker) - parkings.length : markers.indexOf(marker));
        startActivity(intent);
    }

    public void setListener(OnTouchListener listener) {
        mListener = listener;
    }

    public interface OnTouchListener {
        public abstract void onTouch();
    }

    public class TouchableWrapper extends FrameLayout {

        public TouchableWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mListener.onTouch();
                    break;
                case MotionEvent.ACTION_UP:
                    mListener.onTouch();
                    break;
            }
            return super.dispatchTouchEvent(event);
        }
    }
}
