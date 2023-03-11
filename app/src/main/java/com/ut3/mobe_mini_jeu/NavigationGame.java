package com.ut3.mobe_mini_jeu;

import static com.ut3.mobe_mini_jeu.R.id.shipView;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

public class NavigationGame extends AppCompatActivity {
    enum Direction {
        N, S, E, W, NE, NW, SE, SW;

        static Direction rand() {
            return Direction.values()[(new Random()).nextInt(9)];
        }
    }

    private class Boat {
        final static int LOW_SPEED=6;
        final static int HIGH_SPEED=10;

        private Direction dir = Direction.N;
        private int speed = LOW_SPEED; // en m/s

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
            this.dir = Direction.N;
            this.dist = (new Random()).nextInt(Treasure.MAX_DIST- Treasure.MIN_DIST)+ Treasure.MIN_DIST;
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

        public void regenerate() {
            this.dir = Direction.N;
            this.dist = (new Random()).nextInt(Treasure.MAX_DIST- Treasure.MIN_DIST)+ Treasure.MIN_DIST;
        }
    }

    private int randomIndex = 0;
    MediaPlayer coinSoundPlayer = MediaPlayer.create(this, Uri.parse("/toto"));

    private void game(View shipView, Boat boat, Treasure treasure) {
        boolean isFight = updateEnemyFight();

        if (!isFight) {
            updateTreasureDist(boat, treasure);
            shipView.postDelayed(new Runnable() {
                @Override
                public void run() { game(shipView, boat, treasure); }
            }, 10);
        }
    }

    private boolean updateEnemyFight() {
        int random = new Random().nextInt(2000 - randomIndex);
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

    private void updateTreasureDist(Boat boat, Treasure treasure) {
        if (boat.getDir() == treasure.getDir()) {
            treasure.setDist(treasure.getDist() - boat.getSpeed());
        }

        if (treasure.getDist() < - 10) {
            coinSoundPlayer.start();
            treasure.regenerate();
        }
        View treasureView = findViewById(R.id.treasureView);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) treasureView.getLayoutParams();
        params.bottomMargin = treasure.getDist();
        treasureView.setLayoutParams(params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_game);

        Boat boat = new Boat(Direction.N, Boat.LOW_SPEED);
        Treasure treasure = new Treasure();

        // En boucle
        View shipView = findViewById(R.id.shipView);
        game(shipView, boat, treasure);
    }
}