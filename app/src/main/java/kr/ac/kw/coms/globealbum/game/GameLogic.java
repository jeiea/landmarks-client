package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.map.DrawCircleOverlay;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;
import kr.ac.kw.coms.globealbum.provider.UIPromise;
import kr.ac.kw.coms.landmarks.client.ReverseGeocodeResult;

class GameLogic implements IGameInputHandler {
    private GameUI ui;
    private Context context;

    private int problem = 0;
    private int score = 0;
    private int timeScore = 0;
    private int distance = 0;
    private int stage = 0;

    private ArrayList<IPicture> questionPic = new ArrayList<>();


    private int[] stageLimitScore = new int[]{600, 800}; //600,800,1100,1400,1650,2000,2700
    private int[] stageNumberOfGames = new int[]{3, 3};

    private Handler animateHandler = null;  //마커 이동시키는 핸들러
    private Handler drawDottedLineHandler = null;  // 점선 그리는 핸들러
    private Handler timerHandler = null;    //시간 진행시키는 핸들러

    final int TIME_LIMIT_MS = 14000;
    private Marker currentMarker;   //사용자가 찍은 마커
    private Marker answerMarker;    //정답 마커
    private int answerImageviewIndex;
    DrawCircleOverlay drawCircleOverlay;
    DottedLineOverlay dottedLineOverlay;
    boolean rightAnswerTypeB = false;



    enum TimerState {
        Stop,
        Running
    }

    enum GameType {
        A,
        B
    }
    private GameType gameType;
    private TimerState timerState;

    GameLogic(GameUI ui, Context context) {
        this.ui = ui;
        ui.input = this;
        this.context = context;
    }

