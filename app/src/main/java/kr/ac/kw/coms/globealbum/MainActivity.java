package kr.ac.kw.coms.globealbum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    kr.ac.kw.coms.globealbum.EXIFinfo exifInterface = null;

    MapView map = null;
    IMapController mapController=null;
    Drawable marker;
    double currentX,currentY;
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

        //퍼미션 핸들 처리 부분

        //osmdroid 초기 구성
        Context ctx = getApplicationContext();
        org.osmdroid.config.Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = (MapView)findViewById(R.id.map);
        marker=getResources().getDrawable(R.drawable.marker_default);
        marker.setBounds(0,1,1,0);

        mapConfiguration();
       // ddd();
    }


    private void mapConfiguration(){    //맵 생성 후 초기 설정
        map.setTileSource(TileSourceFactory.BASE_OVERLAY_NL);    //맵 렌더링 설정
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        //map.setScrollableAreaLimitLatitude(30,-85,0);
        //map.setScrollableAreaLimitLongitude(150,150,0);

        mapController = map.getController();
        mapController.setZoom(3);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);

        MapEventsReceiver mReceiver = new MapEventsReceiver() { //화면 터치시 좌표 토스트메시지 출력, 좌표로 화면 이동
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Toast.makeText(getBaseContext(), p.getLatitude()+"-"+p.getLongitude(), Toast.LENGTH_SHORT).show();

                mapController.animateTo(p);

                ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
                items.add(new OverlayItem("Marker","Snippet",p));

                final ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(
                        getApplicationContext(), items,
                        new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                            @Override
                            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {

                                return true;
                            }
                            @Override
                            public boolean onItemLongPress(final int index, final OverlayItem item) {
                                return false;
                            }
                        }
                );

                mOverlay.setFocusItemsOnTap(true);
                //map.getOverlays().add(mOverlay);
                return false;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        MapEventsOverlay eventsOverlay = new MapEventsOverlay(getBaseContext(),mReceiver);
        map.getOverlays().add(eventsOverlay);
    }

    private void ddd() {
        exifInterface = new EXIFinfo();
        InputStream is = getResources().openRawResource(R.raw.honeyview_gps);
        try {

            exifInterface.getGPS(new ExifInterface(is));
        }
        catch (IOException e) {}
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
