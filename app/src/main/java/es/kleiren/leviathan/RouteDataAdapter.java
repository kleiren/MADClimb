package es.kleiren.leviathan;

/**
 * Created by Carlos on 11/05/2017.
 */

import android.content.Context;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

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

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.route_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RouteDataAdapter.ViewHolder viewHolder, final int i) {

        viewHolder.txtName.setText(routes.get(i).getName());
        viewHolder.txtGrade.setText(routes.get(i).getGrade());

        final boolean isExpanded = i==mExpandedPosition;
        viewHolder.details.setVisibility(isExpanded?View.VISIBLE:View.GONE);

        viewHolder.itemView.setActivated(isExpanded);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1:i;
               // viewHolder.recyclerView.animate();
                TransitionManager.beginDelayedTransition(viewHolder.recyclerView);
                notifyDataSetChanged();
            }
        });


    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtName, txtGrade;
        public View details, idle;
        public ViewGroup recyclerView;

        public ViewHolder(View view) {
            super(view);

            recyclerView = (ViewGroup) view.findViewById(R.id.card);
            details = (View) view.findViewById(R.id.details);

            details.setVisibility(View.GONE);
            txtName = (TextView)view.findViewById(R.id.textRouteName);
            txtGrade = (TextView)view.findViewById(R.id.textRouteGrade);
        }
    }

}