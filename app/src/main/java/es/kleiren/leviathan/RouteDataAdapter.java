package es.kleiren.leviathan;

/**
 * Created by Carlos on 11/05/2017.
 */

import android.content.Context;
import android.graphics.Color;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.ColumnChartView;

public class RouteDataAdapter extends RecyclerView.Adapter<RouteDataAdapter.ViewHolder> {
    private ArrayList<Route> routes;
    private Context context;
    private int mExpandedPosition = -1;


    public RouteDataAdapter(ArrayList<Route> routes, Context context) {
        this.context = context;
        this.routes = routes;
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

            List<PointValue> values = new ArrayList<PointValue>();
            values.add(new PointValue(0, 2));
            values.add(new PointValue(1, 4));
            values.add(new PointValue(2, 3));
            values.add(new PointValue(3, 4));

            ColumnChartData data = ColumnChartData.generateDummyData();

            viewHolder.chart.setColumnChartData(data);




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


        ViewHolder(View view, int type) {
            super(view);

            if (type == 0){

                chart = (ColumnChartView) view.findViewById(R.id.gradeChart);

            }else {
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