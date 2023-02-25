package com.ut3.mobe_mini_jeu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

public class GameFight extends AppCompatActivity {
    final int TIMER_BEFORE_RECEIVING_DAMAGE = 300;

    ProgressBar lifePointBar;
    ProgressBar lifePointEnnemyBar;
    ProgressBar shotBar;
    int lifePoint = 100;
    int lifePointEnnemy = 100;

    //Progress of the shot progress bar
    int progress = 0;

    //Timer to decrease before receiving damage
    int timer = TIMER_BEFORE_RECEIVING_DAMAGE;

    boolean pressure;

    //Boolean to choose if the shot progress bar must be release
    boolean pressureMustBeRelease = false;

    private Handler handler = new Handler();

    private Runnable launch = new Runnable() {
        @Override
        public void run() {
            //If a pressure is detect increase the damage inflict
            if(pressure) {
                if(!pressureMustBeRelease) {
                    progress++;
                    if(progress == 100){
                        pressureMustBeRelease = true;
                    }
                //Decreasing the damage inflict because the pressure isn't release
                }else{
                    progress--;
                    if(progress == 80){
                        pressureMustBeRelease = false;
                    }
                }
            //Release detected
            }else if(progress > 0) {

                lifePointEnnemy -= progress / 5;
                lifePointEnnemyBar.setProgress(lifePointEnnemy);

                pressureMustBeRelease = false;
                progress = 0;

            }
            shotBar.setProgress(progress);

            //Decreasing the life point of the ship when the timer equal 0 and restart of the timer
            if(--timer == 0){
                timer = TIMER_BEFORE_RECEIVING_DAMAGE;
                lifePoint -= 10;
                lifePointBar.setProgress(lifePoint);
            }

            handler.postDelayed(launch, 10);
        }
    };

    //Function to set up the shot progress bar
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
                }
                return false;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_fight);

        lifePointBar = (ProgressBar) this.findViewById(R.id.hpBar);
        lifePointBar.setProgress(lifePoint);

        lifePointEnnemyBar = (ProgressBar) this.findViewById(R.id.hpBarEnnemy);
        lifePointEnnemyBar.setProgress(lifePointEnnemy);

        setUpShotBar();

        handler.postDelayed(launch, 1000);
    }
}