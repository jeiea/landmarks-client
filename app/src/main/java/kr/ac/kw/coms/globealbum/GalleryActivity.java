package kr.ac.kw.coms.globealbum;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
//https://www.androstock.com/tutorials/create-a-photo-gallery-app-in-android-android-studio.html
    }

    public void GalleryExit_click(View view) {
        this.finishActivity(-1);
    }

}
