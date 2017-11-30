package com.leo618.recorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo618.librecorder.Recorder;
import com.leo618.librecorder.RecorderConfig;
import com.leo618.librecorder.RecorderResultCallback;
import com.leo618.librecorder.RecorderUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Recorder       mRecorder;
    private RecorderConfig mRecorderConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecorder = Recorder.instance(getApplicationContext());
        mRecorderConfig = new RecorderConfig();
        mRecorderConfig.useCameraFront = false;
        mRecorderConfig.quality = Recorder.QUALITY_480P;
        mRecorderConfig.videoWidth = 320;
        mRecorderConfig.videoHeight = 480;
        mRecorderConfig.screenRotation = getWindowManager().getDefaultDisplay().getRotation();
        mRecorderConfig.videoFileDir = getExternalCacheDir().getAbsolutePath();
        mRecorder.setRecorderConfig(mRecorderConfig);
        mRecorder.setRecorderResultCallback(mCallback);

        final SurfaceView mSurface = (SurfaceView) findViewById(R.id.mSurface);
        mSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mRecorder.startPreview(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mRecorder.stopPreview();
            }
        });
    }


    //开始录制
    public void startRecord(View view) {
        mRecorder.startRecord();
    }

    //停止录制
    public void stopRecord(View view) {
        mRecorder.stopRecord();
    }

    //录制回调
    private RecorderResultCallback mCallback = new RecorderResultCallback() {
        @Override
        public void onStart() {
            recordEnd = false;
            runTime();
        }

        @Override
        public void onError(String errorMsg) {
            Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResult(File videoFile) {
            recordEnd = true;
            final String mPath   = videoFile.getAbsolutePath();
            ImageView    imgView = (ImageView) findViewById(R.id.img);
            imgView.setImageBitmap(RecorderUtil.getVideoThumbnail(mPath, 320, 480));
            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, VideoPlayActivity.class);
                    intent.putExtra("path", mPath);
                    startActivity(intent);
                }
            });
        }
    };

    private boolean recordEnd;
    private long mTime = 0;
    private TextView mTimeUI;

    private void runTime() {
        mTimeUI = (TextView) findViewById(R.id.mTimeUI);
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                if (recordEnd) {
                    getWindow().getDecorView().removeCallbacks(this);
                    mTimeUI.setText("");
                    mTime = 0;
                } else {
                    mTimeUI.setText(RecorderUtil.formatDuration(mTime));
                    getWindow().getDecorView().postDelayed(this, 1000);
                    mTime++;
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecorder.onDestroy();
    }

    public void videoListPage(View view) {
        startActivity(new Intent(this, VideoListActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemLOW:
                mRecorderConfig.quality = Recorder.QUALITY_LOW;
                break;
            case R.id.menuItemHIGH:
                mRecorderConfig.quality = Recorder.QUALITY_HIGH;
                break;
            case R.id.menuItemQCIF:
                mRecorderConfig.quality = Recorder.QUALITY_QCIF;
                break;
            case R.id.menuItemCIF:
                mRecorderConfig.quality = Recorder.QUALITY_CIF;
                break;
            case R.id.menuItem480P:
                mRecorderConfig.quality = Recorder.QUALITY_480P;
                break;
            case R.id.menuItem720P:
                mRecorderConfig.quality = Recorder.QUALITY_720P;
                break;
            case R.id.menuItem1080P:
                mRecorderConfig.quality = Recorder.QUALITY_1080P;
                break;
            case R.id.menuItemQVGA:
                mRecorderConfig.quality = Recorder.QUALITY_QVGA;
                break;
            case R.id.menuItem2160P:
                mRecorderConfig.quality = Recorder.QUALITY_2160P;
                break;
        }
        mRecorder.setRecorderConfig(mRecorderConfig);
        return true;
    }
}