    //post delay 사용하기
    void initiateGame() {

        ui.displayLoadingGif(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onGameEntryPoint();
            }
        },1000);

    }

    void onGameEntryPoint() {
        stage++;
        if (stage == 1 || (stage - 1 != stageLimitScore.length && score >= stageLimitScore[stage - 2])) {
            ui.displayGameEntryPoint(stage, stageLimitScore[stage - 1], stageNumberOfGames[stage - 1]);
        } else {
            ui.setRecyclerView(questionPic);
        }
    }

    /**
     * 문제 세팅
     */
    void onProblemReady() {
        int numOfPicInStage = 4;
        int[] id = new int[numOfPicInStage];
        for (int i = 0; i < numOfPicInStage; i++) { //사진 리소스 id 배열에 저장
            id[i] = R.drawable.coord0 + i;
        }
        for (int i = 0; i < 1000; i++) {    //반복하여 리소스 id 섞음
            int random = (int) (Math.random() * numOfPicInStage);
            int tmp = id[0];
            id[0] = id[random];
            id[random] = tmp;
        }

        //GPS 정보 뽑아오기
        Resources resources = context.getResources();
        for (int i = 0; i < numOfPicInStage; i++) {
            ResourcePicture pic = new ResourcePicture(id[i], resources);
//          RemoteJava.INSTANCE.getRandomPictures(10, afterPictureReceive);
            questionPic.add(pic);
            //setReverseGeocodeRegionNameAsPictureTitle(pic);
        }
        onGameReady();
        //displayQuiz();
    }

    void onGameReady(){
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        ui.displayQuiz(stage, problem, stageNumberOfGames[stage - 1]);


        timerState = TimerState.Running;
        timerHandler = new Handler();
        timeThreadhandler();

        chooseQuestionType();
    }

    private void setReverseGeocodeRegionNameAsPictureTitle(final IPicture target) {
        RemoteJava client = RemoteJava.INSTANCE;
        GeoPoint geo = Objects.requireNonNull(target.getMeta().getGeo());
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
                target.getMeta().setAddress(name);

                // 첫 문제 지역명 수신 시 문제 진행
                if (questionPic.indexOf(target) == 0) {
                    onGameReady();
                }
            }
        });
    }


    void enterPositionProblem() {
    }

    void enterPicChoiceProblem() {
    }

    void onProblemDone() {
        timerHandler = null;
        int curScore = calcScore();
        boolean isNull = false;
        if (currentMarker == null){
            isNull = true;
        }
        ui.displayAnswerLayout(gameType,curScore,distance,questionPic.get(problem),isNull);
    }

    void onNextProblem() {
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

        ui.setGameScoreTextView(score);

        return curScore;
    }

    /**
     * 지명 문제, 사진 문제를 골라서 화면에 표시
     */
    private void chooseQuestionType() {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        int randomNumber = random.nextInt(2);
        if (randomNumber == 0) {
            gameType = GameType.A;
            ui.bottomOnePicture(questionPic.get(problem));
            answerMarker = ui.makeMarker("red",Objects.requireNonNull(questionPic.get(problem).getMeta().getGeo()));

        } else if (randomNumber == 1) {
            gameType = GameType.B;
            ui.bottomFourPicture(questionPic);
            answerMarker = ui.makeMarker("red",Objects.requireNonNull(questionPic.get(problem).getMeta().getGeo()),questionPic.get(problem).getMeta().getAddress());
            answerImageviewIndex = problem;

        }
    }


    /**
     * 제한 시간 측정
     */
    private void timeThreadhandler() {
        int stageTimeLimitMs = TIME_LIMIT_MS - problem * 1000;
        ui.setGameTimeProgressBarMAx(stageTimeLimitMs);

        final long deadlineMs = new Date().getTime() + stageTimeLimitMs;
        //final Handler ui = new Handler();
        if (timerHandler != null) {
            timerHandler.post(new Runnable() {
                @Override
                public void run() {
                    long timeLeft = deadlineMs - new Date().getTime();
                    ui.setGameTimeProgressBarProgress((int) timeLeft);
                    timeScore = (int) timeLeft;

                    if (timeLeft > 0 && timerState == TimerState.Running) {
                        timerHandler.postDelayed(this, 35); // about 30fps
                        return;
                    } else if (timerState == TimerState.Stop) {
                        return;
                    }
                    timerState = TimerState.Stop;

                    // 화면을 한번 터치해 마커를 생성하고 난 후
                    // 타임아웃 발생시 그 마커를 위치로 정답 확인
                    if (gameType== GameType.A) {
                        if (currentMarker != null) {
                            timeOutAddUserMarker();
                        } else {
                            //화면에 마커 생성 없이 타임아웃 발생시 정답 확인
                            //currentMarker = new Marker(myMapView);
                            ui.addOverlay(answerMarker);
                        }
                    }   //지명 문제에서 한번 테두리가 있는 후 정답 확인과 테두리 없을 시 정답확인 구현하기
                    else if (gameType == GameType.B) {
                        ui.clearLastSelectIfExists();
                    }

                    onProblemDone();

                    ui.mapviewInvalidate();
                }
            });
        }

    }
    /**
     * 화면을 한번 클릭해 임시 마커 생성 후 타임아웃 발생시 정답 확인
     */
    private void timeOutAddUserMarker() {

        Marker tmpMarker = ui.makeMarker("blue",currentMarker.getPosition());
        ui.addOverlay(tmpMarker);

        distance = calcDistance(currentMarker.getPosition(), answerMarker.getPosition());
        animateMarker(ui.getMyMapView(), tmpMarker, answerMarker.getPosition(), new GeoPointInterpolator.Spherical());

        ui.clearOverlay(currentMarker);
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
     * 사용자가 찍은 마커가 위치에서 시작하여 정답마커까지 이동하는 애니메이션
     *
     * @param map                  mapview
     * @param marker               사용자가자 찍은 마커
     * @param finalPosition        정답 마커의 좌표
     * @param GeoPointInterpolator 마커 이동 방식
     */
    private void animateMarker(final MyMapView map, final Marker marker, final GeoPoint finalPosition, final GeoPointInterpolator GeoPointInterpolator) {
        final GeoPoint startPosition = marker.getPosition();
        animateHandler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 1000;

        map.getController().animateTo(finalPosition, map.getMinZoomLevel(), 1000L);

        drawCircleOverlay = new DrawCircleOverlay(marker.getPosition(), finalPosition, map);
        ui.addOverlay(drawCircleOverlay);

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

                    ui.clearOverlay(marker);
                    ui.addOverlay(answerMarker);
                    ui.addLine(currentMarker.getPosition(), answerMarker.getPosition());
                    dottedLineOverlay = new DottedLineOverlay(map, startPosition, answerMarker.getPosition());
                    ui.addOverlay(dottedLineOverlay);
                    drawDottedLineHandler = new Handler();
                    drawDottedLineHandler.post(new Runnable() { //반복 진행하면서 원 그리기
                        @Override
                        public void run() {
                            ui.mapviewInvalidate();
                            drawDottedLineHandler.postDelayed(this, 1000 / 60);
                        }
                    });
                    onProblemDone();


                    animateHandler = null;

                    map.invalidate();
                }
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
     * @param myMapView              mapview
     * @param finalGeoPosition 화면에 선택한 곳의 좌표
     */
    private void showMarker(final MyMapView myMapView, final GeoPoint finalGeoPosition) {

        final Projection projection = myMapView.getProjection();
        final Point startPoint = new Point();
        projection.toPixels(finalGeoPosition, startPoint);
        final Point finalPoint = new Point();

        finalPoint.x = startPoint.x;
        finalPoint.y = startPoint.y;

        startPoint.x += 40;
        startPoint.y -= 40;

        GeoPoint startGeoPosition = (GeoPoint) projection.fromPixels(startPoint.x, startPoint.y);

        final Marker tmpMarker = ui.makeMarker("blue",startGeoPosition);
        ui.addOverlay(tmpMarker);

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

                answerMarker.setAnchor(0.5f, 1.0f);
                Point pixelPoint = interpolate(v, startPoint, finalPoint);
                GeoPoint geoPoint = (GeoPoint) projection.fromPixels(pixelPoint.x, pixelPoint.y);

                tmpMarker.setPosition(geoPoint);

                if (currentMarker != null) {
                    ui.clearOverlay(currentMarker);
                }

                ui.mapviewInvalidate();
                // Repeat till progress is complete.
                if (t < 1) {
                    // 16ms 후 다시 시작
                    showMarkerHandler.postDelayed(this, 1000 / 60);
                } else {   //정답 마커 위치로 이동되면 정답 마커 추가
                    tmpMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {  //생성된 마커를 클릭하여 화면에 등록
                            timerState = TimerState.Stop;
                            //유저가 선택한 위치의 마커에서 정답 마커까지 이동하는 애니메이션 동작을 하는 마커 생성
                            Marker tmpMarker = ui.makeMarker("blue",marker.getPosition());
                            ui.addOverlay(tmpMarker);

                            distance = calcDistance(finalGeoPosition, answerMarker.getPosition());
                            animateMarker(myMapView, tmpMarker, answerMarker.getPosition(), new GeoPointInterpolator.Linear());

                            ui.clearOverlay(marker);
                            ui.mapviewInvalidate();

                            return true;
                        }
                    });
                    currentMarker = tmpMarker;
                }
            }
        });
    }

    @Override
    public void onSelectPicFirst(ImageView picImageView, View view) {
        if (picImageView == view) {
            rightAnswerTypeB = true;
        } else {
            rightAnswerTypeB = false;
        }
    }

    @Override
    public void onSelectPicSecond(ImageView picImageView, View view) {
        if (picImageView == view) {
            rightAnswerTypeB = true;
        }
        timerState = GameLogic.TimerState.Stop;
        onProblemDone();
    }

    @Override
    public void onSelectPosition(GeoPoint pos) {
    }



    @Override
    public void onPressStart() {
        score = 0;
        problem = 0;
        onProblemReady();
    }

    @Override
    public void onPressNext() {
        if (gameType == GameType.A) {
            if (currentMarker != null) {
                ui.clearOverlay(currentMarker);
                currentMarker = null;
            }
            if (drawDottedLineHandler != null) {
                drawDottedLineHandler.removeMessages(0);
                drawDottedLineHandler = null;
            }
            ui.clearOverlay(answerMarker);
            ui.clearOverlay(drawCircleOverlay);
            ui.clearOverlay(dottedLineOverlay);

        } else if (gameType == GameType.B) {
            ui.clearOverlay(answerMarker);
        }
        problem++;
        ui.clearAnswerLayout(problem,stageNumberOfGames[stage-1]);

        if (problem < stageNumberOfGames[stage - 1]) {
            chooseQuestionType();
            timerState = TimerState.Running;
            timerHandler = new Handler();
            timeThreadhandler();
        } else {
            onGameEntryPoint();
        }
    }

    @Override
    public void onPressRetry() {
    }

    @Override
    public void onPressMarker(MyMapView myMapView, GeoPoint geoPoint) {
        if (gameType == GameType.A && ui != null && animateHandler == null)
            showMarker(myMapView, geoPoint);
    }

    @Override
    public void onPressExit() {
        ui.finishActivity();
    }
}
