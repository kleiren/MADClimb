package es.kleiren.madclimb.sector_activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.extra_activities.ImageViewerActivity;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Route;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.root.GlideApp;

import static android.content.Context.MODE_PRIVATE;


public class RouteListFragment extends Fragment {


    private ArrayList<Route> routesFromFirebase;
    private RouteDataAdapter adapter;
    private Activity parentActivity;
    private Sector sector;

    @BindView(R.id.card_route_view)
    RecyclerView recyclerRoute;
    @BindView(R.id.route_imgCroquis)
    ImageView imgCroquis;
    @BindView(R.id.route_cardViewCroquis)
    View cardViewCroquis;
    @BindView(R.id.route_initial_progress)
    ProgressBar initialProgress;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View routeView = inflater.inflate(R.layout.fragment_route_list, container, false);

        ButterKnife.bind(this, routeView);

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        final StorageReference load = mStorageRef.child(sector.getImg());

        GlideApp.with(parentActivity)
                .load(load)
                .placeholder(R.drawable.mountain_placeholder)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        cardViewCroquis.getLayoutParams().height = resource.getIntrinsicHeight() + 50 ;
                        return false;
                    }
                })
                .into(imgCroquis);

        imgCroquis.setOnClickListener(v -> {
            Intent intent = new Intent(parentActivity, ImageViewerActivity.class);
            intent.putExtra("image", sector.getImg());
            intent.putExtra("title", sector.getName());
            startActivityForResult(intent, 1);
        });
        cardViewCroquis.setOnTouchListener((v, event) -> {
            int y = (int) event.getY() + 15;
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (y < 400)
                    cardViewCroquis.getLayoutParams().height = 400;
                else if (y > 1400)
                    cardViewCroquis.getLayoutParams().height = 1400;
                else
                    cardViewCroquis.getLayoutParams().height = y;
                cardViewCroquis.requestLayout();
            }
            return true;
        });
        routesFromFirebase = new ArrayList<>();

        (new LoadData()).execute();

        return routeView;
    }

    private void prepareData() {

        Gson gson = new Gson();
        String json = parentActivity.getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("ROUTES_DONE", "");
        ArrayList<Route> arRoutes = new ArrayList<>();
        HashMap<String, Route> hmRoutes = new HashMap<>();
        if (!json.isEmpty()) {
            Type type = new TypeToken<HashMap<String, Route>>() {}.getType();
            hmRoutes = gson.fromJson(json, type);
        }

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference child;
        if (sector.getParentSector() != null)
            child = mDatabase.child("zones/" + sector.getZone_id() + "/sectors/" + sector.getParentSector() + "/sub_sectors/" + sector.getId() + "/routes");
        else
            child = mDatabase.child("zones/" + sector.getZone_id() + "/sectors/" + sector.getId() + "/routes");
        HashMap<String, Route> finalHmRoutes = hmRoutes;
        child.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Route route = postSnapshot.getValue(Route.class);
                    String ref = postSnapshot.getRef().toString();
                    route.setReference(ref);
                    if (finalHmRoutes.containsKey(ref))
                            route = finalHmRoutes.get(ref);
                    routesFromFirebase.add(route);
                    adapter = new RouteDataAdapter(routesFromFirebase, getActivity(), sector, false, null);
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
        recyclerRoute.setAdapter(adapter);
    }

    private class LoadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            prepareData();
            return "Executed";
        }

        @Override
        protected void onPreExecute() {
            initialProgress.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            initialProgress.setVisibility(View.GONE);
            super.onPostExecute(s);
        }
    }
}
