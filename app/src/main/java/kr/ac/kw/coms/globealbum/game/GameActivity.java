package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
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
import java.util.Date;
import java.util.List;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.PictureDialogFragment;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.EXIFinfo;

import static kr.ac.kw.coms.globealbum.game.GameActivity.GameState.Answered;
import static kr.ac.kw.coms.globealbum.game.GameActivity.GameState.Solving;
import static kr.ac.kw.coms.globealbum.game.GameActivity.TimerState.Running;
import static kr.ac.kw.coms.globealbum.game.GameActivity.TimerState.Stop;


public class GameActivity extends AppCompatActivity {
    Context context = null;
    MyMapView myMapView = null;
    ImageView questionImageView = null;
    ProgressBar progressBar = null;
    TextView stageTextView = null;
    Button menuButton = null;
    TextView gotonextTextView = null;
    TextView scoreTextView = null;


    Drawable RED_FLAG_DRAWABLE;
    Drawable BLUE_FLAG_DRAWABLE;
    final int PICTURE_NUM = 8;
    int problem = 0;
    int score = 0;
    int timeScore = 0;
    int distanceScore = 0;
    int stage = 1;
    /**
     * 제한시간 타이머가 돌아가는 중인지.
     */
    TimerState stopTimer = Running;
    private Handler animateHandler = null;


    final int TIME_LIMIT_MS = 14000;
    MapEventsOverlay listenerOverlay;

    Marker currentMarker;   //사용자가 찍은 마커
    Marker answerMarker;    //정답 마커
    Polyline polyline;  //마커 사이를 이어주는 직선

    List<PictureInfo> questionPic= new ArrayList<>();



    enum GameState {
        Solving,
        Answered,
    }

    enum TimerState {
        Stop,
        Running
    }

    GameState currentState = Solving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        progressBar = findViewById(R.id.progressbar);
        stageTextView = findViewById(R.id.textview_stage);
        scoreTextView = findViewById(R.id.textview_score);
        menuButton = findViewById(R.id.game_button_menu);
        menuButton.setOnClickListener(new MenuButtonClickListener());
        gotonextTextView = findViewById(R.id.gotonext_textview);

        RED_FLAG_DRAWABLE = getResources().getDrawable(R.drawable.red_flag);
        BLUE_FLAG_DRAWABLE = getResources().getDrawable(R.drawable.blue_flag);

        stageTextView.setText("STAGE " + stage);

        //퀴즈에 나올 사진들을 연결
        questionImageView = findViewById(R.id.picture);
        questionImageView .setOnClickListener(new PictureClickListener());



        //osmdroid 초기 구성
        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        myMapView = findViewById(R.id.map);


        /*
        //      setMapsforge
        //      https://github.com/osmdroid/osmdroid/wiki/Mapsforge


        MapsForgeTileSource.createInstance(getApplication());
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File f = new File(path);
        File[] maps = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getAbsolutePath().matches(".*\\.map");            }
        });  //TODO scan/prompt for map files (.map)
        Toast.makeText(context, maps[0].toString()+"", Toast.LENGTH_SHORT).show();


        XmlRenderTheme theme = null; //null is ok here, uses the default rendering theme if it's not set

        try {
            //this file should be picked up by the mapsforge dependencies
            theme = new AssetsRenderTheme(this.getApplicationContext(), "renderthemes/", "rendertheme-v4.xml");
            //alternative: theme = new ExternalRenderTheme(userDefinedRenderingFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        MapsForgeTileSource fromFiles = MapsForgeTileSource.createFromFiles(maps, theme, "rendertheme-v4");
        MapsForgeTileProvider forge = new MapsForgeTileProvider(
                new SimpleRegisterReceiver(context),
                fromFiles, null);

        myMapView.setTileProvider(forge);
        myMapView.getController().setZoom(fromFiles.getMinimumZoomLevel());
        myMapView.zoomToBoundingBox(fromFiles.getBoundsOsmdroid(), true);
        */


        //마커 이벤트 등록
        listenerOverlay = markerEvent();
        myMapView.getOverlays().add(listenerOverlay);

        setQuestion();

