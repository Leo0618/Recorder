package com.leo618.librecorder;

/**
 * function:视频录制配置信息
 *
 * <p>
 * Created by Leo on 2017/11/29.
 */
public class RecorderConfig {

    /**
     * 是否打开时使用前置摄像头,默认打开后置摄像头
     */
    public boolean useCameraFront = false;

    /**
     * 视频分辨率质量，可设置如下：<p>
     * <p>{@link Recorder#QUALITY_LOW}: 可兼容的最低清
     * <p>{@link Recorder#QUALITY_HIGH} :可兼容的最高清
     * <p>{@link Recorder#QUALITY_480P} :480P
     * <p>{@link Recorder#QUALITY_720P} :720P
     * <p>{@link Recorder#QUALITY_1080P} :1080P
     * <p>{@link Recorder#QUALITY_2160P} :2160P
     */
    public int quality;

    /**
     * 视频预览和捕捉画面宽(px)
     */
    public int videoWidth;

    /**
     * 视频预览和捕捉画面高(px)
     */
    public int videoHeight;

    /**
     * 当前屏幕角度: activity.getWindowManager().getDefaultDisplay().getRotation()
     */
    public int screenRotation;

    /**
     * 视频存放目录
     */
    public String videoFileDir;
}
