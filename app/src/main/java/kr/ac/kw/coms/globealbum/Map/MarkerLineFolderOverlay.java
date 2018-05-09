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
            drawPolygon(getItems().get(overlayListSize-1),item,overlayListSize);
        }
        return super.add(item);
    }

    //마커와 마커 사이를 이어주는 직선을 만든다.
    //직선을 넣을 곳의 앞 뒤 마커를 인자와 인덱스를 전달
    private void drawPolygon(Overlay frontOverlay,Overlay backOverlay,int index){
        Polygon polygon = new Polygon();
        polygon.setFillColor(android.graphics.Color.argb(75, 255,0,0));
        ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();

        geoPoints.add(((Marker)frontOverlay).getPosition());
        geoPoints.add(((Marker)backOverlay).getPosition());
        polygon.setPoints(geoPoints);
        super.mOverlayManager.add(index,polygon);
    }

    //마커 클릭 시 팝업 메뉴를 띄운다
    private void showPopupMenu(final GeoPoint geoPoint) {
        MenuInflater inflater = new MenuInflater(context);
        mapView.removeView(tempPopupMenuParentView);

        PopupMenu popupMenu = new PopupMenu(context, createTempPopupParentMenuView(geoPoint));
        inflater.inflate(R.menu.marker_menu, popupMenu.getMenu());

        //팝업 메뉴에서 삭제 버튼 클릭 시 마커를 지우는 이벤트 리스너
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int size = getItems().size();

                if(size == 1){  //하나있는 마커 지우기
                    remove(getItems().get(0));
                }
                else {
                    for (int i = 0 ; i < size; i=i+2){
                        if(((Marker)getItems().get(i)).getPosition().getLatitude() == geoPoint.getLatitude() &&((Marker)getItems().get(i)).getPosition().getLongitude() == geoPoint.getLongitude()){
                            if( i == 0){    //시작 마커 지우기
                                remove(getItems().get(i+1));
                                remove(getItems().get(i));
                            }
                            else if ( i == size-1){     //끝 마커 지우기
                                remove(getItems().get(i));
                                remove(getItems().get(i-1));
                            }
                            else{       //중간 마커 지우기
                                remove(getItems().get(i+1));
                                remove(getItems().get(i));
                                remove(getItems().get(i-1));
                                drawPolygon(getItems().get(i-2),getItems().get(i-1),i-1);
                            }
                            break;
                        }
                    }
                }
                mapView.invalidate();
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
