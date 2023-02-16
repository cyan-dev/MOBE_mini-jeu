package com.ut3.mobe_mini_jeu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GameFight extends AppCompatActivity implements View.OnTouchListener {
    ProgressBar LifePoint;
    int LifePointEnemy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_fight);

        ProgressBar LifePoint = (ProgressBar) this.findViewById(R.id.hpBar);
        LifePoint.setProgress(100);



    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        LifePoint.setProgress(LifePoint.getProgress() - 10);
        return true;
    }
}