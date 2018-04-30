package kr.ac.kw.coms.globealbum;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MinimapOverlay;

public class MyMapView extends org.osmdroid.views.MapView{
    IMapController mapController = null;
    Marker marker = null;  //지난번 클릭 마커 저장

     // Constructor used by XML layout resource (uses default tile source).
    public MyMapView(final Context context, final AttributeSet attrs) {
        super(context, null, null, attrs);
        this.post(new Runnable() {
                         @Override
                         public void run() {
                             mapConfiguration();

                         }
                     }
        );
    }
    public MyMapView(final Context context) {
        super(context, null, null, null);
    }


    private void mapConfiguration() {    //맵 생성 후 초기 설정

        setTileSource(TileSourceFactory.BASE_OVERLAY_NL);    //맵 렌더링 설정
        setBuiltInZoomControls(false);
        setMultiTouchControls(true);
        setScrollableAreaLimitLatitude(TileSystem.MaxLatitude - 5, -TileSystem.MaxLatitude + 35, 0);
        setScrollableAreaLimitLongitude(-TileSystem.MaxLongitude + 12, TileSystem.MaxLongitude+12, 0);

        double mapRatio = 1; // 타일은 정사각형.
        double dimenRatio = getWidth() / (double) getHeight(); // 화면비율
        int longAxis = dimenRatio < mapRatio ? getHeight() : getWidth(); // 긴 축을 구함
        double zoom = longAxis / 256.0;     //타일 하나의 픽셀수인 256으로 나눔
        double logZoom = Math.log(zoom) / Math.log(2);

        setMinZoomLevel(logZoom);   //최소 줌 조절
        setMaxZoomLevel(6.0);   //최대 줌 조절

        mapController = getController();
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
        getOverlays().add(eventsOverlay);
        invalidate();
    }
    private void addMarker(GeoPoint p) {
        //input : 화면에서 터치된 부분의 위도 경도
        //marker를 mapView에 추가

        getOverlays().remove(marker);
        marker = new Marker(this);
        marker.setPosition(p);

        getOverlays().add(marker);
        invalidate(); //mapView refresh
    }
}
