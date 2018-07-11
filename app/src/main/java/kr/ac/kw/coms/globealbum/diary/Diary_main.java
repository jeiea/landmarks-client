package kr.ac.kw.coms.globealbum.diary;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.diary.Diary_mapNPictures;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.album.ResourcePicture;
import kr.ac.kw.coms.globealbum.provider.PictureProvider;

public class Diary_main extends AppCompatActivity {

    GroupDiaryView diaryImageView = null;
    GroupDiaryView diaryJourneyView = null;
    GroupDiaryView diaryOtherView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_main);
        diaryImageView = findViewById(R.id.diary_main_ImageList);
        diaryJourneyView = findViewById(R.id.diary_main_JourneyList);
        diaryOtherView = findViewById(R.id.diary_main_Otherlist);

        ArrayList<PictureGroup> elementList = new ArrayList<>();
        ArrayList<PictureProvider.Picture> elementRow = new ArrayList<>();
        Resources r = getResources();
        elementRow.add(new ResourcePicture(r, R.drawable.sample0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "0", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Diary_mapNPictures.class));
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "1", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Diary_mapNPictures.class));
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample2, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "2", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Diary_mapNPictures.class));
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample3, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "3", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Diary_mapNPictures.class));
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample4, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "4", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Diary_mapNPictures.class));
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample5, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "5", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Diary_mapNPictures.class));
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample6, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "6", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Diary_mapNPictures.class));
            }
        }));

        elementList.add(new PictureGroup("group1", elementRow));
        diaryImageView.setGroups(elementList);
        elementList.add(new PictureGroup("group2", elementRow));
        diaryJourneyView.setGroups(elementList);
        elementList.clear();
        elementList.add(new PictureGroup("OTHER", elementRow));
        diaryOtherView.setGroups(elementList);
    }

    public void diary_onRadioClick(View view) {
        if (((RadioButton)findViewById(R.id.diary_main_RadioLeft)).isChecked())
        {
            if (findViewById(R.id.diary_main_ImageList).getVisibility() == View.VISIBLE || findViewById(R.id.diary_main_JourneyList).getVisibility() == View.VISIBLE) return;
            findViewById(R.id.diary_main_ImageList).setVisibility(View.VISIBLE);
            findViewById(R.id.diary_main_Otherlist).setVisibility(View.GONE);
        }
        else
        {
            if (findViewById(R.id.diary_main_Otherlist).getVisibility() == View.VISIBLE) return;
            findViewById(R.id.diary_main_ImageList).setVisibility(View.GONE);
            findViewById(R.id.diary_main_JourneyList).setVisibility(View.GONE);
            findViewById(R.id.diary_main_Otherlist).setVisibility(View.VISIBLE);
        }
    }

    public void diary_onBackClick(View view) {
        finish();
    }
}
