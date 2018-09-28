package kr.ac.kw.coms.globealbum.diary;

import java.io.Serializable;
import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.provider.IPicture;

public class Diary_Parcel implements Serializable {
    //서버와의 통신에 쓰이는 데이터 형식 정의
    public String Title;
    public String Description;
    public ArrayList<IPicture> Images;
    public boolean Liked;

    public Diary_Parcel() {
        Title = "";
        Description = "";
        Images = new ArrayList<>();
        Liked = false;
    }

    @Override
    public Diary_Parcel clone() {
        Diary_Parcel Clone = new Diary_Parcel();
        Clone.Title = this.Title;
        Clone.Description = this.Description;
        Clone.Images = (ArrayList<IPicture>) this.Images.clone();
        Clone.Liked = this.Liked;
        return Clone;
    }

    public void set(Diary_Parcel value)
    {
        Title = value.Title;
        Description = value.Description;
        Images = value.Images;
        Liked = value.Liked;
    }
}
