package com.leo618.recorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.leo618.splashpermissionsauth.SplashAuthUI;

/**
 * function:
 *
 * <p>
 * Created by Leo on 2017/11/30.
 */
public class SplashActivity extends AppCompatActivity {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashAuthUI.launch(SplashActivity.this, 100, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(this, MainActivity.class));
            }
        }
        finish();
    }
}
