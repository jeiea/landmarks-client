package kr.ac.kw.coms.globealbum.Map;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;

//마커와 마커 사이를 잇는 선들을 모아서 관리하는 클래스
//오버레이 삽입,삭제 한다.
public class MarkerLineFolderOverlay extends FolderOverlay{
    private Polygon polygon;
    private MapView mapView;
    private Context context;
    /** temporary 1x1 pix view where popup-menu is attached to */
    private View tempPopupMenuParentView = null;

    public MarkerLineFolderOverlay(MapView mapView,Context context){
        super();
        this.mapView = mapView;
        this.context=context;
    }

    //마커와 라인을 OverlayManager에 추가
    @Override
    public boolean add(Overlay item) {

        ((Marker)item).setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                showPopupMenu(marker.getPosition());
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

    //마커 클릭 시 팝업 메뉴를 띄운다
    private void showPopupMenu(GeoPoint geoPoint) {
        MenuInflater inflater = new MenuInflater(context);
        mapView.removeView(tempPopupMenuParentView);

        PopupMenu popupMenu = new PopupMenu(context, createTempPopupParentMenuView(geoPoint));

        inflater.inflate(R.menu.marker_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(context, "dd", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        popupMenu.show();
    }

    //팝업 메뉴 만드는 메서드
    private View createTempPopupParentMenuView(GeoPoint position) {
        if (tempPopupMenuParentView != null)mapView.removeView(tempPopupMenuParentView);
        tempPopupMenuParentView = new View(context);
        MapView.LayoutParams lp = new MapView.LayoutParams(
                1,
                1,
                position, MapView.LayoutParams.CENTER,
                0, 0);
        tempPopupMenuParentView.setVisibility(View.VISIBLE);
        mapView.addView(tempPopupMenuParentView, lp);
        return tempPopupMenuParentView;
    }
}
