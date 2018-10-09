package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.map.DrawCircleOverlay;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.PictureMeta;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;
import kr.ac.kw.coms.globealbum.provider.UIPromise;
import kr.ac.kw.coms.landmarks.client.ReverseGeocodeResult;

interface IGameInputHandler {
    void onSelectPictureCertainly(IPicture selected);

    void onSelectPosition(GeoPoint pos);

    void onTimeout();

    void onPressStart();

    void onPressNext();

    void onPressRetry();

    void onPressExit();

    @Deprecated
    void onPressMarker(MyMapView myMapView, GeoPoint geoPoint);

    void onTouchMap(GeoPoint pt);
}

class GameLogic implements IGameInputHandler {
    private IGameUI rui;
    private GameUI ui;
    private Context context;

    private int problem = 0;
    private int score = 0;
    private int timeScore = 0;
    private int stage = 0;

    private ArrayList<IPicture> questionPic = new ArrayList<>();


    private int[] stageLimitScore = new int[]{400, 500, 600, 800}; //600,800,1100,1400,1650,2000,2700
    private int[] stageNumberOfGames = new int[]{3, 3, 3, 3};

    private Handler animateHandler = null;  //마커 이동시키는 핸들러
    private Handler drawDottedLineHandler = null;  // 점선 그리는 핸들러

    private DrawCircleOverlay drawCircleOverlay;
    private DottedLineOverlay dottedLineOverlay;
    private boolean rightAnswerTypeB = false;
    private GameState state;

    private Random random = new Random(System.currentTimeMillis());

    enum GameState {
        LOADING,
        STAGE_READY,
        SOLVING,
        GRADING,
        GAME_OVER,
    }

    enum GameType {
        POSITION,
        PICTURE
    }

    private GameType gameType;

    GameLogic(GameUI ui, Context context) {
        rui = this.ui = ui;
        rui.setInputHandler(this);
        this.context = context;
    }

