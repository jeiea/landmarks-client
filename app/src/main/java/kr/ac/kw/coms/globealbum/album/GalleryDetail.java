package kr.ac.kw.coms.globealbum.album;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.UrlPicture;

public class GalleryDetail extends AppCompatActivity {
    int index;
    ArrayList<IPicture> pictures;

    OnSwipeTouchListener swipeTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        swipeTouchListener = new OnSwipeTouchListener(this) {
            public void onSwipeRight() {
                index = (index - 1 + pictures.size()) % pictures.size();
                reloadWithIndex();
            }

            public void onSwipeLeft() {
                index = (index + 1) % pictures.size();
                reloadWithIndex();
            }
        };

        //이미지 데이터 불러오기
        //index: 선택한 이미지 번호 (int)
        //pictures: 사진들, ArrayList<IPicture>
        Intent intent = getIntent();
        index = intent.getIntExtra("index", 0);
        pictures = intent.getParcelableArrayListExtra("pictures");
        reloadWithIndex();

        //이미지 스와이프 시 이벤트 구현
        findViewById(R.id.gallerydetail_Image).setOnTouchListener(swipeTouchListener);
    }

    private void reloadWithIndex() {
        IPicture pic = pictures.get(index);
        ImageView iv = findViewById(R.id.gallerydetail_Image);
        GlideApp.with(iv).load(pic).into(iv);
        ((TextView) findViewById(R.id.gallerydetail_imagename)).setText(pic.getTitle());
    }

    public void gallerydetail_onClick(View view) {
        switch (view.getId()) {
            case R.id.gallerydetail_btn_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.gallerydetail_btn_detail:
                //팝업 표시
                findViewById(R.id.gallerydetail_menu).setVisibility(View.VISIBLE);
                break;
            case R.id.gallerydetail_detailMenu_btn_Delete:
                Toast.makeText(this, "asdf", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void gallerydetail_CloseMenu(View view) {
        findViewById(R.id.gallerydetail_menu).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {

        if (findViewById(R.id.gallerydetail_menu).getVisibility() == View.VISIBLE)
            findViewById(R.id.gallerydetail_menu).setVisibility(View.GONE);
        else
            super.onBackPressed();
    }
}
