package kr.ac.kw.coms.globealbum.game;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.common.PictureDialogFragment;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.IPicture;


class GameUI {
    private AppCompatActivity activity;
    IGameInputHandler input;

    // screens
    private ReadyScreen readyScreen;
    private View quizView;

    // game
    private MyMapView myMapView;
    private ProgressBar gameTimeProgressBar;
    private TextView gameStageTextView;
    private TextView gameScoreTextView;
    private TextView gameTargetTextView;
    private Drawable RED_MARKER_DRAWABLE;
    private Drawable BLUE_MARKER_DRAWABLE;

    // answer
    private TextView answerLandNameTextView, answerDistanceTextView, answerScoreTextView;
    private ImageView answerCorrectImageView;
    private ConstraintLayout answerLayout;

    // problem type
    private ConstraintLayout positionProblemLayout;
    private ConstraintLayout choicePicProblemLayout;
    private Drawable redRect;
    private ImageView positionPicImageView;
    private ImageView[] choicePicImageViews = new ImageView[4];


    GameUI(AppCompatActivity activity) {
        this.activity = activity;
        readyScreen = new ReadyScreen();
        initQuiz();
    }

    void displayLoadingGif() {
        activity.setContentView(R.layout.layout_game_loading_animation);
        ImageView imgLoading = activity.findViewById(R.id.game_gif_loading);
        imgLoading.setClickable(false);
        GlideApp.with(imgLoading).load(R.drawable.game_loading_gif).into(imgLoading);
    }

    private void initQuiz() {
        quizView = rootInflate(R.layout.activity_game);

        //game layout
        gameTimeProgressBar = quizView.findViewById(R.id.progressbar);
        gameStageTextView = quizView.findViewById(R.id.textview_stage);
        gameTargetTextView = quizView.findViewById(R.id.textview_target);
        gameScoreTextView = quizView.findViewById(R.id.textview_score);
        redRect = activity.getResources().getDrawable(R.drawable.rectangle_border, null);
        RED_MARKER_DRAWABLE = quizView.getResources().getDrawable(R.drawable.game_red_marker, null);
        BLUE_MARKER_DRAWABLE = quizView.getResources().getDrawable(R.drawable.game_blue_marker, null);

        //answer layout
        answerLayout = quizView.findViewById(R.id.layout_answer);
        answerCorrectImageView = quizView.findViewById(R.id.picture_answer);
        answerDistanceTextView = quizView.findViewById(R.id.textview_land_distance_answer);
        answerLandNameTextView = quizView.findViewById(R.id.textview_land_name_answer);
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
        addOverlay(onPressMarker);
    }

    void displayGameEntryPoint(int stage, int score, int games) {
        readyScreen.setLabelAndChangeBackground(stage, score, games);
        activity.setContentView(readyScreen.getRootView());
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

        View getRootView() {
            return rootView;
        }
    }

    private View rootInflate(@LayoutRes int layout) {
        ViewGroup frame = (ViewGroup) activity.getWindow().getDecorView();
        return activity.getLayoutInflater().inflate(layout, frame, false);
    }

    void displayQuiz(int stage, int problem, int games) {
        gameStageTextView.setText("STAGE " + stage);
        gameTargetTextView.setText("TARGET " + (problem + 1) + "/" + games);
        activity.setContentView(quizView);
    }

    void displayAnswerLayout(GameLogic.GameType gameType, int score, int distance, IPicture pic, boolean isNull) {
        if (gameType == GameLogic.GameType.A) {
            positionProblemLayout.setVisibility(View.GONE);
        } else if (gameType == GameLogic.GameType.B) {
            choicePicProblemLayout.setVisibility(View.GONE);
            myMapView.getController().zoomTo(myMapView.getMinZoomLevel(), 1000L); //인자의 속도에 맞춰서 줌 아웃
        }

        answerLayout.setVisibility(View.VISIBLE);
        answerLayout.setClickable(true);
        answerLandNameTextView.setText(pic.getMeta().getAddress());
        if (gameType == GameLogic.GameType.A && !isNull) {
            answerDistanceTextView.setVisibility(View.VISIBLE);
            answerDistanceTextView.setText(distance + "KM");
        } else {
            answerDistanceTextView.setVisibility(View.INVISIBLE);
        }
        answerScoreTextView.setText("score " + score);
        GlideApp.with(activity).load(pic).into(answerCorrectImageView);

    }


    MyMapView getMyMapView() {
        return myMapView;
    }


