package com.example.ccaro;

import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
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
    private Handler handler = new Handler();  // Khai báo handler để chạy runnable

    // Biến quản lý âm thanh
    private MediaPlayer mediaPlayer, winSoundPlayer;
    private boolean isMusicPlaying = true;
    private ImageButton btnSound;

    private boolean previousWinner;  // Biến để lưu người thắng ván trước
// xử lý chạy ngược time
    private SeekBar seekBar1,seekBar2;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        playWithAI = getIntent().getBooleanExtra("playWithAI", false);
        aiLevel = getIntent().getStringExtra("aiLevel");
        getWidget();
        checkWinner();
        createAnimations();
        // Khởi tạo nhạc nền
        isMusicPlaying = getIntent().getBooleanExtra("isMusicPlaying", true);
        if (isMusicPlaying) {
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();

        }

        // Khởi tạo âm thanh chiến thắng
        winSoundPlayer = MediaPlayer.create(this, R.raw.win);

        previousWinner = false;  // Khởi tạo biến theo mặc định là không có người thắng trước đó
    }

    private void getWidget() {
        playerOneScore = findViewById(R.id.score_Player1);
        playerTwoScore = findViewById(R.id.score_Player2);
        playerStatus = findViewById(R.id.status);
        reset = findViewById(R.id.btn_reset);
        again = findViewById(R.id.btn_play_again);
        player1 = findViewById(R.id.textPlayer1);
        player1.setTextColor(Color.parseColor("#FF0000"));
        seekBar1 = findViewById(R.id.seekBar1);
        seekBar2 = findViewById(R.id.seekBar2);
        startCountdown(seekBar1);

        for (int i = 0; i < 9; i++) {
            String buttonID = "btn" + (i + 1);
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);
            buttons[i].setOnClickListener(this);
        }

        // Xử lý sự kiện nhấn loa
