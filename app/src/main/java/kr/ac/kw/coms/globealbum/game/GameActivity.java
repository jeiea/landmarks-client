package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.osmdroid.config.Configuration;

import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.R;


public class GameActivity extends AppCompatActivity {
    Context context = null;
    MyMapView myMapView= null;
    ImageView imageView=null;
    Animation animationFadeAway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        //osmdroid 초기 구성
        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        myMapView = (MyMapView)findViewById(R.id.map);
        imageView = (ImageView)findViewById(R.id.test_imageview);

        animationFadeAway= AnimationUtils.loadAnimation(this,R.anim.fade_away);


    }


    //onCreate 진행 후 애니메이션 실행
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        imageView.startAnimation(GameActivity.this.animationFadeAway);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myMapView != null)
            myMapView.onResume(); //osmdroid configuration refresh
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myMapView != null)
            myMapView.onPause(); //osmdroid configuration refresh
    }

}
