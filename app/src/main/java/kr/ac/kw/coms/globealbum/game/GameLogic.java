package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.map.DrawCircleOverlay;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.PictureMeta;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;
import kr.ac.kw.coms.globealbum.provider.UIPromise;
import kr.ac.kw.coms.landmarks.client.ReverseGeocodeResult;

interface IGameInputHandler {
    void onSelectPictureCertainly(IPicture selected);

    void onTouchMap(GeoPoint pt);

    void onTimeout();

    void onPressStart();

    void onPressNext();

    void onPressRetry();

    void onPressExit();
}

class GameLogic implements IGameInputHandler {
    private IGameUI rui;
    private GameUI ui;
    private Context context;

    private int problem = 0;
    private int score = 0;
    private long msQuestionStart;
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
    private final int MS_TIME_LIMIT = 14000;

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
        msQuestionStart = new Date().getTime();
        rui.startTimer(MS_TIME_LIMIT);
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
        int deltaScore = reflectScoreAndGetDelta();
        double distance = calcDistanceKm();
        if (gameType == GameType.POSITION) {
            rui.showPositionAnswer(questionPic.get(problem), deltaScore, distance);
        } else {
            rui.showPictureAnswer(questionPic.get(problem), deltaScore);
        }
    }

    private int calcTimeBonusScore() {
        long elapsed = new Date().getTime() - msQuestionStart;
        long remaining = Math.max(0, MS_TIME_LIMIT - elapsed);
        return (int) remaining / 100;
    }

    /**
     * 이번 문제의 점수를 계산
     *
     * @return 점수를 계산하여 리턴
     */
    private int calcProblemScore() {
        if (gameType == GameType.POSITION) {
            // 마커를 화면에 찍지 않고 정답을 확인하는 경우
            if (!rui.getUserMarker().isEnabled()) {
                return -100;
            } else {
                double distance = calcDistanceKm();
                if (distance >= 6000) {
                    return 0;
                }
                int perfect = 300;
                int distanceCut = (int) distance / 25;
                int timeBonus = calcTimeBonusScore();
                return perfect - distanceCut + timeBonus;
            }
        } else if (gameType == GameType.PICTURE) {
            if (!rightAnswerTypeB) {
                return -100;
            } else {
                return 300 + calcTimeBonusScore();
            }
        } else {
            throw new AssertionError("What kind of problem?");
        }
    }

    /**
     * 점수 계산 후 UI에 반영, 증감점수 리턴
     *
     * @return 증감한 점수
     */
    private int reflectScoreAndGetDelta() {
        int deltaScore = calcProblemScore();
        score += deltaScore;
        rui.setScore(score);
        return deltaScore;
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
                showDifferenceAnimAndScore(new GeoPointInterpolator.Spherical());
            } else {
                //화면에 마커 생성 없이 타임아웃 발생시 정답 확인
                Marker sys = rui.getSystemMarker();
                sys.showInfoWindow();
                sys.setEnabled(true);
            }
        }

        onProblemDone();
    }

    /**
     * animateMarker를 대신함. 타이머를 멈추고, 애니메이션을 보여주고, 1초 후에
     * 점수를 보여줌.
     */
    private void showDifferenceAnimAndScore(GeoPointInterpolator geopolator) {
        rui.getUserMarker().setOnMarkerClickListener(null);
        state = GameState.GRADING;
        rui.stopTimer();

        Marker sys = rui.getSystemMarker();
        sys.setPosition(rui.getUserMarker().getPosition());
        GeoPoint rightPos = questionPic.get(problem).getMeta().getGeo();
        rui.animateMarker(sys, rightPos, geopolator);

        // Display score
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onProblemDone();
            }
        }, 1000);
    }

    /**
     * 사용자가 찍은 점과 정답 사이의 km 거리를 빈환
     *
     * @return km 단위 거리
     */
    private double calcDistanceKm() {
        GeoPoint g1 = rui.getSystemMarker().getPosition();
        GeoPoint g2 = rui.getUserMarker().getPosition();
        return g1.distanceToAsDouble(g2) / 1000;
    }

    void releaseResources() {
        rui.exitGame();
    }

    @Override
    public void onSelectPictureCertainly(IPicture selected) {
        rightAnswerTypeB = selected == questionPic.get(problem);
        rui.stopTimer();
        state = GameState.GRADING;
        onProblemDone();
    }

    @Override
    public void onPressStart() {
        score = 0;
        problem = 0;
        onProblemReady();
    }

    @Override
    public void onPressNext() {
        problem++;
        if (problem < stageNumberOfGames[stage - 1]) {
            rui.setQuizInfo(stage, problem, stageNumberOfGames[stage - 1]);
            chooseQuestionType();
            state = GameState.SOLVING;
            rui.startTimer(MS_TIME_LIMIT);
        } else {
            onGameEntryPoint();
        }
    }

    @Override
    public void onPressRetry() {
    }

    @Override
    public void onTouchMap(GeoPoint pt) {
        if (gameType == GameType.POSITION && state == GameState.SOLVING) {
            pointMarker(pt);
        }
    }

    private void pointMarker(GeoPoint pt) {
        final Marker user = rui.getUserMarker();
        rui.pointMarker(user, pt);
        user.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                showDifferenceAnimAndScore(new GeoPointInterpolator.Linear());
                return true;
            }
        });
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
