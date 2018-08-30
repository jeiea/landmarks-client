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

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.provider.UriPicture;

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
                //팝업 표시
                findViewById(R.id.gallerydetail_menu).setVisibility(View.VISIBLE);
                break;
            case R.id.gallerydetail_detailMenu_btn_Delete:
                Toast.makeText(this, "asdf", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void gallerydetail_CloseMenu(View view)
    {
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
