package kr.ac.kw.coms.globealbum.diary;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;

import kr.ac.kw.coms.globealbum.common.RequestCodes;
import org.jetbrains.annotations.NotNull;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GalleryDetail;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.provider.Promise;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;
import kr.ac.kw.coms.globealbum.common.CircularImageKt;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.EXIFinfo;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class Diary_mapNPictures extends AppCompatActivity {

    final ArrayList<Integer> PicturesArray = new ArrayList<>();
    GroupDiaryView picView = null;
    final Diary_Parcel DiaryData = new Diary_Parcel();
    Diary_Parcel DiaryData_Edit = new Diary_Parcel();
    EditImageListAdapter adapter;

    //mapview에서 사용되는 멤버변수
    MyMapView myMapView = null;   //맵뷰 인스턴스
    MapEventsOverlay mapviewClickEventOverlay; //맵 이벤트를 등록하는 오버레이
    List<Marker> markerList = new ArrayList<>();
    List<Polyline> polylineList = new ArrayList<>();
    int selectedMarkerIndex = -1;

    public void ReceiveData() {
        //서버로부터 데이터 전송받기
        PicturesArray.add(R.drawable.coord0);
        PicturesArray.add(R.drawable.coord1);
        PicturesArray.add(R.drawable.coord2);
        PicturesArray.add(R.drawable.coord3);

        DiaryData.Title = "title";
        DiaryData.Description = "text";
        for (int i = 0; i < PicturesArray.size(); i++) {
            DiaryData.Images.add(new ResourcePicture(PicturesArray.get(i), getResources()));
        }
    }

    public void PrepareData() {
        ReceiveData();
        //받은 데이터를 화면에 표시
        ArrayList<PictureGroup> elementList = new ArrayList<>();
        ArrayList<IPicture> pics = new ArrayList<>();
        for (int i = 0; i < DiaryData.Images.size(); i++) {
            pics.add(i, DiaryData.Images.get(i));
        }
        elementList.add(new PictureGroup("", pics));
        picView.setGroups(elementList);
        picView.addOnItemTouchListener(new kr.ac.kw.coms.globealbum.common.RecyclerItemClickListener(picView) {
            @Override
            public void onItemClick(@NotNull View view, int position) {
                super.onItemClick(view, position);
                Intent intent = new Intent(getBaseContext(), GalleryDetail.class);
                intent.putParcelableArrayListExtra("pictures", DiaryData.Images);
                intent.putExtra("index", position - 1);
                intent.setAction(RequestCodes.ACTION_VIEW_PHOTO);
                startActivity(intent);
            }
        }.getItemTouchListener());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_map_n_pictures);

        myMapView = findViewById(R.id.diary_mapNpics_Map);
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

        PrepareData();
        setMarkerToMapview();

        try {
            if (getIntent().getAction().equals(RequestCodes.ACTION_EDIT_DIARY)) {
                diary_onEditClick(null);
            }
        }catch (Exception e)
        {
            //ignore
        }
    }


    /**
     * 다이어리 액티비티 실행시 가져오는 사진들의 정보를 가지고 마커를 맵뷰에 띄워줌
     */
    private void setMarkerToMapview() {
        myMapView.getOverlays().clear();
        markerList = new ArrayList<>();

        //GPS 정보 뽑아오기
        EXIFinfo exifInfo = new EXIFinfo();
        final Drawable[] drawables = new Drawable[DiaryData.Images.size()];
        final ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < DiaryData.Images.size(); i++) {
            final int idx = i;
            IPicture pic = DiaryData.Images.get(i);
            pic.drawable(getResources(), new Promise<Drawable>() {
                int cnt = 0;

                @Override
                public void success(Drawable result) {
                    drawables[idx] = result;
                    arrayList.add(1);
                    cnt++;
                    if (arrayList.size() == DiaryData.Images.size()) {
                        addCircularMarker(drawables);
                    }
                }
            });

        }
        myMapView.invalidate();
    }

    /**
     * 화면에 사진을 원형 아이콘으로 표시
     * @param drawables 원형으로 되기 전의 사진들
     */
    private void addCircularMarker(Drawable[] drawables) {
        for (int idx = 0; idx < drawables.length; idx++) {
            Drawable drawable = drawables[idx];
            Bitmap bm = CircularImageKt.getCircularBitmap(drawable, 150);
            IPicture pic = DiaryData.Images.get(idx);
            Marker marker = addPicMarker(pic.getGeo(), new BitmapDrawable(getResources(), bm));
            markerList.add(marker);
            myMapView.getOverlays().add(marker);
            int markerListSize = markerList.size();
            if (markerListSize > 1) {
                drawPolyline(markerList.get(markerListSize - 2).getPosition(), markerList.get(markerListSize - 1).getPosition());
            }
        }
        /*
        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLong = Double.MAX_VALUE;
        double maxLong = Double.MIN_VALUE;
        for (Marker item : markerList) {
            GeoPoint point = item.getPosition();
            if (point.getLatitude() < minLat)
                minLat = point.getLatitude();
            if (point.getLatitude() > maxLat)
                maxLat = point.getLatitude();
            if (point.getLongitude() < minLong)
                minLong = point.getLongitude();
            if (point.getLongitude() > maxLong)
                maxLong = point.getLongitude();
        }
        BoundingBox boundingBox = new BoundingBox(maxLat, maxLong, minLat, minLong);
        myMapView.zoomToBoundingBox(boundingBox,false);
        myMapView.getController().zoomToSpan(boundingBox.getLatitudeSpan(),boundingBox.getLongitudeSpan());*/
        //myMapView.getController().setCenter(boundingBox.getCenter());
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
        DiaryData_Edit = DiaryData.clone();
        EditText Edit_Title = findViewById(R.id.diary_edit_TitleText);
        EditText Edit_Description = findViewById(R.id.diary_edit_DescriptionText);
        RecyclerView Edit_ImageList = findViewById(R.id.diary_edit_ImageList);


        Edit_Title.setText(DiaryData_Edit.Title);
        Edit_Description.setText(DiaryData_Edit.Description);
        adapter = new EditImageListAdapter(this, DiaryData_Edit.Images);
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
                DiaryData_Edit.Title = Edit_Title.getText().toString();
                DiaryData_Edit.Description = Edit_Description.getText().toString();
                DiaryData_Edit.Images = adapter.getItems();

                //서버 통신 필요
                //DiaryData_Edit를 서버에 전송

                //데이터 다시 가져오기
                DiaryData.set(DiaryData_Edit);
                ((TextView)findViewById(R.id.diary_mapNpics_Title)).setText(DiaryData.Title);
                ((TextView)findViewById(R.id.diary_mapNpics_Description)).setText(DiaryData.Description);
                picView.clearAllItems();
                ArrayList<PictureGroup> elementList = new ArrayList<>();
                elementList.add(new PictureGroup("", DiaryData.Images));
                picView.setGroups(elementList);
                setMarkerToMapview();
                break;
        }
    }

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