package kr.ac.kw.coms.globealbum.game;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class GameActivity extends AppCompatActivity {
    GameUI gui;
    GameLogic logic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gui = new GameUI(this);
        logic = new GameLogic(gui, this);
        logic.initiateGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logic.releaseResources();
    }
}