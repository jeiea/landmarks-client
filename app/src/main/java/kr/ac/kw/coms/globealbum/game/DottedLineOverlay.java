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


/**
 * 정답 확인 시, 이동하는 점선 애니메이션을 그려줌
 */
public class DottedLineOverlay extends Overlay {

    private GeoPoint startGeoPoint;
    private GeoPoint endGeoPoint;
    private static final float[] dashInterval = new float[]{30, 30};

    private int i = 0;

    // Object pooling
    private Point start = new Point();
    private Point end = new Point();
    private Paint paint = new Paint();
    private Path path = new Path();

    void reset(GeoPoint start, GeoPoint end) {
        startGeoPoint = start;
        endGeoPoint = end;
    }

    DottedLineOverlay() {
        paintSetup();
        GeoPoint origin = new GeoPoint(0.0, 0.0);
        reset(origin, origin);
    }

    /**
     * 인자로 정보들을 받음
     *
     * @param startPoint 사용자가 선택한 시작점이 될 좌표
     * @param endPoint   정답 마커가 위치한 끝 좌표
     */
    DottedLineOverlay(GeoPoint startPoint, GeoPoint endPoint) {
        paintSetup();
        reset(startPoint, endPoint);
    }

    /**
     * 맵뷰의 좌표를 화면의 좌표로 변환
     */
    private void changeGeopointToPoint(MapView mapView, GeoPoint geoPoint, Point pt) {
        //맵뷰에서 좌표를 화면에서 x,y좌표로 구해주는 Projection 클래스
        Projection projection = mapView.getProjection();

        // Project the gps coordinate to screen coordinate
        projection.toPixels(geoPoint, pt);
    }

    private void paintSetup() {
        paint.setARGB(255, 13, 39, 89);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5.5f);
    }

    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {
        changeGeopointToPoint(osmv, startGeoPoint, start);
        changeGeopointToPoint(osmv, endGeoPoint, end);
        path.reset();
        path.moveTo(end.x, end.y);
        path.lineTo(start.x, start.y);
        paint.setPathEffect(new DashPathEffect(dashInterval, i));
        i += 2;

        c.drawPath(path, paint);
    }
}
