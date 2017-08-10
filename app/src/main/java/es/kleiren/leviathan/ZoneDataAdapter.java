package es.kleiren.leviathan;

/**
 * Created by Carlos on 11/05/2017.
 */

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static es.kleiren.leviathan.R.id.imageView;

public class ZoneDataAdapter extends RecyclerView.Adapter<ZoneDataAdapter.ViewHolder> implements Filterable {
    private ArrayList<Zone> zones;
    private ArrayList<Zone> filteredZones;
    private Context context;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
// ...



    public ZoneDataAdapter(ArrayList<Zone> zones, Context context) {
        this.context = context;
        this.zones = zones;
        this.filteredZones = zones;
    }

    @Override
    public ZoneDataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zone_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ZoneDataAdapter.ViewHolder viewHolder, int i) {

        viewHolder.txt_name.setText(filteredZones.get(i).getName());

         mDatabase.child("zones").child("toledo").child("img").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(context, dataSnapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, databaseError.toString(), Toast.LENGTH_SHORT).show();

            }
        });



        StorageReference load = mStorageRef.child("images/img_cabeza.png");

        load.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context).load(uri.toString()).into(viewHolder.img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });



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