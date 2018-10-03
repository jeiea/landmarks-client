package kr.ac.kw.coms.globealbum.game;

import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;

import java.util.List;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.map.MyMapView;
import kr.ac.kw.coms.globealbum.provider.IPicture;

class GameUI {
    private AppCompatActivity activity;
    IGameInputHandler input;

    private Drawable redRect;
    private MyMapView myMapView;

    private ProgressBar progressBar;
    private TextView stageTextView;
    private TextView scoreTextView;
    private TextView targetTextView;

    private Button goToNextStageButton;
    private Button exitGameButton;
    private TextView landNameAnswerTextView, landDistanceAnswerTextView, landScoreTextView;
    private ConstraintLayout choicePicPtoblemLayout;
    private ConstraintLayout positionProblemLayout;
    private ConstraintLayout answerLayout;
    private ImageView positionPicImageView;
    private ImageView[] choicePicImageViews = new ImageView[4];
    private ImageView correctAnswerImageView;

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

    void placeMarkerAt(GeoPoint pt) {
    }

    void clearMarker() {
    }

    void bottomOnePicture(IPicture pic) {
    }

    void bottomFourPicture(List<IPicture> pics) {
    }

    void displaySolvedPictures(List<IPicture> pics) {
    }
}
