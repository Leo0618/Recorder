# Recorder
video record by android system API. 使用系统API录制视频.细节查看DEMO


![](https://i.imgur.com/PBEqYrw.png)


## Step0： 获取实例 初始化配置信息 设置回调 ##

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


## Step1： Surface创建成功后启动预览 销毁时停止预览 ##

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


## Step2： 开始录制，停止录制 ##

	mRecorder.startRecord();
	mRecorder.stopRecord();


## Step3： 回调中处理结果 ##
	
	public interface RecorderResultCallback {
	    /**
	     * 录制开始
	     */
	    void onStart();
	
	    /**
	     * 错误信息反馈
	     *
	     * @param errorMsg 错误信息
	     */
	    void onError(String errorMsg);
	
	    /**
	     * 录制完成，返回录制的视频文件
	     *
	     * @param videoFile 已经保存的视频文件
	     */
	    void onResult(File videoFile);
	}

## PS： 预支持的视频分辨率常量，Recorder.xxx ##

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

## 混淆规则 ##

	-dontwarn  com.leo618.librecorder.**
	-keep public class com.leo618.librecorder.Recorder
	-keep public class com.leo618.librecorder.RecorderResultCallback
	-keepclasseswithmembernames class com.leo618.librecorder.** {
	       public *;
	}