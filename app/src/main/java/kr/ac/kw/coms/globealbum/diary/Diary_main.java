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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import kotlin.Unit;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.provider.Diary;
import kr.ac.kw.coms.globealbum.provider.Promise;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.UIPromise;
import kr.ac.kw.coms.landmarks.client.AccountForm;
import kr.ac.kw.coms.landmarks.client.Remote;
import kr.ac.kw.coms.landmarks.client.WithIntId;

public class Diary_main extends AppCompatActivity {

    View Root;
    RecyclerView ImageList;
    RecyclerView JourneyList;
    RecyclerView.Adapter<DiaryListViewHolder> ImageListAdapter;
    RecyclerView.Adapter<DiaryListViewHolder> JourneyListAdapter;
    ImageButton BtnNewDiary;
    boolean isTabLeft = true;
    List<Diary> DownloadedDiaryList;

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

    public void diary_onBackClick(View view) {
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
        AdditiveAnimator.animate(JourneyList).setDuration(200).translationX(Root.getWidth()).start();
        AdditiveAnimator.animate(ImageList).setDuration(200).translationX(0).start();
        isTabLeft = true;
    }

    public void diary_main_SwitchToRight(View view) {
        if (!isTabLeft)
            return;
        final View bar = findViewById(R.id.diary_main_TabBar_HighLight);

        AdditiveAnimator.animate(bar).setDuration(200).translationX(bar.getWidth()).start();
        AdditiveAnimator.animate(ImageList).setDuration(200).translationX(-Root.getWidth()).start();
        AdditiveAnimator.animate(JourneyList).setDuration(200).translationX(0).start();
        isTabLeft = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.MakeNewDiary && resultCode == RESULT_OK)
        {
            //새로 등록된 다이어리 반영하기
        }
    }

    public void PrepareData() {
        //서버에서 데이터 다운로드
        RemoteJava.INSTANCE.login("login", "password", new UIPromise<WithIntId<AccountForm>>(){
            @Override
            public void success(WithIntId<AccountForm> result) {
                super.success(result);
                RemoteJava.INSTANCE.getMyCollections(new UIPromise<List<Diary>>()
                {
                    @Override
                    public void success(List<Diary> result) {
                        DownloadedDiaryList = result;
                        ShowData();
                    }

                    @Override
                    public void failure(@NotNull Throwable cause) {
                        Toast.makeText(Diary_main.this, "데이터 다운로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void failure(@NotNull Throwable cause) {
                Toast.makeText(Diary_main.this, "계정 정보 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void ShowData()
    {
        //준비된 데이터를 화면에 표시
        JourneyListAdapter = new DiaryListAdapter(this, DownloadedDiaryList);
        JourneyList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        JourneyList.setAdapter(JourneyListAdapter);
    }

    public class DiaryListAdapter extends RecyclerView.Adapter<DiaryListViewHolder>
    {
        private AppCompatActivity RootActivity;
        List<Diary> items;

        public DiaryListAdapter(AppCompatActivity RootActivity, List<Diary> items)
        {
            if (items == null)
            {
                Log.e("Diary_main", "Downloaded Data: NULL");
            }
            this.RootActivity = RootActivity;
            this.items = items;
        }

        @NonNull
        @Override
        public DiaryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(RootActivity.getBaseContext()).inflate(R.layout.layout_diary_row, parent, false);
            return new DiaryListViewHolder( view);
        }

        @Override
        public void onBindViewHolder(@NonNull DiaryListViewHolder holder, int position) {
            Diary diaryToShow = items.get(position);
            Glide.with(holder.image_Thumbnail).load(diaryToShow.get(0)).into(holder.image_Thumbnail);
            holder.text_Name.setText(diaryToShow.getTitle());
            Date StartTime = null;
            Date EndTime = null;
            for (int i=0;i<diaryToShow.getSize();i++)
            {
                Date time = diaryToShow.get(i).getMeta().getTime();
                if (StartTime == null || time.compareTo(StartTime) < 0)
                {
                    StartTime = time;
                }
                if (EndTime == null || time.compareTo(EndTime) > 0)
                {
                    EndTime = time;
                }
            }
            holder.text_Date.setText("0000-00-00 ~ 0000-00-00");
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public class DiaryListViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout Root;
        ImageView image_Thumbnail;
        TextView text_Name;
        TextView text_Date;
        int DiaryID;
        public DiaryListViewHolder(View itemView) {
            super(itemView);
            Root = (ConstraintLayout) itemView;
            image_Thumbnail = (ImageView) Root.getViewById(R.id.diary_Row_Thumbnail);
            text_Name = (TextView)Root.getViewById(R.id.diary_Row_Name);
            text_Date = (TextView)Root.getViewById(R.id.diary_Row_Date);
        }
    }
}
