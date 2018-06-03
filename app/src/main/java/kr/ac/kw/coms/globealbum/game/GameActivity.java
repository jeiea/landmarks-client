package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;



import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.PictureDialogFragment;
import kr.ac.kw.coms.globealbum.map.MyMapView;

import static kr.ac.kw.coms.globealbum.game.GameActivity.GameState.Answered;
import static kr.ac.kw.coms.globealbum.game.GameActivity.GameState.Solving;


public class GameActivity extends AppCompatActivity {
    Context context = null;
    MyMapView myMapView = null;
    ImageView[] imageView = null;
    final int PICTURE_NUM = 4;
    MapEventsOverlay listenerOverlay;
    Marker currentMarker;


    enum GameState {
        Solving,
        Answered,
    }

    class Answer{
        Marker answerMarker;
        GeoPoint answerGeopoint;
    }

    Answer answer;
    GameState currentState = Solving;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        //퀴즈에 나올 사진들을 연결
        imageView = new ImageView[PICTURE_NUM];
        imageView[0] = findViewById(R.id.picture1);
        imageView[1] = findViewById(R.id.picture2);
        imageView[2] = findViewById(R.id.picture3);
        imageView[3] = findViewById(R.id.picture4);

        imageView[0].setOnClickListener(new PictureClickListener());
        //imageView[1].setOnClickListener(new PictureClickListener());
        //imageView[2].setOnClickListener(new PictureClickListener());
        //imageView[3].setOnClickListener(new PictureClickListener());


        imageView[0].setImageResource(R.drawable.sample8);


        //osmdroid 초기 구성
        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        myMapView = findViewById(R.id.map);

        //마커 이벤트 등록
        listenerOverlay = markerEvent();
        myMapView.getOverlays().add(listenerOverlay);

        //정답 마커 등록
        answer = new Answer();
        answer.answerMarker = new Marker(myMapView);
        Drawable drawable = getResources().getDrawable(R.drawable.red_flag);
        answer.answerMarker.setIcon(drawable);
        answer.answerMarker.setAnchor(0.0f,1.0f);
        answer.answerGeopoint = new GeoPoint(48.8710,2.4131);   //파리의 좌표
        answer.answerMarker.setPosition(answer.answerGeopoint);



    }

    private MapEventsOverlay markerEvent() {
        return new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {   //화면 한번 터치시

                if (currentMarker != null) {
                    currentMarker.setPosition(p);
                }
                else {
                    Marker marker = new Marker(myMapView);

                    final Drawable drawable = getResources().getDrawable(R.drawable.blue_flag);
                    marker.setIcon(drawable);
                    marker.setPosition(p);
                    marker.setAnchor(0.0f,1.0f);
                    marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {  //생성된 마커를 클릭하여 화면에 등록
                            Toast.makeText(context, "마커 등록 완료", Toast.LENGTH_SHORT).show();
                            currentState = Answered;

                            //정답 확인 부분
                            Marker tmpMarker = new Marker(myMapView);
                            tmpMarker.setIcon(drawable);
                            tmpMarker.setPosition(marker.getPosition());
                            tmpMarker.setAnchor(0.0f,1.0f);
                            myMapView.getOverlays().add(tmpMarker);

                            animateMarker(myMapView,tmpMarker,answer.answerMarker.getPosition(),new GeoPointInterpolator.Spherical());

                            myMapView.invalidate();
                            return true;
                        }
                    });
                    currentMarker = marker;
                    myMapView.getOverlays().add(marker);
                }

                myMapView.invalidate();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {    //길게 터치시
                return false;
            }
        });
    }


    //사용자가 찍은 마커가 위치에서 시작하여 정답마커까지 이동하는 애니메이션
    public void animateMarker(final MapView map, final Marker marker, final GeoPoint finalPosition, final GeoPointInterpolator GeoPointInterpolator) {
        final GeoPoint startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(GeoPointInterpolator.interpolate(v, startPosition, finalPosition));
                map.invalidate();
                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
                else{   //정답 마커 위치로 이동되면 정답 마커 추가
                    marker.remove(myMapView);
                    myMapView.getOverlays().add(answer.answerMarker);
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) { //문제를 맞춘 후 다시 맵 로드
        if (currentState == Answered) {
            currentMarker.remove(myMapView);
            currentMarker = null;

            imageView[0].setImageResource(R.drawable.sample1);

            myMapView.getOverlays().remove(answer.answerMarker);
            answer.answerGeopoint = new GeoPoint(41.895466,12.482323);  //로마의 좌표
            answer.answerMarker.setPosition(answer.answerGeopoint);

            myMapView.invalidate();
            currentState = Solving;
            return true;
        }
        return super.dispatchTouchEvent(ev);
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

    private void setAnswerMarker(){
        //currentMarker.setPosition();
    }

    //사진 클릭 시 크게 띄워주는 이벤트 등록
    class PictureClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {    //이미지뷰를 다이얼로그로 화면에 표시
            ImageView imgv = (ImageView) view;
            PictureDialogFragment pdf = PictureDialogFragment.Companion.newInstance(imgv.getDrawable());
            pdf.show(getSupportFragmentManager(), "wow");
        }
    }
}
