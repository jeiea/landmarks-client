package kr.ac.kw.coms.globealbum.album;

import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import kr.ac.kw.coms.globealbum.provider.PictureProvider.Picture;

public class PictureArray extends ArrayList<Picture> {

    public void addAndSort(Picture picture) {
        add(picture);
        sort();
    }

    public void addAndSort(int index, Picture picture) {
        add(index, picture);
        sort();
    }

    public void setOnClickListener(int index, View.OnClickListener onClickListener) throws IndexOutOfBoundsException {
        Picture picture = get(index);
        picture.setOnClickListener(onClickListener);
        set(index, picture);
    }

    public void sort() {
        Collections.sort(this, new Comparator<Picture>() {
            @Override
            public int compare(Picture o1, Picture o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });
    }
}
