package kr.ac.kw.coms.globealbum.diary;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GalleryDetail;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureArray;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;
import kr.ac.kw.coms.globealbum.common.CircularImageKt;
import kr.ac.kw.coms.globealbum.common.RecyclerItemClickListener;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.map.MyMarker;
import kr.ac.kw.coms.globealbum.provider.EXIFinfo;
import kr.ac.kw.coms.globealbum.provider.IPicture;

/**
 * TODO: 마커들이 화면에 다 들어오고 최대한 확대가 된 상태로 시작되게 해야함
 */
public class Diary_mapNPictures extends AppCompatActivity {

    final ArrayList<Integer> PicturesArray = new ArrayList<>();
    boolean isLiked = false;
    final boolean EDIT_MODE = true;
    final boolean READ_MODE = false;
    boolean isEDIT_MODE = false;
    GroupDiaryView picView = null;
    ListviewAdapter adapter;

    //mapview에서 사용되는 멤버변수
    MyMapView myMapView = null;   //맵뷰 인스턴스
    MapEventsOverlay mapviewClickEventOverlay; //맵 이벤트를 등록하는 오버레이
    List<Marker> markerList = new ArrayList<>();
    List<Polyline> polylineList = new ArrayList<>();
    int selectedMarkerIndex = -1;


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

    class ArgumentedOnClickListener extends RecyclerItemClickListener {
        ArrayList<String> Arg = null;
        //first: 선택한 파일의 경로
        //second: 선택한 파일의 순서상 번호

        public ArgumentedOnClickListener(RecyclerView recyclerView, ArrayList<String> arg) {
            super(recyclerView);
            Arg = arg;
        }

        @Override
        public void onItemClick(@NotNull View view, int position) {
            if (isEDIT_MODE == READ_MODE) {
                //열람 모드
                Intent intent = new Intent(getBaseContext(), GalleryDetail.class);
                intent.putExtra("urls", Arg);
                intent.putExtra("index", position);
                startActivity(intent);
            } else {
                //수정 모드
                findViewById(R.id.diary_mapNpics_View).setVisibility(View.GONE);
                findViewById(R.id.diary_mapNpics_PictureEdit).setVisibility(View.VISIBLE);
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
        ArrayList<String> urls = new ArrayList<>();
        for (int i = 0; i < PicturesArray.size(); i++) {
            pics.add(i, new ResourcePicture(this, PicturesArray.get(i)));
            urls.add(resourceToUri(getBaseContext(), PicturesArray.get(i)).toString());
        }
        elementList.add(new PictureGroup("", pics));
        picView.setGroups(elementList);
        picView.addOnItemTouchListener(new ArgumentedOnClickListener(picView, urls).getItemTouchListener());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_map_n_pictures);

        myMapView = findViewById(R.id.diary_mapNpics_Map);
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
        //mapviewClickEventOverlay = mapviewClickEventDisplay();
        //myMapView.getOverlays().add(mapviewClickEventOverlay);

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


    /**
     * 다이어리 액티비티 실행시 가져오는 사진들의 정보를 가지고 마커를 맵뷰에 띄워줌
     */
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
                markerList.add(marker);
                myMapView.getOverlays().add(marker);

                int markerListSize = markerList.size();
                if (markerListSize > 0) {
                    drawPolyline(markerList.get(markerListSize - 2).getPosition(), markerList.get(markerListSize - 1).getPosition());
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        myMapView.invalidate();
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
        Marker marker = new Marker(myMapView);
        marker.setIcon(drawable);
        marker.setPosition(geoPoint);
        marker.setAnchor(0.25f, 1.0f);
        marker.setOnMarkerClickListener(markerClickEvent());
        return marker;
    }

    /**
     * 마커와 마커 사이의 직선을 그림
     *
     * @param geoPoint1 polyline의 시작 좌표
     * @param geoPoint2 polyline의 끝 좌표
     */
    private void drawPolyline(GeoPoint geoPoint1, GeoPoint geoPoint2) {
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(geoPoint1);
        geoPoints.add(geoPoint2);

        Polyline line = new Polyline();
        line.setPoints(geoPoints);

        polylineList.add(line);
        myMapView.getOverlays().add(line);
    }


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
     * 마커를 클릭했을 시에 동작하는 리스너. 기존의 테두리가 있는 마커가 있으면 테두리 지우고 선택된 마커의 테두리를 흰색으로 변경
     *
     * @return 리스너 반환
     */
    private Marker.OnMarkerClickListener markerClickEvent() {
        return new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {

                Drawable drawable;
                Bitmap bm;
                for (int i = 0; i < markerList.size(); i++) {
                    if (markerList.get(i) == marker) {

                        if (selectedMarkerIndex != -1) {
                            drawable = getResources().getDrawable(PicturesArray.get(selectedMarkerIndex));
                            bm = CircularImageKt.getCircularBitmap(drawable, 150);
                            markerList.get(selectedMarkerIndex).setIcon(new BitmapDrawable(getResources(), bm));
                            markerList.get(selectedMarkerIndex).setAnchor(0.25f, 1.0f);
                            Toast.makeText(Diary_mapNPictures.this, i + " marker is unselected", Toast.LENGTH_SHORT).show();
                        }

                        if (i != selectedMarkerIndex) {
                            drawable = getResources().getDrawable(PicturesArray.get(i));
                            bm = CircularImageKt.getCircularBorderBitmap(drawable, 150);
                            Toast.makeText(Diary_mapNPictures.this, i + " marker is selected", Toast.LENGTH_SHORT).show();
                            marker.setIcon(new BitmapDrawable(getResources(), bm));
                            marker.setAnchor(0.25f, 1.0f);
                            selectedMarkerIndex = i;
                        } else {
                            selectedMarkerIndex = -1;
                        }
                        mapView.invalidate();
                        break;
                    }
                }
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

    public class ListviewAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private ArrayList<Integer> data;
        private int layout;

        public ListviewAdapter(@NotNull Context context, int layout, ArrayList<Integer> data) {
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.data = data;
            this.layout = layout;
        }

        public void swap(int left, int right)
        {
            int tmp = data.get(left);
            data.set(left, data.get(right));
            data.set(right, tmp);
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(layout, parent, false);
            }
            int item = data.get(position);
            ImageView icon = (ImageView) convertView.findViewById(R.id.verticalList_Image);
            icon.setImageResource(item);
            TextView title = (TextView) convertView.findViewById(R.id.verticalList_Title);
            title.setText(new ResourcePicture(getBaseContext(), item).getTitle());
            ((Button)convertView.findViewById(R.id.verticalList_Up)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //위 버튼 클릭
                    if (position > 0)
                        swap(position - 1, position);
                    adapter.notifyDataSetChanged();
                }
            });
            ((Button)convertView.findViewById(R.id.verticalList_Down)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //아래 버튼 클릭
                    if (position < getCount() - 1)
                        swap(position, position + 1);
                    adapter.notifyDataSetChanged();
                }
            });
            ((Button)convertView.findViewById(R.id.verticalList_Delete)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //제거 버튼 클릭
                }
            });
            return convertView;
        }
    }

    public void preparePictureEdit(ArrayList<Integer> PictureList) {
        //사진 순서 편집 창의 내용 준비
        ListView EditList = findViewById(R.id.diary_mapNpics_PictureEdit_List);
        adapter = new ListviewAdapter(getBaseContext(), R.layout.layout_map_n_pictures_verticallist, PictureList);
        EditList.setAdapter(adapter);
    }
}
