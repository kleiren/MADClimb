package es.kleiren.madclimb.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;

import androidx.fragment.app.Fragment;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.common.data.DataBufferObserver;
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
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.extra_activities.ImageViewerActivity;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Route;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.root.GlideApp;
import es.kleiren.madclimb.sector_activity.RouteDataAdapter;
import es.kleiren.madclimb.zone_activity.SectorDataAdapter;

import static android.content.Context.MODE_PRIVATE;


public class HistoryFragment extends Fragment {


    private Activity parentActivity;

    @BindView(R.id.card_route_view)
    RecyclerView recyclerRoute;
    @BindView(R.id.history_emptyView)
    View emptyView;
    public ObservableArrayList observableArrayList;
    private RouteDataAdapter adapter;
    private ArrayList<Route> routesFromFirebase;

    public HistoryFragment() {
    }

    private Observer doneRoutesChanged = new Observer() {
        @Override
        public void update(Observable o, Object newValue) {
            prepareData();
            initViews();
        }
    };

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
        View routeView = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, routeView);
        prepareData();
        initViews();
        return routeView;
    }

    private void prepareData() {
        routesFromFirebase = new ArrayList<>();
        Gson gson = new Gson();
        String json = parentActivity.getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("DONE_ROUTES", "");

        if (!json.isEmpty()) {
            Route[] obj = gson.fromJson(json, Route[].class);
            observableArrayList = new ObservableArrayList();
            observableArrayList.arRoutes = new ArrayList<Route>(Arrays.asList(obj));

            ArrayList<Route> arRoutesCopy = observableArrayList.arRoutes;
            for (Route routeDone :  arRoutesCopy ) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl(routeDone.getRef());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Route route = dataSnapshot.getValue(Route.class);
                        route.setDoneDate(routeDone.getDoneDate());
                        route.setSectorName(routeDone.getSectorName());
                        route.setZoneName(routeDone.getZoneName());
                        route.setDoneAttempt(routeDone.getDoneAttempt());
                        route.setReference(routeDone.getRef());
                        routesFromFirebase.add(route);
                        if (adapter != null)
                            adapter.notifyDataSetChanged();
                        if(routesFromFirebase.isEmpty())
                            emptyView.setVisibility(View.VISIBLE);
                        else
                            emptyView.setVisibility(View.GONE);

                        observableArrayList.addObserver(doneRoutesChanged);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
                    }
                });
            }
        }
    }
    private int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return 0;
    }

    private void initViews() {
        recyclerRoute.addItemDecoration( new VerticalSpaceItemDecoration(getActionBarHeight() + 40));
        recyclerRoute.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new RouteDataAdapter(routesFromFirebase, getActivity(), null, true, this);
        recyclerRoute.setAdapter(adapter);
    }


    static class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpaceHeight;

        VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = verticalSpaceHeight;
            }
        }
    }

    public class ObservableArrayList extends Observable {

        public ArrayList<Route> getArRoutes() {
            return arRoutes;
        }

        public void setArRoutes(ArrayList<Route> arRoutes) {
            this.arRoutes = arRoutes;
            this.setChanged();
            this.notifyObservers(arRoutes);
        }

        ArrayList<Route> arRoutes;

    }
}
