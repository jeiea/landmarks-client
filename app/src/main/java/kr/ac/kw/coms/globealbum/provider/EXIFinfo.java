package kr.ac.kw.coms.globealbum.provider;
/* 작성자: 이상훈 */
import android.net.Uri;
import android.support.annotation.NonNull;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataReader;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import kr.ac.kw.coms.globealbum.R;

public class EXIFinfo {
    Metadata metadata = null;

    public EXIFinfo(){}
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
    public void setMetadata(InputStream inputStream){    //drawable에 있는 사진 파일 메타데이터 추출
        try {
            metadata = ImageMetadataReader.readMetadata(inputStream);
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

    public boolean hasLocation()
    {
        try{
            getLocation();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public GeoPoint getLocationGeopoint(){
        GpsDirectory directory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        GeoLocation location;
        try {
            location = directory.getGeoLocation();
        }
        catch (Exception e)
        {
            return new GeoPoint(0.0,0.0);
        }
        GeoPoint d_location = new GeoPoint(location.getLatitude(),location.getLongitude());
        return d_location;
    }

    public long getTimeTaken() //사진 촬영일 읽기(ms 단위의 Unix Time)
    {
        try {
            return metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class).getDateDigitized().getTime();
        }
        catch (NullPointerException e)
        {
            return 0;
        }
    }
}