        //정답 마커 등록
        setAnswerMarker(questionPic.get(problem));    //파리를 정답으로 등록
        setQuestion();
        timeThreadHandler();
    }
    class PictureInfo{  //사진에 필요한 정보를 가지고 있는 클래스
        int id;
        GeoPoint geoPoint;
        String name;
    }
    //문제 세팅
    private void setQuestion(){
        int[] id = new int[PICTURE_NUM];
        EXIFinfo exifInfo = new EXIFinfo();
        for(int i = 0 ; i < PICTURE_NUM ; i++){ //사진 리소스 id 배열에 저장
            id[i]= R.drawable.coord0+i;
        }
        for(int i = 0 ; i < 1000; i++) {    //반복하여 리소스 id 섞음
            int random = (int)(Math.random()*PICTURE_NUM);
            int tmp = id[0];
            id[0] = id[random];
            id[random] = tmp;
        }
        for(int i = 0; i < PICTURE_NUM; i++){
            //GPS 정보 뽑아오기
            exifInfo.setMetadata(getResources().openRawResource(id[i]));
            GeoPoint geoPoint = exifInfo.getLocationGeopoint();
            //String name =;
            PictureInfo pictureInfo = new PictureInfo();
            pictureInfo.geoPoint=geoPoint;
            pictureInfo.id=id[i];
            //pictureInfo.name=
            questionPic.add(pictureInfo);

        }
    }

    //제한 시간 측정
    private void timeThreadHandler() {
        int stageTimeLimitMs = TIME_LIMIT_MS - problem * 1000;
        progressBar.setMax(stageTimeLimitMs);

        final long deadlineMs = new Date().getTime() + stageTimeLimitMs;
        final Handler ui = new Handler();
        ui.post(new Runnable() {
            @Override
            public void run() {
                long timeLeft = deadlineMs - new Date().getTime();
                progressBar.setProgress((int) timeLeft);

                timeScore = (int) timeLeft;

                if (timeLeft > 0 && stopTimer != Stop) {
                    ui.postDelayed(this, 35); // about 30fps
                    return;
                } else if (stopTimer == Stop) {
                    return;
                }

                // 화면을 한번 터치해 마커를 생성하고 난 후
                // 타임아웃 발생시 그 마커를 위치로 정답 확인
                if (currentMarker != null) {
                    timeOutAddUserMarker();
                } else {
                    //화면에 마커 생성 없이 타임아웃 발생시 정답 확인
                    currentMarker = new Marker(myMapView);
                    currentState = Answered;

                    answerMarker.showInfoWindow();
                    myMapView.getOverlays().add(answerMarker);

                }
                myMapView.invalidate();
            }
        });
    }

    //맵뷰를 클릭하였을 때 발생하는 이벤트
    //마커를 화면에 띄우고, 또 한번 클릭할 경우 정답 확인으로 넘어간다.
    private MapEventsOverlay markerEvent() {
        return new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {   //화면 한번 터치시

                if (currentMarker != null) {
                    if( animateHandler == null) {
                        currentMarker.setPosition(p);

                    }
                } else {
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
        animateHandler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 2000;

        animateHandler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(GeoPointInterpolator.interpolate(v, startPosition, finalPosition)); //보간법 이용, 시작 위치에서 끝 위치까지 가는 구 모양의 경로 도출
                map.invalidate();
                // Repeat till progress is complete.
                if (t < 1) {
                    // 16ms 후 다시 시작
                    animateHandler.postDelayed(this, 16);
                } else {   //정답 마커 위치로 이동되면 정답 마커 추가
                    marker.remove(myMapView);
                    answerMarker.showInfoWindow();
                    myMapView.getOverlays().add(answerMarker);


                    gotonextTextView.setVisibility(View.VISIBLE);
                    calcScore();
                    addPolyline(currentMarker.getPosition(), answerMarker.getPosition());    //마커 사이를 직선으로 연결
                    animateHandler = null;
                    myMapView.setClickable(true);
                    map.invalidate();
                }
            }
        });
    }

    //사용자가 정한 마커와 정답 마커 사이를 잇는 직선 생성
    private void addPolyline(GeoPoint startPosition, GeoPoint destPosition) {
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(startPosition);
        geoPoints.add(destPosition);
        polyline = new Polyline();
        polyline.setPoints(geoPoints);
        polyline.setColor(Color.GRAY);

        myMapView.getOverlays().add(polyline);
    }


    //두 좌표 사이의 거리를 구해서 리턴
    private int calcDistance(GeoPoint geoPoint1, GeoPoint geoPoint2) {
        //http://www.mapanet.eu/en/resources/Script-Distance.htm
        //http://www.codecodex.com/wiki/Calculate_distance_between_two_points_on_a_globe
        //https://stackoverflow.com/questions/5936912/how-to-find-the-distance-between-two-geopoints
        final double PI = 3.14159265358979323846;
        double rad = PI / 180;
        double latitude1 = geoPoint1.getLatitude() * rad;
        double longitude1 = geoPoint1.getLongitude() * rad;
        double latitude2 = geoPoint2.getLatitude() * rad;
        double longitude2 = geoPoint2.getLongitude() * rad;

        double radius = 6378.137;   //earch radius

        double dlon = longitude2 - longitude1;
        double distance = Math.acos(Math.sin(latitude1) * Math.sin(latitude2) + Math.cos(latitude1) * Math.cos(latitude2) * Math.cos(dlon)) * radius;

        return (int) distance;
    }


    //문제 정답을 확인한 후 화면의 아무 부분이나 클릭 시 실행
    //기존 화면 오버레이들을 지우고 정답 마커 다시 설정, 타이머 재시작
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) { //문제를 맞춘 후 다시 맵 로드
        gotonextTextView.setVisibility(View.GONE);
        if (currentState == Answered && animateHandler == null) {

            currentMarker.remove(myMapView);
            currentMarker = null;

            answerMarker.closeInfoWindow();
            myMapView.getOverlays().remove(answerMarker);
            myMapView.getOverlays().remove(polyline);

            myMapView.invalidate();

            problem++;
            switch (problem) {
                case 2:
                    setAnswerMarker(questionPic.get(problem));
                    break;
                case 3:
                    setAnswerMarker(questionPic.get(problem));
                    break;
                case 4:
                    stage++;
                    stageTextView.setText("STAGE " + stage);
                    score = 0;
                    scoreTextView.setText("SCORE " + score);
                    setAnswerMarker(questionPic.get(problem));
                    break;
                case 5:
                    setAnswerMarker(questionPic.get(problem));
                    break;
                case 6:
                    showDialogAfterGame();
            }

            currentState = Solving;
            stopTimer = Running;

            timeThreadHandler();

            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    //사용자 마커 생성
    private Marker addUserMarker(final GeoPoint geoPoint) {
        //마커 생성 및 설정
        Marker marker = new Marker(myMapView);
        marker.setIcon(BLUE_FLAG_DRAWABLE);
        marker.setPosition(geoPoint);
        marker.setAnchor(0.25f, 1.0f);
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {  //생성된 마커를 클릭하여 화면에 등록
                currentState = Answered;
                stopTimer = Stop;

                //유저가 선택한 위치의 마커에서 정답 마커까지 이동하는 애니메이션 동작을 하는 마커 생성
                Marker tmpMarker = new Marker(myMapView);
                tmpMarker.setIcon(BLUE_FLAG_DRAWABLE);
                tmpMarker.setPosition(marker.getPosition());
                tmpMarker.setAnchor(0.25f, 1.0f);
                myMapView.getOverlays().add(tmpMarker);

                int distance = calcDistance(geoPoint, answerMarker.getPosition());
                answerMarker.setSnippet(distance + "Km");    //거리를 마커의 Infowindow에 추가
                animateMarker(myMapView, tmpMarker, answerMarker.getPosition(), new GeoPointInterpolator.Spherical()); //마커 이동 애니메이션

                myMapView.invalidate();

                return true;
            }
        });
        return marker;
    }

    //게임이 끝난 후 다이얼로그 표시
    void showDialogAfterGame(){
        final List<String> listItems = new ArrayList<>();
        listItems.add("다시하기");
        listItems.add("종료");
        final CharSequence[] items = listItems.toArray(new String[listItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle("Menu");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String selectedText = items[i].toString();
                if (selectedText.equals("종료")) {
                    finish();
                }
                else if (selectedText.equals("다시하기")){
                    finish();
                    startActivity(new Intent(GameActivity.this, GameActivity.class));
                }
            }
        });
        builder.show();
    }

    //화면을 한번 클릭해 임시 마커 생성 후 타임아웃 발생시 정답 확인 과정
    private void timeOutAddUserMarker() {
        currentState = Answered;
        stopTimer = Stop;

        Marker tmpMarker = new Marker(myMapView);
        tmpMarker.setIcon(BLUE_FLAG_DRAWABLE);
        tmpMarker.setPosition(currentMarker.getPosition());
        tmpMarker.setAnchor(0.25f, 1.0f);
        myMapView.getOverlays().add(tmpMarker);

        int distance = calcDistance(currentMarker.getPosition(), answerMarker.getPosition());
        answerMarker.setSnippet(distance + "Km");
        animateMarker(myMapView, tmpMarker, answerMarker.getPosition(), new GeoPointInterpolator.Spherical());
        gotonextTextView.setVisibility(View.VISIBLE);
        calcScore();
    }

    //점수 계산
    private void calcScore() {
        final int CRITERIA = 500;

        if (distanceScore >= 5000) {
            distanceScore = 0;
        } else {
            distanceScore = CRITERIA - distanceScore / 10;
        }

        score += distanceScore + timeScore / 1000;
        scoreTextView.setText("SCORE " + score);
    }

    //정답 마커 생성
    private void setAnswerMarker(PictureInfo pi) {
        answerMarker = new Marker(myMapView);
        answerMarker.setIcon(RED_FLAG_DRAWABLE);
        answerMarker.setAnchor(0.25f, 1.0f);
        answerMarker.setTitle(pi.name);
        answerMarker.setPosition(pi.geoPoint);

        questionImageView.setImageResource(pi.id);

    }

    //메뉴 버튼 클릭 시 다이얼로그 표시
    class MenuButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final List<String> listItems = new ArrayList<>();
            listItems.add("설정");
            listItems.add("종료");
            final CharSequence[] items = listItems.toArray(new String[listItems.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setTitle("Menu");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String selectedText = items[i].toString();
                    if (selectedText.equals("종료")) {
                        finish();
                    }
                }
            });
            builder.show();
        }
    }

    //문제 사진 클릭 시 크게 띄워주는 이벤트 등록
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