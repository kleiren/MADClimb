package es.kleiren.madclimb.root;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.multidex.MultiDexApplication;

import com.google.firebase.database.FirebaseDatabase;

import es.kleiren.madclimb.util.ThemeHelper;

public class MyFirebaseApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable disk persistence for the database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String themePref = sharedPreferences.getString("themePref", ThemeHelper.DEFAULT_MODE);
        ThemeHelper.applyTheme(themePref);
    }
}