    //post delay 사용하기
    void initiateGame() {
        state = GameState.LOADING;
        rui.showLoadingGif();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onGameEntryPoint();
            }
        }, 2000);
    }

    private void onGameEntryPoint() {
        stage++;
        boolean isFirstStage = stage == 1;
        boolean isNotEnd = stage - 1 > stageLimitScore.length && score >= stageLimitScore[stage - 2];
        if (isFirstStage || isNotEnd) {
            state = GameState.STAGE_READY;
            rui.showGameEntryPoint(stage, stageLimitScore[stage - 1], stageNumberOfGames[stage - 1]);
        } else {
            state = GameState.GAME_OVER;
            rui.showGameOver(questionPic);
        }
    }

    /**
     * 문제 세팅
     */
    private void onProblemReady() {
        int numOfPicInStage = 4;
        int[] id = new int[numOfPicInStage];
        //사진 리소스 id 배열에 저장
        for (int i = 0; i < numOfPicInStage; i++) {
            id[i] = R.drawable.coord0 + i;
        }
        //반복하여 리소스 id 섞음
        for (int i = 0; i < 1000; i++) {
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

    private void onGameReady() {
        rui.setQuizInfo(stage, problem, stageNumberOfGames[stage - 1]);
        chooseQuestionType();

        state = GameState.SOLVING;
        int TIME_LIMIT_MS = 14000;
        rui.startTimer(TIME_LIMIT_MS);
    }

    /**
     * 지명 문제, 사진 문제를 골라서 화면에 표시
     */
    private void chooseQuestionType() {
        // TODO: capsulize quiz type
        int randomNumber = random.nextInt(2);
        if (randomNumber == 0) {
            enterPositionProblem();
        } else if (randomNumber == 1) {
            enterPicChoiceProblem();
        }
    }

    private void enterPositionProblem() {
        gameType = GameType.POSITION;
        rui.showPositionQuiz(questionPic.get(problem));
        GeoPoint rightPos = questionPic.get(problem).getMeta().getGeo();
        rui.getSystemMarker().setPosition(Objects.requireNonNull(rightPos));
    }

    private void enterPicChoiceProblem() {
        gameType = GameType.PICTURE;
        PictureMeta meta = questionPic.get(problem).getMeta();
        rui.showPictureQuiz(questionPic, meta.getAddress());
        rui.getSystemMarker().setPosition(Objects.requireNonNull(meta.getGeo()));

        // TODO: this answer is predictable, set it randomly
    }

    private void onProblemDone() {
        int deltaScore = calcScore();
        double distance = calcDistance();
        if (gameType == GameType.POSITION) {
            rui.showPositionAnswer(questionPic.get(problem), deltaScore, distance);
        } else {
            rui.showPictureAnswer(questionPic.get(problem), deltaScore);
        }
    }

    /**
     * 점수를 계산
     *
     * @return 점수를 계산하여 리턴
     */
    private int calcScore() {
        int curScore = 0;
        if (gameType == GameType.POSITION) {
            if (!rui.getUserMarker().isEnabled()) { //마커를 화면에 찍지 않고 정답을 확인하는 경우
                curScore = -100;
            } else {
                double distance = calcDistance();
                if (distance >= 6000) {
                    curScore = 0;
                } else {
                    int criteria = 300;
                    curScore = criteria - (int) distance / 25;
                    curScore += timeScore / 100;
                }
            }
        } else if (gameType == GameType.PICTURE) {
            if (!rightAnswerTypeB) {
                curScore = -100;
            } else {
                curScore = 300;
                curScore += timeScore / 100;
            }
        }
        score += curScore;

        rui.setScore(score);

        return curScore;
    }

    /**
     * 화면을 한번 클릭해 임시 마커 생성 후 타임아웃 발생시 정답 확인
     */
    @Override
    public void onTimeout() {
        rui.stopTimer();
        state = GameState.GRADING;

        // 화면을 한번 터치해 마커를 생성하고 난 후
        // 타임아웃 발생시 그 마커를 위치로 정답 확인
        if (gameType == GameType.POSITION) {
            if (rui.getUserMarker().isEnabled()) {
                processProvisional();
            } else {
                //화면에 마커 생성 없이 타임아웃 발생시 정답 확인
                rui.getSystemMarker().setEnabled(true);
            }
        }   //지명 문제에서 한번 테두리가 있는 후 정답 확인과 테두리 없을 시 정답확인 구현하기
        else if (gameType == GameType.PICTURE) {
            ui.clearLastSelectIfExists();
        }

        onProblemDone();

        ui.mapviewInvalidate();
    }

    private void processProvisional() {
        Marker user = rui.getUserMarker();

        GeoPoint rightAns = questionPic.get(problem).getMeta().getGeo();
        Marker sys = rui.getSystemMarker();
        sys.setEnabled(true);
        animateMarker(ui.getMyMapView(), sys, rightAns, new GeoPointInterpolator.Spherical());
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

    private double calcDistance() {
        GeoPoint g1 = rui.getSystemMarker().getPosition();
        GeoPoint g2 = rui.getUserMarker().getPosition();
        return g1.distanceToAsDouble(g2) / 1000;
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
                } else {
                    //정답 마커 위치로 이동되면 정답 마커 추가
                    rui.getSystemMarker().setEnabled(true);
                    dottedLineOverlay = new DottedLineOverlay(map, startPosition, rui.getSystemMarker().getPosition());
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
     * @param myMapView        mapview
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

                Point pixelPoint = interpolate(v, startPoint, finalPoint);
                GeoPoint geoPoint = (GeoPoint) projection.fromPixels(pixelPoint.x, pixelPoint.y);

                Marker anim = rui.getUserMarker();
                anim.setPosition(geoPoint);
                anim.setEnabled(true);

                ui.mapviewInvalidate();
                // Repeat till progress is complete.
                if (t < 1) {
                    // 16ms 후 다시 시작
                    showMarkerHandler.postDelayed(this, 1000 / 60);
                } else {
                    // 플레이어가 생성한 마커를 다시 터치하면 확정
                    anim.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                            state = GameState.GRADING;
                            rui.stopTimer();
                            // 유저가 선택한 위치의 마커에서 정답 마커까지 이동하는 애니메이션
                            // 동작을 하는 마커 생성
                            Marker sys = rui.getSystemMarker();
                            GeoPoint rightPos = sys.getPosition();
                            sys.setPosition(rui.getUserMarker().getPosition());
                            sys.setEnabled(true);
                            animateMarker(myMapView, sys, rightPos, new GeoPointInterpolator.Linear());
                            ui.mapviewInvalidate();
                            return true;
                        }
                    });
                }
            }
        });
    }

    void finishTimerHandler() {
        rui.stopTimer();
    }

    @Override
    public void onSelectPictureCertainly(IPicture selected) {
        if (selected == questionPic.get(problem)) {
            rightAnswerTypeB = true;
        }
        rui.stopTimer();
        state = GameState.GRADING;
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
        if (gameType == GameType.POSITION) {
            if (drawDottedLineHandler != null) {
                drawDottedLineHandler.removeMessages(0);
                drawDottedLineHandler = null;
            }
            ui.clearOverlay(drawCircleOverlay);
            ui.clearOverlay(dottedLineOverlay);
        }
        problem++;
        ui.clearAnswerLayout(problem, stageNumberOfGames[stage - 1]);

        if (problem < stageNumberOfGames[stage - 1]) {
            chooseQuestionType();
            state = GameState.SOLVING;
            rui.startTimer(14000);
        } else {
            onGameEntryPoint();
        }
    }

    @Override
    public void onPressRetry() {
    }

    @Override
    public void onPressMarker(MyMapView myMapView, GeoPoint geoPoint) {
        if (gameType == GameType.POSITION && state == GameState.SOLVING && animateHandler == null) {
            showMarker(myMapView, geoPoint);
        }
    }

    @Override
    public void onTouchMap(GeoPoint pt) {
        if (gameType == GameType.POSITION && state == GameState.SOLVING && animateHandler == null) {
            // showMarker(...).. not implemented
        }
    }

    @Override
    public void onPressExit() {
        rui.exitGame();
    }

    /**
     * ResourcePicture 시절 주소가 없는 걸 얻고자 만든 것. 쓰게 될지는 모름.
     */
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
}
