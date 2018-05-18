package kr.ac.kw.coms.globealbum.album;
/* 작성자: 이상훈 */
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;

public class GalleryActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new GalleryAdapter(getImageFilePath()); //파일 목록을 인수로 제공할 것
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && ((GalleryAdapter)mAdapter).isMultiSelectMode())
        {
            ((GalleryAdapter)mAdapter).UnSelectAll();
            return true;
        }
        setResult(RESULT_CANCELED);
        return super.onKeyDown(keyCode, event);
    }

    private ArrayList<Model> getImageFilePath()
    {
        //이미지 파일 쿼리 및 resId 가져오기
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<Model> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        cursor = this.getContentResolver().query(uri, projection, null, null, null);
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while(cursor.moveToNext())
        {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(new Model(absolutePathOfImage));
        }
        return listOfAllImages;
    }

    public void gallery_onClick(View view) {
        switch (view.getId())
        {
            case R.id.btn_back:
                if (((GalleryAdapter)mAdapter).isMultiSelectMode())
                {
                    ((GalleryAdapter) mAdapter).UnSelectAll();
                }
                else
                {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;
            case R.id.btn_detail:
                View popupView = getLayoutInflater().inflate(R.layout.layout_gallerymenu, null);
                final PopupWindow mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
                Button Delete = (Button)findViewById(R.id.deleteAll);
                Button Send = (Button)findViewById(R.id.sendFiles);
                Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
                break;
        }
    }
}