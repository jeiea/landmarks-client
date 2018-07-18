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

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.album.ResourcePicture;
import kr.ac.kw.coms.globealbum.diary.Diary_main;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.PictureProvider;

public class Diary_mapNPictures extends AppCompatActivity {

    MyMapView mapView = null;   //맵뷰 인스턴스
    MapEventsOverlay listenerOverlay; //맵 이벤트를 등록하는 오버레이


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
        picView.setPadding(20);

        ArrayList<PictureGroup> elementList = new ArrayList<>();
        ArrayList<PictureProvider.Picture> elementRow = new ArrayList<>();
        Context c = getBaseContext();
        elementRow.add(new ResourcePicture(c, R.drawable.coord0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "0", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.coord1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "1", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.coord2, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "2", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.coord3, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "3", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.coord4, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "4", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.coord5, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "5", Toast.LENGTH_SHORT).show();
            }
        }));
        elementRow.add(new ResourcePicture(c, R.drawable.coord6, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Diary_mapNPictures.this, "6", Toast.LENGTH_SHORT).show();
            }
        }));

        elementList.add(new PictureGroup("", elementRow));
        picView.setGroups(elementList);
        picView.setOrientation(1);

        //TODO: show info text

        if (getIntent().getStringExtra("whose").equals("other"))
        {
            findViewById(R.id.diary_mapNpics_EditStart).setVisibility(View.GONE);
        }


        //마커 이벤트 등록
        listenerOverlay = mapviewClickEventDisplay();
        mapView.getOverlays().add(listenerOverlay);


    }
    //경로를 보여주는 다이어리 화면에서 맵뷰를 클릭하였을 때 발생하는 이벤트
    private MapEventsOverlay mapviewClickEventDisplay() {
        return new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {   //화면 한번 터치시
                Toast.makeText(Diary_mapNPictures.this, "clicked", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {    //길게 터치시
                return false;
            }
        });
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
