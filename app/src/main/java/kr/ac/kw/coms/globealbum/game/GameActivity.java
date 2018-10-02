package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.target.DrawableImageViewTarget;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.common.PictureDialogFragment;
import kr.ac.kw.coms.globealbum.map.DrawCircleOverlay;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;
import kr.ac.kw.coms.globealbum.provider.UIPromise;
import kr.ac.kw.coms.landmarks.client.ReverseGeocodeResult;

import static kr.ac.kw.coms.globealbum.game.GameActivity.TimerState.Running;
import static kr.ac.kw.coms.globealbum.game.GameActivity.TimerState.Stop;


public class GameActivity extends AppCompatActivity {
    Context context = null;
    MyMapView myMapView = null;

    ProgressBar progressBar = null;
    TextView stageTextView = null;
    TextView scoreTextView = null;
    TextView targetTextView = null;


    Button goToNextStageButton, exitGameButton;
    TextView landNameAnswerTextView, landDistanceAnswerTextView, landScoreTextView;
    GameType gameType = null;
    ConstraintLayout questionTypeALayout;
    ConstraintLayout questionTypeBLayout;
    ConstraintLayout answerLayout;
    ImageView questionTypeAImageView = null;
    ImageView[] questionTypeBImageView = new ImageView[4];
    ImageView pictureAnswerImageView;

    Drawable RED_FLAG_DRAWABLE;
    Drawable BLUE_FLAG_DRAWABLE;
    final int PICTURE_NUM = 4;
    int problem = 0;
    int score = 0;
    int timeScore = 0;
    int distance = 0;
    boolean rightAnswerTypeB = false;

    TimerState stopTimer = Running;

    private Handler animateHandler = null;  //마커 이동시키는 핸들러
    private Handler drawDottedLineHandler = null;  // 점선 그리는 핸들러
    private Handler ui;


    final int TIME_LIMIT_MS = 14000;
    MapEventsOverlay markerClickListenerOverlay;

    Marker currentMarker;   //사용자가 찍은 마커
    Marker answerMarker;    //정답 마커
    Polyline polyline;  //마커 사이를 이어주는 직선

    DrawCircleOverlay drawCircleOverlay;
    DottedLineOverlay dottedLineOverlay;

    ArrayList<IPicture> questionPic = new ArrayList<>();

    int answerImageviewIndex;


    enum TimerState {
        Stop,
        Running
    }

    enum GameType {
        A,
        B
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        displayNextRoundOrFinishView();
        //displayLoadingGif();
        redRect = getResources().getDrawable(R.drawable.rectangle_border, null);

    }

    int stage = 0;
    Button gameStartButton,gameExitButton;
    TextView gameNextRoundLevelTextview,gameNextRoundGoalTextview;

    int[] limitScore = new int[]{600,800}; //600,800,1100,1400,1650,2000,2700
    int[] numberOfGames = new int[]{3,3};