    /**
     * 지명 찾기 문제에서의 마커 생성
     *
     * @param strColor 마커 색상
     * @param pt       위치
     * @return 생성된 마커
     */
    Marker makeMarker(String strColor, GeoPoint pt) {
        Marker marker = new Marker(myMapView);
        if (strColor.equals("red")) {
            marker.setIcon(RED_MARKER_DRAWABLE);
        } else if (strColor.equals("blue")) {
            marker.setIcon(BLUE_MARKER_DRAWABLE);
        }
        marker.setAnchor(0.5f, 1.0f);
        marker.setPosition(pt);
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                return false;
            }
        });
        return marker;
    }

    /**
     * 사진 고르기 문제에서의 마커 생성
     *
     * @param strColor  마커 색상
     * @param pt        위치
     * @param placeName infowindow에 띄워질 지명 이름
     * @return 생성된 마커
     */
    Marker makeMarker(String strColor, GeoPoint pt, String placeName) {
        Marker marker = new Marker(myMapView);
        if (strColor.equals("red")) {
            marker.setIcon(RED_MARKER_DRAWABLE);
        } else if (strColor.equals("blue")) {
            marker.setIcon(BLUE_MARKER_DRAWABLE);
        }
        marker.setAnchor(0.5f, 1.0f);
        marker.setPosition(pt);
        marker.setTitle(placeName);
        MarkerInfoWindow markerInfoWindow = new MarkerInfoWindow(R.layout.game_infowindow_bubble, myMapView);
        View v = markerInfoWindow.getView();
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        marker.setInfoWindow(markerInfoWindow);
        marker.showInfoWindow();

        myMapView.getOverlays().add(marker);
        return marker;
    }

    void addOverlay(Overlay overlay) {
        myMapView.getOverlays().add(overlay);
    }

    void setGameScoreTextView(int score) {
        gameScoreTextView.setText("SCORE " + score);
    }

    void mapviewInvalidate() {
        myMapView.invalidate();
    }

    void setGameTimeProgressBarMAx(int max) {
        gameTimeProgressBar.setMax(max);
    }

    void setGameTimeProgressBarProgress(int timeLeft) {
        gameTimeProgressBar.setProgress(timeLeft);
    }

    void clearOverlay(Overlay overlay) {
        if (overlay instanceof Marker) {
            InfoWindow.closeAllInfoWindowsOn(myMapView);
        }
        myMapView.getOverlays().remove(overlay);
    }

    void clearAnswerLayout(int problem, int games) {
        answerLayout.setVisibility(View.GONE);
        answerLayout.setClickable(false);

        gameTargetTextView.setText("TARGET " + (problem + 1) + "/" + games);
    }

    /**
     * 사용자가 정한 마커와 정답 마커 사이를 잇는 직선 생성
     *
     * @param startPosition 사용자 마커의 좌표
     * @param destPosition  정답 마커의 좌표
     */
    void addLine(GeoPoint startPosition, GeoPoint destPosition) {
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(startPosition);
        geoPoints.add(destPosition);

    }

    View.OnClickListener onPressStart = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            input.onPressStart();
        }
    };

    View.OnClickListener onPressExit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            input.onPressExit();
        }
    };

    View.OnClickListener onPressPicZoom = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageView imgv = (ImageView) v;
            PictureDialogFragment pdf = PictureDialogFragment.Companion.newInstance(imgv.getDrawable());
            pdf.show(activity.getSupportFragmentManager(), "wow");
        }
    };


    MapEventsOverlay onPressMarker = new MapEventsOverlay(new MapEventsReceiver() {
        @Override
        public boolean singleTapConfirmedHelper(GeoPoint p) {
            input.onPressMarker(myMapView, p);
            return true;
        }

        @Override
        public boolean longPressHelper(GeoPoint p) {
            return false;
        }
    });


    View.OnClickListener onPressNext = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            input.onPressNext();
        }
    };

    View lastSelect;


    /**
     * 예비 선택을 지우고 테두리 없는 상태로 바꿈
     */
    void clearLastSelectIfExists() {
        if (lastSelect == null) {
            return;
        }
        lastSelect.getOverlay().clear();
        lastSelect.setOnClickListener(onPressPicFirst);
    }

    View.OnClickListener onPressPicFirst = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            clearLastSelectIfExists();
            redRect.setBounds(new Rect(0, 0, v.getWidth(), v.getHeight()));
            v.getOverlay().add(redRect);
            v.setOnClickListener(onPressPicSecond);
            lastSelect = v;
            input.onSelectPicFirst(choicePicImageViews[0], v);

        }
    };

    View.OnClickListener onPressPicSecond = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            input.onSelectPicSecond(choicePicImageViews[0], v);
            clearLastSelectIfExists();
            lastSelect = null;

        }
    };


    /**
     * 사진을 보여주고 지명을 찾는 문제 형식
     *
     * @param pic 사진 정보를 가지고 있는 클래스
     */
    void bottomOnePicture(IPicture pic) {
        //레이아웃 설정
        positionPicImageView.setClickable(true);
        positionPicImageView.setVisibility(View.VISIBLE);
        choicePicProblemLayout.setClickable(false);
        choicePicProblemLayout.setVisibility(View.GONE);

        myMapView.getController().setZoom(myMapView.getMinZoomLevel());

        GlideApp.with(activity).load(pic).into(positionPicImageView);
        positionProblemLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 지명을 보여주고 사진을 찾는 문제 형식
     *
     * @param pics 사진 정보를 가지고 있는 클래스
     */
    void bottomFourPicture(List<IPicture> pics) {

        //레이아웃 설정
        choicePicProblemLayout.setVisibility(View.VISIBLE);
        choicePicProblemLayout.setClickable(true);
        positionPicImageView.setClickable(false);
        positionPicImageView.setVisibility(View.GONE);

        //마커에 지명 설정하고 맵뷰에 표시

        myMapView.getController().setZoom(myMapView.getMinZoomLevel());
        myMapView.setClickable(false);

        for (int i = 0; i < 4; i++) {
            choicePicImageViews[i].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            GlideApp.with(activity).load(pics.get(i)).into(choicePicImageViews[i]);
        }
    }


    /**
     * 게임이 완료된 후 사진들을 모아서 보여주는 리사이클뷰 적용
     */
    void showGameOver(List<IPicture> pics) {
        activity.setContentView(R.layout.layout_recycler_view);
        //after game
        RecyclerView recyclerView = activity.findViewById(R.id.after_game_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        AfterGameAdapter adapter = new AfterGameAdapter(activity, pics);
        recyclerView.setAdapter(adapter);
    }

    void displaySolvedPictures(List<IPicture> pics) {
    }

    void finishActivity() {
        activity.finish();
    }
}
