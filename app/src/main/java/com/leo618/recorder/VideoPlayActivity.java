package com.leo618.recorder;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.leo618.librecorder.RecorderUtil;

import java.io.IOException;

/**
 * function:
 *
 * <p></p>
 * Created by lzj on 2017/7/11.
 */
@SuppressLint("SetTextI18n")
public class VideoPlayActivity extends AppCompatActivity {
    private TextureView textureView;
    private TextView    durationProgress;
    private TextView    duratioTotal;

    private String      mVideoPath;
    private MediaPlayer mMediaPlayer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_play);
        textureView = (TextureView) findViewById(R.id.textureView);
        durationProgress = (TextView) findViewById(R.id.durationProgress);
        duratioTotal = (TextView) findViewById(R.id.duratioTotal);
        mVideoPath = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(mVideoPath)) {
            finish();
            return;
        }
        textureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            initVideo();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            textureView = null;
            stopPlay();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    private void initVideo() {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mVideoPath);
            mMediaPlayer.setSurface(new Surface(textureView.getSurfaceTexture()));
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    startPlay();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlay();
                    finish();
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    finish();
                    return false;
                }
            });
            mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    if (mMediaPlayer == null || textureView == null) return;
                    if (width > 0 && height > 0 && width >= height) {
                        int                      heightFinal  = (height / width) * getResources().getDisplayMetrics().widthPixels;
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textureView.getLayoutParams();
                        layoutParams.height = heightFinal;
                        layoutParams.gravity = Gravity.CENTER;
                        textureView.setLayoutParams(layoutParams);
                    }
                }
            });
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    private void startPlay() {
        if (mMediaPlayer == null) return;
        try {
            mMediaPlayer.start();
            duratioTotal.setText(RecorderUtil.formatDuration(mMediaPlayer.getDuration() / 1000));
            getWindow().getDecorView().post(mProgressRunnable);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (durationProgress == null) {
                    getWindow().getDecorView().removeCallbacks(mProgressRunnable);
                    return;
                }
                durationProgress.setText(RecorderUtil.formatDuration(mMediaPlayer.getCurrentPosition() / 1000));
                getWindow().getDecorView().postDelayed(mProgressRunnable, 200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void stopPlay() {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().getDecorView().removeCallbacks(mProgressRunnable);
        mProgressRunnable = null;
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
