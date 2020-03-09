package es.kleiren.madclimb.extra_activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Datum;
import es.kleiren.madclimb.data_classes.Route;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.util.InfoChartUtils;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

public class InfoFragment extends Fragment {

    private String type;
    private Datum datum;

    private static final String ARG_TYPE = "type";
    private static final String ARG_DATUM = "datum";
    private ArrayList<Route> routes;

    @BindView(R.id.infoFrag_gradeChart)
    ColumnChartView columnChartView;
    @BindView(R.id.infoFrag_latLon)
    TextView textViewLatLon;
    @BindView(R.id.infoFrag_txtDate)
    TextView textViewDate;
    @BindView(R.id.infoFrag_layoutDate)
    View layoutDate;
    @BindView(R.id.infoFrag_btnEightADotNu)
    View btnEightADotNu;
    @BindView(R.id.infoFrag_txtInfo)
    TextView txtInfo;
    @BindView(R.id.infoFrag_layoutDescription)
    View layoutDescription;

    public static InfoFragment newInstance(String type, Datum datum) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putSerializable(ARG_DATUM, datum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
            datum = (Datum) getArguments().getSerializable(ARG_DATUM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        ButterKnife.bind(this, view);

        getFragmentManager().beginTransaction()
                .replace(R.id.infoFrag_mapContainer, MapsFragment.newInstance(datum.getLoc(), datum.getName()))
                .commit();

        layoutDate.setVisibility(View.GONE);
        btnEightADotNu.setVisibility(View.GONE);
        layoutDescription.setVisibility(View.GONE);
        columnChartView.setVisibility(View.GONE);

        textViewLatLon.setText(datum.getLoc());

        if (!(datum).getDescription().isEmpty()) {
            layoutDescription.setVisibility(View.VISIBLE);
            txtInfo.setText((datum).getDescription());
        }

        if (type.equals("zone")) {
            if (!(datum).getEightADotNu().isEmpty()) {
                btnEightADotNu.setVisibility(View.VISIBLE);
            }
            view.findViewById(R.id.infoFrag_infoLayout).setVisibility(View.VISIBLE);
        }
        if (type.equals("sector")) {
            if (!((Sector) datum).getDate().isEmpty()) {
                layoutDate.setVisibility(View.VISIBLE);
                textViewDate.setText(((Sector) datum).getDate());
            }
            prepareData();
            columnChartView.setVisibility(View.VISIBLE);
            view.findViewById(R.id.infoFrag_infoLayout).setVisibility(View.VISIBLE);
        }

        btnEightADotNu.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((datum).getEightADotNu()));
            startActivity(browserIntent);
        });
        return view;
    }

    private void prepareData() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        routes = new ArrayList<>();
        DatabaseReference child;
        if (((Sector) datum).getParentSector() != null)
            child = mDatabase.child("zones/" + ((Sector) datum).getZone_id() + "/sectors/" + ((Sector) datum).getParentSector() + "/sub_sectors/" + datum.getId() + "/routes");
        else
            child = mDatabase.child("zones/" + ((Sector) datum).getZone_id() + "/sectors/" + datum.getId() + "/routes");
        child.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Route route = postSnapshot.getValue(Route.class);
                    routes.add(route);
                }
                generateData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
            }
        });
    }

    private void generateData() {
        Integer[] gradesFiltered = new Integer[]{0, 0, 0, 0};

        for (Route route : routes) {
            try {
                gradesFiltered[InfoChartUtils.map.get(route.getGrade()) - 1]++;
            } catch (Exception e) {
            }
        }

        int numSubColumns = 1;
        int numColumns = 4;
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; ++i) {
            values = new ArrayList<>();
            for (int j = 0; j < numSubColumns; ++j) {
                SubcolumnValue temp = new SubcolumnValue(gradesFiltered[i], InfoChartUtils.colors[i]);
                values.add(temp);
            }
            Column column = new Column(values);
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
        }

        ColumnChartData data = new ColumnChartData(columns);

        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i < InfoChartUtils.labels.length; i++) {
            axisValues.add(new AxisValue(i, InfoChartUtils.labels[i].toCharArray()));
        }
        Axis axisX = new Axis(axisValues);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(null);

        columnChartView.setColumnChartData(data);

    }
}
