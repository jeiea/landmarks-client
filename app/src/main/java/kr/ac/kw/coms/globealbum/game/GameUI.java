package kr.ac.kw.coms.globealbum.game;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jetbrains.annotations.Contract;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.common.PictureDialogFragment;
import kr.ac.kw.coms.globealbum.map.DrawCircleOverlay;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.map.OverlayManagerForDoubleTapDisable;
import kr.ac.kw.coms.globealbum.provider.IPicture;

interface IGameUI {
    void setInputHandler(IGameInputHandler handler);

    void showLoadingGif();

    void showGameEntryPoint(int stage, int score, int games);

    void showGameOver(List<IPicture> pictures);

    void setQuizInfo(int stage, int curProblem, int curScore, int allProblem);

    void showQuiz(IGameQuiz quiz);

    void showAnswer(IGameQuiz quiz, int deltaScore);

    Marker getUserMarker();

    Marker getSystemMarker();

    void pointMarker(Marker marker, GeoPoint pt);

    void animateMarker(Marker marker, GeoPoint destination, GeoPointInterpolator geopolator);

    void setScore(int score);

    void startTimer(int msDuration);

    void stopTimer();

    void exitGame();
}

/**
 * GameUI를 관리하는 클래스... 라고 간단히 정의할 수 있다면 좋겠지만.
 * UI 부분을 따로 클래스로 분리해서 가지고 있는 클래스 하나와 로직 요청을 해석해서
 * UI 객체 요청으로 바꿔주는 어댑터 클래스로 더 쪼갤 수 있을 것 같음.
 * 그렇게까지 해야할까 싶으므로 일단 현재 기준은 모든 책임을 UI로, 귀찮으면 로직.
 */
class GameUI implements IGameUI {
    private AppCompatActivity activity;
    private IGameInputHandler input;

    // screens
    private ViewGroup rootView;
    private View loadingView;
    private ReadyScreen readyScreen;
    private View quizView;
    private View recollectionView;

    // game
    private MyMapView myMapView;
    private ProgressBar gameTimeProgressBar;
    private TextView gameStageTextView;
    private TextView gameScoreTextView;
    private TextView gameTargetTextView;
    private Marker userMarker;
    private Marker systemMarker;

    private Handler handler = new Handler();
    private TimerProgressBar timer;
    private InvalidationHelper mapInvalidator;

    // answer
    private TextView answerLandPlaceNameTextView;
    private TextView answerLandCountryTextView;
    private TextView answerDistanceTextView;
    private TextView answerScoreTextView;
    private ImageView answerCorrectImageView;
    private ConstraintLayout answerLayout;

    // problem type
    private ConstraintLayout positionProblemLayout;
    private ConstraintLayout choicePicProblemLayout;
    private Drawable redRect;
    private ImageView positionPicImageView;
    private ImageView[] choicePicImageViews = new ImageView[4];
    private List<IPicture> choicePics;
    private DottedLineOverlay dotLineAnimation;
    private DrawCircleOverlay circleAnimation;

    GameUI(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        activity.setContentView(R.layout.layout_empty);
        rootView = activity.findViewById(R.id.lc_empty_root);
        initQuiz();
        readyScreen = new ReadyScreen();
        recollectionView = rootInflate(R.layout.layout_after_game_recycler_view);
    }

