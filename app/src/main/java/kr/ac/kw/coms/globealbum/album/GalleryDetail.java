package kr.ac.kw.coms.globealbum.album;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.provider.AccountActivity;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.Promise;

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
        if (intent.getAction().equals(RequestCodes.ACTION_VIEW_PHOTO)) {
            index = intent.getIntExtra("index", 0);
            pictures = intent.getParcelableArrayListExtra("pictures");
            reloadWithIndex();
            //이미지 스와이프 시 이벤트 구현
            findViewById(R.id.gallerydetail_Image).setOnTouchListener(swipeTouchListener);
        } else if (intent.getAction().equals(RequestCodes.ACTION_SELECT_PHOTO)) {
            index = 0;
            pictures = intent.getParcelableArrayListExtra("pictures");
            reloadWithIndex();
        }
    }

    private void reloadWithIndex() {
        IPicture pic = pictures.get(index);
        ImageView iv = findViewById(R.id.gallerydetail_Image);
        GlideApp.with(iv).load(pic).into(iv);
        ((TextView) findViewById(R.id.gallerydetail_imagename)).setText(pic.getMeta().getAddress());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void Common_Back_Click(View view) {
        finish();
    }

    public void Common_Profile_Click(View view) {
        startActivity(new Intent(this, AccountActivity.class));
    }

    public void gallerydetail_Share(View view) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".jpg");
        pictures.get(index).drawable(getResources(), new Promise<Drawable>() {
            @Override
            public void success(Drawable result) {
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    Bitmap bm = ((BitmapDrawable) result).getBitmap();
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(GalleryDetail.this, "{package_name}.fileprovider", file));
                startActivity(Intent.createChooser(intent, "사진 공유"));
            }
        });
    }

    public void gallerydetail_Send(View view) {
        //사진 업로드 확인 및 실행
    }
}
