package es.kleiren.madclimb.extra_activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Datum;

public class InfoActivity extends AppCompatActivity {

    String type;
    @BindView(R.id.infoAct_toolbar)
    Toolbar toolbar;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        type = getIntent().getStringExtra("type");
        Datum datum = (Datum) this.getIntent().getSerializableExtra("datum");
        setTitle(datum.getName());
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, InfoFragment.newInstance(type, datum))
                .commit();
    }
}
