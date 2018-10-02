package kr.ac.kw.coms.globealbum.game;

import org.osmdroid.util.GeoPoint;

import kr.ac.kw.coms.globealbum.provider.IPicture;

interface IGameInputHandler {
    void onSelectPicture(IPicture selected);

    void onSelectPosition(GeoPoint pos);

    void onPressNext();

    void onPressRetry();

    void onPressExit();
}
