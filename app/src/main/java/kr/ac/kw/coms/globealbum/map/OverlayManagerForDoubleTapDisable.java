package kr.ac.kw.coms.globealbum.map;

import android.view.MotionEvent;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DefaultOverlayManager;
import org.osmdroid.views.overlay.TilesOverlay;

public class OverlayManagerForDoubleTapDisable extends DefaultOverlayManager {
    public OverlayManagerForDoubleTapDisable(TilesOverlay tilesOverlay) {
        super(tilesOverlay);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e, MapView pMapView) {
        return true;
    }
}
