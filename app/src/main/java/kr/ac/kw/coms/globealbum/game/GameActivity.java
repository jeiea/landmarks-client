package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.osmdroid.config.Configuration;

import kr.ac.kw.coms.globealbum.common.PictureDialogFragment;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.R;


public class GameActivity extends AppCompatActivity {
    Context context = null;
    MyMapView myMapView= null;
    ImageView[] imageView=null;
    Animation animationFadeAway;
    final int PICTURE_NUM = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        //퀴즈에 나올 사진들을 연결
        imageView = new ImageView[PICTURE_NUM];
        imageView[0] = (ImageView)findViewById(R.id.picture1);
        imageView[1] = (ImageView)findViewById(R.id.picture2);
        imageView[2] = (ImageView)findViewById(R.id.picture3);
        imageView[3] = (ImageView)findViewById(R.id.picture4);

        imageView[0].setOnClickListener(new PictureClickListener());
        imageView[1].setOnClickListener(new PictureClickListener());
        imageView[2].setOnClickListener(new PictureClickListener());
        imageView[3].setOnClickListener(new PictureClickListener());


        //animationFadeAway= AnimationUtils.loadAnimation(this,R.anim.fade_away);

        //osmdroid 초기 구성
        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        myMapView = (MyMapView)findViewById(R.id.map);
    }


    //onCreate 진행 후 애니메이션 실행
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //imageView.startAnimation(GameActivity.this.animationFadeAway);

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


    //사진 클릭 시 크게 띄워주는 이벤트 등록
    class PictureClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {    //이미지뷰를 다이얼로그로 화면에 표시
            ImageView imgv = (ImageView)view;
            PictureDialogFragment pdf = PictureDialogFragment.Companion.newInstance(imgv.getDrawable());
            pdf.show(getSupportFragmentManager(), "wow");
        }
    }

}
