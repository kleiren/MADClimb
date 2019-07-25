package es.kleiren.madclimb.root;

import androidx.multidex.MultiDexApplication;

import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable disk persistence for the database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
