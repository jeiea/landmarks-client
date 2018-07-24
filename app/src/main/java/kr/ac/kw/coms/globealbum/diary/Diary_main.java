package kr.ac.kw.coms.globealbum.diary;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
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

    ActionBarDrawerToggle drawerToggle;
//https://medium.com/android-develop-android/android%EA%B0%9C%EB%B0%9C-7-%EB%84%A4%EB%B9%84%EA%B2%8C%EC%9D%B4%EC%85%98-%EB%93%9C%EB%A1%9C%EC%96%B4-nevigation-drawer-942534d5535d
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_main);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DrawerLayout drawerLayout = findViewById(R.id.diary_main_Root);
        prepareViewSample();

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    public void diary_onRadioClick(View view) {
        if (((RadioButton) findViewById(R.id.diary_main_RadioLeft)).isChecked()) {
            if (findViewById(R.id.diary_main_ImageList).getVisibility() == View.VISIBLE || findViewById(R.id.diary_main_JourneyList).getVisibility() == View.VISIBLE)
                return;
            findViewById(R.id.diary_main_ImageList).setVisibility(View.VISIBLE);
            findViewById(R.id.diary_main_Otherlist).setVisibility(View.GONE);
        } else {
            if (findViewById(R.id.diary_main_Otherlist).getVisibility() == View.VISIBLE) return;
            findViewById(R.id.diary_main_ImageList).setVisibility(View.GONE);
            findViewById(R.id.diary_main_JourneyList).setVisibility(View.GONE);
            findViewById(R.id.diary_main_Otherlist).setVisibility(View.VISIBLE);
        }
    }

    public void prepareViewSample() {
        diaryImageView = findViewById(R.id.diary_main_ImageList);
        diaryJourneyView = findViewById(R.id.diary_main_JourneyList);
        diaryOtherView = findViewById(R.id.diary_main_Otherlist);
        diaryOtherView.setNameTextSize(50);
        diaryOtherView.setNameBackgroundColor(0xffffffff);

        ArrayList<PictureGroup> elementList = new ArrayList<>();
        ArrayList<PictureProvider.Picture> elementRow = new ArrayList<>();
        Context c = getBaseContext();
        elementRow.add(new ResourcePicture(c, R.drawable.sample0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "0", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Diary_mapNPictures.class).putExtra("whose", "mine"));
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.sample1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "1", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Diary_mapNPictures.class).putExtra("whose", "mine"));
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.sample2, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "2", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Diary_mapNPictures.class).putExtra("whose", "mine"));
            }
        }));

        elementList.add(new PictureGroup("My Photos", elementRow));
        diaryImageView.setGroups(elementList);
        elementList.clear();
        elementList.add(new PictureGroup("group1", elementRow));
        elementList.add(new PictureGroup("group2", elementRow));
        diaryJourneyView.setGroups(elementList);
        elementList.clear();
        elementRow.clear();
        elementRow.add(new ResourcePicture(c, R.drawable.sample7, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_main.this, "Other", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Diary_mapNPictures.class).putExtra("whose", "other"));
            }
        }));
        elementList.add(new PictureGroup("Other", elementRow));
        diaryOtherView.setGroups(elementList);
    }

    public void diary_onBackClick(View view) {
        finish();
    }



    public void diary_openDrawer(View view) {
    }
}
