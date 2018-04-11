package kr.ac.kw.coms.globealbum;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MinimapOverlay;


public class MapActivity extends AppCompatActivity {

    Context context = null;
    MapView mapView = null;
    DisplayMetrics dm;
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
        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        mapView = (MapView) findViewById(R.id.map);
        dm = getApplicationContext().getResources().getDisplayMetrics();   //해상도 측정을 위한 객체



        mapView.post(new Runnable() {
                     @Override
                     public void run() {
                         mapConfiguration();
                     }
                 }
        );
    }

    private void mapConfiguration() {    //맵 생성 후 초기 설정

        mapView.setTileSource(TileSourceFactory.MAPNIK);    //맵 렌더링 설정
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude - 5, -TileSystem.MaxLatitude + 35, 0);
        mapView.setScrollableAreaLimitLongitude(-TileSystem.MaxLongitude + 12, TileSystem.MaxLongitude+12, 0);

        double mapRatio = 1; // 타일은 정사각형.
        double dimenRatio = mapView.getWidth() / (double) mapView.getHeight(); // 화면비율
        int longAxis = dimenRatio < mapRatio ? mapView.getHeight() : mapView.getWidth(); // 긴 축을 구함
        double zoom = longAxis / 256.0;     //타일 하나의 픽셀수인 256으로 나눔
        double logZoom = Math.log(zoom) / Math.log(2);

        mapView.setMinZoomLevel(logZoom);   //최소 줌 조절
        mapView.setMaxZoomLevel(6.0);   //최대 줌 조절


        //addMinimap();

        mapController = mapView.getController();
        mapController.setZoom(logZoom);

        MapEventsReceiver mReceiver = new MapEventsReceiver() { //화면 터치시 좌표 토스트메시지 출력, 좌표로 화면 이동
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {   //화면 한번 터치시
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
        mapView.getOverlays().add(eventsOverlay);
        mapView.invalidate();
    }

    private void addMarker(GeoPoint p) {
        //input : 화면에서 터치된 부분의 위도 경도
        //marker를 mapView에 추가

        mapView.getOverlays().remove(marker);
        marker = new Marker(mapView);
        marker.setPosition(p);

        mapView.getOverlays().add(marker);
        mapView.invalidate(); //mapView refresh
    }

    private void addMinimap(){
        //미니맵을 생성 후 mapView에 추가

        minimapOverlay = new MinimapOverlay(context, mapView.getTileRequestCompleteHandler());
        minimapOverlay.setWidth(dm.widthPixels / 6);
        minimapOverlay.setHeight(dm.heightPixels / 5);
        minimapOverlay.setTileSource(TileSourceFactory.BASE_OVERLAY_NL);
        minimapOverlay.setZoomDifference(4);

        mapView.getOverlays().add(0, minimapOverlay);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null)
            mapView.onResume(); //osmdroid configuration refresh
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null)
            mapView.onPause(); //osmdroid configuration refresh
    }

}
