package kr.ac.kw.coms.globealbum.game;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kotlin.Pair;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.PictureDialogFragment;
import kr.ac.kw.coms.globealbum.map.DrawCircleOverlay;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.EXIFinfo;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.UIPromise;


import static kr.ac.kw.coms.globealbum.game.GameActivity.TimerState.Running;
import static kr.ac.kw.coms.globealbum.game.GameActivity.TimerState.Stop;


public class GameActivity extends AppCompatActivity {
    public static Activity GActivity;
    Context context = null;
    MyMapView myMapView = null;

    ImageView questionImageView = null;
    ProgressBar progressBar = null;
    TextView stageTextView = null;
    Button menuButton = null;
    TextView scoreTextView = null;


    Button goToNextStageButton,exitGameButton;
    TextView landNameAnswerTextView,landDistanceAnswerTextView,landScoreTextView;
    ImageView pictureAnswerImageView;

    LinearLayout answerLinearLayout;


    Drawable RED_FLAG_DRAWABLE;
    Drawable BLUE_FLAG_DRAWABLE;
    final int PICTURE_NUM = 4;
    int problem = 0;
    int score = 0;
    int timeScore = 0;
    int distanceScore = 0;
    int stage = 1;
    int distance =0;

    /**
     * 제한시간 타이머가 돌아가는 중인지.
     */
    TimerState stopTimer = Running;
    private Handler animateHandler = null;


    private Handler ui;


    final int TIME_LIMIT_MS = 14000;
    MapEventsOverlay listenerOverlay;

    Marker currentMarker;   //사용자가 찍은 마커
    Marker answerMarker;    //정답 마커
    Polyline polyline;  //마커 사이를 이어주는 직선

    DrawCircleOverlay drawCircleOverlay;

    ArrayList<GamePictureInfo> questionPic = new ArrayList<>();


    enum TimerState {
        Stop,
        Running
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GActivity = this;

        //로팅화면 적용
        setContentView(R.layout.layout_game_loading_animation);
        ImageView loadigImageView = findViewById(R.id.gif_loading);
        loadigImageView.setClickable(false);
        DrawableImageViewTarget gifImage = new DrawableImageViewTarget(loadigImageView);
        Glide.with(GameActivity.this).load(R.drawable.owl).into(gifImage);
        ui = new Handler();

