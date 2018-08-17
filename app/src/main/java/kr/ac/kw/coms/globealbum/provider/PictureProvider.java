package kr.ac.kw.coms.globealbum.provider;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;

import com.bumptech.glide.RequestBuilder;

import java.util.ArrayList;
import java.util.Date;

/**
 * 사진 제공 클래스 프로토타입
 */
public abstract class PictureProvider {

    // 사진 하나를 나타냄
    public interface Picture {
        // 비트맵
        RequestBuilder<Drawable> getDrawable();

        // 클릭 리스너
        View.OnClickListener getOnClickListener();
        void setOnClickListener(View.OnClickListener onClickListener);

        View.OnLongClickListener getOnLongClickListener();
        void setOnLongClickListener(View.OnLongClickListener longClickListener);

        // 제목
        String getTitle();
        void setTitle(String title);

        // 생성시각
        Date getTime();

        // 위치
        Pair<Double, Double> getCoords();

        // 사진 삭제
        void delete();
    }

    // 로컬 사진에서 무시할 사진을 설정한다.
    void setLocalIgnore(String pattern) {
    }

    // 로컬 사진 목록을 가져온다.
    public ArrayList<Picture> getLocalPictureList() {
        return null;
    }

    // 찾아보기에서 사용할 서버 사진을 가져온다.
    public ArrayList<Picture> getRemotePictures() {
        return null;
    }

    // 해당 유저의 서버 사진 목록을 가져온다.
    public ArrayList<Picture> getRemoteUserPictures(int userId) {
        return null;
    }

    // 이 외에 퀴즈라든가 유저 정보 등을 통신할 메소드를
    // 구현하게 될 것.
}

class A extends RecyclerView {
   public A(Context c) {
       super(c);
   }
}