package kr.ac.kw.coms.globealbum.diary;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.RequestCodes;

public class Diary_main extends AppCompatActivity {

    View Root;
    RecyclerView ImageList;
    RecyclerView JourneyList;
    ImageButton BtnNewDiary;
    boolean isTabLeft = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_main);

        Root = findViewById(R.id.diary_main_Root);
        ImageList = findViewById(R.id.diary_main_ImageList);
        JourneyList = findViewById(R.id.diary_main_JourneyList);
        BtnNewDiary = findViewById(R.id.diary_main_NewDiary);

        PrepareData();
        ShowData();
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
    }

    public void ShowData()
    {
        //준비된 데이터를 화면에 표시
    }
}
