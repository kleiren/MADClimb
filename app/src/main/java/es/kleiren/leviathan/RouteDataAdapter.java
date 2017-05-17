package es.kleiren.leviathan;

/**
 * Created by Carlos on 11/05/2017.
 */

import android.content.Context;
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
    public void onBindViewHolder(RouteDataAdapter.ViewHolder viewHolder, int i) {

        viewHolder.txtName.setText(routes.get(i).getName());
        viewHolder.txtGrade.setText(Integer.toString(routes.get(i).getGrade()));


    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtName, txtGrade;

        public ViewHolder(View view) {
            super(view);

            txtName = (TextView)view.findViewById(R.id.textRouteName);
            txtGrade = (TextView)view.findViewById(R.id.textRouteGrade);
        }
    }

}