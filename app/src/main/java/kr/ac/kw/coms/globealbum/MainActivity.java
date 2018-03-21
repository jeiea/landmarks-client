package kr.ac.kw.coms.globealbum;

import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    kr.ac.kw.coms.globealbum.EXIFinfo exifInterface = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            Uri uri = data.getData();
            String filePath = uri.getPath();
            exifInterface.getLocation(filePath);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        exifInterface = new kr.ac.kw.coms.globealbum.EXIFinfo();
        Intent choosefile = new Intent(Intent.ACTION_GET_CONTENT);
        choosefile.setType("file/*");
        Intent intent = Intent.createChooser(choosefile, "SELECT FILE");
        startActivityForResult(intent, 1);
    }
}
