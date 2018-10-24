package kr.ac.kw.coms.globealbum.diary;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexDirection;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
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
import kr.ac.kw.coms.globealbum.common.MediaScannerKt;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.Diary;
import kr.ac.kw.coms.globealbum.provider.EXIFinfo;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.LocalPicture;
import kr.ac.kw.coms.globealbum.provider.Promise;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.RemotePicture;
import kr.ac.kw.coms.globealbum.provider.UIPromise;
import kr.ac.kw.coms.landmarks.client.IdCollectionInfo;

public class Diary_mapNPictures extends AppCompatActivity {

    static final String PARCEL_DIARY = "diary";
    final ArrayList<Integer> PicturesArray = new ArrayList<>();
    GroupDiaryView picView = null;
    Diary diary_toShow;
    Diary diary_onEdit;
    EditImageListAdapter editImageListAdapter;

    //mapview에서 사용되는 멤버변수
    MyMapView myMapView = null;   //맵뷰 인스턴스
    List<Marker> markerList = new ArrayList<>();
    List<Polyline> polylineList = new ArrayList<>();
    int selectedMarkerIndex = -1;

    public void setDiary(final Diary data) {
        //받은 데이터를 화면에 표시
        ArrayList<IPicture> pics = new ArrayList<>(data.getPictures());
        ArrayList<PictureGroup> elementList = new ArrayList<>();
        elementList.add(new PictureGroup("", pics));
        picView.clearAllItems();
        picView.setGroups(elementList);
        picView.addOnItemTouchListener(new kr.ac.kw.coms.globealbum.common.RecyclerItemClickListener(picView) {
            @Override
            public void onItemClick(@NotNull View view, int position) {
                super.onItemClick(view, position);
                Intent intent = new Intent(getBaseContext(), GalleryDetail.class);
                ArrayList<IPicture> pics = new ArrayList<>(data.getPictures());
                intent.putParcelableArrayListExtra("pictures", pics);
                intent.putExtra("index", position - 1);
                intent.setAction(RequestCodes.ACTION_VIEW_PHOTO);
                startActivity(intent);
            }
        }.getItemTouchListener());
        setMarkerToMapview();

        ((TextView) findViewById(R.id.diary_mapNpics_Title)).setText(data.getTitle());
        ((TextView) findViewById(R.id.diary_mapNpics_Description)).setText(data.getText());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_map_n_pictures);

        myMapView = findViewById(R.id.diary_mapNpics_Map);
        myMapView.setTileSource(TileSourceFactory.MAPNIK);    //맵 렌더링 설정

        picView = findViewById(R.id.diary_mapNpics_Pics);
        picView.getPicAdapter().setLeftPadding(10);
        picView.getPicAdapter().setRightPadding(10);
        picView.setDirection(FlexDirection.COLUMN);

        diary_toShow = getIntent().getParcelableExtra(PARCEL_DIARY);
        if (getIntent().getAction() == null) ;
            //ignore
        else if (getIntent().getAction().equals(RequestCodes.ACTION_DIARY_OTHERS))
            findViewById(R.id.diary_mapNpics_EditStart).setVisibility(View.GONE);

