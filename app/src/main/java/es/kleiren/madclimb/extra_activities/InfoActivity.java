package es.kleiren.madclimb.extra_activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.transition.Fade;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Datum;


public class InfoActivity extends AppCompatActivity {

    String type;
    @BindView(R.id.infoAct_toolbar)
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ButterKnife.bind(this);
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

}
