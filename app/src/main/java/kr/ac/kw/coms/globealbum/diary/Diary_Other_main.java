package kr.ac.kw.coms.globealbum.diary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.album.ResourcePicture;
import kr.ac.kw.coms.globealbum.provider.PictureProvider;

public class Diary_Other_main extends AppCompatActivity {

    GroupDiaryView diaryView = null;

    public void prepareData() {
        ArrayList<PictureProvider.Picture> ResourceList1 = new ArrayList<>();
        ResourceList1.add(new ResourcePicture(getBaseContext(), R.drawable.coord0, null));
        ResourceList1.add(new ResourcePicture(getBaseContext(), R.drawable.coord1, null));
        ResourceList1.add(new ResourcePicture(getBaseContext(), R.drawable.coord2, null));

        PictureGroup PictureRow = new PictureGroup("Other's group 1", ResourceList1);
        ArrayList<PictureGroup> PictureList = new ArrayList<>();
        PictureList.add(PictureRow);

        ArrayList<PictureProvider.Picture> ResourceList2 = new ArrayList<>();
        ResourceList2.add(new ResourcePicture(getBaseContext(), R.drawable.coord1, null));
        ResourceList2.add(new ResourcePicture(getBaseContext(), R.drawable.coord2, null));
        ResourceList2.add(new ResourcePicture(getBaseContext(), R.drawable.coord3, null));
        PictureRow = new PictureGroup("Other's group 2", ResourceList2);
        PictureList.add(PictureRow);

        diaryView.setGroups(PictureList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_other_main);
        diaryView = findViewById(R.id.diary_other_main_ImageList);

        prepareData();
    }

    public void diary_onBackClick(View view) {
        finish();
    }
}
