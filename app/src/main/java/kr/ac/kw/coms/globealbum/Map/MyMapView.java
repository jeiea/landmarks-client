package kr.ac.kw.coms.globealbum.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MyMapView extends org.osmdroid.views.MapView{
    public Context context=null;
    MapView mapView = this;
    IMapController mapController = null;


    List<MarkerLineFolderOverlay> markerLineFolderOverlayList = new ArrayList<MarkerLineFolderOverlay>();
    MarkerLineFolderOverlay markerLineFolderOverlay = null;  //마커 모아서 관리

     // Constructor used by XML layout resource (uses default tile source).
    public MyMapView(final Context context, final AttributeSet attrs) {
        super(context, null, null, attrs);
        this.context=context;
        markerLineFolderOverlay = new MarkerLineFolderOverlay(this);
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

    //맵 초기 설정
    private void mapConfiguration() {

        setTileSource(TileSourceFactory.BASE_OVERLAY_NL);    //맵 렌더링 설정
        setBuiltInZoomControls(false);
        setMultiTouchControls(true);
        setScrollableAreaLimitLatitude(TileSystem.MaxLatitude - 5, -TileSystem.MaxLatitude + 35, 0);
        setScrollableAreaLimitLongitude(-TileSystem.MaxLongitude + 12, TileSystem.MaxLongitude+12, 0);

        double logZoom = getLogZoom();

        setMinZoomLevel(logZoom);   //최소 줌 조절
        setMaxZoomLevel(6.0);   //최대 줌 조절

        mapController = getController();
        mapController.setZoom(logZoom);

        addMapEventListener();
        invalidate();
    }

    //맵뷰를 화면에 맞추기 위해 필요한 사전 작업
    private double getLogZoom(){
        double mapRatio = 1; // 타일은 정사각형.
        double dimenRatio = getWidth() / (double) getHeight(); // 화면비율
        int longAxis = dimenRatio < mapRatio ? getHeight() : getWidth(); // 긴 축을 구함
        double zoom = longAxis / 256.0;     //타일 하나의 픽셀수인 256으로 나눔

        return Math.log(zoom) / Math.log(2);
    }


    //마커, 경로를 가지고 있는 markerLineFolderOverlay 객체를 반환
    public MarkerLineFolderOverlay getRoute(){
        return markerLineFolderOverlay;
    }

    public void deleteRoute(MarkerLineFolderOverlay folderOverlay){

    }
    public void deleteRoute(int index){

    }

    public interface MarkerTouchListener {
        void OnMarkerTouch(Marker marker);
    }
    ArrayList<MarkerTouchListener> markerListeners = new ArrayList<>();
    //화면 터치 시 동작의 리스너를 등록
    public void setOnTouchMapViewListener(MarkerTouchListener listener) {
        markerListeners.add(listener);
    }

    public void dispatchMarkerTouch(MarkerLineFolderOverlay route, Marker marker) {
        for (MarkerTouchListener listener: markerListeners) {
            listener.OnMarkerTouch(marker);
        }
    }

    //화면 터치 시 동작의 리스너를 해제
    public void removeOnTouchMapViewListener(MapEventsReceiver mapEventsReceiver){
        getOverlays().remove(mapEventsReceiver);
    }

    //화면 터치 시, 마커를 화면에 추가
    private void addMapEventListener(){
        MapEventsReceiver mReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {   //화면 한번 터치시
                mapController.animateTo(p); //좌표로 화면 이동

                Marker marker = new Marker(mapView);
                marker.setPosition(p);

                markerLineFolderOverlay.addMarkerLine(marker);
                getOverlays().add(markerLineFolderOverlay);

                Toast.makeText(context, markerLineFolderOverlay.getItems().size()+"", Toast.LENGTH_SHORT).show();

                return false;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) {    //길게 터치시
                return false;
            }
        };
        MapEventsOverlay eventsOverlay = new MapEventsOverlay(mReceiver);
        getOverlays().add(eventsOverlay);
    }
}