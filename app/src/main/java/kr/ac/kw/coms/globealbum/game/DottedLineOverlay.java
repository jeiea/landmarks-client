package kr.ac.kw.coms.globealbum.game;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import kr.ac.kw.coms.globealbum.map.MyMapView;


/**
 * 정답 확인 시, 이동하는 점선 애니메이션을 그려줌
 */
public class DottedLineOverlay extends Overlay {

    private MyMapView myMapView;
    private GeoPoint startGeoPoint, endGeoPoint;

    /**
     * 인자로 정보들을 받음
     * @param mapView
     * @param startPoint 사용자가 선택한 시작점이 될 좌표
     * @param endPoint 정답 마커가 위치한 끝 좌표
     */
    DottedLineOverlay(final MyMapView mapView,GeoPoint startPoint, GeoPoint endPoint){
        myMapView =  mapView;
        this.startGeoPoint = startPoint;
        this.endGeoPoint = endPoint;
    }

    /**
     * 맵뷰의 좌표를 화면의 좌표로 변환
     */
    private Point changeGeopointToPoint(GeoPoint geoPoint) {
        //맵뷰에서 좌표를 화면에서 x,y좌표로 구해주는 Projection 클래스
        Projection projection = myMapView.getProjection();

        // Project the gps coordinate to screen coordinate
        Point point = new Point();
        projection.toPixels(geoPoint, point);

        return point;
    }


    private int i = 0 ;
    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {
        Path mPath;
        mPath = new Path();
        Point startPoint = changeGeopointToPoint(startGeoPoint);
        Point endPoint = changeGeopointToPoint(endGeoPoint);
        mPath.moveTo(endPoint.x, endPoint.y);
        mPath.lineTo(startPoint.x, startPoint.y);
        Paint mPaint = new Paint();
        mPaint.setARGB(255, 0, 0, 0);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5.5f);

        mPaint.setPathEffect(new DashPathEffect(new float[]{30, 30}, i));
        i += 2;

        c.drawPath(mPath, mPaint);
    }

}
