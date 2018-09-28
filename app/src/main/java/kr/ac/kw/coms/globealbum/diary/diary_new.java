package kr.ac.kw.coms.globealbum.diary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.diary.Diary_Parcel;
import kr.ac.kw.coms.globealbum.diary.EditImageListAdapter;
import kr.ac.kw.coms.globealbum.provider.IPicture;

//새로 작성하는 화면. 여행지 목록에 플로팅 버튼을 통해 진입할 예정.
public class diary_new extends AppCompatActivity {
    Diary_Parcel DiaryData;
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

        DiaryData = new Diary_Parcel();
        RecyclerView Edit_ImageList = findViewById(R.id.diary_edit_ImageList);
        adapter = new EditImageListAdapter(this, DiaryData.Images);
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
                DiaryData.Title = ((TextView) findViewById(R.id.diary_edit_TitleText)).getText().toString();
                DiaryData.Description = ((TextView) findViewById(R.id.diary_edit_DescriptionText)).getText().toString();
                DiaryData.Images = adapter.getItems();
                DiaryData.Liked = false;
                //서버 통신 필요
                Toast.makeText(this, "작성 완료", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                break;
        }
    }
}
