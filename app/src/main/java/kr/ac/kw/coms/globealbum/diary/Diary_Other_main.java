package kr.ac.kw.coms.globealbum.diary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.PictureArray;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;

public class Diary_Other_main extends AppCompatActivity {

    GroupDiaryView diaryView = null;

    public void prepareData() {

        PictureArray ResourceList1 = new PictureArray();
        ResourceList1.add(new ResourcePicture(R.drawable.coord0));
        ResourceList1.add(new ResourcePicture(R.drawable.coord1));
        ResourceList1.add(new ResourcePicture(R.drawable.coord2));
        ResourceList1.sort();

        for (int i = 0;i < ResourceList1.size(); i++)
        {
            final int idx = i;
            ResourceList1.setOnClickListener(i, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Diary_Other_main.this, "" + idx, Toast.LENGTH_SHORT).show();
                }
            });
        }
        PictureGroup PictureRow = new PictureGroup("Other's group 1", ResourceList1);
        ArrayList<PictureGroup> PictureList = new ArrayList<>();
        PictureList.add(PictureRow);

        PictureArray ResourceList2 = new PictureArray();
        ResourceList2.add(new ResourcePicture(R.drawable.coord1));
        ResourceList2.add(new ResourcePicture(R.drawable.coord2));
        ResourceList2.add(new ResourcePicture(R.drawable.coord3));
        ResourceList2.sort();

        for (int i = 0;i < ResourceList2.size(); i++)
        {
            final int idx = i;
            ResourceList2.setOnClickListener(i, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Diary_Other_main.this, "" + idx, Toast.LENGTH_SHORT).show();
                }
            });
        }
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
