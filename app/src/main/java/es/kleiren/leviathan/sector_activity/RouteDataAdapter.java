package es.kleiren.leviathan.sector_activity;

/**
 * Created by Carlos on 11/05/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.leviathan.extra_activities.InfoActivity;
import es.kleiren.leviathan.R;
import es.kleiren.leviathan.data_classes.Route;
import es.kleiren.leviathan.data_classes.Sector;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class RouteDataAdapter extends RecyclerView.Adapter<RouteDataAdapter.ViewHolder> {
    private ArrayList<Route> routes;
    private Sector sector;
    private Context context;
    private int mExpandedPosition = -1;
    private ColumnChartData data;
    private ArrayList<String> grades;

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
    private Integer[] gradesFiltered = new Integer[]{0, 0, 0, 0};
    private Integer[] colors;
    private String[] labels;

    public RouteDataAdapter(ArrayList<Route> routes, Context context, Sector sector) {
        this.context = context;
        this.routes = routes;
        this.sector = sector;
    }

    @Override
    public RouteDataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.route_row, viewGroup, false);
        if (i == 1)
            return new ViewHolder(view, 0);
        else
            return new ViewHolder(view, 1);

    }

    @Override
    public void onBindViewHolder(final RouteDataAdapter.ViewHolder viewHolder, final int i) {
        if (i == 0) {

            generateData();
            viewHolder.chart.setColumnChartData(data);
            viewHolder.btnInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, InfoActivity.class);
                    intent.putExtra("type", "sector");
                    intent.putExtra("datum", sector);
                    context.startActivity(intent);
                }
            });
        }

        viewHolder.txtName.setText(routes.get(i).getName());
        viewHolder.txtGrade.setText(routes.get(i).getGrade());
viewHolder.txtGrade.setTextColor(colors[map.get(routes.get(i).getGrade())-1]);





        viewHolder.txtDetails.setText(routes.get(i).getDescription());

        final boolean isExpanded = i == mExpandedPosition;
        viewHolder.details.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        viewHolder.itemView.setActivated(isExpanded);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1 : i;
                // viewHolder.recyclerView.animate();
                TransitionManager.beginDelayedTransition(viewHolder.recyclerView);
                notifyDataSetChanged();
            }
        });


    }


    private void generateData() {
        grades = new ArrayList<>();
        gradesFiltered = new Integer[]{0, 0, 0, 0};

        colors = new Integer[]{ChartUtils.COLOR_GREEN, ChartUtils.COLOR_BLUE, ChartUtils.COLOR_VIOLET, ChartUtils.COLOR_RED};
        labels = new String[]{"III - V+", "6a - 6c+", "7a - 7c+", "8a - 9c+"};
        gradesFiltered = new Integer[]{0, 0, 0, 0};

        for (Route route : routes) {
            grades.add(route.getGrade());
            gradesFiltered[map.get(route.getGrade())-1]++;

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

        data = new ColumnChartData(columns);

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int i =0; i< labels.length; i++) {

            axisValues.add(new AxisValue(i, labels[i].toCharArray()));
        }
        Axis axisX = new Axis(axisValues);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(null);

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 0;
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textRouteName)
        TextView txtName;
        @BindView(R.id.textRouteGrade)
        TextView txtGrade;
        @BindView(R.id.txtDetails)
        TextView txtDetails;
        @BindView(R.id.details)
        View details;
        @BindView(R.id.card)
        ViewGroup recyclerView;
        @BindView(R.id.gradeChart)
        ColumnChartView chart;
        @BindView(R.id.btnInfo)
        ImageButton btnInfo;
        @BindView(R.id.infoLayout)
        ConstraintLayout infoLayout;


        ViewHolder(View view, int type) {
            super(view);
            ButterKnife.bind(this, view);

            if (type == 0) {
                chart.setZoomEnabled(false);
            } else {
                infoLayout.setVisibility(View.GONE);
            }
            details.setVisibility(View.GONE);

        }
    }
}