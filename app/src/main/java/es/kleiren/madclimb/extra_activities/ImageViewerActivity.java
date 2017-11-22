package es.kleiren.madclimb.extra_activities;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.transition.Fade;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import es.kleiren.madclimb.R;
import es.kleiren.madclimb.root.GlideApp;


public class ImageViewerActivity extends AppCompatActivity {


    private StorageReference mStorageRef;

    String imageRef, title;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);


        imageRef = this.getIntent().getStringExtra("image");
        title = this.getIntent().getStringExtra("title");
        setTitle(title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tlb_imageViewer);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mStorageRef = FirebaseStorage.getInstance().getReference();


        StorageReference load = mStorageRef.child(imageRef);
        GlideApp.with(getApplicationContext())
                .load(load)
                .into((ImageView) findViewById(R.id.imageView));


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(new AutoTransition());
    }

}
