package com.ut3.mobe_mini_jeu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startNavigationGameActivity(View view) {
        Intent intent = new Intent(this, NavigationGame.class);
        startActivity(intent);
    }

    public void startScoreBoardActivity(View view) {
        Intent intent = new Intent(this, ScoreBoard.class);
        startActivity(intent);
    }
}