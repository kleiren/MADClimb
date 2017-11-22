package es.kleiren.leviathan.extra_activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.transition.Fade;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.view.View;

import es.kleiren.leviathan.R;
import es.kleiren.leviathan.data_classes.Datum;


public class InfoActivity extends AppCompatActivity {

    String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Toolbar toolbar = findViewById(R.id.tlb_infoActivity);
        setSupportActionBar(toolbar);

        type = getIntent().getStringExtra("type");
        Datum datum = (Datum) this.getIntent().getSerializableExtra("datum");
        setTitle(datum.getName());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, InfoFragment.newInstance(type, datum))
                .commit();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(new AutoTransition());
    }

}
