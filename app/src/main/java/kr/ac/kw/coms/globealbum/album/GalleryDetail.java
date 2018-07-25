package kr.ac.kw.coms.globealbum.album;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.nio.file.FileSystem;
import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.provider.PictureProvider;

public class GalleryDetail extends AppCompatActivity {
    int index;
    String[] mDataset;

    OnSwipeTouchListener swipeTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        swipeTouchListener = new OnSwipeTouchListener(this)
        {
            public void onSwipeRight() {
                index = (index - 1 + mDataset.length) % mDataset.length;
                UriPicture pic  = new UriPicture(Uri.parse(mDataset[index]), getBaseContext());
                pic.getDrawable().into((ImageView)findViewById(R.id.gallerydetail_Image));
                ((TextView)findViewById(R.id.gallerydetail_imagename)).setText(pic.getTitle());
            }

            public void onSwipeLeft() {
                index = (index + 1) % mDataset.length;
                UriPicture pic  = new UriPicture(Uri.parse(mDataset[index]), getBaseContext());
                pic.getDrawable().into((ImageView)findViewById(R.id.gallerydetail_Image));
                ((TextView)findViewById(R.id.gallerydetail_imagename)).setText(pic.getTitle());
            }

        };

        //이미지 데이터 불러오기
        //index: 선택한 이미지 번호 (int)
        //Dataset: 전체 이미지 집합 (String[])
        Intent intent = getIntent();
        index = intent.getIntExtra("index", 0);
        mDataset = intent.getStringArrayExtra("Dataset");
        if (mDataset == null)
            mDataset = intent.getStringArrayListExtra("urls").toArray(new String[0]);
        UriPicture pic  = new UriPicture(Uri.parse(mDataset[index]), this);
        pic.getDrawable().into((ImageView)findViewById(R.id.gallerydetail_Image));
        Log.i("URL", mDataset[index]);
        ((TextView)findViewById(R.id.gallerydetail_imagename)).setText(pic.getTitle());

        //이미지 스와이프 시 이벤트 구현
        findViewById(R.id.gallerydetail_Image).setOnTouchListener(swipeTouchListener);
    }

    public void gallerydetail_onClick(View view) {
        switch (view.getId())
        {
            case R.id.gallerydetail_btn_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.gallerydetail_btn_detail:
                break;
        }
    }
}
