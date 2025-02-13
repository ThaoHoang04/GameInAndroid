package com.example.ccaro;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView playerOneScore, playerTwoScore, playerStatus, player1;
    private Button[] buttons = new Button[9];
    private Button reset, again;
    private int playerOneScoreCount, playerTwoScoreCount;
    private boolean playerOneActive, playWithAI;
    private Animation scaleUp, moveUp;
    private int[] gameState = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    private int[][] winningPositions = {
            {0,1,2}, {3,4,5}, {6,7,8}, {0,3,6},
            {1,4,7}, {2,5,8}, {0,4,8}, {2,4,6}
    };
    private int rounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        playWithAI = getIntent().getBooleanExtra("playWithAI", false);
        getWidget();
        createAnimations(); // Tạo animation
    }

    private void getWidget() {
        playerOneScore = findViewById(R.id.score_Player1);
        playerTwoScore = findViewById(R.id.score_Player2);
        playerStatus = findViewById(R.id.status);
        reset = findViewById(R.id.btn_reset);
        again = findViewById(R.id.btn_play_again);
        player1 = findViewById(R.id.textPlayer1);
        player1.setTextColor(Color.parseColor("#FF0000"));

        for (int i = 0; i < 9; i++) {
            String buttonID = "btn" + (i + 1);
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);
            buttons[i].setOnClickListener(this);
        }

        playerOneScoreCount = 0;
        playerTwoScoreCount = 0;
        playerOneActive = true;
        rounds = 0;
    }
    private void createAnimations() {
                // Animation phóng to chữ
                scaleUp = new ScaleAnimation(
                        1f, 7f, // Phóng to từ kích thước ban đầu đến gấp năm lần
                        1f, 7f, // Phóng to theo cả chiều rộng và chiều cao
                        Animation.RELATIVE_TO_SELF, 0.5f, // Tâm phóng to là giữa
                        Animation.RELATIVE_TO_SELF, 0.5f // Tâm phóng to là giữa
                );
        scaleUp.setDuration(2000); // Thời gian phóng to 1 giây

        // Animation di chuyển lên trên
        moveUp = new TranslateAnimation(
                0, 0, // Không di chuyển theo chiều ngang
                0, -900  // Di chuyển lên phía trên màn hình (1000px)
        );
        moveUp.setDuration(2000); // Thời gian di chuyển lên
    }
    @Override
    public void onClick(View view) {
        if (!((Button) view).getText().toString().equals("") || checkWinner()) {
            return;
        }

        int gameStatePointer = Integer.parseInt(view.getResources().getResourceEntryName(view.getId()).substring(3)) - 1;

        if (playerOneActive) {
            ((Button)view).setText("X");
            ((Button)view).setTextColor(Color.parseColor("#FFD700"));
            gameState[gameStatePointer] = 0;
            player1.setTextColor(Color.parseColor("#000000"));
            TextView player2 = findViewById(R.id.textPlayer2);
            player2.setTextColor(Color.parseColor("#FF0000"));

        } else {
            TextView player2 = findViewById(R.id.textPlayer2);
            player2.setTextColor(Color.parseColor("#000000"));
            ((Button)view).setText("O");

            ((Button)view).setTextColor(Color.parseColor("#70fc3a"));

            gameState[gameStatePointer] = 1;
            player1.setTextColor(Color.parseColor("#FF0000"));
        }

        rounds++;

        if (checkWinner()) {
            if (playerOneActive) {
                playerOneScoreCount++; // Cập nhật điểm số cho người chơi 1
                playerStatus.setText("Player-1 has won");
            } else {
                playerTwoScoreCount++; // Cập nhật điểm số cho Player 2 hoặc AI
                playerStatus.setText(playWithAI ? "AI has won" : "Player-2 has won");
            }
            playerStatus.setTextColor(Color.parseColor("#FFD700")); // Đổi màu chữ thành vàng
            playerStatus.setTypeface(null, Typeface.BOLD); // In đậm
            playerStatus.setTextSize(40); // Kích thước chữ 50 (điều chỉnh theo nhu cầu của bạn)

            playerStatus.startAnimation(scaleUp); // Phóng to chữ

            playerStatus.startAnimation(moveUp); // Di chuyển chữ lên
            updatePlayerScore(); // Cập nhật điểm số trên giao diện
            new android.os.Handler().postDelayed(this::playAgain, 2000);

            return;
        }

        if (rounds == 9) {
            playerStatus.setText("No Winner");
            playerStatus.setTextColor(Color.parseColor("#FFD700"));
            playerStatus.setTextSize(40); // Kích thước chữ 50 (điều chỉnh theo nhu cầu của bạn)// Đổi màu chữ thành vàng
            playerStatus.setTypeface(null, Typeface.BOLD); // In đậm chữ
            playerStatus.startAnimation(scaleUp); // Phóng to chữ
            playerStatus.startAnimation(moveUp); // Di chuyển chữ lên
            new android.os.Handler().postDelayed(this::playAgain, 2000);
            return;
        }

        playerOneActive = !playerOneActive;

        if (playWithAI && !playerOneActive) {
            aiMove();
        }

        reset.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                playAgain();

                playerOneScoreCount= 0;

                playerTwoScoreCount= 0;

                updatePlayerScore();

            }

        });



        again.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                playAgain();

            }

        });
    }

    private void aiMove() {
        new android.os.Handler().postDelayed(() -> {
            Random rand = new Random();
            int move;
            do {
                move = rand.nextInt(9);
            } while (gameState[move] != 2);

            gameState[move] = 1;
            buttons[move].setText("O");
            buttons[move].setTextColor(Color.parseColor("#70fc3a"));

            rounds++;

            if (checkWinner()) {
                playerTwoScoreCount++;
                updatePlayerScore();
                playerStatus.setText("AI has won");
                playerStatus.setTextColor(Color.parseColor("#FFD700")); // Đổi màu chữ thành vàng
                playerStatus.setTextSize(40); // Kích thước chữ 50 (điều chỉnh theo nhu cầu của bạn)

                playerStatus.startAnimation(scaleUp); // Phóng to chữ
                playerStatus.startAnimation(moveUp); // Di chuyển chữ lên
                new android.os.Handler().postDelayed(this::playAgain, 2000);
                return;
            }

            playerOneActive = true;

            // Đổi màu chữ khi AI đánh
            player1.setTextColor(Color.parseColor("#FF0000"));
            TextView player2 = findViewById(R.id.textPlayer2);
            player2.setTextColor(Color.parseColor("#000000"));
        }, 300); // Trì hoãn .3 giây
    }



    private void playAgain() {
        rounds = 0;
        playerOneActive = true;

        for (int i = 0; i < buttons.length; i++) {
            gameState[i] = 2;
            buttons[i].setText("");
        }

        playerStatus.setText("");

        // Nếu chơi với máy và máy đi trước, thực hiện nước đi đầu tiên
        if (playWithAI && !playerOneActive) {
            aiMove();
        }
    }

    private void updatePlayerScore() {
        playerOneScore.setText(String.valueOf(playerOneScoreCount));
        playerTwoScore.setText(String.valueOf(playerTwoScoreCount));
    }

    private boolean checkWinner() {
        for (int[] winningPosition : winningPositions) {
            if (gameState[winningPosition[0]] == gameState[winningPosition[1]] &&
                    gameState[winningPosition[1]] == gameState[winningPosition[2]] &&
                    gameState[winningPosition[0]] != 2) {
                return true;
            }
        }
        return false;
    }

}
