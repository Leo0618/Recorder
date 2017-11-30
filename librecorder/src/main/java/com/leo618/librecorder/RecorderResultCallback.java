package com.leo618.librecorder;

import java.io.File;

/**
 * function:录制结果回调
 *
 * <p>
 * Created by Leo on 2017/11/29.
 */
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
