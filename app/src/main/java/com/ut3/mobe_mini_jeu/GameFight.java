package com.ut3.mobe_mini_jeu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GameFight extends AppCompatActivity {
    final int DELAY_BETWEEN_ANIMATION = 50;
    final int TIMER_BEFORE_RECEIVING_DAMAGE = 300;
    final int MIN_BEFORE_SHOT_AR_INCREASE = 0;

    private Vibrator vibrator;

    ImageView ship;
    ImageView enemyShip;

    ProgressBar lifePointBar;
    ProgressBar lifePointEnemyBar;
    ProgressBar shotBar;

    TextView scoreText;

    int lifePoint = 100;
    int lifePointEnemy = 100;

    //Progress of the shot progress bar
    int progress = 0;

    //Timer to decrease before receiving damage
    int timer = TIMER_BEFORE_RECEIVING_DAMAGE;

    int timerIdleCanon = DELAY_BETWEEN_ANIMATION;

    boolean pressure;
    boolean shipShot = false;

    //Boolean to choose if the shot progress bar must be release
    boolean pressureMustBeRelease = false;

    private Handler handler = new Handler();

    private Runnable launch = new Runnable() {
        @Override
        public void run() {

            pressureBeforeShooting();

            if(--timer == DELAY_BETWEEN_ANIMATION){
                enemyShip.setImageResource(R.drawable.ship2_canon_fire);
            //Decreasing the life point of the ship when the timer equal 0 and restart of the timer
            } else if(timer == 0){
                timer = TIMER_BEFORE_RECEIVING_DAMAGE;
                lifePoint -= 10;
                lifePointBar.setProgress(lifePoint);
                enemyShip.setImageResource(R.drawable.ship2_canon_idle);
            }

            //Removing the animation of the canon
            if(shipShot && --timerIdleCanon == 0){
                timerIdleCanon = DELAY_BETWEEN_ANIMATION;
                shipShot = false;
                ship.setImageResource(R.drawable.ship1_canon_idle);
            }

            handler.postDelayed(launch, 10);
        }
    };

    private void pressureBeforeShooting(){
        //If a pressure is detect increase the damage inflict
        if (pressure) {
            if (!pressureMustBeRelease) {
                progress += 2;
                if (progress == 100) {
                    pressureMustBeRelease = true;
                }
                //Decreasing the damage inflict because the pressure isn't release
            } else {
                progress -= 2;
                if (progress == MIN_BEFORE_SHOT_AR_INCREASE) {
                    pressureMustBeRelease = false;
                }
            }
            //Release detected
        } else if (progress > 0) {

            ship.setImageResource(R.drawable.ship1_canon_fire);
            shipShot = true;

            lifePointEnemy -= progress / 5;
            lifePointEnemyBar.setProgress(lifePointEnemy);

            //Vibration
            if(vibrator.hasVibrator()){
                vibrator.vibrate(1000);
            }

            //Update of the score
            int calculScore = Integer.parseInt(scoreText.getText().toString()) + progress;
            String scoreString = "" + calculScore;

            scoreText.setText(scoreString.toCharArray(), 0, scoreString.length());

            pressureMustBeRelease = false;
            progress = 0;

        }
        shotBar.setProgress(progress);
    }

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

        lifePointEnemyBar = (ProgressBar) this.findViewById(R.id.hpBarEnnemy);
        lifePointEnemyBar.setProgress(lifePointEnemy);

        scoreText = (TextView) this.findViewById(R.id.scoreText);
        scoreText.setText("0");

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        setUpShotBar();

        ship = (ImageView) this.findViewById(R.id.allyShip);
        enemyShip = (ImageView) this.findViewById(R.id.foeShip);

        handler.postDelayed(launch, 1000);
    }
}