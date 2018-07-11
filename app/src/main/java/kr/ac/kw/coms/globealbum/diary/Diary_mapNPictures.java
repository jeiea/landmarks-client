package kr.ac.kw.coms.globealbum.diary;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.album.ResourcePicture;
import kr.ac.kw.coms.globealbum.diary.Diary_main;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.PictureProvider;

public class Diary_mapNPictures extends AppCompatActivity {

    MyMapView mapView = null;
    GroupDiaryView picView = null;
    WebView JourneyInfoView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_map_n_pictures);

        mapView = findViewById(R.id.diary_mapNpics_Map);
        picView = findViewById(R.id.diary_mapNpics_Pics);
        JourneyInfoView = findViewById(R.id.diary_mapNpics_Read);

        ArrayList<PictureGroup> elementList = new ArrayList<>();
        ArrayList<PictureProvider.Picture> elementRow = new ArrayList<>();
        Resources r = getResources();
        elementRow.add(new ResourcePicture(r, R.drawable.sample0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "0", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "1", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample2, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "2", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample3, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "3", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample4, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "4", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample5, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "5", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(r, R.drawable.sample6, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "6", Toast.LENGTH_SHORT).show();
            }
        }));

        elementList.add(new PictureGroup("", elementRow));
        picView.setGroups(elementList);
        picView.setOrientation(1);

        JourneyInfoView.loadData("<html><body><h1>empty title</h1><hr>empty body</body></html>", "text/html", "UTF-8");
    }

    public void diary_onSaveClick(View view) {
        String title = ((TextView)findViewById(R.id.diary_mapNpics_EditTitle)).getText().toString();
        String body = ((TextView)findViewById(R.id.diary_mapNpics_EditBody)).getText().toString();
        JourneyInfoView.loadData("<html><body><h1>" + title + "</h1><hr>" + StrToHtml(body) + "</body></html>", "text/html", "UTF-8");
        findViewById(R.id.diary_mapNpics_Write).setVisibility(View.GONE);
        findViewById(R.id.diary_mapNpics_Read).setVisibility(View.VISIBLE);
    }

    public void diary_onCancelClick(View view) {
        findViewById(R.id.diary_mapNpics_Write).setVisibility(View.GONE);
        findViewById(R.id.diary_mapNpics_Read).setVisibility(View.VISIBLE);
    }

    private String StrToHtml(String PlainText)
    {
        return PlainText.replace("\n", "<br>");
    }
}
