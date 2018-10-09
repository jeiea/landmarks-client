package kr.ac.kw.coms.globealbum;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.ac.kw.coms.globealbum.album.GalleryActivity;
import kr.ac.kw.coms.globealbum.album.activity_Navigator;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.diary.Diary_main;
import kr.ac.kw.coms.globealbum.game.GameActivity;
import kr.ac.kw.coms.globealbum.provider.EXIFinfo;
import kr.ac.kw.coms.globealbum.provider.LoginActivity;


public class MainActivity extends AppCompatActivity {
    EXIFinfo exifinfo;

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) { //시작 시 권한 처리
            checkPermissions();
        }
        setContentView(R.layout.activity_main);

//        MediaScannerKt.mediaScan(this);
    }

    //layout button click listener
    public void startOnClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_game_start:
                startActivity(new Intent(this, GameActivity.class));
                break;
            case R.id.btn_start_web_gallery:
                startActivityForResult(new Intent(this, GalleryActivity.class), 1);
                break;
            case R.id.btn_start_storage:
                startActivityForResult(new Intent(this, GalleryActivity.class), 2);
                break;
            case R.id.btn_start_navigator:
                startActivityForResult(new Intent(this, activity_Navigator.class), 3);
                break;
            case R.id.imageview_game_my_diary:
                startActivityForResult(new Intent(this, Diary_main.class).setAction(RequestCodes.ACTION_DIARY_MINE), 4);
                break;
            case R.id.imageview_game_other_diary:
                startActivityForResult(new Intent(this, Diary_main.class).setAction(RequestCodes.ACTION_DIARY_OTHERS), 4);
                break;
            default:
        }
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
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            Map<String, Integer> perms = new HashMap<String, Integer>();

            perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

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

    public void startLogin(View view) {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
