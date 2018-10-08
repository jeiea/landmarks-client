package kr.ac.kw.coms.globealbum.diary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.provider.Diary;
import kr.ac.kw.coms.landmarks.client.IdCollectionInfo;

public class Diary_Edit extends AppCompatActivity {

    Diary diary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);
        TextView Title = findViewById(R.id.diary_edit_TitleText);
        TextView Description = findViewById(R.id.diary_edit_DescriptionText);
        RecyclerView ImageList = findViewById(R.id.diary_edit_ImageList);
        diary = LoadData();
        IdCollectionInfo data = diary.getInfo();
        Title.setText(data.getTitle());
        Description.setText(data.getText());
    }

    private Diary LoadData() {
        return (Diary) getIntent().getParcelableExtra("Data");
    }

    public void diary_edit_onClick(View view) {
    }
}
