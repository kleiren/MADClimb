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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ZoneDataAdapter extends RecyclerView.Adapter<ZoneDataAdapter.ViewHolder> {
    private ArrayList<Zone> zones;
    private Context context;


    public ZoneDataAdapter(ArrayList<Zone> zones, Context context) {
        this.context = context;
        this.zones = zones;
    }

    @Override
    public ZoneDataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zone_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ZoneDataAdapter.ViewHolder viewHolder, int i) {

        viewHolder.txt_name.setText(zones.get(i).getName());
        Picasso.with(context).load(zones.get(i).getResource()).into(viewHolder.img);

    }

    @Override
    public int getItemCount() {
        return zones.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txt_name;
        private ImageView img;

        public ViewHolder(View view) {
            super(view);

            txt_name = (TextView)view.findViewById(R.id.textRouteName);
            img = (ImageView) view.findViewById(R.id.img_android);
        }
    }

}