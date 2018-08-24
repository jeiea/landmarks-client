package kr.ac.kw.coms.globealbum.game;

import org.osmdroid.util.GeoPoint;

/**
 *  사진에 필요한 정보를 가지고 있는 클래스
 */
public class GamePictureInfo {
    int id;             //사진의 drawable id
    GeoPoint geoPoint;  //사진 좌표
    String name;        //사진 지명
}
