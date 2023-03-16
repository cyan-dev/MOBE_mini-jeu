package com.ut3.mobe_mini_jeu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Random;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NavigationGame extends AppCompatActivity implements SensorEventListener {
    /*
     * This enum gives the 8 directions the boat and treasure can have
     * These are the 8 cardinal points
     */
    enum Direction {
        N, NO, O, SO, S, SE, E, NE;

        static Direction rand() {
            return Direction.values()[(new Random()).nextInt(8)];
        }
    }

    private class Boat {
        final static int LOW_SPEED = 6;
        final static int HIGH_SPEED = 18;

        private Direction dir;
        private int speed; // en m/s
        private int momentum;

        Boat(Direction dir, int speed) {
            this.dir = dir;
            this.speed = speed;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
            if(speed == Boat.HIGH_SPEED) {
                setMomentum(35);
            }
        }

        public Direction getDir() {
            return dir;
        }

        public int getMomentum() {
            return this.momentum;
        }

        public void setMomentum(int momentum) {
            this.momentum = momentum;
        }
    }

    private class Treasure {
        final static int MIN_DIST = 1000;
        final static int MAX_DIST = 1500;

        private Direction dir;
        private int dist;

        Treasure() {
            generate();
        }

        public Direction getDir() {
            return dir;
        }

        public int getDist() {
            return dist;
        }

        public void setDist(int dist) {
            this.dist = dist;
        }

        public void generate() {
            this.dir = Direction.rand();
            this.dist = (new Random()).nextInt(Treasure.MAX_DIST - Treasure.MIN_DIST) + Treasure.MIN_DIST;
            points = this.dist;
        }
    }

    private class SoundMeter {
        private int audioBufferMinSize;
        private AudioRecord audioRecord = null;

        public void start(Context context) {
            audioBufferMinSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, audioBufferMinSize);
                audioRecord.startRecording();
            }else{
                Intent intent = new Intent(context, MainMenu.class);
                startActivity(intent);
            }
        }

        public void stop() {
            if (audioRecord != null) {
                audioRecord.stop();
            }
        }

        public boolean isLouderThan(int soundLevel) {
            short[] buffer = new short[audioBufferMinSize];
            audioRecord.read(buffer, 0, audioBufferMinSize);

            for (short sample : buffer) {
                if (20 * Math.log10(Math.abs(sample) / 2700.0) > soundLevel) {
                    return true;
                }
            }
            return false;
        }
    }

    // Constant -------------------------------------------------------------------------------------
    private static final int RANDOM_BEFORE_A_FIGHT = 1000;
    private static final int COMPASS_REFRESHING_TIME = 100;
    private static final int SOUND_LEVEL_TO_TURBO = 18;

    //Views
    private TextView cardinalTopLabel, cardinalLeftLabel, cardinalRightLabel;
    private ImageView pointerTopImage, pointerLeftImage, pointerRightLabel;
    private ImageView compassButton;

    // Variables
    private int points = 0;
    private int randomIndex = 0;
    private int measuredDirection = 0;
    private boolean pressure = false;
    private boolean isFighting = false;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];


    //Other
    private SoundMeter soundMeter;
    private SensorManager sensorManager = null;
    private MediaPlayer coinSoundPlayer;
    private Handler handler = new Handler();

    //Runnable to change the direction of the boat
    private Runnable compassRunnable;

    //Game -----------------------------------------------------------------------------------------

    private void game(View shipView, Boat boat, Treasure treasure) {
        updateCursor(boat, treasure);
        updateCardinalLabels(boat);

        isFighting = updateEnemyFight();
        System.out.println(randomIndex);
        if (!isFighting) {
            updateTreasureDist(boat, treasure);
            if (boat.getMomentum() <= 10) {
                updateBoatSpeed(shipView, boat);
            } else {boat.setMomentum(boat.getMomentum()-1);}
            handler.postDelayed(new Runnable() {
                @Override
                public void run() { game(shipView, boat, treasure); }
            }, 50);
        }else{
            soundMeter.stop();
        }
    }

    private void updateBoatSpeed(View shipView, Boat boat){
        if(soundMeter.isLouderThan(SOUND_LEVEL_TO_TURBO)){
            boat.setSpeed(Boat.HIGH_SPEED);
            ((ImageView) shipView).setImageResource(R.drawable.ship1_turbo);
        }else{
            boat.setSpeed(Boat.LOW_SPEED);
            ((ImageView) shipView).setImageResource(R.drawable.ship1_fast);
        }
    }

    private void updateCursor(Boat boat, Treasure treasure) {
        pointerTopImage.setVisibility(View.INVISIBLE);
        pointerLeftImage.setVisibility(View.INVISIBLE);
        pointerRightLabel.setVisibility(View.INVISIBLE);

        int boatDirection = Direction.valueOf(boat.dir.toString()).ordinal();
        int treasureDirection = Direction.valueOf(treasure.dir.toString()).ordinal();

        // If treasure is in front
        if (boat.dir == treasure.dir){
            pointerTopImage.setVisibility(View.VISIBLE);
        }
        // If treasure is on the left
        else if ((boatDirection + 1 ) % 8 == treasureDirection ||
                (boatDirection + 2 ) % 8 == treasureDirection ||
                (boatDirection + 3 ) % 8 == treasureDirection) {
            pointerLeftImage.setVisibility(View.VISIBLE);
        }
        // Else, treasure in on the right or behind
        // So we choose to show on the right
        else {
            pointerRightLabel.setVisibility(View.VISIBLE);
        }
    }

    private boolean updateEnemyFight() {
        int random = new Random().nextInt(RANDOM_BEFORE_A_FIGHT - randomIndex);
        boolean isFight = false;

        if (random == 0) {
            isFight = true;
            Intent intent = new Intent(this, GameFight.class);
            startActivity(intent);
        } else {
            randomIndex++;
        }

        return isFight;
    }

    private void updateScore() {
        // get pref
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        int score = prefs.getInt("score", 0);

        // update score
        score += points;

        // set pref
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("score", score);
        editor.apply();

        // update view
        TextView scoreLabel = (TextView) findViewById(R.id.scoreLabel);
        scoreLabel.setText(Integer.toString(score));
    }

    private void updateLifePoints() {
        // get pref
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        int lifePoints = prefs.getInt("lifePoints", 100);

        // updateView
        ProgressBar lifePointsBar = findViewById(R.id.lifePointsBar);
        lifePointsBar.setProgress(lifePoints);
    }

    private void updateTreasureDist(Boat boat, Treasure treasure) {
        if (boat.getDir() == treasure.getDir()) {
            treasure.setDist(treasure.getDist() - boat.getSpeed());
        }

        if (treasure.getDist() < 0) {
            coinSoundPlayer.start();
            updateScore();
            treasure.generate();
        }
        View treasureView = findViewById(R.id.treasureView);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) treasureView.getLayoutParams();
        params.bottomMargin = treasure.getDist();
        treasureView.setLayoutParams(params);
    }

    //Compass --------------------------------------------------------------------------------------
    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensor = event.sensor.getType();
        float [] values = event.values;

        synchronized (this){
            //Accelerometer
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, accelerometerReading,
                        0, accelerometerReading.length);
                //Magnetic field
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);

            }
        }

        updateOrientationAngles();

        int orientationAnglesToInt = (int) Math.toDegrees(orientationAngles[0])+360;

        //We split the angle between 8 Directions
        int x = orientationAnglesToInt%360;

        //Hysteresis
        int angleInfBoundary = (x + 345)%360;
        int angleSupBoundary = (x + 375)%360;

        if(angleInfBoundary != measuredDirection || angleSupBoundary != measuredDirection) {
            //Smooth value and reverse orientation number
            measuredDirection = (8 - x / 45) % 8;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener((SensorEventListener) this);
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        // "orientationAngles" now has up-to-date information.
    }

    //Initialisation -------------------------------------------------------------------------------

    private void initializeHUD() {
        updateScore();
        updateLifePoints();
        setUpViews();
    }

    private void initializeSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (!prefs.contains("score")) {
            editor.putInt("score", 0);
            editor.apply();
        }
        if (!prefs.contains("lifePoints")) {
            editor.putInt("lifePoints", 100);
            editor.apply();
        }
    }

    private void setUpCompassButton(){
        compassButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    pressure = true;
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    pressure = false;
                }
                return pressure;
            }
        });
    }

    /**
     * Update direction labels on the display
     * if a pressure is detected on the compass button
     *
     * @param boat boat that will give its direction
     */
    private void updateCardinalLabels(Boat boat){
        if(pressure){
            int currentDirection = 8 + measuredDirection;
            boat.dir = Direction.values()[currentDirection % 8];
            cardinalTopLabel.setText(boat.dir.toString());
            cardinalLeftLabel.setText(Direction.values()[(currentDirection + 1) % 8].toString());
            cardinalRightLabel.setText(Direction.values()[(currentDirection - 1) % 8].toString());
        }
    }

    /**
     * Get all useful views from the layouts
     */
    private void setUpViews(){
        //TextView
        cardinalTopLabel = (TextView)  this.findViewById(R.id.textTop);
        cardinalLeftLabel = (TextView)  this.findViewById(R.id.textLeft);
        cardinalRightLabel = (TextView)  this.findViewById(R.id.textRight);

        //ImageView
        pointerTopImage = (ImageView)  this.findViewById(R.id.cursorTop);
        pointerLeftImage = (ImageView)  this.findViewById(R.id.cursorLeft);
        pointerRightLabel = (ImageView)  this.findViewById(R.id.cursorRight);
        compassButton = (ImageView) this.findViewById(R.id.compass);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_game);

        initializeSharedPreferences();
        initializeHUD();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        setUpCompassButton();

        Boat boat = new Boat(Direction.rand(), Boat.LOW_SPEED);
        Treasure treasure = new Treasure();
        coinSoundPlayer = MediaPlayer.create(this, R.raw.coin);

        updateCardinalLabels(boat);

        soundMeter = new SoundMeter();
        soundMeter.start(this);

        // En boucle
        View shipView = findViewById(R.id.shipView);
        game(shipView, boat, treasure);
        handler.postDelayed(compassRunnable, 1000);
    }
}