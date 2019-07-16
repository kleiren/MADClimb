package es.kleiren.madclimb.main;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
import java.util.Observable;

import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.root.GlideApp;
import es.kleiren.madclimb.root.MyAppGlideModule;

public class ObservableZoneList extends Observable {

    private ArrayList<Zone> zones = new ArrayList<>();
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;


    public void getZonesFromFirebaseZoneList(ArrayList<Zone> zones, Context context) {

        this.zones = zones;

        for (Zone zone : this.zones) {
            getZoneFromFirebaseZone(zone, context);
        }

    }

    public void getZoneFromFirebaseZone(final Zone zone, final Context context) {

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("zones/" + zone.getId() + "/sectors").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                zone.setHasSectors(dataSnapshot.exists());
                setChanged();
                notifyObservers(zones);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final StorageReference load = mStorageRef.child(zone.getImg());
        load.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {

                if (MyAppGlideModule.isValidContextForGlide(context)) {
                    GlideApp.with(context)
                            .load(load)
                            .signature(new ObjectKey(storageMetadata.getUpdatedTimeMillis()))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    setChanged();
                                    notifyObservers(zones);
                                    return false;
                                }
                            }).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                }
            }
        });
    }

}
