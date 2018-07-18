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

package es.kleiren.madclimb.extra_activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Datum;
import es.kleiren.madclimb.data_classes.Route;
import es.kleiren.madclimb.data_classes.Sector;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class InfoFragment extends Fragment {
    String type;
    Datum datum;

    private static final String ARG_TYPE = "type";
    private static final String ARG_DATUM = "datum";
    private ArrayList<Route> routes;

    Map<String, Integer> map = new HashMap<String, Integer>() {{
        put("3", 1);
        put("3+", 1);
        put("IV", 1);
        put("IV+", 1);
        put("V", 1);
        put("V+", 1);
        put("6a", 2);
        put("6a+", 2);
        put("6b", 2);
        put("6b+", 2);
        put("6c", 2);
        put("6c+", 2);
        put("7a", 3);
        put("7a+", 3);
        put("7b", 3);
        put("7b+", 2);
        put("7c", 3);
        put("7c+", 3);
    }};

    @BindView(R.id.infoFrag_gradeChart)
    ColumnChartView columnChartView;
    @BindView(R.id.infoFrag_latLon)
    TextView textViewLatLon;
    @BindView(R.id.infoFrag_txtDate)
    TextView textViewDate;
    @BindView(R.id.infoFrag_layoutDate)
    View layoutDate;
    @BindView(R.id.infoFrag_txtInfo)
    TextView txtInfo;


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

        txtInfo.setText(datum.getDescription());
        layoutDate.setVisibility(View.GONE);

        if (type.equals("sector"))
            try {
                if (!((Sector) datum).getDate().isEmpty()) {
                    layoutDate.setVisibility(View.VISIBLE);
                    textViewDate.setText(((Sector) datum).getDate());
                }
            } catch (Exception e) {
            }

        textViewLatLon.setText(datum.getLoc());

        if (type.equals("sector")) {
            prepareData();
            view.findViewById(R.id.infoFrag_chartLayout).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.infoFrag_chartLayout).setVisibility(View.GONE);
        }

        return view;
    }

    private void prepareData() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        routes = new ArrayList<>();

        mDatabase.child("zones/" + ((Sector) datum).getZone_id() + "/sectors/" + datum.getId() + "/routes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("FIREBASE", dataSnapshot.getValue().toString());

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
        Integer[] colors = new Integer[]{ChartUtils.COLOR_GREEN, ChartUtils.COLOR_BLUE, ChartUtils.COLOR_VIOLET, ChartUtils.COLOR_RED};
        String[] labels = new String[]{"III - V+", "6a - 6c+", "7a - 7c+", "8a - 9c+"};

        for (Route route : routes) {
            try {
                gradesFiltered[map.get(route.getGrade()) - 1]++;
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
                SubcolumnValue temp = new SubcolumnValue(gradesFiltered[i], colors[i]);
                values.add(temp);
            }

            Column column = new Column(values);
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
        }

        ColumnChartData data = new ColumnChartData(columns);

        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {

            axisValues.add(new AxisValue(i, labels[i].toCharArray()));
        }
        Axis axisX = new Axis(axisValues);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(null);

        columnChartView.setColumnChartData(data);

    }
}
