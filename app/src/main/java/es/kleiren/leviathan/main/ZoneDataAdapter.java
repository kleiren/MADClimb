package es.kleiren.leviathan.main;

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

import es.kleiren.leviathan.R;
import es.kleiren.leviathan.data_classes.Zone;
import es.kleiren.leviathan.root.GlideApp;

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

        mDatabase.child("zones").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(context, dataSnapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Toast.makeText(context, databaseError.toString(), Toast.LENGTH_SHORT).show();

            }
        });


        StorageReference load = mStorageRef.child(filteredZones.get(i).getImg());

        GlideApp.with(context)
                .load(load).centerCrop()
                .into(viewHolder.img);

//        load.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//
//                    GlideApp.with(context)
//                            .load("https://firebasestorage.googleapis.com/v0/b/leviathan-d57d8.appspot.com/o/images%2Fimg_sanmartin.jpg?alt=media&token=cc8f26b5-9315-4ebd-be0d-d3ef606eca55")
//                            .centerCrop()
//                            .placeholder(R.drawable.mountain_placeholder)
//                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//
//                            .into(viewHolder.img);
//                PicassoHelper.showImageCropped(context, "https://firebasestorage.googleapis.com/v0/b/leviathan-d57d8.appspot.com/o/images%2Fcroq_cabeza.jpg?alt=media&token=ba1232bb-d657-43fc-9674-566749275b68", viewHolder.img);
//
//                //  PicassoHelper.showImageCropped(context, uri.toString(), viewHolder.img);
////                    Picasso.with(context)
////                            .load(uri.toString())
////                            .resize(viewHolder.img.getWidth(),viewHolder.img.getHeight())
////
////                            .centerCrop()
////                            .into(viewHolder.img);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
//
//            }
//        });


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

        public ViewHolder(View view) {
            super(view);

            txt_name = (TextView) view.findViewById(R.id.textRouteName);
            img = (ImageView) view.findViewById(R.id.img_android);
        }
    }

}