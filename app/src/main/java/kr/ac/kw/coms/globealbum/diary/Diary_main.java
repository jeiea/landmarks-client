package kr.ac.kw.coms.globealbum.diary;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.OnSwipeTouchListener;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.common.RecyclerItemClickListener;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.provider.Diary;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;
import kr.ac.kw.coms.globealbum.provider.UIPromise;
import kr.ac.kw.coms.landmarks.client.IdAccountForm;

public class Diary_main extends AppCompatActivity {

    View Root;
    GroupDiaryView ImageList;
    RecyclerView JourneyList;
    RecyclerView.Adapter<DiaryListViewHolder> JourneyListAdapter;
    ImageButton BtnNewDiary;
    boolean isTabLeft = true;
    List<Diary> DownloadedDiaryList;
    List<IPicture> DownloadedImageList;
    int ZoomIndex;
    OnSwipeTouchListener swipeTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_main);

        Root = findViewById(R.id.diary_main_Root);
        ImageList = findViewById(R.id.diary_main_ImageList);
        JourneyList = findViewById(R.id.diary_main_JourneyList);
        BtnNewDiary = findViewById(R.id.diary_main_NewDiary);

        PrepareData();
    }

    public void Common_Back_Click(View view) {
        finish();
    }

    public void diary_main_CloseMenu(View view) {
        findViewById(R.id.diary_main_menuRoot).setVisibility(View.GONE);
    }

    public void diary_main_EditStart(View view) {
        findViewById(R.id.diary_main_menuRoot).setVisibility(View.GONE);
        Intent intent = new Intent(getBaseContext(), Diary_mapNPictures.class).setAction(RequestCodes.ACTION_EDIT_DIARY);
        startActivity(intent);
    }

    public void diary_main_Delete(View view) {
        //삭제 확인
        findViewById(R.id.diary_main_menuRoot).setVisibility(View.GONE);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //예
                        Toast.makeText(Diary_main.this, "DELETE", Toast.LENGTH_SHORT).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //아니오
                        Toast.makeText(Diary_main.this, "CANCEL", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("확실합니까?")
                .setPositiveButton("삭제", dialogClickListener)
                .setNegativeButton("취소", dialogClickListener)
                .show();
    }

    public void diary_main_AddNewDiary(View view) {
        Intent intent = new Intent(getBaseContext(), diary_new.class);
        startActivityForResult(intent, RequestCodes.MakeNewDiary);
    }

    public void diary_main_SwitchToLeft(View view) {
        if (isTabLeft)
            return;
        final View bar = findViewById(R.id.diary_main_TabBar_HighLight);

        AdditiveAnimator.animate(bar).setDuration(200).translationX(0).start();
        ImageList.setVisibility(View.VISIBLE);
        JourneyList.setVisibility(View.GONE);
        findViewById(R.id.diary_main_NewDiary).setVisibility(View.GONE);
        isTabLeft = true;
    }

    public void diary_main_SwitchToRight(View view) {
        if (!isTabLeft)
            return;
        final View bar = findViewById(R.id.diary_main_TabBar_HighLight);

        AdditiveAnimator.animate(bar).setDuration(200).translationX(bar.getWidth()).start();
        JourneyList.setVisibility(View.VISIBLE);
        ImageList.setVisibility(View.GONE);
        findViewById(R.id.diary_main_NewDiary).setVisibility(View.VISIBLE);
        isTabLeft = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.MakeNewDiary && resultCode == RESULT_OK) {
            //새로 등록된 다이어리 반영하기
        }
    }

    public void PrepareData() {
        //서버에서 데이터 다운로드
        RemoteJava.INSTANCE.login("login", "password", new UIPromise<IdAccountForm>() {
            @Override
            public void success(IdAccountForm result) {
                super.success(result);
                RemoteJava.INSTANCE.getMyPictures(new UIPromise<List<IPicture>>() {
                    @Override
                    public void success(List<IPicture> result) {
                        DownloadedImageList = result;
                        ShowImageData();
                    }

                    @Override
                    public void failure(@NotNull Throwable cause) {
                        Toast.makeText(Diary_main.this, "Image 데이터 다운로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });

                RemoteJava.INSTANCE.getMyCollections(new UIPromise<List<Diary>>() {
                    @Override
                    public void success(List<Diary> result) {
                        DownloadedDiaryList = result;
                        ShowDiaryData();
                    }

                    @Override
                    public void failure(@NotNull Throwable cause) {
                        Toast.makeText(Diary_main.this, "Diary 데이터 다운로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void failure(@NotNull Throwable cause) {
                Toast.makeText(Diary_main.this, "계정 정보 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void ShowImageData() {
        //준비된 Image 데이터를 화면에 표시
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
        if (DownloadedImageList.size() == 0)
        {
            DownloadedImageList.add(new ResourcePicture(R.drawable.imagenotfoundbordered));
            PictureGroup PictureRow = new PictureGroup("", (ArrayList<IPicture>) DownloadedImageList);
            List<PictureGroup> PictureList = new ArrayList<>();
            PictureList.add(PictureRow);
            ImageList.getPicAdapter().setSpan(1);
            ImageList.getPicAdapter().setVerticalPadding((int)px);
            ImageList.getPicAdapter().setHorizontalPadding((int)px);
            ImageList.setGroups(PictureList);
        }
        else {
            PictureGroup PictureRow = new PictureGroup("", (ArrayList<IPicture>) DownloadedImageList);
            List<PictureGroup> PictureList = new ArrayList<>();
            PictureList.add(PictureRow);
            ImageList.getPicAdapter().setVerticalPadding(0);
            ImageList.getPicAdapter().setHorizontalPadding(0);
            ImageList.setGroups(PictureList);
            ImageList.addOnItemTouchListener(new RecyclerItemClickListener(ImageList) {
                @Override
                public void onItemClick(@NotNull View view, int position) {
                    if (view instanceof ImageView) {
                        ZoomIndex = position - 1;
                        Glide.with(view).load(DownloadedImageList.get(ZoomIndex)).into(((ImageView) findViewById(R.id.diary_ZoomIn_ZoomImage)));
                        ((TextView) findViewById(R.id.diary_ZoomIn_ZoomName)).setText(DownloadedImageList.get(ZoomIndex).getMeta().getAddress());
                        findViewById(R.id.diary_ZoomIn_Root).setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onLongItemClick(@NotNull View view, int position) {
                    super.onLongItemClick(view, position);
                }
            }.getItemTouchListener());
            swipeTouchListener = new OnSwipeTouchListener(this.getBaseContext()) {
                @Override
                public void onSwipeRight() {
                    if (--ZoomIndex < 0)
                        ZoomIndex += DownloadedImageList.size();
                    Glide.with(findViewById(R.id.diary_ZoomIn_ZoomImage)).load(DownloadedImageList.get(ZoomIndex)).into((ImageView) findViewById(R.id.diary_ZoomIn_ZoomImage));
                    ((TextView) findViewById(R.id.diary_ZoomIn_ZoomName)).setText(DownloadedImageList.get(ZoomIndex).getMeta().getAddress());
                }

                @Override
                public void onSwipeLeft() {
                    if (++ZoomIndex == DownloadedImageList.size())
                        ZoomIndex = 0;
                    Glide.with(findViewById(R.id.diary_ZoomIn_ZoomImage)).load(DownloadedImageList.get(ZoomIndex)).into((ImageView) findViewById(R.id.diary_ZoomIn_ZoomImage));
                    ((TextView) findViewById(R.id.diary_ZoomIn_ZoomName)).setText(DownloadedImageList.get(ZoomIndex).getMeta().getAddress());

                }
            };
            findViewById(R.id.diary_ZoomIn_ZoomImage).setOnTouchListener(swipeTouchListener);
        }
    }

    public void ShowDiaryData() {
        //준비된 Diary 데이터를 화면에 표시
        JourneyListAdapter = new DiaryListAdapter(this, DownloadedDiaryList);
        JourneyList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        JourneyList.setAdapter(JourneyListAdapter);
    }

    public void diary_ZoomIn_CloseZoomIn(View view) {
        findViewById(R.id.diary_ZoomIn_Root).setVisibility(View.INVISIBLE);
    }

    public class DiaryListAdapter extends RecyclerView.Adapter<DiaryListViewHolder> {
        private AppCompatActivity RootActivity;
        List<Diary> items;

        public DiaryListAdapter(AppCompatActivity RootActivity, List<Diary> items) {
            if (items == null) {
                Log.e("Diary_main", "Downloaded Data: NULL");
            }
            this.RootActivity = RootActivity;
            this.items = items;
        }

        @NonNull
        @Override
        public DiaryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(RootActivity.getBaseContext()).inflate(R.layout.layout_diary_row, parent, false);
            return new DiaryListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DiaryListViewHolder holder, int position) {
            if (items == null)
            {
                Glide.with(holder.image_Thumbnail).load(R.drawable.diarynotfoundbordered).into(holder.image_Thumbnail);
                holder.NameTag.setVisibility(View.GONE);
                return;
            }
            final Diary diaryToShow = items.get(position);
            Glide.with(holder.image_Thumbnail).load(diaryToShow.get(0)).into(holder.image_Thumbnail);
            holder.text_Name.setText(diaryToShow.getTitle());

            Date StartTime = null;
            Date EndTime = null;
            for (int i = 0; i < diaryToShow.getSize(); i++) {
                Date time = diaryToShow.get(i).getMeta().getTime();
                if (StartTime == null || time.compareTo(StartTime) < 0) {
                    StartTime = time;
                }
                if (EndTime == null || time.compareTo(EndTime) > 0) {
                    EndTime = time;
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
            String date = sdf.format(StartTime) + " ~ " + sdf.format(EndTime);
            holder.text_Date.setText(date);
            holder.image_Thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Diary_main.this, Diary_mapNPictures.class);
                    intent.putExtra(Diary_mapNPictures.PARCEL_DIARY, diaryToShow);
                    intent.setAction(getIntent().getAction());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (items == null)
                return 1;
            if (items.size() == 0 || items.get(0).size() == 0)
            {
                items = null;
                return 1;
            }
            return items.size();
        }
    }

    public class DiaryListViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout Root;
        ImageView image_Thumbnail;
        ConstraintLayout NameTag;
        TextView text_Name;
        TextView text_Date;
        int DiaryID;

        public DiaryListViewHolder(View itemView) {
            super(itemView);
            Root = (ConstraintLayout) itemView;
            image_Thumbnail = (ImageView) Root.getViewById(R.id.diary_Row_Thumbnail);
            NameTag = (ConstraintLayout) Root.getViewById(R.id.diary_Row_NameTag);
            text_Name = (TextView) NameTag.getViewById(R.id.diary_Row_Name);
            text_Date = (TextView) NameTag.getViewById(R.id.diary_Row_Date);
        }
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.diary_ZoomIn_Root).getVisibility() == View.VISIBLE) {
            findViewById(R.id.diary_ZoomIn_Root).setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}
