package es.kleiren.leviathan;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by carlos on 10/08/17.
 */

public class UploadHelper {


    public static void uploadZone(Zone zone, DatabaseReference mDatabase){

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("zones").child(zone.getName()).setValue(zone);

    }

    public static void uploadRoute(Route route, DatabaseReference mDatabase){

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("zones").child(route.getZoneName()).child(route.getName()).setValue(route);

    }
}