//        btnSound = findViewById(R.id.btn_sound);
//        btnSound.setOnClickListener(v -> toggleMusic());

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
            seekBar1.setVisibility(View.GONE);

            ((Button) view).setTextColor(Color.parseColor("#FFD700"));
            gameState[gameStatePointer] = 0;
            player1.setTextColor(Color.parseColor("#000000"));
            TextView player2 = findViewById(R.id.textPlayer2);
            player2.setTextColor(Color.parseColor("#FF0000"));
            startCountdown(seekBar2);
        } else {
            TextView player2 = findViewById(R.id.textPlayer2);
            player2.setTextColor(Color.parseColor("#000000"));
            ((Button)view).setText("O");
            seekBar2.setVisibility(View.GONE);

            ((Button)view).setTextColor(Color.parseColor("#70fc3a"));

            gameState[gameStatePointer] = 1;
            player1.setTextColor(Color.parseColor("#FF0000"));
              startCountdown(seekBar1);

        }

        rounds++;

        if (checkWinner()) {
            // Điểm cộng chỉ trong hàm checkWinner, không ở đây nữa
            if (playerOneActive) {
                playerStatus.setText("Player-1 has won");
            } else {
                playerStatus.setText(playWithAI ? "Player-1 has lost" : "Player-2 has won");
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
                    playerStatus.setText("Player-1 has  ");
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
            seekBar2.setVisibility(View.GONE);
            startCountdown(seekBar1);
        }, 500);  // AI sẽ đánh sau 500ms
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
                    playerStatus.setText("Player-1 has lost ");
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
            seekBar2.setVisibility(View.GONE);
            startCountdown(seekBar1);
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
                    playerStatus.setText("Player-1 has lost");
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
            seekBar2.setVisibility(View.GONE);
            startCountdown(seekBar1);
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
        if (isMusicPlaying) {  // Chỉ phát âm thanh nếu nhạc đang bật
            if (winSoundPlayer == null) {
                winSoundPlayer = MediaPlayer.create(this, R.raw.win);
            }
            winSoundPlayer.start();
        }
    }


    private void playAgain() {
        rounds = 0;

        // Đảo ngược người đi trước nếu có người thắng ván trước
        if (previousWinner) {
            playerOneActive = !playerOneActive;
        }

        // Xóa trạng thái của game và bắt đầu lại
        for (int i = 0; i < buttons.length; i++) {
            gameState[i] = 2;  // Đặt lại trạng thái của tất cả các ô
            buttons[i].setText("");  // Xóa hết các kí tự trên các nút
        }

        // Reset SeekBar
        seekBar1.setVisibility(View.VISIBLE);
        seekBar2.setVisibility(View.VISIBLE);
        seekBar1.setProgress(100);
        seekBar2.setProgress(100);

        // Bắt đầu lại đếm ngược cho người chơi đầu tiên
        if (playerOneActive) {
            startCountdown(seekBar1);
        } else {
            startCountdown(seekBar2);
        }

        // Cập nhật UI
        playerStatus.setText("");
        previousWinner = false; // Reset trạng thái người thắng

        // Nếu chơi với AI và đến lượt AI, thì AI sẽ đi ngay
        if (playWithAI && !playerOneActive) {
            if ("easy".equals(aiLevel)) {
                aiMove();
            } else if ("medium".equals(aiLevel)) {
                aiMoveMedium();
            } else {
                aiMoveHard();
            }
        }

        // Chạy lại kiểm tra trạng thái SeekBar
        handler.postDelayed(checkSeekBarProgress, 1000);
    }

    private void updatePlayerScore() {
        playerOneScore.setText(String.valueOf(playerOneScoreCount));
        playerTwoScore.setText(String.valueOf(playerTwoScoreCount));
    }
    private Runnable checkSeekBarProgress = new Runnable() {
        @Override
        public void run() {
            // Kiểm tra nếu SeekBar của Player 1 hết thời gian
            if (seekBar1 != null && seekBar1.getProgress() == 0) {
                seekBar1.setVisibility(View.GONE);
                seekBar2.setVisibility(View.GONE);
                playerTwoScoreCount++; // Cập nhật điểm số cho Player 2
                playWinSound();  // Phát âm thanh khi Player 2 thắng
                previousWinner = true;  // Lưu trạng thái người thắng

                playerStatus.setText(playWithAI ? "lost lost" : "Player-2 has won");
                playerStatus.setTextColor(Color.RED);
                playerStatus.setTypeface(null, Typeface.BOLD);
                playerStatus.setTextSize(40);
                playerStatus.startAnimation(scaleUp);
                playerStatus.startAnimation(moveUp);
                updatePlayerScore();

                // Dừng kiểm tra SeekBar và bắt đầu lại trò chơi sau 5 giây
                handler.removeCallbacks(this);
                new android.os.Handler().postDelayed(() -> {
                    playAgain();
                    handler.post(checkSeekBarProgress);  // Chạy lại kiểm tra SeekBar
                }, 1000);

                return;
            }

            // Kiểm tra nếu SeekBar của Player 2 hết thời gian
            if (seekBar2 != null && seekBar2.getProgress() == 0) {
                seekBar1.setVisibility(View.GONE);
                seekBar2.setVisibility(View.GONE);
                playerOneScoreCount++; // Cập nhật điểm số cho Player 1
                playWinSound();  // Phát âm thanh khi Player 1 thắng
                previousWinner = true;  // Lưu trạng thái người thắng

                playerStatus.setText("Player 1 has won");
                playerStatus.setTextColor(Color.RED);
                playerStatus.setTypeface(null, Typeface.BOLD);
                playerStatus.setTextSize(40);
                playerStatus.startAnimation(scaleUp);
                playerStatus.startAnimation(moveUp);
                updatePlayerScore();

                // Dừng kiểm tra SeekBar và bắt đầu lại trò chơi sau 5 giây
                handler.removeCallbacks(this);
                new android.os.Handler().postDelayed(() -> {
                    playAgain();
                    handler.post(checkSeekBarProgress);  // Chạy lại kiểm tra SeekBar
                }, 5000);

                return;
            }

            // Nếu SeekBar chưa hết thời gian, tiếp tục kiểm tra sau mỗi giây
            handler.postDelayed(this, 1000);
        }
    };

    private void startSeekBarTimer() {
        handler.postDelayed(checkSeekBarProgress, 1000);  // Bắt đầu kiểm tra SeekBar mỗi giây
    }

    private void stopSeekBarTimer() {
        handler.removeCallbacks(checkSeekBarProgress);  // Dừng kiểm tra khi kết thúc
    }

    private boolean checkWinner() {
        for (int[] winningPosition : winningPositions) {
            if (gameState[winningPosition[0]] == gameState[winningPosition[1]] &&
                    gameState[winningPosition[1]] == gameState[winningPosition[2]] &&
                    gameState[winningPosition[0]] != 2) {
                if (gameState[winningPosition[0]] == 0) {
                    seekBar1.setVisibility(View.GONE);
                    seekBar2.setVisibility(View.GONE);
                    playerOneScoreCount++; // Cập nhật điểm số cho người chơi 1
                    playWinSound();  // Phát âm thanh khi Player 1 thắng
                    previousWinner = true;  // Lưu trạng thái người thắng
                } else {
                    seekBar2.setVisibility(View.GONE);
                    seekBar1.setVisibility(View.GONE);
                    startCountdown(seekBar2); // Nếu cần, có thể bắt đầu lại đếm ngược cho Player 2
                    playerTwoScoreCount++; // Cập nhật điểm số cho Player 2
                    playWinSound();  // Phát âm thanh khi Player 2 (hoặc AI) thắng
                    previousWinner = true;  // Lưu trạng thái người thắng
                }

                playerStatus.setTextColor(Color.parseColor("#FFD700")); // Đổi màu chữ thành vàng
                playerStatus.setTypeface(null, Typeface.BOLD); // In đậm chữ
                playerStatus.setTextSize(40); // Kích thước chữ 40
                playerStatus.startAnimation(scaleUp); // Phóng to chữ
                playerStatus.startAnimation(moveUp); // Di chuyển chữ lên
                updatePlayerScore(); // Cập nhật điểm số

                stopSeekBarTimer();  // Dừng kiểm tra SeekBar khi có người thắng
                return true;
            }
        }

        // Nếu không có người thắng, tiếp tục theo dõi SeekBar
        startSeekBarTimer();  // Bắt đầu kiểm tra trạng thái SeekBar
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    // Hàm bắt đầu đếm ngược

    private void startCountdown(SeekBar seekBar) {
        if (countDownTimer != null) {
            countDownTimer.cancel();  // Dừng timer trước khi chạy cái mới
        }

        seekBar.setVisibility(View.VISIBLE);
        seekBar.setProgress(100);

        countDownTimer = new CountDownTimer(10000, 100) { // 10s, cập nhật mỗi 100ms
            public void onTick(long millisUntilFinished) {
                seekBar.setProgress((int) (millisUntilFinished / 100));
            }

            public void onFinish() {
                seekBar.setProgress(0);
            }
        }.start();
    }

}