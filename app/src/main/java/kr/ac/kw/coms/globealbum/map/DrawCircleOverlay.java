package kr.ac.kw.coms.globealbum.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import java.util.Map;

import kr.ac.kw.coms.globealbum.game.GameActivity;


public class DrawCircleOverlay extends Overlay {

    private Paint circlePainter;
    private Point userSelectedPoint;
    private Point answerPoint;
    private GeoPoint userSelectedGeopoint;
    private GeoPoint answerGeopoint;
    private int meters;
    private MapView mapView;


    public void setUserGeopoint(GeoPoint geopoint){
        userSelectedGeopoint = geopoint;
    }
    public void setAnswerGeopoint(GeoPoint geopoint){
        answerGeopoint = geopoint;
    }

    public DrawCircleOverlay(GeoPoint userSelectedGeopoint, GeoPoint answerGeopoint, MapView mapView){
        this.userSelectedGeopoint=userSelectedGeopoint;
        this.answerGeopoint=answerGeopoint;
        this.mapView=mapView;

        userSelectedPoint = new Point();
        answerPoint = new Point();

        setCirclePainter();

    }

    public void setCirclePainter(){
        // Set the painter to paint our circle. setColor = blue, setAlpha = 70 so the background
        // can still be seen. Feel free to change these settings
        circlePainter = new Paint();
        circlePainter.setAntiAlias(true);
        circlePainter.setStrokeWidth(2.0f);
        circlePainter.setColor(0xff6666ff);
        circlePainter.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePainter.setAlpha(70);
    }
    public void changeGeopointToPoint(){
        // Get projection from the mapView.
        //맵뷰에서 좌표를 화면에서 x,y좌표로 구해주는 Projection 클래스
        Projection projection = mapView.getProjection();

        //Project the gps coordinate to screen coordinate
        projection.toPixels(userSelectedGeopoint, userSelectedPoint);
        projection.toPixels(answerGeopoint, answerPoint);


    }

    // hack to get more accurate radius, because the accuracy is changing as the location
    // getting further away from the equator
    public int metersToRadius(double latitude) {
        return (int) (mapView.getProjection().metersToEquatorPixels(meters) * (1/ Math.cos(Math.toRadians(latitude))));
    }

    @Override
    public void draw(Canvas c, MapView mapView, boolean shadow) {
        //int radius = metersToRadius(geoCurrentPoint.getLatitude() /1000000);
        // draw the blue circle
        changeGeopointToPoint();

        double radius  = Math.sqrt( Math.pow(Math.abs(answerPoint.x - userSelectedPoint.x),2) + Math.pow(Math.abs(answerPoint.y - userSelectedPoint.y),2) );
        c.drawCircle(userSelectedPoint.x, userSelectedPoint.y, (float)radius, circlePainter);
    }
}