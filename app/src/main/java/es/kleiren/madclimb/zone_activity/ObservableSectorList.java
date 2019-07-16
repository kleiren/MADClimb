package es.kleiren.madclimb.zone_activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Observable;

import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.root.GlideApp;
import es.kleiren.madclimb.root.MyAppGlideModule;

public class ObservableSectorList extends Observable {

    private ArrayList<Sector> sectors = new ArrayList<>();
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;


    public void getSectorImagesFromFirebase(ArrayList<Sector> sectors, Context context) {

        this.sectors = sectors;


        for (Sector sector : this.sectors) {
            loadImageFromFirebase(sector, context);
        }

    }

    public void loadImageFromFirebase(final Sector sector, final Context context) {

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        setChanged();
        notifyObservers(sectors);

        final StorageReference load = mStorageRef.child(sector.getImg());
        load.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                if (MyAppGlideModule.isValidContextForGlide(context)) {
                    GlideApp.with(context)
                            .load(load)
                            .placeholder(R.drawable.mountain_placeholder)
                            .signature(new ObjectKey(storageMetadata.getUpdatedTimeMillis()))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    setChanged();
                                    notifyObservers(sectors);
                                    return false;
                                }
                            }).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                }
            }
        });
    }
}
