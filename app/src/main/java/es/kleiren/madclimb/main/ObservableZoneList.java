package es.kleiren.madclimb.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
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

import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Zone;
import es.kleiren.madclimb.root.GlideApp;
import lecho.lib.hellocharts.view.ColumnChartView;

/**
 * Created by carlos on 12/12/17.
 */

public class ObservableZoneList extends Observable {

    private ArrayList<Zone> zones = new ArrayList<>();
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;


    public void getZonesFromFirebaseZoneList(ArrayList<Zone> zones, Context context, View view) {

        this.zones = zones;


        for (Zone zone : this.zones) {
            getZoneFromFirebaseZone(zone, context, view);
        }

    }

    public void getZoneFromFirebaseZone(final Zone zone, final Context context, View view) {

        mStorageRef = FirebaseStorage.getInstance().getReference();
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mDatabase = FirebaseDatabase.getInstance().getReference();


        mDatabase.child("zones/" + zone.getId() + "/sectors").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                 zone.setHasSectors(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        final StorageReference load = mStorageRef.child(zone.getImg());

        load.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
//
        GlideApp.with(context).asBitmap()
                .load(load)
                .signature(new ObjectKey(storageMetadata.getUpdatedTimeMillis()))
                .listener(new RequestListener<Bitmap>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                       // zone.setImgDrawable(resource);
                        setChanged();
                        notifyObservers(zones);
                        return false;
                    }
                }).into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL);

            }});
//        load.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
//            @Override
//            public void onSuccess(StorageMetadata storageMetadata) {
//
//
//               GlideApp.with(context).asDrawable()
//                        .load(load).centerCrop()
//                        .signature(new ObjectKey(storageMetadata.getUpdatedTimeMillis())).listener(new RequestListener<Drawable>() {
//                   @Override
//                   public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                       Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
//                       return false;
//                   }
//
//                   @Override
//                   public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                       Toast.makeText(context, "loaded", Toast.LENGTH_SHORT).show();
//
//                       zone.setImgDrawable(resource);
//                       setChanged();
//                       notifyObservers(zones);
//                       return false;
//                   }
//
//
//               });
//
//            }
//        });


    }

}
