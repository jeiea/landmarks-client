package kr.ac.kw.coms.globealbum.game;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.List;
import java.util.Objects;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.common.PictureDialogFragment;
import kr.ac.kw.coms.globealbum.map.DrawCircleOverlay;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.IPicture;

import static kr.ac.kw.coms.globealbum.game.ga.TimerState.Running;

class GameUI {
    private AppCompatActivity activity;
    IGameInputHandler input;

    //game entry point
    private ImageView gameStartNextStageButton, gameExitButton;
    private TextView gameNextStageLevelTextview, gameNextStageGoalTextview;
    //game
    private MyMapView myMapView;
    private ProgressBar gameTimeProgressBar;
    private TextView gameStageTextView;
    private TextView gameScoreTextView;
    private TextView gameTargetTextView;
    private Drawable RED_MARKER_DRAWABLE;
    private Drawable BLUE_MARKER_DRAWABLE;

    DrawCircleOverlay drawCircleOverlay;
    DottedLineOverlay dottedLineOverlay;
    //answer
    private Button answerGoToNextStageButton;
    private Button answerExitButton;
    private TextView answerLandNameTextView, answerDistanceTextView, answerScoreTextView;
    private ImageView answerCorrectImageView;
    private ConstraintLayout answerLayout;
    //problem type
    private ConstraintLayout positionProblemLayout;
    private ConstraintLayout choicePicProblemLayout;
    private Drawable redRect;
    private ImageView positionPicImageView;
    private ImageView[] choicePicImageViews = new ImageView[4];

    //after game
    private RecyclerView recyclerView;
    private AfterGameAdapter adapter;

    GameUI(AppCompatActivity activity) {
        this.activity = activity;
        redRect = activity.getResources().getDrawable(R.drawable.rectangle_border, null);
    }

    void displayLoadingGif(boolean show) {
        activity.setContentView(R.layout.layout_game_loading_animation);
        ImageView imgLoading = activity.findViewById(R.id.imageview_game_start);
        imgLoading.setClickable(false);
        GlideApp.with(imgLoading).load(R.drawable.game_loading_gif).into(imgLoading);
    }


    void displayGameEntryPoint(int stage, int score, int games, View.OnClickListener startListener, View.OnClickListener finishListener) {
        activity.setContentView(R.layout.layout_game_entry_point);
        gameStartNextStageButton = activity.findViewById(R.id.game_start_stage_button);
        gameExitButton = activity.findViewById(R.id.game_exit_button);
        gameNextStageLevelTextview = activity.findViewById(R.id.textview_level);
        gameNextStageGoalTextview = activity.findViewById(R.id.textview_goal_score);
        gameNextStageLevelTextview.setText("Level " + stage);
        gameNextStageGoalTextview.setText(score + "/" + games);
        gameStartNextStageButton.setOnClickListener(startListener);
        gameExitButton.setOnClickListener(finishListener);
    }

    void displayQuiz(int stage, int problem, int games, View.OnClickListener nextStageListener, View.OnClickListener finishListener, View.OnClickListener pictureZoomingListener) {
        activity.setContentView(R.layout.activity_game);

        //game layout
        gameTimeProgressBar = activity.findViewById(R.id.progressbar);
        gameStageTextView = activity.findViewById(R.id.textview_stage);
        gameTargetTextView = activity.findViewById(R.id.textview_target);
        gameScoreTextView = activity.findViewById(R.id.textview_score);
        RED_MARKER_DRAWABLE = activity.getResources().getDrawable(R.drawable.game_red_marker);
        BLUE_MARKER_DRAWABLE = activity.getResources().getDrawable(R.drawable.game_blue_marker);

        gameStageTextView.setText("STAGE " + stage);
        gameTargetTextView.setText("TARGET " + (problem + 1) + "/" + games);

        //answer layout
        answerLayout = activity.findViewById(R.id.layout_answer);
        answerCorrectImageView = activity.findViewById(R.id.picture_answer);
        answerExitButton = activity.findViewById(R.id.button_exit);
        answerGoToNextStageButton = activity.findViewById(R.id.button_next);
        answerDistanceTextView = activity.findViewById(R.id.textview_land_distance_answer);
        answerLandNameTextView = activity.findViewById(R.id.textview_land_name_answer);
        answerScoreTextView = activity.findViewById(R.id.textview_land_score_answer);

        answerGoToNextStageButton.setOnClickListener(nextStageListener);
        answerExitButton.setOnClickListener(finishListener);

        //game type layout
        positionProblemLayout = activity.findViewById(R.id.position_problem);
        choicePicProblemLayout = activity.findViewById(R.id.choice_pic);
        positionPicImageView = activity.findViewById(R.id.picture);
        int[] imgViewIds = new int[]{R.id.picture1, R.id.picture2, R.id.picture3, R.id.picture4};
        for (int i = 0; i < imgViewIds.length; i++) {
            choicePicImageViews[i] = activity.findViewById(imgViewIds[i]);
            choicePicImageViews[i].setOnClickListener(new PictureClickZoomingListener());
        }

        positionPicImageView.setOnClickListener(pictureZoomingListener);

        myMapView = activity.findViewById(R.id.map);

        //마커 이벤트 등록
        markerClickListenerOverlay = markerEvent();
        myMapView.getOverlays().add(markerClickListenerOverlay);


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
        if (strColor == "red") {
            marker.setIcon(RED_MARKER_DRAWABLE);
        } else if (strColor == "blue") {
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
        if (strColor == "red") {
            marker.setIcon(RED_MARKER_DRAWABLE);
        } else if (strColor == "blue") {
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

    void addOverlay(Overlay overlay){
        myMapView.getOverlays().add(overlay);
    }

    void mapviewInvalidate(){
        myMapView.invalidate();
    }
    void setGameTimeProgressBarMAx(int max){
        gameTimeProgressBar.setMax(max);
    }
    void setGameTimeProgressBarProgress(int timeLeft){
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

        gameTargetTextView.setText("TARGET " + (problem + 1) + "/" + games));
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

                    onProblemDone();


                    animateHandler = null;

                    map.invalidate();
                }
            }
        });
    }

    /**
     * 문제 사진 클릭 시 다이얼로그로 크게 띄워주는 리스너
     */
    class PictureClickZoomingListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {    //이미지뷰를 다이얼로그로 화면에 표시
            ImageView imgv = (ImageView) view;
            PictureDialogFragment pdf = PictureDialogFragment.Companion.newInstance(imgv.getDrawable());
            pdf.show(activity.getSupportFragmentManager(), "wow");
        }
    }


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
    void setRecyclerView(List<IPicture> pics) {
        activity.setContentView(R.layout.layout_recycler_view);
        recyclerView = activity.findViewById(R.id.after_game_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AfterGameAdapter(activity, pics);
        recyclerView.setAdapter(adapter);
    }

    void displaySolvedPictures(List<IPicture> pics) {
    }

    void finishActivity() {
        activity.finish();
    }
}
