package com.example.ccaro;

import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
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
    private String aiLevel;
    private Animation scaleUp, moveUp;
    private int[] gameState = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    private int[][] winningPositions = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6},
            {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}
    };
    private int rounds;

    // Biến quản lý âm thanh
    private MediaPlayer mediaPlayer, winSoundPlayer;
    private boolean isMusicPlaying = true;
    private ImageButton btnSound;

    private boolean previousWinner;  // Biến để lưu người thắng ván trước

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        playWithAI = getIntent().getBooleanExtra("playWithAI", false);
        aiLevel = getIntent().getStringExtra("aiLevel");
        getWidget();
        createAnimations();

        // Khởi tạo nhạc nền
        isMusicPlaying = getIntent().getBooleanExtra("isMusicPlaying", true);
        if (isMusicPlaying) {
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            btnSound.setImageResource(R.drawable.baseline_volume_up_24);
        } else {
            btnSound.setImageResource(R.drawable.baseline_volume_off_24);
        }

        // Khởi tạo âm thanh chiến thắng
        winSoundPlayer = MediaPlayer.create(this, R.raw.win);

        previousWinner = false;  // Khởi tạo biến theo mặc định là không có người thắng trước đó
    }
    private void toggleMusic() {
        isMusicPlaying = !isMusicPlaying;

        if (isMusicPlaying) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
                mediaPlayer.setLooping(true);
            }
            mediaPlayer.start();
            btnSound.setImageResource(R.drawable.baseline_volume_up_24);
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            btnSound.setImageResource(R.drawable.baseline_volume_off_24);
        }
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

        // Xử lý sự kiện nhấn loa
        btnSound = findViewById(R.id.btn_sound);
        btnSound.setOnClickListener(v -> toggleMusic());

        playerOneScoreCount = 0;
        playerTwoScoreCount = 0;
        playerOneActive = true;
        rounds = 0;
    }

    private void createAnimations() {
        scaleUp = new ScaleAnimation(1f, 7f, 1f, 7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setDuration(2000);

        moveUp = new TranslateAnimation(0, 0, 0, -900);
        moveUp.setDuration(2000);
    }

    @Override
    public void onClick(View view) {
        if (!((Button) view).getText().toString().equals("") || checkWinner()) {
            return;
        }

        int gameStatePointer = Integer.parseInt(view.getResources().getResourceEntryName(view.getId()).substring(3)) - 1;

        if (playerOneActive) {
            ((Button) view).setText("X");
            ((Button) view).setTextColor(Color.parseColor("#FFD700"));
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
            // Điểm cộng chỉ trong hàm checkWinner, không ở đây nữa
            if (playerOneActive) {
                playerStatus.setText("Player-1 has won");
            } else {
                playerStatus.setText(playWithAI ? "AI has won" : "Player-2 has won");
            }
            playerStatus.setTextColor(Color.parseColor("#FFD700")); // Đổi màu chữ thành vàng
            playerStatus.setTypeface(null, Typeface.BOLD); // In đậm
            playerStatus.setTextSize(40); // Kích thước chữ 50 (điều chỉnh theo nhu cầu của bạn)

            playerStatus.startAnimation(scaleUp); // Phóng to chữ

            playerStatus.startAnimation(moveUp); // Di chuyển chữ lên
            updatePlayerScore(); // Cập nhật điểm số trên giao diện
            new android.os.Handler().postDelayed(this::playAgain, 5000);

            return;
        }

        if (rounds == 9) {
            playerStatus.setText("No Winner");
            playerStatus.setTextColor(Color.parseColor("#FFD700"));
            playerStatus.setTextSize(40); // Kích thước chữ 50 (điều chỉnh theo nhu cầu của bạn)// Đổi màu chữ thành vàng
            playerStatus.setTypeface(null, Typeface.BOLD); // In đậm chữ
            playerStatus.startAnimation(scaleUp); // Phóng to chữ
            playerStatus.startAnimation(moveUp); // Di chuyển chữ lên
            new android.os.Handler().postDelayed(this::playAgain, 5000);
            return;
        }

        playerOneActive = !playerOneActive;

        if (playWithAI && !playerOneActive) {
            if ("easy".equals(aiLevel)) {
                aiMove();
            } else if ("medium".equals(aiLevel)) {
                aiMoveMedium();
            } else {
                aiMoveHard();
            }
        }

        reset.setOnClickListener(view1 -> {
            playAgain();
            playerOneScoreCount = 0;
            playerTwoScoreCount = 0;
            updatePlayerScore();
        });

        again.setOnClickListener(view1 -> playAgain());

    }

    private void aiMove() {
        new android.os.Handler().postDelayed(() -> {
            Random rand = new Random();
            int move;
            do {
                move = rand.nextInt(9);
            } while (gameState[move] != 2);  // Lựa chọn ô trống

            gameState[move] = 1;  // AI đánh dấu "O"
            buttons[move].setText("O");
            buttons[move].setTextColor(Color.parseColor("#70fc3a"));
            rounds++;

            // Kiểm tra kết quả sau khi AI đánh xong
            if (checkWinner()) {
                if (!playerOneActive) {
                    playerStatus.setText("AI has won");
                    playWinSound();  // Phát âm thanh khi AI thắng
                    updatePlayerScore();
                    new android.os.Handler().postDelayed(this::playAgain, 2000); // Bắt đầu lại trò chơi sau 2 giây
                }
            } else if (rounds == 9) {
                playerStatus.setText("No Winner");
                playerStatus.setTextColor(Color.parseColor("#FFD700"));
                playerStatus.setTextSize(40);
                playerStatus.setTypeface(null, Typeface.BOLD);
                playerStatus.startAnimation(scaleUp); // Phóng to chữ
                playerStatus.startAnimation(moveUp); // Di chuyển chữ lên
                new android.os.Handler().postDelayed(this::playAgain, 2000); // Bắt đầu lại trò chơi sau 2 giây
            } else {
                playerOneActive = true;  // Đổi lại lượt cho người chơi
            }
            // Đổi màu chữ khi AI đánh
            player1.setTextColor(Color.parseColor("#FF0000"));
            TextView player2 = findViewById(R.id.textPlayer2);
            player2.setTextColor(Color.parseColor("#000000"));
        }, 300);  // AI sẽ đánh sau 500ms
    }
private void aiMoveMedium() {
    new android.os.Handler().postDelayed(() -> {
        int move = -1;

        // Kiểm tra nếu AI có thể thắng
        move = checkForWinningMove(1);
        if (move == -1) {
            // Kiểm tra nếu đối thủ có thể thắng và ngăn chặn
            move = checkForWinningMove(0);
        }

        // Nếu không có nước đi chiến thắng hoặc ngăn chặn, chọn ngẫu nhiên
        if (move == -1) {
            Random rand = new Random();
            do {
                move = rand.nextInt(9);
            } while (gameState[move] != 2);
        }

        gameState[move] = 1;  // AI đánh dấu "O"
        buttons[move].setText("O");
        buttons[move].setTextColor(Color.parseColor("#70fc3a"));
        rounds++;

        if (checkWinner()) {
            if (!playerOneActive) {
                playerStatus.setText("AI has won");
                playWinSound();
                updatePlayerScore();
                new android.os.Handler().postDelayed(this::playAgain, 2000);
            }
        } else if (rounds == 9) {
            playerStatus.setText("No Winner");
            playerStatus.setTextColor(Color.parseColor("#FFD700"));
            playerStatus.setTextSize(40);
            playerStatus.setTypeface(null, Typeface.BOLD);
            playerStatus.startAnimation(scaleUp);
            playerStatus.startAnimation(moveUp);
            new android.os.Handler().postDelayed(this::playAgain, 2000);
        } else {
            playerOneActive = true;
        }

        player1.setTextColor(Color.parseColor("#FF0000"));
        TextView player2 = findViewById(R.id.textPlayer2);
        player2.setTextColor(Color.parseColor("#000000"));
    }, 300);
}

    private void aiMoveHard() {
        new android.os.Handler().postDelayed(() -> {
            int move = findBestMove();

            gameState[move] = 1;  // AI đánh dấu "O"
            buttons[move].setText("O");
            buttons[move].setTextColor(Color.parseColor("#70fc3a"));
            rounds++;

            if (checkWinner()) {
                if (!playerOneActive) {
                    playerStatus.setText("AI has won");
                    playWinSound();
                    updatePlayerScore();
                    new android.os.Handler().postDelayed(this::playAgain, 2000);
                }
            } else if (rounds == 9) {
                playerStatus.setText("No Winner");
                playerStatus.setTextColor(Color.parseColor("#FFD700"));
                playerStatus.setTextSize(40);
                playerStatus.setTypeface(null, Typeface.BOLD);
                playerStatus.startAnimation(scaleUp);
                playerStatus.startAnimation(moveUp);
                new android.os.Handler().postDelayed(this::playAgain, 2000);
            } else {
                playerOneActive = true;
            }

            player1.setTextColor(Color.parseColor("#FF0000"));
            TextView player2 = findViewById(R.id.textPlayer2);
            player2.setTextColor(Color.parseColor("#000000"));
        }, 300);
    }

    // Kiểm tra xem AI có thể thắng không
    private int checkForWinningMove(int player) {
        for (int[] winningPosition : winningPositions) {
            if (gameState[winningPosition[0]] == player &&
                    gameState[winningPosition[1]] == player &&
                    gameState[winningPosition[2]] == 2) {
                return winningPosition[2];
            }
            if (gameState[winningPosition[1]] == player &&
                    gameState[winningPosition[2]] == player &&
                    gameState[winningPosition[0]] == 2) {
                return winningPosition[0];
            }
            if (gameState[winningPosition[0]] == player &&
                    gameState[winningPosition[2]] == player &&
                    gameState[winningPosition[1]] == 2) {
                return winningPosition[1];
            }
        }
        return -1;
    }

    // Tìm nước đi tốt nhất cho AI
    private int findBestMove() {
        int bestVal = Integer.MIN_VALUE;
        int bestMove = -1;

        for (int i = 0; i < 9; i++) {
            if (gameState[i] == 2) {
                gameState[i] = 1;  // Giả lập AI đi
                int moveVal = minimax(gameState, 0, false);
                gameState[i] = 2;  // Khôi phục trạng thái ô

                if (moveVal > bestVal) {
                    bestMove = i;
                    bestVal = moveVal;
                }
            }
        }

        return bestMove;
    }

    // Minimax thuật toán (có thể triển khai cho cấp độ khó)
    private int minimax(int[] board, int depth, boolean isMax) {
        int score = evaluate(board);

        // Nếu AI thắng
        if (score == 10) return score;

        // Nếu đối thủ thắng
        if (score == -10) return score;

        // Nếu không còn nước đi nào
        if (isMovesLeft(board) == false) return 0;

        if (isMax) {
            int best = Integer.MIN_VALUE;

            for (int i = 0; i < 9; i++) {
                if (board[i] == 2) {
                    board[i] = 1;
                    best = Math.max(best, minimax(board, depth + 1, !isMax));
                    board[i] = 2;
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;

            for (int i = 0; i < 9; i++) {
                if (board[i] == 2) {
                    board[i] = 0;
                    best = Math.min(best, minimax(board, depth + 1, !isMax));
                    board[i] = 2;
                }
            }
            return best;
        }
    }

    private boolean isMovesLeft(int[] board) {
        for (int i = 0; i < 9; i++) {
            if (board[i] == 2) return true;
        }
        return false;
    }

    private int evaluate(int[] b) {
        // Kiểm tra tất cả các hàng, cột và đường chéo
        for (int[] winningPosition : winningPositions) {
            if (b[winningPosition[0]] == b[winningPosition[1]] &&
                    b[winningPosition[1]] == b[winningPosition[2]]) {
                if (b[winningPosition[0]] == 1) return 10;
                else if (b[winningPosition[0]] == 0) return -10;
            }
        }
        return 0;
    }
    private void playWinSound() {
        if (winSoundPlayer != null) {
            winSoundPlayer.start();
        }
    }

    private void playAgain() {
        rounds = 0;
        // Đảo ngược người đi trước sau mỗi ván chơi nếu có người thắng ván trước
        if (previousWinner) {
            playerOneActive = !playerOneActive;  // Đổi người đi trước
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.prepareAsync();  // Chuẩn bị lại nhạc nền nếu cần
        }

        // Xóa trạng thái của game và bắt đầu lại
        for (int i = 0; i < buttons.length; i++) {
            gameState[i] = 2;  // Đặt lại trạng thái của tất cả các ô
            buttons[i].setText("");  // Xóa hết các kí tự trên các nút
        }

        playerStatus.setText("");
        previousWinner = false;  // Reset trạng thái người thắng
        if (playWithAI && !playerOneActive) {
//            aiMove();  // AI sẽ đánh ngay sau khi trò chơi bắt đầu lại
//            aiMoveMedium();
            aiMoveHard();
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
                if (gameState[winningPosition[0]] == 0) {
                    playerOneScoreCount++; // Cập nhật điểm số cho người chơi 1
                    playWinSound();  // Phát âm thanh khi Player 1 thắng
                    previousWinner = true;  // Lưu trạng thái người thắng
                } else {
                    playerTwoScoreCount++; // Cập nhật điểm số cho Player 2 hoặc AI
                    playWinSound();  // Phát âm thanh khi Player 2 (hoặc AI) thắng
                    previousWinner = true;  // Lưu trạng thái người thắng
                }

                playerStatus.setTextColor(Color.parseColor("#FFD700")); // Đổi màu chữ thành vàng
                playerStatus.setTypeface(null, Typeface.BOLD); // In đậm chữ
                playerStatus.setTextSize(40); // Kích thước chữ 50
                playerStatus.startAnimation(scaleUp); // Phóng to chữ
                playerStatus.startAnimation(moveUp); // Di chuyển chữ lên
                updatePlayerScore(); // Cập nhật điểm số

                return true;
            }
        }
        return false;
    }
}
