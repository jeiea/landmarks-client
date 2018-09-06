package kr.ac.kw.coms.globealbum.game;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import java.util.Date;

import kr.ac.kw.coms.globealbum.map.MyMapView;

public class DottedLineOverlay extends Overlay {

    MyMapView myMapView;
    GeoPoint startGeoPoint, endGeoPoint;

    private Handler drawDottedLineHandler = new Handler();
    private long startTime;
    private float ratio;


    DottedLineOverlay(final MyMapView mapView,GeoPoint startPoint, GeoPoint endPoint){
        myMapView =  mapView;
        this.startGeoPoint = startPoint;
        this.endGeoPoint = endPoint;

        startTime = new Date().getTime();
        drawDottedLineHandler.post(new Runnable() { //반복 진행하면서 원 그리기
            @Override
            public void run() {
                long nowTime = new Date().getTime();
                long elapsed = nowTime - startTime;
                ratio = Math.min(1000, elapsed) / 1000.f;
                mapView.invalidate();
                if (ratio < 1) {
                    drawDottedLineHandler.postDelayed(this, 1000 / 60);
                }
            }
        });
    }


    private Point changeGeopointToPoint(GeoPoint geoPoint) {
        // Get projection from the mapView.
        //맵뷰에서 좌표를 화면에서 x,y좌표로 구해주는 Projection 클래스

        Projection projection = myMapView.getProjection();

        // Project the gps coordinate to screen coordinate
        Point point = new Point();
        projection.toPixels(geoPoint, point);

        return point;
    }

    boolean flag = false;
    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {
        Path mPath;
        mPath = new Path();
        Point startPoint = changeGeopointToPoint(startGeoPoint);
        Point endPoint = changeGeopointToPoint(endGeoPoint);
        mPath.moveTo(startPoint.x, startPoint.y);
        mPath.lineTo(endPoint.x, endPoint.y);
        Paint mPaint = new Paint();
        mPaint.setARGB(255, 0, 0, 0);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5.5f);
        if(flag){
            mPaint.setPathEffect(new DashPathEffect(new float[]{50, 50, 50}, 0));
        }
        else{
            mPaint.setPathEffect(new DashPathEffect(new float[]{50, 50, 50}, 1));
        }

        flag = !flag;
        c.drawPath(mPath, mPaint);
    }

}
