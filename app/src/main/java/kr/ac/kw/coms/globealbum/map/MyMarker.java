package kr.ac.kw.coms.globealbum.map;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DefaultOverlayManager;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.GroundOverlay2;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

import kr.ac.kw.coms.globealbum.R;

//마커와 마커 사이를 잇는 선들을 모아서 관리하는 클래스
//오버레이 삽입,삭제 한다.
public class MyMarker extends FolderOverlay{
    private MapView mapView;
    private Context context;
    /** temporary 1x1 pix view where popup-menu is attached to */
    private View tempPopupMenuParentView = null;
    public Marker clickedMarker = null;

    final int ONLYMARKER =1;
    final int MARKER_LINE=2;

    final int FIRST_MARKER=11;
    final int SHOWN_MARKER=12;

    private  int curMode=ONLYMARKER;

    public MyMarker(MyMapView mapView){
        super();
        this.mapView = mapView;
        this.context = mapView.context;
    }

    public void setMode(int mode){
        curMode = mode;
    }

    //마커만을 맵뷰에 나타내어 준다
    public boolean addMarker(Marker item){
        if(clickedMarker == null){
            ((Marker)item).setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    showPopupMenu(marker.getPosition(),SHOWN_MARKER);
                    ((MyMapView)mapView).dispatchMarkerTouch(MyMarker.this, marker);
                    return true;
                }
            });
        }

        return super.add(item);
    }

    //마커와 라인(루트)을 OverlayManager에 추가
    public boolean addMarkerLine(Overlay item) {
        /*
        ((Marker) item).setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                showPopupMenu(marker.getPosition(),SHOWN_MARKER);
                ((MyMapView)mapView).dispatchMarkerTouch(MyMarker.this, marker);
                return true;
            }
        });
        */
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

    //마커의 순서를 바꿔주는 메서드, 인자는 짝수여야함
    public void moveMarker(int from, int to) {
        if(from %2 != 0 || to % 2 !=0){ //짝수여야함
            Toast.makeText(context, "wrong", Toast.LENGTH_SHORT).show();
            return;
        }
        int size=getItems().size();
        if(size< 1 || from > size || to > size ){   //인자가 마커 인덱스보다 크면 안됨
            Toast.makeText(context, "wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        OverlayManager tmpOverlayManager;
        tmpOverlayManager = new DefaultOverlayManager(null);

        for(int i = 0 ; i < mOverlayManager.size(); i=i+2){
            Overlay overlay = mOverlayManager.get(i);
            if( i == from ){
                overlay = mOverlayManager.get(to);
            }
            else if ( i == to){
                overlay = mOverlayManager.get(from);
            }
            else{
                tmpOverlayManager.add(overlay);
            }
        }
        for ( Overlay overlay: mOverlayManager) {
            mOverlayManager.remove(overlay);
        }
        for ( Overlay overlay: tmpOverlayManager) {
            addMarkerLine(overlay);
        }
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
    private void showPopupMenu(final GeoPoint geoPoint,int flag) {
        MenuInflater inflater = new MenuInflater(context);
        mapView.removeView(tempPopupMenuParentView);

        PopupMenu popupMenu = new PopupMenu(context, createTempPopupParentMenuView(geoPoint));
        inflater.inflate(R.menu.marker_menu, popupMenu.getMenu());

        //메뉴가 띄워졌을 때, 메뉴아이템 종류 설정
        switch (flag){
            case FIRST_MARKER:
                popupMenu.getMenu().getItem(0).setVisible(true);
                popupMenu.getMenu().getItem(1).setVisible(true);
                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu popupMenu) {
                        //removeMarker(geoPoint);
                        Toast.makeText(context, "closed", Toast.LENGTH_SHORT).show();
                        mapView.invalidate();
                    }
                });
                break;
            case SHOWN_MARKER:
                popupMenu.getMenu().getItem(1).setVisible(true);
                popupMenu.getMenu().getItem(2).setVisible(true);
                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu popupMenu) {
                        return;
                    }
                });
                break;

        }
        //팝업 메뉴에서 클릭 시 리스너
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.add_marker:
                        break;
                    case R.id.delete_marker:    //마커 하나 삭제
                        if( curMode ==ONLYMARKER){  //마커만 있는 경우
                            removeMarker(geoPoint);
                        }
                        else if(curMode ==MARKER_LINE){ //마커와 선이 같이 있는 경우
                            removeMarkerLine(geoPoint);
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
    //좌표에 맞는 마커와 선분을 지우고 인덱스를 반환
    private int removeMarkerLine(GeoPoint geoPoint){
        int size = getItems().size();
        int index =-1;

        if(size == 1){  //하나있는 마커 지우기
            remove(getItems().get(0));
            index=0;
        }
        else {
            for (int i = 0 ; i < size; i=i+2){
                if(((Marker)getItems().get(i)).getPosition().getLatitude() == geoPoint.getLatitude() &&((Marker)getItems().get(i)).getPosition().getLongitude() == geoPoint.getLongitude()){
                    if( i == 0){    //시작 마커 지우기
                        remove(getItems().get(i+1));
                        remove(getItems().get(i));
                        index=i;
                    }
                    else if ( i == size-1){     //끝 마커 지우기
                        remove(getItems().get(i));
                        remove(getItems().get(i-1));
                        index=i;
                    }
                    else{       //중간 마커 지우기
                        remove(getItems().get(i+1));
                        remove(getItems().get(i));
                        remove(getItems().get(i-1));
                        drawPolygon(getItems().get(i-2),getItems().get(i-1),i-1);
                        index=i;
                    }
                    break;
                }
            }
        }
        return index;
    }
    //좌표에 맞는 마커를 지우고 인덱스를 반환
    private int removeMarker(GeoPoint geoPoint){
        int size = getItems().size();
        Toast.makeText(context, "remove marker", Toast.LENGTH_SHORT).show();
        for (int i = 0 ; i < size; i=i+1){
            if(((Marker)getItems().get(i)).getPosition().getLatitude() == geoPoint.getLatitude() &&((Marker)getItems().get(i)).getPosition().getLongitude() == geoPoint.getLongitude()){
                remove(getItems().get(i));
                return i;
            }
        }
        return -1;
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