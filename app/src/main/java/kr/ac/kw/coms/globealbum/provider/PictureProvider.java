package kr.ac.kw.coms.globealbum.provider;


import java.util.ArrayList;

/**
 * 사진 제공 클래스 프로토타입
 */
public abstract class PictureProvider {

    // 로컬 사진에서 무시할 사진을 설정한다.
    void setLocalIgnore(String pattern) {
    }

    // 로컬 사진 목록을 가져온다.
    public ArrayList<IPicture> getLocalPictureList() {
        return null;
    }

    // 찾아보기에서 사용할 서버 사진을 가져온다.
    public ArrayList<IPicture> getRemotePictures() {
        return null;
    }

    // 해당 유저의 서버 사진 목록을 가져온다.
    public ArrayList<IPicture> getRemoteUserPictures(int userId) {
        return null;
    }

    // 이 외에 퀴즈라든가 유저 정보 등을 통신할 메소드를
    // 구현하게 될 것.
}
