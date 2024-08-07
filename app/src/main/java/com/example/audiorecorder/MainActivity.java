package com.example.audiorecorder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //変数
    ArrayList<String> filePath;
    TextView text, folderPath;

    RecyclerView rv;
    ListAdapter la;

    LinearLayoutManager manager;
    DividerItemDecoration decoration;
    ItemTouchHelper helper;
    FileObserver observer;
    Context context;

    //ライフサイクル
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("onCreate","Created");

        text = findViewById(R.id.txt);
        text.setText(R.string.description);

        folderPath = findViewById(R.id.folderPath);

        rv = findViewById(R.id.recyclerview);

        context = getApplicationContext();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart","Started");
        folderPath.setText(getFolderPath());

        setRecyclerView(FileListProperties(getFolderPath()));

        setFileObserver(getFolderPath());

        checkPermissions();
        startNotify(checkForeground());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "Resumed");

        //startNotify(checkForeground());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause","Paused");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("onDestroy","Destroyed");

        stopNotify(checkForeground());

        if (observer != null) {
            observer.stopWatching();
        }
    }

    //メソッド//
    //通知判定
    void startNotify(Boolean check) {
        if (check) {
            startForegroundService(new Intent(MainActivity.this, MyService.class));
        }
    }

    void stopNotify(Boolean check) {
        if (check) {
            stopService(new Intent(MainActivity.this, MyService.class));
        }
    }

    //パーミッション判定
    void checkPermissions() {
        String[] PERMISSIONS;

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            PERMISSIONS = new String[2];
            PERMISSIONS[0] = Manifest.permission.RECORD_AUDIO;
            PERMISSIONS[1] = Manifest.permission.READ_EXTERNAL_STORAGE;

        } else {
            PERMISSIONS = new String[3];
            PERMISSIONS[0] = Manifest.permission.RECORD_AUDIO;
            PERMISSIONS[1] = Manifest.permission.POST_NOTIFICATIONS;
            PERMISSIONS[2] = Manifest.permission.READ_MEDIA_AUDIO;

        }

        for(String permission : PERMISSIONS) {
            if(ActivityCompat.checkSelfPermission(MainActivity.this, permission)
            != PackageManager.PERMISSION_GRANTED) {

                requestPermissions.launch(PERMISSIONS);
            }
        }
    }

    boolean checkForeground() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                    MainActivity.this,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        } else {

            return (ActivityCompat.checkSelfPermission(
                    MainActivity.this,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(
                            MainActivity.this,
                            Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    );
        }
    }

    ActivityResultLauncher<String[]> requestPermissions = registerForActivityResult(

            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if(result.containsValue(false)) {
                    Toast.makeText(context, "要求された総ての権限を許可してください", Toast.LENGTH_SHORT)
                            .show();

                } else {
                    Toast.makeText(context, "権限を許可した後に一度タスク管理画面へ移行してください", Toast.LENGTH_SHORT)
                            .show();
                }
            }
    );

    //ウィジェット設定//

    //セットリサイクラービュー
    void setRecyclerView(ArrayList<String> list) {
        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(manager);

        la = new ListAdapter(list);
        rv.setAdapter(la);

        decoration = new DividerItemDecoration(MainActivity.this,DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(decoration);

        helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //ファイルを削除
                int position =viewHolder.getLayoutPosition();

                if(!filePath.isEmpty()) {
                    try {
                        Files.delete(Paths.get(filePath.get(position)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    //filePath.remove(position);
                    setRecyclerView(FileListProperties(getFolderPath()));
                    Toast.makeText(context,"削除しました。",Toast.LENGTH_SHORT).show();
                    la.notifyItemRemoved(position);
                }
            }
        });

        helper.attachToRecyclerView(rv);
    }

    //フォルダパス判定と取得
    String getFolderPath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());

        return contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath();
    }

    //配列リストの設定
    ArrayList<String> FileListProperties(String path) {

        File[] ArrayFile = new File(path).listFiles();
        filePath = new ArrayList<>();

        if(ArrayFile != null && ArrayFile.length != 0) {
            Arrays.sort(ArrayFile, Collections.reverseOrder());

            for (int i = 0; i < ArrayFile.length; i++) {
                filePath.add(i, ArrayFile[i].getPath());
            }

            return filePath;
        }

        return filePath;
    }

    //セットファイルオブザーバーの設定
    void setFileObserver(String path) {
        observer = new FileObserver(path) {

            @Override
            public void onEvent(int i, @Nullable String s) {
                runOnUiThread(() -> {
                    if(i == FileObserver.CREATE) {
                        setRecyclerView(FileListProperties(getFolderPath()));

                    }
                });
            }
        };

        observer.startWatching();
    }

    //クラス//

    //ビューホルダー
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView filePath;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.view = itemView;
            this.filePath = view.findViewById(R.id.file_path);
        }
    }
    //リストアダプター
    public static class ListAdapter extends RecyclerView.Adapter<ViewHolder> {
        final private List<String> FilePath;
        ViewHolder vh;

        public ListAdapter(List<String> FilePath) { this.FilePath = FilePath;}

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);

            vh = new ViewHolder(v);
            vh.itemView.setOnClickListener(view -> {
                int position = vh.getLayoutPosition();

                String Path = FilePath.get(position);
                Context context = view.getContext();

                context.startActivity(new Intent(context, PlayerActivity.class)
                        .putExtra("player",Path));
            });

            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = new File(FilePath.get(position)).getName();
            holder.filePath.setText(name);
        }

        @Override
        public int getItemCount() { return this.FilePath.size(); }
    }
}