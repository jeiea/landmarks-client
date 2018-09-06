package kr.ac.kw.coms.globealbum.diary;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;

public class Diary_Parcel implements Serializable {
    //서버와의 통신에 쓰이는 데이터 형식 정의
    public String Title;
    public String Text;
    public ArrayList<Uri> Images;
    public boolean Liked;
}
