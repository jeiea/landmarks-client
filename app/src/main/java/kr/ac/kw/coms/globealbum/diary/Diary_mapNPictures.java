package kr.ac.kw.coms.globealbum.diary;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.zip.Inflater;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GalleryDetail;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.album.ResourcePicture;
import kr.ac.kw.coms.globealbum.common.CircularImageKt;
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
        //글의 제목, 내용
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

        ArrayList<PictureGroup> elementList = new ArrayList<>();
        ArrayList<IPicture> pics = new ArrayList<>();
        for (int i = 0; i < PicturesArray.size(); i++) {
            pics.add(i, new ResourcePicture(this, PicturesArray.get(i)));
        }
        elementList.add(new PictureGroup("", pics));
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

        //맵뷰 클릭 시 이벤트 등록
        mapviewClickEventOverlay = mapviewClickEventDisplay();
        mapView.getOverlays().add(mapviewClickEventOverlay);

        oval = getResources().getDrawable(R.drawable.oval_border, null);

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
        preparePictureEdit(PicturesArray);
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
                Drawable drawable = getResources().getDrawable(PicturesArray.get(i));
                Bitmap bm = CircularImageKt.getCircularBitmap(drawable, 150);
                Marker marker = addPicMarker(geoPoint, new BitmapDrawable(getResources(), bm));
                mapView.getOverlays().add(marker);
                markerFolderOverlay.addMarkerLine(marker);
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }

        mapView.getOverlays().add(markerFolderOverlay);
        mapView.invalidate();
    }

    View lastSelect;
    Drawable oval;

    /**
     * 예비 선택을 지우고 테두리 없는 상태로 바꿈
     */
    private void clearLastSelectIfExists() {
        if (lastSelect == null) {
            return;
        }
        lastSelect.getOverlay().clear();
        lastSelect.setOnClickListener(new PictureClickListenerTypeB1());
    }

    /**
     * 답안 이미지를 예비 선택하는 리스너
     */
    public class PictureClickListenerTypeB1 implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            clearLastSelectIfExists();
            oval.setBounds(new Rect(0, 0, view.getWidth(), view.getHeight()));
            view.getOverlay().add(oval);
            //view.setOnClickListener(new PictureClickListenerTypeB2());
            lastSelect = view;
        }
    }

    /**
     * 예비 선택한 답안을 확정하는 리스너
     */
    public class PictureClickListenerTypeB2 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            clearLastSelectIfExists();
        }
    }


    public static Uri resourceToUri(Context context, int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(resID) + '/' +
                context.getResources().getResourceTypeName(resID) + '/' +
                context.getResources().getResourceEntryName(resID));
    }

    /**
     * 관광지 사진을 이미지로 가진 마커 생성
     *
     * @param geoPoint 관광지의 좌표
     * @param drawable 관광지 사진
     * @return 관광지 이미지로 생성된 마커 반환
     */
    private Marker addPicMarker(final GeoPoint geoPoint, Drawable drawable) {
        //마커 생성 및 설정
        Marker marker = new Marker(mapView);
        marker.setIcon(drawable);
        marker.setPosition(geoPoint);
        marker.setAnchor(0.25f, 1.0f);
        marker.setOnMarkerClickListener(markerClickEvent());
        return marker;
    }

    //

    /**
     * 경로를 보여주는 다이어리 화면에서 맵뷰를 클릭할 시의 리스너
     *
     * @return 리스너 반환
     */
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

    //

    /**
     * 경로를 수정하는 다이어리 화면에서 맵뷰를 클릭하였을 때 발생하는 이벤트
     *
     * @return 리스너 반환
     */
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

    /**
     * 마커를 클릭했을 시에 동작하는 리스너
     *
     * @return 리스너 반환
     */
    private Marker.OnMarkerClickListener markerClickEvent() {
        return new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                Toast.makeText(Diary_mapNPictures.this, "Marker Click", Toast.LENGTH_SHORT).show();
                return true;
            }
        };
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

    public class ListviewAdapter extends BaseAdapter
    {
        private LayoutInflater inflater;
        private ArrayList<Integer> data;
        private int layout;
        public ListviewAdapter(@NotNull Context context, int layout, ArrayList<Integer> data)
        {
            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.data=data;
            this.layout=layout;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
            {
                convertView = inflater.inflate(layout, parent, false);
            }
            int item = data.get(position);
            ImageView icon = (ImageView)convertView.findViewById(R.id.verticalList_Image);
            icon.setImageResource(item);
            //TODO: convertView의 TextView값 지정
            return convertView;
        }
    }

    public void preparePictureEdit(ArrayList<Integer> PictureList)
    {
        //사진 순서 편집 창의 내용 준비
        ListView EditList = findViewById(R.id.diary_mapNpics_PictureEdit_List);
        ListviewAdapter adapter = new ListviewAdapter(getBaseContext(), R.id.verticalList_Root, PictureList);
    }
}
