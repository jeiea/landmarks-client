package kr.ac.kw.coms.globealbum.diary;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.flexbox.FlexDirection;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GalleryDetail;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureArray;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.album.ResourcePicture;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.map.MyMarker;
import kr.ac.kw.coms.globealbum.provider.EXIFinfo;
import kr.ac.kw.coms.globealbum.provider.IPicture;

public class Diary_mapNPictures extends AppCompatActivity {

    MyMapView mapView = null;   //맵뷰 인스턴스
    MapEventsOverlay mapviewClickEventOverlay; //맵 이벤트를 등록하는 오버레이
    final ArrayList<Integer> PicturesArray = new ArrayList<>();
    boolean isLiked = false;
    final boolean EDIT_MODE = true;
    final boolean READ_MODE = false;
    boolean isEDIT_MODE = false;

    GroupDiaryView picView = null;

    class InfoText {
        public String Title;
        public String Body;

        public InfoText() {
            Title = "";
            Body = "";
        }

        public InfoText(String title, String body) {
            Title = title;
            Body = body;
        }
    }

    class ArgumentedOnClickListener implements View.OnClickListener {
        Pair<ArrayList<String>, Integer> Arg = null;
        //first: 선택한 파일의 경로
        //second: 선택한 파일의 순서상 번호

        public ArgumentedOnClickListener(Pair<ArrayList<String>, Integer> arg) {
            Arg = arg;
        }

        @Override
        public void onClick(View v) {
            if (isEDIT_MODE == READ_MODE) {
                //열람 모드
                Intent intent = new Intent(getBaseContext(), GalleryDetail.class);
                intent.putExtra("urls", Arg.first);
                intent.putExtra("index", Arg.second);
                startActivity(intent);
            } else {
                //수정 모드
                //TODO: 사진 목록 중 하나를 선택한 상태. 위치 이동이나 제거 등의 옵션 제공
            }
        }
    }

    public void prepareData() {
        PicturesArray.add(R.drawable.coord0);
        PicturesArray.add(R.drawable.coord1);
        PicturesArray.add(R.drawable.coord2);
        PicturesArray.add(R.drawable.coord3);
        final ArrayList<String> urls = new ArrayList<String>();

        ArrayList<PictureGroup> elementList = new ArrayList<>();
        PictureArray elementRow = new PictureArray();
        final Context c = getBaseContext();
        for (int i = 0; i < PicturesArray.size(); i++) {
            final int idx = i;
            elementRow.add(idx, new ResourcePicture(c, PicturesArray.get(idx)));
        }
        elementRow.sort();

        for (IPicture i : elementRow) {
            urls.add(resourceToUri(this, ((ResourcePicture) i).getId()).toString());
        }

        for (int i = 0; i < elementRow.size(); i++) {
            final int idx = i;
            elementRow.setOnClickListener(i, new ArgumentedOnClickListener(new Pair<ArrayList<String>, Integer>(urls, idx)));
        }

        elementList.add(new PictureGroup("", elementRow));
        picView.setGroups(elementList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_map_n_pictures);

        mapView = findViewById(R.id.diary_mapNpics_Map);
        picView = findViewById(R.id.diary_mapNpics_Pics);
        picView.getPicAdapter().setPadding(20);

        prepareData();
        picView.setDirection(FlexDirection.COLUMN);

        //TODO: show info text
        try {
            if (getIntent().getStringExtra("whose").equals("other")) {
                findViewById(R.id.diary_mapNpics_EditStart).setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            //Ignore
        }

        //마커 이벤트 등록
        mapviewClickEventOverlay = mapviewClickEventDisplay();
        mapView.getOverlays().add(mapviewClickEventOverlay);

        //맵뷰에 마커들 등록
        markerFolderOverlay = new MyMarker(mapView);
        setMarkerToMapview();

        //명령 받기
        try {
            if (getIntent().getStringExtra("order").equals("edit")) {
                diary_onEditClick(null);
            }
        } catch (NullPointerException e) {
            //Ignore
        }
    }

    MyMarker markerFolderOverlay;  //마커들을 가지고 있는 오버레이

    //다이어리 액티비티 실행시 가져오는 사진들의 정보를 가지고 마커를 맵뷰에 띄워줌
    private void setMarkerToMapview() {
        //GPS 정보 뽑아오기
        EXIFinfo exifInfo = new EXIFinfo();
        for (int i = 0; i < PicturesArray.size(); i++) {
            exifInfo.setMetadata(getResources().openRawResource(PicturesArray.get(i)));
            final GeoPoint geoPoint = exifInfo.getLocationGeopoint();

            try {    //화면에 사진을 원형 아이콘으로 표시

                Glide.with(this)
                        .load(resourceToUri(this, PicturesArray.get(i)))
                        .apply(RequestOptions.circleCropTransform().override(100, 100))
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {

                                Marker marker = addPicMarker(geoPoint, resource);
                                markerFolderOverlay.addMarkerLine(marker);
                            }
                        });

            } catch (Throwable e) {
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
                context.getResources().getResourceEntryName(resID));
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
        String title = ((TextView) findViewById(R.id.diary_mapNpics_EditTitle)).getText().toString();
        String body = ((TextView) findViewById(R.id.diary_mapNpics_EditBody)).getText().toString();
        if (title.isEmpty() || body.isEmpty()) {
            Toast.makeText(this, "제목/내용을 입력하세요.", Toast.LENGTH_LONG).show();
            return;
        }
        InfoText newInfo = new InfoText(title, body);

        ((TextView) findViewById(R.id.diary_mapNpics_ReadTitle)).setText(newInfo.Title);
        ((TextView) findViewById(R.id.diary_mapNpics_ReadBody)).setText(newInfo.Body);
        findViewById(R.id.diary_mapNpics_Write).setVisibility(View.GONE);
        findViewById(R.id.diary_mapNpics_EditTitle).setVisibility(View.GONE);
        findViewById(R.id.diary_mapNpics_ReadTitle).setVisibility(View.VISIBLE);
        findViewById(R.id.diary_mapNpics_Read).setVisibility(View.VISIBLE);
        findViewById(R.id.diary_mapNpics_EditStart).setVisibility(View.VISIBLE);
        isEDIT_MODE = READ_MODE;
    }

