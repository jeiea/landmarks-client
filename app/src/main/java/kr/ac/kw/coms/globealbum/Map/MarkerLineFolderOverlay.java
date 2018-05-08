package kr.ac.kw.coms.globealbum.Map;

import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;

//마커와 마커 사이를 잇는 선들을 모아서 관리하는 클래스
//오버레이 삽입 삭제를 한다.
public class MarkerLineFolderOverlay extends FolderOverlay{
    private Polygon polygon;
    public MarkerLineFolderOverlay(){
        super();
    }

    @Override
    public boolean add(Overlay item) {

        ((Marker)item).setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                Log.d(marker.getPosition().getLatitude()+" " ,marker.getPosition().getLatitude()+" ");
                return true;
            }
        });

        int overlayListSize = getItems().size();
        if(overlayListSize > 0){
            polygon = new Polygon();
            polygon.setFillColor(android.graphics.Color.argb(75, 255,0,0));
            ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();

            Overlay overlay = getItems().get(overlayListSize-1);
            geoPoints.add(((Marker)overlay).getPosition());
            geoPoints.add(((Marker)item).getPosition());
            polygon.setPoints(geoPoints);
            super.add(polygon);
        }
        return super.add(item);
    }
}
