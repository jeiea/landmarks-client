package kr.ac.kw.coms.globealbum.Map;

import org.mapsforge.core.graphics.Color;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;

public class MyMarker {
    private Marker marker;
    private GeoPoint geoPoint;
    private Polygon polygon;
    MyMarker(Marker marker,GeoPoint geoPoint){
        this.marker = marker;
        this.geoPoint = geoPoint;
    }
    MyMarker(Marker marker,GeoPoint geoPoint,Polygon polygon){
        this.marker = marker;
        this.geoPoint = geoPoint;
        this.polygon=polygon;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
    public void setMarker(Marker marker) {
        this.marker = marker;
    }
    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }
    public void setPolygon(GeoPoint formerGeopoint ,GeoPoint nowGeoPoint) {
        polygon = new Polygon();
        polygon.setFillColor(android.graphics.Color.argb(75, 255,0,0));

        ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
        geoPoints.add(formerGeopoint);
        geoPoints.add(nowGeoPoint);
        polygon.setPoints(geoPoints);
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }
    public Marker getMarker() {
        return marker;
    }
    public Polygon getPolygon() {
        return polygon;
    }
}
