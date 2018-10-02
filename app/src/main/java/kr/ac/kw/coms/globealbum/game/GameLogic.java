package kr.ac.kw.coms.globealbum.game;

import android.os.Handler;

import org.osmdroid.util.GeoPoint;

import kr.ac.kw.coms.globealbum.provider.IPicture;

class GameLogic implements IGameInputHandler {
    private GameUI ui;

    private int problem = 0;
    private int score = 0;
    private int timeScore = 0;
    private int distance = 0;

    private Handler animateHandler = null;  //마커 이동시키는 핸들러
    private Handler drawDottedLineHandler = null;  // 점선 그리는 핸들러

    final int TIME_LIMIT_MS = 14000;

    GameLogic(GameUI ui) {
        this.ui = ui;
        ui.input = this;
    }

    void initiateGame() {
        ui.displayLoadingGif(true);
    }

    void onProblemReady() {
    }

    void enterPositionProblem() {
    }

    void enterPicChoiceProblem() {
    }

    void onProblemDone() {
    }

    void onNextProblem() {
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
