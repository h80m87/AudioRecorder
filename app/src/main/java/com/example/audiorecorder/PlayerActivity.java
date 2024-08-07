package com.example.audiorecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {
//変数//
    ImageView imagePlayer;
    TextView text, currentTime, durationTime;
    SeekBar seekBar;
    MediaPlayer mediaPlayer;
    Handler handler;
    Runnable runnable;
    String path;

//ライフサイクル//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Log.d("onCreate","PAonCreated");

        stopService(new Intent(this,MyService.class));

        imagePlayer = findViewById(R.id.PlayerButton);
        text = findViewById(R.id.file_txt);
        currentTime = findViewById(R.id.StartSecond);
        durationTime = findViewById(R.id.EndSecond);
        seekBar = findViewById(R.id.seekBar);

        mediaPlayer = new MediaPlayer();
        handler = new Handler();
        runnable = () -> {
            updateSeekBar();
            long currentDuration = mediaPlayer.getCurrentPosition();
            currentTime.setText(milliSecondsTimer(currentDuration));
        };

        seekBar.setMax(100);

        path = getIntent().getStringExtra("player");

        prepareMediaPlayer(path);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart","PAonStarted");

        //path = getIntent().getStringExtra("player");
        text.setText(path);

        imagePlayer.setOnClickListener(v -> {
            if(mediaPlayer.isPlaying()) {
                handler.removeCallbacks(runnable);
                mediaPlayer.pause();
                imagePlayer.setImageResource(R.drawable.baseline_play_circle_24);
            } else {
                mediaPlayer.start();
                imagePlayer.setImageResource(R.drawable.baseline_pause_circle_outline_24);
                updateSeekBar();
            }
        });

        //prepareMediaPlayer(path);

        seekBar.setOnTouchListener((v, event) -> {
            SeekBar seekBar = (SeekBar) v;
            int playPosition = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
            mediaPlayer.seekTo(playPosition);
            currentTime.setText(milliSecondsTimer(mediaPlayer.getCurrentPosition()));

            return false;
        });

        mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> seekBar.setSecondaryProgress(percent));

        mediaPlayer.setOnCompletionListener(mp -> {
            seekBar.setProgress(0);
            imagePlayer.setImageResource(R.drawable.baseline_play_circle_24);
            currentTime.setText(R.string.initialize_second);
            durationTime.setText(R.string.initialize_second);
            mediaPlayer.reset();
            prepareMediaPlayer(path);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop","PAonStopped");

        if(Objects.nonNull(mediaPlayer) && mediaPlayer.isPlaying()) {
            handler.removeCallbacks(runnable);
            mediaPlayer.pause();
            imagePlayer.setImageResource(R.drawable.baseline_play_circle_24);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy","PAonDestroyed");

        if(Objects.nonNull(mediaPlayer)) {
            mediaPlayer.release();
            mediaPlayer = null;

            finish();
        }
    }

//メソッド//
    private void prepareMediaPlayer(String path) {
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            durationTime.setText(milliSecondsTimer(mediaPlayer.getDuration()));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void updateSeekBar() {
        if(mediaPlayer.isPlaying()) {
        seekBar.setProgress((int)(((float) mediaPlayer
                .getCurrentPosition() / mediaPlayer.getDuration()) * 100));
        handler.postDelayed(runnable, 1000);
        }
    }

    private String milliSecondsTimer(long milliSeconds) {
        String time ="", seconds;

        int hour = (int) (milliSeconds / (1000 * 60 * 60));
        int minute = (int) (milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
        int second = (int) ((milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if(hour > 0) {
            time = hour + ":";
        }

        if(second < 10) {
            seconds = "0" + second;
        } else {
            seconds = "" + second;
        }

        time = time + minute + ":" + seconds;

        return time;
    }
}