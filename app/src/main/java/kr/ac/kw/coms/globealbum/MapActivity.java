package kr.ac.kw.coms.globealbum;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.preference.PreferenceManager;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;


public class MapActivity extends AppCompatActivity {

    final double WORLDMAP_ZOOM_LEVEL = 1.1;

    Context context=null;
    MapView map = null;
    IMapController mapController = null;
    MinimapOverlay minimapOverlay = null;
    Marker marker = null;  //지난번 클릭 마커 저장
    GeoPoint currentGeopoint = null;   //현재 위치 저장


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //가로모드 고정

        //osmdroid 초기 구성
        context= getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        map = (MapView)findViewById(R.id.map);

        mapConfiguration();
    }
    private void mapConfiguration() {    //맵 생성 후 초기 설정
        final DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();   //해상도 측정을 위한 객체

        map.setTileSource(TileSourceFactory.MAPNIK);    //맵 렌더링 설정
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude-4, -TileSystem.MaxLatitude+35, 0);
        map.setScrollableAreaLimitLongitude(-TileSystem.MaxLongitude+15, TileSystem.MaxLongitude+10, 0);
        map.setMinZoomLevel(WORLDMAP_ZOOM_LEVEL );   //최소 줌 조절
        map.setMaxZoomLevel(6.0);   //최대 줌 조절
        map.setTilesScaledToDpi(true); //dpi에 맞게 조절

        //minimap
        minimapOverlay = new MinimapOverlay(context,map.getTileRequestCompleteHandler());
        minimapOverlay.setWidth(dm.widthPixels/4);
        minimapOverlay.setHeight(dm.heightPixels/4);
        minimapOverlay.setTileSource(TileSourceFactory.BASE_OVERLAY_NL);
        minimapOverlay.setZoomDifference(3);
        map.getOverlays().add(0,minimapOverlay);


        mapController = map.getController();
        mapController.setZoom(WORLDMAP_ZOOM_LEVEL );
        mapController.setCenter(new GeoPoint(40.0,11));


        MapEventsReceiver mReceiver = new MapEventsReceiver() { //화면 터치시 좌표 토스트메시지 출력, 좌표로 화면 이동
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                mapController.animateTo(p); //좌표로 화면 이동
                addMarker(p);
                return false;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) {    //길게 터치시
                return false;
            }
        };

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(mReceiver);
        map.getOverlays().add(eventsOverlay);
        map.invalidate();
    }

    private void addMarker(GeoPoint p) {   //화면 터치시 마커를 화면에 표시
        map.getOverlays().remove(marker);
        marker = new Marker(map);
        marker.setPosition(p);

        map.getOverlays().add(marker);
        map.invalidate(); //map refresh
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(map !=null)
            map.onResume(); //osmdroid configuration refresh
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(map!=null)
            map.onPause(); //osmdroid configuration refresh
    }

}
