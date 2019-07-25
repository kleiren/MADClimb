package es.kleiren.madclimb.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.root.GlideApp;

public class ZoneDataAdapter extends RecyclerView.Adapter<ZoneDataAdapter.ViewHolder> implements Filterable {
    private ArrayList<Zone> zones;
    private ArrayList<Zone> filteredZones;
    private Context context;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;


    ZoneDataAdapter(ArrayList<Zone> zones, Context context) {
        this.context = context;
        this.zones = zones;
        this.filteredZones = zones;
    }

    public Zone getZone(int i) {
        return filteredZones.get(i);
    }

    public ArrayList<Zone> getFilteredZones() {
        return filteredZones;
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

        viewHolder.txtRouteName.setText(filteredZones.get(i).getName());

        final StorageReference load = mStorageRef.child(filteredZones.get(i).getImg());
        GlideApp.with(context)
                .load(load)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        viewHolder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        viewHolder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(viewHolder.imgSector);
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

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.zoneRow_txtRouteName)
        TextView txtRouteName;
        @BindView(R.id.zoneRow_imgSector)
        ImageView imgSector;
        @BindView(R.id.zoneRow_progressBar)
        ProgressBar progressBar;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


}