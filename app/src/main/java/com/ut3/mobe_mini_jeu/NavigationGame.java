package com.ut3.mobe_mini_jeu;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NavigationGame extends AppCompatActivity implements SensorEventListener {
    //Inner Class ----------------------------------------------------------------------------------
    enum Direction {
        N, S, E, O, NE, NO, SE, SO;

        static Direction rand() {
            return Direction.values()[(new Random()).nextInt(8)];
        }

        public String toString(){
            switch (this){
                case N:
                    return "N";
                case S:
                    return "S";
                case E:
                    return "E";
                case O:
                    return "O";
                case NE:
                    return "NE";
                case NO:
                    return "NO";
                case SE:
                    return "SE";
                case SO:
                    return "SO";
                default:
                    return "NO DIRECTION";
            }
        }

        public int getValue(){
            switch (this){
                case N:
                    return 0;
                case S:
                    return 4;
                case E:
                    return 6;
                case O:
                    return 2;
                case NE:
                    return 7;
                case NO:
                    return 1;
                case SE:
                    return 5;
                case SO:
                    return 3;
                default:
                    return -1;
            }
        }
    }

    private class Boat {
        final static int LOW_SPEED=6;
        final static int HIGH_SPEED=10;

        private Direction dir;
        private int speed; // en m/s

        Boat(Direction dir, int speed) {
            this.dir = dir;
            this.speed = speed;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public Direction getDir() {
            return dir;
        }

        public void setDir(Direction dir) {
            this.dir = dir;
        }
    }

    private class Treasure {
        final static int MIN_DIST=1000;
        final static int MAX_DIST=1500;

        private Direction dir;
        private int dist;

        Treasure() {
            generate();
        }

        public Direction getDir() {
            return dir;
        }

        public void setDir(Direction dir) {
            this.dir = dir;
        }

        public int getDist() {
            return dist;
        }

        public void setDist(int dist) {
            this.dist = dist;
        }

        public void generate() {
            this.dir = Direction.rand();
            this.dist = (new Random()).nextInt(Treasure.MAX_DIST- Treasure.MIN_DIST)+ Treasure.MIN_DIST;
            points = this.dist;
        }
    }

    //Variables ------------------------------------------------------------------------------------

    private int lifePoints = 100;
    private int points = 0;
    private int randomIndex = 0;
    private boolean pressure = false;
    private boolean isFighting = false;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];


    //Views

    private TextView textTop;
    private TextView textLeft;
    private TextView textRight;
    private ImageView cursorTop;
    private ImageView cursorLeft;
    private ImageView cursorRight;
    private ImageView compass;

    //Other

    private SensorManager sensorManager = null;

    private MediaPlayer coinSoundPlayer;

    private Handler handler = new Handler();

    //Runnable to change the direction of the boat
    private Runnable compassRunnable;

    //Game -----------------------------------------------------------------------------------------

    private void game(View shipView, Boat boat, Treasure treasure) {
        updateCursor(boat, treasure);

        isFighting = updateEnemyFight();
        System.out.println(randomIndex);
        if (!isFighting) {
            updateTreasureDist(boat, treasure);
            shipView.postDelayed(new Runnable() {
                @Override
                public void run() { game(shipView, boat, treasure); }
            }, 10);
        }
    }

    private void updateCursor(Boat boat, Treasure treasure) {
        cursorTop.setVisibility(View.INVISIBLE);
        cursorLeft.setVisibility(View.INVISIBLE);
        cursorRight.setVisibility(View.INVISIBLE);
        if(boat.dir.getValue() == treasure.dir.getValue()){
            Log.d("Cursor", "boat dir: " + boat.dir + " treasure dir: " + treasure.dir + " DEVANT");
            cursorTop.setVisibility(View.VISIBLE);
        }else if((boat.dir.getValue()+1)%8 == treasure.dir.getValue() ||
                (boat.dir.getValue()+2)%8 == treasure.dir.getValue() ||
                (boat.dir.getValue()+3)%8 == treasure.dir.getValue()
        ){
            Log.d("Cursor", "boat dir: " + boat.dir + " treasure dir: " + treasure.dir + " GAUCHE");
            cursorLeft.setVisibility(View.VISIBLE);
        }else{
            Log.d("Cursor", "boat dir: " + boat.dir + " treasure dir: " + treasure.dir + " DROITE");
            cursorRight.setVisibility(View.VISIBLE);
        }
    }

    private boolean updateEnemyFight() {
        int random = new Random().nextInt(1000 - randomIndex);
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
        //Log.d("MOBE", "SensorChanged");
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

        int xDeg = (int) (Math.toDegrees(orientationAngles[0])+360)%360;
        int yDeg = (int)(Math.toDegrees(orientationAngles[1])+360)%360;
        int zDeg = (int)(Math.toDegrees(orientationAngles[2])+360)%360;

        //Smooth values
        xDeg /= 45;
        yDeg /= 45;
        zDeg /= 45;

        Log.d("Compass", "onSensorChanged: x = " + xDeg + " y = " + yDeg + " z = " + zDeg);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
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

    private void setUpCompass(){
        compass.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d("Pression", "onTouch: " + motionEvent.getAction());
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    pressure = true;
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    pressure = false;
                }
                return pressure;
            }
        });
    }

    private void setUpCompassRunnable(){
        compassRunnable =  new Runnable() {
            @Override
            public void run() {

                if (!isFighting) {
                    handler.postDelayed(compassRunnable, 10);
                }
            }
        };
    }

    private void setUpViews(){
        //TextView
        textTop = (TextView)  this.findViewById(R.id.textTop);
        textLeft = (TextView)  this.findViewById(R.id.textLeft);
        textRight = (TextView)  this.findViewById(R.id.textRight);

        //ImageView
        cursorTop = (ImageView)  this.findViewById(R.id.cursorTop);
        cursorLeft = (ImageView)  this.findViewById(R.id.cursorLeft);
        cursorRight = (ImageView)  this.findViewById(R.id.cursorRight);
        compass = (ImageView) this.findViewById(R.id.compass);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_game);

        initializeSharedPreferences();
        initializeHUD();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        setUpCompass();
        setUpCompassRunnable();

        Boat boat = new Boat(Direction.rand(), Boat.LOW_SPEED);
        Treasure treasure = new Treasure();
        coinSoundPlayer = MediaPlayer.create(this, R.raw.coin);

        // En boucle
        View shipView = findViewById(R.id.shipView);
        game(shipView, boat, treasure);
        handler.postDelayed(compassRunnable, 1000);
    }
}