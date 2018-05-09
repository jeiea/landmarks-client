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
import java.util.List;

import kr.ac.kw.coms.globealbum.R;

//마커와 마커 사이를 잇는 선들을 모아서 관리하는 클래스
//오버레이 삽입,삭제 한다.
public class MarkerLineFolderOverlay extends FolderOverlay{
    private MapView mapView;
    private Context context;
    /** temporary 1x1 pix view where popup-menu is attached to */
    private View tempPopupMenuParentView = null;

    public MarkerLineFolderOverlay(MyMapView mapView){
        super();
        this.mapView = mapView;
        this.context = mapView.context;
    }



    //마커만을 맵뷰에 나타내어 준다
    public boolean addMarker(Marker item){
        ((Marker)item).setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                showPopupMenu(marker.getPosition());
                return true;
            }
        });
        return super.add(item);
    }

    //마커와 라인(루트)을 OverlayManager에 추가
    public boolean addMarkerLine(Overlay item) {

        ((Marker) item).setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                showPopupMenu(marker.getPosition());
                ((MyMapView)mapView).dispatchMarkerTouch(MarkerLineFolderOverlay.this, marker);
                return true;
            }
        });

        int overlayListSize = getItems().size();
        if (overlayListSize > 0) {
            drawPolygon(getItems().get(overlayListSize - 1), item, overlayListSize);
        }
        return super.add(item);
    }
    //현재 사용 중인 마커를 반환
    public List<Marker> getMarkerList(){
        List<Marker> markerList = new ArrayList<Marker>();
        int size = getItems().size();

        for(int i = 0 ; i < size ; i=i+2){
            markerList.add((Marker)(getItems().get(i)));
        }
        return markerList;
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
                switch (menuItem.getItemId()){
                    case R.id.delete_marker:    //마커 하나 삭제
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
                        break;
                    case R.id.delete_marker_all:    //모든 마커 삭제
                        while( getItems().size() >0){
                            remove(getItems().get(0));
                        }
                        break;
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
