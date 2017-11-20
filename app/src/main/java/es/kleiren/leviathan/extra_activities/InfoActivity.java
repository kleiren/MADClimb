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


public class InfoActivity extends AppCompatActivity {

    String title;
    String location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        location = getIntent().getStringExtra("location");

        Toolbar toolbar = (Toolbar) findViewById(R.id.tlb_infoActivity);
        setSupportActionBar(toolbar);

        title = this.getIntent().getStringExtra("title");
        setTitle(title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, InfoFragment.newInstance(location, title))
                .commit();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(new AutoTransition());
    }

}
