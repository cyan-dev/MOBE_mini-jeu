package com.ut3.mobe_mini_jeu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainMenu extends AppCompatActivity {

    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean mic_acces = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView bestScoreLabel = (TextView) this.findViewById(R.id.bestScoreLabel);
        TextView lastScoreLabel = (TextView) this.findViewById(R.id.lastScoreLabel);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        bestScoreLabel.setText("Best : " + prefs.getInt("bestScore", 0));
        lastScoreLabel.setText("Last : " + prefs.getInt("score", 0));


        if(!mic_acces) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mic_acces = requestCode == REQUEST_RECORD_AUDIO_PERMISSION;
    }

    public void startNavigationGameActivity(View view) {
        if(!mic_acces) return;

        SharedPreferences.Editor editor = getSharedPreferences("prefs", MODE_PRIVATE).edit();
        editor.putInt("score", 0);
        editor.putInt("lifePoints", 100);
        editor.apply();

        Intent intent = new Intent(this, NavigationGame.class);
        startActivity(intent);
    }
}