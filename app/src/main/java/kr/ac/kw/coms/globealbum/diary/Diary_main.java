package kr.ac.kw.coms.globealbum.diary;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import kotlin.Unit;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.OnSwipeTouchListener;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.common.RecyclerItemClickListener;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.provider.AccountActivity;
import kr.ac.kw.coms.globealbum.provider.Diary;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.Promise;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.RemotePicture;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;
import kr.ac.kw.coms.globealbum.provider.UIPromise;
import kr.ac.kw.coms.landmarks.client.NearGeoPoint;
import kr.ac.kw.coms.landmarks.client.PictureQuery;

public class Diary_main extends AppCompatActivity {

    View Root;
    GroupDiaryView ImageList;
    RecyclerView JourneyList;
    ConstraintLayout TabLeft;
    ConstraintLayout TabRight;
    RecyclerView.Adapter<DiaryListViewHolder> JourneyListAdapter;
    ImageButton BtnNewDiary;
    boolean isTabLeft = true;
    List<Diary> DownloadedDiaryList;
    List<IPicture> DownloadedImageList;
    int ZoomIndex;
    OnSwipeTouchListener swipeTouchListener;
    IPicture ImageToSend;
    Diary DiaryToSend;
    boolean IsImageSelected;
    RecyclerView.OnItemTouchListener ImageTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_main);

        Root = findViewById(R.id.diary_main_Root);
        ImageList = findViewById(R.id.diary_main_ImageList);
        JourneyList = findViewById(R.id.diary_main_JourneyList);
        BtnNewDiary = findViewById(R.id.diary_main_NewDiary);
        TabLeft = findViewById(R.id.diary_main_Tab_Left);
        TabRight = findViewById(R.id.diary_main_Tab_Right);
        GroupDiaryView ImageNowLoading = findViewById(R.id.diary_main_ImageNowLoading);
        GroupDiaryView JourneyNowLoading = findViewById(R.id.diary_main_JourneyNowLoading);
        ImageTouchListener = new RecyclerItemClickListener(ImageList) {
            //사진 클릭 이벤트
            @Override
            public void onItemClick(@NotNull View view, int position) {
                if (view instanceof ImageView) {
                    ZoomIndex = position - 1;
                    GlideApp.with(view).load(DownloadedImageList.get(ZoomIndex)).into(((ImageView) findViewById(R.id.diary_ZoomIn_ZoomImage)));
                    divideAddress(DownloadedImageList.get(ZoomIndex).getMeta().getAddress());
                    findViewById(R.id.diary_ZoomIn_Root).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLongItemClick(@NotNull View view, int position) {
                //롱클릭 이벤트
                IsImageSelected = true;
                ImageToSend = DownloadedImageList.get(position - 1);
                findViewById(R.id.diary_main_menuRoot).setVisibility(View.VISIBLE);
                findViewById(R.id.diary_main_menuEdit).setVisibility(View.INVISIBLE);
                findViewById(R.id.diary_main_menuShare).setVisibility(View.VISIBLE);
                if (!getIntent().getAction().equals(RequestCodes.ACTION_DIARY_MINE)) {
                    findViewById(R.id.diary_main_menuDelete).setVisibility(View.GONE);
                }
            }
        }.getItemTouchListener();


        ArrayList<IPicture> nowloading = new ArrayList<>();
        nowloading.add(new ResourcePicture(R.drawable.nowloading));
        ArrayList<PictureGroup> LoadingScreen = new ArrayList<PictureGroup>();
        LoadingScreen.add(new PictureGroup("", nowloading));
        ImageNowLoading.getPicAdapter().setColumns(1);
        JourneyNowLoading.getPicAdapter().setColumns(1);
        ImageNowLoading.getPicAdapter().setImageScaleType(ImageView.ScaleType.FIT_CENTER);
        JourneyNowLoading.getPicAdapter().setImageScaleType(ImageView.ScaleType.FIT_CENTER);
        ImageNowLoading.setGroups(LoadingScreen);
        JourneyNowLoading.setGroups(LoadingScreen);
    }

    @Override
    protected void onResume() {
        findViewById(R.id.diary_main_ImageNowLoading).setVisibility(View.VISIBLE);
        findViewById(R.id.diary_main_JourneyNowLoading).setVisibility(View.VISIBLE);
        ImageList.setVisibility(View.GONE);
        JourneyList.setVisibility(View.GONE);
        PrepareData();
        super.onResume();
    }

    public void Common_Back_Click(View view) {
        finish();
    }

    public void Common_Profile_Click(View view) {
        startActivity(new Intent(this, AccountActivity.class));
    }

    public void diary_main_CloseMenu(View view) {
        findViewById(R.id.diary_main_menuRoot).setVisibility(View.GONE);
    }

    public void diary_main_EditStart(View view) {
        //(앨범 전용) 수정
        findViewById(R.id.diary_main_menuRoot).setVisibility(View.GONE);

        Intent intent = new Intent(Diary_main.this, Diary_mapNPictures.class);
        intent.putExtra(Diary_mapNPictures.PARCEL_DIARY, DiaryToSend);
        intent.putExtra(RequestCodes.ACTION_EDIT_DIARY, RequestCodes.ACTION_EDIT_DIARY);
        intent.setAction(getIntent().getAction());
        startActivity(intent);
    }

    public void diary_main_SharePicture(View view) {
        //(사진 전용) 공유
        findViewById(R.id.diary_main_menuRoot).setVisibility(View.GONE);
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");


        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".jpg");

        ImageToSend.drawable(getResources(), new Promise<Drawable>() {
            @Override
            public void success(Drawable result) {
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    Bitmap bm = ((BitmapDrawable) result).getBitmap();
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(Diary_main.this, "{package_name}.fileprovider", file));
                startActivity(Intent.createChooser(intent, "사진 공유"));
            }

            @Override
            public void failure(@NotNull Throwable cause) {
                super.failure(cause);
            }
        });

    }

    public void diary_main_Delete(View view) {
        //삭제 확인
        findViewById(R.id.diary_main_menuRoot).setVisibility(View.GONE);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //예

                        if (IsImageSelected) {
                            //이미지 삭제
                            RemoteJava.INSTANCE.deletePicture(((RemotePicture) ImageToSend).getInfo().getId(), new UIPromise<Unit>() {
                                @Override
                                public void success(Unit result) {
                                    Toast.makeText(Diary_main.this, "삭제 완료", Toast.LENGTH_SHORT).show();
                                    PrepareData();
                                }
                            });
                        } else {
                            //다이어리 삭제
                            RemoteJava.INSTANCE.deleteCollection(DiaryToSend.getId(), new UIPromise<Unit>() {
                                @Override
                                public void success(Unit result) {
                                    Toast.makeText(Diary_main.this, "삭제 완료", Toast.LENGTH_SHORT).show();
                                    PrepareData();
                                }
                            });
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //아니오
                        Toast.makeText(Diary_main.this, "삭제 취소", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("확실합니까?")
                .setPositiveButton("삭제", dialogClickListener)
                .setNegativeButton("취소", dialogClickListener)
                .show();
    }

    public void diary_main_AddNewDiary(View view) {
        Intent intent = new Intent(getBaseContext(), Diary_newDiary.class);
        startActivityForResult(intent, RequestCodes.MakeNewDiary);
    }

    public void diary_main_SwitchToLeft(View view) {
        if (isTabLeft)
            return;
        final View bar = findViewById(R.id.diary_main_TabBar_HighLight);

        AdditiveAnimator.animate(bar).setDuration(200).translationX(0).start();
        TabLeft.setVisibility(View.VISIBLE);
        TabRight.setVisibility(View.GONE);
        if (getIntent().getAction().equals(RequestCodes.ACTION_DIARY_MINE)) {
            findViewById(R.id.diary_main_NewImage).setVisibility(View.VISIBLE);
            findViewById(R.id.diary_main_NewDiary).setVisibility(View.GONE);
        }
        isTabLeft = true;
    }

    public void diary_main_SwitchToRight(View view) {
        if (!isTabLeft)
            return;
        final View bar = findViewById(R.id.diary_main_TabBar_HighLight);

        AdditiveAnimator.animate(bar).setDuration(200).translationX(bar.getWidth()).start();
        TabRight.setVisibility(View.VISIBLE);
        TabLeft.setVisibility(View.GONE);
        if (getIntent().getAction().equals(RequestCodes.ACTION_DIARY_MINE)) {
            findViewById(R.id.diary_main_NewImage).setVisibility(View.GONE);
            findViewById(R.id.diary_main_NewDiary).setVisibility(View.VISIBLE);
        }
        isTabLeft = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.MakeNewDiary && resultCode == RESULT_OK) {
            //새로 등록된 다이어리 반영하기
        }
    }

    public void PrepareData() {
        //서버에서 데이터 다운로드
        if (getIntent().getAction().equals(RequestCodes.ACTION_DIARY_MINE)) {
            RemoteJava.INSTANCE.getMyPictures(new UIPromise<List<RemotePicture>>() {
                @Override
                public void success(List<RemotePicture> result) {
                    DownloadedImageList = new ArrayList<>();
                    DownloadedImageList.addAll(result);
                    ImageList.getPicAdapter().clearAllItems();
                    ShowImageData();
                    findViewById(R.id.diary_main_ImageNowLoading).setVisibility(View.GONE);
                    findViewById(R.id.diary_main_ImageList).setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(@NotNull Throwable cause) {
                    Toast.makeText(Diary_main.this, "이미지 데이터 다운로드 실패", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.diary_main_ImageNowLoading).setVisibility(View.GONE);
                    findViewById(R.id.diary_main_ImageList).setVisibility(View.VISIBLE);
                }
            });

            RemoteJava.INSTANCE.getMyCollections(new UIPromise<List<Diary>>() {
                @Override
                public void success(List<Diary> result) {
                    DownloadedDiaryList = result;
                    ShowDiaryData();
                    findViewById(R.id.diary_main_JourneyNowLoading).setVisibility(View.GONE);
                    findViewById(R.id.diary_main_JourneyList).setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(@NotNull Throwable cause) {
                    Toast.makeText(Diary_main.this, "다이어리 데이터 다운로드 실패", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.diary_main_JourneyNowLoading).setVisibility(View.GONE);
                    findViewById(R.id.diary_main_JourneyList).setVisibility(View.VISIBLE);
                }
            });
        } else if (getIntent().getAction().equals(RequestCodes.ACTION_DIARY_OTHERS)) {
            ((TextView) findViewById(R.id.diary_main_Tab_Left_Text)).setText("Image");
            ((TextView) findViewById(R.id.diary_main_Tab_Right_Text)).setText("Diary");
            findViewById(R.id.diary_main_NewImage).setVisibility(View.GONE);
            findViewById(R.id.diary_main_NewDiary).setVisibility(View.GONE);
            RemoteJava.INSTANCE.getRandomPictures(30, new UIPromise<List<RemotePicture>>() {
                @Override
                public void success(List<RemotePicture> result) {
                    DownloadedImageList = new ArrayList<>();
                    DownloadedImageList.addAll(result);
                    ImageList.getPicAdapter().clearAllItems();
                    ShowImageData();
                    findViewById(R.id.diary_main_ImageNowLoading).setVisibility(View.GONE);
                    findViewById(R.id.diary_main_ImageList).setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(@NotNull Throwable cause) {
                    Toast.makeText(Diary_main.this, "이미지 데이터 다운로드 실패", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.diary_main_ImageNowLoading).setVisibility(View.GONE);
                    findViewById(R.id.diary_main_ImageList).setVisibility(View.VISIBLE);
                }
            });

            RemoteJava.INSTANCE.getRandomCollections(new UIPromise<List<Diary>>() {
                @Override
                public void success(List<Diary> result) {
                    DownloadedDiaryList = result;
                    ShowDiaryData();
                    findViewById(R.id.diary_main_JourneyNowLoading).setVisibility(View.GONE);
                    findViewById(R.id.diary_main_JourneyList).setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(@NotNull Throwable cause) {
                    Toast.makeText(Diary_main.this, "다이어리 데이터 다운로드 실패", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.diary_main_JourneyNowLoading).setVisibility(View.GONE);
                    findViewById(R.id.diary_main_JourneyList).setVisibility(View.VISIBLE);
                }
            });
        } else {
            ((TextView) findViewById(R.id.diary_main_Tab_Left_Text)).setText("Image");
            ((TextView) findViewById(R.id.diary_main_Tab_Right_Text)).setText("Diary");
            findViewById(R.id.diary_main_NewImage).setVisibility(View.GONE);
            findViewById(R.id.diary_main_NewDiary).setVisibility(View.GONE);

            IPicture QueryPicture = getIntent().getParcelableExtra("Query");
            GeoPoint point = QueryPicture.getMeta().getGeo();
            PictureQuery query = new PictureQuery();
            assert point != null;
            query.setGeoFilter(new NearGeoPoint(point.getLatitude(), point.getLongitude(), 500));
            RemoteJava.INSTANCE.getPictures(query, new UIPromise<List<RemotePicture>>() {
                @Override
                public void success(List<RemotePicture> result) {
                    DownloadedImageList = new ArrayList<>();
                    DownloadedImageList.addAll(result);
                    ImageList.getPicAdapter().clearAllItems();
                    ShowImageData();
                    findViewById(R.id.diary_main_ImageNowLoading).setVisibility(View.GONE);
                    findViewById(R.id.diary_main_ImageList).setVisibility(View.VISIBLE);
                }
            });

            RemoteJava.INSTANCE.getCollectionsContainPicture(((RemotePicture) QueryPicture).getInfo().getId(), new UIPromise<List<Diary>>() {
                @Override
                public void success(List<Diary> result) {
                    DownloadedDiaryList = result;
                    ShowDiaryData();
                    findViewById(R.id.diary_main_JourneyNowLoading).setVisibility(View.GONE);
                    findViewById(R.id.diary_main_JourneyList).setVisibility(View.VISIBLE);
                    if (getIntent().getAction().equals(RequestCodes.ACTION_DIARY_RELATED_DIARY_FIRST)) {
                        diary_main_SwitchToRight(null);
                    }
                }
            });

        }
    }

    public void ShowImageData() {
        //준비된 Image 데이터를 화면에 표시
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
        if (DownloadedImageList.size() == 0) {
            DownloadedImageList.add(new ResourcePicture(R.drawable.imagenotfoundbordered));
            PictureGroup PictureRow = new PictureGroup("", (ArrayList<IPicture>) DownloadedImageList);
            List<PictureGroup> PictureList = new ArrayList<>();
            PictureList.add(PictureRow);
            ImageList.clearAllItems();
            ImageList.removeAllViews();
            ImageList.getPicAdapter().setColumns(1);
            ImageList.getPicAdapter().setVerticalPadding((int) px);
            ImageList.getPicAdapter().setHorizontalPadding((int) px);
            ImageList.setGroups(PictureList);
        } else {
            PictureGroup PictureRow = new PictureGroup("", (ArrayList<IPicture>) DownloadedImageList);
            List<PictureGroup> PictureList = new ArrayList<>();
            PictureList.add(PictureRow);
            ImageList.getPicAdapter().setColumns(3);
            ImageList.getPicAdapter().setVerticalPadding(0);
            ImageList.getPicAdapter().setHorizontalPadding(0);
            ImageList.getPicAdapter().setImageScaleType(ImageView.ScaleType.CENTER_CROP);
            ImageList.setGroups(PictureList);
            ImageList.removeOnItemTouchListener(ImageTouchListener);
            ImageList.addOnItemTouchListener(ImageTouchListener);
            swipeTouchListener = new OnSwipeTouchListener(this.getBaseContext()) {
                @Override
                public void onSwipeRight() {
                    if (--ZoomIndex < 0)
                        ZoomIndex += DownloadedImageList.size();
                    GlideApp.with(findViewById(R.id.diary_ZoomIn_ZoomImage)).load(DownloadedImageList.get(ZoomIndex)).into((ImageView) findViewById(R.id.diary_ZoomIn_ZoomImage));
                    divideAddress(DownloadedImageList.get(ZoomIndex).getMeta().getAddress());
                }

                @Override
                public void onSwipeLeft() {
                    if (++ZoomIndex == DownloadedImageList.size())
                        ZoomIndex = 0;
                    GlideApp.with(findViewById(R.id.diary_ZoomIn_ZoomImage)).load(DownloadedImageList.get(ZoomIndex)).into((ImageView) findViewById(R.id.diary_ZoomIn_ZoomImage));
                    divideAddress(DownloadedImageList.get(ZoomIndex).getMeta().getAddress());
                }
            };
            findViewById(R.id.diary_ZoomIn_ZoomImage).setOnTouchListener(swipeTouchListener);
        }
    }

    private void divideAddress(String string) {
        int start = 0;
        int end = string.length();
        int firstSpace = string.indexOf(" ");
        if (firstSpace == -1) {
            ((TextView) findViewById(R.id.diary_ZoomIn_ZoomName1)).setText(string);
            findViewById(R.id.diary_ZoomIn_ZoomName2).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.diary_ZoomIn_ZoomName1)).setText(string.substring(start, firstSpace));
            findViewById(R.id.diary_ZoomIn_ZoomName2).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.diary_ZoomIn_ZoomName2)).setText(string.substring(firstSpace + 1, end));
        }
    }/**/

    public void ShowDiaryData() {
        //준비된 Diary 데이터를 화면에 표시
        JourneyListAdapter = new DiaryListAdapter(this, DownloadedDiaryList);
        JourneyList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        JourneyList.setAdapter(JourneyListAdapter);
    }

    public void diary_ZoomIn_CloseZoomIn(View view) {
        findViewById(R.id.diary_ZoomIn_Root).setVisibility(View.INVISIBLE);
    }

    public void diary_main_AddNewImage(View view) {
        //추가할 이미지 선택하기(startactivity)
        startActivity(new Intent(this, Diary_newImage.class));
    }

    public class DiaryListAdapter extends RecyclerView.Adapter<DiaryListViewHolder> {
        private AppCompatActivity RootActivity;
        List<Diary> items;

        public DiaryListAdapter(AppCompatActivity RootActivity, List<Diary> items) {
            if (items == null) {
                Log.e("Diary_main", "Downloaded Data: NULL");
            }
            this.RootActivity = RootActivity;
            this.items = items;
        }

        @NonNull
        @Override
        public DiaryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(RootActivity.getBaseContext()).inflate(R.layout.layout_diary_row, parent, false);
            return new DiaryListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DiaryListViewHolder holder, int position) {
            if (items == null) {
                GlideApp.with(holder.image_Thumbnail).load(R.drawable.diarynotfoundbordered).into(holder.image_Thumbnail);
                holder.NameTag.setVisibility(View.GONE);
                return;
            }
            final Diary diaryToShow = items.get(position);
            try {
                holder.image_Thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
                GlideApp.with(holder.image_Thumbnail).load(diaryToShow.getPictures().get(0)).centerCrop().placeholder(R.drawable.nowloading2).into(holder.image_Thumbnail);
            } catch (IndexOutOfBoundsException e) {
                holder.Root.setVisibility(View.GONE);
                return;
            }
            holder.text_Name.setText(diaryToShow.getTitle());

            Date StartTime = null;
            Date EndTime = null;
            for (int i = 0; i < diaryToShow.getPictures().size(); i++) {
                Date time = diaryToShow.getPictures().get(i).getMeta().getTime();
                if (StartTime == null || time.compareTo(StartTime) < 0) {
                    StartTime = time;
                }
                if (EndTime == null || time.compareTo(EndTime) > 0) {
                    EndTime = time;
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
            String date = sdf.format(StartTime) + " ~ " + sdf.format(EndTime);
            holder.text_Date.setText(date);
            holder.image_Thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Diary_main.this, Diary_mapNPictures.class);
                    intent.putExtra(Diary_mapNPictures.PARCEL_DIARY, diaryToShow);
                    intent.setAction(getIntent().getAction());
                    startActivity(intent);
                }
            });
            holder.image_Thumbnail.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //앨범 롱클릭
                    if (!getIntent().getAction().equals(RequestCodes.ACTION_DIARY_MINE))
                        return true;
                    IsImageSelected = false;
                    DiaryToSend = diaryToShow;
                    findViewById(R.id.diary_main_menuRoot).setVisibility(View.VISIBLE);
                    findViewById(R.id.diary_main_menuEdit).setVisibility(View.VISIBLE);
                    findViewById(R.id.diary_main_menuShare).setVisibility(View.INVISIBLE);
                    return true;
                }
            });

        }

        @Override
        public int getItemCount() {
            if (items == null)
                return 1;
            if (items.size() == 0 || items.get(0).getPictures().size() == 0) {
                items = null;
                return 1;
            }
            return items.size();
        }
    }

    public class DiaryListViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout Root;
        ImageView image_Thumbnail;
        ConstraintLayout NameTag;
        TextView text_Name;
        TextView text_Date;
        int DiaryID;

        public DiaryListViewHolder(View itemView) {
            super(itemView);
            Root = (ConstraintLayout) itemView;
            image_Thumbnail = (ImageView) Root.getViewById(R.id.diary_Row_Thumbnail);
            NameTag = (ConstraintLayout) Root.getViewById(R.id.diary_Row_NameTag);
            text_Name = (TextView) NameTag.getViewById(R.id.diary_Row_Name);
            text_Date = (TextView) NameTag.getViewById(R.id.diary_Row_Date);
        }
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.diary_ZoomIn_Root).getVisibility() == View.VISIBLE) {
            findViewById(R.id.diary_ZoomIn_Root).setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}
