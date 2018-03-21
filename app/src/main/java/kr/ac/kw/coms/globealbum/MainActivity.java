package kr.ac.kw.coms.globealbum;

import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.content.Context;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;


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

    MapView map = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //퍼미션 핸들 처리 부분

        //osmdroid 초기 구성
        Context ctx = getApplicationContext();
        org.osmdroid.config.Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // 맵 만들기
        setContentView(R.layout.activity_main);
        map = (MapView)findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(2);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);


        ddd();
    }

    private void ddd() {
        exifInterface = new kr.ac.kw.coms.globealbum.EXIFinfo();
        Intent choosefile = new Intent(Intent.ACTION_GET_CONTENT);
        choosefile.setType("file/*");
        Intent intent = Intent.createChooser(choosefile, "SELECT FILE");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume(); //osmdroid configuration refresh
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause(); //osmdroid configuration refresh
    }
}
