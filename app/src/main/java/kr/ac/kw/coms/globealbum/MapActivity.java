package kr.ac.kw.coms.globealbum;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.util.DisplayMetrics;
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


public class MapActivity extends AppCompatActivity {

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
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = (MapView)findViewById(R.id.map);

        mapConfiguration();


    }
    private void mapConfiguration() {    //맵 생성 후 초기 설정
        final DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();   //해상도 측정을 위한 객체

        map.setTileSource(TileSourceFactory.BASE_OVERLAY_NL);    //맵 렌더링 설정
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude, -TileSystem.MaxLatitude, 0);
        map.setScrollableAreaLimitLongitude(-TileSystem.MaxLongitude, TileSystem.MaxLongitude, 0);
        map.setMinZoomLevel(1.25);   //최소 줌 조절
        map.setMaxZoomLevel(6.0);   //최대 줌 조절
        map.setTilesScaledToDpi(true); //dpi에 맞게 조절

        //minimap
        minimapOverlay = new MinimapOverlay(this,map.getTileRequestCompleteHandler());
        minimapOverlay.setWidth(dm.widthPixels/5);
        minimapOverlay.setHeight(dm.heightPixels/5);
        minimapOverlay.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        map.getOverlays().add(0,minimapOverlay);

        mapController = map.getController();
        mapController.setZoom(1.25);
        mapController.setCenter(new GeoPoint(42.8583,-10));

        MapEventsReceiver mReceiver = new MapEventsReceiver() { //화면 터치시 좌표 토스트메시지 출력, 좌표로 화면 이동
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Toast.makeText(getBaseContext(), p.getLatitude() + "-" + p.getLongitude(), Toast.LENGTH_SHORT).show();
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
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

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
