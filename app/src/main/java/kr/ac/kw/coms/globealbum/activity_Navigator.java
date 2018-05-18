package kr.ac.kw.coms.globealbum;
/* 작성자: 이상훈 */
import android.content.Context;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class activity_Navigator extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<String> folderList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigator);

        WindowManager wm = (WindowManager)getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final LinearLayout maplayout = (LinearLayout)findViewById(R.id.MapLayout);
        Point size = new Point();
        display.getSize(size);
        maplayout.setMinimumHeight((int)(size.y * 0.4));
    }

    public void makeList()
    {
        folderList = SearchImageFolders();
        mRecyclerView = (RecyclerView)findViewById(R.id.MapImgView);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MapImageAdapter(folderList);
        mRecyclerView.setAdapter(mAdapter);
    }

    public ArrayList<String> SearchImageFolders()
    {
        ArrayList<String> folderList = new ArrayList<>();



        return folderList;
    }
}
