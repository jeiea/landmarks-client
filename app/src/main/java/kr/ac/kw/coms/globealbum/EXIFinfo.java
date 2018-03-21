package kr.ac.kw.coms.globealbum;

import android.media.ExifInterface;
import android.util.Log;

import java.util.Arrays;

public class EXIFinfo {
    ExifInterface exifInterface = null;
    public EXIFinfo()
    {
    }

    float[] getLocation(String Filename)
    {
        float[] location = new float[2];
        try {
            exifInterface = new ExifInterface(Filename);
            Log.d("SH_FILE", Filename);
        }
        catch (Exception e)
        {
            Log.d("SH", "ERROR");
            return null;
        }
        Log.d("SH", exifInterface.getAttribute(ExifInterface.TAG_GPS_DEST_LATITUDE));
        exifInterface.getLatLong(location);
        Log.d("SH_EXIF", Arrays.toString(location));
        return location;
    }
}