        setDiary(diary_toShow);
        if (getIntent().hasExtra(RequestCodes.ACTION_EDIT_DIARY) && getIntent().getStringExtra(RequestCodes.ACTION_EDIT_DIARY).equals(RequestCodes.ACTION_EDIT_DIARY))
            diary_onEditClick(null);
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.diary_new_EditLayout).getVisibility() == View.VISIBLE)
            diary_Switch(VIEW_MODE);
        else
            super.onBackPressed();
    }

    /**
     * 다이어리 액티비티 실행시 가져오는 사진들의 정보를 가지고 마커를 맵뷰에 띄워줌
     */
    private void setMarkerToMapview() {
        myMapView.getOverlays().clear();
        myMapView.invalidate();
        markerList = new ArrayList<>();

        int cntImgs = Objects.requireNonNull(diary_toShow.getImages()).size();
        if (cntImgs < 1) {
            return;
        }

        //GPS 정보 뽑아오기
        EXIFinfo exifInfo = new EXIFinfo();
        final Drawable[] drawables = new Drawable[cntImgs];
        ArrayList<ArrayList<IPicture>> ppics = new ArrayList<>();
        ppics.add(new ArrayList<>(diary_toShow.getPictures()));
        myMapView.getDiaryOverlay().setChains(ppics);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                myMapView.fitZoomToMarkers();
            }
        }, 100);
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
        diary_onEdit = diary_toShow;
        diary_Switch(EDIT_MODE);
        EditText Edit_Title = findViewById(R.id.diary_edit_TitleText);
        EditText Edit_Description = findViewById(R.id.diary_edit_DescriptionText);
        RecyclerView Edit_ImageList = findViewById(R.id.diary_edit_ImageList);

        IdCollectionInfo editData = diary_onEdit.getInfo();
        Edit_Title.setText(editData.getTitle());
        Edit_Description.setText(editData.getText());
        ArrayList<IPicture> pics = new ArrayList<>(diary_onEdit.getPictures());
        editImageListAdapter = new EditImageListAdapter(this, pics);
        Edit_ImageList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        Edit_ImageList.setAdapter(editImageListAdapter);
    }

    final int VIEW_MODE = 0;
    final int EDIT_MODE = 1;
    final int ADD_MODE = 2;
    final int ZOOMIN_MODE = 3;

    public void diary_Switch(int MODE) {
        switch (MODE) {
            case VIEW_MODE:
                findViewById(R.id.diary_mapNpics_ViewLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.diary_new_EditLayout).setVisibility(View.GONE);
                findViewById(R.id.diary_mapNpics_AddLayout).setVisibility(View.GONE);
                findViewById(R.id.diary_mapNpics_ZoomInLayout).setVisibility(View.GONE);
                break;
            case EDIT_MODE:
                findViewById(R.id.diary_new_EditLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.diary_mapNpics_ViewLayout).setVisibility(View.GONE);
                findViewById(R.id.diary_mapNpics_AddLayout).setVisibility(View.GONE);
                findViewById(R.id.diary_mapNpics_ZoomInLayout).setVisibility(View.GONE);
                break;
            case ADD_MODE:
                RecyclerView NewImageList = (RecyclerView) findViewById(R.id.diary_mapNpics_AddImageList);
                NewImageList.setHasFixedSize(true);
                NewImageList.setLayoutManager(new GridLayoutManager(this, 4));
                NewImageList.setAdapter(new NewImageAdapter(getImageFilePath()));
                findViewById(R.id.diary_new_EditLayout).setVisibility(View.GONE);
                findViewById(R.id.diary_mapNpics_ViewLayout).setVisibility(View.GONE);
                findViewById(R.id.diary_mapNpics_AddLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.diary_mapNpics_ZoomInLayout).setVisibility(View.GONE);
                break;
            case ZOOMIN_MODE:
                findViewById(R.id.diary_new_EditLayout).setVisibility(View.GONE);
                findViewById(R.id.diary_mapNpics_ViewLayout).setVisibility(View.GONE);
                findViewById(R.id.diary_mapNpics_AddLayout).setVisibility(View.GONE);
                findViewById(R.id.diary_mapNpics_ZoomInLayout).setVisibility(View.VISIBLE);
                break;
        }
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
                IdCollectionInfo editData = diary_onEdit.getInfo();
                editData.setTitle(Edit_Title.getText().toString());
                editData.setText(Edit_Description.getText().toString());
                ArrayList<Integer> imgIds = new ArrayList<>();
                for (IPicture pic : editImageListAdapter.getItems()) {
                    imgIds.add(((RemotePicture) pic).getInfo().getId());
                }
                editData.setImages(imgIds);

                RemoteJava.INSTANCE.modifyCollection(diary_onEdit.getInfo().getId(), editData, afterModify);
                break;
        }
    }

    public void diary_ZoomIn_CloseZoomIn(View view) {
        diary_Switch(ADD_MODE);
    }

    Promise<Diary> afterModify = new UIPromise<Diary>() {
        @Override
        public void success(Diary result) {
            diary_toShow = result;
            setDiary(result);
        }

        @Override
        public void failure(@NotNull Throwable cause) {
            Toast.makeText(Diary_mapNPictures.this, cause.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    public class EditImageListAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        final ArrayList<IPicture> mItems = new ArrayList<>();

        public EditImageListAdapter(AppCompatActivity RootActivity, ArrayList<IPicture> Items) {
            mItems.addAll(Items);
        }

        public void AddNewPicture(IPicture newPicture) {
            mItems.add(newPicture);
            notifyDataSetChanged();
        }

        public ArrayList<IPicture> getItems() {
            return mItems;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < getItemCount() - 1) {
                return 0;
            } else {
                return 1;
            }
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            if (viewType == 0) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_map_n_pictures_verticallist, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_map_n_pictures_verticallist_new, parent, false);
            }
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, final int position) {
            //목록의 내용 추가
            if (position < getItemCount() - 1) {
                Glide.with(holder.imageView).load(mItems.get(position)).into(holder.imageView);
                GeoPoint point = mItems.get(position).getMeta().getGeo();
                holder.text_Title.setText(mItems.get(position).getMeta().getAddress() + "\n위도 " + Math.round(point.getLatitude()) + ", 경도 " + Math.round(point.getLongitude()));
                holder.btn_Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(Diary_mapNPictures.this);
                        alert.setTitle("삭제 확인");
                        alert.setMessage("사진을 삭제합니다.");
                        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mItems.remove(position);
                                notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });
                        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alert.show();
                    }
                });
                holder.btn_MoveUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position == 0)
                            return;
                        IPicture swap = mItems.get(position);
                        mItems.set(position, mItems.get(position - 1));
                        mItems.set(position - 1, swap);
                        notifyDataSetChanged();
                    }
                });
                holder.btn_MoveDown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position > getItemCount() - 2)
                            return;
                        IPicture swap = mItems.get(position);
                        mItems.set(position, mItems.get(position + 1));
                        mItems.set(position + 1, swap);
                        notifyDataSetChanged();
                    }
                });
            } else {
                holder.btn_New.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(Diary_mapNPictures.this, "New Image!", Toast.LENGTH_SHORT).show();
                        diary_Switch(ADD_MODE);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size() + 1;
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout rootLayout;
        ConstraintLayout updownBox;
        ImageView imageView;
        TextView text_Title;
        ImageButton btn_Delete;
        ImageButton btn_New;
        ImageButton btn_MoveUp;
        ImageButton btn_MoveDown;

        public ItemViewHolder(View itemView) {
            super(itemView);
            rootLayout = (ConstraintLayout) itemView;
            imageView = (ImageView) rootLayout.getViewById(R.id.verticalList_Image);
            text_Title = (TextView) rootLayout.getViewById(R.id.verticalList_Title);
            if (rootLayout.getId() == R.id.verticalList_Root) {
                btn_Delete = (ImageButton) rootLayout.getViewById(R.id.verticalList_Delete);
                updownBox = (ConstraintLayout) rootLayout.getViewById(R.id.verticalList_UpDownBox);
                btn_MoveUp = (ImageButton) updownBox.getViewById(R.id.verticalList_MoveUp);
                btn_MoveDown = (ImageButton) updownBox.getViewById(R.id.verticalList_MoveDown);
            } else
                btn_New = (ImageButton) rootLayout.getViewById(R.id.verticalList_New);

        }
    }

    private ArrayList<String> getImageFilePath() {
        //이미지 파일 쿼리 및 resId 가져오기
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        MediaScannerKt.mediaScan(getBaseContext());
        ArrayList<String> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        cursor = this.getContentResolver().query(uri, projection, null, null, null);
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data); //각 파일의 절대경로 구하기
            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

    class NewImageAdapter extends RecyclerView.Adapter<NewImageAdapter.ViewHolder> {
        private ArrayList<String> urlList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public int DisplayWidth;
            public ImageView imageView;

            public ViewHolder(ImageView v) {
                super(v);
                imageView = v;
                DisplayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            }
        }

        public NewImageAdapter(ArrayList<String> urlList) {
            this.urlList = urlList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView v = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.inner_view, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final String url = urlList.get(position);
            Glide.with(holder.imageView).load(url).into(holder.imageView);
            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
            params.height = holder.DisplayWidth / 4;
            holder.imageView.setLayoutParams(params);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Glide.with(Diary_mapNPictures.this).load(url).into((ImageView) findViewById(R.id.diary_ZoomIn_ZoomImage));
                    findViewById(R.id.diary_ZoomIn_ZoomName1).setVisibility(View.GONE);
                    findViewById(R.id.diary_ZoomIn_ZoomName2).setVisibility(View.GONE);
                    findViewById(R.id.diary_ZoomIn_Confirm).setVisibility(View.VISIBLE);
                    diary_Switch(ZOOMIN_MODE);
                    findViewById(R.id.diary_ZoomIn_Confirm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EXIFinfo exifInfo = new EXIFinfo(url);
                            double[] location = exifInfo.getLocation();
                            if (location == null)
                            {
                                Toast.makeText(Diary_mapNPictures.this, "위치 정보를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                IPicture newPicture = new LocalPicture(url);
                                //diary_onEdit.add((RemotePicture) newPicture);
                                editImageListAdapter.AddNewPicture(newPicture);
                            }
                            diary_Switch(EDIT_MODE);
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return urlList.size();
        }
    }
}