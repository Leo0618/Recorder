package com.leo618.librecorder;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * function:
 *
 * <p>
 * Created by Leo on 2017/11/24.
 */
@SuppressWarnings({"deprecation", "WeakerAccess", "ResultOfMethodCallIgnored", "unused"})
public class Recorder {
    private static final AtomicReference<Recorder> INSTANCE = new AtomicReference<>();

    /** 获取实例，建议使用Application上下文 */
    public static Recorder instance(Context context) {
        for (; ; ) {
            Recorder instance = INSTANCE.get();
            if (instance != null) return instance;
            instance = new Recorder(context);
            if (INSTANCE.compareAndSet(null, instance)) return instance;
        }
    }

    private Recorder(Context context) {
        mAppContext = context;
        int[] availableCameras = RecorderUtil.findAvailableCameras();
        CAMERA_FRONT_POSITION = availableCameras[0];
        CAMERA_POST_POSITION = availableCameras[1];
    }

    /** 设置录制配置信息 */
    public void setRecorderConfig(RecorderConfig config) {
        mRecorderConfig = config;
    }

    private Context        mAppContext;
    private RecorderConfig mRecorderConfig;
    private Camera         mCamera;
    private int mCurrentSelectedCameraId = -1;
    private int CAMERA_POST_POSITION     = -1;
    private int CAMERA_FRONT_POSITION    = -1;
    private SurfaceHolder          mHolder;
    private RecorderResultCallback mCallback;

    //设置回调
    public void setRecorderResultCallback(RecorderResultCallback callback) {
        mCallback = callback;
    }

    //切换摄像头
    public void switchCamera() {
        if (mHolder == null) return;
        mCurrentSelectedCameraId = mCurrentSelectedCameraId == CAMERA_POST_POSITION ? CAMERA_FRONT_POSITION : CAMERA_POST_POSITION;
        stopPreview();
        startPreview(mHolder);
    }

    //启动预览
    public void startPreview(SurfaceHolder holder) {
        RecorderLog.d("startPreview");
        if (!RecorderUtil.hasCameraPermission(mAppContext)) {
            RecorderLog.e("不具有摄像头使用权限");
            if (mCallback != null) mCallback.onError("未获取到摄像权限");
            return;
        }
        try {
            RecorderUtil.checkRecorderConfig(mRecorderConfig);
            if (mCurrentSelectedCameraId == -1) {
                mCurrentSelectedCameraId = mRecorderConfig.useCameraFront ? CAMERA_FRONT_POSITION : CAMERA_POST_POSITION;
            }
            mHolder = holder;
            mCamera = Camera.open(mCurrentSelectedCameraId);
            Camera.Parameters mParams = mCamera.getParameters();
            mParams.setRecordingHint(true);//设置RecordingHint可以加快录制启动速度,部分手机会使预览效果拉伸,录制正常
            RecorderUtil.setCameraDisplayOrientation(mRecorderConfig.screenRotation, mCurrentSelectedCameraId, mCamera);//浏览角度
            List<String> focusModes = mParams.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            List<String> whiteBalance = mParams.getSupportedWhiteBalance();
            if (whiteBalance.contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
                mParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            }
            mParams.setMeteringAreas(null);
            int[] bestPreviewWH = RecorderUtil.getBestPreviewWH(mRecorderConfig.videoWidth, mRecorderConfig.videoHeight, mParams.getSupportedPreviewSizes());
            mParams.setPreviewSize(bestPreviewWH[0], bestPreviewWH[1]);
            mCamera.setParameters(mParams);
            if (!focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                mCamera.cancelAutoFocus();
            }
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            RecorderLog.e("startPreview Exception. msg:" + e.getMessage());
            if (mCallback != null) mCallback.onError("摄像预览启动失败");
        }
    }

    //停止预览
    public void stopPreview() {
        RecorderLog.d("stopPreview");
        try {
            if (null != mCamera) {
                mCamera.stopPreview();
                mCamera.setPreviewDisplay(null); //这句要在stopPreview后执行，不然会卡顿或者花屏
                mCamera.release();
                mCamera = null;
            }
            resetMediaRecorder();
        } catch (Exception e) {
            RecorderLog.d("stopPreview Exception. msg:" + e.getMessage());
            if (mCallback != null) mCallback.onError("摄像预览关闭失败");
        }
    }


