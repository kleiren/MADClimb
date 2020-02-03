package es.kleiren.madclimb.zone_activity;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
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
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.root.GlideApp;
import es.kleiren.madclimb.util.InfoChartUtils;

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

        viewHolder.txtSectorName.setText(filteredSectors.get(i).getName());


        Spannable spannable = new SpannableString(" " + filteredSectors.get(i).routesFiltered[0] + "  " + filteredSectors.get(i).routesFiltered[1] + "  " + filteredSectors.get(i).routesFiltered[2] + "  " + filteredSectors.get(i).routesFiltered[3] + " ");
        int length = 0;
        for (int j = 0; j < filteredSectors.get(i).routesFiltered.length; j++) {
            spannable.setSpan(new ForegroundColorSpan(InfoChartUtils.colors[j]), length, length + filteredSectors.get(i).routesFiltered[j].toString().length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            length += filteredSectors.get(i).routesFiltered[j].toString().length() + 2;
        }
        viewHolder.txtStats.setText(spannable);


        final StorageReference load = mStorageRef.child(filteredSectors.get(i).getImg());
        GlideApp.with(context)
                .load(load)
                .placeholder(R.drawable.mountain_placeholder)
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
                .into(viewHolder.img);
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
        TextView txtSectorName;
        @BindView(R.id.sectorRow_txtStats)
        TextView txtStats;
        @BindView(R.id.sectorRow_imgSector)
        ImageView img;
        @BindView(R.id.sectorRow_progressBar)
        ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}