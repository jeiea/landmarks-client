package kr.ac.kw.coms.globealbum.map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import java.util.Date;


//https://gist.github.com/danielniko/775803 참고

public class DrawCircleOverlay extends Overlay {

    private Paint circlePainter;
    private Point userSelectedPoint;
    private Point answerPoint;
    private GeoPoint userSelectedGeopoint;
    private GeoPoint answerGeopoint;
    private MapView mapView;
    private long startTime;
    private double radius;
    private float ratio;

    private Handler drawCircleHandler = new Handler();

    public DrawCircleOverlay(GeoPoint userSelectedGeopoint, GeoPoint answerGeopoint, final MapView mapView) {
        this.userSelectedGeopoint = userSelectedGeopoint;
        this.answerGeopoint = answerGeopoint;
        this.mapView = mapView;

        userSelectedPoint = new Point();
        answerPoint = new Point();

        setCirclePainter();

        startTime = new Date().getTime();

        drawCircleHandler.post(new Runnable() {
            @Override
            public void run() {
                mapView.invalidate();
                long nowTime = new Date().getTime();
                long elapsed = nowTime - startTime;
                ratio = Math.min(1000, elapsed) / 1000.f;
                if (ratio < 1) {
                    drawCircleHandler.postDelayed(this, 1000 / 60);
                }
            }
        });
    }


    public void setCirclePainter() {
        // Set the painter to paint our circle. setColor = blue, setAlpha = 70 so the background
        // can still be seen. Feel free to change these settings
        circlePainter = new Paint();
        circlePainter.setAntiAlias(true);
        circlePainter.setStrokeWidth(2.0f);
        circlePainter.setColor(0xff6666ff);
        circlePainter.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePainter.setAlpha(70);
    }

    public void changeGeopointToPoint() {
        // Get projection from the mapView.
        //맵뷰에서 좌표를 화면에서 x,y좌표로 구해주는 Projection 클래스
        Projection projection = mapView.getProjection();

        //Project the gps coordinate to screen coordinate
        projection.toPixels(userSelectedGeopoint, userSelectedPoint);
        projection.toPixels(answerGeopoint, answerPoint);

        radius = Math.sqrt(Math.pow(Math.abs(answerPoint.x - userSelectedPoint.x), 2) + Math.pow(Math.abs(answerPoint.y - userSelectedPoint.y), 2));
    }

    @Override
    public void draw(Canvas c, MapView mapView, boolean shadow) {
        changeGeopointToPoint();


        // 여기서 반지름 계산
        c.drawCircle(userSelectedPoint.x, userSelectedPoint.y, (float) radius * ratio, circlePainter);
    }
}