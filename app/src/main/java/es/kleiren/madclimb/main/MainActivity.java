package es.kleiren.madclimb.main;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.survivingwithandroid.weather.lib.StandardHttpClient;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.City;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

import java.util.List;

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
    public String updates;
    private boolean shownNewZones = false;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private String updateId;

    WeatherClient.WeatherEventListener weatherEventListener = new WeatherClient.WeatherEventListener() {
        @Override
        public void onWeatherRetrieved(CurrentWeather weather) {
            Toast.makeText(MainActivity.this, "" + weather.weather.temperature.getMinTemp(), Toast.LENGTH_SHORT).show();
            Log.i("CARLOS", "" + weather.weather.currentCondition.getCondition());
        }

        @Override
        public void onWeatherError(WeatherLibException wle) {

            wle.printStackTrace();

        }

        @Override
        public void onConnectionError(Throwable t) {

            Log.e("CARLOS", "" + t.getMessage());

        }
    };
    private WeatherClient client;

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

            case R.id.nav_news:
                showChangelog();
                try {
                    WeatherConfig weatherConfig = new WeatherConfig();
                    weatherConfig.ApiKey = getResources().getString(R.string.weather_key);
                    client = (new WeatherClient.ClientBuilder()).attach(this)
                            .httpClient(com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient.class)
                            .provider(new OpenweathermapProviderType())
                            .config(weatherConfig)
                            .build();

                    WeatherRequest weatherRequest = new WeatherRequest(40.4010736,-3.762422);
                    client.getCurrentCondition(weatherRequest, weatherEventListener);
                } catch (WeatherProviderInstantiationException e) {
                    e.printStackTrace();
                }
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
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, ZoneListFavFragment.newInstance())
                        .commit();
                return true;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.welcome)
                    .setMessage(R.string.welcome_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(getResources().getDrawable(R.drawable.ic_info_black))
                    .show();
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
            checkFirebaseNews(false);
        } else {
            checkFirebaseNews(true);
        }
    }

    private void showChangelog() {
        try {
            if (!getSupportFragmentManager().findFragmentByTag("changelog_fragment").isVisible())
                new ChangelogDialogFragment().show(getSupportFragmentManager(), "changelog_fragment");
        } catch (Exception e) {
            new ChangelogDialogFragment().show(getSupportFragmentManager(), "changelog_fragment");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    void checkFirebaseNews(final boolean show) {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("updates").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    updates = dataSnapshot.child("text").getValue().toString();
                    updateId = dataSnapshot.child("id").getValue().toString();
                    if (!updateId.equals(getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("updateId", ""))) {
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("updateId", updateId)
                                .apply();
                        if (show) showChangelog();
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE", "The read failed: " + databaseError.getCode());
            }
        });
    }

}
