package kr.ac.kw.coms.globealbum;

import android.media.ExifInterface;
import android.util.Log;

import java.util.Arrays;

public class EXIFinfo {
    public EXIFinfo()
    {
    }

    float[] getLocation(String Filename)
    {
        ExifInterface exif;
        try {
            exif = new ExifInterface(Filename);
            Log.d("SH_FILE", Filename);
        }
        catch (Exception e)
        {
            Log.d("SH", "ERROR");
            return null;
        }
        return getGPS(exif);
    }

    float[] getGPS(ExifInterface exif) {
        float[] location = new float[2];
        Log.d("SH", exif.getAttribute(ExifInterface.TAG_GPS_DEST_LATITUDE));
        exif.getLatLong(location);
        Log.d("SH_EXIF", Arrays.toString(location));
        return location;
    }
}
