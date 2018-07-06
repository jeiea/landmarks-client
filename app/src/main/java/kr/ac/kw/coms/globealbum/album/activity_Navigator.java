package kr.ac.kw.coms.globealbum.album;
/* 작성자: 이상훈 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.provider.PictureProvider;

public class activity_Navigator extends AppCompatActivity {
    float density;
    ArrayList<PictureGroup> data;
    kr.ac.kw.coms.globealbum.album.GroupDiaryView recycle_gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigator);

        density = getResources().getDisplayMetrics().density;
        WindowManager wm = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final LinearLayout mapLayout = (LinearLayout) findViewById(R.id.MapLayout);
        Point size = new Point();
        display.getSize(size);
        mapLayout.setMinimumHeight((int) (size.y * 0.5));
        final View Divider = (View)findViewById(R.id.Divider);

        View.OnDragListener mDragListener = new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                View view;
                if (v instanceof View) {
                    view = (View) v;
                } else {
                    return false;
                }

                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        if (!event.getResult()) {
                            ViewGroup.LayoutParams params = mapLayout.getLayoutParams();
                            params.height = (int) event.getY() - (int)(density * 24) - (int)(Divider.getHeight() / 2);
                            mapLayout.setLayoutParams(params);
                        }
                        return true;

                }

                return true;
            }
        };


        Divider.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                v.startDrag(null, new CanvasShadow(v), null, 0);
                return false;
            }
        });
        Divider.setOnDragListener(mDragListener);

        getData();
        recycle_gallery = (kr.ac.kw.coms.globealbum.album.GroupDiaryView)findViewById(R.id.recycle_gallery);
        recycle_gallery.setGroups((List)data);
    }

    class CanvasShadow extends View.DragShadowBuilder {
        int width, height;

        public CanvasShadow(View v) {
            super(v);
            width = v.getWidth();
            height = v.getHeight();
        }

        @Override
        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
            shadowSize.set(width * 2, height);
            shadowTouchPoint.set(width, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            Paint pnt = new Paint();
            pnt.setColor(Color.GRAY);
            canvas.drawRect(0, 0, width * 2, height, pnt);
        }
    }

    class PictureGroup
    {
        public String name;
        ArrayList<Bitmap> PictureList;
        ArrayList<PictureProvider.Picture> pics;
        public PictureGroup(String name, ArrayList<Bitmap> PictureList)
        {
            this.name = name;
            //TODO: Bitmap to Picture
            this.PictureList = PictureList;
        }
    }

    public void getData()
    {
        data = new ArrayList<>();
        ArrayList<Bitmap> group1 = new ArrayList<>();
        group1.add(BitmapFactory.decodeResource(getResources(), R.drawable.sample0));
        group1.add(BitmapFactory.decodeResource(getResources(), R.drawable.sample1));
        group1.add(BitmapFactory.decodeResource(getResources(), R.drawable.sample2));
        group1.add(BitmapFactory.decodeResource(getResources(), R.drawable.sample3));
        group1.add(BitmapFactory.decodeResource(getResources(), R.drawable.sample4));
        data.add(new PictureGroup("group 1", group1));
        ArrayList<Bitmap> group2 = new ArrayList<>();
        group2.add(BitmapFactory.decodeResource(getResources(), R.drawable.sample5));
        group2.add(BitmapFactory.decodeResource(getResources(), R.drawable.sample6));
        group2.add(BitmapFactory.decodeResource(getResources(), R.drawable.sample7));
        group2.add(BitmapFactory.decodeResource(getResources(), R.drawable.sample8));
        group2.add(BitmapFactory.decodeResource(getResources(), R.drawable.sample9));
        data.add(new PictureGroup("group 2", group2));
    }

    class GroupDiaryView extends RecyclerView
    {
        GroupedPicAdapter picAdapter = new GroupedPicAdapter();
        public GroupDiaryView(Context context) {
            this(context, null, 0);
        }
        public GroupDiaryView(Context context, AttributeSet attrs, int defStyle)
        {
            super(context, attrs, defStyle);
            this.setAdapter(picAdapter);
            FlexboxLayoutManager mFlexboxLayoutManager = new FlexboxLayoutManager(context);
            mFlexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
            mFlexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
            mFlexboxLayoutManager.setAlignItems(AlignItems.STRETCH);
            this.setLayoutManager(mFlexboxLayoutManager);
        }
    }

    class GroupedPicAdapter extends RecyclerView.Adapter<GroupedPicAdapter.ElementViewHolder>
    {
        public ArrayList<Object> viewData = new ArrayList<>();
        //String or Picture

        @NonNull
        @Override
        public GroupedPicAdapter.ElementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ElementViewHolder vh = null;
            if (viewType == 0)
            {
                SeparatorHolder viewHolder = new SeparatorHolder(new TextView(parent.getContext()));
                ViewGroup.LayoutParams params = viewHolder.textView.getLayoutParams();
                params.height = 40;
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                viewHolder.textView.setBackgroundColor(0xFFFFFF88);
                viewHolder.textView.setLayoutParams(params);
                vh = viewHolder;
            }
            else
            {
                PictureHolder viewHolder = new PictureHolder(new ImageView(parent.getContext()));
                DisplayMetrics metrics = parent.getResources().getDisplayMetrics();
                int mw = metrics.widthPixels / 3;
                int mh = metrics.heightPixels / 4;
                viewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ViewGroup.LayoutParams params = viewHolder.imageView.getLayoutParams();
                params.width = mw;
                params.height = mh;
                viewHolder.imageView.setLayoutParams(params);
            }
            return vh;
        }

        public int getItemViewType(int position)
        {
            return viewData.get(position) instanceof String ? 0: 1;
        }

        @Override
        public void onBindViewHolder(@NonNull GroupedPicAdapter.ElementViewHolder holder, int position) {
            if (holder instanceof SeparatorHolder)
            {
                ((SeparatorHolder) holder).textView.setText(viewData.get(position).toString());
            }
            else if (holder instanceof PictureHolder)
            {
                PictureProvider.Picture pic = (PictureProvider.Picture)viewData.get(position);
                ((PictureHolder) holder).imageView.setImageDrawable(pic.getDrawable());
            }
        }

        @Override
        public int getItemCount() {
            return viewData.size();
        }

        class ElementViewHolder extends RecyclerView.ViewHolder
        {
            View view;
            public ElementViewHolder(View itemView) {
                super(itemView);
            }
        }
        class SeparatorHolder extends ElementViewHolder
        {
            TextView textView;
            public SeparatorHolder(View itemView) {
                super(itemView);
            }
        }
        class PictureHolder extends ElementViewHolder
        {
            ImageView imageView;
            public PictureHolder(View itemView) {
                super(itemView);
            }
        }
    }
}