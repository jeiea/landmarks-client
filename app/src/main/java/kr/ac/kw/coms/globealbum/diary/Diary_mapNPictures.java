package kr.ac.kw.coms.globealbum.diary;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.GroundOverlay2;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.album.ResourcePicture;
import kr.ac.kw.coms.globealbum.diary.Diary_main;
import kr.ac.kw.coms.globealbum.game.GeoPointInterpolator;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.map.MyMarker;
import kr.ac.kw.coms.globealbum.provider.EXIFinfo;
import kr.ac.kw.coms.globealbum.provider.PictureProvider;

public class Diary_mapNPictures extends AppCompatActivity {

    MyMapView mapView = null;   //맵뷰 인스턴스
    MapEventsOverlay mapviewClickEventOverlay; //맵 이벤트를 등록하는 오버레이
    final int PICTURE_NUM=4;

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

        elementList.add(new PictureGroup("", elementRow));
        picView.setGroups(elementList);
        picView.setOrientation(1);

        //TODO: show info text
        if (getIntent().getStringExtra("whose").equals("other"))
        {
            findViewById(R.id.diary_mapNpics_EditStart).setVisibility(View.GONE);
        }



        //drawable의 id 등록
        for(int i = 0 ; i  < PICTURE_NUM; i++) {
            id[i] = R.drawable.coord0 +i;
        }

        //마커 이벤트 등록
        mapviewClickEventOverlay = mapviewClickEventDisplay();
        mapView.getOverlays().add(mapviewClickEventOverlay);

        //맵뷰에 마커들 등록
        markerFolderOverlay = new MyMarker(mapView);
        setMarkerToMapview();

    }


    MyMarker markerFolderOverlay;  //마커들을 가지고 있는 오버레이
    int[] id = new int[PICTURE_NUM];

    //다이어리 액티비티 실행시 가져오는 사진들의 정보를 가지고 마커를 맵뷰에 띄워줌
    private void setMarkerToMapview(){
        //GPS 정보 뽑아오기
        EXIFinfo exifInfo = new EXIFinfo();
        for(int i = 0 ; i < PICTURE_NUM ; i++){
            exifInfo.setMetadata(getResources().openRawResource(id[i]));
            final GeoPoint geoPoint = exifInfo.getLocationGeopoint();

            try{    //화면에 사진을 원형 아이콘으로 표시

                Glide.with(this)
                        .load(resourceToUri(this, id[i]))
                        .apply(RequestOptions.circleCropTransform().override(100,100))
                        .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {

                        Marker marker = addPicMarker(geoPoint,resource);
                        markerFolderOverlay.addMarkerLine(marker);
                    }
                });

            }
            catch (Throwable e){
                e.printStackTrace();
            }

        }

        mapView.getOverlays().add(markerFolderOverlay);
        mapView.invalidate();
    }



    public static Uri resourceToUri(Context context, int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(resID) + '/' +
                context.getResources().getResourceTypeName(resID) + '/' +
                context.getResources().getResourceEntryName(resID) );
    }



    //관광지 사진을 이미지로 가진 마커 생성
    private Marker addPicMarker(final GeoPoint geoPoint, Drawable drawable) {
        //마커 생성 및 설정
        Marker marker = new Marker(mapView);
        marker.setIcon(drawable);
        marker.setPosition(geoPoint);
        marker.setAnchor(0.25f, 1.0f);
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {  //마커 클릭 시 행동
                return true;
            }
        });
        return marker;
    }

    //경로를 보여주는 다이어리 화면에서 맵뷰를 클릭하였을 때 발생하는 이벤트
    private MapEventsOverlay mapviewClickEventDisplay() {
        return new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {   //화면 한번 터치시
                Toast.makeText(Diary_mapNPictures.this, "Display Activity", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {    //길게 터치시
                return false;
            }
        });
    }

    //경로를 수정하는 다이어리 화면에서 맵뷰를 클릭하였을 때 발생하는 이벤트
    private MapEventsOverlay mapviewClickEventEdit() {
        return new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {   //화면 한번 터치시
                Toast.makeText(Diary_mapNPictures.this, "Edit Activity", Toast.LENGTH_SHORT).show();
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
