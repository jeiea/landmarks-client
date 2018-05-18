package kr.ac.kw.coms.globealbum;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Display;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class activity_Navigator extends AppCompatActivity {
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
}
