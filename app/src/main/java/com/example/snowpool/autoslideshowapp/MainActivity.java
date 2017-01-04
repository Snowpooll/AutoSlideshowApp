package com.example.snowpool.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button next;
    private Button back;
    private Button play;
    private ImageView imageView;

    boolean isPlaying; //flag判定
    private int now=0; //cursor 操作のため

    private Handler mHandler = new Handler(); //UI Threadへのpost用ハンドラ
    private MyTimer myTimer; //timerTask
    private Timer timer;

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED){
                getContentsInfo(now);
            }else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSIONS_REQUEST_CODE);
            }
        }else {
            getContentsInfo(now);
        }

        isPlaying = true;

        next =(Button)findViewById(R.id.next);
        next.setOnClickListener(this);
        back =(Button)findViewById(R.id.back);
        back.setOnClickListener(this);
        play =(Button)findViewById(R.id.play);
        play.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.next:
                now = now+1;
                getContentsInfo(now);
                break;

            case R.id.back:
                now = now-1;
                getContentsInfo(now);
                break;

            case R.id.play:
                if(isPlaying){
                    isPlaying=false;
                    play.setText("停止");
                    next.setEnabled(false);
                    back.setEnabled(false);
                    //timer インスタンス生成
                    this.timer = new Timer();
                    //timer task インスタンス
                    this.myTimer = new MyTimer();
                    //timer schedule を2秒で
                    this.timer.schedule(myTimer,2000,2000);
                }else {
                    isPlaying=true;
                    play.setText("再生");
                    next.setEnabled(true);
                    back.setEnabled(true);
                    //タイマー停止
                    this.timer.cancel();
                    //大量のキャンセル対策
                    this.timer.purge();
                    this.timer = null;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSIONS_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getContentsInfo(now);
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo(int page){
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        //最大値取得
        int maxCount = cursor.getCount();
        if(maxCount > 0){
            if(page <= -1){
                now = maxCount -1;
            }else if( page > maxCount -1){
                now = 0;
            }
            cursor.moveToPosition(now);
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);
                imageView =(ImageView)findViewById(R.id.imageview);
                imageView.setImageURI(imageUri);
        }else {
            Toast.makeText(this,"画像がありません",Toast.LENGTH_SHORT).show();
        }
//        if(cursor.moveToFirst()){
//            do{
//                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
//                Long id = cursor.getLong(fieldIndex);
//                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);
//                imageView =(ImageView)findViewById(R.id.imageview);
//                imageView.setImageURI(imageUri);
//            }while (cursor.moveToNext());
//        }
        cursor.close();
    }

    public class MyTimer extends TimerTask{
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //自動送り
                    getContentsInfo(now);
                    now = now+1;
                }
            });
        }
    }
}
