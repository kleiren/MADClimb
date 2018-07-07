package es.kleiren.madclimb.main;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetMenuDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @BindView(R.id.main_drawerLayout)
    DrawerLayout drawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView sideNavigationView;
    @BindView(R.id.navigation)
    BottomNavigationView bottomNavigationView;
    private DatabaseReference mDatabase;
    public ArrayList<ArrayList<String>> zonesFromFirebase = new ArrayList<>();
    private boolean shownNewZones = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ZoneListFragment.newInstance())
                .commit();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        sideNavigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setBackgroundColor(Color.WHITE);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        checkFirstRun();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_share:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "MADClimb");
                String sAux = "\nTe recomiendo esta app\n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id=es.kleiren.madclimb \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "choose one"));
                return true;

            case R.id.nav_send:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "madclimbapp@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MADClimb error");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                return true;

            case R.id.nav_twitter:

                Intent intent;
                try {
                    getApplicationContext().getPackageManager().getPackageInfo("com.twitter.android", 0);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=carlosanred"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/carlosanred"));
                }
                startActivity(intent);
                return true;

            case R.id.nav_rate:

                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                return true;

            case R.id.nav_github:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kleiren/Leviathan"));
                startActivity(browserIntent);
                return true;

            case R.id.nav_about:
                new LibsBuilder()
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withFields(R.string.class.getFields())
                        .start(this);
                return true;

            case R.id.nav_zones:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, ZoneListFragment.newInstance())
                        .commit();
                return true;

            case R.id.nav_map:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, ZoneListMapFragment.newInstance())
                        .commit();
                return true;

            case R.id.nav_favs:
                Toast.makeText(this, "Próximamente :)", Toast.LENGTH_SHORT).show();
                return true;

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            // Place your dialog code here to display the dialog

            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.welcome)
                    .setMessage(R.string.welcome_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(getResources().getDrawable(R.drawable.info))
                    .show();
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        } else {
            //checkFirebaseChanges();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
        } else {
            signInAnonymously();
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("LEVIATHAN", "signInAnonymously:FAILURE", exception);
            }
        });
    }

    void showNewZones() {
        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson2 = new Gson();
        String json2 = mPrefs.getString("SerializableObject", "");
        ArrayList<ArrayList<String>> zonesFromPreferences = gson2.fromJson(json2, new TypeToken<ArrayList<ArrayList<String>>>() {
        }.getType());
        try {
            Log.i("ZonesFromPreferences", zonesFromPreferences.toString());
            Log.i("ZonesFromFirebase", zonesFromFirebase.toString());
        } catch (Exception e) {
            return;
        }
        Gson gson = new Gson();
        String json = gson.toJson(zonesFromFirebase);
        prefsEditor.putString("SerializableObject", json);
        prefsEditor.apply();

        if (zonesFromPreferences.size() != 0) {
            StringBuilder newZones = new StringBuilder();
            iLoop:
            for (int i = 0; i < zonesFromFirebase.size(); i++) {
                if (!zonesFromFirebase.get(i).isEmpty()) {
                    for (int j = 0; j < zonesFromPreferences.size(); j++) {
                        Log.i("num", i + " " + j);
                        Log.i("1", zonesFromFirebase.get(i).toString());
                        Log.i("2", zonesFromPreferences.get(j).toString());

                        if (zonesFromFirebase.get(i).get(0).equals(zonesFromPreferences.get(j).get(0))) {
                            String zoneName = zonesFromFirebase.get(i).get(0);

                            Log.i("1", zonesFromFirebase.get(i).get(0).toString());
                            Log.i("2", zonesFromPreferences.get(j).get(0).toString());

                            Collection firstList = zonesFromPreferences.get(j);
                            Collection secondList = zonesFromFirebase.get(i);
                            Log.i("COLL1", secondList.toString());
                            Log.i("COLL2", firstList.toString());
                            secondList.removeAll(firstList);

                            if (!secondList.isEmpty()) {
                                newZones.append("Zona: " + zoneName + "\n    Nuevos sectores: ");

                                for (String sector : zonesFromFirebase.get(i)) {
                                    newZones.append(sector).append(" ");
                                }
                                newZones.append("\n");
                            }
                            continue iLoop;
                        } else {

                            if (j >= zonesFromPreferences.size() - 1) {
                                newZones.append("Nueva zona: " + zonesFromFirebase.get(i).get(0) + "\n    Con sectores: ");
                                for (i = 1; i < zonesFromFirebase.get(i).size(); i++) {
                                    newZones.append(zonesFromFirebase.get(i)).append(" ");
                                }
                            }

                        }
                    }
                }
            }
            if (!newZones.toString().isEmpty()) {
                final BottomSheetMenuDialog dialog = new BottomSheetBuilder(this, R.style.BottomSheetBuilder_DialogStyle)
                        .setMode(BottomSheetBuilder.MODE_LIST)
                        .setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark))
                        .addTitleItem("Zonas y sectores añadidos desde última conexión")
                        .setTitleTextColor(getResources().getColor(R.color.black_overlay))
                        .addTitleItem(newZones.toString())
                        .expandOnStart(false)
                        .createDialog();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.show();
                    }
                });
            }
        }
    }

    void checkFirebaseChanges() {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("zones").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("FIREBASE", dataSnapshot.getValue().toString());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Log.i("FIREBASE", postSnapshot.child("sectors").toString());
                    ArrayList<String> zoneFromFirebase = new ArrayList<>();
                    zoneFromFirebase.add(postSnapshot.child("name").getValue().toString());
                    for (DataSnapshot postPostSnapshot : postSnapshot.child("sectors").getChildren()) {
                        zoneFromFirebase.add(postPostSnapshot.child("name").getValue().toString());
                    }
                    zonesFromFirebase.add(zoneFromFirebase);
                }

                if (!shownNewZones) {
                    showNewZones();
                    shownNewZones = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
            }
        });
    }


}
