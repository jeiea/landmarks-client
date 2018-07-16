package kr.ac.kw.coms.globealbum.diary;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
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
    class InfoText
    {
        public String Title;
        public String Body;
        public InfoText()
        {
            Title = "";
            Body = "";
        }
        public InfoText(String title, String body)
        {
            Title = title;
            Body = body;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_map_n_pictures);

        mapView = findViewById(R.id.diary_mapNpics_Map);
        picView = findViewById(R.id.diary_mapNpics_Pics);

        ArrayList<PictureGroup> elementList = new ArrayList<>();
        ArrayList<PictureProvider.Picture> elementRow = new ArrayList<>();
        Context c = getBaseContext();
        elementRow.add(new ResourcePicture(c, R.drawable.sample0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "0", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.sample1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "1", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.sample2, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "2", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.sample3, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "3", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.sample4, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "4", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.sample5, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "5", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.sample6, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "6", Toast.LENGTH_SHORT).show();
            }
        }));

        elementList.add(new PictureGroup("", elementRow));
        picView.setGroups(elementList);
        picView.setOrientation(1);

        //TODO: show info text
    }

    public void diary_onSaveClick(View view) {
        String title = ((TextView)findViewById(R.id.diary_mapNpics_EditTitle)).getText().toString();
        String body = ((TextView)findViewById(R.id.diary_mapNpics_EditBody)).getText().toString();
        if (title.isEmpty() || body.isEmpty())
        {
            Toast.makeText(this, "제목/내용을 입력하세요.", Toast.LENGTH_LONG).show();
            return;
        }
        InfoText newInfo = new InfoText(title, body);

        ((TextView)findViewById(R.id.diary_mapNpics_ReadTitle)).setText(newInfo.Title);
        ((TextView)findViewById(R.id.diary_mapNpics_ReadBody)).setText(newInfo.Body);
        findViewById(R.id.diary_mapNpics_Write).setVisibility(View.GONE);
        findViewById(R.id.diary_mapNpics_Read).setVisibility(View.VISIBLE);
        findViewById(R.id.diary_mapNpics_EditStart).setVisibility(View.VISIBLE);
    }

    public void diary_onCancelClick(View view) {
        findViewById(R.id.diary_mapNpics_Write).setVisibility(View.GONE);
        findViewById(R.id.diary_mapNpics_Read).setVisibility(View.VISIBLE);
        findViewById(R.id.diary_mapNpics_EditStart).setVisibility(View.VISIBLE);
    }

    public void diary_onEditClick(View view)
    {
        ((EditText)findViewById(R.id.diary_mapNpics_EditTitle)).setText(((TextView)findViewById(R.id.diary_mapNpics_ReadTitle)).getText());
        ((EditText)findViewById(R.id.diary_mapNpics_EditBody)).setText(((TextView)findViewById(R.id.diary_mapNpics_ReadBody)).getText());

        findViewById(R.id.diary_mapNpics_Write).setVisibility(View.VISIBLE);
        findViewById(R.id.diary_mapNpics_Read).setVisibility(View.GONE);
        findViewById(R.id.diary_mapNpics_EditStart).setVisibility(View.GONE);
    }
}