    private void displayNextRoundOrFinishView(){
        stage++;
        if( stage == 1 || (stage  - 1 != limitScore.length && score >= limitScore[stage-1])){
            setContentView(R.layout.layout_game_next_round);
            gameStartButton = findViewById(R.id.button_start);
            gameExitButton = findViewById(R.id.button_exit);
            gameNextRoundLevelTextview = findViewById(R.id.textview_level);
            gameNextRoundGoalTextview = findViewById(R.id.textview_goal_score);
            gameNextRoundLevelTextview.setText("Level " + stage);
            gameNextRoundGoalTextview.setText(limitScore[stage-1]+"/"+numberOfGames[stage-1]);
            gameStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    score=0;
                    problem=0;
                    setQuestion();
                }
            });
            gameExitButton.setOnClickListener(new GameFinishListener());
        }
        else {
            setRecyclerView();
        }
    }

    private void displayLoadingGif() {
        setContentView(R.layout.layout_game_loading_animation);
        ImageView loadigImageView = findViewById(R.id.gif_loading);
        loadigImageView.setClickable(false);
        DrawableImageViewTarget gifImage = new DrawableImageViewTarget(loadigImageView);
        GlideApp.with(this).load(R.drawable.owl).into(gifImage);
    }


    /**
     * 문제 세팅
     */
    private void setQuestion() {
        int[] id = new int[PICTURE_NUM];
        for (int i = 0; i < PICTURE_NUM; i++) { //사진 리소스 id 배열에 저장
            id[i] = R.drawable.coord0 + i;
        }
        for (int i = 0; i < 1000; i++) {    //반복하여 리소스 id 섞음
            int random = (int) (Math.random() * PICTURE_NUM);
            int tmp = id[0];
            id[0] = id[random];
            id[random] = tmp;
        }

        //GPS 정보 뽑아오기
        Resources resources = getResources();
        for (int i = 0; i < PICTURE_NUM; i++) {
            ResourcePicture pic = new ResourcePicture(id[i], resources);
            questionPic.add(pic);
//            setReverseGeocodeRegionNameAsPictureTitle(pic);
        }
        displayQuiz();
    }

    private void setReverseGeocodeRegionNameAsPictureTitle(final IPicture target) {
        RemoteJava client = RemoteJava.INSTANCE;
        GeoPoint geo = Objects.requireNonNull(target.getGeo());
        client.reverseGeocode(geo.getLatitude(), geo.getLongitude(), new UIPromise<ReverseGeocodeResult>() {
            @Override
            public void failure(@NotNull Throwable cause) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                cause.printStackTrace(pw);
                Log.e("failfail", cause.toString() + sw.toString());
            }

            @Override
            public void success(ReverseGeocodeResult result) {
                String name = result.getCountry() + " " + result.getDetail();
                target.setTitle(name);

                // 첫 문제 지역명 수신 시 문제 진행
                if (questionPic.indexOf(target) == 0) {
                    displayQuiz();
                }
            }
        });
    }


    /**
     * 전체적인 게임 틀을 구성
     */
    private void displayQuiz() {
        setContentView(R.layout.activity_game);

        progressBar = findViewById(R.id.progressbar);
        stageTextView = findViewById(R.id.textview_stage);
        targetTextView = findViewById(R.id.textview_target);
        scoreTextView = findViewById(R.id.textview_score);
        questionTypeALayout = findViewById(R.id.cl_point_problem);

        //정답 확인 부분 뷰 연결
        pictureAnswerImageView = findViewById(R.id.picture_answer);

        exitGameButton = findViewById(R.id.button_exit);
        goToNextStageButton = findViewById(R.id.button_next);
        landDistanceAnswerTextView = findViewById(R.id.textview_land_distance_answer);
        landNameAnswerTextView = findViewById(R.id.textview_land_name_answer);
        landScoreTextView = findViewById(R.id.textview_land_score_answer);
        answerLayout = findViewById(R.id.layout_answer);

        goToNextStageButton.setOnClickListener(new GameNextQuizListener());
        exitGameButton.setOnClickListener(new GameFinishListener());


        RED_FLAG_DRAWABLE = getResources().getDrawable(R.drawable.red_flag);
        BLUE_FLAG_DRAWABLE = getResources().getDrawable(R.drawable.blue_flag);

        stageTextView.setText("STAGE " + stage);
        targetTextView.setText("TARGET "+(problem+1)+"/"+(numberOfGames[stage-1]));

        //퀴즈에 나올 사진들을 연결
        questionTypeAImageView = findViewById(R.id.picture);
        questionTypeAImageView.setOnClickListener(new PictureClickListenerTypeA());      //이미지뷰 클릭 시 화면 확대해서 보여줌
        questionTypeBLayout = findViewById(R.id.pictures);

        int[] imgViewIds = new int[]{R.id.picture1, R.id.picture2, R.id.picture3, R.id.picture4};
        for (int i = 0; i < imgViewIds.length; i++) {
            questionTypeBImageView[i] = findViewById(imgViewIds[i]);
            questionTypeBImageView[i].setOnClickListener(new PictureClickListenerTypeB1());
        }


        //osmdroid 초기 구성
        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        myMapView = findViewById(R.id.map);

        //마커 이벤트 등록
        markerClickListenerOverlay = markerEvent();
        myMapView.getOverlays().add(markerClickListenerOverlay);
        stopTimer = Running;
        ui = new Handler();
        timeThreadhandler();

        chooseQuestionType();
    }

    /**
     * 제한 시간 측정
     */
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
                    stopTimer = Stop;


                    // 화면을 한번 터치해 마커를 생성하고 난 후
                    // 타임아웃 발생시 그 마커를 위치로 정답 확인
                    if (gameType == GameType.A) {
                        if (currentMarker != null) {
                            timeOutAddUserMarker();
                        } else {
                            //화면에 마커 생성 없이 타임아웃 발생시 정답 확인
                            //currentMarker = new Marker(myMapView);

                            myMapView.getOverlays().add(answerMarker);

                        }
                    }   //지명 문제에서 한번 테두리가 있는 후 정답 확인과 테두리 없을 시 정답확인 구현하기
                    else if (gameType == GameType.B) {
                        clearLastSelectIfExists();
                    }

                    setAnswerLayout();

                    myMapView.invalidate();
                }
            });
        }

    }

    /**
     * 맵뷰를 클릭하였을 때 발생하는 이벤트
     * 마커를 화면에 띄우고, 또 한번 클릭할 경우 정답 확인으로 넘어간다.
     *
     * @return 마커를 클릭했을 시의 이벤트
     */
    private MapEventsOverlay markerEvent() {
        return new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {   //화면 한번 터치시
                if (gameType == GameType.A && ui != null && animateHandler == null)
                    showMarker(myMapView, p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {    //길게 터치시
                return false;
            }
        });
    }

    private Point interpolate(float fraction, Point a, Point b) {
        double lat = (b.x - a.x) * fraction + a.x;
        double lng = (b.y - a.y) * fraction + a.y;
        return new Point((int) lat, (int) lng);
    }

    /**
     * 화면을 클릭해 마커를 생성했을 때 애니메이션을 주어서 좌표에 생성
     *
     * @param map              mapview
     * @param finalGeoPosition 화면에 선택한 곳의 좌표
     */
    private void showMarker(final MapView map, final GeoPoint finalGeoPosition) {

        final Projection projection = map.getProjection();
        final Point startPoint = new Point();
        projection.toPixels(finalGeoPosition, startPoint);
        final Point finalPoint = new Point();

        finalPoint.x = startPoint.x;
        finalPoint.y = startPoint.y;

        startPoint.x += 50;
        startPoint.y -= 50;

        GeoPoint startGeoPosition = (GeoPoint) projection.fromPixels(startPoint.x, startPoint.y);

        final Marker tmpMarker = new Marker(map);
        tmpMarker.setPosition(startGeoPosition);
        tmpMarker.setIcon(BLUE_FLAG_DRAWABLE);
        //tmpMarker.info
        map.getOverlays().add(tmpMarker);

        final Handler showMarkerHandler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new LinearInterpolator();
        final float durationInMs = 175;

        showMarkerHandler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                tmpMarker.setAnchor(0.25f, 1.0f);
                Point pixelPoint = interpolate(v, startPoint, finalPoint);
                GeoPoint geoPoint = (GeoPoint) projection.fromPixels(pixelPoint.x, pixelPoint.y);

                tmpMarker.setPosition(geoPoint); //보간법 이용, 시작 위치에서 끝 위치까지 가는 경로 도출

                if (currentMarker != null) {
                    currentMarker.remove(map);
                }

                map.invalidate();
                // Repeat till progress is complete.
                if (t < 1) {
                    // 16ms 후 다시 시작
                    showMarkerHandler.postDelayed(this, 1000 / 60);
                } else {   //정답 마커 위치로 이동되면 정답 마커 추가
                    tmpMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {  //생성된 마커를 클릭하여 화면에 등록
                            stopTimer = Stop;
                            //유저가 선택한 위치의 마커에서 정답 마커까지 이동하는 애니메이션 동작을 하는 마커 생성
                            Marker tmpMarker = new Marker(myMapView);
                            //tmpMarker.setIcon(BLUE_FLAG_DRAWABLE);
                            tmpMarker.setPosition(marker.getPosition());
                            tmpMarker.setAnchor(0.25f, 1.0f);
                            myMapView.getOverlays().add(tmpMarker);

                            distance = calcDistance(finalGeoPosition, answerMarker.getPosition());
                            animateMarker(myMapView, tmpMarker, answerMarker.getPosition(), new GeoPointInterpolator.Linear()); //마커 이동 애니메이션

                            marker.remove(myMapView);
                            myMapView.invalidate();

                            return true;
                        }
                    });
                    currentMarker = tmpMarker;
                }
            }
        });
    }


    /**
     * 사용자가 찍은 마커가 위치에서 시작하여 정답마커까지 이동하는 애니메이션
     *
     * @param map                  mapview
     * @param marker               사용자가자 찍은 마커
     * @param finalPosition        정답 마커의 좌표
     * @param GeoPointInterpolator 마커 이동 방식
     */
    private void animateMarker(final MapView map, final Marker marker, final GeoPoint finalPosition, final GeoPointInterpolator GeoPointInterpolator) {
        final GeoPoint startPosition = marker.getPosition();
        animateHandler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 1000;

        map.getController().animateTo(finalPosition, myMapView.getMinZoomLevel(), 1000L);

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

                    setAnswerLayout();


                    animateHandler = null;

                    map.invalidate();
                }
            }
        });
    }

    /**
     * 정답 확인 레이아웃 값 설정하고 띄우기
     */
    private void setAnswerLayout() {
        ui = null;
        if (gameType == GameType.A) {
            questionTypeALayout.setVisibility(View.GONE);
        } else if (gameType == GameType.B) {
            questionTypeBLayout.setVisibility(View.GONE);
            myMapView.getController().zoomTo(myMapView.getMinZoomLevel(), 1000L); //인자의 속도에 맞춰서 줌 아웃
        }

        int curScore = calcScore();

        answerLayout.setVisibility(View.VISIBLE);
        answerLayout.setClickable(true);
        landNameAnswerTextView.setText(questionPic.get(problem).getTitle());
        if (gameType == GameType.A && currentMarker != null) {
            landDistanceAnswerTextView.setVisibility(View.VISIBLE);
            landDistanceAnswerTextView.setText(distance + "KM");
        } else {
            landDistanceAnswerTextView.setVisibility(View.INVISIBLE);
        }
        landScoreTextView.setText("score " + curScore);
        GlideApp.with(context).load(questionPic.get(problem)).into(pictureAnswerImageView);

    }

    /**
     * 사용자가 정한 마커와 정답 마커 사이를 잇는 직선 생성
     *
     * @param startPosition 사용자 마커의 좌표
     * @param destPosition  정답 마커의 좌표
     */
    //
    private void addPolyline(GeoPoint startPosition, GeoPoint destPosition) {
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(startPosition);
        geoPoints.add(destPosition);
        dottedLineOverlay = new DottedLineOverlay(myMapView, startPosition, destPosition);
        polyline = new Polyline();

        polyline.setPoints(geoPoints);
        polyline.setColor(Color.GRAY);

        myMapView.getOverlays().add(dottedLineOverlay);
        drawDottedLineHandler = new Handler();
        drawDottedLineHandler.post(new Runnable() { //반복 진행하면서 원 그리기
            @Override
            public void run() {
                myMapView.invalidate();
                drawDottedLineHandler.postDelayed(this, 1000 / 60);
            }
        });
    }

    /**
     * 두 좌표 사이의 거리를 구해서 리턴
     *
     * @param geoPoint1 좌표1
     * @param geoPoint2 좌표2
     * @return 좌표 사이의 거리를 구해 정수형 Km로 반환
     */
    private int calcDistance(GeoPoint geoPoint1, GeoPoint geoPoint2) {
        return (int) (geoPoint1.distanceToAsDouble(geoPoint2) / 1000);
    }

    /**
     * 게임이 끝난 후 다이얼로그 표시
     * 미사용
     */
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

    /**
     * 화면을 한번 클릭해 임시 마커 생성 후 타임아웃 발생시 정답 확인
     */
    private void timeOutAddUserMarker() {

        Marker tmpMarker = new Marker(myMapView);
        //tmpMarker.setIcon(BLUE_FLAG_DRAWABLE);
        tmpMarker.setPosition(currentMarker.getPosition());
        tmpMarker.setAnchor(0.25f, 1.0f);
        myMapView.getOverlays().add(tmpMarker);

        distance = calcDistance(currentMarker.getPosition(), answerMarker.getPosition());
        animateMarker(myMapView, tmpMarker, answerMarker.getPosition(), new GeoPointInterpolator.Spherical());

        currentMarker.remove(myMapView);
    }


    /**
     * 점수를 계산
     *
     * @return 점수를 계산하여 리턴
     */
    private int calcScore() {
        int curScore = 0;
        if (gameType == GameType.A) {
            if (currentMarker == null) { //마커를 화면에 찍지 않고 정답을 확인하는 경우
                curScore = -100;
            } else {
                int criteria = 300;
                curScore = criteria - distance / 100;
                curScore += timeScore / 100;
            }
        } else if (gameType == GameType.B) {
            if (rightAnswerTypeB == false) {
                curScore = -100;
            } else {
                curScore = 300;
                curScore += timeScore / 100;
            }
        }
        score += curScore;

        scoreTextView.setText("SCORE " + score);

        return curScore;
    }

    /**
     * 사진을 보여주고 지명을 찾는 문제 형식
     *
     * @param pi 사진 정보를 가지고 있는 클래스
     */
    private void setPictureQuestion(IPicture pi) {
        gameType = GameType.A;
        //레이아웃 설정

        questionTypeAImageView.setClickable(true);
        questionTypeAImageView.setVisibility(View.VISIBLE);
        questionTypeBLayout.setClickable(false);
        questionTypeBLayout.setVisibility(View.GONE);

        answerMarker = new Marker(myMapView);
        answerMarker.setIcon(RED_FLAG_DRAWABLE);
        answerMarker.setAnchor(0.25f, 1.0f);
        answerMarker.setPosition(Objects.requireNonNull(pi.getGeo()));

        answerMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                return false;
            }
        });

        myMapView.getController().setZoom(myMapView.getMinZoomLevel());

        GlideApp.with(this).load(pi).into(questionTypeAImageView);
        questionTypeALayout.setVisibility(View.VISIBLE);
    }


    /**
     * 지명을 보여주고 사진을 찾는 문제 형식
     *
     * @param pi 사진 정보를 가지고 있는 클래스
     */
    private void setPlaceNameQuestion(IPicture pi) {
        gameType = GameType.B;
        //레이아웃 설정
        questionTypeBLayout.setVisibility(View.VISIBLE);
        questionTypeBLayout.setClickable(true);
        questionTypeAImageView.setClickable(false);
        questionTypeAImageView.setVisibility(View.GONE);

        //마커에 지명 설정하고 맵뷰에 표시
        answerMarker = new Marker(myMapView);
        answerMarker.setIcon(RED_FLAG_DRAWABLE);
        answerMarker.setAnchor(0.25f, 1.0f);
        answerMarker.setPosition(Objects.requireNonNull(pi.getGeo()));
        answerMarker.setTitle(pi.getTitle());
        MarkerInfoWindow markerInfoWindow = new MarkerInfoWindow(R.layout.bonuspack_bubble, myMapView);
        View v = markerInfoWindow.getView();
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        answerMarker.setInfoWindow(markerInfoWindow);


        answerMarker.showInfoWindow();
        myMapView.getOverlays().add(answerMarker);
        myMapView.setClickable(false);


        answerImageviewIndex = problem;

        for (int i = 0; i < 4; i++) {
            questionTypeBImageView[i].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            GlideApp.with(context).load(questionPic.get(i)).into(questionTypeBImageView[i]);
        }
    }

    /**
     * 한 스테이지가 끝난 후 다음 단계로 넘어가는 리스너
     */
    class GameNextQuizListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

          if (gameType == GameType.A) {
                if (currentMarker != null) {
                    currentMarker.remove(myMapView);
                    currentMarker = null;
                }
                if (drawDottedLineHandler != null) {
                    drawDottedLineHandler.removeMessages(0);
                    drawDottedLineHandler = null;
                }
                myMapView.getOverlays().remove(answerMarker);
                myMapView.getOverlays().remove(polyline);
                myMapView.getOverlays().remove(drawCircleOverlay);
                myMapView.getOverlays().remove(dottedLineOverlay);

            } else if (gameType == GameType.B) {
                InfoWindow.closeAllInfoWindowsOn(myMapView);
                myMapView.getOverlays().remove(answerMarker);

            }

            answerLayout.setVisibility(View.GONE);
            answerLayout.setClickable(false);

            problem++;
            targetTextView.setText("TARGET "+(problem+1)+"/"+(numberOfGames[stage-1]));

            if(problem < numberOfGames[stage-1]){
                chooseQuestionType();
                stopTimer = Running;
                ui = new Handler();
                timeThreadhandler();
            }
            else{
                displayNextRoundOrFinishView();
            }
        }
    }

    /**
     * 지명 문제, 사진 문제를 골라서 화면에 표시
     */
    private void chooseQuestionType() {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        int randomNumber = random.nextInt(2);
        if(randomNumber == 0){
            setPictureQuestion(questionPic.get(problem));
        }
        else if (randomNumber == 1){
            setPlaceNameQuestion(questionPic.get(problem));
        }
    }


    RecyclerView recyclerView;
    AfterGameAdapter adapter;

    /**
     * 게임이 완료된 후 사진들을 모아서 보여주는 리사이클뷰 적용
     */
    private void setRecyclerView() {
        setContentView(R.layout.layout_recycler_view);
        recyclerView = findViewById(R.id.after_game_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AfterGameAdapter(this, questionPic);
        recyclerView.setAdapter(adapter);
    }


    /**
     * 한 게임이 끝난 후 게임을 종료하는 리스너
     */
    class GameFinishListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }

    }

    /**
     * 문제 사진 클릭 시 다이얼로그로 크게 띄워주는 리스너
     */
    public class PictureClickListenerTypeA implements View.OnClickListener {
        @Override
        public void onClick(View view) {    //이미지뷰를 다이얼로그로 화면에 표시
            ImageView imgv = (ImageView) view;
            PictureDialogFragment pdf = PictureDialogFragment.Companion.newInstance(imgv.getDrawable());
            pdf.show(getSupportFragmentManager(), "wow");
        }
    }

    View lastSelect;
    Drawable redRect;

    /**
     * 예비 선택을 지우고 테두리 없는 상태로 바꿈
     */
    private void clearLastSelectIfExists() {
        if (lastSelect == null) {
            return;
        }
        lastSelect.getOverlay().clear();
        lastSelect.setOnClickListener(new PictureClickListenerTypeB1());
    }

    /**
     * 답안 이미지를 예비 선택하는 리스너
     */
    public class PictureClickListenerTypeB1 implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            clearLastSelectIfExists();
            redRect.setBounds(new Rect(0, 0, view.getWidth(), view.getHeight()));
            view.getOverlay().add(redRect);
            view.setOnClickListener(new PictureClickListenerTypeB2());
            lastSelect = view;
            if (questionTypeBImageView[problem] == view) {
                rightAnswerTypeB = true;
            } else {
                rightAnswerTypeB = false;
            }
        }
    }

    /**
     * 예비 선택한 답안을 확정하는 리스너
     */
    public class PictureClickListenerTypeB2 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (questionTypeBImageView[problem] == v) {
                rightAnswerTypeB = true;
            }
            stopTimer = Stop;
            clearLastSelectIfExists();
            setAnswerLayout();
            lastSelect = null;
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