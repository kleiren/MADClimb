package es.kleiren.madclimb.root;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable disk persistence for the database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
