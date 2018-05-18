package kr.ac.kw.coms.globealbum;
/* 작성자: 이상훈 */
import android.graphics.Bitmap;

public class Model {
    private String imagePath;
    private boolean isSelected = false;
    public Model(String imagePath)
    {
        this.imagePath = imagePath;
    }
    public String getImage()
    {
        return imagePath;
    }
    public void setSelected(Boolean selected)
    {
        isSelected = selected;
    }
    public boolean isSelected()
    {
        return isSelected;
    }
}