    private MediaRecorder mMediaRecorder;
    private          String  mCurrentFilePath = "";
    private volatile boolean mRecording       = false;

    //开始录制
    public void startRecord() {
        if (mRecording) return;
        try {
            if (mMediaRecorder == null) mMediaRecorder = new MediaRecorder();
            mMediaRecorder.reset();
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);

            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            mMediaRecorder.setProfile(getProfileUsable(mRecorderConfig.quality));
            //int[] bestVideoWH = RecorderUtil.getBestVideoWH(mParams.getPreviewSize(), mParams.getSupportedVideoSizes());
            //mMediaRecorder.setVideoSize(bestVideoWH[0], bestVideoWH[1]);

            mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
            if (mCurrentSelectedCameraId == CAMERA_POST_POSITION) {
                mMediaRecorder.setOrientationHint(90);
            } else {
                mMediaRecorder.setOrientationHint(270);
            }
            String mDir = mRecorderConfig.videoFileDir;
            if (!mDir.endsWith(File.separator)) mDir += File.separator;
            mCurrentFilePath = mDir + String.valueOf(System.currentTimeMillis()) + ".mp4";
            RecorderUtil.createFileIfNotExist(mCurrentFilePath);
            mMediaRecorder.setOutputFile(mCurrentFilePath);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mRecording = true;
            if (mCallback != null) mCallback.onStart();
        } catch (Exception e) {
            File file = new File(mCurrentFilePath);
            if (file.exists()) file.delete();
            RecorderLog.e("startRecord Exception. msg:" + e.getMessage());
            if (mCallback != null) mCallback.onError("视频录制启动失败");
        }
    }

    //停止录制
    public void stopRecord() {
        if (!mRecording) return;
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.setPreviewDisplay(null);
                mMediaRecorder.stop();
                if (mCallback != null) mCallback.onResult(new File(mCurrentFilePath));
            }
        } catch (Exception e) {
            RecorderLog.e("stopRecord Exception. msg:" + e.getMessage());
            if (mCallback != null) mCallback.onError("视频录制停止失败");
        } finally {
            mRecording = false;
        }
    }

    private void resetMediaRecorder() {
        mRecording = false;
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.setPreviewDisplay(null);
                mMediaRecorder.stop();
            }
        } catch (Exception e) {
            RecorderLog.d("resetMediaRecorder Exception. msg:" + e.getMessage());
        }
    }

    //资源释放
    public void onDestroy() {
        stopPreview();
        mCallback = null;
        mHolder = null;
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * 返回当前指定分辨率参数信息，无合适的则返回可以的最好模式
     */
    @NonNull
    public CamcorderProfile getProfileUsable(int quality) {
        if (CamcorderProfile.hasProfile(mCurrentSelectedCameraId, quality)) {
            return CamcorderProfile.get(mCurrentSelectedCameraId, quality);
        }
        return CamcorderProfile.get(mCurrentSelectedCameraId, QUALITY_HIGH);
    }

    /**
     * 可兼容的最低清
     */
    public static final int QUALITY_LOW  = 0;
    /**
     * 可兼容的最高清
     */
    public static final int QUALITY_HIGH = 1;

    /**
     * QCIF (176 x 144)
     */
    public static final int QUALITY_QCIF = 2;

    /**
     * CIF (352 x 288)
     */
    public static final int QUALITY_CIF   = 3;
    /**
     * 480P (720 x 480)
     */
    public static final int QUALITY_480P  = 4;
    /**
     * 720p (1280 x 720)
     */
    public static final int QUALITY_720P  = 5;
    /**
     * 1080p (1920 x 1080)
     */
    public static final int QUALITY_1080P = 6;
    /**
     * QVGA (320x240)
     */
    public static final int QUALITY_QVGA  = 7;
    /**
     * 2160p (3840x2160)
     */
    public static final int QUALITY_2160P = 8;

}