    private void initQuiz() {
        quizView = rootInflate(R.layout.activity_game);

        //game layout
        gameTimeProgressBar = quizView.findViewById(R.id.progressbar);
        gameStageTextView = quizView.findViewById(R.id.textview_stage);
        gameTargetTextView = quizView.findViewById(R.id.textview_target);
        gameScoreTextView = quizView.findViewById(R.id.textview_score);
        redRect = activity.getResources().getDrawable(R.drawable.rectangle_border, null);

        //answer layout
        answerLayout = quizView.findViewById(R.id.layout_answer);
        answerCorrectImageView = quizView.findViewById(R.id.picture_answer);
        answerDistanceTextView = quizView.findViewById(R.id.textview_land_distance_answer);
        answerLandPlaceNameTextView = quizView.findViewById(R.id.textview_land_place_name_answer);
        answerLandCountryTextView = quizView.findViewById(R.id.textview_land_country_name_answer);
        answerScoreTextView = quizView.findViewById(R.id.textview_land_score_answer);

        ImageView answerGoToNextStageButton = quizView.findViewById(R.id.button_next);
        answerGoToNextStageButton.setOnClickListener(onPressNext);
        ImageView answerExitButton = quizView.findViewById(R.id.button_exit);
        answerExitButton.setOnClickListener(onPressExit);

        //game type layout
        positionProblemLayout = quizView.findViewById(R.id.position_problem);
        choicePicProblemLayout = quizView.findViewById(R.id.choice_pic);
        int[] imgViewIds = new int[]{R.id.picture1, R.id.picture2, R.id.picture3, R.id.picture4};
        for (int i = 0; i < imgViewIds.length; i++) {
            choicePicImageViews[i] = quizView.findViewById(imgViewIds[i]);
            choicePicImageViews[i].setOnClickListener(onPressPicFirst);
        }

        positionPicImageView = quizView.findViewById(R.id.picture);
        positionPicImageView.setOnClickListener(onPressPicZoom);

        myMapView = quizView.findViewById(R.id.map);
        myMapView.setTileSource(TileSourceFactory.BASE_OVERLAY_NL);    //맵 렌더링 설정
        myMapView.setMaxZoomLevel(5.0);
        TilesOverlay tiOv = new TilesOverlay(myMapView.getTileProvider(), myMapView.getContext());
        OverlayManagerForDoubleTapDisable ovMa = new OverlayManagerForDoubleTapDisable(tiOv);
        myMapView.setOverlayManager(ovMa);
        myMapView.getOverlays().add(onTouchMap);
        mapInvalidator = new InvalidationHelper(handler, myMapView, 1000 / 60);

        circleAnimation = new DrawCircleOverlay();
        myMapView.getOverlays().add(circleAnimation);

        dotLineAnimation = new DottedLineOverlay();
        myMapView.getOverlays().add(dotLineAnimation);

        Resources resources = quizView.getResources();
        Drawable redMarkerDrawable = resources.getDrawable(R.drawable.game_red_marker, null);
        systemMarker = makeMarker(redMarkerDrawable);
        addBalloonToMarker(systemMarker);

        Drawable blueMarkerDrawable = resources.getDrawable(R.drawable.game_blue_marker, null);
        userMarker = makeMarker(blueMarkerDrawable);
    }

    @NonNull
    private Marker makeMarker(Drawable icon) {
        Marker marker = new Marker(myMapView);
        marker.setIcon(icon);
        marker.setAnchor(0.5f, 1.0f);
        marker.setEnabled(false);
        myMapView.getOverlays().add(marker);
        return marker;
    }