        //게임 시작 전 문제 세팅
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                setQuestion();
            }
        });
    }



    //문제 세팅
    private void setQuestion() {
        int[] id = new int[PICTURE_NUM];
        EXIFinfo exifInfo = new EXIFinfo();
        RemoteJava client = new RemoteJava();
        for (int i = 0; i < PICTURE_NUM; i++) { //사진 리소스 id 배열에 저장
            id[i] = R.drawable.coord0 + i;
        }
        for (int i = 0; i < 1000; i++) {    //반복하여 리소스 id 섞음
            int random = (int) (Math.random() * PICTURE_NUM);
            int tmp = id[0];
            id[0] = id[random];
            id[random] = tmp;
        }

        for (int i = 0; i < PICTURE_NUM; i++) {
            //GPS 정보 뽑아오기
            exifInfo.setMetadata(getResources().openRawResource(id[i]));
            final GeoPoint geoPoint = exifInfo.getLocationGeopoint();
            final int s = id[i];
            //역지오코딩을 통해 지역 정보 뽑아오기
            client.reverseGeocode(geoPoint.getLatitude(), geoPoint.getLongitude(), new UIPromise<Pair<String, String>>() {
                @Override
                public void failure(@NotNull Throwable cause) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    cause.printStackTrace(pw);
                    Log.e("failfail", cause.toString() + sw.toString());
                }

                @Override
                public void success(Pair<String, String> result) {
                    String name = result.getFirst() + " " + result.getSecond();
                    GamePictureInfo pictureInfo = new GamePictureInfo();
                    pictureInfo.geoPoint = geoPoint;
                    pictureInfo.id = s;
                    pictureInfo.name = name;
                    questionPic.add(pictureInfo);

                    if (questionPic.size() == 1) {  //문제가 하나 완성 시 초기 구성 진행
                        displayQuiz();
                    }
                }
            });
        }
    }

    private void displayQuiz() {
        setContentView(R.layout.activity_game);
        progressBar = findViewById(R.id.progressbar);
        stageTextView = findViewById(R.id.textview_stage);
        scoreTextView = findViewById(R.id.textview_score);
        menuButton = findViewById(R.id.game_button_menu);
        menuButton.setOnClickListener(new MenuButtonClickListener());

        //정답 확인 부분 뷰 연결
        pictureAnswerImageView = findViewById(R.id.picture_answer);
        exitGameButton = findViewById(R.id.button_exit);
        goToNextStageButton = findViewById(R.id.button_next);
        landDistanceAnswerTextView = findViewById(R.id.textview_land_distance_answer);
        landNameAnswerTextView = findViewById(R.id.textview_land_name_answer);
        landScoreTextView = findViewById(R.id.textview_land_score_answer);
        answerLinearLayout = findViewById(R.id.layout_answer);

        goToNextStageButton.setOnClickListener(new GameNextQuizListener());
        exitGameButton.setOnClickListener(new GameFinishListener());


        RED_FLAG_DRAWABLE = getResources().getDrawable(R.drawable.red_flag);
        BLUE_FLAG_DRAWABLE = getResources().getDrawable(R.drawable.blue_flag);

        stageTextView.setText("STAGE " + stage);

        //퀴즈에 나올 사진들을 연결
        questionImageView = findViewById(R.id.picture);
        questionImageView.setOnClickListener(new PictureClickListener());

        //osmdroid 초기 구성
        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        myMapView = findViewById(R.id.map);

        //마커 이벤트 등록
        listenerOverlay = markerEvent();
        myMapView.getOverlays().add(listenerOverlay);

        timeThreadhandler();
        setAnswerMarker(questionPic.get(problem));  //정답 마커 설정
    }

    //제한 시간 측정
    private void timeThreadhandler() {
        int stageTimeLimitMs = TIME_LIMIT_MS - problem * 1000;
        progressBar.setMax(stageTimeLimitMs);

        final long deadlineMs = new Date().getTime() + stageTimeLimitMs;
        //final Handler ui = new Handler();
        if (ui != null) {
            ui.post(new Runnable() {
                @Override
                public void run() {
                    long timeLeft = deadlineMs - new Date().getTime();
                    progressBar.setProgress((int) timeLeft);

                    timeScore = (int) timeLeft;

                    if (timeLeft > 0 && stopTimer == Running) {
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
                        stopTimer = Stop;

                        myMapView.getOverlays().add(answerMarker);

                        setAnswerLayout();
                    }
                    myMapView.invalidate();
                }
            });
        }

    }

    //맵뷰를 클릭하였을 때 발생하는 이벤트
    //마커를 화면에 띄우고, 또 한번 클릭할 경우 정답 확인으로 넘어간다.
    private MapEventsOverlay markerEvent() {
        return new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {   //화면 한번 터치시

                if (currentMarker != null) {
                    if (animateHandler == null) {
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
        final float durationInMs = 1000;
        map.getController().zoomTo(myMapView.getLogZoom(), 1700L);          //인자의 속도에 맞춰서 줌 아웃

        drawCircleOverlay = new DrawCircleOverlay(marker.getPosition(), finalPosition, map);
        myMapView.getOverlays().add(drawCircleOverlay);

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

                //map.getController().zoomOut(2000L);          //인자의 속도에 맞춰서 줌 아웃
                map.invalidate();
                // Repeat till progress is complete.
                if (t < 1) {
                    // 16ms 후 다시 시작
                    animateHandler.postDelayed(this, 1000 / 60);
                } else {   //정답 마커 위치로 이동되면 정답 마커 추가
                    marker.remove(myMapView);
                    myMapView.getOverlays().add(answerMarker);
                    addPolyline(currentMarker.getPosition(), answerMarker.getPosition());    //마커 사이를 직선으로 연결

                    calcScore();
                    setAnswerLayout();


                    animateHandler = null;

                    //myMapView.setClickable(true);
                    map.invalidate();
                }
            }
        });
    }



    //정답 확인 레이아웃 값 설정하고 띄우기
    private void setAnswerLayout() {

        questionImageView.setVisibility(View.GONE);
        questionImageView.setClickable(false);

        answerLinearLayout.setVisibility(View.VISIBLE);
        answerLinearLayout.setClickable(true);
        landNameAnswerTextView.setText(questionPic.get(problem).name);
        landDistanceAnswerTextView.setText(distance+"KM");
        landScoreTextView.setText(score+"");
        Glide.with(context).load(questionPic.get(problem).id).into(pictureAnswerImageView);

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

    //사용자 마커 생성
    private Marker addUserMarker(final GeoPoint geoPoint) {
        //마커 생성 및 설정
        Marker marker = new Marker(myMapView);
        //marker.setIcon(BLUE_FLAG_DRAWABLE);
        marker.setPosition(geoPoint);
        marker.setAnchor(0.25f, 1.0f);
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {  //생성된 마커를 클릭하여 화면에 등록
                stopTimer = Stop;
                //유저가 선택한 위치의 마커에서 정답 마커까지 이동하는 애니메이션 동작을 하는 마커 생성
                Marker tmpMarker = new Marker(myMapView);
                //tmpMarker.setIcon(BLUE_FLAG_DRAWABLE);
                tmpMarker.setPosition(marker.getPosition());
                tmpMarker.setAnchor(0.25f, 1.0f);
                myMapView.getOverlays().add(tmpMarker);

                distance = calcDistance(geoPoint, answerMarker.getPosition());
                animateMarker(myMapView, tmpMarker, answerMarker.getPosition(), new GeoPointInterpolator.Spherical()); //마커 이동 애니메이션

                marker.remove(myMapView);
                myMapView.invalidate();

                return true;
            }
        });
        return marker;
    }

    //게임이 끝난 후 다이얼로그 표시
    void showDialogAfterGame() {
        final List<String> listItems = new ArrayList<>();
        listItems.add("다시하기");
        listItems.add("종료");
        final CharSequence[] items = listItems.toArray(new String[listItems.size()]);

        stopTimer = Stop;
        ui = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle("Menu");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String selectedText = items[i].toString();
                if (selectedText.equals("종료")) {
                    finish();
                } else if (selectedText.equals("다시하기")) {
                    finish();
                    startActivity(new Intent(GameActivity.this, GameActivity.class));
                }
            }
        });
        builder.show();
    }

    //화면을 한번 클릭해 임시 마커 생성 후 타임아웃 발생시 정답 확인 과정
    private void timeOutAddUserMarker() {
        stopTimer = Stop;

        Marker tmpMarker = new Marker(myMapView);
        //tmpMarker.setIcon(BLUE_FLAG_DRAWABLE);
        tmpMarker.setPosition(currentMarker.getPosition());
        tmpMarker.setAnchor(0.25f, 1.0f);
        myMapView.getOverlays().add(tmpMarker);

        distance = calcDistance(currentMarker.getPosition(), answerMarker.getPosition());
        animateMarker(myMapView, tmpMarker, answerMarker.getPosition(), new GeoPointInterpolator.Spherical());

        currentMarker.remove(myMapView);
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
    private void setAnswerMarker(GamePictureInfo pi) {
        answerMarker = new Marker(myMapView);
        answerMarker.setIcon(RED_FLAG_DRAWABLE);
        answerMarker.setAnchor(0.25f, 1.0f);
        answerMarker.setPosition(pi.geoPoint);

        myMapView.getController().setZoom(myMapView.getLogZoom());

        Glide.with(context).load(pi.id).into(questionImageView);
        questionImageView.invalidate();
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



    RecyclerView recyclerView;
    AfterGameAdapter adapter;


    //한 스테이지가 끝난 후 다음 단계로 넘어갈 수 있는 이벤트
    class GameNextQuizListener implements  View.OnClickListener{
        @Override
        public void onClick(View v) {

            currentMarker.remove(myMapView);
            currentMarker = null;

            myMapView.getOverlays().remove(answerMarker);
            myMapView.getOverlays().remove(polyline);
            myMapView.getOverlays().remove(drawCircleOverlay);

            myMapView.invalidate();


            questionImageView.setVisibility(View.VISIBLE);
            questionImageView.setClickable(true);
            answerLinearLayout.setVisibility(View.GONE);
            answerLinearLayout.setClickable(false);



            problem++;
            switch (problem) {
                case 1:
                    setRecyclerView();

                    //setAnswerMarker(questionPic.get(problem));
                    break;
                case 2:
                    stage++;
                    stageTextView.setText("STAGE " + stage);
                    score = 0;
                    scoreTextView.setText("SCORE " + score);
                    setAnswerMarker(questionPic.get(problem));
                    break;
                case 3:
                    setAnswerMarker(questionPic.get(problem));
                    break;
                case 4:
                    showDialogAfterGame();
                    break;
            }
            stopTimer = Running;
            timeThreadhandler();

        }
    }

    private void setRecyclerView() {    //게임이 완료된 후 사진들을 모아서 보여주는 리사이클뷰 적용
        setContentView(R.layout.layout_recycler_view);
        recyclerView = findViewById(R.id.after_game_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AfterGameAdapter(this, questionPic);
        recyclerView.setAdapter(adapter);
    }


    //한 스테이지가 끝난 후 게임을 종료할 수 있는 이벤트
    class GameFinishListener implements  View.OnClickListener{
        @Override
        public void onClick(View v) {
            finish();
        }

    }


    //문제 사진 클릭 시 크게 띄워주는 이벤트 등록
    public class PictureClickListener implements View.OnClickListener {
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