    public void diary_onCancelClick(View view) {
        findViewById(R.id.diary_mapNpics_EditTitle).setVisibility(View.GONE);
        findViewById(R.id.diary_mapNpics_Write).setVisibility(View.GONE);
        findViewById(R.id.diary_mapNpics_ReadTitle).setVisibility(View.VISIBLE);
        findViewById(R.id.diary_mapNpics_Read).setVisibility(View.VISIBLE);
        findViewById(R.id.diary_mapNpics_EditStart).setVisibility(View.VISIBLE);
        isEDIT_MODE = READ_MODE;
    }

    public void diary_onEditClick(View view) {
        ((EditText) findViewById(R.id.diary_mapNpics_EditTitle)).setText(((TextView) findViewById(R.id.diary_mapNpics_ReadTitle)).getText());
        ((EditText) findViewById(R.id.diary_mapNpics_EditBody)).setText(((TextView) findViewById(R.id.diary_mapNpics_ReadBody)).getText());

        findViewById(R.id.diary_mapNpics_EditTitle).setVisibility(View.VISIBLE);
        findViewById(R.id.diary_mapNpics_Write).setVisibility(View.VISIBLE);
        findViewById(R.id.diary_mapNpics_ReadTitle).setVisibility(View.GONE);
        findViewById(R.id.diary_mapNpics_Read).setVisibility(View.GONE);
        findViewById(R.id.diary_mapNpics_EditStart).setVisibility(View.INVISIBLE);
        isEDIT_MODE = EDIT_MODE;
    }

    public void diary_onLikeClick(View view) {
        //하트 클릭
        if (isLiked) {
            ((ImageButton) findViewById(R.id.diary_mapNpics_TitleLike)).setImageResource(R.drawable.heartgrey);
            isLiked = false;
        } else {
            ((ImageButton) findViewById(R.id.diary_mapNpics_TitleLike)).setImageResource(R.drawable.heart);
            isLiked = true;
        }

    }
}
