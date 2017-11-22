package es.kleiren.madclimb.util;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import es.kleiren.madclimb.data_classes.Route;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.data_classes.Zone;

/**
 * Created by carlos on 10/08/17.
 */

public class UploadHelper {


    public static void uploadZone(Zone zone) {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("zones").child(zone.getName()).setValue(zone);

    }

    public static void uploadSector(Sector sector) {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("zones").child(sector.getZoneName()).child(sector.getName()).setValue(sector);


    }


    public static void uploadRoute(Route route) {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("zones").child(route.getZoneName()).child(route.getSectorName()).child(route.getName()).setValue(route);

    }


    public static void uploadFile(Uri uri, String name, UploadTask uploadTask, StorageReference storageRef) {


        Uri file = uri;
        StorageReference riversRef = storageRef.child("images/" + name);
        uploadTask = riversRef.putFile(file);

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new

                                                OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        // Handle unsuccessful uploads
                                                    }
                                                }).

                addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                });
    }
}
