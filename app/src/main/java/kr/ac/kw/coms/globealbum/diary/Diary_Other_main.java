package kr.ac.kw.coms.globealbum.diary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import android.widget.TabHost;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.common.RecyclerItemClickListener;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;
import org.jetbrains.annotations.NotNull;

public class Diary_Other_main extends AppCompatActivity {

    GroupDiaryView diaryImageView = null;
    GroupDiaryView diaryJourneyView = null;
    ArrayList<Diary_Parcel> DiaryDataList = new ArrayList<>();

    public void ReceiveData()
    {
        DiaryDataList.add(new Diary_Parcel().add(new ResourcePicture(R.drawable.coord1, getResources()))
        .add(new ResourcePicture(R.drawable.coord2, getResources())));
        DiaryDataList.add(new Diary_Parcel().add(new ResourcePicture(R.drawable.coord2, getResources()))
                .add(new ResourcePicture(R.drawable.coord3, getResources())));
        DiaryDataList.add(new Diary_Parcel().add(new ResourcePicture(R.drawable.coord3, getResources()))
                .add(new ResourcePicture(R.drawable.coord4, getResources())));
    }

    public void PrepareData() {

        ArrayList<PictureGroup> elementList = new ArrayList<>();
        ArrayList<IPicture> pics = new ArrayList<>();
        for (int i=0;i<DiaryDataList.size();i++)
        {
            pics.clear();
            for (int j=0;j<DiaryDataList.get(i).Images.size();j++)
                pics.add(j, DiaryDataList.get(i).Images.get(j));
            elementList.add(new PictureGroup("Group" + (i + 1), (ArrayList<IPicture>) pics.clone()));
        }
        diaryJourneyView.setGroups(elementList);
        diaryJourneyView.addOnItemTouchListener(new RecyclerItemClickListener(diaryJourneyView)
        {
            @Override
            public void onItemClick(@NotNull View view, int position) {
                super.onItemClick(view, position);
            }

            @Override
            public void onLongItemClick(@NotNull View view, int position) {
                super.onLongItemClick(view, position);
            }
        }.getItemTouchListener());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_other_main);
        diaryJourneyView = findViewById(R.id.diary_other_main_JourneyList);

        TabHost tabHost = (TabHost)findViewById(R.id.diary_other_main_TabHost);
        tabHost.setup();
        TabHost.TabSpec ts1 = tabHost.newTabSpec("Tab_ImageList");
        ts1.setIndicator("이미지");
        ts1.setContent(R.id.diary_other_main_ImageList);
        TabHost.TabSpec ts2 = tabHost.newTabSpec("Tab_JourneyList");
        ts2.setIndicator("여행지");
        ts2.setContent(R.id.diary_other_main_JourneyList);

        ReceiveData();
        PrepareData();

        tabHost.addTab(ts1);
        tabHost.addTab(ts2);
    }

    public void diary_onBackClick(View view) {
        finish();
    }
}
