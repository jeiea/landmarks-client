package kr.ac.kw.coms.globealbum.diary;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.album.ResourcePicture;
import kr.ac.kw.coms.globealbum.provider.PictureProvider;

public class Diary_main extends AppCompatActivity {

    GroupDiaryView diaryImageView = null;
    GroupDiaryView diaryJourneyView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_main);
        diaryImageView = findViewById(R.id.diary_main_ImageList);
        diaryJourneyView = findViewById(R.id.diary_main_JourneyList);

        ArrayList<PictureGroup> elementList = new ArrayList<>();
        ArrayList<PictureProvider.Picture> elementRow = new ArrayList<>();
        Resources r = getResources();
        elementRow.add(new ResourcePicture(r, R.drawable.sample0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "0", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "1", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample2, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "2", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample3, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "3", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample4, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "4", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample5, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "5", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample6, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "6", Toast.LENGTH_SHORT).show();
            }
        }));

        elementList.add(new PictureGroup("group1", elementRow));
        diaryImageView.setGroups(elementList);
        elementList.add(new PictureGroup("group2", elementRow));
        diaryJourneyView.setGroups(elementList);
    }

    public void diary_onRadioClick(View view) {
        if (((RadioButton)findViewById(R.id.diary_main_RadioLeft)).isChecked())
        {
            //My 체크됨

        }
        else
        {
            //Others 체크됨
        }
    }

}
