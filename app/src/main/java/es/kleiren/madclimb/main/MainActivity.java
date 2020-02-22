package es.kleiren.madclimb.main;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.aboutlibraries.LibsBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.util.ThemeHelper;


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
    private SwitchCompat switcher;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private String updateId;

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
        bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBackground));
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
                String sAux = getString(R.string.recommend_app);
                sAux = sAux + "https://play.google.com/store/apps/details?id=es.kleiren.madclimb \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "choose one"));
                return true;

            case R.id.nav_send:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", getString(R.string.dev_email), null));
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
                return true;

            case R.id.nav_restrictions:
                Intent goToRestrictions = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bocm.es/boletin/CM_Orden_BOCM/2020/02/17/BOCM-20200217-19.PDF"));
                startActivity(goToRestrictions);
                return true;

            case R.id.nav_dark:
                switcher.setChecked(!switcher.isChecked());
                setDarkModePreference(switcher.isChecked());
                ThemeHelper.applyDarkTheme(switcher.isChecked());
                recreate();
                return true;

            case R.id.nav_policy:
                Intent goToPrivacy = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/madclimb-privacy-policy"));
                startActivity(goToPrivacy);
                return true;

            case R.id.nav_about:
                new LibsBuilder()
                        .withActivityTheme(R.style.WhiteTheme)
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

            case R.id.nav_history:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, HistoryFragment.newInstance())
                        .commit();
                return true;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
            checkFirebaseNews(false);
        } else {
            checkFirebaseNews(true);
        }
    }

    public void setDarkModePreference(boolean pref) {
        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .edit()
                .putBoolean("isDarkModeEnabled", pref)
                .apply();
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
        switcher = findViewById(R.id.switcher);
        boolean isDarkModeEnabled = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isDarkModeEnabled", false);
        switcher.setChecked(isDarkModeEnabled);
        switcher.setOnClickListener(v -> {
            setDarkModePreference(switcher.isChecked());
            ThemeHelper.applyDarkTheme(switcher.isChecked());
            recreate();
        });
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
