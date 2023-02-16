package com.ut3.mobe_mini_jeu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.Random;

public class NavigationGame extends AppCompatActivity {
    public enum Direction {
        N, S, E, W, NE, NW, SE, SW;
    }

    private class Boat {
        private Direction dir = Direction.N;
        private double speed = 10.0; // en m/s

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
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
        private Direction dir;
        private double dist;

        public Treasure(Direction dir, double dist) {
            this.dir = dir;
            this.dist = dist;
        }

        public Direction getDir() {
            return dir;
        }

        public void setDir(Direction dir) {
            this.dir = dir;
        }

        public double getDist() {
            return dist;
        }

        public void setDist(double dist) {
            this.dist = dist;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigation_game);
    }
}