    private void addBalloonToMarker(@NonNull Marker marker) {
        marker.setTitle("");
        MarkerInfoWindow miw = new MarkerInfoWindow(R.layout.game_infowindow_bubble, myMapView);
        // infowindow 터치 시 사라지는 것 방지
        miw.getView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            @SuppressWarnings("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        marker.setInfoWindow(miw);
    }

    @Override
    public void setInputHandler(IGameInputHandler input) {
        this.input = input;
        positionPicImageView.addOnLayoutChangeListener(input.getOnImageViewSizeChanged());
        choicePicImageViews[0].addOnLayoutChangeListener(input.getOnImageViewSizeChanged());
    }

    @Override
    public void showLoadingGif() {
        hideAllStateViews();
        loadingView = rootInflate(R.layout.layout_game_loading_animation);
        ImageView imgLoading = activity.findViewById(R.id.game_loading_animation);
        GlideApp.with(imgLoading).load(R.drawable.game_loading_bar).into(imgLoading);
    }

    private void removeLoadingGif() {
        if (loadingView != null) {
            rootView.removeView(loadingView);
            loadingView = null;
        }
    }

    @Override
    public void showGameEntryPoint(int stage, int score, int games) {
        removeLoadingGif();
        hideAllStateViews();

        handler.removeCallbacksAndMessages(null);
        readyScreen.setLabelAndChangeBackground(stage, score, games);
        readyScreen.setVisibility(View.VISIBLE);
    }

    private void hideAllStateViews() {
        for (int i = 0; i < rootView.getChildCount(); i++) {
            rootView.getChildAt(i).setVisibility(View.INVISIBLE);
        }
    }

    class ReadyScreen {
        private TextView gameNextStageLevelTextview;
        private TextView gameNextStageGoalTextview;
        private View backgroundView;
        private View rootView;
        private Random random = new Random(System.currentTimeMillis());

        ReadyScreen() {
            rootView = rootInflate(R.layout.layout_game_entry_point);
            backgroundView = rootView.findViewById(R.id.game_start_background);
            gameNextStageLevelTextview = rootView.findViewById(R.id.textview_level);
            gameNextStageGoalTextview = rootView.findViewById(R.id.textview_goal_score);
            ImageView gameStartNextStageButton = rootView.findViewById(R.id.game_start_stage_btn);
            gameStartNextStageButton.setOnClickListener(onPressStart);
            ImageView gameExitButton = rootView.findViewById(R.id.game_start_exit_btn);
            gameExitButton.setOnClickListener(onPressExit);
        }

        void setLabelAndChangeBackground(int stage, int score, int games) {
            int[] bgId = new int[]{
                    R.drawable.game_start_bg1,
                    R.drawable.game_start_bg2,
                    R.drawable.game_start_bg3,
                    R.drawable.game_start_bg4,
                    R.drawable.game_start_bg5,
                    R.drawable.game_start_bg6,
                    R.drawable.game_start_bg7
            };
            int randomNumber = random.nextInt(7);
            backgroundView.setBackgroundResource(bgId[randomNumber]);
            gameNextStageLevelTextview.setText("Level " + stage);
            gameNextStageGoalTextview.setText(score + "/" + games);
        }

        void setVisibility(int visibility) {
            rootView.setVisibility(visibility);
        }
    }

    private View rootInflate(@LayoutRes int layout) {
        View v = activity.getLayoutInflater().inflate(layout, rootView, false);
        rootView.addView(v);
        return v;
    }

    /**
     * 문제를 보임
     *
     * @param stage      현재 스테이지
     * @param curProblem 현 스테이지에서 푸는 문제 번째
     * @param allProblem 현 스테이지의 총 문제 수
     */
    @Override
    public void setQuizInfo(int stage, int curProblem, int curScore, int allProblem) {
        gameStageTextView.setText("STAGE " + stage);
        gameScoreTextView.setText("SCORE " + curScore);
        gameTargetTextView.setText("TARGET " + (curProblem + 1) + "/" + allProblem);
        answerDistanceTextView.setVisibility(View.INVISIBLE);
        answerLayout.setVisibility(View.INVISIBLE);
        answerLayout.setClickable(false);
        myMapView.getController().setCenter(new GeoPoint(70f, 40f));
        myMapView.getController().setZoom(myMapView.getMinZoomLevel());

        hideDrawingOverlays();

        hideAllStateViews();
        quizView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showQuiz(IGameQuiz quiz) {
        if (quiz instanceof PositionQuiz) {
            showPositionQuiz((PositionQuiz) quiz);
        } else {
            showPictureQuiz((PicChoiceQuiz) quiz);
        }
    }

    /**
     * 사진을 보여주고 지명을 찾는 문제 형식
     *
     * @param pq 퀴즈 정보 객체
     */
    private void showPositionQuiz(PositionQuiz pq) {
        //레이아웃 설정
        positionPicImageView.setClickable(true);
        positionPicImageView.setVisibility(View.VISIBLE);
        choicePicProblemLayout.setClickable(false);
        choicePicProblemLayout.setVisibility(View.INVISIBLE);

//        GlideApp.with(activity).load(pq.getPicture()).into(positionPicImageView);
        positionPicImageView.setImageDrawable(pq.getGlideTargets().get(0).getDrawable());
        positionProblemLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 지명을 보여주고 사진을 찾는 문제 형식
     *
     * @param pq 퀴즈 정보 객체
     */
    private void showPictureQuiz(PicChoiceQuiz pq) {
        choicePics = pq.getPictures();

        //레이아웃 설정
        choicePicProblemLayout.setVisibility(View.VISIBLE);
        choicePicProblemLayout.setClickable(true);
        positionPicImageView.setClickable(false);
        positionPicImageView.setVisibility(View.INVISIBLE);

        //마커에 지명 설정하고 맵뷰에 표시
        systemMarker.showInfoWindow();
        systemMarker.setEnabled(true);

        myMapView.setClickable(false);

        for (int i = 0; i < 4; i++) {
            Drawable drawable = pq.getGlideTargets().get(i).getDrawable();
            choicePicImageViews[i].setImageDrawable(drawable);
        }
    }

    private void hideDrawingOverlays() {
        circleAnimation.setEnabled(false);
        dotLineAnimation.setEnabled(false);
        systemMarker.closeInfoWindow();
        systemMarker.setEnabled(false);
        userMarker.setEnabled(false);

        gameTimeProgressBar.setProgress(gameTimeProgressBar.getMax());
    }

    @Override
    public Marker getUserMarker() {
        return userMarker;
    }

    @Override
    public Marker getSystemMarker() {
        return systemMarker;
    }

    @Override
    public void setScore(int score) {
        gameScoreTextView.setText("SCORE " + score);
    }

    @Override
    public void startTimer(int msDuration) {
        timer = new TimerProgressBar(msDuration);
    }

    @Override
    public void stopTimer() {
        if (timer != null) {
            handler.removeCallbacks(timer);
            timer = null;
        }
    }

    class TimerProgressBar implements Runnable {
        long msDeadline;

        TimerProgressBar(int msDuration) {
            gameTimeProgressBar.setMax(msDuration);
            msDeadline = new Date().getTime() + msDuration;
            postIfNotNull(this, 0);
        }

        @Override
        public void run() {
            long timeLeft = msDeadline - new Date().getTime();
            gameTimeProgressBar.setProgress((int) timeLeft);

            if (timeLeft > 0) {
                // 30fps
                postIfNotNull(this, 1000 / 30);
            } else {
                input.onTimeout();
                // ontimeout
            }
        }
    }

    @Override
    public void exitGame() {
        handler.removeCallbacksAndMessages(null);
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
    }

    @Override
    public void showAnswer(IGameQuiz quiz, int deltaScore) {
        answerScoreTextView.setText("score " + deltaScore);
        if (quiz instanceof PositionQuiz) {
            showPositionAnswer((PositionQuiz) quiz);
        } else if (quiz instanceof PicChoiceQuiz) {
            showPicChoiceAnswer((PicChoiceQuiz) quiz);
        }
    }

    private void showAnswerCommon(Drawable pic, String address) {
        mapInvalidator.postInvalidate();
        answerLayout.setVisibility(View.VISIBLE);
        answerLayout.setClickable(true);
        divideAndShowAddress(address == null ? "" : address);
        answerCorrectImageView.setImageDrawable(pic);
    }

    private void divideAndShowAddress(@NonNull String string) {
        int start = 0;
        int end = string.length();
        int firstSpace = string.indexOf(" ");
        if (firstSpace == -1) {
            answerLandPlaceNameTextView.setText(string);
        } else {
            answerLandCountryTextView.setText(string.substring(start, firstSpace));
            answerLandPlaceNameTextView.setText(string.substring(firstSpace + 1, end));
        }
    }

    private void showPicChoiceAnswer(@NonNull PicChoiceQuiz quiz) {
        clearSelectedRectangle();

        Drawable correct = quiz.getGlideTargets().get(quiz.getCorrectIdx()).getDrawable();
        String address = quiz.getCorrectPicture().getMeta().getAddress();
        showAnswerCommon(correct, address);

        choicePicProblemLayout.setVisibility(View.INVISIBLE);
        //인자의 속도에 맞춰서 줌 아웃
        myMapView.getController().setCenter(new GeoPoint(70f, 40f));
        myMapView.getController().zoomTo(myMapView.getMinZoomLevel(), 1000L);
    }

    private void showPositionAnswer(@NonNull PositionQuiz quiz) {
        systemMarker.setOnMarkerClickListener(onMarkerClickDoingNothing);
        userMarker.setOnMarkerClickListener(onMarkerClickDoingNothing);

        Drawable pic = quiz.getGlideTargets().get(0).getDrawable();
        String address = quiz.getPicture().getMeta().getAddress();
        showAnswerCommon(pic, address);

        positionProblemLayout.setVisibility(View.INVISIBLE);
        myMapView.getController().setCenter(new GeoPoint(70f, 40f));
        myMapView.getController().zoomTo(myMapView.getMinZoomLevel(), 1000L);

        double distance = quiz.getKmFrom(systemMarker);
        if (distance == Double.POSITIVE_INFINITY) {
            answerDistanceTextView.setVisibility(View.INVISIBLE);
        } else {
            answerDistanceTextView.setVisibility(View.VISIBLE);
            String d = String.format(Locale.KOREAN, "%.1f km", distance);
            answerDistanceTextView.setText(d);
        }
    }

    @Override
    public void animateMarker(Marker marker, final GeoPoint destination, GeoPointInterpolator geopolator) {
        final GeoPoint start = marker.getPosition();
        myMapView.getController().animateTo(new GeoPoint(70f, 40f), myMapView.getMinZoomLevel(), 1000L);

        MarkerAnimation anim = new MarkerAnimation(marker, destination, 1000);
        anim.geoInterpolator = geopolator;
        anim.afterRun = new Runnable() {
            @Override
            public void run() {
                dotLineAnimation.reset(start, destination);
                dotLineAnimation.setEnabled(true);
                handler.post(fps30Forever);
            }
        };
        handler.post(anim);

        circleAnimation.resetCircle(start, destination);
        circleAnimation.setEnabled(true);
    }

    @Override
    public void pointMarker(Marker marker, GeoPoint pt) {
        Projection proj = myMapView.getProjection();
        Point disp = proj.toPixels(pt, null);
        GeoPoint start = (GeoPoint) proj.fromPixels(disp.x, disp.y - 50);
        marker.setPosition(start);
        MarkerAnimation anim = new MarkerAnimation(marker, pt, 250);
        anim.setTimeInterpolator(new AccelerateInterpolator(0.3f));
        handler.post(anim);
    }

    private Runnable fps30Forever = new Runnable() {
        @Override
        public void run() {
            mapInvalidator.postInvalidate();
            postIfNotNull(this, 1000 / 30);
        }
    };

    class MarkerAnimation implements Runnable {
        Marker marker;
        long msStart;
        long msDuration;
        GeoPoint startPosition;
        GeoPoint finalPosition;
        private Interpolator timeInterpolator = new AccelerateDecelerateInterpolator();
        GeoPointInterpolator geoInterpolator = new GeoPointInterpolator.Linear();
        Runnable afterRun;

        MarkerAnimation(Marker marker, GeoPoint finalPosition, int msDuration) {
            this.marker = marker;
            this.msDuration = msDuration;
            this.startPosition = marker.getPosition();
            this.finalPosition = finalPosition;
            msStart = SystemClock.uptimeMillis();
            marker.setEnabled(true);
        }

        void setTimeInterpolator(Interpolator timeInterpolator) {
            this.timeInterpolator = timeInterpolator;
        }

        @Override
        public void run() {
            long elapsed = SystemClock.uptimeMillis() - msStart;
            float t = (float) elapsed / msDuration;

            if (t < 1) {
                // 보간법 이용, 시작 위치에서 끝 위치까지 가는 구 모양의 경로 도출
                float v = timeInterpolator.getInterpolation(t);
                marker.setPosition(geoInterpolator.interpolate(v, startPosition, finalPosition));
                mapInvalidator.postInvalidate();

                postIfNotNull(this, 1000 / 60);
            } else if (afterRun != null) {
                afterRun.run();
            }
        }
    }

    private void postIfNotNull(Runnable r, long msDelay) {
        if (handler != null) {
            handler.postDelayed(r, msDelay);
        }
    }

    private View.OnClickListener onPressStart = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            input.onPressStart();
        }
    };

    private View.OnClickListener onPressExit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            input.onPressExit();
        }
    };

    private View.OnClickListener onPressRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            input.onPressRetry();
        }
    };

    private View.OnClickListener onPressPicZoom = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageView imgv = (ImageView) v;
            PictureDialogFragment pdf = PictureDialogFragment.Companion.newInstance(imgv.getDrawable());
            pdf.show(activity.getSupportFragmentManager(), "wow");
        }
    };

    private MapEventsOverlay onTouchMap = new MapEventsOverlay(new MapEventsReceiver() {
        @Override
        public boolean singleTapConfirmedHelper(GeoPoint p) {
            input.onTouchMap(p);
            return true;
        }

        @Override
        public boolean longPressHelper(GeoPoint p) {
            return false;
        }
    });

    /**
     * 정답화면에서 마커 클릭시 infowindow 띄우는 것 방지 하는 리스너
     */
    private Marker.OnMarkerClickListener onMarkerClickDoingNothing = new Marker.OnMarkerClickListener() {
        @Contract(pure = true)
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            return false;
        }
    };


    private View.OnClickListener onPressNext = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            input.onPressNext();
        }
    };

    private View lastSelect;


    private void clearSelectedRectangle() {
        if (lastSelect == null) {
            return;
        }
        lastSelect.getOverlay().clear();
        lastSelect.setOnClickListener(onPressPicFirst);
        lastSelect = null;
    }

    /**
     * 예비 선택을 지우고 테두리 없는 상태로 바꿈
     */
    private void clearLastSelectIfExists() {
        if (lastSelect == null) {
            return;
        }
        lastSelect.getOverlay().clear();
        lastSelect.setOnClickListener(onPressPicFirst);
    }

    private View.OnClickListener onPressPicFirst = new View.OnClickListener() {
        @Override
        public void onClick(@NonNull View v) {
            clearLastSelectIfExists();
            redRect.setBounds(new Rect(0, 0, v.getWidth(), v.getHeight()));
            v.getOverlay().add(redRect);
            v.setOnClickListener(onPressPicSecond);
            lastSelect = v;

            ImageView iv = (ImageView) v;
            int idx = Arrays.asList(choicePicImageViews).indexOf(iv);
            input.onSelectPictureFirst(choicePics.get(idx));
        }
    };

    private View.OnClickListener onPressPicSecond = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageView iv = (ImageView) v;
            int idx = Arrays.asList(choicePicImageViews).indexOf(iv);
            input.onSelectPictureCertainly(choicePics.get(idx));
            clearLastSelectIfExists();
            lastSelect = null;

        }
    };


    /**
     * 게임이 완료된 후 사진들을 리사이클러뷰로 보여줌
     */
    @Override
    public void showGameOver(List<IPicture> pics) {
        handler.removeCallbacksAndMessages(null);

        hideDrawingOverlays();
        hideAllStateViews();
        recollectionView.setVisibility(View.VISIBLE);

        ImageView afterGameExitButton = activity.findViewById(R.id.after_game_exit_button);
        ImageView afterGameRetryButton = activity.findViewById(R.id.after_game_retry_button);
        afterGameExitButton.setOnClickListener(onPressExit);
        afterGameRetryButton.setOnClickListener(onPressRetry);

        RecyclerView recyclerView = activity.findViewById(R.id.after_game_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        AfterGameAdapter adapter = new AfterGameAdapter(activity, pics);
        recyclerView.setAdapter(adapter);
    }
}
