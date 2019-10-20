package es.kleiren.madclimb.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.extra_activities.ImageViewerActivity;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Route;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.root.GlideApp;
import es.kleiren.madclimb.sector_activity.RouteDataAdapter;

import static android.content.Context.MODE_PRIVATE;


public class HistoryFragment extends Fragment {


    private Activity parentActivity;

    @BindView(R.id.card_route_view)
    RecyclerView recyclerRoute;
    private RouteDataAdapter adapter;
    private ArrayList<Route> routesFromFirebase;

    public HistoryFragment() {
    }

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    private static final String ARG_SECTOR = "sector";

    public static HistoryFragment newInstance(Sector sector) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View routeView = inflater.inflate(R.layout.fragment_history, container, false);

        ButterKnife.bind(this, routeView);

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        (new LoadData()).execute();


        return routeView;
    }

    private void prepareData() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference child;
        routesFromFirebase = new ArrayList<>();

        Gson gson = new Gson();
        String json = parentActivity.getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("DONE_ROUTES", "");
        ArrayList<Route> arRoutes = new ArrayList<>();
        if (! json.isEmpty()) {
            Route[] obj = gson.fromJson(json, Route[].class);
            arRoutes = new ArrayList<>(Arrays.asList(obj));

            for (Route route : arRoutes) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl(route.getRef());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Route route = dataSnapshot.getValue(Route.class);
                        routesFromFirebase.add(route);
                        adapter = new RouteDataAdapter(routesFromFirebase, getActivity(), null);
                        initViews();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
                    }
                });
            }
        }
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
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
