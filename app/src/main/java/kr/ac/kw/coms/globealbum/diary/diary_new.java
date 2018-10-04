package kr.ac.kw.coms.globealbum.diary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.provider.Diary;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.Promise;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.RemotePicture;
import kr.ac.kw.coms.globealbum.provider.UIPromise;
import kr.ac.kw.coms.landmarks.client.CollectionInfo;

//새로 작성하는 화면. 여행지 목록에 플로팅 버튼을 통해 진입할 예정.
public class diary_new extends AppCompatActivity {
    CollectionInfo diary;
    EditImageListAdapter adapter;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.SelectNewPhoto && resultCode == RESULT_OK) {
            adapter.AddNewPicture((IPicture) data.getParcelableExtra("data"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_new);

        diary = new CollectionInfo();
        RecyclerView Edit_ImageList = findViewById(R.id.diary_edit_ImageList);
        adapter = new EditImageListAdapter(this, new ArrayList<IPicture>());
        Edit_ImageList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        Edit_ImageList.setAdapter(adapter);
    }

    public void diary_edit_onClick(View view) {
        //새로 쓰기 화면에서 뒤로/저장 버튼 클릭 시
        switch (view.getId()) {
            case R.id.diary_edit_btnBack:
                Toast.makeText(this, "작성 취소", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.diary_edit_btnSave:
                diary.setTitle(((TextView) findViewById(R.id.diary_edit_TitleText)).getText().toString());
                diary.setText(((TextView) findViewById(R.id.diary_edit_DescriptionText)).getText().toString());
                ArrayList<Integer> imgIds = new ArrayList<>();
                for (IPicture p : adapter.getItems()) {
                    imgIds.add(((RemotePicture) p).getInfo().getId());
                }
                diary.setImages(imgIds);
                diary.setLiking(false);

                //서버 통신 필요
                RemoteJava.INSTANCE.uploadCollection(diary, afterUpload);
                break;
        }
    }

    Promise<Diary> afterUpload = new UIPromise<Diary>() {
        @Override
        public void success(Diary result) {
            Toast.makeText(diary_new.this, "작성 완료", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void failure(@NotNull Throwable cause) {
            super.failure(cause);
        }
    };
}
