package kr.ac.kw.coms.globealbum.game;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class GameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GameUI gui = new GameUI(this);
        GameLogic logic = new GameLogic(gui, this);
        gui.input = logic;
        logic.initiateGame();
    }
}