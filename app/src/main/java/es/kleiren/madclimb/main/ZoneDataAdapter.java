package es.kleiren.madclimb.main;

/**
 * Created by Carlos on 11/05/2017.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.root.GlideApp;

public class ZoneDataAdapter extends RecyclerView.Adapter<ZoneDataAdapter.ViewHolder> implements Filterable {
    private ArrayList<Zone> zones;
    private ArrayList<Zone> filteredZones;
    private Context context;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;

    public ZoneDataAdapter(ArrayList<Zone> zones, Context context) {
        this.context = context;
        this.zones = zones;
        this.filteredZones = zones;
    }

    @Override
    public ZoneDataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);


        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zone_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ZoneDataAdapter.ViewHolder viewHolder, int i) {

        viewHolder.txt_name.setText(filteredZones.get(i).getName());

        mDatabase.child("zones/" + filteredZones.get(i).getId() + "/sectors").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) viewHolder.imgNoSectors.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        StorageReference load = mStorageRef.child(filteredZones.get(i).getImg());

        GlideApp.with(context)
                .load(load).centerCrop()
                .placeholder(R.drawable.mountain_placeholder)
                .into(viewHolder.img);

    }

    @Override
    public int getItemCount() {
        return filteredZones.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {

                    filteredZones = zones;
                } else {

                    ArrayList<Zone> tempList = new ArrayList<>();

                    for (Zone zone : zones) {

                        if (zone.getName().toLowerCase().contains(charString.toLowerCase())) {

                            tempList.add(zone);
                        }
                    }

                    filteredZones = tempList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredZones;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredZones = (ArrayList<Zone>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_name;
        private ImageView img;
        private ImageView imgNoSectors;

        public ViewHolder(View view) {
            super(view);

            txt_name = view.findViewById(R.id.textRouteName);
            img = view.findViewById(R.id.img_zone);
            imgNoSectors = view.findViewById(R.id.img_noSectors);
        }
    }

}