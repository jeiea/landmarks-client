package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.config.Configuration;
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
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.map.DrawCircleOverlay;
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

    void initiateGame() {
        ui.displayLoadingGif(true);
    }

    void onGameEntryPoint() {
        stage++;
        if (stage == 1 || (stage - 1 != stageLimitScore.length && score >= stageLimitScore[stage - 2])) {
            ui.displayGameEntryPoint(stage, stageLimitScore[stage - 1], stageNumberOfGames[stage - 1], new GameStartStageListener(), new GameFinishListener());
        } else {
            ui.setRecyclerView();
        }
    }

    /**
     * 문제 세팅
     */
    void onProblemReady() {
        int numOfPicInStage = stageNumberOfGames[stage - 1];
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
            setReverseGeocodeRegionNameAsPictureTitle(pic);
        }
        //displayQuiz();
    }

    void onGameReady(){
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        ui.displayQuiz(stage, problem, stageNumberOfGames[stage - 1], new GameNextQuizListener(), new GameFinishListener(), new PictureClickZoomingListener());


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
        if (gameType == GameType.A) {
            questionTypeALayout.setVisibility(View.GONE);
        } else if (gameType == GameType.B) {
            questionTypeBLayout.setVisibility(View.GONE);
            myMapView.getController().zoomTo(myMapView.getMinZoomLevel(), 1000L); //인자의 속도에 맞춰서 줌 아웃
        }

        int curScore = calcScore();

        answerLayout.setVisibility(View.VISIBLE);
        answerLayout.setClickable(true);
        landNameAnswerTextView.setText(questionPic.get(problem).getMeta().getAddress());
        if (gameType == GameType.A && currentMarker != null) {
            landDistanceAnswerTextView.setVisibility(View.VISIBLE);
            landDistanceAnswerTextView.setText(distance + "KM");
        } else {
            landDistanceAnswerTextView.setVisibility(View.INVISIBLE);
        }
        landScoreTextView.setText("score " + curScore);
        GlideApp.with(context).load(questionPic.get(problem)).into(pictureAnswerImageView);

    }

    void onNextProblem() {
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
                        clearLastSelectIfExists();
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
        animateMarker(myMapView, tmpMarker, answerMarker.getPosition(), new GeoPointInterpolator.Spherical());

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
     *  시작화면에서 게임화면으로 넘어가는 리스너
     */
    class GameStartStageListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            score = 0;
            problem = 0;
            onProblemReady();
        }
    }

    /**
     * 게임 액티비티 종료 리스너
     */
    class GameFinishListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ui.finishActivity();
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
    }


    View lastSelect;

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
            timerState = GameLogic.TimerState.Stop;
            clearLastSelectIfExists();
            onProblemDone();
            lastSelect = null;
        }
    }


        @Override
    public void onSelectPicture(IPicture selected) {
    }

    @Override
    public void onSelectPosition(GeoPoint pos) {
    }

    @Override
    public void onPressNext() {
    }

    @Override
    public void onPressRetry() {
    }

    @Override
    public void onPressExit() {
    }


}
