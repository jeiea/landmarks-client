package kr.ac.kw.coms.globealbum;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
 //GalleryView로 교체 사용 예정
public class GalleryActivity extends AppCompatActivity {
    ArrayList<Bitmap> ThumbnailList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        GridAdapter adapter = new GridAdapter(getApplicationContext(), R.layout.galleryimagecell);
        GridView gv = (GridView)findViewById(R.id.GalleryGridView);
        gv.setAdapter(adapter);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //사진 선택 시 후속 과정
            }
        });
    }

    public void GalleryExit_click(View view) {
        this.finishActivity(-1);
    }

    class GridAdapter extends BaseAdapter
    {
        Context context;
        int Layout;
        LayoutInflater inf;

        @Override
        public int getCount() {
            return ThumbnailList.size();
        }

        @Override
        public Object getItem(int i) {
            return ThumbnailList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null)
                view = inf.inflate(Layout, null);
            ImageView iv = (ImageView)view.findViewById(R.id.ImageCell);
            iv.setImageBitmap(ThumbnailList.get(i));
            return view;
        }

        public GridAdapter(Context context, int Layout)
        {
            this.context = context;
            this.Layout = Layout;
            inf = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }
}
