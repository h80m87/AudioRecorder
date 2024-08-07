package com.example.audiorecorder;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyService extends Service {
    public MyService() {
    }
    //変数
    MediaRecorder mediaRecorder = null;
    Notification notification;
    NotificationChannel notificationChannel;
    NotificationManager notificationManager;
    public static final String KEY = "AudioRecorderForegroundService";
    public static final int ID = 1;

    //ライフサイクル
    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d("onCreate", "ServiceCreated");
        notificationChannel = new NotificationChannel(KEY,
                "オーディオレコーダー", NotificationManager.IMPORTANCE_DEFAULT);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //notificationManager.createNotificationChannel(notificationChannel);

    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("onStartCommand", "onStartCommand()");
        boolean check = intent.getBooleanExtra("Start", false);

        Intent i = new Intent(this, MyReceiver.class);

        NotificationCompat.Action start = new NotificationCompat.Action
                .Builder(R.drawable.ic_stat_recording,
                "開始",check? null:makePendingIntent(i,false)).build();

        NotificationCompat.Action complete = new NotificationCompat.Action
                .Builder(R.drawable.ic_stat_recording,
                "完了",check? makePendingIntent(i, true):null).build();

        if(check) {
            notification = new NotificationCompat.Builder(this, KEY)
                    .setSmallIcon(R.drawable.ic_stat_recording)
                    .setContentTitle("起動中…")
                    .setSilent(true)
                    .setPriority(2)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(start)
                    .addAction(complete)
                    .build();

            if(mediaRecorder == null) {
                startRecording();
            }

        } else {
            notification = new NotificationCompat.Builder(this, KEY)
                    .setSmallIcon(R.drawable.ic_stat_recording)
                    .setContentTitle("待機中…")
                    .setSilent(true)
                    .setPriority(2)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(start)
                    .addAction(complete)
                    .build();

            if(mediaRecorder != null) {
                stopRecording();
            }

        }

        //notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        startForeground(ID,notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopSelf();
    }

    //メソッド
    //アクションボタン判定
    @RequiresApi(api = Build.VERSION_CODES.S)
    PendingIntent makePendingIntent(Intent i, boolean check) {
        if(check) {

            return PendingIntent.getBroadcast(this,101,
                    i.putExtra("Start", true),
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {

            return PendingIntent.getBroadcast(this,101,
                    i.putExtra("Start",false),
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    //メディアレコーダーメソッド
    void startRecording() {
        if(mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(checkFile());

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                Toast.makeText(this, "録音を開始しました", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    void stopRecording() {
        if(mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();

            mediaRecorder = null;

            Toast.makeText(this,"録音を終了しました",Toast.LENGTH_SHORT).show();
        }
    }

    //ファイルの保存設定
    private String checkFile() {

        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(music, getDate()+".mp3");

        return file.getPath();
    }
    private String getDate() {
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy年 MM月 dd日 a hh時 mm分 ss秒");

        return sdf.format(date);
    }

   @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}