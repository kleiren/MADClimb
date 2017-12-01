package es.kleiren.madclimb.zone_activity;

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

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.root.GlideApp;

import static android.content.Context.MODE_PRIVATE;

public class SectorDataAdapter extends RecyclerView.Adapter<SectorDataAdapter.ViewHolder> implements Filterable {
    private ArrayList<Sector> sectors;
    private ArrayList<Sector> filteredSectors;
    private Context context;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;


    public SectorDataAdapter(ArrayList<Sector> sectors, Context context) {
        this.context = context;
        this.sectors = sectors;
        this.filteredSectors = sectors;
    }

    @Override
    public SectorDataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);


        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sector_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SectorDataAdapter.ViewHolder viewHolder, int i) {

        viewHolder.txt_name.setText(filteredSectors.get(i).getName());

        mDatabase.child("zones").child(filteredSectors.get(i).getId()).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final StorageReference load = mStorageRef.child(filteredSectors.get(i).getCroquis());

//        GlideApp.with(context)
//                .load(load)
//                .placeholder(R.drawable.mountain_placeholder)
//                .centerCrop()
//                .into(viewHolder.img);


        load.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {

                    GlideApp.with(context)
                            .load(load)
                            .placeholder(R.drawable.mountain_placeholder)
                            .centerCrop()
                            .signature(new ObjectKey(storageMetadata.getUpdatedTimeMillis()))

                            .into(viewHolder.img);


            }
        });

    }

    @Override
    public int getItemCount() {
        return filteredSectors.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {

                    filteredSectors = sectors;
                } else {

                    ArrayList<Sector> tempList = new ArrayList<>();

                    for (Sector sector : sectors) {

                        if (sector.getName().toLowerCase().contains(charString.toLowerCase())) {

                            tempList.add(sector);
                        }
                    }

                    filteredSectors = tempList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredSectors;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredSectors = (ArrayList<Sector>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.sectorRow_txtSectorName)
        TextView txt_name;
        @BindView(R.id.sectorRow_imgSector)
        ImageView img;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}