package kr.ac.kw.coms.globealbum;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    kr.ac.kw.coms.globealbum.EXIFinfo exifinfo;

    MapView map = null;
    IMapController mapController = null;

    Marker marker = null;  //지난번 클릭 마커 저장
    GeoPoint currentGeopoint = null;   //현재 위치 저장
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 573 && resultCode == RESULT_OK) //이미지 선택 완료
        {
            Uri uri = data.getData();
            String filePath = uri.getPath();
            exifinfo = new EXIFinfo(filePath);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >=23){ //시작 시 권한 처리
            checkPermissions();
        }

        //osmdroid 초기 구성
        Context ctx = getApplicationContext();
        org.osmdroid.config.Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = (MapView) findViewById(R.id.map);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        //Toast.makeText(ctx, width+" "+height, Toast.LENGTH_SHORT).show();

        mapConfiguration();
        //ReadImage();
    }


    private void mapConfiguration() {    //맵 생성 후 초기 설정
        map.setTileSource(TileSourceFactory.BASE_OVERLAY_NL);    //맵 렌더링 설정
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude, -TileSystem.MaxLatitude, 0);
        map.setScrollableAreaLimitLongitude(-TileSystem.MaxLongitude, TileSystem.MaxLongitude, 0);
        map.setMinZoomLevel(2.0);   //최소 줌 조절
        map.setMaxZoomLevel(6.0);   //최대 줌 조절

        mapController = map.getController();
        mapController.setZoom(0.0);

        MapEventsReceiver mReceiver = new MapEventsReceiver() { //화면 터치시 좌표 토스트메시지 출력, 좌표로 화면 이동
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Toast.makeText(getBaseContext(), p.getLatitude() + "-" + p.getLongitude(), Toast.LENGTH_SHORT).show();
                mapController.animateTo(p); //좌표로 화면 이동
                addMarker(p);

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {    //길게 터치시
                return false;
            }
        };
        MapEventsOverlay eventsOverlay = new MapEventsOverlay(getBaseContext(), mReceiver);
        map.getOverlays().add(eventsOverlay);
    }

    private void addMarker(GeoPoint p) {   //화면 터치시 마커를 화면에 표시
        map.getOverlays().remove(marker);
        marker = new Marker(map);
        marker.setPosition(p);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        map.getOverlays().add(marker);
    }

    private void ReadImage() { //이미지 선택
        Intent choosefile = new Intent(Intent.ACTION_GET_CONTENT);
        choosefile.setType("image/*");
        Intent intent = Intent.createChooser(choosefile, "SELECT FILE");
        startActivityForResult(intent, 573);
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume(); //osmdroid configuration refresh
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause(); //osmdroid configuration refresh
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            if( Build.VERSION.SDK_INT >=23){
                requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if( requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS){
            Map<String, Integer> perms = new HashMap<String, Integer>();

            perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.READ_EXTERNAL_STORAGE,PackageManager.PERMISSION_GRANTED);

            for (int i = 0; i < permissions.length; i++)
                perms.put(permissions[i], grantResults[i]);

            //권한 여부 확인
            Boolean location = perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            Boolean writeStorage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            Boolean readStorage = perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            if (location && writeStorage && readStorage) {
                Toast.makeText(MainActivity.this, "모든 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else { //권한이 거부된 경우
                Toast.makeText(this, "어플 실행을 위해선 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                finish();   //앱 종료
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
