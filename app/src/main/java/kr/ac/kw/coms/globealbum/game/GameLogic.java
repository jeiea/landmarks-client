package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.PictureMeta;
import kr.ac.kw.coms.globealbum.provider.Promise;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.RemotePicture;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;
import kr.ac.kw.coms.globealbum.provider.UIPromise;

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
    private IGameUI ui;
    private Context context;

    private int problem;
    private int score;
    private long msQuestionStart;
    private int stage;

    /**
     * 현재 문제
     */
//    private ArrayList<IPicture> hotPictures = new ArrayList<>();
    private IGameQuiz currentQuiz;
    /**
     * 출제되었고 나중에 정리해서 보여줄 사진들
     */
    private ArrayList<IPicture> shownPictures = new ArrayList<>();


    private int[] stageLimitScore = new int[]{400, 500, 600, 800}; //600,800,1100,1400,1650,2000,2700
    private int[] stageNumberOfGames = new int[]{3, 3, 3, 3};

    private boolean rightAnswerTypeB;
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
        this.ui = ui;
        this.ui.setInputHandler(this);
        this.context = context;
    }

    private void resetGameStatus() {
        state = GameState.LOADING;
        problem = 0;
        score = 0;
        stage = 0;
        ui.setScore(score);
    }

    void initiateGame() {
        ui.showLoadingGif();
        resetGameStatus();
        realLoading();
    }

    private void dummyLoading() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                enterStageEntry();
            }
        }, 2000);
    }

    private void realLoading() {
        // getRandomPictures에서 하나를 일단 가져와야 미래에도 버퍼링이 보장됨.
        RemoteJava.INSTANCE.getRandomPictures(1, onReadyBuffer);
    }

    class ErrorToastPromise extends UIPromise<List<RemotePicture>> {
        @Override
        public void failure(@NotNull Throwable cause) {
            String msg = String.format("서버 연결에 실패했습니다: %s", cause.toString());
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            ui.exitGame();
        }
    }

    private Promise<List<RemotePicture>> onReadyBuffer = new ErrorToastPromise() {
        @Override
        public void success(List<RemotePicture> result) {
            enterStageEntry();
        }
    };

    private void enterStageEntry() {
        stage++;
        boolean isFirstStage = stage == 1;
        boolean isNotEnd = stage - 1 > stageLimitScore.length && score >= stageLimitScore[stage - 2];
        if (isFirstStage || isNotEnd) {
            state = GameState.STAGE_READY;
            ui.showGameEntryPoint(stage, stageLimitScore[stage - 1], stageNumberOfGames[stage - 1]);
        } else {
            state = GameState.GAME_OVER;
            ui.showGameOver(shownPictures);
        }
    }

    @Override
    public void onPressStart() {
        problem = 0;
        enterNewQuiz();
    }

    @Override
    public void onPressExit() {
        ui.exitGame();
    }

    /**
     * 앱 리소스로 문제 세팅
     */
    private void receiveQuizResources() {
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

        // 문제 진입
        Resources resources = context.getResources();
        if (random.nextBoolean()) {
            ResourcePicture pic = new ResourcePicture(id[0], resources);
            PositionQuiz quiz = new PositionQuiz(pic);
            enterPositionQuiz(quiz);
        } else {
            ArrayList<ResourcePicture> pics = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                pics.add(new ResourcePicture(id[i], resources));
            }
            PicChoiceQuiz quiz = new PicChoiceQuiz(pics, random);
            enterPicChoiceQuiz(quiz);
        }
    }

    private void receiveQuizRemote() {
        int n = random.nextBoolean() ? 1 : 4;
        RemoteJava.INSTANCE.getRandomPictures(n, onReceivePictures);
    }

    private void enterNewQuiz() {
        receiveQuizRemote();
//        receiveQuizResources();
        ui.setQuizInfo(stage, problem, stageNumberOfGames[stage - 1]);
    }

    private Promise<List<RemotePicture>> onReceivePictures = new ErrorToastPromise() {
        @Override
        public void success(@NonNull List<RemotePicture> result) {
            if (result.size() == 1) {
                PositionQuiz quiz = new PositionQuiz(result.get(0));
                enterPositionQuiz(quiz);
            } else {
                PicChoiceQuiz quiz = new PicChoiceQuiz(result, random);
                enterPicChoiceQuiz(quiz);
            }
        }
    };

    private void enterPositionQuiz(@NonNull PositionQuiz quiz) {
        gameType = GameType.POSITION;
        ui.showPositionQuiz(quiz.picture);
        GeoPoint rightPos = quiz.picture.getMeta().getGeo();
        ui.getSystemMarker().setPosition(Objects.requireNonNull(rightPos));

        enterQuizCommon(quiz);
    }

    private void enterPicChoiceQuiz(@NonNull PicChoiceQuiz quiz) {
        gameType = GameType.PICTURE;
        PictureMeta meta = quiz.getCorrectPicture().getMeta();
        ui.showPictureQuiz(quiz.pictures, meta.getAddress());
        ui.getSystemMarker().setPosition(Objects.requireNonNull(meta.getGeo()));

        enterQuizCommon(quiz);
    }

    private void enterQuizCommon(IGameQuiz quiz) {
        currentQuiz = quiz;
        state = GameState.SOLVING;
        msQuestionStart = new Date().getTime();
        ui.startTimer(MS_TIME_LIMIT);
    }

    private void onProblemDone() {
        int deltaScore = reflectScoreAndGetDelta();
        double distance = calcDistanceKm();
        for (IPicture pic : currentQuiz.getUsedPictures()) {
            if (!shownPictures.contains(pic)) {
                shownPictures.add(pic);
            }
        }
        if (currentQuiz instanceof PositionQuiz) {
            ui.showPositionAnswer(((PositionQuiz) currentQuiz).picture, deltaScore, distance);
        } else {
            ui.showPicChoiceAnswer(((PicChoiceQuiz) currentQuiz).getCorrectPicture(), deltaScore);
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
            if (!ui.getUserMarker().isEnabled()) {
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
        ui.setScore(score);
        return deltaScore;
    }

    /**
     * 화면을 한번 클릭해 임시 마커 생성 후 타임아웃 발생시 정답 확인
     */
    @Override
    public void onTimeout() {
        ui.stopTimer();
        state = GameState.GRADING;

        // 화면을 한번 터치해 마커를 생성하고 난 후
        // 타임아웃 발생시 그 마커를 위치로 정답 확인
        if (gameType == GameType.POSITION) {
            if (ui.getUserMarker().isEnabled()) {
                showDifferenceAnimAndScore(new GeoPointInterpolator.Spherical());
            } else {
                //화면에 마커 생성 없이 타임아웃 발생시 정답 확인
                Marker sys = ui.getSystemMarker();
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
        ui.getUserMarker().setOnMarkerClickListener(null);
        state = GameState.GRADING;
        ui.stopTimer();

        Marker sys = ui.getSystemMarker();
        sys.setPosition(ui.getUserMarker().getPosition());
        IPicture pic = currentQuiz.getUsedPictures().iterator().next();
        GeoPoint rightPos = pic.getMeta().getGeo();
        ui.animateMarker(sys, rightPos, geopolator);

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
        GeoPoint g1 = ui.getSystemMarker().getPosition();
        GeoPoint g2 = ui.getUserMarker().getPosition();
        return g1.distanceToAsDouble(g2) / 1000;
    }

    void releaseResources() {
        ui.exitGame();
    }

    @Override
    public void onSelectPictureCertainly(IPicture selected) {
        rightAnswerTypeB = selected == ((PicChoiceQuiz) currentQuiz).getCorrectPicture();
        ui.stopTimer();
        state = GameState.GRADING;
        onProblemDone();
    }

    @Override
    public void onPressNext() {
        problem++;
        if (problem < stageNumberOfGames[stage - 1]) {
            enterNewQuiz();
        } else {
            enterStageEntry();
        }
    }

    @Override
    public void onTouchMap(GeoPoint pt) {
        if (gameType == GameType.POSITION && state == GameState.SOLVING) {
            pointMarker(pt);
        }
    }

    private void pointMarker(GeoPoint pt) {
        final Marker user = ui.getUserMarker();
        ui.pointMarker(user, pt);
        user.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                showDifferenceAnimAndScore(new GeoPointInterpolator.Linear());
                return true;
            }
        });
    }

    @Override
    public void onPressRetry() {
        resetGameStatus();
        enterStageEntry();
    }
}
