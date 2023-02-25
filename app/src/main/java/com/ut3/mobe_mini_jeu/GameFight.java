package com.ut3.mobe_mini_jeu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

public class GameFight extends AppCompatActivity {
    ProgressBar lifePoint;
    ProgressBar lifePointEnnemy;
    ProgressBar shotBar;
    int LifePointEnemy;
    int progress = 0;
    boolean pressure;

    private Handler handler = new Handler();

    private Runnable launch = new Runnable() {
        @Override
        public void run() {
            Log.d("Pression", "run !");
            if(pressure) {
                shotBar.setProgress(++progress);
            }else{
                progress = 0;
                shotBar.setProgress(progress);
            }

            handler.postDelayed(launch, 10);
        }
    };

    private void setUpShotBar(){
        shotBar = (ProgressBar) this.findViewById(R.id.shotProgressBar);
        shotBar.setProgress(0);
        shotBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d("Pression", "onTouch: " + motionEvent.getAction());
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    pressure = true;
                    return true;
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    pressure = false;
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_fight);

        lifePoint = (ProgressBar) this.findViewById(R.id.hpBar);
        lifePoint.setProgress(100);

        lifePointEnnemy = (ProgressBar) this.findViewById(R.id.hpBarEnnemy);
        lifePointEnnemy.setProgress(100);

        setUpShotBar();

        handler.postDelayed(launch, 1000);
    }
}