package kr.ac.kw.coms.globealbum.album;

import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

import kr.ac.kw.coms.globealbum.provider.IPicture;

public class PictureArray extends ArrayList<IPicture> {

    public void addAndSort(IPicture picture) {
        add(picture);
        sort();
    }

    public void addAndSort(int index, IPicture picture) {
        add(index, picture);
        sort();
    }

    public void setOnClickListener(int index, View.OnClickListener onClickListener) throws IndexOutOfBoundsException {
        IPicture picture = get(index);
        set(index, picture);
    }

    public void setOnLongClickListener(int index, View.OnLongClickListener onLongClickListener) throws IndexOutOfBoundsException {
        IPicture picture = get(index);
        set(index, picture);
    }

    public void sort() {
        Collections.sort(this, new Comparator<IPicture>() {
            @Override
            public int compare(IPicture o1, IPicture o2) {
                Date t1 = Objects.requireNonNull(o1.getMeta().getTime());
                Date t2 = Objects.requireNonNull(o2.getMeta().getTime());
                return t1.compareTo(t2);
            }
        });
    }

    public int swap(int left, int right) {
        try {
            IPicture swap = get(left);
            set(left, get(right));
            set(right, swap);
        }
        catch (Exception e)
        {
            return -1;
        }
        return 0;
    }
}
