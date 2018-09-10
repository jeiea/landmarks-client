package kr.ac.kw.coms.globealbum.diary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import kr.ac.kw.coms.globealbum.R;

public class Diary_Edit extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);
        TextView Title = findViewById(R.id.diary_edit_TitleText);
        TextView Description = findViewById(R.id.diary_edit_DescriptionText);
        RecyclerView ImageList = findViewById(R.id.diary_edit_ImageList);
        Diary_Parcel Data = LoadData();
        Title.setText(Data.Title);
        Description.setText(Data.Text);
    }

    private Diary_Parcel LoadData()
    {
        Diary_Parcel Data = (Diary_Parcel) getIntent().getSerializableExtra("Data");
        return Data;
    }

    public void diary_edit_onClick(View view) {
    }
}
