package kr.ac.kw.coms.globealbum.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.Toast;

import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.util.TileSystemWebMercator;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlin.jvm.functions.Function1;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.provider.IPicture;

public class MyMapView extends org.osmdroid.views.MapView implements ILandmarkMapView {
    public Context context = null;

    ArrayList<MarkerTouchListener> markerListeners = new ArrayList<>(); //마커클릭 시 필요한 리스너를 모아둔다
    MyMarker markerLineFolderOverlay = null;  //마커 모아서 관리

    private static boolean isConfigurationLoaded = false;

    // Constructor used by XML layout resource (uses default tile source).
    public MyMapView(final Context context, final AttributeSet attrs) {
        super(context, null, null, attrs);
        this.context = context;
        if (!isConfigurationLoaded) {
            // https://github.com/jeiea/globe-album/issues/9
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            String cfgExpirationExtend = "osmdroid.ExpirationExtendedDuration";
            long eightDays = 8 * 24 * 60 * 60 * 1000;
            pref.edit().putLong(cfgExpirationExtend, eightDays).apply();
            Configuration.getInstance().load(context, pref);
            isConfigurationLoaded = true;
        }
        this.post(new Runnable() {
                      @Override
                      public void run() {
                          mapConfiguration();

                      }
                  }
        );
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
        setScrollableAreaLimitLatitude(tileSystem.getMaxLatitude(), tileSystem.getMinLatitude() + 30, 0);
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


    //마커, 경로를 가지고 있는 markerLineFolderOverlay 객체를 반환
    public MyMarker getRoute() {
        return markerLineFolderOverlay;
    }

    public void deleteRoute(MyMarker folderOverlay) {

    }

    public void deleteRoute(int index) {

    }

    List<DiaryOverlays> groups = new ArrayList<>();
    List<DiaryOverlays> chains = new ArrayList<>();
    HashMap<IPicture, GlideTarget> targets = new HashMap<>();
    HashMap<Marker, IPicture> pictures = new HashMap<>();
    Function1<IPicture, Void> touchListener = new Function1<IPicture, Void>() {
        @Override
        public Void invoke(IPicture iPicture) {
            return null;
        }
    };

    private void addToThisAndMakeTargets(ArrayList<IPicture> ar, @NotNull List<? extends IPicture> list) {
        ar.clear();
        ArrayList<IPicture> newAr = new ArrayList<>();
        ar.addAll(list);
        for (IPicture pic : ar) {
            GlideApp.with(this).load(pic).into(new GlideTarget(pic));
        }
    }

    private void addTarget(GlideTarget target, IPicture pic) {
        targets.put(pic, target);
        pictures.put(target.marker, pic);
        getOverlays().add(target.marker);
    }

    private void removeTarget(GlideTarget target) {
        getOverlays().remove(target.marker);
        IPicture pic = pictures.remove(target.marker);
        targets.remove(pic);
    }

    class GlideTarget extends CustomViewTarget<MyMapView, Drawable> {
        IPicture picture;
        Marker marker;

        GlideTarget(IPicture pic) {
            super(MyMapView.this);
            picture = pic;
        }

        @Override
        protected void onResourceCleared(@Nullable Drawable placeholder) {
            removeTarget(this);
        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            removeTarget(this);
        }

        @Override
        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
            Marker m = new Marker(MyMapView.this);
            GeoPoint p = picture.getMeta().getGeo();
            if (p != null) {
                m.setPosition(p);
            }
            m.setImage(resource);
            m.setOnMarkerClickListener(onMarkerClick);
            marker = m;

            addTarget(this, picture);
        }
    }

    Marker.OnMarkerClickListener onMarkerClick = new Marker.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            if (touchListener != null) {
                touchListener.invoke(pictures.get(marker));
                return true;
            } else {
                return false;
            }
        }
    };

    @NotNull
    @Override
    public MapView getMapView() {
        return this;
    }

    @Override
    public void setGroups(@NotNull List<? extends List<? extends IPicture>> list) {
        ArrayList<DiaryOverlays> g = new ArrayList<>();
        for (List<? extends IPicture> group : list) {
            g.add(new DiaryOverlays(this, group, false));
        }
        for (DiaryOverlays group : groups) {
            group.detach();
        }
        groups = g;
    }

    @Override
    public void setChains(@NotNull List<? extends List<? extends IPicture>> list) {
        ArrayList<DiaryOverlays> g = new ArrayList<>();
        for (List<? extends IPicture> group : list) {
            g.add(new DiaryOverlays(this, group, true));
        }
        for (DiaryOverlays group : chains) {
            group.detach();
        }
        chains = g;
    }

    @NotNull
    @Override
    public List<List<IPicture>> getChains() {
        return getGroupsInternal(chains);
    }

    @NotNull
    @Override
    public List<List<IPicture>> getGroups() {
        return getGroupsInternal(groups);
    }

    @NonNull
    private static List<List<IPicture>> getGroupsInternal(List<DiaryOverlays> gs) {
        ArrayList<List<IPicture>> ppics = new ArrayList<>();
        for (DiaryOverlays g : gs) {
            ppics.add(new ArrayList<>(g.getPictures()));
        }
        return ppics;
    }

    @Override
    public void setOnTouchThumbnail(@NotNull Function1<IPicture, Void> listener) {
        touchListener = listener;
    }

    @NotNull
    @Override
    public Function1<IPicture, Void> getOnTouchThumbnail() {
        return touchListener;
    }

    @Override
    public void addToSelection(@NotNull IPicture picture) {

    }

    @Override
    public void removeFromSelection(@NotNull IPicture picture) {

    }

    @Override
    public void clearSelection() {

    }

    @Override
    public void fitZoomToMarkers() {
        ArrayList<Marker> markers = new ArrayList<>();

        for (Overlay o : getOverlays()) {
            if (o instanceof DiaryOverlays.PictureMarker) {
                markers.add((DiaryOverlays.PictureMarker) o);
            }
        }
        BoundingBox boundingBox1 = getBoundingBox();
        if (markers.size() == 1) {
            Marker item = markers.get(0);
            double lat = item.getPosition().getLatitude();
            double lon = item.getPosition().getLongitude();
            boundingBox1.set(lat + 5, lon + 5, lat - 5, lon - 5);
        } else {
            double minLat = +85.0f;
            double maxLat = -85.0f;
            double minLon = +180.0f;
            double maxLon = -180.0f;
            for (Marker item : markers) {
                GeoPoint point = item.getPosition();
                double lat = point.getLatitude();
                double lon = point.getLongitude();

                maxLat = Math.max(lat, maxLat);
                minLat = Math.min(lat, minLat);
                maxLon = Math.max(lon, maxLon);
                minLon = Math.min(lon, minLon);

            }
            boundingBox1.set(maxLat, maxLon, minLat, minLon);
        }
        zoomToBoundingBox(boundingBox1, false);

        //getController().zoomToSpan(boundingBox1.getLatitudeSpan(), boundingBox1.getLongitudeSpan());
        //getController().setCenter(boundingBox1.getCenterWithDateLine());

        getController().zoomOut();
        invalidate();
    }

    public interface MarkerTouchListener {
        void OnMarkerTouch(Marker marker);
    }

    //화면 터치 시 동작의 리스너를 등록
    public void setOnTouchMapViewListener(MarkerTouchListener listener) {
        markerListeners.add(listener);
    }

    //발생 이벤트를 전달
    public void dispatchMarkerTouch(MyMarker route, Marker marker) {
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
