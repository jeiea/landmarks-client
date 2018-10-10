package kr.ac.kw.coms.globealbum.map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;


//https://gist.github.com/danielniko/775803 참고

/**
 * 정답 확인 시, 사용자가 찍은 마커의 위치를 중심으로 정답 마커까지 원을 생성
 */
public class DrawCircleOverlay extends Overlay {

    private final static int msDuration = 1000;
    private final static int fps = 30;
    private Paint circlePainter;

    //정답 좌표를 화면의 좌표로 변경
    private Point userSelectedPoint = new Point();
    private Point answerPoint = new Point();

    private double radius;
    private float ratio;

    private long startTime;
    private GeoPoint userSelectedGeopoint;  //유저가 정답을 선택한 좌표
    private GeoPoint answerGeopoint;        //정답 좌표

    private Handler drawCircleHandler = new Handler();


    public DrawCircleOverlay() {
        setCirclePainter();
        GeoPoint origin = new GeoPoint(0.0, 0.0);
        resetCircle(origin, origin);
    }

    /**
     * 핸들러를 이용해 원 그리는 기본 설정
     *
     * @param userSelectedGeopoint 원의 중심이 될 사용자가 선택한 좌표
     * @param answerGeopoint       최대 반지름이 될 정답 좌표
     * @param mapView
     */
    public DrawCircleOverlay(GeoPoint userSelectedGeopoint, GeoPoint answerGeopoint, final MapView mapView) {
        setCirclePainter();
        resetCircle(userSelectedGeopoint, answerGeopoint);

        drawCircleHandler.post(new Runnable() { //반복 진행하면서 원 그리기
            @Override
            public void run() {
                updateRatio();
                mapView.invalidate();
                if (ratio < 1) {
                    drawCircleHandler.postDelayed(this, 1000 / fps);
                }
            }
        });
    }

    private void updateRatio() {
        long nowTime = SystemClock.uptimeMillis();
        long elapsed = nowTime - startTime;
        ratio = Math.min(1f, elapsed / (float) msDuration);
    }

    public void resetCircle(GeoPoint start, GeoPoint end) {
        startTime = SystemClock.uptimeMillis();
        userSelectedGeopoint = start;
        answerGeopoint = end;
    }

    /**
     * 그려질 원의 스타일, 색, 투명도 설정
     */
    private void setCirclePainter() {
        circlePainter = new Paint();
        circlePainter.setAntiAlias(true);
        circlePainter.setStrokeWidth(2.0f);
        circlePainter.setColor(0xff6666ff);
        circlePainter.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePainter.setAlpha(70);
    }

    /**
     * 맵뷰의 좌표를 화면의 좌표로 변환
     */
    private void changeGeoPointToPoint(MapView mapView) {
        // Get projection from the mapView.
        //맵뷰에서 좌표를 화면에서 x, y 좌표로 구해주는 Projection 클래스
        Projection projection = mapView.getProjection();

        //Project the gps coordinate to screen coordinate
        projection.toPixels(userSelectedGeopoint, userSelectedPoint);
        projection.toPixels(answerGeopoint, answerPoint);

        int xDiff = Math.abs(answerPoint.x - userSelectedPoint.x);
        int yDiff = Math.abs(answerPoint.y - userSelectedPoint.y);
        radius = Math.hypot(xDiff, yDiff);
    }

    /**
     * 원을 그림
     */
    @Override
    public void draw(Canvas c, MapView mapView, boolean shadow) {
        changeGeoPointToPoint(mapView);
        updateRatio();
        c.drawCircle(userSelectedPoint.x, userSelectedPoint.y, (float) radius * ratio, circlePainter);
    }
}