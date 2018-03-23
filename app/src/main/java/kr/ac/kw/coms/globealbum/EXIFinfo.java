package kr.ac.kw.coms.globealbum;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

import java.io.File;
import java.io.IOException;

public class EXIFinfo {
    Metadata metadata = null;
    public EXIFinfo(String Filename) //생성자, 파일 위치 지정
    {
        try {
            metadata = ImageMetadataReader.readMetadata(new File(Filename));
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public double[] getLocation() //경위도 데이터 읽기
    {
        GpsDirectory directory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        GeoLocation location = directory.getGeoLocation();
        double[] d_location = {location.getLatitude(), location.getLongitude()};
        return d_location;
    }
}
