package kr.ac.kw.coms.globealbum;

import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.*;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class EXIFinfo {
    Metadata metadata = null;
    public EXIFinfo(String Filename)
    {
        metadata = new ImageMetadataReader.readMetadata(new File(Filename));
    }
    public double[] getLocation()
    {
        GpsDirectory directory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        GeoLocation location = directory.getGeoLocation();
        double[] d_location = {location.getLatitude(), location.getLongitude()};
        return d_location;
    }
}
