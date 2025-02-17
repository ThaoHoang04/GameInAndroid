package com.example.ccaro;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseModeActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = true;
    private ImageButton btnSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);

        Button btnPlayWithFriend = findViewById(R.id.btn_play_with_friend);
        Button btnPlayWithAI = findViewById(R.id.btn_play_with_ai);
        btnSound = findViewById(R.id.btn_sound);

        // Khởi tạo nhạc nền
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Xử lý sự kiện bật/tắt nhạc
        btnSound.setOnClickListener(v -> toggleMusic());

        // Xử lý chọn chế độ chơi
        btnPlayWithFriend.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseModeActivity.this, MainActivity.class);
            intent.putExtra("playWithAI", false);
            intent.putExtra("isMusicPlaying", isMusicPlaying);
            startActivity(intent);
        });

        btnPlayWithAI.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseModeActivity.this, MainActivity.class);
            intent.putExtra("playWithAI", true);
            intent.putExtra("isMusicPlaying", isMusicPlaying);
            startActivity(intent);
        });
    }

    private void toggleMusic() {
        isMusicPlaying = !isMusicPlaying;
        if (isMusicPlaying) {
            mediaPlayer.start();
            btnSound.setImageResource(R.drawable.baseline_volume_up_24);
        } else {
            mediaPlayer.pause();
            btnSound.setImageResource(R.drawable.baseline_volume_off_24);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
