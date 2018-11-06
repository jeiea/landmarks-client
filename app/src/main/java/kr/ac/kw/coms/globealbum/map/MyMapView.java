package kr.ac.kw.coms.globealbum.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.TileSystem;
import org.osmdroid.util.TileSystemWebMercator;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class MyMapView extends org.osmdroid.views.MapView implements ILandmarkMapView {
    public Context context = null;

    ArrayList<MarkerTouchListener> markerListeners = new ArrayList<>(); //마커클릭 시 필요한 리스너를 모아둔다

    DiaryOverlayFolder diaries = null;

    private static boolean isConfigurationLoaded = false;

    // Constructor used by XML layout resource (uses default tile source).
    public MyMapView(Context context, AttributeSet attrs) {
        super(context, null, null, attrs);
        this.context = context;
        if (!isConfigurationLoaded) {
            // https://github.com/jeiea/globe-album/issues/9
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            String cfgExpirationExtend = "osmdroid.ExpirationExtendedDuration";
            long threeMonths = 90L * 24 * 60 * 60 * 1000;
            pref.edit().putLong(cfgExpirationExtend, threeMonths).apply();
            Configuration.getInstance().load(context, pref);
            isConfigurationLoaded = true;
        }
        mapConfiguration();
    }

    public MyMapView(final Context context) {
        this(context, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        double logZoom = getLogZoom(w, h);
        setMinZoomLevel(logZoom);   //최소 줌 조절
        //setMaxZoomLevel(5.0);   //최대 줌 조절
        getController().setZoom(logZoom);
    }

    //맵 초기 설정
    private void mapConfiguration() {

        //setTileSource(TileSourceFactory.BASE_OVERLAY_NL);    //맵 렌더링 설정
        setBuiltInZoomControls(false);
        setMultiTouchControls(true);
        TileSystem tileSystem = new TileSystemWebMercator();
        setScrollableAreaLimitLatitude(tileSystem.getMaxLatitude(), tileSystem.getMinLatitude(), 0);
        setScrollableAreaLimitLongitude(tileSystem.getMinLongitude(), tileSystem.getMaxLongitude(), 0);
        //맵 반복 방지
        setHorizontalMapRepetitionEnabled(false);
        setVerticalMapRepetitionEnabled(false);

        invalidate();
    }

    //맵뷰를 화면에 맞추기 위해 필요한 사전 작업
    public double getLogZoom(int width, int height) {
        double mapRatio = 1; // 타일은 정사각형.
        double dimenRatio = width / (double) height; // 화면비율
        int longAxis = dimenRatio < mapRatio ? height : width; // 긴 축을 구함
        double zoom = longAxis / 256.0;     //타일 하나의 픽셀수인 256으로 나눔

        return Math.log(zoom) / Math.log(2);
    }

    @NotNull
    @Override
    public MapView getMapView() {
        return this;
    }

    @NotNull
    @Override
    public IDiaryOverlay getDiaryOverlay() {
        if (diaries == null) {
            diaries = new DiaryOverlayFolder(this);
            getOverlays().add(diaries);
        }
        return diaries;
    }

    public interface MarkerTouchListener {
        void OnMarkerTouch(Marker marker);
    }

    //화면 터치 시 동작의 리스너를 등록
    public void setOnTouchMapViewListener(MarkerTouchListener listener) {
        markerListeners.add(listener);
    }

    //발생 이벤트를 전달
    public void dispatchMarkerTouch(Marker route, Marker marker) {
        for (MarkerTouchListener listener : markerListeners) {
            listener.OnMarkerTouch(marker);
        }
    }

    //현재 화면에 있는 마커의 개수 변경시 알려주는 리시버
    public void addShowCurrentMarkerChangeReceiver() {
        MapListener mapListener = new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                /*
                final List<OverlayItem> displayed = mMyLocationOverlay.getDisplayedItems();
                final StringBuilder buffer = new StringBuilder();
                String sep = "";
                for (final OverlayItem item : displayed) {
                    buffer.append(sep).append('\'').append(item.getAddress()).append('\'');
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

    public void deleteMapEventReceiver(MapEventsOverlay mapEventsOverlay) {
        Toast.makeText(context, "dd", Toast.LENGTH_SHORT).show();
        getOverlays().remove(mapEventsOverlay);
    }

}
