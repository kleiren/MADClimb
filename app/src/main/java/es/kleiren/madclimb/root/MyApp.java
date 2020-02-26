package es.kleiren.madclimb.root;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import es.kleiren.madclimb.util.ThemeHelper;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable disk persistence for the database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        boolean isDarkModeEnabled = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isDarkModeEnabled", false);
        ThemeHelper.applyDarkTheme(isDarkModeEnabled);
    }
}
