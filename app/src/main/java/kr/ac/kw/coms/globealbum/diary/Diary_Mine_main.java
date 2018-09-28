package kr.ac.kw.coms.globealbum.diary;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

import kr.ac.kw.coms.globealbum.common.RequestCodes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GalleryDetail;
import kr.ac.kw.coms.globealbum.album.GroupDiaryView;
import kr.ac.kw.coms.globealbum.album.PictureArray;
import kr.ac.kw.coms.globealbum.album.PictureGroup;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;
import kr.ac.kw.coms.globealbum.common.RecyclerItemClickListener;
import kr.ac.kw.coms.globealbum.provider.IPicture;

public class Diary_Mine_main extends AppCompatActivity {

    GroupDiaryView diaryImageView = null;
    GroupDiaryView diaryJourneyView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_mine_main);


        TabHost tabHost = (TabHost) findViewById(R.id.diary_mine_main_TabHost);
        tabHost.setup();
        prepareViewSample();

        TabHost.TabSpec ts1 = tabHost.newTabSpec("Tab_ImageList");
        ts1.setIndicator("내 이미지");
        ts1.setContent(R.id.diary_mine_main_ImageList);


        TabHost.TabSpec ts2 = tabHost.newTabSpec("Tab_JourneyList");
        ts2.setIndicator("내 여행지");
        ts2.setContent(R.id.diary_mine_main_JourneyList);


        tabHost.addTab(ts1);
        tabHost.addTab(ts2);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.d("TAB", tabId);
            }
        });
    }

    public void prepareViewSample() {
        diaryImageView = findViewById(R.id.diary_mine_main_ImageList);
        diaryJourneyView = findViewById(R.id.diary_mine_main_JourneyList);

        ArrayList<PictureGroup> elementList = new ArrayList<>();
        PictureArray elementRow = new PictureArray();
        Context c = getBaseContext();
        elementRow.add(new ResourcePicture(R.drawable.sample0));
        elementRow.add(new ResourcePicture(R.drawable.sample1));
        elementRow.add(new ResourcePicture(R.drawable.sample2));
        elementRow.sort();

        final ArrayList<IPicture> pictures = new ArrayList<>(elementRow);

        elementList.add(new PictureGroup("My Photos", elementRow));
        diaryImageView.setGroups(elementList);
        diaryImageView.addOnItemTouchListener(new RecyclerItemClickListener(diaryImageView) {
            @Override
            public void onItemClick(@NotNull View view, int position) {
                super.onItemClick(view, position);
                Object o = diaryImageView.getPicAdapter().getViewData().get(position);
                if (o instanceof ResourcePicture) {
                    Toast.makeText(Diary_Mine_main.this, ((ResourcePicture) o).getTitle(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getBaseContext(), GalleryDetail.class);
                    intent.putParcelableArrayListExtra("pictures", pictures);
                    intent.putExtra("index", position-1);
                    startActivity(intent);
                }
            }
        }.getItemTouchListener());


        elementList.clear();
        elementRow.clear();
        elementRow.add(new ResourcePicture(R.drawable.sample0));
        elementRow.add(new ResourcePicture(R.drawable.sample1));
        elementRow.setOnLongClickListener(0, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                findViewById(R.id.diary_mine_main_menuRoot).setVisibility(View.VISIBLE);
                return true;
            }
        });
        elementRow.setOnLongClickListener(1, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                findViewById(R.id.diary_mine_main_menuRoot).setVisibility(View.VISIBLE);
                return true;
            }
        });
        elementList.add(new PictureGroup("group1", elementRow));
        elementList.add(new PictureGroup("group2", elementRow));
        diaryJourneyView.setGroups(elementList);
        diaryJourneyView.addOnItemTouchListener(new RecyclerItemClickListener(diaryJourneyView) {
            @Override
            public void onItemClick(@NotNull View view, int position) {
                super.onItemClick(view, position);
                Object o = diaryJourneyView.getPicAdapter().getViewData().get(position);
                if (o instanceof ResourcePicture) {
                    ResourcePicture rp = (ResourcePicture) o;
                    Toast.makeText(Diary_Mine_main.this, String.valueOf(rp.getTitle()), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getBaseContext(), Diary_mapNPictures.class).putExtra("whose", "mine"));
                }
            }
        }.getItemTouchListener());
    }

    public void diary_onBackClick(View view) {
        finish();
    }

    public void diary_mine_main_CloseMenu(View view) {
        findViewById(R.id.diary_mine_main_menuRoot).setVisibility(View.GONE);
    }

    public void diary_mine_main_EditStart(View view) {
        findViewById(R.id.diary_mine_main_menuRoot).setVisibility(View.GONE);
        Intent intent = new Intent(getBaseContext(), Diary_mapNPictures.class).putExtra("order", "edit");
        startActivity(intent);
    }

    public void diary_mine_main_Delete(View view) {
        //삭제 확인
        findViewById(R.id.diary_mine_main_menuRoot).setVisibility(View.GONE);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //예
                        Toast.makeText(Diary_Mine_main.this, "DELETE", Toast.LENGTH_SHORT).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //아니오
                        Toast.makeText(Diary_Mine_main.this, "CANCEL", Toast.LENGTH_SHORT).show();
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

    public void diary_mine_main_AddNewDiary(View view) {
        Intent intent = new Intent(getBaseContext(), diary_new.class);
        startActivityForResult(intent, RequestCodes.MakeNewDiary);
    }
}
