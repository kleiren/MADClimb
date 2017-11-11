package es.kleiren.leviathan;

/**
 * Created by Carlos on 11/05/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class RouteDataAdapter extends RecyclerView.Adapter<RouteDataAdapter.ViewHolder> {
    private ArrayList<Route> routes;
    private Sector sector;
    private Context context;
    private int mExpandedPosition = -1;
    private ColumnChartData data;

    public RouteDataAdapter(ArrayList<Route> routes, Context context, Sector sector) {
        this.context = context;
        this.routes = routes;
        this.sector = sector;
    }

    @Override
    public RouteDataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        if (i == 1) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.info_row, viewGroup, false);
            return new ViewHolder(view, 0);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.route_row, viewGroup, false);
            return new ViewHolder(view, 1);
        }
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
                    intent.putExtra("title", sector.getName());
                    context.startActivity(intent);
                }
            });
        } else {

            viewHolder.txtName.setText(routes.get(i).getName());
            viewHolder.txtGrade.setText(routes.get(i).getGrade());
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
    }


    private void generateData() {
        int numSubcolumns = 1;
        int numColumns = 8;
        // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; ++i) {

            values = new ArrayList<SubcolumnValue>();
            for (int j = 0; j < numSubcolumns; ++j) {
                values.add(new SubcolumnValue((float) Math.random() * 50f + 5, ChartUtils.pickColor()));
            }

            Column column = new Column(values);
            column.setHasLabels(true);
            //column.setHasLabelsOnlyForSelected(hasLabelForSelected);
            columns.add(column);
        }

        data = new ColumnChartData(columns);

        data.setAxisXBottom(null);
        data.setAxisYLeft(null);

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 2;
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtGrade, txtDetails;
        View details, idle;
        ViewGroup recyclerView;
        ColumnChartView chart;
        ImageButton btnInfo;


        ViewHolder(View view, int type) {
            super(view);

            if (type == 0) {

                chart = (ColumnChartView) view.findViewById(R.id.gradeChart);
                chart.setZoomEnabled(false);
                btnInfo = (ImageButton) view.findViewById(R.id.btnInfo);

            } else {
                recyclerView = (ViewGroup) view.findViewById(R.id.card);
                details = (View) view.findViewById(R.id.details);

                txtDetails = (TextView) view.findViewById(R.id.txtDetails);

                details.setVisibility(View.GONE);
                txtName = (TextView) view.findViewById(R.id.textRouteName);
                txtGrade = (TextView) view.findViewById(R.id.textRouteGrade);
            }
        }
    }
}