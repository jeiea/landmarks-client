package kr.ac.kw.coms.globealbum.diary;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import kr.ac.kw.coms.globealbum.album.GalleryDetail;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureArray;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.album.ResourcePicture;
import kr.ac.kw.coms.globealbum.provider.PictureProvider;

public class Diary_Mine_main extends AppCompatActivity {

    GroupDiaryView diaryImageView = null;
    GroupDiaryView diaryJourneyView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_mine_main);


        TabHost tabHost = (TabHost)findViewById(R.id.diary_mine_main_TabHost);
        tabHost.setup();
        prepareViewSample();

        TabHost.TabSpec ts1 = tabHost.newTabSpec("Tab_ImageList");
        ts1.setIndicator("내 이미지");
        ts1.setContent(R.id.diary_mine_main_ImageList);



        TabHost.TabSpec ts2 = tabHost.newTabSpec("Tab_JourneyList");
        ts2.setIndicator("내 여행지");
        ts2.setContent(R.id.diary_mine_main_JourneyList);


        tabHost.addTab(ts1);
        tabHost.addTab(ts2);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.d("TAB", tabId);
            }
        });
    }

    public void prepareViewSample() {
        diaryImageView = findViewById(R.id.diary_mine_main_ImageList);
        diaryJourneyView = findViewById(R.id.diary_mine_main_JourneyList);

        ArrayList<PictureGroup> elementList = new ArrayList<>();
        PictureArray elementRow = new PictureArray();
        Context c = getBaseContext();
        elementRow.add(new ResourcePicture(c, R.drawable.sample0, null));
        elementRow.add(new ResourcePicture(c, R.drawable.sample1, null));
        elementRow.add(new ResourcePicture(c, R.drawable.sample2, null));
        elementRow.sort();

        final ArrayList<String> urls = new ArrayList<>();

        for (PictureProvider.Picture i : elementRow) {
            urls.add(Diary_mapNPictures.resourceToUri(this, ((ResourcePicture) i).getid()).toString());
        }

        for (int i = 0; i < elementRow.size(); i++) {
            final int idx = i;
            elementRow.setOnClickListener(i, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getBaseContext(), GalleryDetail.class);
                    intent.putExtra("urls", urls);
                    intent.putExtra("index", idx);
                    startActivity(intent);
                }
            });
        }

        elementList.add(new PictureGroup("My Photos", elementRow));
        diaryImageView.setGroups(elementList);


        elementList.clear();
        elementRow.clear();
        elementRow.add(new ResourcePicture(c, R.drawable.sample0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_Mine_main.this, "0", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getBaseContext(), Diary_mapNPictures.class).putExtra("whose", "mine"));
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.sample1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_Mine_main.this, "1", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getBaseContext(), Diary_mapNPictures.class).putExtra("whose", "mine"));
            }
        }));

        elementList.add(new PictureGroup("group1", elementRow));
        elementList.add(new PictureGroup("group2", elementRow));
        diaryJourneyView.setGroups(elementList);
    }

    public void diary_onBackClick(View view) {
        finish();
    }
}
