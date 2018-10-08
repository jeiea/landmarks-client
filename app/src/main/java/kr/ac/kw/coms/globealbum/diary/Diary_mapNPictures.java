package kr.ac.kw.coms.globealbum.diary;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GalleryDetail;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.common.CircularImageKt;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.Diary;
import kr.ac.kw.coms.globealbum.provider.EXIFinfo;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.Promise;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.RemotePicture;
import kr.ac.kw.coms.landmarks.client.CollectionInfo;

public class Diary_mapNPictures extends AppCompatActivity {

    static final String PARCEL_DIARY = "diary";
    final ArrayList<Integer> PicturesArray = new ArrayList<>();
    GroupDiaryView picView = null;
    Diary diary;
    EditImageListAdapter adapter;

    //mapview에서 사용되는 멤버변수
    MyMapView myMapView = null;   //맵뷰 인스턴스
    MapEventsOverlay mapviewClickEventOverlay; //맵 이벤트를 등록하는 오버레이
    List<Marker> markerList = new ArrayList<>();
    List<Polyline> polylineList = new ArrayList<>();
    int selectedMarkerIndex = -1;

    public void setDiary(Diary data) {
        diary = data;
        //받은 데이터를 화면에 표시
        ArrayList<IPicture> pics = new ArrayList<IPicture>(diary);
        ArrayList<PictureGroup> elementList = new ArrayList<>();
        elementList.add(new PictureGroup("", pics));
        picView.setGroups(elementList);
        picView.addOnItemTouchListener(new kr.ac.kw.coms.globealbum.common.RecyclerItemClickListener(picView) {
            @Override
            public void onItemClick(@NotNull View view, int position) {
                super.onItemClick(view, position);
                Intent intent = new Intent(getBaseContext(), GalleryDetail.class);
                ArrayList<IPicture> pics = new ArrayList<IPicture>(diary);
                intent.putParcelableArrayListExtra("pictures", pics);
                intent.putExtra("index", position - 1);
                intent.setAction(RequestCodes.ACTION_VIEW_PHOTO);
                startActivity(intent);
            }
        }.getItemTouchListener());
        //setMarkerToMapview();
        ((TextView)findViewById(R.id.diary_mapNpics_Title)).setText(diary.getTitle());
        ((TextView)findViewById(R.id.diary_mapNpics_Description)).setText(diary.getText());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_map_n_pictures);

        myMapView = findViewById(R.id.diary_mapNpics_Map);
        myMapView.setMaxZoomLevel(15.0);
        picView = findViewById(R.id.diary_mapNpics_Pics);
        picView.getPicAdapter().setPadding(20);
        picView.setDirection(FlexDirection.COLUMN);

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

        Diary data = getIntent().getParcelableExtra(PARCEL_DIARY);
        setDiary(data);

        try {
            if (Objects.equals(getIntent().getAction(), RequestCodes.ACTION_EDIT_DIARY)) {
                diary_onEditClick(null);
            }
        } catch (Exception e) {
            //ignore
        }
    }


    /**
     * 다이어리 액티비티 실행시 가져오는 사진들의 정보를 가지고 마커를 맵뷰에 띄워줌
     */
    private void setMarkerToMapview() {
        myMapView.getOverlays().clear();
        myMapView.invalidate();
        markerList = new ArrayList<>();

        int cntImgs = Objects.requireNonNull(diary.getImages()).size();
        if (cntImgs < 1) {
            return;
        }

        //GPS 정보 뽑아오기
        EXIFinfo exifInfo = new EXIFinfo();
        final Drawable[] drawables = new Drawable[cntImgs];
        ArrayList<ArrayList<IPicture>> ppics = new ArrayList<>();
        ppics.add(diary.toArrayList());
        myMapView.setChains(ppics);

        myMapView.fitZoomToMarkers();
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

    public void diary_onEditClick(View view) {
        //편집 시작
        diary_Switch(EDIT_MODE);
        EditText Edit_Title = findViewById(R.id.diary_edit_TitleText);
        EditText Edit_Description = findViewById(R.id.diary_edit_DescriptionText);
        RecyclerView Edit_ImageList = findViewById(R.id.diary_edit_ImageList);

        CollectionInfo editData = diary.getInfo().getValue();
        Edit_Title.setText(editData.getTitle());
        Edit_Description.setText(editData.getText());
        ArrayList<IPicture> pics = new ArrayList<IPicture>(diary);
        adapter = new EditImageListAdapter(this, pics);
        Edit_ImageList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        Edit_ImageList.setAdapter(adapter);
    }


    final int VIEW_MODE = 0;
    final int EDIT_MODE = 1;

    public void diary_Switch(int MODE) {
        if (MODE == VIEW_MODE) {
            findViewById(R.id.diary_mapNpics_ViewLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.diary_mapNpics_EditLayout).setVisibility(View.GONE);
        } else if (MODE == EDIT_MODE) {
            findViewById(R.id.diary_mapNpics_EditLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.diary_mapNpics_ViewLayout).setVisibility(View.GONE);
        }
    }

    public void diary_onLikeClick(View view) {
        //하트 클릭
    }

    public void diary_edit_onClick(View view) {
        //수정 화면에서 뒤로/저장 버튼 클릭 시
        switch (view.getId()) {
            case R.id.diary_edit_btnBack:
                diary_Switch(VIEW_MODE);
                Toast.makeText(this, "편집 취소", Toast.LENGTH_SHORT).show();
                break;
            case R.id.diary_edit_btnSave:
                diary_Switch(VIEW_MODE);
                Toast.makeText(this, "편집 완료", Toast.LENGTH_SHORT).show();
                //변경된 내용 반영
                EditText Edit_Title = findViewById(R.id.diary_edit_TitleText);
                EditText Edit_Description = findViewById(R.id.diary_edit_DescriptionText);
                CollectionInfo editData = diary.getInfo().getValue();
                editData.setTitle(Edit_Title.getText().toString());
                editData.setText(Edit_Description.getText().toString());
                ArrayList<Integer> imgIds = new ArrayList<>();
                for (IPicture pic : adapter.getItems()) {
                    imgIds.add(((RemotePicture) pic).getInfo().getId());
                }
                editData.setImages(imgIds);

                RemoteJava.INSTANCE.modifyCollection(diary.getInfo().getId(), editData, afterModify);
                break;
        }
    }

    Promise<Diary> afterModify = new Promise<Diary>() {
        @Override
        public void success(Diary result) {
            setDiary(result);
        }

        @Override
        public void failure(@NotNull Throwable cause) {
            Toast.makeText(Diary_mapNPictures.this, cause.toString(), Toast.LENGTH_SHORT);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.SelectNewPhoto) {
            if (data == null) {
                Toast.makeText(this, "선택된 사진 없음", Toast.LENGTH_SHORT).show();
                return;
            }
            IPicture returned_data = data.getParcelableExtra("data");
            adapter.AddNewPicture(returned_data);
        }
    }
}