package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;


import java.util.ArrayList;
import java.util.List;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.PictureDialogFragment;
import kr.ac.kw.coms.globealbum.map.MyMapView;

import static kr.ac.kw.coms.globealbum.game.GameActivity.GameState.Answered;
import static kr.ac.kw.coms.globealbum.game.GameActivity.GameState.Solving;
import static kr.ac.kw.coms.globealbum.game.GameActivity.TimerState.Running;
import static kr.ac.kw.coms.globealbum.game.GameActivity.TimerState.Stop;


public class GameActivity extends AppCompatActivity {
    Context context = null;
    MyMapView myMapView = null;
    ImageView[] imageView = null;
    ProgressBar progressBar = null;
    TextView stageTextView=null;

    final int PICTURE_NUM = 4;
    int stage=1;
    TimerState stopTimer=Running;

    final int STAGE_TIME = 14;
    MapEventsOverlay listenerOverlay;

    Marker currentMarker;   //사용자가 찍은 마커
    Marker answerMarker;    //정답 마커
    Polyline polyline;  //마커 사이를 이어주는 직선

    enum GameState {
        Solving,
        Answered,
    }
    enum TimerState{
        Stop,
        Running
    }
    GameState currentState = Solving;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        //퀴즈에 나올 사진들을 연결
        imageView = new ImageView[PICTURE_NUM];
        imageView[0] = findViewById(R.id.picture1);
        //imageView[1] = findViewById(R.id.picture2);
        //imageView[2] = findViewById(R.id.picture3);
        //imageView[3] = findViewById(R.id.picture4);
        progressBar = findViewById(R.id.progressbar);
        stageTextView = findViewById(R.id.textview_stage);

        imageView[0].setOnClickListener(new PictureClickListener());
        //imageView[1].setOnClickListener(new PictureClickListener());
        //imageView[2].setOnClickListener(new PictureClickListener());
        //imageView[3].setOnClickListener(new PictureClickListener());

        stageTextView.setText("Stage "+stage);

        //osmdroid 초기 구성
        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        myMapView = findViewById(R.id.map);

        //마커 이벤트 등록
        listenerOverlay = markerEvent();
        myMapView.getOverlays().add(listenerOverlay);

        //정답 마커 등록
        setAnswerMarker(new GeoPoint(48.85625,2.34375),"Paris, France",R.drawable.sample8);    //파리를 정답으로 등록


        timeThreadHandler();
    }



    //제한 시간 측정하는 스레드
    private void timeThreadHandler(){
        progressBar.setMax(STAGE_TIME-stage);
        progressBar.setProgress(STAGE_TIME-stage);

        new Thread(new Runnable() {
            int value = STAGE_TIME-stage;
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        progressBar.setProgress(--value);
                        if (value == 0||stopTimer == Stop) {
                            if(value==0){
                                currentMarker=new Marker(myMapView);
                                currentState = Answered;

                                myMapView.getOverlays().add(answerMarker);
                                myMapView.invalidate();
                            }
                            Thread.interrupted();
                            break;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //맵뷰를 클릭하였을 때 발생하는 이벤트
    //마커를 화면에 띄우고, 또 한번 클릭할 경우 정답 확인으로 넘어간다.
    private MapEventsOverlay markerEvent() {
        return new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {   //화면 한번 터치시

                if (currentMarker != null) {
                    currentMarker.setPosition(p);
                }
                else {
                    Marker marker = addUserMarker(p);
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
    private void animateMarker(final MapView map, final Marker marker, final GeoPoint finalPosition, final GeoPointInterpolator GeoPointInterpolator) {
        final GeoPoint startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 2000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed=  SystemClock.uptimeMillis() - start;
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
                    myMapView.getOverlays().add(answerMarker);
                    addPolyline(currentMarker.getPosition(),answerMarker.getPosition());
                    map.invalidate();
                }
            }
        });
    }

    //사용자가 정한 마커와 정답 마커 사이를 잇는 직선 생성
    private void addPolyline(GeoPoint startPosition, GeoPoint destPosition){
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(startPosition);
        geoPoints.add(destPosition);
        polyline = new Polyline();
        polyline.setPoints(geoPoints);
        polyline.setColor(Color.GRAY);

        myMapView.getOverlays().add(polyline);
    }


    //두 좌표 사이의 거리를 구해서 리턴
    private int calcDistance(GeoPoint geoPoint1, GeoPoint geoPoint2){

        final  double PI = 3.14159265358979323846;
        double rad = PI/180;
        double latitude1 = geoPoint1.getLatitude()*rad;
        double longitude1 = geoPoint1.getLongitude()*rad;
        double latitude2 = geoPoint2.getLatitude()*rad;
        double longitude2 = geoPoint2.getLongitude()*rad;

        double radius = 6378.137;   //earch radius

        double dlon = longitude2-longitude1;
        double distance = Math.acos(Math.sin(latitude1)* Math.sin(latitude2) + Math.cos(latitude1)*Math.cos(latitude2)*Math.cos(dlon))*radius;

        return (int)distance;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) { //문제를 맞춘 후 다시 맵 로드
        if (currentState == Answered) {

            currentMarker.remove(myMapView);
            currentMarker = null;

            answerMarker.closeInfoWindow();
            myMapView.getOverlays().remove(answerMarker);

            setAnswerMarker(new GeoPoint(41.895466,12.482323),"Roma, ITALY",R.drawable.sample1); //로마의 좌표
            myMapView.getOverlays().remove(polyline);   //polyline 삭제

            myMapView.invalidate();

            currentState = Solving;

            stage++;
            stageTextView.setText("Stage "+stage);
            stopTimer=Running;

            timeThreadHandler();

            return true;
        }
        return super.dispatchTouchEvent(ev);
    }


    //사용자 마커 생성
    private Marker addUserMarker(final GeoPoint geoPoint){
        Marker marker = new Marker(myMapView);
        final Drawable drawable = getResources().getDrawable(R.drawable.blue_flag);
        marker.setIcon(drawable);
        marker.setPosition(geoPoint);
        marker.setAnchor(0.0f,1.0f);
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {  //생성된 마커를 클릭하여 화면에 등록
                Toast.makeText(context, "마커 등록 완료", Toast.LENGTH_SHORT).show();
                currentState = Answered;
                stopTimer=Stop;

                //정답 확인 부분
                Marker tmpMarker = new Marker(myMapView);
                tmpMarker.setIcon(drawable);
                tmpMarker.setPosition(marker.getPosition());
                tmpMarker.setAnchor(0.0f,1.0f);
                myMapView.getOverlays().add(tmpMarker);


                answerMarker.setSnippet(calcDistance(geoPoint,answerMarker.getPosition())+"Km");
                animateMarker(myMapView,tmpMarker,answerMarker.getPosition(),new GeoPointInterpolator.Spherical());

                myMapView.invalidate();
                return true;
            }
        });
        return marker;
    }

    //정답 마커 생성
    private void setAnswerMarker(GeoPoint geoPoint,String name,int id){
        answerMarker = new Marker(myMapView);
        Drawable drawable = getResources().getDrawable(R.drawable.red_flag);
        answerMarker.setIcon(drawable);
        answerMarker.setAnchor(0.0f,1.0f);
        answerMarker.setTitle(name);
        answerMarker.showInfoWindow();
        answerMarker.setPosition(geoPoint);

        imageView[0].setImageResource(id);

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
