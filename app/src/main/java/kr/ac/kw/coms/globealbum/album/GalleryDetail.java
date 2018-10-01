package kr.ac.kw.coms.globealbum.album;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.provider.IPicture;

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
        if (!intent.getAction().equals(RequestCodes.ACTION_SELECT_PHOTO))
        {
            findViewById(R.id.gallerydetail_btn_Select).setVisibility(View.INVISIBLE);
        }

        //이미지 스와이프 시 이벤트 구현
        findViewById(R.id.gallerydetail_Image).setOnTouchListener(swipeTouchListener);
    }

    private void reloadWithIndex() {
        IPicture pic = pictures.get(0);
        ImageView iv = findViewById(R.id.gallerydetail_Image);
        GlideApp.with(iv).load(pic).into(iv);
        ((TextView) findViewById(R.id.gallerydetail_imagename)).setText(pic.getTitle());
    }

    public void galleryselect_onClick(View view) {
        //사진 선택
        setResult(RESULT_OK, new Intent().putExtra("data", pictures.get(0)));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void gallerycancel_onClick(View view) {
        finish();
    }
}
