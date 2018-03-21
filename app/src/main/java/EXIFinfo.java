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
