package com.example.ccaro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ChooseModeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);

        Button btnPlayWithFriend = findViewById(R.id.btn_play_with_friend);
        Button btnPlayWithAI = findViewById(R.id.btn_play_with_ai);

        btnPlayWithFriend.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseModeActivity.this, MainActivity.class);
            intent.putExtra("playWithAI", false);
            startActivity(intent);
        });

        btnPlayWithAI.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseModeActivity.this, MainActivity.class);
            intent.putExtra("playWithAI", true);
            startActivity(intent);
        });
    }
}
