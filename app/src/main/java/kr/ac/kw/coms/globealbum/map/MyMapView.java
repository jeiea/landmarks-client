package kr.ac.kw.coms.globealbum.map;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
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
    IMapController mapController = null;

    ArrayList<MarkerTouchListener> markerListeners = new ArrayList<>(); //마커클릭 시 필요한 리스너를 모아둔다
    List<MarkerLineFolderOverlay> markerLineFolderOverlayList = new ArrayList<MarkerLineFolderOverlay>();
    MarkerLineFolderOverlay markerLineFolderOverlay = null;  //마커 모아서 관리

     // Constructor used by XML layout resource (uses default tile source).
    public MyMapView(final Context context, final AttributeSet attrs) {
        super(context, null, null, attrs);
        this.context=context;
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


    //맵뷰의 내용을 다시 그려주는 메서드
    public void myMapViewInvalidate(){
        invalidate();
    }


    //맵 초기 설정
    private void mapConfiguration() {

        setTileSource(TileSourceFactory.BASE_OVERLAY_NL);    //맵 렌더링 설정
        setBuiltInZoomControls(false);
        setMultiTouchControls(true);
        setScrollableAreaLimitLatitude(TileSystem.MaxLatitude - 5, TileSystem.MinLatitude+ 35, 0);
        //setScrollableAreaLimitLongitude(TileSystem.MinLongitude+ 12, TileSystem.MaxLongitude+12, 0);

        double logZoom = getLogZoom();

        setMinZoomLevel(logZoom);   //최소 줌 조절
        setMaxZoomLevel(6.0);   //최대 줌 조절

        mapController = getController();
        mapController.setZoom(logZoom);


        markerLineFolderOverlay = new MarkerLineFolderOverlay(this);


        addMarkerToMapviewReceiver();
        myMapViewInvalidate();
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

    //화면 터치 시 동작의 리스너를 등록
    public void setOnTouchMapViewListener(MarkerTouchListener listener) {
        markerListeners.add(listener);
    }

    //발생 이벤트를 전달
    public void dispatchMarkerTouch(MarkerLineFolderOverlay route, Marker marker) {
        for (MarkerTouchListener listener: markerListeners) {
            listener.OnMarkerTouch(marker);
        }
    }

    //현재 화면에 있는 마커의 개수 변경시 알려주는 리시버
    public void addShowCurrentMarkerChangeReceiver(){
        MapListener mapListener = new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                /*
                final List<OverlayItem> displayed = mMyLocationOverlay.getDisplayedItems();
                final StringBuilder buffer = new StringBuilder();
                String sep = "";
                for (final OverlayItem item : displayed) {
                    buffer.append(sep).append('\'').append(item.getTitle()).append('\'');
                    sep = ", ";
                }
                Toast.makeText(
                        SampleWithMinimapItemizedoverlay.this,
                        "Currently displayed: " + buffer.toString(), Toast.LENGTH_LONG).show();
                        */
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        };
        addMapListener(mapListener);
    }

    public void addMapListener(MapListener mapListener){
        addMapListener(mapListener);
    }

    //화면에 마커를 등록하는 리시버
    public void addMarkerToMapviewReceiver(){
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {   //화면 한번 터치시
                //mapController.animateTo(p); //좌표로 화면 이동
                Marker marker = new Marker(MyMapView.this);
                marker.setPosition(p);

                marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {  //기존에 있는 마커를 터치해야지 화면에 계속 생기게 만듦
                        if (markerLineFolderOverlay.clickedMarker.getPosition().getLongitude() == marker.getPosition().getLongitude() && markerLineFolderOverlay.clickedMarker.getPosition().getLatitude() == marker.getPosition().getLatitude()){
                            markerLineFolderOverlay.clickedMarker=null;
                            markerLineFolderOverlay.addMarker(marker);
                            Toast.makeText(context, "마커 등록 완료", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                if( markerLineFolderOverlay.clickedMarker== null){  //새로운 마커 추가
                    markerLineFolderOverlay.clickedMarker=marker;
                    markerLineFolderOverlay.addMarker(marker);
                }
                else{   //기존에 있는 마커를 지우고 새로운 위치에 마커 생성
                    markerLineFolderOverlay.remove(markerLineFolderOverlay.clickedMarker);
                    markerLineFolderOverlay.clickedMarker=marker;
                    markerLineFolderOverlay.addMarker(marker);
                }
                getOverlays().add(markerLineFolderOverlay);
                myMapViewInvalidate();
                return true;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) {    //길게 터치시
                return false;
            }
        };
        addMapEventReceiver(mapEventsReceiver);
    }
    //이벤트 리시버를 받아서 맵뷰에 등록
    public void addMapEventReceiver(MapEventsReceiver mapEventsReceiver){
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        getOverlays().add(mapEventsOverlay);
    }
}