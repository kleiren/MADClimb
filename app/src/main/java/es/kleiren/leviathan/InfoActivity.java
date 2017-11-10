package es.kleiren.leviathan;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.transition.Fade;
import android.support.v7.app.AppCompatActivity;
import android.transition.AutoTransition;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class InfoActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

         setContentView(R.layout.activity_info);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new InfoFragment())
                .commit();



    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(new AutoTransition());
    }

}
