package kr.ac.kw.coms.globealbum.album;
/* 작성자: 이상훈 */
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.ProfileActivity;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.MediaScannerKt;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.provider.LocalPicture;

public class GalleryActivity extends AppCompatActivity {
    private RecyclerView ImageList;
    private RecyclerView.Adapter ImageListAdapter;
    private RecyclerView.LayoutManager ImageListLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        ImageList = (RecyclerView)findViewById(R.id.gallery_ImageList);
        ImageList.setHasFixedSize(true);
        ImageListLayoutManager = new GridLayoutManager(this, 3);
        ImageList.setLayoutManager(ImageListLayoutManager);
        ImageListAdapter = new GalleryAdapter(getImageFilePath(), getIntent().getAction()); //파일 목록을 인수로 제공할 것
        ImageList.setAdapter(ImageListAdapter);
    }

    private ArrayList<String> getImageFilePath()
    {
        //이미지 파일 쿼리 및 resId 가져오기
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        MediaScannerKt.mediaScan(getBaseContext());
        ArrayList<String> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        cursor = this.getContentResolver().query(uri, projection, null, null, null);
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while(cursor.moveToNext())
        {
            absolutePathOfImage = cursor.getString(column_index_data); //각 파일의 절대경로 구하기
            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

    public void diary_ZoomIn_CloseZoomIn(View view) {
        findViewById(R.id.diary_ZoomIn_Root).setVisibility(View.GONE);
    }


    class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
        private ArrayList<String> mDataset;
        String Mode;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView mImageView;
            public int DisplayWidth;

            public ViewHolder(ImageView v) {
                super(v);
                mImageView = v;
                DisplayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            }
        }

        public GalleryAdapter(ArrayList<String> Dataset, String Action) {
            mDataset = Dataset;
            if (Action.equals(RequestCodes.ACTION_SELECT_PHOTO) || Action.equals(RequestCodes.ACTION_VIEW_PHOTO))
                Mode = Action;
        }

        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            ImageView v = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.inner_view, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final String Url = mDataset.get(position);
            Glide.with(holder.mImageView).load(Url).into(holder.mImageView);
            ViewGroup.LayoutParams params = holder.mImageView.getLayoutParams();
            params.height = holder.DisplayWidth / 3;
            holder.mImageView.setLayoutParams(params);

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //이미지 확대
                    Glide.with(findViewById(R.id.diary_ZoomIn_Root)).load(Url).into((ImageView) findViewById(R.id.diary_ZoomIn_ZoomImage));
                    ((TextView)findViewById(R.id.diary_ZoomIn_ZoomName)).setText("");
                    findViewById(R.id.diary_ZoomIn_Root).setVisibility(View.VISIBLE);
                    if (Mode.equals(RequestCodes.ACTION_VIEW_PHOTO)) {
                        //단순 열람 모드
                        findViewById(R.id.diary_ZoomIn_Confirm).setVisibility(View.GONE);
                    } else if (Mode.equals(RequestCodes.ACTION_SELECT_PHOTO)) {
                        //파일 선택 모드
                        findViewById(R.id.diary_ZoomIn_Confirm).setVisibility(View.VISIBLE);
                        findViewById(R.id.diary_ZoomIn_Confirm).setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                //사진 추가
                                setResult(RESULT_OK, new Intent().putExtra("data", new LocalPicture(Url)).setAction(RequestCodes.ACTION_SELECT_PHOTO));
                                finish();
                                return true;
                            }
                        });
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataset == null ? 0 : mDataset.size();
        }

    }

    public void Common_Back_Click(View v)
    {
        finish();
    }

    public void Common_Profile_Click(View view){
        startActivity(new Intent(this, ProfileActivity.class));
    }
}