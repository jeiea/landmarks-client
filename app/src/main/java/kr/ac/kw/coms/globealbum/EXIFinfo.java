package  kr.ac.kw.coms.globealbum;

import android.media.ExifInterface;

public class EXIFinfo {
    static EXIFinfo instance = null;
    ExifInterface exifInterface = null;
    public EXIFinfo getInstance()
    {
        if (instance == null)
            instance = new EXIFinfo();
        return instance;
    }
    public EXIFinfo()
    {
        getInstance();
    }

    float[] getLocation(String Filename)
    {
        float[] location = new float[2];
        try {
            exifInterface = new ExifInterface(Filename);
        }
        catch (Exception e)
        {
            return null;
        }
        exifInterface.getLatLong(location);
        return location;
    }
}
