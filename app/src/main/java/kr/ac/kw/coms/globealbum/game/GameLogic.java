package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.PictureMeta;
import kr.ac.kw.coms.globealbum.provider.Promise;
import kr.ac.kw.coms.globealbum.provider.UIPromise;

interface IGameInputHandler {
    @NonNull
    View.OnLayoutChangeListener getOnImageViewSizeChanged();

    void onSelectPictureFirst(IPicture selected);

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
    private GameQuizFactory quizFactory;

    private int problem;
    private int score;
    private long msQuestionStart;
    private int stage;

    /**
     * 현재 문제
     */
    private IGameQuiz currentQuiz;
    /**
     * 출제되었고 나중에 정리해서 보여줄 사진들
     */
    private ArrayList<IPicture> shownPictures = new ArrayList<>();


    private int[] stageLimitScore = new int[]{450, 500, 575, 700, 900}; //600,800,1100,1400,1650,2000,2700
    private int[] stageNumberOfGames = new int[]{3, 3, 3, 3, 3};

    private boolean rightAnswerTypePic;
    private GameState state;

    private final int MS_TIME_LIMIT = 9000;

    enum GameState {
        LOADING,
        STAGE_READY,
        SOLVING,
        GRADING,
        GAME_OVER,
    }

    GameLogic(GameUI ui, Context context) {
        quizFactory = new GameQuizFactory(context);
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

    @NonNull
    @Override
    public View.OnLayoutChangeListener getOnImageViewSizeChanged() {
        return quizFactory;
    }

    private void realLoading() {
        quizFactory.fetchQuiz(onReadyBuffer);
    }

    class ErrorToastPromise<T> extends UIPromise<T> {
        @Override
        public void failure(@NotNull Throwable cause) {
            String msg = String.format("서버 연결에 실패했습니다: %s", cause.toString());
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            ui.exitGame();
        }
    }

    private Promise<IGameQuiz> onReadyBuffer = new ErrorToastPromise<IGameQuiz>() {
        @Override
        public void success(IGameQuiz result) {
            if (currentQuiz != null) {
                currentQuiz.dispose();
            }
            currentQuiz = result;
            enterStageEntry();
        }
    };

    private void enterStageEntry() {
        stage++;
        boolean isFirstStage = stage == 1;
        boolean isNotEnd = isFirstStage || stage - 1 < stageLimitScore.length && score >= stageLimitScore[stage - 2];
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
        score = 0;
        enterNewQuiz();
    }

    @Override
    public void onPressExit() {
        ui.exitGame();
    }

    private void enterNewQuiz() {
        ui.setQuizInfo(stage, problem, score, stageNumberOfGames[stage - 1]);
        // 처음은 미리 문제를 가져오기에 다시 가져오면 중복
        if (stage == 1 && problem == 0) {
            onReceivePictures.success(currentQuiz);
        } else {
            quizFactory.fetchQuiz(onReceivePictures);
        }
    }

    private Promise<IGameQuiz> onReceivePictures = new ErrorToastPromise<IGameQuiz>() {
        public void success(@NonNull IGameQuiz quiz) {
            if (quiz instanceof PositionQuiz) {
                enterQuizCommon(quiz);
            } else if (quiz instanceof PicChoiceQuiz) {
                enterQuizCommon(quiz);

                PictureMeta meta = ((PicChoiceQuiz) quiz).getCorrectPicture().getMeta();
                ui.getSystemMarker().setTitle(meta.getAddress());
                ui.getSystemMarker().setPosition(Objects.requireNonNull(meta.getGeo()));
            } else {
                failure(new RuntimeException("뭔가 잘못 만들었나봐요.."));
            }
        }
    };

    private void enterQuizCommon(IGameQuiz quiz) {
        currentQuiz = quiz;
        state = GameState.SOLVING;
        msQuestionStart = new Date().getTime();
        ui.showQuiz(quiz);
        ui.startTimer(MS_TIME_LIMIT - 1000 * stage);
    }

    private void checkAnswerAndGrading() {
        int deltaScore = reflectScoreAndGetDelta();
        for (IPicture pic : currentQuiz.getUsedPictures()) {
            if (!shownPictures.contains(pic)) {
                shownPictures.add(pic);
            }
        }
        if (currentQuiz instanceof PositionQuiz) {
            showPositionQuizAnswer((PositionQuiz) currentQuiz, deltaScore);
        } else {
            ui.showAnswer(currentQuiz, deltaScore);
            rightAnswerTypePic = false;
        }
    }

    private void showPositionQuizAnswer(@NonNull PositionQuiz quiz, int deltaScore) {
        // 정답 마커 표시
        GeoPoint rightPos = quiz.getPicture().getMeta().getGeo();
        ui.getSystemMarker().setPosition(Objects.requireNonNull(rightPos));
        ui.getSystemMarker().setEnabled(true);

        ui.showAnswer(quiz, deltaScore);
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
        if (currentQuiz instanceof PositionQuiz) {
            // 마커를 화면에 찍지 않고 정답을 확인하는 경우
            if (!ui.getUserMarker().isEnabled()) {
                return -100;
            } else {
                PositionQuiz quiz = (PositionQuiz) currentQuiz;
                double distance = quiz.getKmFrom(ui.getUserMarker());
                if (distance >= 8000) {
                    return 0;
                }
                int perfect = 300;
                int distanceCut = (int) distance / 40;
                int timeBonus = calcTimeBonusScore();
                return perfect - distanceCut + timeBonus;
            }
        } else if (currentQuiz instanceof PicChoiceQuiz) {
            if (!rightAnswerTypePic) {
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
        if (currentQuiz instanceof PositionQuiz) {
            if (ui.getUserMarker().isEnabled()) {
                showDifferenceAnimAndScore(new GeoPointInterpolator.Linear());
            } else {
                //화면에 마커 생성 없이 타임아웃 발생 시 채점
                checkAnswerAndGrading();
            }
        } else {
            checkAnswerAndGrading();
        }
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
                checkAnswerAndGrading();
            }
        }, 1000);
    }

    void releaseResources() {
        ui.exitGame();
    }

    @Override
    public void onSelectPictureFirst(IPicture selected) {
        rightAnswerTypePic = selected == ((PicChoiceQuiz) currentQuiz).getCorrectPicture();
    }

    @Override
    public void onSelectPictureCertainly(IPicture selected) {
        rightAnswerTypePic = selected == ((PicChoiceQuiz) currentQuiz).getCorrectPicture();
        ui.stopTimer();
        state = GameState.GRADING;
        checkAnswerAndGrading();
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
        if (currentQuiz instanceof PositionQuiz && state == GameState.SOLVING) {
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
