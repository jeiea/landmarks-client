package kr.ac.kw.coms.globealbum.diary;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.ProfileActivity;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.MediaScannerKt;
import kr.ac.kw.coms.globealbum.provider.EXIFinfo;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.LocalPicture;
import kr.ac.kw.coms.globealbum.provider.Promise;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.RemotePicture;


public class Diary_newImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_new_image);

    }

    @Override
    protected void onStart() {
        super.onStart();
        RecyclerView NewImageList = (RecyclerView) findViewById(R.id.diary_newImage_AddImageList);
        NewImageList.setHasFixedSize(true);
        NewImageList.setLayoutManager(new GridLayoutManager(Diary_newImage.this, 4));
        NewImageList.setAdapter(new NewImageAdapter(getImageFilePath()));
    }


    public void Common_Back_Click(View view) {
        super.onBackPressed();
    }

    public void Common_Profile_Click(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    private ArrayList<String> getImageFilePath() {
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
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data); //각 파일의 절대경로 구하기
            if (new EXIFinfo(absolutePathOfImage).hasLocation())
                listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

    private class NewImageAdapter extends RecyclerView.Adapter<NewImageAdapter.ViewHolder> {
        private ArrayList<String> urlList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public int DisplayWidth;
            public ImageView imageView;

            public ViewHolder(ImageView v) {
                super(v);
                imageView = v;
                DisplayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            }
        }

        public NewImageAdapter(ArrayList<String> urlList) {
            this.urlList = urlList;
        }

        @NonNull
        @Override
        public NewImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView v = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.inner_view, parent, false);
            NewImageAdapter.ViewHolder vh = new NewImageAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull NewImageAdapter.ViewHolder holder, int position) {
            final String url = urlList.get(position);
            Glide.with(holder.imageView).load(url).into(holder.imageView);
            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
            params.height = holder.DisplayWidth / 4;
            holder.imageView.setLayoutParams(params);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Glide.with(Diary_newImage.this).load(url).into((ImageView) findViewById(R.id.diary_ZoomIn_ZoomImage));
                    findViewById(R.id.diary_ZoomIn_ZoomName1).setVisibility(View.GONE);
                    findViewById(R.id.diary_ZoomIn_ZoomName2).setVisibility(View.GONE);
                    findViewById(R.id.diary_ZoomIn_Confirm).setVisibility(View.VISIBLE);

                    findViewById(R.id.diary_newImage_ZoomInLayout).setVisibility(View.VISIBLE);

                    findViewById(R.id.diary_ZoomIn_Confirm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            findViewById(R.id.diary_newImage_LoadingScreen).setVisibility(View.VISIBLE);
                            EXIFinfo exifInfo = new EXIFinfo(url);
                            try {
                                double[] location = exifInfo.getLocation();
                            } catch (NullPointerException e) {
                                Toast.makeText(Diary_newImage.this, "위치 정보를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            RemoteJava.INSTANCE.uploadPicture(new LocalPicture(url), new Promise<RemotePicture>()
                            {
                                @Override
                                public void success(RemotePicture result) {
                                    super.success(result);
                                    findViewById(R.id.diary_newImage_LoadingScreen).setVisibility(View.GONE);
                                    finish();
                                }

                                @Override
                                public void failure(@NotNull Throwable cause) {
                                    super.failure(cause);
                                    findViewById(R.id.diary_newImage_LoadingScreen).setVisibility(View.GONE);
                                    Toast.makeText(Diary_newImage.this, "업로드에 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return urlList.size();
        }
    }

    public void diary_ZoomIn_CloseZoomIn(View view) {
        findViewById(R.id.diary_newImage_ZoomInLayout).setVisibility(View.GONE);
    }
}
