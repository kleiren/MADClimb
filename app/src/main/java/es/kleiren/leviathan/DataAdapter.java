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

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
    private ArrayList<Zone> zones;
    private Context context;


    public DataAdapter(ArrayList<Zone> zones, Context context) {
        this.context = context;
        this.zones = zones;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, int i) {

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

            txt_name = (TextView)view.findViewById(R.id.tv_country);
            img = (ImageView) view.findViewById(R.id.img_android);
        }
    }

}