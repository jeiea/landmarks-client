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
    private Point answerPoint;      //정답 좌표를 화면의 좌표로 변경
    private GeoPoint userSelectedGeopoint;  //유저가 정답을 선택한 좌표
    private GeoPoint answerGeopoint;        //정답 좌표
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

        drawCircleHandler.post(new Runnable() { //반복 진행하면서 원 그리기
            @Override
            public void run() {
                long nowTime = new Date().getTime();
                long elapsed = nowTime - startTime;
                ratio = Math.min(1000, elapsed) / 1000.f;
                mapView.invalidate();
                if (ratio < 1) {
                    drawCircleHandler.postDelayed(this, 1000 / 60);
                }
            }
        });
    }


    public void setCirclePainter() {
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

        c.drawCircle(userSelectedPoint.x, userSelectedPoint.y, (float) radius * ratio, circlePainter); //원 그리기
    }
}