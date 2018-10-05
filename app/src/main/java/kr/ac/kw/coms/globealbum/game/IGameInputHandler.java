package kr.ac.kw.coms.globealbum.game;

import android.view.View;
import android.widget.ImageView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;

import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.IPicture;

interface IGameInputHandler {
    void onSelectPicFirst(ImageView picImageView, View view);

    void onSelectPicSecond(ImageView picImageView,View view);

    void onSelectPosition(GeoPoint pos);

    void onPressStart();

    void onPressNext();

    void onPressRetry();

    void onPressExit();

    void onPressMarker(MyMapView myMapView,GeoPoint geoPoint);

}