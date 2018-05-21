package kr.ac.kw.coms.globealbum.album;
/* 작성자: 이상훈 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.DragEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;

public class activity_Navigator extends AppCompatActivity {
    private ArrayList<String> folderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigator);

        WindowManager wm = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final LinearLayout mapLayout = (LinearLayout) findViewById(R.id.MapLayout);
        Point size = new Point();
        display.getSize(size);
        mapLayout.setMinimumHeight((int) (size.y * 0.5));

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
                        if (event.getResult())
                            Toast.makeText(activity_Navigator.this, "Drag&Drop ended", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return true;
            }
        };

        View Divider = (View)findViewById(R.id.Divider);
        Divider.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                v.startDrag(null, new CanvasShadow(v), null, 0);
                return false;
            }
        });
    }

    public void makeList() {
        folderList = SearchImageFolders();
    }

    public ArrayList<String> SearchImageFolders() {
        ArrayList<String> folderList = new ArrayList<>();

        folderList.add("ADDRESS 1");
        folderList.add("ADDRESS 2");
        folderList.add("ADDRESS 3");

        return folderList;
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
}
